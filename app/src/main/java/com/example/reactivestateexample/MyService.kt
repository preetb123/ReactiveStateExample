package com.example.reactivestateexample

import android.R
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.lifecycle.LifecycleService
import mobx.core.action
import mobx.core.autorun
import mobx.core.computed
import mobx.core.observable
import mobx.core.reaction
import mobx.core.whenThen


class DataStore {
    var counter by observable(0)
        private set
    val counterSquare by computed { counter * counter }

    fun increment() = action("INCREMENT"){
        counter += 1
    }
}

class MyService : LifecycleService() {
//    private privateval vm by reactiveState { MainViewModel(scope) }

    // Binder given to clients.
    private val binder = LocalBinder()
    val dataStore: DataStore = DataStore()

    /**
     * Class used for the client Binder. Because we know this service always
     * runs in the same process as its clients, we don't need to deal with IPC.
     */
    inner class LocalBinder : Binder() {
        // Return this instance of LocalService so clients can call public methods.
        fun getService(): MyService = this@MyService
    }

    override fun onCreate() {
        super.onCreate()

    }

    override fun onBind(intent: Intent): IBinder? {
        super.onBind(intent)
        return binder
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
        createNotificationChannel();
        val notificationIntent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this,
            0, notificationIntent, PendingIntent.FLAG_IMMUTABLE
        )
        val notification: Notification = NotificationCompat.Builder(this, "1234")
            .setContentTitle("Foreground Service")
            .setContentText("Testing")
            .setSmallIcon(R.drawable.ic_media_play)
            .setContentIntent(pendingIntent)
            .build()

        startForeground(1, notification);
        //do heavy work on a background thread
        //stopSelf();

        reactions()

        repeatT()
        return START_NOT_STICKY;
    }

    private fun reactions() {
        observeChanges({ dataStore.counterSquare }) {
            Log.d("reaction", "new val: "+ it)
        }

        whenThen({dataStore.counterSquare == 121}){
            Log.d("test", "reached 11")
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val serviceChannel = NotificationChannel(
                "1234",
                "Foreground Service Channel",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            val manager = getSystemService(
                NotificationManager::class.java
            )
            manager.createNotificationChannel(serviceChannel)
        }
    }

    private fun repeatT() {
        Handler(Looper.getMainLooper()).postDelayed(Runnable {
            Log.d("TEST", "Current count: " + dataStore.counterSquare)
            if(dataStore.counterSquare > 500){
                stopSelf()
                return@Runnable
            }
            dataStore.increment()
            repeatT()
        }, 1000)
    }

//    override fun onError(error: Throwable) {
//        TODO("Not yet implemented")
//    }
}