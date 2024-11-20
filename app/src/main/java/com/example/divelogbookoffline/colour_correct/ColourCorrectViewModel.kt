package com.example.divelogbookoffline.colour_correct

import android.app.Application
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import applyColorFilter
import kotlinx.coroutines.launch
/*import org.pytorch.IValue
import org.pytorch.LiteModuleLoader
import org.pytorch.Module
import org.pytorch.Tensor
import org.pytorch.torchvision.TensorImageUtils*/
import java.io.File
import java.io.FileOutputStream
import kotlin.math.roundToInt


class ColourCorrectViewModel(application: Application) : AndroidViewModel(application) {

    private val _outputBitmap = MutableLiveData<Bitmap>()
    val outputBitmap: LiveData<Bitmap> get() = _outputBitmap




    fun runInference(inputBitmap: Bitmap) {
        viewModelScope.launch {
            initOpenCV()
            _outputBitmap.postValue(applyColorFilter(inputBitmap))
        }

    }































  /* private val size = 256

   private var model: Module? = null
   private val _outputBitmap = MutableLiveData<Bitmap>()
   val outputBitmap: LiveData<Bitmap> get() = _outputBitmap

   init {
      loadModel()
   }

   private fun loadModel() {
      model = LiteModuleLoader.load(assetFilePath("funie_generator.pt")) // Replace with your model
   }

   // Main function to process the input Bitmap
   fun runInference(inputBitmap: Bitmap) {
      val resizedBitmap = Bitmap.createScaledBitmap(inputBitmap, size, size, true) // Resize as needed
      val inputTensor = preprocessImage(resizedBitmap, size, size)
      val outputTensor = model?.forward(IValue.from(inputTensor))?.toTensor()
      val outputTensor2 = model?.forward(IValue. from(inputTensor))?.toTensor()


      outputTensor?.let {
         val bitmap = tensorToBitmap(it)
         _outputBitmap.postValue(bitmap)
        Log.d("Bawbag", it.toString())
      }
   }

   // Convert Bitmap to Tensor
   private fun preprocessImage(bitmap: Bitmap, imgHeight: Int, imgWidth: Int): Tensor {
      // Resize with bicubic scaling
      val resizedBitmap = Bitmap.createScaledBitmap(bitmap, imgWidth, imgHeight, true)
      val width = resizedBitmap.width
      val height = resizedBitmap.height
      val floatArray = FloatArray(3 * width * height)

      for (y in 0 until height) {
         for (x in 0 until width) {
            val pixel = resizedBitmap.getPixel(x, y)
            val r = (Color.red(pixel) / 255.0f - 0.5f) / 0.5f
            val g = (Color.green(pixel) / 255.0f - 0.5f) / 0.5f
            val b = (Color.blue(pixel) / 255.0f - 0.5f) / 0.5f

            val index = (y * width + x) * 3
            floatArray[index] = r
            floatArray[index + 1] = g
            floatArray[index + 2] = b
         }
      }
      return Tensor.fromBlob(floatArray, longArrayOf(1, 3, height.toLong(), width.toLong()))
   }

   private fun tensorToBitmap(tensor: Tensor): Bitmap {
      val width = size
      val height = size
      val tensorData = tensor.dataAsFloatArray
      val unnormalizedData = FloatArray(tensorData.size)
      for (i in tensorData.indices) {
         unnormalizedData[i] = tensorData[i] * 0.5f + 0.5f
      }

      // Create a Bitmap to store output
      val outputBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)

      for (y in 0 until height) {
         for (x in 0 until width) {
            val index = (y * width + x) * 3
            val r = (unnormalizedData[index] * 255).toInt().coerceIn(0, 255)
            val g = (unnormalizedData[index + 1] * 255).toInt().coerceIn(0, 255)
            val b = (unnormalizedData[index + 2] * 255).toInt().coerceIn(0, 255)
            outputBitmap.setPixel(x, y, Color.rgb(r, g, b))
         }
      }
      return outputBitmap

   }

   private fun assetFilePath(assetName: String): String {
      val file = File(getApplication<Application>().filesDir, assetName)
      if (!file.exists()) {
         getApplication<Application>().assets.open(assetName).use { inputStream ->
            FileOutputStream(file).use { outputStream ->
               inputStream.copyTo(outputStream)
            }
         }
      }
      return file.absolutePath
   }*/
}
