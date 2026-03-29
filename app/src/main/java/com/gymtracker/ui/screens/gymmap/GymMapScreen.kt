package com.gymtracker.ui.screens.gymmap

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOff
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
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
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker

// ─── Data model ───────────────────────────────────────────────────────────────

data class GymPlace(
    val name: String,
    val address: String,
    val lat: Double,
    val lng: Double
)

// ─── UI state ─────────────────────────────────────────────────────────────────

sealed class GymMapUiState {
    object Loading : GymMapUiState()
    object PermissionDenied : GymMapUiState()
    data class Error(val message: String) : GymMapUiState()
    data class Success(
        val userLat: Double,
        val userLng: Double,
        val gyms: List<GymPlace>
    ) : GymMapUiState()
}

// ─── ViewModel ────────────────────────────────────────────────────────────────

class GymMapViewModel(private val context: Context) : ViewModel() {
    private val _uiState = MutableStateFlow<GymMapUiState>(GymMapUiState.Loading)
    val uiState: StateFlow<GymMapUiState> = _uiState

    private val httpClient = OkHttpClient()

    @SuppressLint("MissingPermission")
    fun loadNearbyGyms() {
        viewModelScope.launch {
            _uiState.value = GymMapUiState.Loading
            try {
                val fusedClient = LocationServices.getFusedLocationProviderClient(context)
                val location = fusedClient.lastLocation.await()
                if (location == null) {
                    _uiState.value = GymMapUiState.Error(
                        "Could not get your current location.\nMake sure GPS is enabled and try again."
                    )
                    return@launch
                }
                val gyms = fetchNearbyGyms(location.latitude, location.longitude)
                _uiState.value = GymMapUiState.Success(location.latitude, location.longitude, gyms)
            } catch (e: Exception) {
                _uiState.value = GymMapUiState.Error(e.message ?: "Unknown error occurred")
            }
        }
    }

    fun onPermissionDenied() {
        _uiState.value = GymMapUiState.PermissionDenied
    }

    /** Overpass API — free, no key required, uses OpenStreetMap data. */
    private suspend fun fetchNearbyGyms(lat: Double, lng: Double): List<GymPlace> =
        withContext(Dispatchers.IO) {
            val query = """
                [out:json][timeout:15];
                (
                  node["leisure"="fitness_centre"](around:5000,$lat,$lng);
                  node["amenity"="gym"](around:5000,$lat,$lng);
                  way["leisure"="fitness_centre"](around:5000,$lat,$lng);
                  way["amenity"="gym"](around:5000,$lat,$lng);
                );
                out center;
            """.trimIndent()

            val body = query.toRequestBody("text/plain".toMediaType())
            val request = Request.Builder()
                .url("https://overpass-api.de/api/interpreter")
                .post(body)
                .build()

            val response = httpClient.newCall(request).execute()
            val json = response.body?.string() ?: return@withContext emptyList()
            parseOverpassResponse(json)
        }

    private fun parseOverpassResponse(json: String): List<GymPlace> {
        return try {
            val elements = JSONObject(json).optJSONArray("elements") ?: return emptyList()
            buildList {
                for (i in 0 until elements.length()) {
                    val el = elements.getJSONObject(i)
                    val tags = el.optJSONObject("tags") ?: continue
                    val name = tags.optString("name").takeIf { it.isNotBlank() } ?: "Gym"
                    val address = buildAddress(tags)

                    // nodes have lat/lon directly; ways have a "center" object
                    val lat: Double
                    val lng: Double
                    if (el.has("lat")) {
                        lat = el.getDouble("lat")
                        lng = el.getDouble("lon")
                    } else {
                        val center = el.optJSONObject("center") ?: continue
                        lat = center.getDouble("lat")
                        lng = center.getDouble("lon")
                    }
                    add(GymPlace(name, address, lat, lng))
                }
            }
        } catch (e: Exception) {
            emptyList()
        }
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

// ─── Screen ───────────────────────────────────────────────────────────────────

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun GymMapScreen(
    viewModel: GymMapViewModel = viewModel(factory = GymMapViewModelFactory(LocalContext.current))
) {
    val uiState by viewModel.uiState.collectAsState()

    val locationPermission = rememberPermissionState(
        permission = Manifest.permission.ACCESS_FINE_LOCATION
    ) { isGranted ->
        if (isGranted) viewModel.loadNearbyGyms() else viewModel.onPermissionDenied()
    }

    LaunchedEffect(Unit) {
        if (locationPermission.status.isGranted) {
            viewModel.loadNearbyGyms()
        } else {
            locationPermission.launchPermissionRequest()
        }
    }

    when (val state = uiState) {
        is GymMapUiState.Loading -> MapLoadingContent()
        is GymMapUiState.PermissionDenied -> MapPermissionDeniedContent(
            onRetry = { locationPermission.launchPermissionRequest() }
        )
        is GymMapUiState.Error -> MapErrorContent(
            message = state.message,
            onRetry = { viewModel.loadNearbyGyms() }
        )
        is GymMapUiState.Success -> GymMapContent(state = state)
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
            Icon(
                Icons.Filled.LocationOff, null,
                modifier = Modifier.size(56.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                "Location Permission Needed",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                "GymMap needs your location to show nearby gyms. " +
                    "Please grant the location permission.",
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Button(onClick = onRetry, shape = RoundedCornerShape(10.dp)) {
                Text("Grant Permission")
            }
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
            Icon(
                Icons.Filled.LocationOff, null,
                modifier = Modifier.size(56.dp),
                tint = MaterialTheme.colorScheme.error
            )
            Text(
                "Could Not Load Map",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                message,
                style = MaterialTheme.typography.bodySmall,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Button(onClick = onRetry, shape = RoundedCornerShape(10.dp)) {
                Text("Retry")
            }
        }
    }
}

// ─── Map content (osmdroid) ───────────────────────────────────────────────────

@Composable
private fun GymMapContent(state: GymMapUiState.Success) {
    val context = LocalContext.current
    val userPoint = remember(state.userLat, state.userLng) {
        GeoPoint(state.userLat, state.userLng)
    }

    // Build MapView once; update markers when gym list changes
    val mapView = remember {
        Configuration.getInstance().userAgentValue = context.packageName
        MapView(context).apply {
            setTileSource(TileSourceFactory.MAPNIK)
            setMultiTouchControls(true)
            controller.setZoom(15.0)
            controller.setCenter(userPoint)
        }
    }

    // Drop markers whenever the gym list is available
    LaunchedEffect(state.gyms) {
        mapView.overlays.clear()
        state.gyms.forEach { gym ->
            val marker = Marker(mapView).apply {
                position = GeoPoint(gym.lat, gym.lng)
                title = gym.name
                snippet = gym.address
                setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
            }
            mapView.overlays.add(marker)
        }
        mapView.invalidate()
    }

    DisposableEffect(Unit) {
        mapView.onResume()
        onDispose { mapView.onPause() }
    }

    Box(Modifier.fillMaxSize()) {
        AndroidView(
            factory = { mapView },
            modifier = Modifier.fillMaxSize()
        )

        // Gyms-found badge
        if (state.gyms.isNotEmpty()) {
            Surface(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 14.dp),
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

        // Re-center FAB
        FloatingActionButton(
            onClick = {
                mapView.controller.animateTo(userPoint)
                mapView.controller.zoomTo(15.0, 500L)
            },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp),
            containerColor = MaterialTheme.colorScheme.primary
        ) {
            Icon(Icons.Filled.MyLocation, contentDescription = "Re-center on my location")
        }
    }
}
