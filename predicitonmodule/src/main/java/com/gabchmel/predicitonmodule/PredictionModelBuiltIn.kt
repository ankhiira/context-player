package com.gabchmel.predicitonmodule

import android.content.Context
import com.gabchmel.common.data.ChargingMethod
import com.gabchmel.common.data.ConvertedData
import com.gabchmel.common.data.NetworkType
import com.gabchmel.common.data.UserActivity
import com.gabchmel.common.utils.arffFileName
import com.gabchmel.common.utils.convertedArffFileName
import com.gabchmel.common.utils.convertedCsvFileName
import weka.classifiers.Evaluation
import weka.classifiers.trees.RandomForest
import weka.core.Attribute
import weka.core.DenseInstance
import weka.core.Instances
import weka.core.converters.ArffLoader
import weka.core.converters.ArffSaver
import weka.core.converters.CSVLoader
import weka.filters.Filter
import weka.filters.unsupervised.instance.RemovePercentage
import java.io.File
import java.util.Random


class PredictionModelBuiltIn(val context: Context) {

    private lateinit var forest: RandomForest

    private var wifiList = arrayListOf<UInt>()

    /**
     * Read the dataset from arff file
     *
     * @return
     */
    private fun getDataset(): Instances {
        val arffLoader = ArffLoader()
        val arffFile = File(context.filesDir, convertedArffFileName)
        arffLoader.setFile(arffFile)
        val dataset = arffLoader.dataSet

        // Randomize dataset values
        dataset.randomize(Random(0))

        return dataset
    }

    private fun getTrainDataset(dataset: Instances): Instances {
        val removePercentage = RemovePercentage().apply {
            setInputFormat(dataset)
            percentage = 20.0
        }

        return Filter.useFilter(dataset, removePercentage)
    }

    private fun getTestDataset(dataset: Instances): Instances {
        val removePercentage = RemovePercentage().apply {
            setInputFormat(dataset)
            percentage = 20.0
            invertSelection = true
        }

        return Filter.useFilter(dataset, removePercentage)
    }

    /**
     * Create and evaluate model
     *
     * @param classNames
     * @param wifiList
     * @return
     */
    fun createModel(classNames: ArrayList<String>, wifiList: ArrayList<UInt>): Boolean {

        this.wifiList = wifiList
        if (classNames.size <= 1) {
            return false
        }

        convertCsvToArff(context, classNames, wifiList)

        if (File(context.filesDir, convertedArffFileName).exists()) {
            val originalDataset = getDataset()
            val trainDataset = getTrainDataset(originalDataset)
            val testDataset = getTestDataset(originalDataset)

            // Set index of the class attribute
            trainDataset.setClassIndex(0)
            testDataset.setClassIndex(0)
            originalDataset.setClassIndex(0)

            forest = RandomForest()
            forest.numTrees = 100

            // Train the model
            forest.buildClassifier(trainDataset)

            // Test the model
            val eval = Evaluation(trainDataset)
            eval.evaluateModel(forest, testDataset)

            // Print the evaluation summary
            println("Decision Tress Evaluation")
            println(eval.toSummaryString())
            print(" the expression for the input data as per algorithm is ")
            println(forest)
            println(eval.toMatrixString())
            println(eval.toClassDetailsString())
            eval.predictions()
        }
        return true
    }

    /**
     * Make prediction on input data
     *
     * @param input
     * @param classNames
     * @return
     */
    fun predict(input: ConvertedData, classNames: ArrayList<String>): String {

        val booleanVector = ArrayList<String>(listOf("0", "1"))

        // Names of the attributes used in input
        val sinTime = Attribute("sinTime")
        val cosTime = Attribute("cosTime")
        val dayOfWeekSin = Attribute("dayOfWeekSin")
        val dayOfWeekCos = Attribute("dayOfWeekCos")
        val state = Attribute("state", UserActivity.entries.getStringList())
        val light = Attribute("light")
        val orientation = Attribute("orientation", booleanVector)
        val btConnected = Attribute("btConnected", booleanVector)
        val headphonesPlugged = Attribute("headphonesPlugged", booleanVector)
        val temperature = Attribute("temperature")
        val wifi = Attribute("wifi", wifiList.getStringList())
        val connection = Attribute("connection", NetworkType.entries.getStringList())
        val batteryStatus = Attribute("batteryStatus", booleanVector)
        val chargingType = Attribute("chargingType", ChargingMethod.entries.getStringList())
        val proximity = Attribute("proximity")
        val heartRate = Attribute("heartRate")
        val location = Attribute("location")
        val xCoord = Attribute("xCoord")
        val yCoord = Attribute("yCoord")
        val zCoord = Attribute("zCoord")

        // Create a list of input attributes
        val attributeList = object : ArrayList<Attribute?>(2) {
            init {
                add(Attribute("@@class@@", classNames))
                add(sinTime)
                add(cosTime)
                add(dayOfWeekSin)
                add(dayOfWeekCos)
                add(state)
                add(light)
                add(orientation)
                add(btConnected)
                add(headphonesPlugged)
                add(temperature)
                add(wifi)
                add(connection)
                add(batteryStatus)
                add(chargingType)
                add(proximity)
                add(heartRate)
                add(location)
                add(xCoord)
                add(yCoord)
                add(zCoord)
            }
        }

        // Unpredicted datasets (reference to sample structure for new instances)
        val dataUnpredicted = Instances(
            "TestInstances",
            attributeList,
            1
        )

        // First feature will be the target variable
        dataUnpredicted.setClassIndex(0)

        // Create new instance and assign the attributes their values
        val newInstance = object : DenseInstance(dataUnpredicted.numAttributes()) {
            init {
                setValue(sinTime, input.sinTime)
                setValue(cosTime, input.cosTime)
                setValue(dayOfWeekSin, input.dayOfWeekSin)
                setValue(dayOfWeekCos, input.dayOfWeekCos)
                setValue(state, input.currentActivity.toString())
                setValue(light, input.lightSensorValue.toDouble())
                setValue(orientation, input.isDeviceLying.toString())
                setValue(btConnected, input.bluetoothDeviceConnected.toDouble())
                setValue(headphonesPlugged, input.headphonesPluggedIn.toString())
                setValue(temperature, input.temperature.toDouble())
                setValue(wifi, input.wifi.toString())
                setValue(connection, input.connection.toString())
                setValue(batteryStatus, input.isDeviceCharging.toDouble())
                setValue(chargingType, input.chargingType.toString())
                setValue(proximity, input.proximity.toDouble())
                setValue(heartRate, input.heartRate.toDouble())
                setValue(location, input.locationCluster.toDouble())
                setValue(xCoord, input.xCoord)
                setValue(yCoord, input.yCoord)
                setValue(zCoord, input.zCoord)
            }
        }

        // reference to dataset
        newInstance.setDataset(dataUnpredicted)

        var className = ""

        // predict new sample
        try {
            val resultArray = Array<String?>(4) { null }

            // Classify instance multiple times to get more results
            for (i in 0..3) {
                val result = forest.classifyInstance(newInstance)
                resultArray[i] = classNames[result.toInt()]
            }

            className = resultArray.random().toString()
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return className
    }

    private fun List<Any>.getStringList() = this.map { it.toString() }

    /**
     * Convert csv to arff file representation
     *
     * @param context
     * @param classNames
     * @param wifiList
     */
    private fun convertCsvToArff(
        context: Context,
        classNames: ArrayList<String>,
        wifiList: ArrayList<UInt>
    ) {
        val csvFile = File(context.filesDir, convertedCsvFileName)

        if (!csvFile.exists()) {
            return
        }

        val csvLoader = CSVLoader()
        csvLoader.setSource(csvFile)
        val data = csvLoader.dataSet

        // convert data to arff format
        ArffSaver().apply {
            instances = data
            setFile(File(context.filesDir, arffFileName))
            writeBatch()
        }

        // create nominal attribute label
        val arffFile = File(context.filesDir, arffFileName)
        var text = arffFile.readText()

        // Replace attribute description in arff file
        val classNamesString = classNames.joinToString(separator = ",")
        text = replaceNumeric(text, "class", classNamesString)

        text = replaceNominal(text, "state", UserActivity.getEntriesString())
        text = replaceNominal(text, "connection", NetworkType.getEntriesString())
        text = replaceNominal(text, "chargingType", ChargingMethod.getEntriesString())

        text = replaceToBoolean(text, "batteryStatus")
        text = replaceToBoolean(text, "orientation")
        text = replaceToBoolean(text, "btConnected")
        text = replaceToBoolean(text, "headphonesPlugged")

        if (wifiList.isNotEmpty()) {
            val wifiListString = wifiList.joinToString(separator = ",") { wifiName ->
                wifiName.toString()
            }
            text = replaceNumeric(text, "wifi", wifiListString)
        }

        val convertedArffFile = File(context.filesDir, convertedArffFileName)
        convertedArffFile.writeText(text)
    }

    private fun replaceNominal(text: String, attribute: String, nominalValues: String): String {
        return text.replace(
            regex = "@attribute $attribute \\{.*\\}".toRegex(),
            replacement = "@attribute $attribute {$nominalValues}"
        )
    }

    private fun replaceToBoolean(text: String, attribute: String): String {
        return text.replace(
            oldValue = "@attribute $attribute numeric",
            newValue = "@attribute $attribute {0,1}"
        )
    }

    private fun replaceNumeric(text: String, attribute: String, values: String): String {
        return text.replace(
            oldValue = "@attribute $attribute numeric",
            newValue = "@attribute $attribute {$values}"
        )
    }

    @Suppress("UNCHECKED_CAST")
    fun savePredictionToFile(convertedData: ConvertedData, prediction: String) {
        // Save predictions with their input to CSV file
        val predictionFile = File(context.filesDir, "predictions.csv")
        var predictionString = "$prediction,"
//        ConvertedData::class.primaryConstructor?.parameters?.let { parameters ->
//            for (property in parameters) {
//                val propertyNew = convertedData::class.members
//                    .first { it.name == property.name } as KProperty1<Any, *>
//                predictionString += when (property.name) {
//                    "wifi" -> "${convertedData.wifi},"
//                    else -> "${propertyNew.get(convertedData)},"
//                }
//            }
//        }

        predictionString = predictionString.dropLast(1).apply { this + "\n" }
        predictionFile.appendText(predictionString)
    }
}