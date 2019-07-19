package com.liye.camera;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.CaptureResult;
import android.hardware.camera2.TotalCaptureResult;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.PermissionChecker;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.TextureView;
import android.view.View;
import android.widget.Toast;


public class MainActivity extends AppCompatActivity {

    private String TAG = "MainActivity111";
    private TextureView mTextureView;
    private CameraDevice mCameraDevice;
    private View mButton;
    private CameraHelper mCameraHelper;
    private View switch_button;

    private View.OnClickListener mCaptureClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            mCameraHelper.takePicture();
        }
    } ;


    private final TextureView.SurfaceTextureListener mSurfaceTextureListener
            = new TextureView.SurfaceTextureListener() {

        @Override
        public void onSurfaceTextureAvailable(SurfaceTexture texture, int width, int height) {
            mCameraHelper.openCamera("0");
            Log.d(TAG,"onSurfaceTextureAvailable");
        }

        @Override
        public void onSurfaceTextureSizeChanged(SurfaceTexture texture, int width, int height) {

        }

        @Override
        public boolean onSurfaceTextureDestroyed(SurfaceTexture texture) {
            Log.d(TAG,"onSurfaceTextureDestroyed");
            mCameraHelper.releaseCamera();
            return true;
        }

        @Override
        public void onSurfaceTextureUpdated(SurfaceTexture texture) {
        }

    };
    private View.OnClickListener mSwitchClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            mCameraHelper.switchCamera();
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ScreenUtils.fullScreen(this);
        CheckerPermission();

        initView();
        mCameraHelper = new CameraHelper(getApplicationContext(),mTextureView);

    }


    private void CheckerPermission(){
        //动态权限检测
        if (PermissionChecker.checkSelfPermission(MainActivity.this, Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_DENIED) {
            ActivityCompat.requestPermissions(MainActivity.this, new String[] { Manifest.permission.CAMERA },
                    1);
        }
        if (PermissionChecker.checkSelfPermission(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                == PackageManager.PERMISSION_DENIED) {
            ActivityCompat.requestPermissions(MainActivity.this, new String[] {Manifest.permission.WRITE_EXTERNAL_STORAGE },
                    2);
        }
    }


    private void initView(){
        mTextureView = findViewById(R.id.preview_surface);
        mButton = findViewById(R.id.button);
        switch_button = findViewById(R.id.switch_button);
        mButton.setOnClickListener(mCaptureClickListener);
        switch_button.setOnClickListener(mSwitchClickListener);
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode == 1){
            if (grantResults[0]  == PackageManager.PERMISSION_GRANTED){
                Toast.makeText(MainActivity.this," 相机权限ok",Toast.LENGTH_SHORT).show();
                 mCameraHelper.openCamera("0");
            }else{
                Toast.makeText(MainActivity.this,"相机权限fail",Toast.LENGTH_SHORT).show();
                finish();
            }
        }
        if(requestCode == 2){
            if (grantResults.length>0  && grantResults[0]  == PackageManager.PERMISSION_GRANTED){
                Toast.makeText(MainActivity.this," 存储权限ok",Toast.LENGTH_SHORT).show();
                //initCamera();
            }else{
                Toast.makeText(MainActivity.this,"存储权限fail",Toast.LENGTH_SHORT).show();
                // finish();
            }
        }
    }
    @Override
    protected void onResume() {
        super.onResume();
        if(mTextureView != null ){
            mTextureView.setSurfaceTextureListener(mSurfaceTextureListener);}
    }

}
