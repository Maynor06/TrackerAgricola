package com.example.demogps

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CircleOptions
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.PolylineOptions
import org.json.JSONArray
import android.graphics.Color

class Mapa_demo : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_mapa_demo)

        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.mapFragment) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        mMap.mapType = GoogleMap.MAP_TYPE_HYBRID;

        val jsonData = """
            [
                { "lat": 37.774900, "lng": -122.419400, "timestamp": "2025-03-04 14:00:00" },
                { "lat": 37.774934, "lng": -122.419386, "timestamp": "2025-03-04 14:00:20" },
                { "lat": 37.774968, "lng": -122.419372, "timestamp": "2025-03-04 14:00:40" },
                { "lat": 37.775002, "lng": -122.419358, "timestamp": "2025-03-04 14:01:00" },
                { "lat": 37.775036, "lng": -122.419344, "timestamp": "2025-03-04 14:01:20" }
            ]

        """

        val coordinates = parseJsonCoordinates(jsonData)

        drawRouteOnMap(coordinates)
 // marcadores
        for (point in coordinates) {
            addMarkerWithCircle(point.first, point.second)
        }

        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(coordinates.first().first, 20f) )


    }

    private fun parseJsonCoordinates(jsonString: String): List<Pair<LatLng, String>>  {
        val jsonArray = JSONArray(jsonString)
        val coordinatesList = mutableListOf<Pair<LatLng, String>>()

        for (i in 0 until jsonArray.length()) {
            val jsonObject = jsonArray.getJSONObject(i)
            val lat = jsonObject.getDouble("lat")
            val lng = jsonObject.getDouble("lng")
            val timestamp = jsonObject.getString("timestamp")
            coordinatesList.add(Pair(LatLng(lat, lng), timestamp ))
        }

        return coordinatesList
    }

    private fun drawRouteOnMap(coordinates: List<Pair<LatLng, String>> ) {
        val polyneOptions = PolylineOptions()
            .addAll(coordinates.map { it.first })
            .width(8f)
            .color(android.graphics.Color.BLUE)

        mMap.addPolyline(polyneOptions);
    }

    private fun addMarkerWithCircle(position: LatLng, timestamp: String){

        mMap.addMarker(
            MarkerOptions()
                .position(position )
                .title("Punto registrado")
                .snippet("Fecha y hora: $timestamp")
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED))
        )

        mMap.addCircle(
            CircleOptions()
                .center(position)
                .radius(0.2)
                .strokeWidth(2f)
                .strokeColor(Color.BLACK)
                .fillColor(Color.argb(50, 0, 0, 255))
        )

    }

}