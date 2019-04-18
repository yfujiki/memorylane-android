package com.responsivebytes.memorylane

import android.Manifest
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.annotation.StringRes
import androidx.appcompat.app.AlertDialog
import com.google.android.gms.maps.SupportMapFragment
import kotlinx.android.synthetic.main.activity_main.*
import permissions.dispatcher.*

@RuntimePermissions
class MainActivity : AppCompatActivity() {
    companion object {
        val TAG = MainActivity.javaClass.canonicalName
    }

    lateinit var mapFragment: SupportMapFragment

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment

        configureMapControlsWithPermissionCheck()
    }

    @NeedsPermission(Manifest.permission.ACCESS_FINE_LOCATION)
    fun configureMapControls() {
        mapFragment.getMapAsync {
            it.isMyLocationEnabled = true
            it.uiSettings.isCompassEnabled = true
            it.uiSettings.isMyLocationButtonEnabled = true
            it.uiSettings.isIndoorLevelPickerEnabled = true
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
