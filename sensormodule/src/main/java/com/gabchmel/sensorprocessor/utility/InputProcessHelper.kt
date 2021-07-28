package com.gabchmel.sensorprocessor.utility

import android.app.Service
import android.content.Context
import android.util.Log
import com.gabchmel.common.ConvertedData
import com.gabchmel.sensorprocessor.LocationClusteringAlg
import com.gabchmel.sensorprocessor.SensorData
import com.github.doyaaaaaken.kotlincsv.dsl.csvReader
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.*
import kotlin.reflect.KProperty1
import kotlin.reflect.full.primaryConstructor


object InputProcessHelper {

    private val locationList = mutableListOf<LocationClusteringAlg.Location>()
    private val locationListDouble = mutableListOf<DoubleArray>()

    // Calculation of haversine distance, serves for location clustering
    private val haversineDistance = object :
        LocationClusteringAlg.Distance<LocationClusteringAlg.Location> {
        override fun LocationClusteringAlg.Location.distance(to: LocationClusteringAlg.Location): Double {
            val R = 6371000.0
            val lat1 = Math.toRadians(lat)
            val lat2 = Math.toRadians(to.lat)
            val lon1 = Math.toRadians(lon)
            val lon2 = Math.toRadians(to.lon)

            return 2 * R * asin(
                sqrt(
                    ((lat1 - lat2) / 2).pow(2) + cos(lat1) * cos(lat2) * sin((lon1 - lon2) / 2).pow(
                        2
                    )
                )
            )
        }
    }

    fun inputProcessHelper(sensorData: SensorData): ConvertedData {

        val currentTime = sensorData.currentTime
        val latitude = sensorData.latitude
        val longitude = sensorData.longitude

        var dayOfWeek = 0
        var timeInSeconds = 0

        currentTime?.let {
            // Convert to similar representation as in other models
            dayOfWeek =
                if (currentTime.day == 0) {
                    6
                } else {
                    currentTime.day - 1
                }

            // Convert current time to seconds
            timeInSeconds =
                (currentTime.hours * 60 + currentTime.minutes) * 60 + currentTime.seconds
        }
        // Get number of seconds in a day
        val secondsInDay = 24 * 60 * 60

        // Represent time in it's circular representation
        val sinTime = sin(2 * PI * timeInSeconds / secondsInDay)
        val cosTime = cos(2 * PI * timeInSeconds / secondsInDay)

        // Represent day of the week in it's circular representation
        val dayOfWeekSin = sin(dayOfWeek * (2 * PI / 7))
        val dayOfWeekCos = cos(dayOfWeek * (2 * PI / 7))

        // Convert longitude and latitude to x,y,z coordinates - haversine distance
        val xCoord = latitude?.let { lat ->
            longitude?.let { long -> cos(lat) * cos(long) }
        }
        val yCoord = latitude?.let { lat ->
            longitude?.let { long -> cos(lat) * sin(long) }
        }
        val zCoord = latitude?.let { lat ->
            sin(lat)
        }

//        val location = LocationClusteringAlg.Location(
//            longitude!!, latitude!!
//        )
//        locationList.add(location)

        val locationCluster = 0

//        val clusters = DblClusters<DoubleArray>(2, 10)
////        clusters.setKeyer()
//
//        for (loc in locationListDouble) {
//            clusters.add(1.0, loc, DoubleArray(0))
//        }
//
//        val results = clusters.results()

//        // Clustering the location values
//        val dbscan = LocationClusteringAlg.DBSCAN(150.0, 4)
//        val dbscanClusters = dbscan.fit_transform(locationList, haversineDistance)
//
//        val locationCluster = if (dbscanClusters.last() == LocationClusteringAlg.Outsider) {
//            0
//        } else {
//            (dbscanClusters.last() as LocationClusteringAlg.Identified).id
//        }

        return ConvertedData(sinTime, cosTime, dayOfWeekSin, dayOfWeekCos, sensorData.currentState,
            sensorData.lightSensorValue, sensorData.deviceLying, sensorData.BTdeviceConnected,
            sensorData.headphonesPluggedIn, sensorData.pressure,sensorData.temperature,
            sensorData.wifi,sensorData.connection,sensorData.batteryStatus,sensorData.chargingType,
            sensorData.proximity,sensorData.humidity,sensorData.heartBeat,sensorData.heartRate,
            locationCluster, (xCoord ?: 0.0), (yCoord ?: 0.0), (zCoord ?: 0.0))
    }

    // Give header to the file and process dates
    suspend fun processInputCSV(context: Context): Pair<ArrayList<String>, ArrayList<UInt>> = withContext(Dispatchers.Default) {
        val inputFile = File(context.filesDir, "data.csv")
        val csvFile = File(context.filesDir, "convertedData.csv")
        val classNames = arrayListOf<String>()

        // Convert the whole file each time
        csvFile.writeText(
            "class,sinTime,cosTime,dayOfWeekSin," +
                "dayOfWeekCos,state,light,orientation,BTconnected,headphonesPlugged," +
                "pressure,temperature,wifi,connection,batteryStatus,chargingType," +
                "proximity,humidity,heartRate,heartBeat,location,xCoord,yCoord,zCoord" + "\n"
        )

        val wifiList = arrayListOf<UInt>()

        if (inputFile.exists()) {
            csvReader().open(inputFile) {
                readAllAsSequence()
                    .map { row ->
//                        val dateNew = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
//                            val format =
//                                DateTimeFormatter.ofPattern(
//                                    "E MMM dd HH:mm:ss ZZZZ yyyy",
//                                    context.resources.configuration.locales.get(0)
//                                )
//                            val localDate = LocalDateTime.parse(row[1], format)
//                            Date.from(localDate.atZone(ZoneId.systemDefault()).toInstant())
//                        } else {
                            val formatter =
                                SimpleDateFormat("E MMM dd HH:mm:ss ZZZZ yyyy", Locale.ENGLISH)
                            val dateNew = formatter.parse(row[1])!!
//                        }

                        // Check if the target class is int he list of classes, if not add it
                        if (!classNames.contains(row[0])) {
                            classNames.add(row[0])
                        }

                        // Check if the SensorData structure changed
                        val prefs = context.getSharedPreferences("MyPrefsFile", Service.MODE_PRIVATE)
                        val counterOld = prefs.getInt("csv", 0)
                        if (counterOld != row.size && counterOld != 0) {
                            // If yes, delete the data.csv file
                            val dataInputFile = File(context.filesDir, "data.csv")
                            if (dataInputFile.exists()) {
                                context.deleteFile("data.csv")
                            }
                        }

                        // Add latitude and longitude to dataset for location clustering
//                        val location = LocationClusteringAlg.Location(
//                            row[3].toDouble(), row[2].toDouble()
//                        )
//                        locationList.add(location)
//                        locationListDouble.add(doubleArrayOf(row[3].toDouble(), row[2].toDouble()))

                        row[0] to SensorData(
                            dateNew, row[2].toDouble(), row[3].toDouble(), row[4], row[5].toFloat(),
                            row[6].toFloat(), row[7].toFloat(),
                            row[8].toFloat(), row[9].toFloat(), row[10].toFloat(), row[11].toUInt(),
                            row[12], row[13], row[14], row[15].toFloat(), row[16].toFloat(),
                            row[17].toFloat(), row[18].toFloat()
                        )
                    }.map {
                        // Process the data to be suitable as model input
                        it.first to inputProcessHelper(it.second)
                    }.forEach {
                        // TODO if file exists, only append
//                        if (!csvFile.exists()) {
//                            csvFile.writeText(
//                                "class,sinTime,cosTime,dayOfWeekSin," +
//                                        "dayOfWeekCos,xCoord,yCoord,zCoord" + "\n"
//                            )
//                        }
                        try {
                            // Write to csv file
//                            val data = it.second.joinToString(separator = ",", postfix = "\n")
                            val data : ConvertedData = it.second
                            var csvString = ""
                            // Iterate over object properties
                            for (property in ConvertedData::class.primaryConstructor?.parameters!!) {
                                val propertyNew = data::class.members
                                    .first { it.name == property.name } as KProperty1<Any, *>
                                // Check for wifi property because with UInt it is not working well
                                if (property.name == "wifi") {
                                    csvString += "${data.wifi},"
                                    // Save every individual wifi into the list
                                    if(!wifiList.contains(data.wifi)) {
                                        data.wifi?.let { it1 -> wifiList.add(it1) }
                                    }
                                } else {
                                    csvString += "${propertyNew.get(data)},"
                                }
                            }
                            // Drop comma after last element
                            csvString = csvString.dropLast(1)
                            csvString += "\n"
                            csvFile.appendText(it.first + "," + csvString)
                        } catch (e: IOException) {
                            Log.e("Err", "Couldn't write to file", e)
                        }
                    }
            }
        } else {
            Log.e("File", "The input file doesn't exist")
        }

        var dbscanClusters = emptyList<LocationClusteringAlg.Cluster>()
        var index = 0
        var first = true

//        GlobalScope.launch(Dispatchers.Default) {
//            // Clustering the location values
//            val dbscan = LocationClusteringAlg.DBSCAN(150.0, 2)
//            dbscanClusters = dbscan.fit_transform(locationList, haversineDistance)
//        }

        // File to save data with clustered location
        val locationNewFile = File(context.filesDir, "convertedLocData.csv")
        locationNewFile.writeText("")

//        if (csvFile.exists()) {
//            csvReader().open(csvFile) {
//                readAllAsSequence()
//                    .forEach { row ->
//                        // Convert row to String without spaces
//                        var rowNew = row.toString().replace("\\s".toRegex(), "")
//                        rowNew = rowNew.substring(1, rowNew.length-3)
//                        var connected = ""
//                        // Detecting the first row, which has header information
//                        if (!first) {
//                            // Add location cluster value to others
//                            connected = if (dbscanClusters[index] == LocationClusteringAlg.Outsider) {
//                                "$rowNew,${0}\n"
//                            } else {
//                                "$rowNew,${(dbscanClusters[index] as LocationClusteringAlg.Identified).id}\n"
//                            }
//                            index++
//                        } else {
//                            first = false
//                            connected = "$rowNew\n"
//                        }
//                        locationNewFile.appendText(connected)
//                    }
//            }
//        }

        return@withContext Pair(classNames, wifiList)
    }
}