package com.v7idea.template.View;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.v7idea.tool.ViewScaling;

import org.opencv.samples.facedetect.R;

import java.text.DecimalFormat;

/**
 * Created by G50-3 on 2017/8/2.
 */

public class CustomView_SeekBar extends LinearLayout {
    public TextView showTitle, showValue;
    public SeekBar seekBar;

    public CustomView_SeekBar(Context context) {
        super(context);
        init();
    }

    public CustomView_SeekBar(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public CustomView_SeekBar(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init()
    {
        LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View contentView = inflater.inflate(R.layout.seekbar_view, null);

        addView(contentView);

        showTitle = (TextView) contentView.findViewById(R.id.CustomSeekbar_TextView_Text);
        seekBar = (SeekBar) contentView.findViewById(R.id.CustomSeekbar_SeekBar_Value);
        showValue = (TextView) contentView.findViewById(R.id.CustomSeekbar_TextView_Value);
    }

    public void scaleLayout()
    {
        ViewScaling.scaleLayout(showTitle);
        ViewScaling.scaleLayout(seekBar);
        ViewScaling.scaleLayout(showValue);
    }

    //設定開頭的字串
    public void setText(String Text) {
        showTitle.setText(Text);
    }

    //滑桿最大值
    public void setMax(int Text) {
        seekBar.setMax(Text);
    }

    //滑桿初始值(Int)
    public void Value() {

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                showValue.setText(Integer.toString(progress));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
    }

    //滑桿初始值(Int)，奇數
    public void OddlValue() {

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (progress == 0) {
                    showValue.setText(Integer.toString(progress));
                } else if ((progress) % 2 == 1) {
                    showValue.setText(Integer.toString(progress));
                } else if ((progress) % 2 == 0) {
                    showValue.setText(Integer.toString(progress - 1));
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
    }


    //初始值(Float)
    public void setFloatProgress(float Text) {
        seekBar.setProgress((int) Text);
        showValue.setText(Float.toString(Text));
    }


    //滑桿初始值(Float)，小數點第2位
    public void OneTenthFloatValue() {

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

                showValue.setText(Float.toString((float) (progress * 0.01)));

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
    }


    //初始值(Double)，小數點第1位
    public void setDoubleProgress(double Text) {
        DecimalFormat df = new DecimalFormat("0.0");
        String stringdf = df.format(Text);
        seekBar.setProgress((int) Text);
        showValue.setText(stringdf);
    }


    //滑桿初始值(Double)，小數點第1位
    public void DoubleValue() {

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                DecimalFormat df = new DecimalFormat("0.0");
                String stringdf = df.format((Double) (progress * 0.1));
                showValue.setText(stringdf);

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
    }

    //Rho滑桿初始值(Double)，小數點第1位
    public void RhoDoubleValue() {

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                DecimalFormat df = new DecimalFormat("0.0");
                String stringdf = df.format((Double) (progress * 1.0));
                showValue.setText(stringdf);

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
    }

    public String getValue() {
        return showValue.getText().toString();
    }
}
