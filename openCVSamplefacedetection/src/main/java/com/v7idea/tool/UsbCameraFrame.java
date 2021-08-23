package com.v7idea.tool;

import android.graphics.Bitmap;
import android.util.Log;

import com.serenegiant.usb.UVCCamera;

import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.Utils;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;

import java.nio.ByteBuffer;

/**
 * Created by mortal on 2017/9/21.
 */

public class UsbCameraFrame implements CameraBridgeViewBase.CvCameraViewFrame {

    private final static String TAG = "UsbCameraFrame";

    private Mat rgbaMat = null;

    public void setData(Bitmap bitmap){
        if(rgbaMat == null){
            rgbaMat = new Mat();
        }

        Utils.bitmapToMat(bitmap, rgbaMat);
    }

    public void setByteBuffer(ByteBuffer byteBuffer){
        if(rgbaMat == null){
            rgbaMat = new Mat();
        }

        byte[] frameBytes = new byte[byteBuffer.remaining()];
        byteBuffer.get(frameBytes);

//        Log.e(TAG, "frameBytes length: "+frameBytes.length);

        Mat frameMat = new Mat(UVCCamera.DEFAULT_PREVIEW_HEIGHT + (UVCCamera.DEFAULT_PREVIEW_HEIGHT/2)
                , UVCCamera.DEFAULT_PREVIEW_WIDTH, CvType.CV_8UC1);
        frameMat.put(0, 0, frameBytes);

        Mat bgraMat = new Mat();

        Imgproc.cvtColor(frameMat, bgraMat, Imgproc.COLOR_YUV2BGRA_NV12, 4);
        Imgproc.cvtColor(bgraMat, rgbaMat, Imgproc.COLOR_BGRA2RGBA);
    }

    public void setRgbaMat(Mat rgbaMat){
        this.rgbaMat = rgbaMat;
    }

    public ByteBuffer getRGBAFrame(){
        return matToByteBuffer(rgbaMat);
    }

    public ByteBuffer getYV12Frame(){
        Mat yv12Mat = new Mat();
        Imgproc.cvtColor(rgbaMat, yv12Mat, Imgproc.COLOR_RGBA2YUV_YV12);

        return matToByteBuffer(yv12Mat);
    }

    private ByteBuffer matToByteBuffer(Mat mat){
        int cols = mat.cols();
        int rows = mat.rows();
        int elemSize = (int)mat.elemSize();
        byte[] data = new byte[cols * rows * elemSize];

        mat.get(0, 0, data);

        ByteBuffer buf = ByteBuffer.allocate(data.length);
        buf.put(data);

        return buf;
    }

    @Override
    public Mat rgba()
    {
        return rgbaMat;
    }

    @Override
    public Mat gray() {
        return null;
    }

    public UsbCameraFrame copy(){
        UsbCameraFrame usbCameraFrame = new UsbCameraFrame();

        Mat newRGBMat = new Mat();
        rgbaMat.copyTo(newRGBMat);

        usbCameraFrame.setRgbaMat(newRGBMat);

        return usbCameraFrame;
    }
}
