package com.v7idea.template.View;

import android.app.ProgressDialog;
import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.v7idea.tool.ViewScaling;

import org.opencv.samples.facedetect.R;

/**
 * Created by mortal on 15/3/15.
 */
public class BannerUserBar extends RelativeLayout {
    private final String tag = "BannerUserBar";

    public BannerUserBar(Context context) {
        super(context);
        init();
    }

    public BannerUserBar(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public BannerUserBar(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private RelativeLayout contentLayout = null;
    private TextView showNoInternet = null;
    private SpecialImageView cancel = null;

    private void init() {
        LayoutInflater inflater = LayoutInflater.from(getContext());

        contentLayout = (RelativeLayout) inflater.inflate(R.layout.banner_user_status_bar, null);
        cancel = (SpecialImageView) contentLayout.findViewById(R.id.userStatusBar_SpecialImageView_cancel);
        showNoInternet = (TextView) contentLayout.findViewById(R.id.userStatusBar_TextView_CurrentNoInternet);
//        cancel.setOnClickListener(onLogoutPress);

        this.addView(contentLayout);
    }

    public void setScale() {
        ViewScaling.setScale(contentLayout, ViewGroup.LayoutParams.MATCH_PARENT, (int) (80 * ViewScaling.getScaleValue()));
        ViewScaling.setScaleByRelativeLayout(cancel);
        ViewScaling.setScaleTextSize(showNoInternet, ViewScaling.getScaleMin());
        showNoInternet.setPadding((int) (54 * ViewScaling.getScaleValue()), 0, (int) (54 * ViewScaling.getScaleValue()), 0);

        cancel.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                setVisibility(View.GONE);
            }
        });
    }

    private ProgressDialog waitingDialog = null;
    private boolean isLogout = false;
}
