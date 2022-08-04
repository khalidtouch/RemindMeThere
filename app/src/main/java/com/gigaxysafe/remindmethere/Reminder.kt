package com.gigaxysafe.remindmethere

import com.google.android.gms.maps.model.LatLng
import java.util.*

data class Reminder(
    var id: String = UUID.randomUUID().toString(),
    var latLng: LatLng?,
    var radius: Double?,
    var message: String?
)