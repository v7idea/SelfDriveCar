package com.v7idea.tool;

import java.io.Serializable;

/**
 * Created by mortal on 2017/3/15.
 */
public class ServoReverse implements Serializable
{
    private String[] sChannel = null;

    public ServoReverse(int TotalChannel)
    {
        sChannel = new String[TotalChannel];
    }

    public ServoReverse(int TotalChannel, String Ch1, String Ch2, String Ch3, String Ch4, String Ch5, String Ch6, String Ch7, String Ch8)
    {
        sChannel = new String[TotalChannel];
        sChannel[0] = Ch1;
        sChannel[1] = Ch2;
        sChannel[2] = Ch3;
        sChannel[3] = Ch4;
        sChannel[4] = Ch5;
        sChannel[5] = Ch6;
        sChannel[6] = Ch7;
        sChannel[7] = Ch8;
    }

    public static ServoReverse getInstance(String[] valueArray)
    {
        if(valueArray != null && valueArray.length == 8)
        {
            return new ServoReverse(valueArray.length
                    , valueArray[0]
                    , valueArray[1]
                    , valueArray[2]
                    , valueArray[3]
                    , valueArray[4]
                    , valueArray[5]
                    , valueArray[6]
                    , valueArray[7]);
        }

        return null;
    }

    public void setChannelData(int index,String channelName)
    {
        sChannel[index] = channelName;
    }

    public String getChannelData(int index)
    {
        return sChannel[index];
    }
}
