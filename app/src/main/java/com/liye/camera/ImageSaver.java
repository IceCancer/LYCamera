package com.liye.camera;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.Image;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

public class ImageSaver implements Runnable {
    private Image mReader;
    private Context mContext;
    private String TAG = "ImageSaver";

    public ImageSaver(Image reader, Context context) {
        mContext = context;
        mReader = reader;
    }

    @Override
    public void run() {
        Log.d(TAG, "正在保存图片");
         String DCIM_CAMERA_FOLDER_ABSOLUTE_PATH =
                Environment.getExternalStoragePublicDirectory(
                        Environment.DIRECTORY_DCIM).toString() + "/Camera";

         String   fileName = System.currentTimeMillis() + ".jpg";
        File file = new File(DCIM_CAMERA_FOLDER_ABSOLUTE_PATH, fileName);
        Toast.makeText(mContext,file.toString(),Toast.LENGTH_LONG).show();
        FileOutputStream outputStream = null;
        try {
            outputStream = new FileOutputStream(file);
            ByteBuffer buffer = mReader.getPlanes()[0].getBuffer();
            byte[] buff = new byte[buffer.remaining()];
            buffer.get(buff);
            BitmapFactory.Options ontain = new BitmapFactory.Options();
            ontain.inSampleSize = 100;
            Bitmap bm = BitmapFactory.decodeByteArray(buff, 0, buff.length, ontain);
            outputStream.write(buff);
            Log.d(TAG, "保存图片完成");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (mReader != null) {
                mReader.close();
            }
            if (outputStream != null) {
                try {
                    outputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        try {
            MediaStore.Images.Media.insertImage(mContext.getContentResolver(),DCIM_CAMERA_FOLDER_ABSOLUTE_PATH,fileName,null);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        Intent intent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        Uri uri = Uri.fromFile(file);
        intent.setData(uri);
        mContext.sendBroadcast(intent);
    }
}
