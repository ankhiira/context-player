package com.gabchmel.common.utils

import android.app.Service
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import com.gabchmel.common.data.LocalBinder
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

// Function to handle service binding
suspend fun <T : Service> Context.bindService(clazz: Class<T>) =
    suspendCoroutine { cont ->
        val connection = object : ServiceConnection {

            override fun onServiceConnected(className: ComponentName, service: IBinder) {
                // We've bound to SensorProcessService, cast the IBinder and get SensorProcessService instance
                val binder = service as LocalBinder<T>
                cont.resume(binder.getService())
            }

            override fun onServiceDisconnected(arg0: ComponentName) {
            }
        }

        val intent = Intent(this@bindService, clazz)
        this@bindService.bindService(
            intent, connection,
            Service.BIND_AUTO_CREATE
        )
    }
