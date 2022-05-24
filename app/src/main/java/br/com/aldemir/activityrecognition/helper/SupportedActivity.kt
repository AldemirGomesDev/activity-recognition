package br.com.aldemir.activityrecognition.helper

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import br.com.aldemir.activityrecognition.R
import com.google.android.gms.location.DetectedActivity
import java.lang.IllegalArgumentException

const val SUPPORTED_ACTIVITY_KEY = "activity_key"

enum class SupportedActivity(
    @DrawableRes val activityImage: Int,
    @StringRes val activityText: Int
) {
    NOT_STARTED(R.drawable.ic_watch_later, R.string.time_to_start),
    STILL(R.drawable.ic_stoped, R.string.still_text),
    WALKING(R.drawable.ic_walking, R.string.walking_text),
    RUNNING(R.drawable.ic_running, R.string.running_text),
    IN_VEHICLE(R.drawable.ic_vehicle, R.string.vehicle_text),
    ON_BICYCLE(R.drawable.ic_bicycle, R.string.bicycle_text),
    ON_FOOT(R.drawable.ic_foot, R.string.on_foot_text),
    TILTING(R.drawable.ic_tilting, R.string.tilting_text);

    companion object {

        fun fromActivityType(type: Int): SupportedActivity = when (type) {
            DetectedActivity.STILL -> STILL
            DetectedActivity.WALKING -> WALKING
            DetectedActivity.RUNNING -> RUNNING
            DetectedActivity.IN_VEHICLE -> IN_VEHICLE
            DetectedActivity.ON_BICYCLE -> ON_BICYCLE
            DetectedActivity.ON_FOOT -> ON_FOOT
            DetectedActivity.TILTING -> TILTING
            else -> throw IllegalArgumentException("activity $type not supported")
        }

    }
}