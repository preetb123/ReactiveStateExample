package com.example.reactivestateexample

import android.annotation.SuppressLint
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.IBinder
import android.widget.Button
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.ensody.reactivestate.BaseReactiveState
import com.ensody.reactivestate.ErrorEvents
import com.ensody.reactivestate.android.autoRun
import com.ensody.reactivestate.android.reactiveState
import com.ensody.reactivestate.autoRun
import com.ensody.reactivestate.derived
import com.ensody.reactivestate.get
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import mobx.core.Disposable
import mobx.core.autorun

interface MainEvents : ErrorEvents {
}

class MainViewModel(
    scope: CoroutineScope,
//    store: StateFlowStore,
) : BaseReactiveState<MainEvents>(scope) {
    val number = MutableStateFlow(0)
    val squareOfNumber: StateFlow<Number> = derived {
        get(number) * get(number)
    }

    fun increment() {
        number.value += 1
    }
}

class MainActivity : AppCompatActivity(), MainEvents {

    private val viewModel by reactiveState { MainViewModel(scope) }
    private val disposables = mutableSetOf<Disposable>()


    private lateinit var textView: TextView
    private lateinit var mService: MyService
    private var mBound: Boolean = false

    /** Defines callbacks for service binding, passed to bindService().  */
    private val connection = object : ServiceConnection {

        override fun onServiceConnected(className: ComponentName, service: IBinder) {
            // We've bound to LocalService, cast the IBinder and get LocalService instance.
            val binder = service as MyService.LocalBinder
            mService = binder.getService()
            mBound = true

            initUIUpdates()
        }

        override fun onServiceDisconnected(arg0: ComponentName) {
            mBound = false
        }
    }

    private fun initUIUpdates() {
//        autorun {
//            textView.text = "square of ${mService.dataStore.counter} is: ${mService.dataStore.counterSquare}"
//        }.also {
//            disposables.add(it)
//        }
    }

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val button = findViewById<Button>(R.id.button)
        textView = findViewById<TextView>(R.id.textView)



        val intent = Intent(this, MyService::class.java)

        ContextCompat.startForegroundService(this, intent)

        bindService(intent, connection, Context.BIND_AUTO_CREATE)
    }

    override fun onError(error: Throwable) {
    }

    override fun onStop() {
        super.onStop()
        unbindService(connection)
        mBound = false
    }

    override fun onDestroy() {
        super.onDestroy()

        disposables.forEach { it.dispose() }
        disposables.clear()
    }
}