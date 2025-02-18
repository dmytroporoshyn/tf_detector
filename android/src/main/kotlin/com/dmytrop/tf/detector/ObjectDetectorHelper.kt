package com.dmytrop.tf.detector

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Matrix
import android.os.SystemClock
import android.renderscript.Allocation
import android.renderscript.Element
import android.renderscript.RenderScript
import android.renderscript.ScriptIntrinsicYuvToRGB
import android.renderscript.Type
import android.util.Log
import com.google.android.gms.tflite.client.TfLiteInitializationOptions
import com.google.android.gms.tflite.gpu.support.TfLiteGpu
import com.google.android.odml.image.MlImage
import io.flutter.plugin.common.MethodChannel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.tensorflow.lite.support.image.ImageProcessor
import org.tensorflow.lite.support.image.TensorImage
import org.tensorflow.lite.support.image.ops.Rot90Op
import org.tensorflow.lite.task.core.BaseOptions
import org.tensorflow.lite.task.gms.vision.TfLiteVision
import org.tensorflow.lite.task.gms.vision.detector.Detection
import org.tensorflow.lite.task.gms.vision.detector.ObjectDetector
import java.io.File
import java.io.FileInputStream
import java.io.IOException
import java.nio.ByteBuffer
import java.nio.channels.FileChannel

class ObjectDetectorHelper(
        private val context: Context,
) {

    private val TAG = "ObjectDetectionHelper"

    // For this example this needs to be a var so it can be reset on changes. If the ObjectDetector
    // will not change, a lazy val would be preferable.
    private var objectDetector: ObjectDetector? = null
    private var gpuSupported = false

//    init {
//        val gpuAvailable = TfLiteGpu.isGpuDelegateAvailable(context).result;
//        val optionsBuilder =
//            TfLiteInitializationOptions.builder()
//        if (gpuAvailable) {
//            optionsBuilder.setEnableGpuDelegateSupport(true)
//        }
//        TfLiteVision.initialize(context, optionsBuilder.build()).await()
//    }

    fun init(result: MethodChannel.Result) {
        TfLiteGpu.isGpuDelegateAvailable(context).onSuccessTask { gpuAvailable: Boolean ->
            val optionsBuilder =
                    TfLiteInitializationOptions.builder()
            if (gpuAvailable) {
                optionsBuilder.setEnableGpuDelegateSupport(true)
            }
            TfLiteVision.initialize(context, optionsBuilder.build())
        }.addOnSuccessListener {
            result.success(true)
        }.addOnFailureListener {
            result.error("Could not init TfLiteVision", it.message, null)
        }
    }

    fun close() {
        objectDetector?.close()
        objectDetector = null
    }

    // Initialize the object detector using current settings on the
    // thread that is using it. CPU and NNAPI delegates can be used with detectors
    // that are created on the main thread and used on a background thread, but
    // the GPU delegate needs to be used on the thread that initialized the detector
    fun setupObjectDetector(
            model: String, threshold: Float = 0.4f,
            numThreads: Int = 2,
            maxResults: Int = 3,
            currentDelegate: Int = 0,
    ) {
        if (!TfLiteVision.isInitialized()) {
            Log.e(TAG, "setupObjectDetector: TfLiteVision is not initialized yet")
            return
        }

        // Create the base options for the detector using specifies max results and score threshold
        val optionsBuilder =
                ObjectDetector.ObjectDetectorOptions.builder()
                        .setScoreThreshold(threshold)
                        .setMaxResults(maxResults)

        // Set general detection options, including number of used threads
        val baseOptionsBuilder = BaseOptions.builder().setNumThreads(numThreads)

        // Use the specified hardware for running the model. Default to CPU
        when (currentDelegate) {
            DELEGATE_CPU -> {
                // Default
            }
            DELEGATE_GPU -> {
                if (gpuSupported) {
                    baseOptionsBuilder.useGpu()
                } else {
                    Log.e(TAG, "detect: GPU is not supported")
                    throw Exception()
                }
            }
            DELEGATE_NNAPI -> {
                baseOptionsBuilder.useNnapi()
            }
        }

        optionsBuilder.setBaseOptions(baseOptionsBuilder.build())

        try {
            val inputStream = FileInputStream(File(model))
            val fileChannel = inputStream.channel
            val declaredLength = fileChannel.size()
            val buffer = fileChannel.map(FileChannel.MapMode.READ_ONLY, 0, declaredLength)

            objectDetector =
                    ObjectDetector.createFromBufferAndOptions(buffer, optionsBuilder.build())

        } catch (e: Exception) {
            Log.e(TAG, "TFLite failed to load model with error: " + e.message, e)
            throw e
        }
    }

    fun detect(bytesList: List<ByteArray>, imageHeight: Int, imageWidth: Int, imageRotation: Int, result: MethodChannel.Result): MutableList<Detection>? {
        if (!TfLiteVision.isInitialized()) {
            Log.e(TAG, "detect: TfLiteVision is not initialized yet")
            throw Exception()
        }

        if (objectDetector == null) {
            Log.e(TAG, "detect: Object Detector is not initialized yet")
            throw Exception()
        }

        // Inference time is the difference between the system time at the start and finish of the
        // process
        var inferenceTime = SystemClock.uptimeMillis()

        // Create preprocessor for the image.
        // See https://www.tensorflow.org/lite/inference_with_metadata/
        //            lite_support#imageprocessor_architecture

        Log.d("milo", "-imageRotation / 90:  " + (-imageRotation / 90))

        // Preprocess the image and convert it into a TensorImage for detection.

        CoroutineScope(Dispatchers.IO).launch {
            val image = feedInputTensorFrame(bytesList, imageHeight, imageWidth, 0)
            val resized = Bitmap.createScaledBitmap(image, 640, 480, true)

            val imageProcessor = ImageProcessor.Builder().add(Rot90Op(-imageRotation / 90)).build()
            val tensorImage = imageProcessor.process(TensorImage.fromBitmap(resized))

            val results = objectDetector!!.detect(tensorImage)

            withContext(Dispatchers.Main) {
                val detections = results.map { detection: Detection ->
                    val top = detection.boundingBox.top
                    val right = detection.boundingBox.right
                    val bottom = detection.boundingBox.bottom
                    val left = detection.boundingBox.left

                    val label = detection.categories[0].label
                    val score = detection.categories[0].score

                    val rect: MutableMap<String, Any> = HashMap()
                    rect["top"] = top
                    rect["right"] = right
                    rect["bottom"] = bottom
                    rect["left"] = left

                    val map: MutableMap<String, Any> = HashMap()
                    map["rect"] = rect
                    map["label"] = label
                    map["score"] = score

                    map
                }
                result.success(detections)// Update UI safely
            }
        }

//        val tensorImage = imageProcessor.process(TensorImage.fromBitmap(image))

//        val results = objectDetector?.detect(tensorImage)
//        inferenceTime = SystemClock.uptimeMillis() - inferenceTime
        return null;
//        objectDetectorListener.onResults(
//          results,
//          inferenceTime,
//          tensorImage.height,
//          tensorImage.width,
//            image)
    }

    @Throws(IOException::class)
    fun feedInputTensorFrame(bytesList: List<ByteArray>, imageHeight: Int, imageWidth: Int, rotation: Int): Bitmap {
        val Y = ByteBuffer.wrap(bytesList[0])
        val U = ByteBuffer.wrap(bytesList[1])
        val V = ByteBuffer.wrap(bytesList[2])
        val Yb = Y.remaining()
        val Ub = U.remaining()
        val Vb = V.remaining()
        val data = ByteArray(Yb + Ub + Vb)
        Y[data, 0, Yb]
        V[data, Yb, Vb]
        U[data, Yb + Vb, Ub]
        val bitmapRaw = Bitmap.createBitmap(imageWidth, imageHeight, Bitmap.Config.ARGB_8888)
        val bmData = renderScriptNV21ToRGBA888(
                context,
                imageWidth,
                imageHeight,
                data)
        bmData.copyTo(bitmapRaw)
        val matrix = Matrix()
        matrix.postRotate(rotation.toFloat())
//        return Bitmap.createBitmap(bitmapRaw, 0, 0, bitmapRaw.width, bitmapRaw.height, matrix, true)
//        feedInputTensor()

        return Bitmap.createBitmap(bitmapRaw, 0, 0, bitmapRaw.width, bitmapRaw.height, matrix, true)
    }

    private fun renderScriptNV21ToRGBA888(context: Context?, width: Int, height: Int, nv21: ByteArray): Allocation {
        // https://stackoverflow.com/a/36409748
        val rs = RenderScript.create(context)
        val yuvToRgbIntrinsic = ScriptIntrinsicYuvToRGB.create(rs, Element.U8_4(rs))
        val yuvType = Type.Builder(rs, Element.U8(rs)).setX(nv21.size)
        val `in` = Allocation.createTyped(rs, yuvType.create(), Allocation.USAGE_SCRIPT)
        val rgbaType = Type.Builder(rs, Element.RGBA_8888(rs)).setX(width).setY(height)
        val out = Allocation.createTyped(rs, rgbaType.create(), Allocation.USAGE_SCRIPT)
        `in`.copyFrom(nv21)
        yuvToRgbIntrinsic.setInput(`in`)
        yuvToRgbIntrinsic.forEach(out)
        return out
    }

    interface DetectorListener {

        fun onInitialized()
        fun onError(error: String)
        fun onResults(
                results: MutableList<Detection>?,
                inferenceTime: Long,
                imageHeight: Int,
                imageWidth: Int,
                image: Bitmap
        )
    }

    companion object {
        const val DELEGATE_CPU = 0
        const val DELEGATE_GPU = 1
        const val DELEGATE_NNAPI = 2
        const val MODEL_TRACKER_CURRENT = 0
//        const val MODEL_TRACKER_PREVIOUS = 1
    }
}
