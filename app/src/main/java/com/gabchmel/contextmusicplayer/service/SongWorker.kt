package com.gabchmel.contextmusicplayer.service

// TODO play after music service is on - or maybe every time
//        // Load list of songs from local storage
//        loadSongs()
//
//        // Bind to SensorProcessService to later write to the file
//        this.bindService(
//            Intent(this, SensorDataProcessingService::class.java),
//            connection,
//            Context.BIND_AUTO_CREATE
//        )

//        // Every 10 seconds write to file sensor measurements with the song ID
//        fixedRateTimer(period = 10000) {
//            if (isPlaying)
//                currentSong.value?.title?.let { title ->
//                    currentSong.value?.author?.let { author ->
//                        // Create a hashCode to use it as ID of the song
//                        val titleAuthor = "$title,$author".hashCode().toUInt()
//                        sensorDataProcessingService.value?.writeToFile(titleAuthor.toString())
//                    }
//                }
//        }