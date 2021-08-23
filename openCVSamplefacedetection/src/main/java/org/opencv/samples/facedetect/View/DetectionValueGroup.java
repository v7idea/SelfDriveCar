package org.opencv.samples.facedetect.View;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TableLayout;
import android.widget.TableRow;

import com.v7idea.Data.DetectionValue;
import com.v7idea.template.View.V7TitleView;
import com.v7idea.tool.ViewScaling;

import org.opencv.samples.facedetect.R;

/**
 * Created by mortal on 2017/10/26.
 */

public class DetectionValueGroup extends RelativeLayout{

    public DetectionValueGroup(Context context) {
        super(context);
        init();
    }

    public DetectionValueGroup(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public DetectionValueGroup(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private LinearLayout contentLayout = null;
    private TableLayout tableLayout = null;

    private V7TitleView M1 = null;
    private V7TitleView C1 = null;
    private V7TitleView P11 = null;
    private V7TitleView P12 = null;

    private V7TitleView M2 = null;
    private V7TitleView C2 = null;
    private V7TitleView P21 = null;
    private V7TitleView P22 = null;

    private LinearLayout accArea = null;
    private V7TitleView accValue1 = null;
    private V7TitleView accValue2 = null;
    private V7TitleView accValue3 = null;

    private LinearLayout gyoArea = null;
    private V7TitleView gyoValue1 = null;
    private V7TitleView gyoValue2 = null;
    private V7TitleView gyoValue3 = null;

    private void init(){

        LayoutInflater inflater = LayoutInflater.from(getContext());

        contentLayout = (LinearLayout) inflater.inflate(R.layout.detect_line_value_group_layout, null);

        addView(contentLayout);

        ViewScaling.scaleLayout(contentLayout);

        tableLayout = (TableLayout) ViewScaling.findViewByIdAndScale(contentLayout, R.id.DetectionGroup_TableLayout_ValueGroup);

        M1 = (V7TitleView) ViewScaling.findViewByIdAndScale(contentLayout, R.id.DetectionGroup_V7TitleView_M1);
        C1 = (V7TitleView) ViewScaling.findViewByIdAndScale(contentLayout, R.id.DetectionGroup_V7TitleView_C1);
        P11 = (V7TitleView) ViewScaling.findViewByIdAndScale(contentLayout, R.id.DetectionGroup_V7TitleView_P11);
        P12 = (V7TitleView) ViewScaling.findViewByIdAndScale(contentLayout, R.id.DetectionGroup_V7TitleView_P12);

        M2 = (V7TitleView) ViewScaling.findViewByIdAndScale(contentLayout, R.id.DetectionGroup_V7TitleView_M2);
        C2 = (V7TitleView) ViewScaling.findViewByIdAndScale(contentLayout, R.id.DetectionGroup_V7TitleView_C2);
        P21 = (V7TitleView) ViewScaling.findViewByIdAndScale(contentLayout, R.id.DetectionGroup_V7TitleView_P21);
        P22 = (V7TitleView) ViewScaling.findViewByIdAndScale(contentLayout, R.id.DetectionGroup_V7TitleView_P22);

        accArea = (LinearLayout) ViewScaling.findViewByIdAndScale(contentLayout, R.id.DetectionGroup_LinearLayout_accArea);
        accValue1 = (V7TitleView) ViewScaling.findViewByIdAndScale(accArea, R.id.DetectionGroup_LinearLayout_accValue1);
        accValue2 = (V7TitleView) ViewScaling.findViewByIdAndScale(accArea, R.id.DetectionGroup_LinearLayout_accValue2);
        accValue3 = (V7TitleView) ViewScaling.findViewByIdAndScale(accArea, R.id.DetectionGroup_LinearLayout_accValue3);

        accArea.setVisibility(View.GONE);

        gyoArea = (LinearLayout) ViewScaling.findViewByIdAndScale(contentLayout, R.id.DetectionGroup_LinearLayout_gyoArea);
        gyoValue1 = (V7TitleView) ViewScaling.findViewByIdAndScale(gyoArea, R.id.DetectionGroup_V7TitleView_gyoValue1);
        gyoValue2 = (V7TitleView) ViewScaling.findViewByIdAndScale(gyoArea, R.id.DetectionGroup_V7TitleView_gyoValue2);
        gyoValue3 = (V7TitleView) ViewScaling.findViewByIdAndScale(gyoArea, R.id.DetectionGroup_V7TitleView_gyoValue3);
    }

    public void setDetectionValue(DetectionValue detectionValue){
        M1.setText(String.format("%.2f", detectionValue.slopeValue));
        C1.setText(String.format("%.2f", detectionValue.interceptValue));

        String p11Value = detectionValue.topX + "\n" + detectionValue.topY;
        String p12Value = detectionValue.bottomX + "\n" + detectionValue.bottomY;

        P11.setText(p11Value);
        P12.setText(p12Value);

        M2.setText(String.format("%.2f", detectionValue.defaultSlope));
        C2.setText(String.format("%.2f", detectionValue.defaultIntercept));

        String p21Value = detectionValue.defaultTopX + "\n" + detectionValue.defaultTopY;
        String p22Value = detectionValue.defaultBottomX + "\n" + detectionValue.defaultBottomY;

        P21.setText(p21Value);
        P22.setText(p22Value);
    }

    public void isShowAccelerator(boolean isAccelerator){
        if(isAccelerator){
            accArea.setVisibility(View.VISIBLE);
            gyoArea.setVisibility(View.GONE);
        }
        else{
            accArea.setVisibility(View.GONE);
            gyoArea.setVisibility(View.VISIBLE);
        }
    }

    public void setM1TextColor(int color){
        M1.setTextColor(color);
    }

    public void setSensorValue(float[] value){
        if(accArea.getVisibility() == View.VISIBLE){
            accValue1.setText(String.format("%.3f", value[0]));
            accValue2.setText(String.format("%.3f", value[1]));
            accValue3.setText(String.format("%.3f", value[2]));
        }
        else{
            gyoValue1.setText(String.format("%.3f", value[0]));
            gyoValue2.setText(String.format("%.3f", value[1]));
            gyoValue3.setText(String.format("%.3f", value[2]));
        }
    }
}
