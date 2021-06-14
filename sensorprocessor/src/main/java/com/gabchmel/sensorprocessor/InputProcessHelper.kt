package com.gabchmel.sensorprocessor

import android.content.Context
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
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

    @RequiresApi(Build.VERSION_CODES.O)
    fun processInputCSV(context: Context) {

        val inputFile = File(context.filesDir, "data.csv")

        val csvFile = File(context.filesDir, "convertedData.csv")

        if (inputFile.exists()) {
            csvReader().open(inputFile) {

                // TODO check date formats
                val formatter = SimpleDateFormat("E MMM dd HH:mm:ss ZZZZ yyyy", Locale.ENGLISH)

                val format=
                    DateTimeFormatter.ofPattern("E MMM dd HH:mm:ss ZZZZ yyyy", Locale.ENGLISH)

                readAllAsSequence().onEach {
                    println(it) //[a, b, c]
                }.map { row ->

                    val dateNew = if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                        val localDate = LocalDateTime.parse(row[1], format)
                        Date.from(localDate.atZone(ZoneId.systemDefault()).toInstant())
                    } else {
                        formatter.parse(row[1])!!
                    }

                    row[0] to SensorData(
                        dateNew, row[2].toDouble(), row[3].toDouble(), row[4], row[5].toFloat(),
                        row[6].toFloat(), row[7].toFloat(),
                        row[8].toFloat()
                    )
                }.map {
                   it.first to inputProcessHelper(it.second)
                }.forEach {
                    if(csvFile.exists()) {
                        try {
                            val data = it.second.joinToString(separator = ",", postfix = "\n")
                            // Write to csv file
                            csvFile.appendText(it.first + "," + data)
                        } catch (e: IOException) {
                            Log.e("Err", "Couldn't write to file", e)
                        }
                    } else {
                        csvFile.appendText("class,sinTime,cosTime,dayOfWeekSin,"+
                                "dayOfWeekCos,xCoord,yCoord,zCoord" + "\n")
                    }
                }
            }

//        val reader = CSVReaderHeaderAware(FileReader(inputFile))
//        val resultList = mutableListOf<Map<String, String>>()
//        var line = reader.readMap()
//        while (line != null) {
//            resultList.add(line)
//            line = reader.readMap()
//        }
//        println(resultList)
//        // Line 2, by column name
//        println(resultList[1]["my column name"])
        }
    }
}