package com.liye.camera;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.ImageFormat;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.CaptureResult;
import android.hardware.camera2.TotalCaptureResult;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.media.ImageReader;
import android.os.Handler;
import android.os.HandlerThread;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.util.Size;
import android.view.Surface;
import android.view.TextureView;
import android.widget.Toast;

import java.util.Arrays;
import java.util.List;

public class CameraHelper {
    private Context mContext;
    private HandlerThread mBackgroundThread;
    private Handler mBackgroundHandler;
    private CameraManager cameraManager;
    private String[] idList;
    private String TAG = "CameraHelper";
    private List<CameraCharacteristics.Key<?>> list;
    private Size[] size;
    private CameraDevice mCameraDevice;
    private TextureView mTextureView;
    private ImageReader mImageReader;
    private CameraCaptureSession mCaptureSession;
    private String currentCameraId = "0";


    public CameraHelper(Context context,TextureView textureView) {
        mContext = context;
        init();
        openCameraPreInit();
        mTextureView = textureView;

    }

    private ImageReader.OnImageAvailableListener mOnImageAvailableListener = new ImageReader.OnImageAvailableListener() {
        @Override
        public void onImageAvailable(ImageReader reader) {

            mBackgroundHandler.post(new ImageSaver(reader.acquireNextImage(),mContext));
            Log.d(TAG, "onImageAvailable: ");
        }
    };


    private CameraDevice.StateCallback mStateCallback = new CameraDevice.StateCallback() {

        private CaptureRequest.Builder mPreviewRequestBuilder;

        @Override
        public void onOpened(CameraDevice camera) {
            Log.d("liye", "onOpened: ");
            Toast.makeText(mContext, "open sucessful ", Toast.LENGTH_LONG).show();
            mCameraDevice = camera;
            SurfaceTexture surfcetexture = mTextureView.getSurfaceTexture();
            Surface surface = new Surface(surfcetexture);
            try {
                mPreviewRequestBuilder = mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
            } catch (CameraAccessException e) {
                e.printStackTrace();
            }

            mPreviewRequestBuilder.addTarget(surface);
            //大小需要设置
            mImageReader = ImageReader.newInstance(2560, 1920, ImageFormat.JPEG, 2);
            mImageReader.setOnImageAvailableListener(mOnImageAvailableListener, mBackgroundHandler);
           // mPreviewRequestBuilder.addTarget(mImageReader.getSurface());
            try {
                //此方法需要了解  参数
                mCameraDevice.createCaptureSession(Arrays.asList(surface, mImageReader.getSurface()),
                        new CameraCaptureSession.StateCallback() {

                            @Override
                            public void onConfigured(CameraCaptureSession cameraCaptureSession) {

                                mCaptureSession = cameraCaptureSession;
                                if (null == mCameraDevice) {
                                    return;
                                }

                                try {
                                    mPreviewRequestBuilder.set(CaptureRequest.CONTROL_AF_MODE,
                                            CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE);

                                    //需要了解第二参
                                    cameraCaptureSession.setRepeatingRequest(mPreviewRequestBuilder.build(),
                                            null, mBackgroundHandler);
                                } catch (CameraAccessException e) {
                                    e.printStackTrace();
                                }
                            }

                            @Override
                            public void onConfigureFailed(CameraCaptureSession cameraCaptureSession) {
                                Log.d(TAG, "onConfigureFailed: ");

                            }

                            @Override
                            public void onClosed(CameraCaptureSession session) {
                                super.onClosed(session);
                                Log.d("liye", "onClosed: ");
                            }
                        }, null
                );
            } catch (CameraAccessException e) {
                e.printStackTrace();
            }

        }

        @Override
        public void onDisconnected(@NonNull CameraDevice camera) {
            Toast.makeText(mContext, "open onDisconnected ", Toast.LENGTH_LONG).show();
        }

        @Override
        public void onError(@NonNull CameraDevice camera, int error) {
            Toast.makeText(mContext, "open onError ", Toast.LENGTH_LONG).show();

        }
    };


    private void init() {
        cameraManager = (CameraManager) mContext.getSystemService(Context.CAMERA_SERVICE);
        mBackgroundThread = new HandlerThread("CameraBackground");
        mBackgroundThread.start();
        mBackgroundHandler = new Handler(mBackgroundThread.getLooper());
    }

    private void openCameraPreInit() {
        try {
            //获取相机列表
            idList = cameraManager.getCameraIdList();
            if (idList == null)
                return;
            for (String i : idList) {
                //获取指定相机特性
                list = cameraManager.getCameraCharacteristics(i).getKeys();
                for (CameraCharacteristics.Key<?> a : list) {
                    Log.d(TAG + "liye", "  a: = " + a + "  value  = " + cameraManager.getCameraCharacteristics(i).get(a));
                }
                Log.d(TAG + "liye", "  i = " + i + "  ---------------------------------------------------");

                //获取支持的分辨率
                StreamConfigurationMap map = cameraManager.getCameraCharacteristics(i).get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
                size = map.getOutputSizes(ImageFormat.JPEG);
                for (Size a : size) {
                    Log.d(TAG + "liye", "  ImageFormat.JPEG  size: = " + a);
                }

                size = map.getOutputSizes(SurfaceTexture.class);
                for (Size a : size) {
                    Log.d(TAG + "liye", "  SurfaceTexture.class  size: = " + a);
                }


            }

        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }



    public void openCamera(String cameraId) {
        Log.d(TAG, "openCamera: 1");

        if (ActivityCompat.checkSelfPermission(mContext, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            Toast.makeText(mContext,"请打开相机权限",Toast.LENGTH_LONG).show();
            return;
        }
        try {
            Log.d(TAG, "openCamera: 2");
            cameraManager.openCamera(cameraId, mStateCallback, mBackgroundHandler);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    public void takePicture(){
        final CaptureRequest.Builder captureBuilder;
        try {
            captureBuilder = mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE);
            captureBuilder.addTarget(mImageReader.getSurface());

            // Use the same AE and AF modes as the preview.
            captureBuilder.set(CaptureRequest.CONTROL_AF_MODE,
                    CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE);
            CameraCaptureSession.CaptureCallback CaptureCallback
                    = new CameraCaptureSession.CaptureCallback() {
                @Override
                public void onCaptureCompleted(@NonNull CameraCaptureSession session,
                                               @NonNull CaptureRequest request,
                                               @NonNull TotalCaptureResult result) {
                }
            };
            mCaptureSession.capture(captureBuilder.build(), CaptureCallback, null);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    public void releaseCamera(){
        mCameraDevice.close();
    }

    public void  switchCamera(){
        releaseCamera();
        if(currentCameraId.equals("0")){
            currentCameraId = "1";
        }else{
            currentCameraId ="0";
        }
        openCamera(currentCameraId);
    }

    private CameraCaptureSession.CaptureCallback mCaptureCallback
            = new CameraCaptureSession.CaptureCallback() {
        private void process(CaptureResult result) {

        }

        @Override
        public void onCaptureProgressed( CameraCaptureSession session,
                                         CaptureRequest request,
                                         CaptureResult partialResult) {
            process(partialResult);
        }

        @Override
        public void onCaptureCompleted( CameraCaptureSession session,
                                        CaptureRequest request,
                                        TotalCaptureResult result) {
            process(result);
        }

    };



}
