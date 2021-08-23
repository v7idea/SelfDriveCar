package com.v7idea.landdetecttest;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.util.Log;
import android.util.Size;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import org.opencv.android.Utils;
import org.opencv.core.Mat;
import org.opencv.videoio.VideoCapture;
import org.opencv.videoio.Videoio;

import java.util.List;

/**
 * Created by mortal on 2017/7/10.
 */

public class HelloOpenCVView extends SurfaceView implements SurfaceHolder.Callback, Runnable
{
    private VideoCapture mCamera;

    public HelloOpenCVView(Context context) {
        super(context);
    }

    public HelloOpenCVView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public HelloOpenCVView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {

    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {

    }

    @Override
    public void run() {
        while (true) {
            Bitmap bmp = null;
            synchronized (this) {
                if (mCamera == null)
                    break;
                if (!mCamera.grab())
                    break;

                bmp = processFrame(mCamera);
            }
            if (bmp != null) {
                Canvas canvas = getHolder().lockCanvas();
                if (canvas != null) {
                    canvas.drawBitmap(bmp, (canvas.getWidth()  - bmp.getWidth())  / 2,
                            (canvas.getHeight() - bmp.getHeight()) / 2, null);
                    getHolder().unlockCanvasAndPost(canvas);

                }
                bmp.recycle();
            }
        }
    }

    protected Bitmap processFrame(VideoCapture capture) {
        Mat mRgba = new Mat();
        capture.retrieve(mRgba);
        //process mRgba
        Bitmap bmp = Bitmap.createBitmap(mRgba.cols(), mRgba.rows(), Bitmap.Config.ARGB_8888);
        try {
            Utils.matToBitmap(mRgba, bmp);
        } catch(Exception e) {
            Log.e("processFrame", "Utils.matToBitmap() throws an exception: " + e.getMessage());
            bmp.recycle();
            bmp = null;
        }
        return bmp;
    }

    public boolean cameraOpen() {
        synchronized (this) {
            cameraRelease();
            mCamera = new VideoCapture(Videoio.CV_CAP_ANDROID_BACK);
            if (!mCamera.isOpened()) {
                mCamera.release();
                mCamera = null;
                Log.e("HelloOpenCVView", "Failed to open native camera");
                return false;
            }
        }
        return true;
    }

    public void cameraRelease() {
        synchronized(this) {
            if (mCamera != null) {
                mCamera.release();
                mCamera = null;
            }
        }
    }

    private void cameraSetup(int width, int height) {
        synchronized (this) {
            if (mCamera != null && mCamera.isOpened()) {

                int mFrameWidth = 960;
                int mFrameHeight = 540;

                mCamera.set(Videoio.CV_CAP_PROP_FRAME_WIDTH, mFrameWidth);
                mCamera.set(Videoio.CV_CAP_PROP_FRAME_HEIGHT, mFrameHeight);
            }
        }
    }
}
