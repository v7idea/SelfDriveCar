package com.v7idea.tool;

import android.view.View;

/**
 * Created by mortal on 2017/3/16.
 */

public class ButtonData implements IButtonData
{
    private String buttonText = null;
    private int imageResourceID = -1;
    private View.OnClickListener onClickListener = null;

    public ButtonData(String buttonText, int imageResourceID, View.OnClickListener onClickListener)
    {
        this.buttonText = buttonText;
        this.imageResourceID = imageResourceID;
        this.onClickListener = onClickListener;
    }

    @Override
    public String getButtonText() {
        return buttonText;
    }

    @Override
    public int getButtonImageResourceID() {
        return imageResourceID;
    }

    @Override
    public View.OnClickListener getOnClickListener() {
        return onClickListener;
    }
}
