package com.gabchmel.predicitonmodule

//import smile.classification.RandomForest

import android.content.Context
import android.util.Log
import weka.classifiers.Evaluation
import weka.classifiers.trees.RandomForest
import weka.core.Instances
import weka.core.converters.CSVLoader
import java.io.File


class PredictionModelBuiltIn (context: Context) {

    val context = context

    /** file names are defined */
    val TRAINING_DATA_SET_FILENAME = "convertedData.csv"
    val TESTING_DATA_SET_FILENAME = "convertedData.csv"

    fun createModel() {

        process()

        val pathNew = context.filesDir.absolutePath

//        val inputFile = File(context.filesDir, "convertedData.csv").toPath()

        val path = context.getFileStreamPath("convertedData.csv")


        if (path.exists()) {
            Log.d("exists","jej")


//        val fileURL: URL = javaClass.classLoader.getResource("convertedData.csv")
//        val fileName: String = fileURL.file
//        val filePath: String = fileURL.path
//
//        val input = read.csv(pathNew)
//        val rf = randomForest(Formula.lhs("class"), input)
        //    Log.d("RF", "OOB error: ${rf.error()}")
        }
//        return rf
    }

    private fun getDataSet(fileName : String): Instances {

        val initialFile = File(context.filesDir, "convertedData.csv")

        val classIdx = 0

        val loader = CSVLoader()
        loader.setFile(initialFile)
        val dataSet= loader.dataSet
        /** set the index based on the data given in the arff files */
        dataSet.setClassIndex(classIdx)
        return dataSet
    }

    fun process() {
        val trainingDataSet: Instances = getDataSet(TRAINING_DATA_SET_FILENAME)
        val testingDataSet: Instances = getDataSet(TESTING_DATA_SET_FILENAME)

        val forest = RandomForest()
        forest.numTrees = 10

        /** */
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
    }

    fun predict(input : DoubleArray) {


//        val rf = createModel()
//        rf.
    }
}