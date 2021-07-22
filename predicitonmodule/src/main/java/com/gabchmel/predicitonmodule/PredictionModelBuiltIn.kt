package com.gabchmel.predicitonmodule

//import smile.classification.RandomForest

import android.content.Context
import android.util.Log
import com.gabchmel.common.ConvertedData
import weka.classifiers.Evaluation
import weka.classifiers.trees.RandomForest
import weka.core.Attribute
import weka.core.DenseInstance
import weka.core.FastVector
import weka.core.Instances
import weka.core.converters.ArffLoader
import weka.core.converters.ArffSaver
import weka.core.converters.CSVLoader
import java.io.File
import java.util.*
import kotlin.math.roundToInt


class PredictionModelBuiltIn(val context: Context) {

    private lateinit var forest: RandomForest
    // Name of the input arff file
    val file = "arffData_converted.arff"
    private val csvConvertedFile = "convertedLocData.csv"

    // Function to read the dataset from arff file
    private fun getDataset(): Pair<Instances, Instances> {

        val initialFile = File(context.filesDir, file)
        val classIdx = 0

        // Load arff file
        val loader = ArffLoader()
        loader.setFile(initialFile)
        val dataSet = loader.dataSet

        // Set index of the class attribute
        dataSet.setClassIndex(classIdx)

        // Randomize dataset values
        dataSet.randomize(Random(0))

        // Split train and test data in 80 percent to train data
        val trainSize = (dataSet.numInstances() * 0.8).roundToInt()
        val testSize = dataSet.numInstances() - trainSize
        val train = Instances(dataSet, 0, trainSize)
        val test = Instances(dataSet, trainSize, testSize)

        return Pair(train, test)
    }

    // Function to create and evaluate model
    fun createModel(classNames: ArrayList<String>, wifiList: ArrayList<UInt>): Boolean {

        convertCSVtoarrf(context, classNames, wifiList)

        if (classNames.size == 1) {
            return false
        }

        if (File(context.filesDir, file).exists()) {

            val (trainingDataSet, testDataSet) = getDataset()

            val test = trainingDataSet.equalHeaders(testDataSet)

            forest = RandomForest()
            // Train the model
            forest.buildClassifier(trainingDataSet)
            // Test the dataset
            val eval = Evaluation(trainingDataSet)
            eval.evaluateModel(forest, testDataSet)

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

    // Function to make prediction on input data
    fun predict(input: ConvertedData, classNames: ArrayList<String>): String {

        // To create attributes with all possible values
        val stateList = listOf("IN_VEHICLE","STILL","WALKING","RUNNING","UNKNOWN")
        val connectionList = listOf("NONE","TRANSPORT_CELLULAR","TRANSPORT_WIFI","TRANSPORT_ETHERNET")
        val batteryStatusList = listOf("NONE","CHARGING","NOT_CHARGING")
        val chargingTypeList = listOf("NONE","USB","AC","WIRELESS")

        val stateVector = FastVector<String>(5)
        stateVector.addAll(stateList)

        val connectionVector = FastVector<String>(4)
        connectionVector.addAll(connectionList)

        val batteryStatVector = FastVector<String>(3)
        batteryStatVector.addAll(batteryStatusList)

        val chargingTypeVector = FastVector<String>(4)
        chargingTypeVector.addAll(chargingTypeList)

        // Names of the attributes used in input
        val sinTime = Attribute("sinTime")
        val cosTime = Attribute("cosTime")
        val dayOfWeekSin = Attribute("dayOfWeekSin")
        val dayOfWeekCos = Attribute("dayOfWeekCos")
//        val xCoord = Attribute("xCoord")
//        val yCoord = Attribute("yCoord")
//        val zCoord = Attribute("zCoord")
        val state = Attribute("state", stateVector)
        val light = Attribute("light")
        val orientation = Attribute("orientation")
        val BTconnected = Attribute("BTconnected")
        val headphonesPlugged = Attribute("headphonesPlugged")
        val pressure = Attribute("pressure")
        val temperature = Attribute("temperature")
        val wifi = Attribute("wifi")
        val connection = Attribute("connection", connectionVector)
        val batteryStatus = Attribute("batteryStatus", batteryStatVector)
        val chargingType = Attribute("chargingType", chargingTypeVector)
        val proximity = Attribute("proximity")
        val humidity = Attribute("humidity")
        val heartRate = Attribute("heartRate")
        val heartBeat = Attribute("heartBeat")
        val location = Attribute("location")

        // Create a list of input attributes
        val attributeList = object : ArrayList<Attribute?>(2) {
            init {
                val attributeClass = Attribute("@@class@@", classNames)
                add(attributeClass)
                add(sinTime)
                add(cosTime)
                add(dayOfWeekSin)
                add(dayOfWeekCos)
//                add(xCoord)
//                add(yCoord)
//                add(zCoord)
                add(state)
                add(light)
                add(orientation)
                add(BTconnected)
                add(headphonesPlugged)
                add(pressure)
                add(temperature)
                add(wifi)
                add(connection)
                add(batteryStatus)
                add(chargingType)
                add(proximity)
                add(humidity)
                add(heartRate)
                add(heartBeat)
                add(location)
            }
        }

        // unpredicted data sets (reference to sample structure for new instances)
        val dataUnpredicted = Instances(
            "TestInstances",
            attributeList, 1
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
//                setValue(xCoord, input[4])
//                setValue(yCoord, input[5])
//                setValue(zCoord, input[6])
                setValue(state, input.state)
                setValue(light, input.lightSensorValue!!.toDouble())
                setValue(orientation, input.deviceLying!!.toDouble())
                setValue(BTconnected, input.BTdeviceConnected!!.toDouble())
                setValue(pressure, input.pressure!!.toDouble())
                setValue(temperature, input.temperature!!.toDouble())
                setValue(wifi, input.wifi!!.toDouble())
                setValue(connection, input.connection)
                setValue(batteryStatus, input.batteryStatus)
                setValue(chargingType, input.chargingType)
                setValue(proximity, input.proximity!!.toDouble())
                setValue(humidity, input.humidity!!.toDouble())
                setValue(heartRate, input.heartRate!!.toDouble())
                setValue(heartBeat, input.heartBeat!!.toDouble())
                setValue(location, input.locationCluster.toDouble())
            }
        }

        // reference to dataset
        newInstance.setDataset(dataUnpredicted)

        var className = ""
        val resultArray = Array<String?>(4) {null}

        // predict new sample
        try {
            // Classify instance multiple times to get more results
            for (i in 0..3) {
                val result = forest.classifyInstance(newInstance)
                resultArray[i] = classNames[result.toInt()]
//                Log.d("WekaTest", "Nr: itemNumber, predicted: ${resultArray[i]}")
            }
            className = resultArray.random().toString()
            Log.d("random", className)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return className
    }

    // Function to convert CSV file to arff file representation
    private fun convertCSVtoarrf(context: Context, classNames: ArrayList<String>,
                                 wifiList: ArrayList<UInt>) {

        // load the CSV file
        val load = CSVLoader()
        val csvFile = File(context.filesDir, csvConvertedFile)

        if(csvFile.exists()) {
            load.setSource(csvFile)
            val data = load.dataSet

            // convert data to arff format
            val arffSaver = ArffSaver()
            arffSaver.instances = data
            arffSaver.setFile(File(context.filesDir, "arffData.arff"))
            arffSaver.writeBatch()

            // create nominal attribute label
            val file = File(context.filesDir, "arffData.arff")
            val fileOut = File(context.filesDir, "arffData_converted.arff")
            var text = file.readText()

            val classNamesString = classNames.joinToString(separator = ",") { className ->
                className
            }

            val wifiListString = wifiList.joinToString(separator = ",") { wifiName ->
                wifiName.toString()
            }

            // Replace attribute description in arff file
            text = text.replace(
                "@attribute class numeric", "@attribute class {" +
                        classNamesString + "}"
            )

            text = text.replace(
                "@attribute state \\{.*\\}".toRegex(), "@attribute state {" +
                        "IN_VEHICLE,STILL,WALKING,RUNNING,UNKNOWN}"
            )

            text = text.replace(
                "@attribute connection \\{.*\\}".toRegex(), "@attribute connection {" +
                        "NONE,TRANSPORT_CELLULAR,TRANSPORT_WIFI,TRANSPORT_ETHERNET}"
            )

            text = text.replace(
                "@attribute batteryStatus \\{.*\\}".toRegex(), "@attribute batteryStatus {" +
                        "NONE,CHARGING,NOT_CHARGING}"
            )

            text = text.replace(
                "@attribute chargingType \\{.*\\}".toRegex(), "@attribute chargingType {" +
                        "NONE,USB,AC,WIRELESS}"
            )

            if(wifiList.isNotEmpty()) {
                // Replace attribute description in arff file
                text = text.replace(
                    "@attribute wifi numeric", "@attribute wifi {" +
                            wifiListString + "}"
                )
            }
            fileOut.writeText(text)
        }
    }
}