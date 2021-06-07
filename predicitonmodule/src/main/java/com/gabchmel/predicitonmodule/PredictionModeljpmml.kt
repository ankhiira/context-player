package com.gabchmel.predicitonmodule

import android.content.Context
import org.dmg.pmml.FieldName
import org.jpmml.evaluator.*
import org.jpmml.model.SerializationUtil


class PredictionModeljpmml(val context: Context) {

    @Throws(java.lang.Exception::class)
    private fun createEvaluator(): Evaluator {
        val assetManager = context.assets
        val inStream = assetManager.open("model.pmml.ser")
        val pmml = SerializationUtil.deserializePMML(inStream)
        val modelEvaluatorFactory = ModelEvaluatorFactory.newInstance()
        val modelEvaluator: ModelEvaluator<*> = modelEvaluatorFactory.newModelEvaluator(pmml, null)

        // Performing the self-check
        modelEvaluator.verify()
        return modelEvaluator
    }

//    @Throws(Exception::class)
//    private fun createEvaluator(): Evaluator {
//
//        val assetManager = context.assets
//        val inStream = assetManager.open("model.pmml.ser")
//
//        return LoadingModelEvaluatorBuilder()
//            .load(inStream)
//            .build()
//
////        val reader: Reader = InputStreamReader(ims)
////        val jsonPmml: PMML = Gson().fromJson(reader, PMML::class.java)
////        val modelEvaluatorBuilder: EvaluatorBuilder = ModelEvaluatorBuilder(jsonPmml)
////        return modelEvaluatorBuilder.build()
//    }

    fun eval() {

        // Building a model evaluator from a PMML file
        val evaluator = createEvaluator()

        // Printing input (x1, x2, .., xn) fields
        val inputFields: List<InputField> = evaluator.inputFields
        println("Input fields: $inputFields")

        // Printing primary result (y) field(s)
        val targetFields: List<TargetField> = evaluator.targetFields
        println("Target field(s): $targetFields")

        // Printing secondary result (eg. probability(y), decision(y)) fields
        val outputFields: MutableList<org.jpmml.evaluator.OutputField>? = evaluator.outputFields
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