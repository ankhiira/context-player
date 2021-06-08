package com.gabchmel.predicitonmodule

import android.content.Context
import org.pmml4s.model.Model

class PredictionModel(val context: Context) {

    // Function to predict song on the given input data
    fun predict() {
        // Input data for the model
        val input = floatArrayOf(0.781831f,0.265953f,0.410050f,-0.872427f,-0.522189f,0.62349f,0.85283f)
        // TODO Maybe will do it in this formal if it works
        val inputMap = hashMapOf("day_of_week_sin" to -0.522189f,
                                                    "coord_z" to 0.85283f,
                                                    "coord_y" to 0.781831f,
                                                    "coord_x" to 0.62349f,
                                                    "sin_time" to -0.872427f,
                                                    "day_of_week_cos" to 0.410050f,
                                                    "cos_time" to 0.265953f)

        // Load the model from asset folder
        val assetManager = context.assets
        val inStream = assetManager.open("random_forest.pmml")

        // Create a model from input pmml file
        val model = Model.fromInputStream(inStream)

        // Helper variables - get order of input variables by their name
        val inNames = model.inputNames()
        val inputSchema = model.inputSchema()

        // Make prediction
        val result = model.predict<Any>(input).map { it as Double }
        val outputNames = model.outputNames()

        // Zip output values with their labels
        val zipMap = outputNames.zip(result).toMap()
        val maximum = zipMap.maxBy { it.value }
        val labelsZip = outputNames.zip(result).toMap().toString()

        // Print out the prediction result
        println(labelsZip)
        println(maximum)
    }
}