package com.albaincompaniesltd.app.midal;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.util.SparseArray;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.vision.CameraSource;
import com.google.android.gms.vision.Detector;
import com.google.android.gms.vision.text.TextBlock;
import com.google.android.gms.vision.text.TextRecognizer;

import java.io.IOException;

public class MainActivity extends AppCompatActivity {

    SurfaceView cameraView;
    TextView txtdisplay;
    CameraSource cameraSource;
    final int RequestCameraPermissionID = 1001;
    Button plate;



    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

    switch (requestCode) {
        case RequestCameraPermissionID:
        {

            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED)
                    return;
            }

            try {
                cameraSource.start(cameraView.getHolder());
            } catch (IOException e) {
                e.printStackTrace();
            }


        }


    }

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        cameraView = (SurfaceView) findViewById(R.id.surface_view);
        txtdisplay = findViewById(R.id.txtview);
        plate = findViewById(R.id.btnPlate);

        plate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //

                String plateNo = txtdisplay.getText().toString().trim();

                Intent intent = new Intent(MainActivity.this, ReportActivity.class);
                startActivity(intent);
                intent.putExtra("PlateNumber", plateNo);


            }
        });


        TextRecognizer textRecognizer = new TextRecognizer.Builder(getApplicationContext()).build();
        if (!textRecognizer.isOperational()) {

            Log.w("MainActivity", "Dependancies not available");
        } else {


            cameraSource = new CameraSource.Builder(getApplicationContext(), textRecognizer)
                    .setFacing(CameraSource.CAMERA_FACING_BACK)
                    .setRequestedPreviewSize(1280, 1024)
                    .setRequestedFps(2.0f)
                    .setAutoFocusEnabled(true)
                    .build();

            cameraView.getHolder().addCallback(new SurfaceHolder.Callback() {
                @Override
                public void surfaceCreated(SurfaceHolder holder) {

                    try {

                        if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                              ActivityCompat.requestPermissions(MainActivity.this,
                                      new String[]{Manifest.permission.CAMERA},
                                      RequestCameraPermissionID);

                            return;
                        }
                        cameraSource.start(cameraView.getHolder());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                }

                @Override
                public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

                }

                @Override
                public void surfaceDestroyed(SurfaceHolder holder) {

                    cameraSource.stop();

                }
            });

            textRecognizer.setProcessor(new Detector.Processor<TextBlock>() {
                @Override
                public void release() {

                }

                @Override
                public void receiveDetections(Detector.Detections<TextBlock> detections) {

                    final SparseArray<TextBlock> items = detections.getDetectedItems();
                    if (items.size()!=0 ) {


                        txtdisplay.post(new Runnable() {
                            @Override
                            public void run() {
                                StringBuilder builder = new StringBuilder();
                                for (int x = 0; x<items.size(); ++x) {

                                  TextBlock item = items.valueAt(x);
                                  builder.append(item.getValue());
                                  builder.append("\n");

                                }

                                txtdisplay.setText(builder.toString());
                            }
                        });
                    }


                }
            });
        }


    }
}
