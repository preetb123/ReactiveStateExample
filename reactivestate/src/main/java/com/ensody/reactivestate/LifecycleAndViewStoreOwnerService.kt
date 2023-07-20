package com.ensody.reactivestate

import androidx.lifecycle.HasDefaultViewModelProviderFactory
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner

public open class LifecycleAndViewStoreOwnerService() : LifecycleService(),
    ViewModelStoreOwner, HasDefaultViewModelProviderFactory {
    public val mViewModelStore: ViewModelStore = ViewModelStore()
    private var mFactory: ViewModelProvider.Factory? = null
    override fun onCreate() {
        super.onCreate()
        lifecycle.addObserver(object : LifecycleEventObserver {
            override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
                if (source.lifecycle.currentState == Lifecycle.State.DESTROYED) {
                    mViewModelStore.clear()
                    source.lifecycle.removeObserver(this)
                }
            }
        })
    }

    override fun getViewModelStore(): ViewModelStore {
        return mViewModelStore
    }

    override fun getDefaultViewModelProviderFactory(): ViewModelProvider.Factory {
        return mFactory
            ?: ViewModelProvider.AndroidViewModelFactory(
                application
            ).also {
                mFactory = it
            }
    }
}
