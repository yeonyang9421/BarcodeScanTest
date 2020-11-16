package io.github.tyeolrik.Barcodescantest

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.util.SparseArray
import android.view.SurfaceHolder
import android.view.SurfaceView
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.android.gms.vision.CameraSource
import com.google.android.gms.vision.Detector
import com.google.android.gms.vision.barcode.Barcode
import com.google.android.gms.vision.barcode.BarcodeDetector
import com.google.mlkit.codelab.translate.util.ScopedExecutor
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.IOException
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors


class QRCodeScan3 : AppCompatActivity() {
    private lateinit var cameraSource: CameraSource
    var cameraSurface: SurfaceView? = null
    private var mbarcodes: SparseArray<Barcode>? = null
    private var tvLoginId: TextView? = null
    private var tvLoginPass: TextView? = null
    private var textViewResult: TextView? = null
    private lateinit var barcodeDetector: BarcodeDetector

    /** Blocking camera operations are performed using this executor */
    private lateinit var cameraExecutor: ExecutorService
    private lateinit var scopedExecutor: ScopedExecutor

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

        // Callback을 이용해서 SurfaceView를 실시간으로 Mobile Vision API와 연결
        cameraSurface!!.holder.addCallback(object : SurfaceHolder.Callback {
            override fun surfaceCreated(holder: SurfaceHolder) {
                try {   // try-catch 문은 Camera 권한획득을 위한 권장사항
                    if (ContextCompat.checkSelfPermission(applicationContext, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                        cameraSource.start(cameraSurface!!.holder) // Mobile Vision API 시작
                        return
                    }
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }

            override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {}
            override fun surfaceDestroyed(holder: SurfaceHolder) {
                cameraSource.stop() // SurfaceView가 종료되었을 때, Mobile Vision API 종료
                Log.d("NowStatus", "SurfaceView Destroyed and CameraSource Stopped")
            }
        })

        barcodeDetector()
//        if(barcodes !=null) Toast.makeText(QRCodeScan.this, "스캔완료!", Toast.LENGTH_SHORT).show();

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
                //                    runOnUiThread(new Runnable() {
    //                        @Override
    //                        public void run() {
    //                            Toast.makeText(QRCodeScan.this, "스캔완료!", Toast.LENGTH_SHORT).show();
    //
    //                        }
    //                    });
                CoroutineScope(Dispatchers.Main).launch {
//                    Toast.makeText(this@QRCodeScan3, "스캔완료!", Toast.LENGTH_SHORT).show();
                        textViewResult?.text=barcodeContents
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
}