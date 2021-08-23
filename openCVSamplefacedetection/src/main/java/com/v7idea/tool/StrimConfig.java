package com.v7idea.tool;

/**
 * Created by mortal on 2017/3/15.
 */

public class StrimConfig
{
    private String ChannelName;
    private String Upper;
    private String Lower;
    private String Middle;
    private String failSafe;

    public StrimConfig(String ChannelName, String Upper, String Lower, String Middle, String failSafe)
    {
        this.ChannelName = ChannelName;
        this.Upper = Upper;
        this.Lower = Lower;
        this.Middle = Middle;
        this.failSafe = failSafe;
    }

    public String getChannelName() {
        return ChannelName;
    }

    public void setChannelName(String channelName) {
        ChannelName = channelName;
    }

    public String getUpper() {
        return Upper;
    }

    public void setUpper(String upper) {
        Upper = upper;
    }

    public String getLower() {
        return Lower;
    }

    public void setLower(String lower) {
        Lower = lower;
    }

    public String getMiddle() {
        return Middle;
    }

    public void setMiddle(String middle) {
        Middle = middle;
    }

    public String getFailSafe() {
        return failSafe;
    }

    public void setFailSafe(String thisData) {
        this.failSafe = thisData;
    }
}
