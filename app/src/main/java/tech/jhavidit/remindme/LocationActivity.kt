package tech.jhavidit.remindme

import android.Manifest
import android.annotation.SuppressLint
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.location.Location
import android.location.LocationManager
import android.net.Uri
import android.os.Bundle
import android.os.Looper
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.google.android.gms.location.*
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import tech.jhavidit.remindme.databinding.ActivityLocationBinding

class LocationActivity : AppCompatActivity() {
    private lateinit var mFusedLocationClient: FusedLocationProviderClient
    private lateinit var binding: ActivityLocationBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_location)
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        if (!isLocationEnabled()) {
            Toast.makeText(
                this,
                "Your location provider is turned off, please turn it on",
                Toast.LENGTH_SHORT
            ).show()

            val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
            startActivity(intent)
        } else {
            // START
            Dexter.withActivity(this)
                .withPermissions(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION

                )
                .withListener(object : MultiplePermissionsListener {
                    override fun onPermissionsChecked(report: MultiplePermissionsReport?) {
                        if (report!!.areAllPermissionsGranted()) {
                            requestLocationData()
                        }

                        if (report.isAnyPermissionPermanentlyDenied) {
                            Toast.makeText(
                                this@LocationActivity,
                                "You have denied permission. Please allow it is mandatory.",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }

                    override fun onPermissionRationaleShouldBeShown(
                        permissions: MutableList<PermissionRequest>?,
                        token: PermissionToken?
                    ) {
                        showRationalDialogForPermissions()
                    }
                }).onSameThread()
                .check()
            // END
        }


    }

    private fun showRationalDialogForPermissions() {
        AlertDialog.Builder(this)
            .setMessage("It Looks like you have turned off permissions required for this feature. It can be enabled under Application Settings")
            .setPositiveButton(
                "GO TO SETTINGS"
            ) { _, _ ->
                try {
                    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                    val uri = Uri.fromParts("package", packageName, null)
                    intent.data = uri
                    startActivity(intent)
                } catch (e: ActivityNotFoundException) {
                    e.printStackTrace()
                }
            }
            .setNegativeButton("Cancel") { dialog,
                                           _ ->
                dialog.dismiss()
            }.show()
    }

    private fun isLocationEnabled(): Boolean {
        val locationManager: LocationManager =
            getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(
            LocationManager.NETWORK_PROVIDER
        )

    }

    @SuppressLint("MissingPermission")
    private fun requestLocationData() {

        val mLocationRequest = LocationRequest()
        mLocationRequest.interval = 20000
        mLocationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY

        mFusedLocationClient.requestLocationUpdates(
            mLocationRequest, mLocationCallback,
            Looper.myLooper()
        )
    }

    private val mLocationCallback = object : LocationCallback() {
        @SuppressLint("LogNotTimber")
        override fun onLocationResult(locationResult: LocationResult) {
            val mLastLocation: Location = locationResult.lastLocation
            Log.d("Current Latitude", mLastLocation.latitude.toString())
            Log.d("Current Longitude", mLastLocation.longitude.toString())

        }
    }
}