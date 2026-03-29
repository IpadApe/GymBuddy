package com.gymtracker.ui.screens.gymmap

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.LocationOff
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import java.net.URLEncoder
import java.util.concurrent.TimeUnit

// ─── Data models ──────────────────────────────────────────────────────────────

data class GymPlace(
    val name: String,
    val address: String,
    val lat: Double,
    val lng: Double
)

data class GeocodedPlace(
    val displayName: String,
    val shortName: String,
    val lat: Double,
    val lng: Double
)

// ─── UI state ─────────────────────────────────────────────────────────────────

sealed class GymMapUiState {
    object Loading : GymMapUiState()
    object PermissionDenied : GymMapUiState()
    data class Error(val message: String) : GymMapUiState()
    data class Success(
        val centerLat: Double,
        val centerLng: Double,
        val gyms: List<GymPlace>
    ) : GymMapUiState()
}

// ─── ViewModel ────────────────────────────────────────────────────────────────

@OptIn(FlowPreview::class)
class GymMapViewModel(private val context: Context) : ViewModel() {

    private val _uiState = MutableStateFlow<GymMapUiState>(GymMapUiState.Loading)
    val uiState: StateFlow<GymMapUiState> = _uiState

    // Search bar text
    val searchQuery = MutableStateFlow("")

    // Nominatim suggestions
    private val _searchResults = MutableStateFlow<List<GeocodedPlace>>(emptyList())
    val searchResults: StateFlow<List<GeocodedPlace>> = _searchResults

    private val _isSearching = MutableStateFlow(false)
    val isSearching: StateFlow<Boolean> = _isSearching

    // Remember GPS coords so the FAB can always snap back
    private var gpsLat = 0.0
    private var gpsLng = 0.0

    private val httpClient = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(35, TimeUnit.SECONDS)
        .writeTimeout(15, TimeUnit.SECONDS)
        .build()

    private val overpassServers = listOf(
        "https://overpass-api.de/api/interpreter",
        "https://overpass.kumi.systems/api/interpreter",
        "https://overpass.openstreetmap.ru/api/interpreter"
    )

    // ── Location permission flow ─────────────────────────────────────────────

    @SuppressLint("MissingPermission")
    fun loadNearbyGyms() {
        viewModelScope.launch {
            _uiState.value = GymMapUiState.Loading
            try {
                val fused = LocationServices.getFusedLocationProviderClient(context)
                val loc = fused.lastLocation.await() ?: run {
                    _uiState.value = GymMapUiState.Error(
                        "Could not get your current location.\nMake sure GPS is enabled and try again."
                    )
                    return@launch
                }
                gpsLat = loc.latitude
                gpsLng = loc.longitude
                val gyms = fetchNearbyGyms(gpsLat, gpsLng)
                _uiState.value = GymMapUiState.Success(gpsLat, gpsLng, gyms)
            } catch (e: Exception) {
                _uiState.value = GymMapUiState.Error(e.message ?: "Unknown error occurred")
            }
        }
    }

    fun onPermissionDenied() {
        _uiState.value = GymMapUiState.PermissionDenied
    }

    // ── Search (Nominatim geocoding) ─────────────────────────────────────────

    fun searchPlaces(query: String) {
        if (query.isBlank()) {
            _searchResults.value = emptyList()
            return
        }
        viewModelScope.launch {
            _isSearching.value = true
            try {
                _searchResults.value = geocode(query)
            } catch (_: Exception) {
                _searchResults.value = emptyList()
            } finally {
                _isSearching.value = false
            }
        }
    }

    fun selectPlace(place: GeocodedPlace) {
        searchQuery.value = place.shortName
        _searchResults.value = emptyList()
        viewModelScope.launch {
            _uiState.value = GymMapUiState.Loading
            try {
                val gyms = fetchNearbyGyms(place.lat, place.lng)
                _uiState.value = GymMapUiState.Success(place.lat, place.lng, gyms)
            } catch (e: Exception) {
                _uiState.value = GymMapUiState.Error(e.message ?: "Search failed")
            }
        }
    }

    fun clearSearch() {
        searchQuery.value = ""
        _searchResults.value = emptyList()
    }

    fun returnToGps() {
        if (gpsLat == 0.0 && gpsLng == 0.0) {
            loadNearbyGyms()
        } else {
            clearSearch()
            viewModelScope.launch {
                _uiState.value = GymMapUiState.Loading
                try {
                    val gyms = fetchNearbyGyms(gpsLat, gpsLng)
                    _uiState.value = GymMapUiState.Success(gpsLat, gpsLng, gyms)
                } catch (e: Exception) {
                    _uiState.value = GymMapUiState.Error(e.message ?: "Unknown error")
                }
            }
        }
    }

    // ── Nominatim geocoding ──────────────────────────────────────────────────

    private suspend fun geocode(query: String): List<GeocodedPlace> =
        withContext(Dispatchers.IO) {
            val encoded = URLEncoder.encode(query, "UTF-8")
            val url = "https://nominatim.openstreetmap.org/search" +
                "?q=$encoded&format=json&limit=5&addressdetails=0"
            val request = Request.Builder()
                .url(url)
                .header("User-Agent", context.packageName)
                .get()
                .build()
            val response = httpClient.newCall(request).execute()
            val body = response.body?.string() ?: return@withContext emptyList()
            parseNominatim(body)
        }

    private fun parseNominatim(json: String): List<GeocodedPlace> {
        return try {
            val arr = JSONArray(json)
            buildList {
                for (i in 0 until arr.length()) {
                    val obj = arr.getJSONObject(i)
                    val full = obj.getString("display_name")
                    val short = full.split(",").take(2).joinToString(", ").trim()
                    add(
                        GeocodedPlace(
                            displayName = full,
                            shortName = short,
                            lat = obj.getString("lat").toDouble(),
                            lng = obj.getString("lon").toDouble()
                        )
                    )
                }
            }
        } catch (_: Exception) {
            emptyList()
        }
    }

    // ── Overpass gym search ──────────────────────────────────────────────────

    private suspend fun fetchNearbyGyms(lat: Double, lng: Double): List<GymPlace> =
        withContext(Dispatchers.IO) {
            /*
             * Strategy — three complementary passes, all within 7 km:
             *
             * 1. Tag-based (standard OSM keys):
             *    leisure = fitness_centre | health_club
             *    amenity = gym | fitness_centre
             *    sport   = fitness | bodybuilding | crossfit | weightlifting
             *              (regex so "fitness;yoga", "fitness;crossfit" also match)
             *
             * 2. Sports-centre sub-filter:
             *    leisure=sports_centre that explicitly lists a fitness sport
             *    (avoids picking up football clubs, swimming pools, etc.)
             *
             * 3. Name catch-all:
             *    Picks up gyms that only have a name tag but no proper
             *    leisure/sport tag — e.g. "GymRat", "Arena Gym", "Max Fitness",
             *    "CrossFit Praha", "Posilovna u Nováků".
             *
             * nwr = node + way + relation in one shot.
             * out center tags = gives a lat/lng centre for ways & relations.
             */
            val query = """
                [out:json][timeout:30];
                (
                  nwr["leisure"~"^(fitness_centre|health_club)${'$'}"](around:7000,$lat,$lng);
                  nwr["amenity"~"^(gym|fitness_centre)${'$'}"](around:7000,$lat,$lng);
                  nwr["sport"~"fitness|bodybuilding|crossfit|weightlifting"](around:7000,$lat,$lng);
                  nwr["leisure"="sports_centre"]["sport"~"fitness|bodybuilding|crossfit"](around:7000,$lat,$lng);
                  nwr["name"~"gym|fitness|posilovna|crossfit|fitcentrum|fitnes",i](around:7000,$lat,$lng);
                );
                out center tags;
            """.trimIndent()

            val body = query.toRequestBody("text/plain".toMediaType())
            var lastError: Exception? = null
            for (server in overpassServers) {
                try {
                    val req = Request.Builder().url(server).post(body).build()
                    val resp = httpClient.newCall(req).execute()
                    if (!resp.isSuccessful) continue
                    val json = resp.body?.string() ?: continue
                    return@withContext parseOverpass(json)
                } catch (e: Exception) {
                    lastError = e
                }
            }
            throw lastError ?: Exception("All Overpass servers failed")
        }

    private fun parseOverpass(json: String): List<GymPlace> {
        return try {
            val elements = JSONObject(json).optJSONArray("elements") ?: return emptyList()
            val seen = mutableSetOf<String>()
            buildList {
                for (i in 0 until elements.length()) {
                    val el = elements.getJSONObject(i)
                    val tags = el.optJSONObject("tags") ?: continue

                    // Prefer explicit name, fall back to brand, then derive from tag
                    val name = tags.optString("name").takeIf { it.isNotBlank() }
                        ?: tags.optString("brand").takeIf { it.isNotBlank() }
                        ?: tags.optString("operator").takeIf { it.isNotBlank() }
                        ?: inferName(tags)
                        ?: continue

                    if (!seen.add(name.lowercase().trim())) continue

                    val address = buildAddress(tags)
                    val (elLat, elLng) = if (el.has("lat")) {
                        el.getDouble("lat") to el.getDouble("lon")
                    } else {
                        val c = el.optJSONObject("center") ?: continue
                        c.getDouble("lat") to c.getDouble("lon")
                    }
                    add(GymPlace(name, address, elLat, elLng))
                }
            }
        } catch (_: Exception) {
            emptyList()
        }
    }

    /** Derive a display name for tagged-but-unnamed elements. */
    private fun inferName(tags: JSONObject): String? = when {
        tags.optString("leisure").contains("fitness") -> "Fitness Center"
        tags.optString("amenity") == "gym"            -> "Gym"
        tags.optString("sport").contains("crossfit")  -> "CrossFit"
        tags.optString("sport").contains("fitness")   -> "Fitness Center"
        tags.optString("sport").contains("bodybuilding") -> "Gym"
        else -> null
    }

    private fun buildAddress(tags: JSONObject): String {
        val street = tags.optString("addr:street")
        val house = tags.optString("addr:housenumber")
        val city = tags.optString("addr:city")
        return listOf(
            if (street.isNotBlank() && house.isNotBlank()) "$street $house"
            else street.takeIf { it.isNotBlank() },
            city.takeIf { it.isNotBlank() }
        ).filterNotNull().joinToString(", ").ifBlank { "No address available" }
    }
}

class GymMapViewModelFactory(private val context: Context) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        @Suppress("UNCHECKED_CAST")
        return GymMapViewModel(context.applicationContext) as T
    }
}

// ─── Screen root ──────────────────────────────────────────────────────────────

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun GymMapScreen(
    viewModel: GymMapViewModel = viewModel(factory = GymMapViewModelFactory(LocalContext.current))
) {
    val uiState by viewModel.uiState.collectAsState()

    val locationPermission = rememberPermissionState(
        permission = Manifest.permission.ACCESS_FINE_LOCATION
    ) { granted ->
        if (granted) viewModel.loadNearbyGyms() else viewModel.onPermissionDenied()
    }

    LaunchedEffect(Unit) {
        if (locationPermission.status.isGranted) viewModel.loadNearbyGyms()
        else locationPermission.launchPermissionRequest()
    }

    when (val state = uiState) {
        is GymMapUiState.Loading       -> MapLoadingContent()
        is GymMapUiState.PermissionDenied -> MapPermissionDeniedContent(
            onRetry = { locationPermission.launchPermissionRequest() }
        )
        is GymMapUiState.Error         -> MapErrorContent(state.message) { viewModel.loadNearbyGyms() }
        is GymMapUiState.Success       -> GymMapContent(state, viewModel)
    }
}

// ─── Loading ──────────────────────────────────────────────────────────────────

@Composable
private fun MapLoadingContent() {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            Text("Finding nearby gyms…", style = MaterialTheme.typography.bodyMedium)
        }
    }
}

// ─── Permission denied ────────────────────────────────────────────────────────

@Composable
private fun MapPermissionDeniedContent(onRetry: () -> Unit) {
    Box(Modifier.fillMaxSize().padding(32.dp), contentAlignment = Alignment.Center) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Icon(Icons.Filled.LocationOff, null, Modifier.size(56.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant)
            Text("Location Permission Needed",
                style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Text(
                "GymMap needs your location to show nearby gyms.",
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Button(onClick = onRetry, shape = RoundedCornerShape(10.dp)) { Text("Grant Permission") }
        }
    }
}

// ─── Error ────────────────────────────────────────────────────────────────────

@Composable
private fun MapErrorContent(message: String, onRetry: () -> Unit) {
    Box(Modifier.fillMaxSize().padding(32.dp), contentAlignment = Alignment.Center) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Icon(Icons.Filled.LocationOff, null, Modifier.size(56.dp),
                tint = MaterialTheme.colorScheme.error)
            Text("Could Not Load Map",
                style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Text(message, style = MaterialTheme.typography.bodySmall,
                textAlign = TextAlign.Center, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Button(onClick = onRetry, shape = RoundedCornerShape(10.dp)) { Text("Retry") }
        }
    }
}

// ─── Map + search overlay ────────────────────────────────────────────────────

@SuppressLint("MissingPermission")
@Composable
private fun GymMapContent(state: GymMapUiState.Success, viewModel: GymMapViewModel) {
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current

    val searchQuery by viewModel.searchQuery.collectAsState()
    val searchResults by viewModel.searchResults.collectAsState()
    val isSearching by viewModel.isSearching.collectAsState()

    // Build MapView once
    val mapView = remember {
        Configuration.getInstance().userAgentValue = context.packageName
        MapView(context).apply {
            setTileSource(TileSourceFactory.MAPNIK)
            setMultiTouchControls(true)
            controller.setZoom(14.0)
            controller.setCenter(GeoPoint(state.centerLat, state.centerLng))
        }
    }

    // Animate camera whenever the center location changes (GPS load or search result)
    LaunchedEffect(state.centerLat, state.centerLng) {
        mapView.controller.animateTo(GeoPoint(state.centerLat, state.centerLng))
        mapView.controller.zoomTo(14.0, 500L)
    }

    // Refresh markers whenever the gym list changes
    LaunchedEffect(state.gyms) {
        mapView.overlays.clear()
        state.gyms.forEach { gym ->
            mapView.overlays.add(
                Marker(mapView).apply {
                    position = GeoPoint(gym.lat, gym.lng)
                    title = gym.name
                    snippet = gym.address
                    setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                }
            )
        }
        mapView.invalidate()
    }

    DisposableEffect(Unit) {
        mapView.onResume()
        onDispose { mapView.onPause() }
    }

    Box(Modifier.fillMaxSize()) {

        // ── Map ──────────────────────────────────────────────────────────────
        AndroidView(factory = { mapView }, modifier = Modifier.fillMaxSize())

        // ── Search bar + suggestions ──────────────────────────────────────────
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp)
                .align(Alignment.TopCenter)
        ) {
            // Search field
            Card(
                shape = RoundedCornerShape(14.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (isSearching) {
                        CircularProgressIndicator(
                            modifier = Modifier
                                .padding(start = 12.dp)
                                .size(20.dp),
                            strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.primary
                        )
                    } else {
                        Icon(
                            Icons.Filled.Search, null,
                            modifier = Modifier.padding(start = 12.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    TextField(
                        value = searchQuery,
                        onValueChange = { viewModel.searchQuery.value = it },
                        placeholder = {
                            Text(
                                "Search city or area…",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        },
                        singleLine = true,
                        modifier = Modifier
                            .weight(1f)
                            .onFocusChanged { /* keep results visible while focused */ },
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = MaterialTheme.colorScheme.surface,
                            unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                            focusedIndicatorColor = androidx.compose.ui.graphics.Color.Transparent,
                            unfocusedIndicatorColor = androidx.compose.ui.graphics.Color.Transparent
                        ),
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                        keyboardActions = KeyboardActions(onSearch = {
                            focusManager.clearFocus()
                            viewModel.searchPlaces(searchQuery)
                        })
                    )
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = {
                            viewModel.clearSearch()
                            focusManager.clearFocus()
                        }) {
                            Icon(Icons.Filled.Close, "Clear search",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }

            // Suggestions dropdown
            if (searchResults.isNotEmpty()) {
                Spacer(Modifier.height(4.dp))
                Card(
                    shape = RoundedCornerShape(14.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    LazyColumn {
                        items(searchResults) { place ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        focusManager.clearFocus()
                                        viewModel.selectPlace(place)
                                    }
                                    .padding(horizontal = 16.dp, vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Icon(
                                    Icons.Filled.Search, null,
                                    modifier = Modifier.size(18.dp),
                                    tint = MaterialTheme.colorScheme.primary
                                )
                                Column {
                                    Text(
                                        place.shortName,
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Medium,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        place.displayName,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                            if (searchResults.last() != place) {
                                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                            }
                        }
                    }
                }
            }
        }

        // ── Gyms count badge (only when search bar has no results open) ───────
        if (state.gyms.isNotEmpty() && searchResults.isEmpty()) {
            Surface(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 76.dp),
                shape = RoundedCornerShape(20.dp),
                color = MaterialTheme.colorScheme.primaryContainer,
                tonalElevation = 4.dp
            ) {
                Text(
                    text = "${state.gyms.size} gym${if (state.gyms.size == 1) "" else "s"} within 5 km",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 7.dp)
                )
            }
        }

        // ── Re-center FAB ─────────────────────────────────────────────────────
        FloatingActionButton(
            onClick = {
                focusManager.clearFocus()
                viewModel.returnToGps()
            },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp),
            containerColor = MaterialTheme.colorScheme.primary
        ) {
            Icon(Icons.Filled.MyLocation, contentDescription = "Return to my location")
        }
    }
}
