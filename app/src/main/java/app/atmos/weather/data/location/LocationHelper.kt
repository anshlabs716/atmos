package app.atmos.weather.data.location

import android.content.Context
import android.location.Address
import android.location.Geocoder
import android.os.Build
import app.atmos.weather.data.model.LocationItem
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import java.util.Locale

@Serializable
private data class BigDataCloudReverseResponse(
    val locality: String? = null,
    val city: String? = null,
    val principalSubdivision: String? = null,
    val countryName: String? = null,
    val countryCode: String? = null
)

object LocationHelper {

    suspend fun reverseGeocode(
        context: Context,
        latitude: Double,
        longitude: Double,
        httpClient: HttpClient? = null
    ): LocationItem = withContext(Dispatchers.IO) {
        // 1. Try Android's built-in Geocoder
        var resolvedName: String? = null
        var resolvedAdmin1: String? = null
        var resolvedCountry: String? = null

        if (Geocoder.isPresent()) {
            try {
                val geocoder = Geocoder(context, Locale.getDefault())
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    var addressList: List<Address>? = null
                    geocoder.getFromLocation(latitude, longitude, 1) { addresses ->
                        addressList = addresses
                    }
                    // Wait briefly if needed or use synchronous fallback
                    val addr = addressList?.firstOrNull()
                    if (addr != null) {
                        resolvedName = addr.locality ?: addr.subLocality ?: addr.subAdminArea ?: addr.adminArea
                        resolvedAdmin1 = addr.adminArea ?: addr.subAdminArea
                        resolvedCountry = addr.countryName
                    }
                }

                if (resolvedName == null) {
                    @Suppress("DEPRECATION")
                    val addresses = geocoder.getFromLocation(latitude, longitude, 1)
                    val addr = addresses?.firstOrNull()
                    if (addr != null) {
                        resolvedName = addr.locality ?: addr.subLocality ?: addr.subAdminArea ?: addr.adminArea
                        resolvedAdmin1 = addr.adminArea ?: addr.subAdminArea
                        resolvedCountry = addr.countryName
                    }
                }
            } catch (_: Exception) {
                // Geocoder service might fail on emulators without network location provider
            }
        }

        // 2. If Android Geocoder didn't return a name, use BigDataCloud free client-side reverse geocode
        if (resolvedName.isNullOrBlank() && httpClient != null) {
            try {
                val bdc: BigDataCloudReverseResponse = httpClient.get("https://api.bigdatacloud.net/data/reverse-geocode-client") {
                    parameter("latitude", latitude)
                    parameter("longitude", longitude)
                    parameter("localityLanguage", "en")
                }.body()

                resolvedName = bdc.locality?.takeIf { it.isNotBlank() }
                    ?: bdc.city?.takeIf { it.isNotBlank() }
                    ?: bdc.principalSubdivision
                resolvedAdmin1 = bdc.principalSubdivision
                resolvedCountry = bdc.countryName ?: bdc.countryCode
            } catch (_: Exception) {}
        }

        val finalName = resolvedName?.trim()?.takeIf { it.isNotBlank() } ?: "Local Area"
        val finalCountry = resolvedCountry?.trim().orEmpty()
        val finalAdmin = resolvedAdmin1?.trim()

        LocationItem(
            id = "loc_current_${(latitude * 100).toInt()}_${(longitude * 100).toInt()}",
            name = finalName,
            country = finalCountry,
            admin1 = finalAdmin,
            latitude = latitude,
            longitude = longitude,
            timezone = "auto",
            isCurrentLocation = true
        )
    }
}
