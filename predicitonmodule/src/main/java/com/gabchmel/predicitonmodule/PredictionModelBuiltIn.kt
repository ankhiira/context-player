package com.gabchmel.predicitonmodule

//import smile.classification.RandomForest

import android.content.Context
import android.util.Log
import weka.classifiers.Evaluation
import weka.classifiers.trees.RandomForest
import weka.core.Attribute
import weka.core.DenseInstance
import weka.core.Instances
import weka.core.converters.ArffLoader
import weka.core.converters.ArffSaver
import weka.core.converters.CSVLoader
import java.io.File


class PredictionModelBuiltIn(val context: Context) {

    lateinit var forest: RandomForest
    // Name of the input arff file
    val file = "arffData_converted.arff"
    val csvConvertedFile = "convertedData.csv"

    // Function to read the dataset from arff file
    private fun getDataset(): Instances {

        val initialFile = File(context.filesDir, file)
        val classIdx = 0

        // Load arff file
        val loader = ArffLoader()
        loader.setFile(initialFile)
        val dataSet = loader.dataSet

        // Set index of the class attribute
        dataSet.setClassIndex(classIdx)
        return dataSet
    }

    // Function to create and evaluate model
    fun createModel(classNames: ArrayList<String>) {

        convertCSVtoarrf(context, classNames)

        lateinit var trainingDataSet: Instances
        if (File(context.filesDir, file).exists()) {
            // TODO split train test data na zacatku pred vytvorenim souboru
            trainingDataSet = getDataset()

            forest = RandomForest()
            // Train the model
            forest.buildClassifier(trainingDataSet)
            // Test the dataset
            val eval = Evaluation(trainingDataSet)
            eval.evaluateModel(forest, trainingDataSet)

            // Print the evaluation summary
            println("Decision Tress Evaluation")
            println(eval.toSummaryString())
            print(" the expression for the input data as per algorithm is ")
            println(forest)
            println(eval.toMatrixString())
            println(eval.toClassDetailsString())
            eval.predictions()
        }
    }

    // Function to make prediction on input data
    fun predict(input: DoubleArray, classNames: ArrayList<String>): String {

        // Names of the attributes used in input
        val sinTime = Attribute("sinTime")
        val cosTime = Attribute("cosTime")
        val dayOfWeekSin = Attribute("dayOfWeekSin")
        val dayOfWeekCos = Attribute("dayOfWeekCos")
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
                add(xCoord)
                add(yCoord)
                add(zCoord)
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
                setValue(sinTime, input[0])
                setValue(cosTime, input[1])
                setValue(dayOfWeekSin, input[2])
                setValue(dayOfWeekCos, input[3])
                setValue(xCoord, input[4])
                setValue(yCoord, input[5])
                setValue(zCoord, input[6])
            }
        }

        // reference to dataset
        newInstance.setDataset(dataUnpredicted)

        var className = ""

        // predict new sample
        try {
            val result= forest.classifyInstance(newInstance)
            className = classNames[result.toInt()]
            val msg =
                "Nr: itemNumber, predicted: $className, actual: 2046003820"
            Log.d("WekaTest", msg)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return className
    }

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