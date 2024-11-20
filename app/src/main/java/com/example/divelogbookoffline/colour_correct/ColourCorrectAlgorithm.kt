package com.example.divelogbookoffline.colour_correct

import android.graphics.Bitmap
import android.util.Log
import org.opencv.android.OpenCVLoader
import org.opencv.android.Utils
import org.opencv.core.*
import org.opencv.imgcodecs.Imgcodecs
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.PI
import org.opencv.core.CvType.CV_64F
import org.opencv.core.CvType.CV_8U
import org.opencv.core.Scalar
import org.opencv.core.Core
import org.opencv.core.CvType.CV_32F
import org.opencv.imgproc.Imgproc
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.PI

import kotlin.math.*
const val TAG = "ColourCorrectAlgorithm"
const val THRESHOLD_RATIO = 2000
const val MIN_AVG_RED = 60
const val MAX_HUE_SHIFT = 120
const val BLUE_MAGIC_VALUE = 1.2



// Function to apply hue shift on red channel
fun hueShiftRed(mat: Mat, h: Double): Mat {
    val U = cos(h * PI / 180)
    val W = sin(h * PI / 180)

    // Split the input matrix into individual channels
    val channels = mutableListOf<Mat>()
    Core.split(mat, channels)

    val r = channels[0]  // Red channel
    val g = channels[1]  // Green channel
    val b = channels[2]  // Blue channel

    // Convert to CV_64F for precision in calculations
    r.convertTo(r, CV_64F)
    g.convertTo(g, CV_64F)
    b.convertTo(b, CV_64F)

    // Apply transformations to each channel using the formulas from Python code
    val rMultiplier = 0.299 + 0.701 * U + 0.168 * W
    val gMultiplier = 0.587 - 0.587 * U + 0.330 * W
    val bMultiplier = 0.114 - 0.114 * U - 0.497 * W

    Core.multiply(r, Scalar(rMultiplier), r)
    Core.multiply(g, Scalar(gMultiplier), g)
    Core.multiply(b, Scalar(bMultiplier), b)

    // Merge the transformed channels back into a single matrix
    val result = Mat()
    Core.merge(listOf(r, g, b), result)

    // Clamp values to [0, 255] and convert to 8-bit type (CV_8U)
    Core.min(result, Scalar(255.0), result)
    Core.max(result, Scalar(0.0), result)
    result.convertTo(result, CV_8U)

    return result
}




// Normalizing the interval (similar to Python's normalizing_interval)
fun normalizingInterval(array: DoubleArray): Pair<Int, Int> {
    var high = 255
    var low = 0
    var maxDist = 0

    for (i in 1 until array.size) {
        val dist = array[i] - array[i - 1]
        if (dist > maxDist) {
            maxDist = dist.toInt()
            high = array[i].toInt()
            low = array[i - 1].toInt()
        }
    }

    return Pair(low, high)
}

// Apply the color filter matrix to the image
fun applyFilter(mat: Mat, filt: DoubleArray): Mat {
    // Extract the channels (BGR order)
    val r = Mat()
    val g = Mat()
    val b = Mat()

    // Extract the individual channels from the Mat
    Core.extractChannel(mat, r, 0)  // Blue channel
    Core.extractChannel(mat, g, 1)  // Green channel
    Core.extractChannel(mat, b, 2)  // Red channel

    // Convert to CV_64F to perform arithmetic operations
    r.convertTo(r, CV_64F)
    g.convertTo(g, CV_64F)
    b.convertTo(b, CV_64F)

    // Apply the filter coefficients
    // Red channel: r = r * filt[0] + g * filt[1] + b * filt[2] + filt[4] * 255
    Core.multiply(r, Scalar(filt[0]), r)
    Core.multiply(g, Scalar(filt[1]), g)
    Core.multiply(b, Scalar(filt[2]), b)
    Core.add(r, Scalar(filt[4] * 255.0), r)
    Core.add(g, Scalar(filt[6] * 255.0), g)
    Core.add(b, Scalar(filt[12] * 255.0), b)

    // Merge the channels back into a single Mat
    val filteredMat = Mat()
    Core.merge(arrayListOf(r, g, b), filteredMat)

    // Clip the values to ensure they are in the range [0, 255]
    Core.min(filteredMat, Scalar(255.0), filteredMat)
    Core.max(filteredMat, Scalar(0.0), filteredMat)

    // Convert back to CV_8U (8-bit unsigned integer) for proper display or saving
    val finalMat = Mat()
    filteredMat.convertTo(finalMat, CV_8U)

    return finalMat
}



// Apply filter to the mat for older OpenCV versions
fun getFilterMatrix(mat: Mat): DoubleArray {
    val resizedMat = Mat()
    Imgproc.resize(mat, resizedMat, Size(256.0, 256.0))


    val avgMat = Mat(1, 3, CvType.CV_64F) // Use CV_64F for double values

// Calculate the average color of the resized Mat
    val avgColor = Core.mean(resizedMat)

// Use a DoubleArray to hold the RGB values
    val avgValues = doubleArrayOf(avgColor.`val`[0], avgColor.`val`[1], avgColor.`val`[2])

// Put the values into avgMat. Make sure to pass a vararg of Double
    avgMat.put(0, 0, *avgValues)  // Spread the array (use * to pass as vararg)


    var newAvgR = avgMat.get(0, 0)[0]
    var hueShift = 0
    while (newAvgR < MIN_AVG_RED) {
        val shifted = hueShiftRed(avgMat, hueShift.toDouble())
        val sumElems = Core.sumElems(shifted)
        newAvgR = sumElems.`val`[0] // For red channel sum
        hueShift++
        if (hueShift > MAX_HUE_SHIFT) {
            newAvgR = MIN_AVG_RED.toDouble()
        }
    }

    val shiftedMat = hueShiftRed(mat, hueShift.toDouble())
    val newRChannel = Mat()
    val sumElems = Core.sumElems(shiftedMat)
    newRChannel.put(0, 0, sumElems.`val`[0])

    val histR = IntArray(256)
    val histG = IntArray(256)
    val histB = IntArray(256)

    val normalizeMat = Array(256) { DoubleArray(3) }

    val thresholdLevel = (mat.rows() * mat.cols()) / THRESHOLD_RATIO
    for (x in 0 until 256) {
        if (histR[x] < thresholdLevel) normalizeMat[x][0] = x.toDouble()
        if (histG[x] < thresholdLevel) normalizeMat[x][1] = x.toDouble()
        if (histB[x] < thresholdLevel) normalizeMat[x][2] = x.toDouble()
    }

    normalizeMat[255][0] = 255.0
    normalizeMat[255][1] = 255.0
    normalizeMat[255][2] = 255.0

    val (adjustRLow, adjustRHigh) = normalizingInterval(normalizeMat.map { it[0].toInt().toDouble() }.toDoubleArray())
    val (adjustGLow, adjustGHigh) = normalizingInterval(normalizeMat.map { it[1].toInt().toDouble() }.toDoubleArray())
    val (adjustBLow, adjustBHigh) = normalizingInterval(normalizeMat.map { it[2].toInt().toDouble() }.toDoubleArray())


    val shifted = hueShiftRed( Mat(1, 1, CvType.CV_8UC3, Scalar(1.0, 1.0, 1.0)), hueShift.toDouble())
    val shiftedR = 0.0//shifted.get(0, 0)[0]
    val shiftedG = 0.0//shifted.get(0, 1)[0]
    val shiftedB =0.0// shifted.get(0, 2)[0]

    val redGain = 256 / (adjustRHigh - adjustRLow).toDouble()
    val greenGain = 256 / (adjustGHigh - adjustGLow).toDouble()
    val blueGain = 256 / (adjustBHigh - adjustBLow).toDouble()

    val redOffset = (-adjustRLow / 256.0) * redGain
    val greenOffset = (-adjustGLow / 256.0) * greenGain
    val blueOffset = (-adjustBLow / 256.0) * blueGain

    val adjustedRed = shiftedR * redGain
    val adjustedRedGreen = shiftedG * redGain
    val adjustedRedBlue = shiftedB * redGain * BLUE_MAGIC_VALUE

    return doubleArrayOf(
        adjustedRed, adjustedRedGreen, adjustedRedBlue, 0.0, redOffset,
        0.0, greenGain, 0.0, 0.0, greenOffset,
        0.0, 0.0, blueGain, 0.0, blueOffset,
        0.0, 0.0, 0.0, 1.0, 0.0
    )
}





// Main function to correct the image
    fun correct(mat: Mat): Mat {
        val filterMatrix = getFilterMatrix(mat)
        val correctedMat = applyFilter(mat, filterMatrix)

        // Ensure correctedMat is in the correct format (CV_8U) before applying color space conversion
        val correctedMat8U = Mat()
        correctedMat.convertTo(correctedMat8U, CV_8U)  // Convert from CV_64F to CV_8U

        // Ensure the image is in BGR (OpenCV default)
        Imgproc.cvtColor(correctedMat8U, correctedMat8U, Imgproc.COLOR_RGB2BGR)

        return correctedMat8U
    }

// Example usage to load and save corrected image
fun correctImage(inputPath: String, outputPath: String) {
    val mat = Imgcodecs.imread(inputPath)
    Imgproc.cvtColor(mat, mat, Imgproc.COLOR_BGR2RGB)

    val correctedMat = correct(mat)
    Imgcodecs.imwrite(outputPath, correctedMat)
}

// Example usage to load and save corrected image
/*fun correctImage(inputPath: String, outputPath: String) {
    val mat = Imgcodecs.imread(inputPath)
    Imgproc.cvtColor(mat, mat, Imgproc.COLOR_BGR2RGB)

    val correctedMat = correct(mat)
    Imgcodecs.imwrite(outputPath, correctedMat)
}*/

fun bitmapToMat(bitmap: Bitmap): Mat {
    val mat = Mat()

    // Convert the Bitmap to Mat (RGBA to CV_8UC4)
    Utils.bitmapToMat(bitmap, mat)

    // Convert from RGBA (Android Bitmap) to BGR (OpenCV)
    // If the image has an alpha channel, we need to convert to BGR, ignoring the alpha channel
    if (mat.channels() == 4) {
        Imgproc.cvtColor(mat, mat, Imgproc.COLOR_RGBA2BGR)
    } else {
        Imgproc.cvtColor(mat, mat, Imgproc.COLOR_RGB2BGR)
    }

    return mat
}

// Function to convert Mat to Bitmap
fun matToBitmap(mat: Mat): Bitmap {
    // Convert the Mat to CV_8UC3 (standard 3-channel image format)
    val mat8U = Mat()
    if (mat.depth() == CV_64F) {
        mat.convertTo(mat8U, CV_8U)  // Convert from CV_64F to CV_8U
    } else {
        mat.copyTo(mat8U)  // Copy the Mat if it's already in a valid format
    }

    // Ensure that the image is in BGR format (OpenCV default)
    if (mat8U.channels() == 3) {
        // Convert RGB to BGR if necessary
        Imgproc.cvtColor(mat8U, mat8U, Imgproc.COLOR_RGB2BGR)
    }

    // Create a Bitmap and convert the Mat to it
    val bitmap = Bitmap.createBitmap(mat8U.cols(), mat8U.rows(), Bitmap.Config.ARGB_8888)
    Utils.matToBitmap(mat8U, bitmap)

    return bitmap
}

fun getColourCorrectedBitmap(inputBitmap: Bitmap): Bitmap{
    val mat8U = Mat()
    bitmapToMat(inputBitmap).convertTo(mat8U, CV_8U)
    return matToBitmap(correct(mat8U))
}

fun initOpenCV(){
    if (OpenCVLoader.initLocal()) {
        Log.i(TAG, "OpenCV loaded successfully");
    } else {
        Log.e(TAG, "OpenCV initialization failed!");
        return;
    }
}


