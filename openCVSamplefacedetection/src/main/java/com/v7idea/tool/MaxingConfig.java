package com.v7idea.tool;

/**
 * Created by mortal on 2017/3/15.
 */

public class MaxingConfig
{
    private String[] mChannel = null;

    public MaxingConfig(int TotalChannel)
    {
        mChannel = new String[TotalChannel];
    }

    public MaxingConfig(int TotalChannel, String Ch1, String Ch2, String Ch3, String Ch4, String Ch5, String Ch6, String Ch7, String Ch8)
    {
        mChannel = new String[TotalChannel];
        mChannel[0] = Ch1;
        mChannel[1] = Ch2;
        mChannel[2] = Ch3;
        mChannel[3] = Ch4;
        mChannel[4] = Ch5;
        mChannel[5] = Ch6;
        mChannel[6] = Ch7;
        mChannel[7] = Ch8;
    }

    public static MaxingConfig getInstance(String[] maxingConfigArray)
    {
        if(maxingConfigArray != null && maxingConfigArray.length == 8)
        {
            return new MaxingConfig(maxingConfigArray.length
                    , maxingConfigArray[0]
                    , maxingConfigArray[1]
                    , maxingConfigArray[2]
                    , maxingConfigArray[3]
                    , maxingConfigArray[4]
                    , maxingConfigArray[5]
                    , maxingConfigArray[6]
                    , maxingConfigArray[7]);
        }

        return null;
    }

    public void setChannelData(int index,String channelName)
    {
        mChannel[index] = channelName;
    }

    public String getChannelData(int index)
    {
        return mChannel[index];
    }
}
