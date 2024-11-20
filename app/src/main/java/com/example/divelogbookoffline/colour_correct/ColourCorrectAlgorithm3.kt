import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.graphics.Paint
import kotlin.math.cos
import kotlin.math.PI
import kotlin.math.sin

import java.io.ByteArrayOutputStream
import java.nio.ByteBuffer


fun bitmapTo4ChannelPixelArray(bitmap: Bitmap): ByteArray {
    // Create a ByteArray to hold pixel data for each channel: RGBA (4 channels)
    val width = bitmap.width
    val height = bitmap.height
    val pixelArray = ByteArray(width * height * 4) // RGBA for each pixel

    // Loop through the pixels and get RGBA values
    for (y in 0 until height) {
        for (x in 0 until width) {
            val pixel = bitmap.getPixel(x, y) // Get pixel value at (x, y)

            // Extract RGBA channels using Color class methods
            val r = Color.red(pixel)
            val g = Color.green(pixel)
            val b = Color.blue(pixel)
            val a = Color.alpha(pixel)

            // Store the values in the ByteArray
            val pos = (y * width + x) * 4
            pixelArray[pos] = r.toByte()
            pixelArray[pos + 1] = g.toByte()
            pixelArray[pos + 2] = b.toByte()
            pixelArray[pos + 3] = a.toByte()
        }
    }
    return pixelArray
}

fun bitmapToPixelArray(bitmap: Bitmap): ByteArray {
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

    return bytePixels
}




fun byteArrayToBitmap(pixels: ByteArray, width: Int, height: Int): Bitmap {
    // Validate byte array size
    require(pixels.size == width * height * 4) {
        "Pixel array size (${pixels.size}) does not match dimensions: $width x $height."
    }

    // Create a Bitmap to hold the data
    val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)

    // Create an IntArray to hold ARGB pixel values
    val intPixels = IntArray(width * height)

    // Convert byte array (RGBA) to IntArray (ARGB)
    for (i in 0 until pixels.size step 4) {
        val r = pixels[i].toInt() and 0xFF
        val g = pixels[i + 1].toInt() and 0xFF
        val b = pixels[i + 2].toInt() and 0xFF
        val a = pixels[i + 3].toInt() and 0xFF

        intPixels[i / 4] = (a shl 24) or (r shl 16) or (g shl 8) or b
    }

    // Set the IntArray to the Bitmap
    bitmap.setPixels(intPixels, 0, width, 0, 0, width, height)

    return bitmap
}


fun applyColorMatrixToBitmap(matrix: FloatArray, inputBitmap: Bitmap): Bitmap {
    // Create a ColorMatrix object from the provided matrix
    val colorMatrix = ColorMatrix(matrix)

    // Create a Paint object and set its ColorMatrix to the transformation matrix
    val paint = Paint()
    paint.colorFilter = ColorMatrixColorFilter(colorMatrix)

    // Create a new bitmap to hold the result
    val outputBitmap = Bitmap.createBitmap(inputBitmap.width, inputBitmap.height, inputBitmap.config)

    // Create a canvas to apply the paint (with color matrix) to the new bitmap
    val canvas = Canvas(outputBitmap)
    canvas.drawBitmap(inputBitmap, 0f, 0f, paint)

    return outputBitmap
}

fun applyColorMatrixToBitmap(filter: FloatArray, pixels: ByteArray, width: Int, height: Int): Bitmap {

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

    return byteArrayToBitmap(filteredPixels, width, height)
}


fun rgbaMatrixToBitmap(rgbaData: ByteArray, width: Int, height: Int): Bitmap {
    require(rgbaData.size == width * height * 4) {
        "The size of rgbaData must match the dimensions width x height x 4."
    }

    val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
    val pixels = IntArray(width * height)

    var index = 0
    for (i in pixels.indices) {
        val r = rgbaData[index].toInt() and 0xFF
        val g = rgbaData[index + 1].toInt() and 0xFF
        val b = rgbaData[index + 2].toInt() and 0xFF
        val a = rgbaData[index + 3].toInt() and 0xFF

        pixels[i] = (a shl 24) or (r shl 16) or (g shl 8) or b
        index += 4
    }

    bitmap.setPixels(pixels, 0, width, 0, 0, width, height)
    return bitmap
}



fun applyColorFilter(bitmap: Bitmap): Bitmap {

    val byteArray = bitmapToPixelArray(bitmap)

    val colorMatrix = getColorFilterMatrix(byteArray, bitmap.width, bitmap.height)

    return  applyColorMatrixToBitmap(colorMatrix, byteArray, bitmap.width, bitmap.height)


}

fun getColorFilterMatrix(pixels: ByteArray, width: Int, height: Int): FloatArray {
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


    for (y in 0 until height) {
        for (x in 0 until width * 4 step 4) {
            val pos = x + (width * 4) * y

            var red = pixels[pos + 0].toInt() and 0xFF // Convert byte to unsigned int
            val green = pixels[pos + 1].toInt() and 0xFF // Convert byte to unsigned int
            val blue = pixels[pos + 2].toInt() and 0xFF // Convert byte to unsigned int

            // Apply hue shift to red, green, blue values
            val shifted = hueShiftRed(red.toFloat(), green.toFloat(), blue.toFloat(), hueShift)

            // Combine the shifted values for red, green, blue channels
            red = (shifted.first + shifted.second + shifted.third).toInt()
            red = red.coerceIn(0, 255)

            // Update histogram
            hist["r"]?.let { it[red] += 1 }
            hist["g"]?.let { it[green] += 1 }
            hist["b"]?.let { it[blue] += 1 }

        }
    }

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

    return avg
}















