package com.zonezapapp.ui.guardian

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.android.material.switchmaterial.SwitchMaterial
import com.zonezapapp.R
import com.zonezapapp.data.LocationData
import com.zonezapapp.services.GeocodingHelper
import com.zonezapapp.services.WardLocationService
import kotlinx.coroutines.launch
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker

class WardLiveMapActivity : AppCompatActivity() {

    private var mapView: MapView? = null
    private var wardMarker: Marker? = null
    private var wardId: String = ""
    private var wardName: String = ""
    private val wardLocationService = WardLocationService()
    private val liveRefreshIntervalMs = 5_000L
    private var liveHandler: Handler? = null
    private var liveRunnable: Runnable? = null
    private var lastGeoPoint: GeoPoint? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Configuration.getInstance().load(this, getPreferences(MODE_PRIVATE))
        Configuration.getInstance().userAgentValue = packageName
        setContentView(R.layout.activity_ward_live_map)

        wardId = intent.getStringExtra(EXTRA_WARD_ID) ?: ""
        wardName = intent.getStringExtra(EXTRA_WARD_NAME) ?: "Ward"

        supportActionBar?.title = "Track: $wardName"
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        findViewById<android.widget.TextView>(R.id.wardNameText).text = wardName

        val mv = findViewById<MapView>(R.id.mapView)
        mapView = mv
        mv.setTileSource(TileSourceFactory.MAPNIK)
        mv.setMultiTouchControls(true)
        mv.controller?.setZoom(14.0)
        mv.controller?.setCenter(GeoPoint(20.0, 77.0))
        wardMarker = Marker(mv, this).apply {
            title = wardName
            position = GeoPoint(0.0, 0.0) // updated when location is fetched
        }
        mv.overlays.add(wardMarker!!)

        refreshLocation()
        if (findViewById<SwitchMaterial>(R.id.liveTrackingSwitch).isChecked) {
            startLiveRefresh()
        }

        findViewById<com.google.android.material.button.MaterialButton>(R.id.refreshLocationButton).setOnClickListener {
            refreshLocation()
        }

        val liveSwitch = findViewById<SwitchMaterial>(R.id.liveTrackingSwitch)
        liveSwitch.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) startLiveRefresh() else stopLiveRefresh()
        }
        liveSwitch.isChecked = true
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }

    override fun onResume() {
        super.onResume()
        mapView?.onResume()
    }

    override fun onPause() {
        super.onPause()
        mapView?.onPause()
    }

    private fun refreshLocation() {
        if (wardId.isEmpty()) return
        lifecycleScope.launch {
            try {
                val location = wardLocationService.getLatestWardLocation(wardId)
                if (location != null) {
                    updateMapWithLocation(location)
                    updateLastUpdatedText(location.timestamp)
                } else {
                    Toast.makeText(this@WardLiveMapActivity, "No location data yet", Toast.LENGTH_SHORT).show()
                    findViewById<android.widget.TextView>(R.id.lastUpdatedText).text = "No location available"
                }
            } catch (e: Exception) {
                android.util.Log.e("WardLiveMap", "Error fetching location", e)
                Toast.makeText(this@WardLiveMapActivity, "Error loading location", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun updateMapWithLocation(location: LocationData) {
        val map = mapView ?: return
        val geoPoint = GeoPoint(location.latitude, location.longitude)
        wardMarker?.position = geoPoint
        wardMarker?.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
        lifecycleScope.launch {
            val address = GeocodingHelper.getAddressFromLocation(this@WardLiveMapActivity, location)
            wardMarker?.snippet = address ?: "Accuracy: ${location.accuracy.toInt()}m"
            map.invalidate()
        }
        wardMarker?.snippet = "Accuracy: ${location.accuracy.toInt()}m"
        map.invalidate()

        val prev = lastGeoPoint
        lastGeoPoint = geoPoint
        if (prev == null) {
            map.controller.setCenter(geoPoint)
            map.controller.setZoom(16.0)
        } else if (distanceBetween(prev, geoPoint) > 50.0) {
            map.controller.animateTo(geoPoint)
        }
    }

    private fun distanceBetween(a: GeoPoint, b: GeoPoint): Double {
        val earthRadius = 6_371_000.0
        val dLat = Math.toRadians(b.latitude - a.latitude)
        val dLon = Math.toRadians(b.longitude - a.longitude)
        val x = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(Math.toRadians(a.latitude)) * Math.cos(Math.toRadians(b.latitude)) *
                Math.sin(dLon / 2) * Math.sin(dLon / 2)
        val c = 2 * Math.atan2(Math.sqrt(x), Math.sqrt(1 - x))
        return earthRadius * c
    }

    private fun updateLastUpdatedText(timestampMs: Long) {
        val diff = System.currentTimeMillis() - timestampMs
        val minutesAgo = (diff / 60_000).toInt()
        val text = when {
            minutesAgo < 1 -> "Last updated: Just now"
            minutesAgo < 60 -> "Last updated: $minutesAgo min ago"
            else -> "Last updated: ${minutesAgo / 60} hr ago"
        }
        findViewById<android.widget.TextView>(R.id.lastUpdatedText).text = text
    }

    private fun startLiveRefresh() {
        stopLiveRefresh()
        val handler = Handler(Looper.getMainLooper())
        val runnable = object : Runnable {
            override fun run() {
                refreshLocation()
                handler.postDelayed(this, liveRefreshIntervalMs)
            }
        }
        liveHandler = handler
        liveRunnable = runnable
        handler.postDelayed(runnable, liveRefreshIntervalMs)
    }

    private fun stopLiveRefresh() {
        liveHandler?.let { it.removeCallbacks(liveRunnable ?: return) }
        liveHandler = null
        liveRunnable = null
    }

    override fun onDestroy() {
        stopLiveRefresh()
        super.onDestroy()
    }

    companion object {
        const val EXTRA_WARD_ID = "ward_id"
        const val EXTRA_WARD_NAME = "ward_name"
    }
}
