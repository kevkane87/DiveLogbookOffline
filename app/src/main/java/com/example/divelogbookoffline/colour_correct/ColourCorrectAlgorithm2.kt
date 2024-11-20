/*
import android.graphics.Bitmap
import android.graphics.Color
import kotlin.math.cos
import kotlin.math.PI
import kotlin.math.sin


fun applyColorFilter(bitmap: Bitmap): Bitmap {
    val width = bitmap.width
    val height = bitmap.height
    val pixels = ByteArray(width * height * 4) // RGBA (4 bytes per pixel)

    // Step 1: Convert Bitmap to ByteArray (RGBA format)
    bitmap.getPixels(IntArray(width * height), 0, width, 0, 0, width, height)
    for (i in pixels.indices step 4) {
        val color = bitmap.getPixel(i / 4 % width, i / 4 / width)
        pixels[i] = (Color.red(color) and 0xFF).toByte() // Red
        pixels[i + 1] = (Color.green(color) and 0xFF).toByte() // Green
        pixels[i + 2] = (Color.blue(color) and 0xFF).toByte() // Blue
        pixels[i + 3] = (Color.alpha(color) and 0xFF).toByte() // Alpha
    }

    // Step 2: Get the color filter matrix using the provided function
    val colorMatrix = getColorFilterMatrix(pixels, width, height)

    // Step 3: Apply the color filter matrix to each pixel
    val filteredPixels = IntArray(width * height)
    for (i in 0 until width * height) {
        val offset = i * 4
        val r = pixels[offset].toInt() and 0xFF
        val g = pixels[offset + 1].toInt() and 0xFF
        val b = pixels[offset + 2].toInt() and 0xFF
        val a = pixels[offset + 3].toInt() and 0xFF

        // Apply the color filter transformation
        val newR = (colorMatrix[0] * r + colorMatrix[1] * g + colorMatrix[2] * b + colorMatrix[4]).coerceIn(0f, 255f).toInt()
        val newG = (colorMatrix[6] * g + colorMatrix[7] * g + colorMatrix[8] * b + colorMatrix[9]).coerceIn(0f, 255f).toInt()
        val newB = (colorMatrix[12] * b + colorMatrix[13] * b + colorMatrix[14] * a + colorMatrix[15]).coerceIn(0f, 255f).toInt()
        filteredPixels[i] = Color.argb(a, newR, newG, newB)
    }

    // Step 4: Convert back to Bitmap
    val filteredBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
    filteredBitmap.setPixels(filteredPixels, 0, width, 0, 0, width, height)

    return filteredBitmap
}

fun getColorFilterMatrix(pixels: ByteArray, width: Int, height: Int): FloatArray {
    val numOfPixels = width * height
    val thresholdRatio = 2000
    val thresholdLevel = numOfPixels / thresholdRatio
    val minAvgRed = 60
    val maxHueShift = 120
    val blueMagicValue = 1.2f
    val hist = mutableMapOf("r" to IntArray(256), "g" to IntArray(256), "b" to IntArray(256))
    val normalize = mutableMapOf("r" to mutableListOf(0), "g" to mutableListOf(0), "b" to mutableListOf(0))
    val adjust = mutableMapOf<String, Pair<Int, Int>>()
    var hueShift = 0
    val avg = calculateAverageColor(pixels, width, height)
    // Calculate shift amount:
    var newAvgRed= avg.first
    while (newAvgRed < minAvgRed) {
        val shifted = hueShiftRed(avg.first, avg.second, avg.third, hueShift)
        newAvgRed = (shifted.first + shifted.second + shifted.third).toInt().toFloat()
        hueShift++
        if (hueShift > maxHueShift) newAvgRed = minAvgRed.toFloat()
    }
    // Create histogram with new red values:
    for (y in 0 until height) {
        for (x in 0 until width * 4 step 4) {
            val pos = x + (width * 4) * y
            var red = pixels[pos].toInt() and 0xFF
            val green = pixels[pos + 1].toInt() and 0xFF
            val blue = pixels[pos + 2].toInt() and 0xFF
            val shifted = hueShiftRed(red.toFloat(), green.toFloat(), blue.toFloat(), hueShift)
            red = ((shifted.first + shifted.second + shifted.third).toInt()).coerceIn(0, 255)
            hist["r"]!![red]++
            hist["g"]!![green]++
            hist["b"]!![blue]++
        }
    }
    // Find values under threshold:
    for (i in 0 until 256) {
        if (hist["r"]!![i] - thresholdLevel < 2) normalize["r"]!!.add(i)
        if (hist["g"]!![i] - thresholdLevel < 2) normalize["g"]!!.add(i)
        if (hist["b"]!![i] - thresholdLevel < 2) normalize["b"]!!.add(i)
    }
    normalize["r"]!!.add(255)
    normalize["g"]!!.add(255)
    normalize["b"]!!.add(255)
    adjust["r"] = normalizingInterval(normalize["r"]!!)
    adjust["g"] = normalizingInterval(normalize["g"]!!)
    adjust["b"] = normalizingInterval(normalize["b"]!!)
    val shifted = hueShiftRed(1f, 1f, 1f, hueShift)
    val redGain = 256 / (adjust["r"]!!.second - adjust["r"]!!.first).toFloat()
    val greenGain = 256 / (adjust["g"]!!.second - adjust["g"]!!.first).toFloat()
    val blueGain = 256 / (adjust["b"]!!.second - adjust["b"]!!.first).toFloat()
    val redOffset = (-adjust["r"]!!.first / 256f) * redGain
    val greenOffset = (-adjust["g"]!!.first / 256f) * greenGain
    val blueOffset = (-adjust["b"]!!.first / 256f) * blueGain
    val adjustRed = shifted.first * redGain
    val adjustRedGreen = shifted.second * redGain
    val adjustRedBlue = shifted.third * redGain * blueMagicValue
    return floatArrayOf(
        adjustRed, adjustRedGreen, adjustRedBlue, 0f, redOffset,
        0f, greenGain, 0f, 0f, greenOffset,
        0f, 0f, blueGain, 0f, blueOffset,
        0f, 0f, 0f, 1f, 0f
    )
}
fun calculateAverageColor(pixels: ByteArray, width: Int, height: Int): Triple<Float, Float, Float> {
    var redSum = 0L
    var greenSum = 0L
    var blueSum = 0L
    for (y in 0 until height) {
        for (x in 0 until width * 4 step 4) {
            val pos = x + (width * 4) * y
            redSum += pixels[pos].toInt() and 0xFF
            greenSum += pixels[pos + 1].toInt() and 0xFF
            blueSum += pixels[pos + 2].toInt() and 0xFF
        }
    }
    val numOfPixels = width * height
    return Triple(redSum.toFloat() / numOfPixels, greenSum.toFloat() / numOfPixels, blueSum.toFloat() / numOfPixels)
}
fun hueShiftRed(r: Float, g: Float, b: Float, h: Int): Triple<Float, Float, Float> {
    val U = cos(h * PI / 180)
    val W = sin(h * PI / 180)
    val newR = (0.299 + 0.701 * U + 0.168 * W) * r
    val newG = (0.587 - 0.587 * U + 0.330 * W) * g
    val newB = (0.114 - 0.114 * U - 0.497 * W) * b
    return Triple(newR.toFloat(), newG.toFloat(), newB.toFloat())
}
fun normalizingInterval(normArray: List<Int>): Pair<Int, Int> {
    var maxDist = 0
    var high = 255
    var low = 0
    for (i in 1 until normArray.size) {
        val dist = normArray[i] - normArray[i - 1]
        if (dist > maxDist) {
            maxDist = dist
            high = normArray[i]
            low = normArray[i - 1]
        }
    }
    return Pair(low, high)
}*/
