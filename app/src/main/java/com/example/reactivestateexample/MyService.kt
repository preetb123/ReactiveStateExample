package com.example.reactivestateexample

import android.content.Intent
import android.os.Binder
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStore
import com.ensody.reactivestate.LifecycleAndViewStoreOwnerService
import com.ensody.reactivestate.reactiveState


class MyService : LifecycleAndViewStoreOwnerService(), MainEvents {
    private val vm by reactiveState { MainViewModel(scope) }

    // Binder given to clients.
    private val binder = LocalBinder()

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
        repeatT()
        return super.onStartCommand(intent, flags, startId)
    }

    private fun repeatT() {
        Handler(Looper.getMainLooper()).postDelayed(Runnable {
            if(vm.squareOfNumber.value.toInt() > 500){
                stopSelf()
                return@Runnable
            }
            vm.increment()
            repeatT()
        }, 500)
    }

    override fun onError(error: Throwable) {
        TODO("Not yet implemented")
    }
}