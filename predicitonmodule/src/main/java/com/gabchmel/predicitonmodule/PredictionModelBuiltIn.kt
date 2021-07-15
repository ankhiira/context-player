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
    private val csvConvertedFile = "convertedData.csv"

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
    fun createModel(classNames: ArrayList<String>): Boolean {

        convertCSVtoarrf(context, classNames)

        if (classNames.size == 1) {
            return false
        }

        if (File(context.filesDir, file).exists()) {

            val (trainingDataSet, testDataSet) = getDataset()

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

        val fastVector = FastVector<String>(2)
        fastVector.addElement("NONE")
        fastVector.addElement("STILL")

        // Names of the attributes used in input
        val sinTime = Attribute("sinTime")
        val cosTime = Attribute("cosTime")
        val dayOfWeekSin = Attribute("dayOfWeekSin")
        val dayOfWeekCos = Attribute("dayOfWeekCos")
//        val xCoord = Attribute("xCoord")
//        val yCoord = Attribute("yCoord")
//        val zCoord = Attribute("zCoord")
        val state = Attribute("state", fastVector)

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
                Log.d("WekaTest", "Nr: itemNumber, predicted: $className")
            }
            className = resultArray.random().toString()
            Log.d("random", className)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return className
    }

    // Function to convert CSV file to arff file representation
    private fun convertCSVtoarrf(context: Context, classNames: ArrayList<String>) {

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

            // Replace attribute description in arff file
            text = text.replace(
                "@attribute class numeric", "@attribute class {" +
                        classNamesString + "}"
            )
            fileOut.writeText(text)
        }
    }
}