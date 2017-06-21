//https://stackoverflow.com/questions/27272845/how-to-use-androids-camera-or-camera2-api-to-support-old-and-new-api-versions-w
//http://www.akexorcist.com/2013/02/android-code.html

package com.example.somjetr.mymapandplace;


import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.BitmapFactory;
import android.hardware.Camera;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;

/**
 * Created by somjet.r on 2017-06-01.
 */

public class CameraActivity extends AppCompatActivity implements SurfaceHolder.Callback , Camera.PictureCallback {

    Camera mCamera;
    SurfaceView mPreview;
    Button btnTakePic;

    @Override
    protected void onCreate(Bundle savedInstanceState)  {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_camera);

        //permission
        if (ContextCompat.checkSelfPermission(CameraActivity.this, Manifest.permission.CAMERA)  != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(CameraActivity.this, new String[] {Manifest.permission.CAMERA}, 123);
            return;
        }

        mPreview = (SurfaceView)findViewById(R.id.sfView);
        mPreview.getHolder().addCallback(this);


        btnTakePic = (Button)findViewById(R.id.btnTakePic);
        btnTakePic.setOnClickListener(new Button.OnClickListener() {
            public void onClick(View v) {
                mCamera.takePicture(null, null, null, CameraActivity.this);

            }
        });

    }


    public void onResume() {
        super.onResume();
        mCamera = Camera.open();
    }

    public void onPause() {
        super.onPause();
        mCamera.release();
    }


    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        try {
            mCamera.setPreviewDisplay(mPreview.getHolder());
            mCamera.startPreview();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
    }


    @Override
    public void onPictureTaken(byte[] picData, Camera camera) {

        File imagesFolder = new File(Environment.getExternalStorageDirectory()
                , "DCIM/MyCamera");
        imagesFolder.mkdirs();

        File file = new File(Environment.getExternalStorageDirectory(), "IMG_" + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(Calendar.getInstance().getTime())+".jpg");


        if (ContextCompat.checkSelfPermission(CameraActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE)  != PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(CameraActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE)  != PackageManager.PERMISSION_GRANTED
                ) {
            ActivityCompat.requestPermissions(CameraActivity.this, new String[] {Manifest.permission.WRITE_EXTERNAL_STORAGE}, 123);
            return;
        }

        try {

            FileOutputStream fos=new FileOutputStream(file.getPath());

            //<100kb
            fos.write(BrowsePicAndResizeActivity.ResizeImg(BitmapFactory.decodeByteArray(picData, 0, picData.length),100,""));
            fos.close();
            Toast.makeText(CameraActivity.this, "Save Complete. " + file.getPath(), Toast.LENGTH_SHORT).show();


            //delay
            Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    mCamera.stopPreview();
                    mCamera.startPreview();
                }
            }, 2000);



        } catch (IOException e) {
            e.printStackTrace();
        }



    }
}
