package app.atmos.weather.data.model

import kotlinx.serialization.Serializable

@Serializable
data class LocationItem(
    val id: String,
    val name: String,
    val country: String,
    val admin1: String? = null,
    val latitude: Double,
    val longitude: Double,
    val timezone: String = "auto",
    val isCurrentLocation: Boolean = false
) {
    val displayName: String
        get() = if (admin1.isNullOrBlank()) {
            if (country.isBlank()) name else "$name, $country"
        } else {
            "$name, $admin1"
        }
}

val DefaultLocations = listOf(
    LocationItem(
        id = "loc_tokyo",
        name = "Tokyo",
        country = "Japan",
        admin1 = "Tokyo",
        latitude = 35.6762,
        longitude = 139.6503,
        timezone = "Asia/Tokyo"
    ),
    LocationItem(
        id = "loc_london",
        name = "London",
        country = "United Kingdom",
        admin1 = "England",
        latitude = 51.5074,
        longitude = -0.1278,
        timezone = "Europe/London"
    ),
    LocationItem(
        id = "loc_new_york",
        name = "New York",
        country = "United States",
        admin1 = "New York",
        latitude = 40.7128,
        longitude = -74.0060,
        timezone = "America/New_York"
    ),
    LocationItem(
        id = "loc_paris",
        name = "Paris",
        country = "France",
        admin1 = "Île-de-France",
        latitude = 48.8566,
        longitude = 2.3522,
        timezone = "Europe/Paris"
    ),
    LocationItem(
        id = "loc_sydney",
        name = "Sydney",
        country = "Australia",
        admin1 = "New South Wales",
        latitude = -33.8688,
        longitude = 151.2093,
        timezone = "Australia/Sydney"
    )
)
