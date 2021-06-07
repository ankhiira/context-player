package com.gabchmel.predicitonmodule

import android.content.Context
import com.google.gson.Gson
import org.pmml4s.model.Model

class PredictionModel(val context: Context) {

    fun predict() {
        var data = floatArrayOf(0.1f, 0.1f, 0.1f, 0.1f, 0.1f, 0.1f, 0.1f)

        val assetManager = context.assets
        val inStream = assetManager.open("decision_tree.pmml")

//        var model = Model.fromFile("c:\\src\\ContextPlayerPython\\decision_tree.pmml")
        var model = Model.fromInputStream(inStream)
        var result: Any = model.predict<Any>(data)
        println("Result:$result")
        val result2 =  Gson().toJson(result)
        println("Result2:$result2")
    }
}