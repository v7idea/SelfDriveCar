package com.v7idea.template.View;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.widget.ImageView;

/**
 * Created by mortal on 2017/9/25.
 */

public class AutoReleaseImageView extends ImageView {

    private boolean isAutoReleaseImage = true;

    public void setAutoReleaseImage(boolean isAutoReleaseImage)
    {
        this.isAutoReleaseImage = isAutoReleaseImage;
    }

    public AutoReleaseImageView(Context context) {
        super(context);
    }

    public AutoReleaseImageView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public AutoReleaseImageView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public AutoReleaseImageView(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    public void release() {
        releaseForegroundDrawable();

        Drawable backgroundDrawable = this.getBackground();

        if (backgroundDrawable != null) {
            backgroundDrawable.setCallback(null);
            backgroundDrawable = null;
        }

        this.setBackgroundDrawable(null);
    }

    public void releaseForegroundDrawable()
    {
        Drawable getDrawable = this.getDrawable();

        if (getDrawable != null) {
            getDrawable.setCallback(null);
            getDrawable = null;
        }

        this.setImageDrawable(null);
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if(isAutoReleaseImage)
        {
            release();
        }
    }
}
