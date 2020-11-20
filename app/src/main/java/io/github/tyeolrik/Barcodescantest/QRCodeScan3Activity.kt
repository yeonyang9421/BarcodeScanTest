package io.github.tyeolrik.Barcodescantest

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.*
import android.os.Bundle
import android.util.DisplayMetrics
import android.util.Log
import android.util.SparseArray
import android.view.SurfaceHolder
import android.view.SurfaceView
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.*
import androidx.camera.core.Camera
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import com.google.android.gms.vision.CameraSource
import com.google.android.gms.vision.Detector
import com.google.android.gms.vision.barcode.Barcode
import com.google.android.gms.vision.barcode.BarcodeDetector
import com.google.mlkit.codelab.translate.util.ScopedExecutor
import io.github.tyeolrik.Barcodescantest.util.BeepManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.IOException
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import kotlin.math.abs
import kotlin.math.ln
import kotlin.math.max
import kotlin.math.min

class QRCodeScan3Activity : AppCompatActivity() {
    private lateinit var cameraSource: CameraSource
    var cameraSurface: SurfaceView? = null
    private var mbarcodes: SparseArray<Barcode>? = null
    private var tvLoginId: TextView? = null
    private var tvLoginPass: TextView? = null
    private var textViewResult: TextView? = null
    private lateinit var barcodeDetector: BarcodeDetector
    private val beepManager: BeepManager? = null

    /** Blocking camera operations are performed using this executor */
    private lateinit var cameraExecutor: ExecutorService
    private lateinit var scopedExecutor: ScopedExecutor
    private var lastText: String? = null

    private var displayId: Int = -1
    private var cameraProvider: ProcessCameraProvider? = null
    private var camera: Camera? = null
    private var imageAnalyzer: ImageAnalysis? = null
    private lateinit var container: ConstraintLayout
    private  var viewFinder: PreviewView? = null


    companion object {
        const val DESIRED_WIDTH_CROP_PERCENT = 8
        const val DESIRED_HEIGHT_CROP_PERCENT = 74

        private const val REQUEST_CODE_PERMISSIONS = 10

        private val REQUIRED_PERMISSIONS = arrayOf(Manifest.permission.CAMERA)
        private const val RATIO_4_3_VALUE = 4.0 / 3.0
        private const val RATIO_16_9_VALUE = 16.0 / 9.0
        private const val TAG = "QrcodeScan3Activity"


    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_q_r_code_scan3)

        tvLoginId = findViewById(R.id.tv_login_id_for3)
        tvLoginPass = findViewById(R.id.tv_login_ps_for3)
        textViewResult = findViewById(R.id.textViewResult_for3)
        cameraSurface = findViewById<View>(R.id.cameraSurface_for3) as SurfaceView // SurfaceView 선언 :: Boilerplate

        // Initialize our background executor
        cameraExecutor = Executors.newSingleThreadExecutor()
        scopedExecutor = ScopedExecutor(cameraExecutor)

        barcodeDetector = BarcodeDetector.Builder(this)
                .setBarcodeFormats(Barcode.QR_CODE) // QR_CODE로 설정하면 좀더 빠르게 인식할 수 있습니다.
                .build()
        Log.d("NowStatus", "BarcodeDetector Build Complete")

        cameraSource = CameraSource.Builder(this, barcodeDetector)
                .setFacing(CameraSource.CAMERA_FACING_BACK)
                .setRequestedFps(29.8f)
                .setRequestedPreviewSize(1080, 1920)
                .setAutoFocusEnabled(true)
                .build()

        Log.d("NowStatus", "CameraSource Build Complete")


        // Callback을 이용해서 SurfaceView를 실시간으로 Mobile Vision API와 연결
//        cameraSurface?.apply {
//            setZOrderOnTop(true)
//            holder.setFormat(PixelFormat.TRANSPARENT)
        cameraSurface?.holder?.addCallback(object : SurfaceHolder.Callback {
            override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {
            }

            override fun surfaceCreated(holder: SurfaceHolder) {
                try {
                    if (ContextCompat.checkSelfPermission(applicationContext, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {

                        cameraSource.start(cameraSurface?.holder) // Mobile Vision API 시작
//                        cameraSurface?.holder?.let {
//                            drawOverlay(it, DESIRED_HEIGHT_CROP_PERCENT, DESIRED_WIDTH_CROP_PERCENT)
//                        }
                        return
                    }
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }

            override fun surfaceDestroyed(holder: SurfaceHolder) {
                cameraSource.stop() // SurfaceView가 종료되었을 때, Mobile Vision API 종료
                Log.d("NowStatus", "SurfaceView Destroyed and CameraSource Stopped")
            }
        })
//            holder.let {  drawOverlay(it, QrcodeScan2Activity.DESIRED_HEIGHT_CROP_PERCENT, QrcodeScan2Activity.DESIRED_WIDTH_CROP_PERCENT) }
        barcodeDetector()

        // Request camera permissions
        if (allPermissionsGranted()) {
            // Wait for the views to be properly laid out
            viewFinder?.post {
                // Keep track of the display in which this view is attached
                displayId = viewFinder!!.display.displayId

                // Set up the camera and its use cases
                setUpCamera()
            }
        } else {
            requestPermissions(REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS)
        }


    }


    private fun barcodeDetector() {
        barcodeDetector.setProcessor(object : Detector.Processor<Barcode?> {
            override fun release() {
                Log.d("NowStatus", "BarcodeDetector SetProcessor Released")
            }

            override fun receiveDetections(detections: Detector.Detections<Barcode?>) {
                //                mbarcodes = detections.detectedItems
                //                if (mbarcodes.size() != 0) {
                val barcodeContents = detections.detectedItems.valueAt(0)!!.displayValue // 바코드 인식 결과물
                Log.d("Detection", barcodeContents)
                if (barcodeContents == null || barcodeContents == lastText) {
                    // Prevent duplicate scans
                    return
                }
                lastText = barcodeContents

                CoroutineScope(Dispatchers.Main).launch {
                    Toast.makeText(this@QRCodeScan3Activity, "스캔완료!", Toast.LENGTH_SHORT).show();
                    textViewResult?.text = barcodeContents
//                    beepManager!!.playBeepSoundAndVibrate()
                    val array = barcodeContents.split("/").toTypedArray()
                    for (i in array.indices) {
                        tvLoginId?.setText(array[0])
                        tvLoginPass?.setText(array[1])
                    }
                    cameraExecutor.shutdown()
                    scopedExecutor.shutdown()

                }
            }
        })
    }

    override fun onDestroy() {
        super.onDestroy()
        // Shut down our background executor
        cameraExecutor.shutdown()
        scopedExecutor.shutdown()
    }

    private fun drawOverlay(
            holder: SurfaceHolder,
            heightCropPercent: Int,
            widthCropPercent: Int
    ) {
        val canvas = holder.lockCanvas()
        val bgPaint = Paint().apply {
            alpha = 140
        }
        canvas.drawPaint(bgPaint)
        val rectPaint = Paint()
        rectPaint.xfermode = PorterDuffXfermode(PorterDuff.Mode.CLEAR)
        rectPaint.style = Paint.Style.FILL
        rectPaint.color = Color.WHITE
        val outlinePaint = Paint()
        outlinePaint.style = Paint.Style.STROKE
        outlinePaint.color = Color.WHITE
        outlinePaint.strokeWidth = 4f
        val surfaceWidth = holder.surfaceFrame.width()
        val surfaceHeight = holder.surfaceFrame.height()

        val cornerRadius = 25f
        // Set rect centered in frame
        val rectTop = surfaceHeight * heightCropPercent / 8 / 100f
        val rectLeft = surfaceWidth * widthCropPercent / 2 / 100f
        val rectRight = surfaceWidth * (1 - widthCropPercent / 4 / 100f)
        val rectBottom = surfaceHeight * (1 - heightCropPercent / 4 / 100f)
        val rect = RectF(rectLeft, rectTop, rectRight, rectBottom)
        canvas.drawRoundRect(
                rect, cornerRadius, cornerRadius, rectPaint
        )
        canvas.drawRoundRect(
                rect, cornerRadius, cornerRadius, outlinePaint
        )
        val textPaint = Paint()
        textPaint.color = Color.WHITE
        textPaint.textSize = 50F

        val overlayText = getString(R.string.overlay_help)
        val textBounds = Rect()
        textPaint.getTextBounds(overlayText, 0, overlayText.length, textBounds)
        val textX = (surfaceWidth - textBounds.width()) / 2f
        val textY = rectBottom + textBounds.height() + 15f // put text below rect and 15f padding
        canvas.drawText(getString(R.string.overlay_help), textX, textY, textPaint)
        holder.unlockCanvasAndPost(canvas)
    }

    /**
     *  [androidx.camera.core.ImageAnalysisConfig] requires enum value of
     *  [androidx.camera.core.AspectRatio]. Currently it has values of 4:3 & 16:9.
     *
     *  Detecting the most suitable ratio for dimensions provided in @params by comparing absolute
     *  of preview ratio to one of the provided values.
     *
     *  @param width - preview width
     *  @param height - preview height
     *  @return suitable aspect ratio
     */
    private fun aspectRatio(width: Int, height: Int): Int {
        val previewRatio = ln(max(width, height).toDouble() / min(width, height))
        if (abs(previewRatio - ln(RATIO_4_3_VALUE))
                <= abs(previewRatio - ln(RATIO_16_9_VALUE))
        ) {
            return AspectRatio.RATIO_4_3
        }
        return AspectRatio.RATIO_16_9
    }


    /** Initialize CameraX, and prepare to bind the camera use cases  */
    private fun setUpCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)
        cameraProviderFuture.addListener(Runnable {

            // CameraProvider
            cameraProvider = cameraProviderFuture.get()

            // Build and bind the camera use cases
            bindCameraUseCases()
        }, ContextCompat.getMainExecutor(this))
    }

    private fun bindCameraUseCases() {
        val cameraProvider = cameraProvider
                ?: throw IllegalStateException("Camera initialization failed.")

        // Get screen metrics used to setup camera for full screen resolution
        val metrics = DisplayMetrics().also { viewFinder!!.display.getRealMetrics(it) }
        Log.d(TAG, "Screen metrics: ${metrics.widthPixels} x ${metrics.heightPixels}")

        val screenAspectRatio = aspectRatio(metrics.widthPixels, metrics.heightPixels)
        Log.d(TAG, "Preview aspect ratio: $screenAspectRatio")

        val rotation = viewFinder!!.display.rotation

        val preview = Preview.Builder()
                .setTargetAspectRatio(screenAspectRatio)
                .setTargetRotation(rotation)
                .build()

        // Build the image analysis use case and instantiate our analyzer
//        barcordResult()
        // Select back camera since text detection does not work with front camera
        val cameraSelector =
                CameraSelector.Builder().requireLensFacing(CameraSelector.LENS_FACING_BACK).build()

        try {
//             Unbind use cases before rebinding
            cameraProvider.unbindAll()
            camera = cameraProvider.bindToLifecycle(
                    this, cameraSelector, preview
            )
            // Bind use cases to camera
            preview.setSurfaceProvider(viewFinder!!.surfaceProvider)
        } catch (exc: IllegalStateException) {
            Log.e(TAG, "Use case binding failed. This must be running on main thread.", exc)
        }
    }


    /**
     * Process result from permission request dialog box, has the request
     * been granted? If yes, start Camera. Otherwise display a toast
     */
    override fun onRequestPermissionsResult(
            requestCode: Int, permissions: Array<String>, grantResults: IntArray
    ) {
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (allPermissionsGranted()) {
                viewFinder?.post {
                    // Keep track of the display in which this view is attached
                    displayId = viewFinder!!.display.displayId

                    // Set up the camera and its use cases
//                    setUpCamera()
                }
            } else {
                Toast.makeText(
                        this,
                        "Permissions not granted by the user.",
                        Toast.LENGTH_SHORT
                ).show()
            }
        }
    }


    /**
     * Check if all permission specified in the manifest have been granted
     */
    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(
                this, it
        ) == PackageManager.PERMISSION_GRANTED
    }


}