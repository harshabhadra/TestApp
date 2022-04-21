package com.harshabhadra.testapp.ui

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.harshabhadra.testapp.utils.CheckPermission
import com.harshabhadra.testapp.utils.ConstUtils
import com.harshabhadra.testapp.service.ForegroundService
import com.harshabhadra.testapp.MyApplication.Companion.session
import com.harshabhadra.testapp.utils.SessionManager
import com.harshabhadra.testapp.common.BaseActivity
import com.harshabhadra.testapp.databinding.ActivityLocationBinding


class LocationActivity : BaseActivity() {

    private lateinit var binding: ActivityLocationBinding
    private lateinit var locationBroadcastReceiver: LocationBroadcastReceiver

    companion object {
        private const val TAG = "LocationActivity"
    }

    @SuppressLint("VisibleForTests")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLocationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        locationBroadcastReceiver = LocationBroadcastReceiver()
        CheckPermission(this).locationPermission {
            startMyService()
        }
    }

    override fun onResume() {
        super.onResume()
        LocalBroadcastManager.getInstance(this).registerReceiver(
            locationBroadcastReceiver,
            IntentFilter(
                ForegroundService.ACTION_FOREGROUND_ONLY_LOCATION_BROADCAST
            )
        )
    }

    override fun onPause() {

        LocalBroadcastManager.getInstance(this).unregisterReceiver(
            locationBroadcastReceiver
        )
        super.onPause()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            ConstUtils.LOCATION_REQ_CODE -> {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.isNotEmpty()
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED
                ) {
                    startMyService()
                } else {
                    Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun startMyService() {
        if (session(this).getPrefBool(SessionManager.SERVICE_RUNNING).not()) {
            val intent = Intent(this, ForegroundService::class.java)
            startService(intent)
        }
    }

    private inner class LocationBroadcastReceiver : BroadcastReceiver() {

        override fun onReceive(context: Context, intent: Intent) {
            Log.e(TAG, "on Receive")
            val location = intent.getParcelableExtra<Location>(
                ForegroundService.EXTRA_LOCATION
            )
            if (location != null) {
                loadResultToScreen(
                    location.latitude.toString(), location.longitude.toString()
                )
                Log.e(TAG, "Updated location: ${location.latitude}, ${location.longitude}")
            }
        }
    }

    private fun loadResultToScreen(lat: String, lng: String) {
        binding.latValueTv.text = lat
        binding.lngValueTv.text = lng
    }
}