package com.gabchmel.sensorprocessor.utils

import android.app.Service
import android.content.Context
import android.util.Log
import com.gabchmel.common.data.ChargingMethod
import com.gabchmel.common.data.ConvertedData
import com.gabchmel.common.data.NetworkType
import com.gabchmel.common.data.SensorValues
import com.gabchmel.common.data.UserActivity
import com.gabchmel.common.utils.convertedCsvFileName
import com.gabchmel.common.utils.dataCsvFileName
import com.gabchmel.sensorprocessor.data.model.ProcessedCsvValues
import com.github.doyaaaaaken.kotlincsv.dsl.csvReader
import kotlinx.datetime.toLocalDateTime
import java.io.File
import java.io.IOException
import kotlin.math.PI
import kotlin.math.asin
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt
import kotlin.reflect.KProperty1
import kotlin.reflect.full.primaryConstructor


object InputProcessHelper {

    private val locationList = mutableListOf<LocationClusteringAlg.Location>()
    private val locationListDouble = mutableListOf<DoubleArray>()

    // Calculation of haversine distance, serves for location clustering
    private val haversineDistance = object :
        LocationClusteringAlg.Distance<LocationClusteringAlg.Location> {
        override fun LocationClusteringAlg.Location.distance(to: LocationClusteringAlg.Location): Double {
            val constR = 6371000.0
            val lat1 = Math.toRadians(lat)
            val lat2 = Math.toRadians(to.lat)
            val lon1 = Math.toRadians(lon)
            val lon2 = Math.toRadians(to.lon)

            return 2 * constR * asin(
                sqrt(
                    ((lat1 - lat2) / 2).pow(2) + cos(lat1) * cos(lat2) * sin((lon1 - lon2) / 2).pow(
                        2
                    )
                )
            )
        }
    }

    fun getProcessedSensorValues(sensorData: SensorValues): ConvertedData {
        val currentTime = sensorData.currentTime
        val latitude = sensorData.latitude
        val longitude = sensorData.longitude

        val locationCluster = 0

        var dayOfWeek = 0
        var timeInSeconds = 0

        currentTime?.let {
            //TODO NEW : check the representation
            // Convert to similar representation as in other models
//            dayOfWeek = if (currentTime.day == 0) 6 else currentTime.day - 1
            dayOfWeek = currentTime.dayOfWeek.ordinal

            // Convert current time to seconds
            timeInSeconds = (currentTime.hour * 60 + currentTime.minute) * 60 + currentTime.second
        }

        // Get number of seconds in a day
        val secondsInDay = 24 * 60 * 60

        // Represent time in it's circular representation
        val sinTime = sin(2 * PI * timeInSeconds / secondsInDay)
        val cosTime = cos(2 * PI * timeInSeconds / secondsInDay)

        // Represent day of the week in it's circular representation
        val dayOfWeekSin = sin(dayOfWeek * (2 * PI / 7))
        val dayOfWeekCos = cos(dayOfWeek * (2 * PI / 7))

        // Convert longitude and latitude to x,y,z coordinates
        val xCoord = cos(latitude) * cos(longitude)
        val yCoord = cos(latitude) * sin(longitude)
        val zCoord = sin(latitude)

//        val location = LocationClusteringAlg.Location(
//            longitude, latitude
//        )
//        locationList.add(location)

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

        // Return the data in the ConvertedData class
        return ConvertedData(
            sinTime = sinTime,
            cosTime = cosTime,
            dayOfWeekSin = dayOfWeekSin,
            dayOfWeekCos = dayOfWeekCos,
            currentActivity = sensorData.userActivity,
            lightSensorValue = sensorData.lightSensorValue,
            isDeviceLying = sensorData.isDeviceLying.toInt(),
            bluetoothDeviceConnected = sensorData.isBluetoothDeviceConnected.toInt(),
            headphonesPluggedIn = sensorData.isHeadphonesPluggedIn.toInt(),
            temperature = sensorData.temperature,
            wifi = sensorData.wifiSsid ?: 0u,
            connection = sensorData.networkConnectionType,
            isDeviceCharging = sensorData.isDeviceCharging.toInt(),
            chargingType = sensorData.chargingType,
            proximity = sensorData.proximity,
            heartRate = sensorData.heartRate,
            locationCluster = locationCluster,
            xCoord = xCoord,
            yCoord = yCoord,
            zCoord = zCoord
        )
    }

    private fun Boolean?.toInt(): Int {
        return if (this == true) 1 else 0
    }

    // Give header to the file and process dates
    fun processInputCSV(context: Context): ProcessedCsvValues {

        val classNames = arrayListOf<String>()
        val wifiList = arrayListOf<UInt>()

        // Convert the whole file each time
        val convertedCsvFile = File(context.filesDir, convertedCsvFileName)
        convertedCsvFile.writeText(
            "class," +
                    "sinTime," +
                    "cosTime," +
                    "dayOfWeekSin," +
                    "dayOfWeekCos," +
                    "state," +
                    "light," +
                    "orientation," +
                    "BTConnected," +
                    "headphonesPlugged," +
                    "temperature," +
                    "wifi," +
                    "connection," +
                    "batteryStatus," +
                    "chargingType," +
                    "proximity," +
                    "heartRate," +
                    "location," +
                    "xCoord," +
                    "yCoord," +
                    "zCoord" + "\n"
        )

        val csvFile = File(context.filesDir, dataCsvFileName)
        if (csvFile.exists()) {
            csvReader().open(csvFile) {
                readAllAsSequence()
                    .map { row ->
                        // Check if the target class is int he list of classes, if not add it
                        if (!classNames.contains(row[0])) {
                            classNames.add(row[0])
                        }

                        // Check if the SensorData structure changed
                        val prefs =
                            context.getSharedPreferences("MyPrefsFile", Service.MODE_PRIVATE)
                        val counterOld = prefs.getInt("csv", 0)
                        if (counterOld != row.size && counterOld != 0) {
                            // If yes, delete the data.csv file
                            val dataInputFile = File(context.filesDir, dataCsvFileName)
                            if (dataInputFile.exists()) {
                                context.deleteFile(dataCsvFileName)
                            }
                        }

                        // TODO use location clustering alg
                        // Add latitude and longitude to dataset for location clustering
//                        val location = LocationClusteringAlg.Location(
//                            row[3].toDouble(), row[2].toDouble()
//                        )
//                        locationList.add(location)
//                        locationListDouble.add(doubleArrayOf(row[3].toDouble(), row[2].toDouble()))

                        // Convert the CSV row to SensorData class
                        //TODO don't depend on position
                        row[0] to SensorValues(
                            row[1].toLocalDateTime(),
                            row[2].toDouble(),
                            row[3].toDouble(),
                            UserActivity.valueOf(row[4]),
                            row[5].toFloat(),
                            row[6].toFloat(),
                            row[7].toFloat(),
                            row[8].toBoolean(),
                            row[9].toBoolean(),
                            row[10].toBoolean(),
                            if (row[11] != "null") row[11].toUInt() else null,
                            NetworkType.valueOf(row[12]),
                            row[13].toBoolean(),
                            ChargingMethod.valueOf(row[14]),
                            row[15].toFloat()
                        )
                    }.map {
                        // Process the data to be suitable as model input
                        it.first to getProcessedSensorValues(it.second)
                    }.forEach { pair ->
                        try {
                            // Write to csv file
                            val data: ConvertedData = pair.second
                            var csvString = ""
                            // Iterate over object properties
                            ConvertedData::class.primaryConstructor?.parameters?.let { parameters ->
                                for (property in parameters) {
                                    val propertyNew = data::class.members
                                        .first { it.name == property.name } as KProperty1<Any, *>
                                    // Check for wifi property because with UInt it is not working well
                                    if (property.name == "wifi") {
                                        csvString += "${data.wifi},"
                                        // Save every individual wifi into the list
                                        if (!wifiList.contains(data.wifi)) {
                                            data.wifi.let { it1 -> wifiList.add(it1) }
                                        }
                                    } else {
                                        csvString += "${propertyNew.get(data)},"
                                    }
                                }
                            }

                            // Drop comma after last element
                            csvString = csvString.dropLast(1)
                            csvString += "\n"
                            convertedCsvFile.appendText(pair.first + "," + csvString)
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

        return ProcessedCsvValues(classNames, wifiList)
    }
}