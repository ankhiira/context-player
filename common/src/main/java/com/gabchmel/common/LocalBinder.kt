package com.gabchmel.common

import android.app.Service
import android.os.Binder


abstract class LocalBinder<T : Service> : Binder() {
    abstract fun getService(): T
}