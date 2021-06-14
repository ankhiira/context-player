package com.gabchmel.predicitonmodule

//import smile.classification.RandomForest

import android.content.Context
import android.util.Log
import android.widget.Toast
import weka.classifiers.Evaluation
import weka.classifiers.trees.RandomForest
import weka.core.Attribute
import weka.core.DenseInstance
import weka.core.Instances
import weka.core.converters.ArffLoader
import weka.core.converters.ArffSaver
import weka.core.converters.CSVLoader
import weka.filters.Filter
import weka.filters.unsupervised.attribute.NumericToNominal
import java.io.File


class PredictionModelBuiltIn(context: Context) {

    val context = context

    lateinit var forest: RandomForest

    // input arff file
    val file = "arffData2.arff"

//    fun createModel2() {

//        val initialFile = File(context.filesDir, "convertedData.csv").absolutePath
//
//        val file = context.getFileStreamPath("convertedData.csv")
//
//        if (file.exists()) {
//            Log.d("exists","jej")
//
//        val input = read.csv(initialFile)
//        val rf = randomForest(Formula.lhs("class"), input)
//            Log.d("RF", "OOB error: ${rf}")
//        }
//    }

    private fun getDataset(fileName: String): Instances {

        val initialFile = File(context.filesDir, file)
        val classIdx = 0

        val loader = ArffLoader()
//        val loader = CSVLoader()
        loader.setFile(initialFile)
        val dataSet = loader.dataSet

        val converter = NumericToNominal()
        val options = arrayOfNulls<String>(2)
        options[0] = "-R"
        options[1] = "1"
        converter.options = options
        converter.setInputFormat(dataSet)
        converter.attributeIndices

        val newData: Instances = Filter.useFilter(dataSet, converter)

        println("Before")
        for (i in 0..2) {
            println("Nominal? " + dataSet.attribute(i).isNominal)
        }

        println("After")
        for (i in 0..2) {
            println("Nominal? " + newData.attribute(i).isNominal)
        }

        /** set the index based on the data given in the arff files */
        dataSet.setClassIndex(classIdx)
        return dataSet
    }

    fun createModel() {

        convertCSVtoarrf(context)

        lateinit var trainingDataSet: Instances
        if (File(context.filesDir, file).exists()) {
            trainingDataSet = getDataset(file)
            val testingDataSet: Instances = getDataset(file)

            forest = RandomForest()
//            forest.numTrees = 10

            forest.buildClassifier(trainingDataSet)
            /**
             * train the algorithm with the training data and evaluate the
             * algorithm with testing data
             */
            val eval = Evaluation(trainingDataSet)
            eval.evaluateModel(forest, trainingDataSet)

            /** Print the algorithm summary */
            println("** Decision Tress Evaluation with Datasets **")
            println(eval.toSummaryString())
            print(" the expression for the input data as per algorithm is ")
            println(forest)
            println(eval.toMatrixString())
            println(eval.toClassDetailsString())
            eval.predictions()
        }
    }

    fun predict() {

        // we need those for creating new instances later
        // order of attributes/classes needs to be exactly equal to those used for training
        val sinTime = Attribute("sinTime")
        val cosTime = Attribute("cosTime")
        val dayOfWeekSin = Attribute("dayOfWeekSin")
        val dayOfWeekCos = Attribute("dayOfWeekCos")
        val xCoord = Attribute("xCoord")
        val yCoord = Attribute("yCoord")
        val zCoord = Attribute("zCoord")

        val classes: ArrayList<String?> = object : ArrayList<String?>() {
            init {
                add("class-1") // cls nr 1
                add("class-2") // cls nr 2
                add("class-3") // cls nr 3
                add("class-4") // cls nr 3
            }
        }

        val attributeList: ArrayList<Attribute?> = object : ArrayList<Attribute?>(2) {
            init {
                val attributeClass = Attribute("@@class@@", classes)
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
        // last feature is target variable
        dataUnpredicted.setClassIndex(0)

        // create new instance: this one should fall into the setosa domain
        val newInstance = object : DenseInstance(dataUnpredicted.numAttributes()) {
            init {
                setValue(sinTime, 0.1)
                setValue(cosTime, 0.1)
                setValue(dayOfWeekSin, 0.1)
                setValue(dayOfWeekCos, 0.1)
                setValue(xCoord, 0.1)
                setValue(yCoord, 0.1)
                setValue(zCoord, 0.1)
            }
        }

        // reference to dataset
        newInstance.setDataset(dataUnpredicted)

        // predict new sample
        try {
            val result: Double = forest.classifyInstance(newInstance)
            val className = classes[result.toInt()]
            val msg =
                "Nr: " + "01" + ", predicted: " + className + ", actual: " + "label"
            Log.d("WEKA_TEST", msg)
            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun convertCSVtoarrf(context: Context) {

        // load the CSV file
        val load = CSVLoader()
        load.setSource(File(context.filesDir, "convertedData.csv"))
        val data = load.dataSet

        val arffSaver = ArffSaver()
        arffSaver.instances = data
        arffSaver.setFile(File(context.filesDir, "arffData.arff"))
        arffSaver.writeBatch()

        arffSaver.instances

        val f = File(context.filesDir, "arffData.arff")
        val f2 = File(context.filesDir, "arffData2.arff")
        var text = f.readText()
        text = text.replace("@attribute class numeric", "@attribute class {"+
        "2046003820,343331343,4027449371" + "}")
        f2.writeText(text)

        Log.d("writeToFile", "WrittenArff")
    }
}