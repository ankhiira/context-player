# Context Player

A smart music player that predicts a song depending on the user's context. Whenever the user plays a song, the current context is stored in the form of sensor readings or detected activity values.

## Features

- music playback of songs from local storage
- context detection via random forest model
- locally saving current context into CSV file

## Screenshots

<p float="left">
<img src="https://github.com/4Gabby4/context-player/blob/master/screenshots/NowPlayingScreen.png" height="400">
<img src="https://github.com/4Gabby4/context-player/blob/master/screenshots/SongListScreen.png" height="400">
  <img src="https://github.com/4Gabby4/context-player/blob/master/screenshots/Notifications.png" height="400">
</p>

## Versions

- in the `dev` branch is debug version of the application
  - in this version predictions are triggered each minute, not depending on the context change
  - data are collected every 10 seconds when the song is playing
- in the `master` branch in release version
  - this version triggers prediction every 10 minutes and only if the context changed
  - data are collected every 40 seconds when the song is playing and it starts 10 seconds after the song started

## Usage

1. have songs saved locally on the device
2. play songs (at least two)
3. the predictions will then appear in the notification

## Contextual data collected

- Sensor data
  - ambient light sensor
  - temperature sensor
  - humidity sensor
  - proximity sensor
  - pressure sensor
- Detected activities
  - activity detection - walking, still, running, riding a bike, traveling by a car
  - cable heaphones plugged in
  - bluetooth headphones plugged in
  - to which WiFi is the devices connected - hashed name
  - type of internet connection - WiFi, cellular, none
  - if the device is charging
  - type of charger - USB, AC, wireless
