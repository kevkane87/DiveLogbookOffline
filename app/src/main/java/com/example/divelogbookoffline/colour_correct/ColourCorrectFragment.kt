package com.example.divelogbookoffline.colour_correct

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import com.example.divelogbookoffline.Dive
import com.example.divelogbookoffline.add_dive.AddDiveViewModel
import com.example.divelogbookoffline.add_dive.AddDiveViewModelFactory
import com.example.divelogbookoffline.databinding.FragmentAddDiveBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

import androidx.compose.foundation.layout.aspectRatio



class ColourCorrectFragment : Fragment() {

    private val viewModel by viewModels<ColourCorrectViewModel> { ColourCorrectViewModelFactory(requireActivity().application) }


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setContent {
                ImagePickerScreen(viewModel)
            }
        }
    }
}

@Composable
fun ImagePickerScreen(viewModel: ColourCorrectViewModel) {
    var imageUri by remember { mutableStateOf<Uri?>(null) }
    var bitmap by remember { mutableStateOf<Bitmap?>(null) }
    var processedBitmap by remember { mutableStateOf<Bitmap?>(null) } // For processed image
    var isProcessing by remember { mutableStateOf(false) }  // To track the processing state

    // Launch image picker
    val context = LocalContext.current
    val launcher =
        rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            // Reset previous images before loading new ones
            bitmap = null
            processedBitmap = null
            isProcessing = false // Reset processing state
            imageUri = uri
        }

    // Load the selected image and process it with the ViewModel
    LaunchedEffect(imageUri) {
        imageUri?.let { uri ->
            bitmap = loadBitmapFromUri(uri, context)
            bitmap?.let {
                isProcessing = true  // Set to true while processing
                viewModel.runInference(it) // Run inference
            }
        }
    }

    // Observe the processed bitmap and stop the loading when done
    val resultBitmap by viewModel.outputBitmap.observeAsState()
    LaunchedEffect(resultBitmap) {
        resultBitmap?.let {
            processedBitmap = it
            isProcessing = false // Stop loading when result is available
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(0.dp, 16.dp, ),
            verticalArrangement = Arrangement.Top, // Align content at the top
            horizontalAlignment = Alignment.CenterHorizontally // Center horizontally
        ) {
            // Button to select image
            Button(
                onClick = { launcher.launch("image/*") }
            ) {
                Text(text = "Pick Image")
            }

            Spacer(modifier = Modifier.height(16.dp)) // Space between button and images

            // Show a loading indicator when processing
            if (isProcessing) {
                CircularProgressIndicator(modifier = Modifier.size(64.dp))
            } else {
                // Show the selected image scaled to width
                bitmap?.let {
                    val aspectRatio = it.width.toFloat() / it.height.toFloat()
                    Image(
                        bitmap = it.asImageBitmap(),
                        contentDescription = "Selected Image",
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(aspectRatio) // Maintain aspect ratio
                    )
                }

                // Show the processed image once available, scaled to width
                processedBitmap?.let {
                    val aspectRatio = it.width.toFloat() / it.height.toFloat()
                    Image(
                        bitmap = it.asImageBitmap(),
                        contentDescription = "Processed Image",
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(aspectRatio) // Maintain aspect ratio
                    )
                }
            }
        }
    }
}



// Helper function to load a bitmap from a URI
suspend fun loadBitmapFromUri(uri: Uri, context: Context): Bitmap? {
    return withContext(Dispatchers.IO) {
        try {
            MediaStore.Images.Media.getBitmap(context.contentResolver, uri)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}
