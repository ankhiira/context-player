package com.gabchmel.contextmusicplayer.playlist

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels


class SongListFragment : Fragment() {

    private val viewModel: SongListViewModel by viewModels()


    // Register the permissions callback, which handles the user's response to the
// system permissions dialog. Save the return value, an instance of
// ActivityResultLauncher. You can use either a val, as shown in this snippet,
// or a lateinit var in your onAttach() or onCreate() method.
    val requestPermissionLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted: Boolean ->
            if (isGranted) {
                // Permission is granted. Continue the action or workflow in your
                // app.
                viewModel.loadSongs()
            } else {
                // Explain to the user that the feature is unavailable because the
                // features requires a permission that the user has denied. At the
                // same time, respect the user's decision. Don't link to system
                // settings in an effort to convince the user to change their
                // decision.
            }
        }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        return ComposeView(requireContext()).apply { setContent {

            if (ActivityCompat.checkSelfPermission(
                    requireContext(),
                    Manifest.permission.READ_EXTERNAL_STORAGE
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                Column {
                    Text("Sorry potrebujem permission")
                    Button({

                        requestPermissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)

                    }) {
                        Text("Get permis")
                    }
                }
            } else {

            Column() {
                Text(text = "Tvoje pesncky")
                LazyColumn {
                    items(viewModel.songs) { song ->
                        SongRow(song)
                    }
                }
            }

            }


        } }
    }

}

@Composable
fun SongRow(song: Song) {
    Text(text = "Title: ${song.title}", fontWeight = FontWeight.Bold)
}
@Preview
@Composable
fun ExampleSongRow() {
    SongRow(Song("Blala", Uri.EMPTY))
}