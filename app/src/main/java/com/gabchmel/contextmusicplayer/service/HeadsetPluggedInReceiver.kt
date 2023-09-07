package com.gabchmel.contextmusicplayer.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

// Set the volume level on headphones plugged in
class HeadsetPluggedInReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent) {
        val action = intent.action
        if (Intent.ACTION_HEADSET_PLUG == action) {
            val headphonesPluggedIn = intent.getIntExtra("state", -1)
            if (headphonesPluggedIn == 1) {
//                player.setVolume(0.5f, 0.5f)
            }
        }
    }
}