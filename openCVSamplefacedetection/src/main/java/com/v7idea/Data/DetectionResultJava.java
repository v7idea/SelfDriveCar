package com.v7idea.Data;

/**
 * Created by mortal on 2017/7/21.
 */

public class DetectionResultJava
{
    public int leftTopX;
    public int leftTopY;
    public int leftBottomX;
    public int leftBottomY;
    public double leftSlope;
    public double leftIntercept;

    public int rightTopX;
    public int rightTopY;
    public int rightBottomX;
    public int rightBottomY;
    public double rightSlope;
    public double rightIntercept;

    public DetectionResultJava(int leftTopX, int leftTopY, int leftBottomX, int leftBottomY, double leftSlope, double leftIntercept
            , int rightTopX, int rightTopY, int rightBottomX, int rightBottomY, double rightSlope, double rightIntercept) {
        this.leftTopX = leftTopX;
        this.leftTopY = leftTopY;
        this.leftBottomX = leftBottomX;
        this.leftBottomY = leftBottomY;
        this.leftSlope = leftSlope;
        this.leftIntercept = leftIntercept;
        this.rightTopX = rightTopX;
        this.rightTopY = rightTopY;
        this.rightBottomX = rightBottomX;
        this.rightBottomY = rightBottomY;
        this.rightSlope = rightSlope;
        this.rightIntercept = rightIntercept;
    }

    public DetectionResultJava(double[] arrayData)
    {
        leftTopX = (int)arrayData[0];
        leftTopY = (int)arrayData[1];
        leftBottomX = (int)arrayData[2];
        leftBottomY = (int)arrayData[3];
        leftSlope = arrayData[4];
        leftIntercept = arrayData[5];

        rightTopX = (int)arrayData[6];
        rightTopY = (int)arrayData[7];
        rightBottomX = (int)arrayData[8];
        rightBottomY = (int)arrayData[9];
        rightSlope = arrayData[10];
        rightIntercept = arrayData[11];

        arrayData = null;
    }
}
