package com.gabchmel.sensorprocessor

import android.content.Context
import android.os.Build
import android.util.Log
import com.github.doyaaaaaken.kotlincsv.dsl.csvReader
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.*
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin


object InputProcessHelper {
    fun inputProcessHelper(sensorData: SensorData): DoubleArray {

        val currentTime = sensorData.currentTime
        val latitude = sensorData.latitude
        val longitude = sensorData.longitude

        // Convert to similar representation as in other models
        val dayOfWeek =
            if (currentTime.day == 0) {
                6
            } else {
                currentTime.day - 1
            }

        // Convert current time to seconds
        val timeInSeconds =
            (currentTime.hours * 60 + currentTime.minutes) * 60 + currentTime.seconds
        // Get number of seconds in a day
        val secondsInDay = 24 * 60 * 60

        // Represent time in it's circular representation
        val sinTime = sin(2 * PI * timeInSeconds / secondsInDay)
        val cosTime = cos(2 * PI * timeInSeconds / secondsInDay)

        // Represent day of the week in it's circular representation
        val dayOfWeekSin = sin(dayOfWeek * (2 * PI / 7))
        val dayOfWeekCos = cos(dayOfWeek * (2 * PI / 7))

        // Convert longitude and latitude to x,y,z coordinates
        val xCoord = latitude?.let { lat ->
            longitude?.let { long -> cos(lat) * cos(long) }
        }
        val yCoord = latitude?.let { lat ->
            longitude?.let { long -> cos(lat) * sin(long) }
        }
        val zCoord = latitude?.let { lat ->
            sin(lat)
        }

        return doubleArrayOf(
            sinTime, cosTime, dayOfWeekSin,
            dayOfWeekCos, (xCoord ?: 0.0), (yCoord ?: 0.0), (zCoord ?: 0.0)
        )
    }

    fun processInputCSV(context: Context): ArrayList<String> {

        val inputFile = File(context.filesDir, "data.csv")
        val csvFile = File(context.filesDir, "convertedData.csv")
        val classNames = arrayListOf<String>()

        csvFile.writeText(
            "class,sinTime,cosTime,dayOfWeekSin," +
                    "dayOfWeekCos,xCoord,yCoord,zCoord" + "\n"
        )

        // TODO else
        if (inputFile.exists()) {
            csvReader().open(inputFile) {

                readAllAsSequence().onEach {
//                    println(it) //[a, b, c]
                }.map { row ->

                    val dateNew = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                        val format =
                            DateTimeFormatter.ofPattern(
                                "E MMM dd HH:mm:ss ZZZZ yyyy",
                                context.resources.configuration.locales.get(0)
                            )
                        val localDate = LocalDateTime.parse(row[1], format)
                        Date.from(localDate.atZone(ZoneId.systemDefault()).toInstant())
                    } else {
                        // TODO check date formats
                        val formatter =
                            SimpleDateFormat("E MMM dd HH:mm:ss ZZZZ yyyy", Locale.ENGLISH)
                        formatter.parse(row[1])!!
                    }

                    if (!classNames.contains(row[0])) {
                        classNames.add(row[0])
                    }

                    row[0] to SensorData(
                        dateNew, row[2].toDouble(), row[3].toDouble(), row[4], row[5].toFloat(),
                        row[6].toFloat(), row[7].toFloat(),
                        row[8].toFloat()
                    )
                }.map {
                    it.first to inputProcessHelper(it.second)
                }.forEach {
//                    if (csvFile.exists()) {
                        try {
                            // Write to csv file
                            val data = it.second.joinToString(separator = ",", postfix = "\n")
                            csvFile.appendText(it.first + "," + data)
                        } catch (e: IOException) {
                            Log.e("Err", "Couldn't write to file", e)
                        }
//                    } else {
//                        csvFile.writeText(
//                            "class,sinTime,cosTime,dayOfWeekSin," +
//                                    "dayOfWeekCos,xCoord,yCoord,zCoord" + "\n"
//                        )
//                    }
                }
            }
        }
        return classNames
    }
}