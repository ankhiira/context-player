package com.gabchmel.predicitonmodule

import ai.onnxruntime.OnnxTensor
import ai.onnxruntime.OrtEnvironment
import ai.onnxruntime.OrtSession
import java.io.IOException
import java.nio.FloatBuffer

class PredictionModelonnx {

    private var env: OrtEnvironment = OrtEnvironment.getEnvironment()
    var session: OrtSession = env.createSession("res/raw/onnx_model.onnx", OrtSession.SessionOptions())

    private lateinit var t1 : OnnxTensor
    private lateinit var t2 : OnnxTensor
    var inputs = mapOf("name1" to t1,"name2" to t2)

    fun eval() {
        session.inputNames

        try {
            var results = session.run(inputs)
        } catch (e : IOException) {

        }

        lateinit var sourceData: FloatBuffer // assume your data is loaded into a FloatBuffer

        lateinit var dimensions: LongArray // and the dimensions of the input are stored here

        val tensorFromBuffer: OnnxTensor = OnnxTensor.createTensor(env, sourceData, dimensions)

        val sourceArray = Array(28) {
            FloatArray(
                28
            )
        } // assume your data is loaded into a float array

        val tensorFromArray = OnnxTensor.createTensor(env, sourceArray)
    }
}