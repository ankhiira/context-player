package com.gabchmel.predicitonmodule

import android.content.Context
import com.google.gson.Gson
import org.pmml4s.model.Model

class PredictionModel(val context: Context) {

    // Function to predict song on the given input data
    fun predict() {
        // Input data for the model
        val input = floatArrayOf(0.1f, 0.1f, 0.1f, 0.1f, 0.1f, 0.1f, 0.1f)

        // Load the model from asset folder
        val assetManager = context.assets
        val inStream = assetManager.open("decision_tree.pmml")

        // Create a model from input pmml file
        val model = Model.fromInputStream(inStream)
        // Make prediction
        val result: Any = model.predict<Any>(input)
        // Convert to JSON format to print out
        val resultJSON =  Gson().toJson(result)
        println("Result2:$resultJSON")
    }
}