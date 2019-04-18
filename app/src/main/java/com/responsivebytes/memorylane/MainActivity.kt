package com.responsivebytes.memorylane

import android.Manifest
import android.location.Location
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.annotation.StringRes
import androidx.appcompat.app.AlertDialog
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.location.*
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import kotlinx.android.synthetic.main.activity_main.*
import permissions.dispatcher.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.CameraUpdate



@RuntimePermissions
class MainActivity : AppCompatActivity() {
    companion object {
        private val TAG = MainActivity.javaClass.canonicalName
    }

    private lateinit var mapFragment: SupportMapFragment
    private lateinit var apiClient: GoogleApiClient

    private var map: GoogleMap? = null
    private var currentLocationMarker: Marker? = null
    private var currentLocation: LatLng? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment

        prepareGoogleApi()

        operationsWithLocationWithPermissionCheck()
    }

    fun prepareGoogleApi() {
        val connectionCallbacks = object: GoogleApiClient.ConnectionCallbacks {
            override fun onConnected(p0: Bundle?) {
            }
            override fun onConnectionSuspended(p0: Int) {
            }
        }
        apiClient = GoogleApiClient.Builder(this).addApi(LocationServices.API).addConnectionCallbacks(connectionCallbacks).build()
        apiClient.connect()
    }

    @NeedsPermission(Manifest.permission.ACCESS_FINE_LOCATION)
    fun operationsWithLocation() {
        configureMapControls()
        queryForLocation()
    }

    fun queryForLocation() {
        val request = LocationRequest.create()
        request.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
        request.setNumUpdates(1)
        request.setInterval(0)

        var locationCallback = object: LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                currentLocation = LatLng(result.lastLocation.latitude, result.lastLocation.longitude)
                addMarkerToCurrentLocation()
            }
            override fun onLocationAvailability(var1: LocationAvailability) {

            }
        }

        LocationServices.getFusedLocationProviderClient(this).requestLocationUpdates(request, locationCallback, Looper.getMainLooper())
    }

    fun configureMapControls() {
        mapFragment.getMapAsync {
            map = it
            map?.isMyLocationEnabled = true
            map?.uiSettings?.isCompassEnabled = true
            map?.uiSettings?.isMyLocationButtonEnabled = true
            map?.uiSettings?.isZoomControlsEnabled = true

            addMarkerToCurrentLocation()
        }
    }

    fun addMarkerToCurrentLocation() {
        currentLocation?.let {
            val markerOptions = MarkerOptions()
            markerOptions.position(it)
            markerOptions.title("CurrentLocation")
            currentLocationMarker = map?.addMarker(markerOptions)

            val cameraUpdate = CameraUpdateFactory.newLatLngZoom(it, 10f)
            map?.animateCamera(cameraUpdate)
        }
    }

    @OnShowRationale(Manifest.permission.ACCESS_FINE_LOCATION)
    fun showRationalForLocation(request: PermissionRequest) {
        showRationaleDialog(R.string.permission_location_rational, request)
    }

    @OnPermissionDenied(Manifest.permission.ACCESS_FINE_LOCATION)
    fun onLocationDenied() {
        Toast.makeText(this, R.string.permission_location_denied, Toast.LENGTH_SHORT).show()
    }

    @OnNeverAskAgain(Manifest.permission.ACCESS_FINE_LOCATION)
    fun onLocationNeverAskAgain() {
        Toast.makeText(this, R.string.permission_location_never_ask_again, Toast.LENGTH_SHORT).show()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        Log.i(MainActivity.TAG, "$permissions, $grantResults")
        onRequestPermissionsResult(requestCode, grantResults)
    }

    private fun showRationaleDialog(@StringRes messageResId: Int, request: PermissionRequest) {
        AlertDialog.Builder(this)
            .setPositiveButton(R.string.button_allow) { _, _ -> request.proceed() }
            .setNegativeButton(R.string.button_deny) { _, _ -> request.cancel() }
            .setCancelable(false)
            .setMessage(messageResId)
            .show()
    }
}
