package com.zonezapapp.services

import android.content.Context
import android.location.Geocoder
import com.zonezapapp.data.LocationData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Locale

/**
 * Reverse geocoding: convert (lat, lng) to a human-readable address.
 * Runs on IO dispatcher; returns null if Geocoder is unavailable or fails.
 */
object GeocodingHelper {

    suspend fun getAddressFromLocation(context: Context, latitude: Double, longitude: Double): String? {
        return withContext(Dispatchers.IO) {
            if (!Geocoder.isPresent()) return@withContext null
            try {
                val geocoder = Geocoder(context, Locale.getDefault())
                @Suppress("DEPRECATION")
                val addresses = geocoder.getFromLocation(latitude, longitude, 1)
                val address = addresses?.firstOrNull() ?: return@withContext null
                formatAddress(address).takeIf { it.isNotBlank() }
            } catch (e: Exception) {
                android.util.Log.w("GeocodingHelper", "Reverse geocode failed", e)
                null
            }
        }
    }

    suspend fun getAddressFromLocation(context: Context, location: LocationData): String? {
        return getAddressFromLocation(context, location.latitude, location.longitude)
    }

    private fun formatAddress(address: android.location.Address): String {
        val parts = mutableListOf<String>()
        address.thoroughfare?.takeIf { it.isNotBlank() }?.let { parts.add(it) }
        address.subLocality?.takeIf { it.isNotBlank() }?.let { parts.add(it) }
        address.locality?.takeIf { it.isNotBlank() }?.let { parts.add(it) }
        address.adminArea?.takeIf { it.isNotBlank() }?.let { parts.add(it) }
        address.countryName?.takeIf { it.isNotBlank() }?.let { parts.add(it) }
        if (parts.isEmpty()) {
            address.getAddressLine(0)?.takeIf { it.isNotBlank() }?.let { parts.add(it) }
        }
        return parts.distinct().joinToString(", ").takeIf { it.isNotBlank() } ?: ""
    }
}
