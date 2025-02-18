package com.dmytrop.tf.detector

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.graphics.Matrix
import android.graphics.Paint
import android.renderscript.Allocation
import android.renderscript.Element
import android.renderscript.RenderScript
import android.renderscript.ScriptIntrinsicYuvToRGB
import android.renderscript.Type
import android.util.Log
import io.flutter.embedding.engine.plugins.FlutterPlugin
import io.flutter.embedding.engine.plugins.activity.ActivityAware
import io.flutter.embedding.engine.plugins.activity.ActivityPluginBinding
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugin.common.MethodChannel.MethodCallHandler
import io.flutter.plugin.common.MethodChannel.Result
import java.io.IOException
import java.nio.ByteBuffer
import java.nio.ByteOrder


private const val BYTES_PER_CHANNEL = 4


/** TfDetectorPlugin */
class TfDetectorPlugin : FlutterPlugin, MethodCallHandler, ActivityAware {
    /// The MethodChannel that will the communication between Flutter and native Android
    ///
    /// This local reference serves to register the plugin with the Flutter Engine and unregister it
    /// when the Flutter Engine is detached from the Activity
    private lateinit var channel: MethodChannel
    private var objectDetectorHelper: ObjectDetectorHelper? = null
    private lateinit var context: Context

    override fun onAttachedToEngine(flutterPluginBinding: FlutterPlugin.FlutterPluginBinding) {
        context = flutterPluginBinding.applicationContext
//    try {
//      Log.i("DETECTOR", "start")
//      objectDetectorHelper = ObjectDetectorHelper(
//              context = context
//      )
//      Log.i("DETECTOR", "objectDetectorHelper")
//    } catch (e: Exception) {
//      Log.i("DETECTOR", e.message ?: "ERROR")
//    }
        channel = MethodChannel(flutterPluginBinding.binaryMessenger, "tf_detector")
        channel.setMethodCallHandler(this)
    }

    override fun onMethodCall(call: MethodCall, result: Result) {
        Log.i("DETECTOR", call.method)
        when (call.method) {
            "init" -> {
                init(call, result)
            }
            "close" -> {
                close(call, result)
            }
            "setup" -> {
                setup(call, result)
            }

            "detect" -> {
                detect(call, result)
            }

            else ->
                result.notImplemented()
        }
    }

    private fun close(
            call: MethodCall,
            result: Result
    ) {
        try {
            objectDetectorHelper?.close()
            result.success(true)
        } catch (e: Exception) {
            result.error("Could not close detector", e.message, null)
        }
    }

    private fun init(
            call: MethodCall,
            result: Result
    ) {
        try {
            objectDetectorHelper = ObjectDetectorHelper(
                    context = context
            )
            objectDetectorHelper?.init(result)
        } catch (e: Exception) {
            result.error("Could not init detector", e.message, null)
        }
    }

    private fun setup(
            call: MethodCall,
            result: Result
    ) {
        try {
            val model = call.argument<String>("model")
            val threshold: Double = call.argument<Double>("threshold") ?: 0.4
            val numThreads = call.argument<Int>("numThreads") ?: 2
            val maxResults = call.argument<Int>("maxResults") ?: 3
            val delegate = call.argument<Int>("delegate") ?: 0
            Log.i("DETECTOR", model ?: "EMPTY")
            objectDetectorHelper?.setupObjectDetector(
                    model = model!!,
                    threshold = threshold.toFloat(),
                    numThreads = numThreads,
                    maxResults = maxResults,
                    currentDelegate = delegate
            )
            result.success(true)
        } catch (e: Exception) {
            result.error("Could not setup detector", e.message, null)
        }
    }

    private fun detect(
            call: MethodCall,
            result: Result
    ) {
        try {
            val bytesList = call.argument<List<ByteArray>>("bytesList")!!
            val imageHeight = call.argument<Int>("imageHeight")!!
            val imageWidth = call.argument<Int>("imageWidth")!!


//            buffer.rewind()
//            val bitmap = Bitmap.createBitmap(512, 512, Bitmap.Config.ARGB_8888)
//            bitmap.copyPixelsFromBuffer(buffer)


//            val bitmap = Bitmap.createBitmap(imgData.width, imgData.height, Bitmap.Config.ARGB_8888)
//            val matrix: Matrix = getTransformationMatrix(imgData.width, imgData.height,
//                    480, 640, false)
//            val test = Bitmap.createBitmap(bitmap, 0, 0, 480, 640, matrix, true)
//
//
//            Log.i("DETECTOR", imgData.height.toString())
//            Log.i("DETECTOR", imgData.width.toString())


//            val resizedBmp: Bitmap = Bitmap.createBitmap(imgData, 0, 0, 480, 640)


//            Log.i("DETECTOR", resized.height.toString())
//            Log.i("DETECTOR", resized.width.toString())
//            val bitmap = Bitmap.createBitmap(
//                    imageWidth,
//                    imageHeight,
//                    Bitmap.Config.ARGB_8888
//            )
//            bitmap.copyPixelsFromBuffer(imgData)


//            val bitmap = Bitmap.createBitmap(
//                    imageWidth,
//                    imageHeight,
//                    Bitmap.Config.ARGB_8888
//            )
//            val buffer = ByteBuffer.wrap(bytesList[0])
//            buffer.rewind();
//            bitmap.copyPixelsFromBuffer(buffer)
//
            objectDetectorHelper?.detect(
                    bytesList, imageHeight, imageWidth,
                    imageRotation = 90,
                    result
            )

//            Log.i("DETECTOR", results.toString())

//            result.success(true)
        } catch (e: Exception) {
            Log.i("DETECTOR", e.stackTraceToString())
            result.error("Could not detect", e.message, null)
        }
    }


//    @Throws(IOException::class)
//    fun feedInputTensorFrame(bytesList: List<ByteArray?>, imageHeight: Int, imageWidth: Int, mean: Float, std: Float, rotation: Int): ByteBuffer {
//        val Y = ByteBuffer.wrap(bytesList[0])
//        val U = ByteBuffer.wrap(bytesList[1])
//        val V = ByteBuffer.wrap(bytesList[2])
//        val Yb = Y.remaining()
//        val Ub = U.remaining()
//        val Vb = V.remaining()
//        val data = ByteArray(Yb + Ub + Vb)
//        Y[data, 0, Yb]
//        V[data, Yb, Vb]
//        U[data, Yb + Vb, Ub]
//        var bitmapRaw = Bitmap.createBitmap(imageWidth, imageHeight, Bitmap.Config.ARGB_8888)
//        val bmData = renderScriptNV21ToRGBA888(
//                context,
//                imageWidth,
//                imageHeight,
//                data)
//        bmData.copyTo(bitmapRaw)
//        val matrix = Matrix()
//        matrix.postRotate(rotation.toFloat())
//        bitmapRaw = Bitmap.createBitmap(bitmapRaw, 0, 0, bitmapRaw.width, bitmapRaw.height, matrix, true)
//        return feedInputTensor(bitmapRaw, mean, std)
//    }

    @Throws(IOException::class)
    fun feedInputTensor(bitmapRaw: Bitmap, mean: Float, std: Float): ByteBuffer {
        val inputSize = 512
        val inputChannels = 3
//        val bytePerChannel = if (tensor.dataType() == DataType.UINT8) 1 else BYTES_PER_CHANNEL
//        val bytePerChannel = 1
        val bytePerChannel = BYTES_PER_CHANNEL

        val imgData = ByteBuffer.allocateDirect(1 * inputSize * inputSize * inputChannels * bytePerChannel)
        imgData.order(ByteOrder.nativeOrder())
        var bitmap = bitmapRaw
        if (bitmapRaw.width != inputSize || bitmapRaw.height != inputSize) {
            val matrix: Matrix = getTransformationMatrix(bitmapRaw.width, bitmapRaw.height,
                    inputSize, inputSize, false)
            bitmap = Bitmap.createBitmap(inputSize, inputSize, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bitmap)
            if (inputChannels == 1) {
                val paint = Paint()
                val cm = ColorMatrix()
                cm.setSaturation(0f)
                val f = ColorMatrixColorFilter(cm)
                paint.colorFilter = f
                canvas.drawBitmap(bitmapRaw, matrix, paint)
            } else {
                canvas.drawBitmap(bitmapRaw, matrix, null)
            }
        }
//        if (tensor.dataType() == DataType.FLOAT32) {
        for (i in 0 until inputSize) {
            for (j in 0 until inputSize) {
                val pixelValue = bitmap.getPixel(j, i)
                if (inputChannels > 1) {
                    imgData.putFloat(((pixelValue shr 16 and 0xFF) - mean) / std)
                    imgData.putFloat(((pixelValue shr 8 and 0xFF) - mean) / std)
                    imgData.putFloat(((pixelValue and 0xFF) - mean) / std)
                } else {
                    imgData.putFloat(((pixelValue shr 16 or (pixelValue shr 8) or pixelValue and 0xFF) - mean) / std)
                }
            }
        }
//        } else {
//            for (i in 0 until inputSize) {
//                for (j in 0 until inputSize) {
//                    val pixelValue = bitmap.getPixel(j, i)
//                    if (inputChannels > 1) {
//                        imgData.put((pixelValue shr 16 and 0xFF).toByte())
//                        imgData.put((pixelValue shr 8 and 0xFF).toByte())
//                        imgData.put((pixelValue and 0xFF).toByte())
//                    } else {
//                        imgData.put((pixelValue shr 16 or (pixelValue shr 8) or pixelValue and 0xFF).toByte())
//                    }
//                }
//            }
//        }
        return imgData
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

    private fun getTransformationMatrix(srcWidth: Int,
                                        srcHeight: Int,
                                        dstWidth: Int,
                                        dstHeight: Int,
                                        maintainAspectRatio: Boolean): Matrix {
        val matrix = Matrix()
        if (srcWidth != dstWidth || srcHeight != dstHeight) {
            val scaleFactorX = dstWidth / srcWidth.toFloat()
            val scaleFactorY = dstHeight / srcHeight.toFloat()
            if (maintainAspectRatio) {
                val scaleFactor = Math.max(scaleFactorX, scaleFactorY)
                matrix.postScale(scaleFactor, scaleFactor)
            } else {
                matrix.postScale(scaleFactorX, scaleFactorY)
            }
        }
        matrix.invert(Matrix())
        return matrix
    }

    override fun onDetachedFromEngine(binding: FlutterPlugin.FlutterPluginBinding) {
        channel.setMethodCallHandler(null)
    }

    override fun onAttachedToActivity(binding: ActivityPluginBinding) {
//    TODO("Not yet implemented")
    }

    override fun onDetachedFromActivityForConfigChanges() {
//    TODO("Not yet implemented")
    }

    override fun onReattachedToActivityForConfigChanges(binding: ActivityPluginBinding) {
//    TODO("Not yet implemented")
    }

    override fun onDetachedFromActivity() {
//    TODO("Not yet implemented")
    }
}
