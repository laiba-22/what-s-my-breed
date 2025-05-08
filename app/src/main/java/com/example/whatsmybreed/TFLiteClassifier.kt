package com.example.whatsmybreed

import android.content.Context
import org.tensorflow.lite.Interpreter
import java.io.*
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel

class TFLiteClassifier(private val context: Context) {
    private lateinit var interpreter: Interpreter
    private val labels: List<String> by lazy { loadLabels() }

    // Load the TFLite model file from assets
    fun loadModel() {
        val fileDescriptor = context.assets.openFd("model.tflite") // Changed to match the .tflite file
        val inputStream = FileInputStream(fileDescriptor.fileDescriptor)
        val fileChannel = inputStream.channel
        val startOffset = fileDescriptor.startOffset
        val declaredLength = fileDescriptor.declaredLength
        val mappedByteBuffer: MappedByteBuffer =
            fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength)

        interpreter = Interpreter(mappedByteBuffer) // Load the model into the interpreter
    }

    // Predict method that takes a FloatArray input and returns the model's predictions
    fun predict(inputBuffer: ByteBuffer): FloatArray {
        val output = Array(1) { FloatArray(labels.size) }
        interpreter.run(inputBuffer, output)
        return output[0]
    }


    // Get the label for a given index from the labels list
    fun getLabel(index: Int): String {
        return if (index in labels.indices) labels[index] else "Unknown"
    }

    // Load the labels file (assuming you have a "labels.txt" in assets folder)
    private fun loadLabels(): List<String> {
        val labelList = mutableListOf<String>()
        context.assets.open("labels.txt").bufferedReader().useLines { lines ->
            lines.forEach { labelList.add(it.trim()) }
        }
        return labelList
    }
}
