package io.github.tyeolrik.Barcodescantest;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.util.SparseArray;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.TextView;

import androidx.core.content.ContextCompat;

import com.google.android.gms.vision.CameraSource;
import com.google.android.gms.vision.Detector;
import com.google.android.gms.vision.barcode.Barcode;
import com.google.android.gms.vision.barcode.BarcodeDetector;

import java.io.IOException;

public class QRCodeScan extends Activity {

    CameraSource cameraSource;
    SurfaceView cameraSurface;
    private SparseArray<Barcode> barcodes;
    private TextView tvLoginId, tvLoginPass, textViewResult;
    private BarcodeDetector barcodeDetector;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_qrcode_scan);

        tvLoginId = findViewById(R.id.tv_login_id);
        tvLoginPass = findViewById(R.id.tv_login_ps);
        textViewResult = findViewById(R.id.textViewResult);
        cameraSurface = (SurfaceView) findViewById(R.id.cameraSurface); // SurfaceView 선언 :: Boilerplate

        barcodeDetector = new BarcodeDetector.Builder(this)
                .setBarcodeFormats(Barcode.QR_CODE) // QR_CODE로 설정하면 좀더 빠르게 인식할 수 있습니다.
                .build();
        Log.d("NowStatus", "BarcodeDetector Build Complete");

        cameraSource = new CameraSource
                .Builder(this, barcodeDetector)
                .setFacing(CameraSource.CAMERA_FACING_BACK)
                .setRequestedFps(29.8f)
                .setRequestedPreviewSize(1080, 1920)
                .setAutoFocusEnabled(true)
                .build();
        Log.d("NowStatus", "CameraSource Build Complete");

        // Callback을 이용해서 SurfaceView를 실시간으로 Mobile Vision API와 연결
        cameraSurface.getHolder().addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder holder) {
                try {   // try-catch 문은 Camera 권한획득을 위한 권장사항
                    if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                        cameraSource.start(cameraSurface.getHolder());  // Mobile Vision API 시작
                        return;
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
            }

            @Override
            public void surfaceDestroyed(SurfaceHolder holder) {
                cameraSource.stop();    // SurfaceView가 종료되었을 때, Mobile Vision API 종료
                Log.d("NowStatus", "SurfaceView Destroyed and CameraSource Stopped");
            }
        });

        barcodeDetector.setProcessor(new Detector.Processor<Barcode>() {
            @Override
            public void release() {
                Log.d("NowStatus", "BarcodeDetector SetProcessor Released");
            }

            @Override
            public void receiveDetections(Detector.Detections<Barcode> detections) {
                barcodes = detections.getDetectedItems();
                if (barcodes.size() != 0) {
                    String barcodeContents = barcodes.valueAt(0).displayValue; // 바코드 인식 결과물
                    Log.d("Detection", barcodeContents);
//                    runOnUiThread(new Runnable() {
//                        @Override
//                        public void run() {
//                            Toast.makeText(QRCodeScan.this, "스캔완료!", Toast.LENGTH_SHORT).show();
//
//                        }
//                    });
                    textViewResult.setText(barcodeContents);
//                    String result = str.substring(str.lastIndexOf("/")+1);
                    String[] array = barcodeContents.split("/");
                    for (int i = 0; i < array.length; i++) {
                        tvLoginId.setText(array[0]);
                        tvLoginPass.setText(array[1]);

                    }

                }
            }

        });
//        if(barcodes !=null) Toast.makeText(QRCodeScan.this, "스캔완료!", Toast.LENGTH_SHORT).show();

    }
}
