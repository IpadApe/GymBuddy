package com.gymtracker.data.sync

import android.content.Context
import com.gymtracker.data.database.AppDatabase
import com.gymtracker.data.database.entities.ExerciseEntity
import com.gymtracker.data.database.entities.WorkoutExerciseEntity
import com.gymtracker.data.database.entities.WorkoutSessionEntity
import com.gymtracker.data.database.entities.WorkoutSetEntity
import fi.iki.elonen.NanoHTTPD
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.json.JSONObject

class WatchSyncServer(
    private val context: Context,
    private val scope: CoroutineScope
) : NanoHTTPD(8765) {

    private val db by lazy { AppDatabase.getInstance(context) }

    override fun serve(session: IHTTPSession): Response {
        return when {
            session.method == Method.POST && session.uri == "/api/sync-workout" ->
                handleSyncWorkout(session)
            session.method == Method.GET && session.uri == "/ping" ->
                newFixedLengthResponse("pong")
            else ->
                newFixedLengthResponse(Response.Status.NOT_FOUND, MIME_PLAINTEXT, "Not found")
        }
    }

    private fun handleSyncWorkout(session: IHTTPSession): Response {
        return try {
            val body = mutableMapOf<String, String>()
            session.parseBody(body)
            val json = JSONObject(body["postData"] ?: return badRequest("No body"))

            scope.launch(Dispatchers.IO) {
                try {
                    saveWorkoutFromWatch(json)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

            newFixedLengthResponse(Response.Status.OK, "application/json", """{"result":"ok"}""")
        } catch (e: Exception) {
            badRequest(e.message ?: "Parse error")
        }
    }

    private suspend fun saveWorkoutFromWatch(json: JSONObject) {
        val startTime = json.getLong("startTime")
        val endTime = json.getLong("endTime")
        val durationSec = ((endTime - startTime) / 1000).toInt()
        val exercisesArr = json.getJSONArray("exercises")

        var totalVolume = 0.0
        for (i in 0 until exercisesArr.length()) {
            val ex = exercisesArr.getJSONObject(i)
            val sets = ex.getJSONArray("sets")
            for (j in 0 until sets.length()) {
                val set = sets.getJSONObject(j)
                totalVolume += set.getDouble("weight") * set.getInt("reps")
            }
        }

        val sessionId = db.workoutSessionDao().insertSession(
            WorkoutSessionEntity(
                name = "Watch Workout",
                splitType = "Custom",
                startTime = startTime,
                endTime = endTime,
                durationSeconds = durationSec,
                totalVolumeKg = totalVolume
            )
        )

        for (i in 0 until exercisesArr.length()) {
            val ex = exercisesArr.getJSONObject(i)
            val exName = ex.getString("name")
            val category = ex.optString("category", "Custom")

            val exercise = db.exerciseDao().getExerciseByName(exName)
                ?: run {
                    val id = db.exerciseDao().insertExercise(
                        ExerciseEntity(
                            name = exName,
                            primaryMuscleGroup = category,
                            secondaryMuscleGroups = "",
                            equipmentType = "Other",
                            movementType = "Custom",
                            difficulty = "Intermediate",
                            instructions = "",
                            isCustom = true
                        )
                    )
                    db.exerciseDao().getExerciseById(id)!!
                }

            val workoutExId = db.workoutExerciseDao().insertWorkoutExercise(
                WorkoutExerciseEntity(
                    sessionId = sessionId,
                    exerciseId = exercise.id,
                    orderIndex = i
                )
            )

            val sets = ex.getJSONArray("sets")
            val setEntities = (0 until sets.length()).map { j ->
                val set = sets.getJSONObject(j)
                WorkoutSetEntity(
                    workoutExerciseId = workoutExId,
                    setNumber = j + 1,
                    setType = "Working",
                    weight = set.getDouble("weight"),
                    reps = set.getInt("reps"),
                    isCompleted = true,
                    completedAt = startTime + j * 60_000L
                )
            }
            db.workoutSetDao().insertAll(setEntities)
        }
    }

    private fun badRequest(msg: String) =
        newFixedLengthResponse(Response.Status.BAD_REQUEST, MIME_PLAINTEXT, msg)
}
