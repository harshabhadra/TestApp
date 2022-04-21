package com.harshabhadra.testapp.service

import android.app.*
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.location.Location
import android.os.BatteryManager
import android.os.Build
import android.os.IBinder
import android.os.Looper
import android.provider.ContactsContract.Directory.PACKAGE_NAME
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.google.android.gms.location.*
import com.google.android.gms.tasks.Task
import com.harshabhadra.testapp.MyApplication.Companion.session
import com.harshabhadra.testapp.R
import com.harshabhadra.testapp.utils.SessionManager
import kotlinx.coroutines.*
import java.util.concurrent.TimeUnit

class ForegroundService : Service() {

    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private lateinit var locationRequest: LocationRequest
    private lateinit var locationCallback: LocationCallback
    private var currentLocation: Location? = null
    private lateinit var notificationManager: NotificationManager
    private lateinit var mBroadcastReceiver: BroadcastReceiver
    private lateinit var scope: CoroutineScope
    private var level = 0
    private var temp = 0

    override fun onCreate() {
        super.onCreate()

        val job = Job()
        scope = CoroutineScope(Dispatchers.Main + job)
        notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)

        // Create a LocationRequest.
        locationRequest = LocationRequest.create().apply {
            interval = TimeUnit.MINUTES.toMillis(10)
            fastestInterval = TimeUnit.MINUTES.toMillis(15)
            maxWaitTime = TimeUnit.MINUTES.toMillis(20)
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        }

        // Initialize the LocationCallback.
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                super.onLocationResult(locationResult)
                currentLocation = locationResult.lastLocation
                Log.e(TAG, "location update: ${currentLocation?.latitude}")

                // Notify our Activity that a new location was added.
                notifyActivity(currentLocation)
            }
        }

        scope.launch {
            subscribeToLocationUpdates()
        }

        val iFilter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
        mBroadcastReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                // Get the battery percentage
                level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, 0)

                // Get the battery temperature in celsius
                temp = intent.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, 0) / 10
                Log.e(TAG, "onReceive: $level, temp: $temp")
                val notification = generateNotification()
                notificationManager.notify(1111, notification)
            }
        }
        // Register the broadcast receiver
        registerReceiver(mBroadcastReceiver, iFilter)
    }

    override fun onDestroy() {
        unregisterReceiver(mBroadcastReceiver)
        super.onDestroy()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        val action = intent?.action
        if (action == CANCEL_FOREGROUND) {
            stopMyForegroundService()
        } else {
            val notification = generateNotification()
            startForeground(1111, notification)
            session(this).savePrefBool(SessionManager.SERVICE_RUNNING, true)
        }
        return START_STICKY
    }

    private fun subscribeToLocationUpdates() {
        try {
            //Subscribe to location changes
            val builder = LocationSettingsRequest.Builder().addLocationRequest(locationRequest)
            val client: SettingsClient = LocationServices.getSettingsClient(this)
            val task: Task<LocationSettingsResponse> = client.checkLocationSettings(builder.build())
            task.addOnSuccessListener {
                Log.e(TAG, "location setting response: ${it.locationSettingsStates?.isGpsUsable}")
                fusedLocationProviderClient.requestLocationUpdates(
                    locationRequest, locationCallback,
                    Looper.getMainLooper()
                )

            }
            task.addOnFailureListener {
                Log.e(TAG, "exception: ${it.message}")
            }
        } catch (unlikely: SecurityException) {
            Log.e(TAG, "Lost location permissions. Couldn't remove updates. $unlikely")
        }
    }

    private fun stopMyForegroundService() {
        Log.d(TAG, "unsubscribeToLocationUpdates()")

        try {
            //Unsubscribe to location changes.
            val removeTask = fusedLocationProviderClient.removeLocationUpdates(locationCallback)
            removeTask.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Log.d(TAG, "location callback removed")
                    stopSelf()
                } else {
                    Log.d(TAG, "Failed to stop location callback")
                }
            }

        } catch (unlikely: SecurityException) {
            Log.e(TAG, "Lost location permissions. Couldn't remove updates. $unlikely")
        }
        scope.cancel()
        session(this).savePrefBool(SessionManager.SERVICE_RUNNING, false)
        stopForeground(true)

    }

    private fun notifyActivity(currentLocation: Location?) {
        val intent = Intent(ACTION_FOREGROUND_ONLY_LOCATION_BROADCAST)
        currentLocation?.let {
            intent.putExtra(EXTRA_LOCATION, it)
        }
        LocalBroadcastManager.getInstance(applicationContext).sendBroadcast(intent)
    }

    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    private fun generateNotification(): Notification {
        Log.d(TAG, "generateNotification()")

        // 0. Get data
        val mainNotificationText = "Battery level $level, Temp: $temp"
        val titleText = getString(R.string.app_name)

        // 1. Create Notification Channel for O+ and beyond devices (26+).
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            val notificationChannel = NotificationChannel(
                NOTIFICATION_CHANNEL_ID,
                "Location Notification Channel",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            notificationManager.createNotificationChannel(notificationChannel)
        }

        // 2. Build the BIG_TEXT_STYLE.
        val bigTextStyle = NotificationCompat.BigTextStyle()
            .bigText(mainNotificationText)
            .setBigContentTitle(titleText)

        val cancelIntent = Intent(this, ForegroundService::class.java).apply {
            action = CANCEL_FOREGROUND
        }
        val cancelPendingIntent =
            PendingIntent.getService(this, 0, cancelIntent, PendingIntent.FLAG_CANCEL_CURRENT)
        // 4. Build and issue the notification.
        // Notification Channel Id is ignored for Android pre O (26).
        val notificationCompatBuilder =
            NotificationCompat.Builder(applicationContext, NOTIFICATION_CHANNEL_ID)

        return notificationCompatBuilder
            .setStyle(bigTextStyle)
            .setContentTitle(titleText)
            .setContentText(mainNotificationText)
            .setSound(null)
            .setSmallIcon(R.mipmap.ic_launcher_round)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .setOngoing(true)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .addAction(R.drawable.ic_launcher_background, "Stop", cancelPendingIntent)
            .build()
    }

    companion object {
        private const val TAG = "ForegroundService"
        private const val NOTIFICATION_CHANNEL_ID = "test_notification_channel"
        internal const val ACTION_FOREGROUND_ONLY_LOCATION_BROADCAST =
            "$PACKAGE_NAME.action.FOREGROUND_ONLY_LOCATION_BROADCAST"

        internal const val EXTRA_LOCATION = "$PACKAGE_NAME.extra.LOCATION"
        const val CANCEL_FOREGROUND = "cancel_foreground"
    }
}