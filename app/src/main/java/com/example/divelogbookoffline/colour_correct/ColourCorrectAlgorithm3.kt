import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.graphics.Paint
import android.util.Log
import com.example.divelogbookoffline.colour_correct.TAG
import kotlin.math.cos
import kotlin.math.PI
import kotlin.math.sin

import java.io.ByteArrayOutputStream
import java.nio.ByteBuffer


fun bitmapToPixelArray(bitmap: Bitmap): ByteArray {
    Log.d(TAG, "Starting bitmapToPixelArray")
    // Ensure the bitmap is in ARGB_8888 format
    val convertedBitmap = if (bitmap.config != Bitmap.Config.ARGB_8888) {
        bitmap.copy(Bitmap.Config.ARGB_8888, true)
    } else {
        bitmap
    }

    val width = convertedBitmap.width
    val height = convertedBitmap.height
    val pixelCount = width * height

    // Allocate an IntArray to store pixel values
    val intPixels = IntArray(pixelCount)
    convertedBitmap.getPixels(intPixels, 0, width, 0, 0, width, height)

    // Create a ByteArray for the 4-channel RGBA data
    val bytePixels = ByteArray(pixelCount * 4)

    // Extract ARGB components and fill the byte array
    var byteIndex = 0
    for (pixel in intPixels) {
        val a = (pixel shr 24 and 0xFF).toByte() // Alpha
        val r = (pixel shr 16 and 0xFF).toByte() // Red
        val g = (pixel shr 8 and 0xFF).toByte()  // Green
        val b = (pixel and 0xFF).toByte()       // Blue

        bytePixels[byteIndex++] = r
        bytePixels[byteIndex++] = g
        bytePixels[byteIndex++] = b
        bytePixels[byteIndex++] = a
    }

    Log.d(TAG, "Ending bitmapToPixelArray")
    return bytePixels

}




fun byteArrayToBitmap(pixels: ByteArray, width: Int, height: Int): Bitmap {
    Log.d(TAG, "Starting byteArrayToBitmap")
    // Validate byte array size
    require(pixels.size == width * height * 4) {
        "Pixel array size (${pixels.size}) does not match dimensions: $width x $height."
    }

    // Create a Bitmap to hold the data
    val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)

    // Directly set pixels to the Bitmap row by row
    for (y in 0 until height) {
        val rowPixels = IntArray(width)
        for (x in 0 until width) {
            val i = (y * width + x) * 4
            val r = pixels[i].toInt() and 0xFF
            val g = pixels[i + 1].toInt() and 0xFF
            val b = pixels[i + 2].toInt() and 0xFF
            val a = pixels[i + 3].toInt() and 0xFF

            rowPixels[x] = (a shl 24) or (r shl 16) or (g shl 8) or b
        }
        bitmap.setPixels(rowPixels, 0, width, 0, y, width, 1)
    }

    Log.d(TAG, "Ended byteArrayToBitmap")
    return bitmap

}


fun applyColorMatrixToBitmap(filter: FloatArray, pixels: ByteArray, width: Int, height: Int): Bitmap {
    Log.d(TAG, "Starting applyColorMatrixToBitmap")
    val filteredPixels = pixels.clone()
    for (i in pixels.indices step 4) {
        val r = (pixels[i].toInt() and 0xFF)
        val g = (pixels[i + 1].toInt() and 0xFF)
        val b = (pixels[i + 2].toInt() and 0xFF)

        filteredPixels[i] = ((r * filter[0] + g * filter[1] + b * filter[2] + filter[4] * 255)
            .toInt()
            .coerceIn(0, 255))
            .toByte() // Red

        filteredPixels[i + 1] = ((g * filter[6] + filter[9] * 255)
            .toInt()
            .coerceIn(0, 255))
            .toByte() // Green

        filteredPixels[i + 2] = ((b * filter[12] + filter[14] * 255)
            .toInt()
            .coerceIn(0, 255))
            .toByte() // Blue

        filteredPixels[i + 3] = pixels[i + 3] // Preserve Alpha
    }

    Log.d(TAG, "Ended applyColorMatrixToBitmap")
    return byteArrayToBitmap(filteredPixels, width, height)

}



fun applyColorFilter(bitmap: Bitmap, scaledDownBitmap: Bitmap): Bitmap {

    val byteArray = bitmapToPixelArray(scaledDownBitmap)

    val colorMatrix = getColorFilterMatrix(byteArray, scaledDownBitmap.width, scaledDownBitmap.height)

    return  applyColorMatrixToBitmap(colorMatrix, bitmapToPixelArray(bitmap), bitmap.width, bitmap.height)


}

fun getColorFilterMatrix(pixels: ByteArray, width: Int, height: Int): FloatArray {
    Log.d(TAG, "Starting getColorFilterMatrix")
    val numOfPixels = width * height
    val thresholdRatio = 2000
    val thresholdLevel = numOfPixels / thresholdRatio
    val minAvgRed = 60
    val maxHueShift = 120
    val blueMagicValue = 1.2f

    val hist = mutableMapOf(
        "r" to mutableListOf<Int>(),
        "g" to mutableListOf<Int>(),
        "b" to mutableListOf<Int>()
    )
    val normalize = mutableMapOf(
        "r" to mutableListOf<Int>(),
        "g" to mutableListOf<Int>(),
        "b" to mutableListOf<Int>()
    )
    val adjust = mutableMapOf(
        "r" to mutableListOf<Int>(),
        "g" to mutableListOf<Int>(),
        "b" to mutableListOf<Int>()
    )
    var hueShift = 0

    // Populate hist with 256 zeros
    repeat(256) {
        hist["r"]?.add(0)
        hist["g"]?.add(0)
        hist["b"]?.add(0)
    }

    val avg = calculateAverageColor(pixels, width, height)

    var newAvgRed = avg["r"] ?: 0f
    while (newAvgRed < minAvgRed) {
        val shifted = hueShiftRed(avg["r"]!!, avg["g"]!!, avg["b"]!!, hueShift)
        newAvgRed = shifted.first + shifted.second + shifted.third
        hueShift++
        if (hueShift > maxHueShift) {
            newAvgRed = 60f // Max value
        }
    }

    Log.d(TAG, "Starting histogram loop")

    for (i in pixels.indices step 4) {
        // Extract RGB components
        val red = pixels[i].toInt() and 0xFF
        val green = pixels[i + 1].toInt() and 0xFF
        val blue = pixels[i + 2].toInt() and 0xFF

        // Apply hue shift
        val hueAdjusted = hueShiftRed(red.toFloat(), green.toFloat(), blue.toFloat(), hueShift)

        // Combine the shifted values
        val shiftedRed = (hueAdjusted.first + hueAdjusted.second + hueAdjusted.third).toInt().coerceIn(0, 255)

        // Update histograms
        hist["r"]!![shiftedRed] += 1
        hist["g"]!![green] += 1
        hist["b"]!![blue] += 1
    }

    Log.d(TAG, "Ended histogram loop")

    normalize["r"]?.add(0)
    normalize["g"]?.add(0)
    normalize["b"]?.add(0)

    for (i in 0 until 256) {
        if (hist["r"]!![i] - thresholdLevel < 2) normalize["r"]?.add(i)
        if (hist["g"]!![i] - thresholdLevel < 2) normalize["g"]?.add(i)
        if (hist["b"]!![i] - thresholdLevel < 2) normalize["b"]?.add(i)
    }

    // Push 255 as end value in normalize array:
    normalize["r"]?.add(255)
    normalize["g"]?.add(255)
    normalize["b"]?.add(255)

    adjust["r"] = normalizingInterval(normalize["r"]!!)
    adjust["g"] = normalizingInterval(normalize["g"]!!)
    adjust["b"] = normalizingInterval(normalize["b"]!!)

    val shifted = hueShiftRed(1f, 1f, 1f, hueShift)
    val redGain = 256f / (adjust["r"]!![1] - adjust["r"]!![0])
    val greenGain = 256f / (adjust["g"]!![1] - adjust["g"]!![0])
    val blueGain = 256f / (adjust["b"]!![1] - adjust["b"]!![0])

    val redOffset = (-adjust["r"]!!.first() / 256f) * redGain
    val greenOffset = (-adjust["g"]!!.first() / 256f) * greenGain
    val blueOffset = (-adjust["b"]!!.first() / 256f) * blueGain

    val adjstRed = shifted.first * redGain
    val adjstRedGreen = shifted.second * redGain
    val adjstRedBlue = shifted.third * redGain * blueMagicValue

    return floatArrayOf(
        adjstRed, adjstRedGreen, adjstRedBlue, 0f, redOffset,
        0f, greenGain, 0f, 0f, greenOffset,
        0f, 0f, blueGain, 0f, blueOffset,
        0f, 0f, 0f, 1f, 0f
    )

    Log.d(TAG, "Ended getColorFilterMatrix")
}

fun normalizingInterval(normArray: List<Int>): MutableList<Int> {
    var high = 255
    var low = 0
    var maxDist = 0

    for (i in 1 until normArray.size) {
        val dist = normArray[i] - normArray[i - 1]
        if (dist > maxDist) {
            maxDist = dist
            high = normArray[i]
            low = normArray[i - 1]
        }
    }

    return listOf(low, high).toMutableList()
}

fun hueShiftRed(r: Float, g: Float, b: Float, h: Int): Triple<Float, Float, Float> {
    val u = kotlin.math.cos(h * Math.PI / 180).toFloat()
    val w = kotlin.math.sin(h * Math.PI / 180).toFloat()

    val newR = (0.299f + 0.701f * u + 0.168f * w) * r
    val newG = (0.587f - 0.587f * u + 0.330f * w) * g
    val newB = (0.114f - 0.114f * u - 0.497f * w) * b

    return Triple(newR, newG, newB)
}


fun calculateAverageColor(pixels: ByteArray, width: Int, height: Int): Map<String, Float> {
    Log.d(TAG, "Starting calculateAverageColor")

    val avg = mutableMapOf("r" to 0f, "g" to 0f, "b" to 0f)

    for (y in 0 until height) {
        for (x in 0 until width * 4 step 4) {
            val pos = x + (width * 4) * y

            // Sum values:
            avg["r"] = avg["r"]!! + (pixels[pos].toInt() and 0xFF)
            avg["g"] = avg["g"]!! + (pixels[pos + 1].toInt() and 0xFF)
            avg["b"] = avg["b"]!! + (pixels[pos + 2].toInt() and 0xFF)
        }
    }

    // Calculate average:
    val totalPixels = (width * height)
    avg["r"] = avg["r"]!! / totalPixels
    avg["g"] = avg["g"]!! / totalPixels
    avg["b"] = avg["b"]!! / totalPixels

    Log.d(TAG, "Ended calculateAverageColor")
    return avg

}