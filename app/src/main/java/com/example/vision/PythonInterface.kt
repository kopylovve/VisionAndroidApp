package com.example.vision
import com.chaquo.python.PyObject
import com.chaquo.python.Python
import java.io.ByteArrayInputStream
import java.io.File
import java.io.FileOutputStream

object PythonInterface {
    private val python: Python = Python.getInstance()
    private val pyModule: PyObject = python.getModule("test")

    fun process_image(imagePath: String): String {
        return pyModule.callAttr("process_image", imagePath).toString()
    }

    fun text_to_speech(text: String): ByteArray {
        return pyModule.callAttr("text_to_speech", text).toJava(ByteArray::class.java)
    }
}
