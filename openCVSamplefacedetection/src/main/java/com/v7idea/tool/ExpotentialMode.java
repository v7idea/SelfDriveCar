package com.v7idea.tool;

/**
 * Created by mortal on 2017/3/15.
 */

public class ExpotentialMode
{
    private String[] eChannel = null;

    public ExpotentialMode(int TotalChannel)
    {
        eChannel = new String[TotalChannel];
    }

    public ExpotentialMode(int TotalChannel, String Ch1, String Ch2, String Ch3, String Ch4, String Ch5, String Ch6, String Ch7, String Ch8)
    {
        eChannel = new String[TotalChannel];
        eChannel[0] = Ch1;
        eChannel[1] = Ch2;
        eChannel[2] = Ch3;
        eChannel[3] = Ch4;
        eChannel[4] = Ch5;
        eChannel[5] = Ch6;
        eChannel[6] = Ch7;
        eChannel[7] = Ch8;
    }

    public static ExpotentialMode getInstance(String[] expotentialModeArray)
    {
        if(expotentialModeArray != null && expotentialModeArray.length == 8)
        {
            return new ExpotentialMode(expotentialModeArray.length
                    , expotentialModeArray[0]
                    , expotentialModeArray[1]
                    , expotentialModeArray[2]
                    , expotentialModeArray[3]
                    , expotentialModeArray[4]
                    , expotentialModeArray[5]
                    , expotentialModeArray[6]
                    , expotentialModeArray[7]);
        }

        return null;
    }

    public void setChannelData(int index,String channelName)
    {
        eChannel[index] = channelName;
    }

    public String getChannelData(int index)
    {
        return eChannel[index];
    }
}
