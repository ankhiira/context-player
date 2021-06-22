package com.gabchmel.contextmusicplayer

import android.app.ActivityManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.widget.Toast


class ContextReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {

        var isRunning = false

        val manager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        for (service in manager.getRunningServices(Integer.MAX_VALUE)) {
            if (AutoPlaySongService::class.java.name.equals(service.service.className)) {
                isRunning = true
            }
            isRunning = false
        }

        Toast.makeText(context, "ACTION_USER_PRESENT", Toast.LENGTH_SHORT).show()
        Intent(context, AutoPlaySongService::class.java).also { intent2 ->
            if(!isRunning)
                context.startService(intent2)
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