package fr.retourecole.child

data class LocationState(
    val latitude: Double? = null,
    val longitude: Double? = null,
    val timestamp: Long? = null,
    val tracking: Boolean = false
)
