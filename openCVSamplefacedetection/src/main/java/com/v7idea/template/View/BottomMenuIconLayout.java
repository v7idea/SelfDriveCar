package com.v7idea.template.View;

import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.v7idea.tool.ViewScaling;

import org.opencv.samples.facedetect.R;

/**
 * 按鈕物件
 *
 * @author 祐鑫
 */
public class BottomMenuIconLayout extends RelativeLayout {
    private final String tag = "BottomMenuIconLayout";

    //物件
    private Context thisContext;

    //view
    private BottomMenuIconLayout thisObject = null;
    private RelativeLayout thisLayout;
    private RelativeLayout clickArea = null;
    private ImageView thisImage;
    private TextView thisText;

    private final int defaultLayoutHeight = 100;
//	private final int defaultImageWidth = 112;    //在Layout中的圖片原始寛
//	private final int defaultImageHeight = 114;   //在Layout中的圖片原始高

    private int itemWidth = 0;
    private float scaleValue = 0.0f;

    private TextView ShowNoticeCount = null;

    /**
     * @param context
     * @param width
     */
    public BottomMenuIconLayout(Context context, int width) {
        super(context);

        thisContext = context;
        scaleValue = ViewScaling.getScaleValue();

        itemWidth = width;

        thisObject = this;

        Log.e(tag, "itemWidth: " + itemWidth);

        setLayout();
    }

    /**
     * 設定Layout
     */
    private void setLayout() {
        //RelativeLayout
        thisLayout = (RelativeLayout) ((Activity) thisContext).getLayoutInflater().inflate(R.layout.bottommenuiconlayout, null);
        LinearLayout.LayoutParams thisLayoutUse = (LinearLayout.LayoutParams) thisLayout.getLayoutParams();

//		thisLayout.setBackgroundColor(Color.GREEN);

        if (thisLayoutUse == null) {
            thisLayoutUse = new LinearLayout.LayoutParams(itemWidth, (int) (defaultLayoutHeight * scaleValue));
        } else {
            thisLayoutUse.width = itemWidth;
            thisLayoutUse.height = (int) (defaultLayoutHeight * scaleValue);
        }

        thisLayoutUse.leftMargin = 0;
        thisLayoutUse.topMargin = 0;

        //ImageView
        thisImage = (ImageView)ViewScaling.findViewByIdAndScale(thisLayout, R.id.bottommenuiconlayout_itemIcon);

        //TextView
        thisText = (TextView) ViewScaling.findViewByIdAndScale(thisLayout,R.id.bottommenuiconlayout_itemText);

        ShowNoticeCount = (TextView) ViewScaling.findViewByIdAndScale(thisLayout, R.id.bottommenuiconlayout_NoticeCount);

        clickArea = (RelativeLayout) ViewScaling.findViewByIdAndScale(thisLayout, R.id.bottommenuiconlayout_iconTouch);
        thisObject.addView(thisLayout, thisLayoutUse);
    }

    public void setIconClickListener(OnClickListener onClick) {
        clickArea.setOnClickListener(onClick);
    }

    public void setIconId(int id) {
        clickArea.setId(id);

        switch (id) {
            case 1000:
                thisText.setText("CH1");

                break;

            case 1001:
                thisText.setText("CH2");

                break;

            case 1002:
                thisText.setText("CH3");

                break;

            case 1003:
                thisText.setText("CH4");

                break;

            case 1004:
                thisText.setText("CH5");

                break;

            case 1005:
                thisText.setText("CH6");

                break;
        }
    }

    public void setNoticeCount(int intCount)
    {
        Log.e(tag, "intCount: "+intCount);

        if(ShowNoticeCount != null)
        {
            if(intCount <= 0)
            {
                ShowNoticeCount.setVisibility(View.GONE);
            }
            else
            {
                ShowNoticeCount.setVisibility(View.VISIBLE);
                ShowNoticeCount.setText("" + intCount);
            }
        }
    }

    public void setIconBackground(int intResourceID) {
        if (thisLayout != null) {
            thisLayout.setBackgroundResource(intResourceID);
        }
    }

    public void setReleaseIconBackground() {
        if (thisLayout != null) {
            Drawable background = thisLayout.getBackground();

            if (background != null) {
                background.setCallback(null);
                background = null;
            }

            thisLayout.setBackground(null);
        }
    }

    public String getIconText() {
        return thisText.getText().toString();
    }

    public int getIconId() {
        return clickArea.getId();
    }

    public void setTextColor(int color) {
        thisText.setTextColor(color);
    }

    public void release() {
        if (clickArea != null) {
            clickArea.removeAllViews();
            clickArea.setBackgroundDrawable(null);
        }

        if (thisLayout != null) {
            thisLayout.removeAllViews();
            thisLayout.setBackgroundDrawable(null);
        }

        if (thisImage != null) thisImage.setImageDrawable(null);
        if (thisObject != null) thisObject = null;

        if (thisText != null) {
            thisText.setText("");
            thisText.setBackgroundDrawable(null);
        }

        thisLayout = null;
        clickArea = null;
        thisImage = null;
        thisText = null;

        thisObject = null;
    }

    @Override
    public void setSelected(boolean selected) {
        super.setSelected(selected);

        if(selected)
        {
            clickArea.setBackgroundResource(R.mipmap.bottom_select);
        }
        else
        {
            clickArea.setBackground(null);
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        release();
    }

    public int getItemWidth() {
        return itemWidth;
    }

    public void setItemWidth(int itemWidth) {
        this.itemWidth = itemWidth;
    }
}
