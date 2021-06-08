package com.gabchmel.predicitonmodule

import android.content.Context
import org.pmml4s.model.Model

class PredictionModel(val context: Context) {

    // Function to predict song on the given input data
    fun predict() {
        // Input data for the model
        val input = floatArrayOf(-0.522189f,0.85283f,0.781831f,0.62349f,-0.872427f,0.410050f,0.265953f)

        // Load the model from asset folder
        val assetManager = context.assets
        val inStream = assetManager.open("decision_tree.pmml")

        // Create a model from input pmml file
        val model = Model.fromInputStream(inStream)
        // Make prediction
        val result = model.predict<Any>(input).map { it as Double }
        val labels= listOf("prva0", "druha")

        val target = model.targetNames()

        val labelsZip = labels.zip(result).toMap().toString()
        println(labelsZip)
    }
}