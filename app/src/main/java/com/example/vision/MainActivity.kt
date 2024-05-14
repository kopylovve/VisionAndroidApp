package com.example.vision

import android.content.Intent
import android.graphics.Bitmap
import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import java.io.ByteArrayInputStream
import java.io.File
import java.io.FileOutputStream
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream

class MainActivity : AppCompatActivity() {

    private val PICK_IMAGE_REQUEST = 1
    private val CAPTURE_IMAGE_REQUEST = 2

    private lateinit var imageView: ImageView
    private lateinit var galleryButton: Button
    private lateinit var cameraButton: Button
    private lateinit var textView: TextView
    private lateinit var mediaPlayer: MediaPlayer

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        imageView = findViewById(R.id.imageView)
        galleryButton = findViewById(R.id.galleryButton)
        cameraButton = findViewById(R.id.cameraButton)
        textView = findViewById(R.id.textView)

        galleryButton.setOnClickListener {
            openGallery()
        }

        cameraButton.setOnClickListener {
            openCamera()
        }
    }

    private fun openGallery() {
        try {
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            startActivityForResult(intent, PICK_IMAGE_REQUEST)
        } catch (e: Exception) {
            Log.e("MainActivity", "Ошибка открытия галереи: ${e.message}")
        }
    }

    private fun openCamera() {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        startActivityForResult(intent, CAPTURE_IMAGE_REQUEST)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        textView.text = ""
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.data != null) {
            processImage(data.data)
        } else if (requestCode == CAPTURE_IMAGE_REQUEST && resultCode == RESULT_OK && data != null) {
            val imageBitmap = data.extras?.get("data") as Bitmap
            imageView.setImageBitmap(imageBitmap)
            val stream = ByteArrayOutputStream()
            imageBitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream)
            val imageBytes = stream.toByteArray()
            processImageBytes(imageBytes)
        }
    }

    private fun processImage(imageUri: Uri?) {
        try {
            imageView.setImageURI(imageUri)
            val imageFile = File(cacheDir, "image.jpg")
            imageUri?.let { uri ->
                contentResolver.openInputStream(uri)?.use { input ->
                    FileOutputStream(imageFile).use { output ->
                        input.copyTo(output)
                    }
                }
            }

            GlobalScope.launch(Dispatchers.Main) {
                val result = withContext(Dispatchers.IO) {
                    PythonInterface.process_image(imageFile.absolutePath)
                }
                textView.text = result

                Log.d("MainActivity", "Распознанный текст: $result")

                try {
                    withContext(Dispatchers.IO) {
                        val audioBytes = PythonInterface.text_to_speech(result)
                        saveAudioToFile(audioBytes)
                    }
                    val audioFile = File(filesDir, "output.mp3")
                    playAudio(audioFile)
                } catch (e: Exception) {
                    Log.e("TextToSpeech", "Error: ${e.message}")
                }
            }
        } catch (e: Exception) {
            Log.e("MainActivity", "Ошибка: ${e.message}")
        }
    }

    private fun processImageBytes(imageBytes: ByteArray) {
        try {
            val imageFile = File(cacheDir, "image.jpg")
            FileOutputStream(imageFile).use { output ->
                output.write(imageBytes)
            }

            GlobalScope.launch(Dispatchers.Main) {
                val result = withContext(Dispatchers.IO) {
                    PythonInterface.process_image(imageFile.absolutePath)
                }
                textView.text = result

                Log.d("MainActivity", "Распознанный текст: $result")

                try {
                    withContext(Dispatchers.IO) {
                        val audioBytes = PythonInterface.text_to_speech(result)
                        saveAudioToFile(audioBytes)
                    }
                    val audioFile = File(filesDir, "output.mp3")
                    playAudio(audioFile)
                } catch (e: Exception) {
                    Log.e("TextToSpeech", "Error: ${e.message}")
                }
            }
        } catch (e: Exception) {
            Log.e("MainActivity", "Ошибка: ${e.message}")
        }
    }

    private fun saveAudioToFile(audioBytes: ByteArray) {
        try {
            val file = File(filesDir, "output.mp3")
            FileOutputStream(file).use { output ->
                output.write(audioBytes)
            }
            Log.d("MainActivity", "Аудиофайл ${file.absolutePath} сохранен")
        } catch (e: Exception) {
            Log.e("TextToSpeech", "Error saving audio file: ${e.message}")
        }
    }

    private fun playAudio(file: File) {
        try {
            mediaPlayer = MediaPlayer()
            mediaPlayer.setDataSource(file.absolutePath)
            mediaPlayer.prepare()
            mediaPlayer.start()
            mediaPlayer.setOnCompletionListener {
                file.delete() // Удаление файла после воспроизведения
                Log.d("MainActivity", "Аудиофайл ${file.absolutePath} удален")
            }
            Log.d("MainActivity", "Файл проигралcя")
        } catch (e: Exception) {
            Log.e("TextToSpeech", "Error playing audio: ${e.message}")
        }
    }
}