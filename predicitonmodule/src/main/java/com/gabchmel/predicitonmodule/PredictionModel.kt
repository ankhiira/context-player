package com.gabchmel.predicitonmodule

import android.content.Context
import com.google.gson.Gson
import org.dmg.pmml.FieldName
import org.dmg.pmml.PMML
import org.jpmml.evaluator.*
import java.io.InputStream
import java.io.InputStreamReader
import java.io.Reader


class PredictionModel(val context: Context) {

    @Throws(Exception::class)
    private fun createEvaluator(): Evaluator {
        val assetManager = context.assets

        val ims: InputStream = assetManager.open("decision_tree.pmml")
        val reader: Reader = InputStreamReader(ims)
        val jsonPmml: PMML = Gson().fromJson(reader, PMML::class.java)
        val modelEvaluatorBuilder: EvaluatorBuilder = ModelEvaluatorBuilder(jsonPmml)
        return modelEvaluatorBuilder.build()
    }

    fun eval() {

        // Building a model evaluator from a PMML file
        val evaluator = createEvaluator()

        // Performing the self-check
        evaluator.verify()

        // Printing input (x1, x2, .., xn) fields
        val inputFields: List<InputField> = evaluator.inputFields
        println("Input fields: $inputFields")

        // Printing primary result (y) field(s)
        val targetFields: List<TargetField> = evaluator.targetFields
        println("Target field(s): $targetFields")

        // Printing secondary result (eg. probability(y), decision(y)) fields
        val outputFields: List<OutputField> = evaluator.outputFields
        println("Output fields: $outputFields");

        // Iterating through columnar data (eg. a CSV file, an SQL result set)
        while (true) {
            // Reading a record from the data source
            val inputRecord: Map<String, *> = readRecord()

            val arguments = mutableMapOf<FieldName, FieldValue>()

            // Mapping the record field-by-field from data source schema to PMML schema
            for (inputField in inputFields) {
                val inputName: FieldName = inputField.name

                val rawValue = inputRecord[inputName.value]

                // Transforming an arbitrary user-supplied value to a known-good PMML value
                val inputValue: FieldValue = inputField.prepare(rawValue)

                arguments[inputName] = inputValue
            }

            // Evaluating the model with known-good arguments
            val results = evaluator.evaluate(arguments)

            // Decoupling results from the JPMML-Evaluator runtime environment
            val resultRecord = EvaluatorUtil.decodeAll(results)

            // Writing a record to the data sink
            writeRecord(resultRecord)
        }
    }

    fun readRecord(): Map<String, *> {
        lateinit var map: Map<String, *>
        return map
    }

    fun writeRecord(resultRecord: Any) {

    }
}