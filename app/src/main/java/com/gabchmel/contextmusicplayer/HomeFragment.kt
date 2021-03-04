package com.gabchmel.contextmusicplayer

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.MediaPlayer
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.support.v4.media.session.MediaSessionCompat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.SeekBar
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.fragment.app.Fragment
import kotlinx.android.synthetic.main.fragment_home.*

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [HomeFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class HomeFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    lateinit var runnable: Runnable
    private var handler = Handler()

    lateinit var notificationChannel: NotificationChannel
    lateinit var builder: NotificationCompat.Builder

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_home, container, false)

        if (container != null) {
            createNotificationChannel(container.context)
        }

        // Media Player
        val mediaPlayer = MediaPlayer.create(activity, R.raw.careful)

        val btnPlay = view.findViewById<Button>(R.id.btn_play)

        val seekBar = view.findViewById<SeekBar>(R.id.seekBar)

        seekBar.progress = 0

        // Max length of the seekBar (length of the song)
        seekBar.max = mediaPlayer.duration

        btnPlay.setOnClickListener {

            if (!mediaPlayer.isPlaying) {

                mediaPlayer.start()

                // Set icon to pause
                btnPlay.setBackgroundResource(R.drawable.ic_pause_black_24dp)
            } else {

                mediaPlayer.pause()

                // Set icon to play
                btnPlay.setBackgroundResource(R.drawable.ic_play_arrow_black_24dp)
            }
        }

        seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener{
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {

                if (fromUser) {
                    mediaPlayer.seekTo(progress)
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {

            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {

            }
        })

        // To handle the moving of the seekBar while the song is playing
        runnable = Runnable {
            seekBar.progress = mediaPlayer.currentPosition
            handler.postDelayed(runnable, 1000)
        }

        handler.postDelayed(runnable, 1000)

        // After the song finishes go to the initial design
        mediaPlayer.setOnCompletionListener {
            btnPlay.setBackgroundResource(R.drawable.ic_play_arrow_black_24dp)
            seekBar.progress = 0
        }

        return view
    }

    companion object {

        const val CHANNEL_ID = "channel"
        const val notificationID = 1234

        const val ACTION_PREVIOUS = "actionprev"
        const val CHANNEL_PLAY = "actionplay"
        const val CHANNEL_NEXT = "actionnext"

        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment BlankFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            HomeFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }

    private fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            var description = "Test notification"
            val descriptionText = "description"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            notificationChannel = NotificationChannel(CHANNEL_ID, description, importance).apply {
                description = descriptionText
            }

            val notificationManager : NotificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            notificationManager.createNotificationChannel(notificationChannel)

            builder = NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_baseline_headset_24)
                .setContentTitle("Name")
                .setContentText("Text")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
        }

        // Specification of activity that will be executed after click on the notification will be performed
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

        // Definition of the intent execution that execute the according activity
        val pendingIntent: PendingIntent = PendingIntent.getActivity(context, 0, intent, 0)

        // Bitmap for album art on notification
        val albumArt = BitmapFactory.decodeResource(resources, R.raw.context_player_icon)

        // Definition of notification layout
        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_baseline_headset_24)
            .setContentTitle("Track title")
            .setContentText("Author")
            .setStyle(androidx.media.app.NotificationCompat.MediaStyle())
            .addAction(R.drawable.ic_skip_previous_black_24dp, "Prev", pendingIntent)
            .addAction(R.drawable.ic_play_arrow_black_24dp, "Play", pendingIntent)
            .addAction(R.drawable.ic_skip_next_black_24dp, "Next", pendingIntent)
            .setLargeIcon(albumArt)
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setContentIntent(pendingIntent)

        // Creates notification object with set parameters
        with(NotificationManagerCompat.from(context)) {
            notify(notificationID, builder.build())
        }
    }
}