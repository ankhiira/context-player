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
    private fun getDataset(): Triple<Instances, Instances, Instances> {

        val arffFile = File(context.filesDir, convertedArffFileName)
        val classIdx = 0

        // Load arff file
        val loader = ArffLoader()
        loader.setFile(arffFile)
        val dataset = loader.dataSet

        // Randomize dataset values
        //TODO is seed 0 doing something?
        dataset.randomize(Random(0))

        // If the class is of the nominal type, stratify the data
        // dataSet.stratify(10)

        // Remove test percentage from data to get the train set
        var removePercentage = RemovePercentage().apply {
            setInputFormat(dataset)
            percentage = 20.0
        }
        val train = Filter.useFilter(dataset, removePercentage)

        // Remove train percentage from data to get the test set
        //TODO check if setInputFormat and percentage necessary
        removePercentage = RemovePercentage().apply {
            setInputFormat(dataset)
            percentage = 20.0
            invertSelection = true
        }
        val test = Filter.useFilter(dataset, removePercentage)

        // Set index of the class attribute
        train.setClassIndex(classIdx)
        test.setClassIndex(classIdx)

        return Triple(train, test, dataset)
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

            val (trainingDataSet, testDataSet, origDataSet) = getDataset()

            trainingDataSet.setClassIndex(0)
            testDataSet.setClassIndex(0)
            origDataSet.setClassIndex(0)

            val test = trainingDataSet.equalHeaders(testDataSet)

            forest = RandomForest()

            // Set number of trees to 100
            forest.numTrees = 100
            // Train the model
            forest.buildClassifier(trainingDataSet)
            // Test the model
            val eval = Evaluation(trainingDataSet)
            eval.evaluateModel(forest, testDataSet)

//            eval.crossValidateModel(forest, origDataSet, 10, Random(1))
//            println("Estimated Accuracy: ${eval.pctCorrect()}")

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

        val wifiNamesList = mutableListOf<String>()
        wifiList.forEach { wifiName ->
            wifiNamesList.add(wifiName.toString())
        }

        val stateVector = ArrayList<String>(UserActivity.entries.map { it.toString() })
        val connectionVector = ArrayList<String>(NetworkType.entries.map { it.toString() })
        val chargingTypeVector = ArrayList<String>(ChargingMethod.entries.map { it.toString() })
        val booleanVector = ArrayList<String>(listOf("0", "1"))
        val wifiListVec = ArrayList<String>(wifiNamesList)

        // Names of the attributes used in input
        val sinTime = Attribute("sinTime")
        val cosTime = Attribute("cosTime")
        val dayOfWeekSin = Attribute("dayOfWeekSin")
        val dayOfWeekCos = Attribute("dayOfWeekCos")
        val state = Attribute("state", stateVector)
        val light = Attribute("light")
        val orientation = Attribute("orientation", booleanVector)
        val btConnected = Attribute("btConnected", booleanVector)
        val headphonesPlugged = Attribute("headphonesPlugged", booleanVector)
        val temperature = Attribute("temperature")
        val wifi = Attribute("wifi", wifiListVec)
        val connection = Attribute("connection", connectionVector)
        val batteryStatus = Attribute("batteryStatus", booleanVector)
        val chargingType = Attribute("chargingType", chargingTypeVector)
        val proximity = Attribute("proximity")
        val heartRate = Attribute("heartRate")
        val location = Attribute("location")
        val xCoord = Attribute("xCoord")
        val yCoord = Attribute("yCoord")
        val zCoord = Attribute("zCoord")

        // Create a list of input attributes
        val attributeList = object : ArrayList<Attribute?>(2) {
            init {
                val attributeClass = Attribute("@@class@@", classNames)
                add(attributeClass)
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

        // unpredicted data sets (reference to sample structure for new instances)
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
        val resultArray = Array<String?>(4) { null }

        // predict new sample
        try {
            // Classify instance multiple times to get more results
            for (i in 0..3) {
                val result = forest.classifyInstance(newInstance)
                resultArray[i] = classNames[result.toInt()]
            }
            className = resultArray.random().toString()
            // Log.d("random", className)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return className
    }

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
}