package com.gabchmel.contextmusicplayer

import android.app.ActivityManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.widget.Toast

@Deprecated ("nebudu pouzivat bcast receiver protoze neupozitelne pro vetsi API")
class ContextReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {

        var isRunning = false

        // Check if the service is running
        val manager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
//        for (serviceInfo in manager.getRunningServices(Integer.MAX_VALUE)) {
//            if (AutoPlaySongService::class.java.name.equals(serviceInfo.service.className)) {
//                isRunning = true
//            }
//        }

        Toast.makeText(context, "ACTION_USER_PRESENT", Toast.LENGTH_SHORT).show()
        if (!isRunning) {

//            Intent(context, AutoPlaySongService::class.java).also { intentService ->
//                context.startService(intentService)
//            }
//            GlobalScope.async {
//                context.bindService(AutoPlaySongService::class.java)
//            }
        } else {

//            val service = CompletableDeferred<AutoPlaySongService>()

//            GlobalScope.async {
//                val serviceAuto = service.await()
//                serviceAuto.playSong()
//            }
        }

//        context!!.startService(Intent(context, AutoPlaySongService::class.java))

//        if (intent.getIntExtra("state", -1) == 1) {
//            context!!.startService(Intent(context, AutoPlaySongService::class.java))
//        }

//        if (intent!!.action.equals("MyAction")) {
//            Toast.makeText(context, "ACTION_USER_PRESENT", Toast.LENGTH_SHORT).show()
//            if (intent.getIntExtra("state", -1) == 1) {
//                context!!.startService(Intent(context, AutoPlaySongService::class.java))
//            }
//        }
    }
}