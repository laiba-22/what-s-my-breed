package com.example.whatsmybreed

import java.nio.ByteBuffer
import java.nio.ByteOrder
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.whatsmybreed.ui.theme.WhatsMyBreedTheme
import java.io.File
import java.io.InputStream
import androidx.activity.compose.rememberLauncherForActivityResult

class MainActivity : ComponentActivity() {
    private lateinit var classifier: TFLiteClassifier

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        classifier = TFLiteClassifier(this)
        classifier.loadModel()

        setContent {
            WhatsMyBreedTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    AppContent(classifier)
                }
            }
        }
    }

    @Composable
    fun AppContent(classifier: TFLiteClassifier) {
        val context = LocalContext.current
        var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
        var prediction by remember { mutableStateOf<String?>(null) }

        val imagePicker = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.GetContent()
        ) { uri: Uri? ->
            selectedImageUri = uri
            uri?.let {
                val inputStream: InputStream? = context.contentResolver.openInputStream(uri)
                val bitmap = BitmapFactory.decodeStream(inputStream)
                val file = File(cacheDir, "temp_img.jpg")
                file.outputStream().use { bitmap.compress(Bitmap.CompressFormat.JPEG, 100, it) }

                val input = preprocessImage(file)
                val output = classifier.predict(input)
                val topIndex = output.indices.maxByOrNull { output[it] } ?: -1
                val label = classifier.getLabel(topIndex)
                prediction = "My Breed: $label"
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "What's My Breed",
                fontSize = 36.sp,
                fontWeight = FontWeight.Bold
            )

            Image(
                painter = painterResource(id = R.drawable.my_logo), // Use your actual image name
                contentDescription = "App Logo",
                modifier = Modifier
                    .size(150.dp)
                    .clip(CircleShape)
                    .background(Color.LightGray)
            )


            Button(onClick = { imagePicker.launch("image/*") }) {
                Text("Upload a Picture")
            }

            selectedImageUri?.let { uri ->
                val bitmap = BitmapFactory.decodeStream(context.contentResolver.openInputStream(uri))
                bitmap?.let {
                    Image(
                        bitmap = it.asImageBitmap(),
                        contentDescription = null,
                        modifier = Modifier.size(200.dp)
                    )
                }
            }

            prediction?.let {
                Text(it, fontSize = 18.sp, fontWeight = FontWeight.Medium)
            }
        }
    }

    fun preprocessImage(imageFile: File): ByteBuffer {
        val bitmap = BitmapFactory.decodeFile(imageFile.absolutePath)
        val resized = Bitmap.createScaledBitmap(bitmap, 224, 224, true)

        val byteBuffer = ByteBuffer.allocateDirect(1 * 224 * 224 * 3 * 4)
        byteBuffer.order(ByteOrder.nativeOrder())

        val intValues = IntArray(224 * 224)
        resized.getPixels(intValues, 0, 224, 0, 0, 224, 224)

        for (pixel in intValues) {
            val r = ((pixel shr 16) and 0xFF) / 255.0f
            val g = ((pixel shr 8) and 0xFF) / 255.0f
            val b = (pixel and 0xFF) / 255.0f

            byteBuffer.putFloat(r)
            byteBuffer.putFloat(g)
            byteBuffer.putFloat(b)
        }

        return byteBuffer
    }

}
