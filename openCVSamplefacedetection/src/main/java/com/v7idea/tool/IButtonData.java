package com.v7idea.tool;

import android.view.View;

/**
 * Created by mortal on 2017/3/16.
 */

public interface IButtonData
{
    public String getButtonText();
    public int getButtonImageResourceID();
    public View.OnClickListener  getOnClickListener();
}
