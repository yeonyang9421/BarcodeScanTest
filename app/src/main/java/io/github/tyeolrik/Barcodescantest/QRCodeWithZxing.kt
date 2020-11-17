package io.github.tyeolrik.Barcodescantest

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.zxing.BarcodeFormat
import com.google.zxing.ResultPoint
import com.google.zxing.client.android.BeepManager
import com.journeyapps.barcodescanner.BarcodeCallback
import com.journeyapps.barcodescanner.BarcodeResult
import com.journeyapps.barcodescanner.DecoratedBarcodeView
import com.journeyapps.barcodescanner.DefaultDecoderFactory
import java.util.*

class QRCodeWithZxing : AppCompatActivity() {
    private var barcodeView: DecoratedBarcodeView? = null
    private var beepManager: BeepManager? = null
    private var lastText: String? = null

    private val REQUEST_CODE_PERMISSIONS = 10
    private val REQUIRED_PERMISSIONS = Arrays.asList(Manifest.permission.CAMERA)

    private lateinit var tvLoginId: TextView
    private lateinit var tvLoginPass: TextView
    private lateinit var textViewResult: TextView

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String?>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (allPermissionsGranted()) {
                // 허락 됨
                Toast.makeText(this, "허락 됨", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this,
                        "Permissions not granted by the user.",
                        Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }

    private fun allPermissionsGranted(): Boolean {
        for (permission in REQUIRED_PERMISSIONS) {
            if (ContextCompat.checkSelfPermission(
                            applicationContext, permission) != PackageManager.PERMISSION_GRANTED) {
                return false
            }
        }
        return true
    }


    private val callback: BarcodeCallback = object : BarcodeCallback {
        override fun barcodeResult(result: BarcodeResult) {
            Log.d("ysh-result ", "barcodeResult: ${result}")
            if (result.text == null || result.text == lastText) {
                // Prevent duplicate scans
                return
            }
            lastText = result.text
            barcodeView!!.setStatusText(result.text)
            beepManager!!.playBeepSoundAndVibrate()

            textViewResult.text = result.text
//                    beepManager!!.playBeepSoundAndVibrate()
            val array = result.text.split("/").toTypedArray()
            for (i in array.indices) {
                tvLoginId?.setText(array[0])
                tvLoginPass?.setText(array[1])
            }

        }

        override fun possibleResultPoints(resultPoints: List<ResultPoint>) {}
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_q_r_code_with_zxing)

        barcodeView = findViewById<View>(R.id.barcode_scanner) as DecoratedBarcodeView
        tvLoginId = findViewById(R.id.tv_login_id_for_zxing)
        tvLoginPass = findViewById(R.id.tv_login_ps_for_zxing)
        textViewResult = findViewById(R.id.textViewResult_for_zxing)

        initBarcodeView()

        if (allPermissionsGranted()) {
            // 허락 됨
//            Toast.makeText(this, "허락 됨", Toast.LENGTH_SHORT).show();
            barcodeView?.decodeContinuous(callback)
        } else {
            ActivityCompat.requestPermissions(
                    this, (REQUIRED_PERMISSIONS.toTypedArray() as Array<String?>), REQUEST_CODE_PERMISSIONS)
        }
    }

    private fun initBarcodeView() {
        val formats: Collection<BarcodeFormat> = Arrays.asList(BarcodeFormat.QR_CODE, BarcodeFormat.CODE_39)
        barcodeView?.getBarcodeView()?.decoderFactory = DefaultDecoderFactory(formats)
        barcodeView?.initializeFromIntent(intent)
        barcodeView?.decodeContinuous(callback)

        beepManager = BeepManager(this)
    }

    override fun onResume() {
        super.onResume()
        barcodeView!!.resume()
    }

    override fun onPause() {
        super.onPause()
        barcodeView!!.pause()
    }

    fun pause(view: View?) {
        barcodeView!!.pause()
    }

    fun resume(view: View?) {
        barcodeView!!.resume()
    }

    fun triggerScan(view: View?) {
        barcodeView!!.decodeSingle(callback)
    }


}