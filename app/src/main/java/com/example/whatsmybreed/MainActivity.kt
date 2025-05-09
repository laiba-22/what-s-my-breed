package com.example.whatsmybreed
import android.Manifest
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import com.example.whatsmybreed.ui.theme.WhatsMyBreedTheme
import java.io.File
import java.io.InputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder


class MainActivity : ComponentActivity()
{

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
        val photoFile = remember { File(cacheDir, "camera_capture.jpg") }
        val photoUri: Uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.provider",
            photoFile
        )

        var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
        var prediction by remember { mutableStateOf<String?>(null) }

        // TO CLASSIFY IF IT'S A CAT OR DOG
        val catBreeds = listOf(
            "abyssinian", "bengal", "birman", "bombay",
            "british_shorthair", "egyptian_mau", "maine_coon",
            "persian", "ragdoll", "russian_blue", "siamese", "sphynx"
        )

        // Camera launcher initialized before permission launcher
        val cameraLauncher = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.TakePicture()
        ) { success ->
            if (success) {
                selectedImageUri = photoUri

                val input = preprocessImage(photoFile)
                val output = classifier.predict(input)
                val topIndex = output.indices.maxByOrNull { output[it] } ?: -1
                val label = classifier.getLabel(topIndex)
                val type = if (catBreeds.contains(label.lowercase())) "Cat" else "Dog"
                prediction = "I am a $type\nMy Breed: $label"
            }
        }

        // Camera permission and launcher
        val cameraPermission = Manifest.permission.CAMERA
        val permissionLauncher = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.RequestPermission()
        ) { isGranted ->
            if (isGranted) {
                cameraLauncher.launch(photoUri)
            } else {
                Toast.makeText(context, "Camera permission is required", Toast.LENGTH_SHORT).show()
            }
        }

        // Image picker for uploading images
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
                val type = if (catBreeds.contains(label.lowercase())) "Cat" else "Dog"
                prediction = "I am a $type\nMy Breed: $label"
            }
        }

        // UI layout
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
                painter = painterResource(id = R.drawable.my_logo),
                contentDescription = "App Logo",
                modifier = Modifier
                    .size(150.dp)
                    .clip(CircleShape)
                    .background(Color.LightGray)
            )

            Button(onClick = { imagePicker.launch("image/*") }) {
                Text("Upload a Picture")
            }

            Button(onClick = { permissionLauncher.launch(cameraPermission) }) {
                Text("Take a Picture")
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
                Text(
                    text = it,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
                )
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
