package com.v7idea.tool;

import android.os.Handler;
import android.os.Message;
import android.util.Log;

import org.json.JSONObject;
import org.opencv.samples.facedetect.Command;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;

/**
 * Created by mortal on 2017/8/17.
 */

public class Commissioner
{
    private final static String TAG = "Commissioner";
    private final static byte[] SOI_MARKER = { (byte) 0xFF, (byte) 0xD8 };
    private final static byte[] EOF_MARKER = { (byte) 0xFF, (byte) 0xD9 };

    public final static int GET_MESSAGE = 0;

    private String ipAddress = "";
    private int port = 5050;

    private DatagramSocket socket = null;
    private DatagramPacket socketPacket = null;
    private ByteArrayOutputStream buffer = null;
    private byte[] receiver = null;

    private Thread sendMessageThread = null;
    private Thread receiveMessage = null;

    public boolean isStartSend = false;
    public boolean isStartReceive = false;

    private boolean isConnectedDevice = false;

    private Handler handler = null;

    private String command = null;

    private Thread sendVideoDataThread = null;

    public boolean isStartSendVideo = false;

    public Commissioner()
    {

        this.receiver = new byte[1028];
        this.socketPacket = new DatagramPacket(receiver,receiver.length);

        this.buffer = new ByteArrayOutputStream();
    }

    public void init(){
        try {

            this.socket = new DatagramSocket(Constants.DEFAULT_PORT);
            this.socket.setReceiveBufferSize(207000);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void release(){
        if(this.socket != null){
            this.socket.close();
            this.socket = null;
        }
    }

    public void setConnectedDevice(boolean isConnectedDevice)
    {
        this.isConnectedDevice = isConnectedDevice;
    }

    private void sendMessage(String message){

        String sendData = "@" + message + "@";

        byte[] data = sendData.getBytes();

        try {
            DatagramPacket packet = new DatagramPacket(data, data.length
                    , InetAddress.getByName(ipAddress), port);

            if(socket != null && socket.isClosed() == false){
                socket.send(packet);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void startSendVideo()
    {
        isStartSendVideo = true;

        if(this.sendVideoDataThread == null)
        {
            this.sendVideoDataThread = new Thread(sendCameraFrameThread);
        }

        this.sendVideoDataThread.start();
    }

    public void stopSendVideo()
    {
        isStartSendVideo = false;

        if(this.sendVideoDataThread != null)
        {
            this.sendVideoDataThread.interrupt();
        }

        sendVideoDataThread = null;
    }

    public void startSendMessage(Handler handler)
    {
        isStartSend = true;
        this.handler = handler;

        if(this.sendMessageThread == null)
        {
            this.sendMessageThread = new Thread(sendMessageRunnable);
        }

        this.sendMessageThread.start();
    }

    public void stopSendMessage()
    {
        isStartReceive = false;

        if(this.sendMessageThread != null)
        {
            this.sendMessageThread.interrupt();
        }

        sendMessageThread = null;
    }

    public void startReceiveClientMessage(Handler handler)
    {
        isStartReceive = true;
        this.handler = handler;

        if(this.receiveMessage == null)
        {
            this.receiveMessage = new Thread(receiveData);
        }

        this.receiveMessage.start();
    }

    public void stopReceiveClientMessage()
    {
        isStartReceive = false;

        if(this.receiveMessage != null)
        {
            this.receiveMessage.interrupt();
            this.receiveMessage = null;
        }
    }

    public JSONObject detectionParameter = null;

    private Runnable sendMessageRunnable = new Runnable() {
        @Override
        public void run() {
            while (isStartSend)
            {
                if(ipAddress != null && ipAddress.isEmpty() == false)
                {
                    command = Command.isConnectedDevice(isConnectedDevice) + ";" + Command.isConnectedApp(true);

                    if(detectionParameter != null){
                        command += ";responseDetectionParameter:" + detectionParameter.toString();
                    }

                    sendMessage(command);
                }

                try {
                    sendMessageThread.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    };

    private Runnable receiveData = new Runnable() {
        @Override
        public void run() {

            while (isStartReceive)
            {
                try {

                    byte[] receiveCommandBuffer = new byte[2600];
                    DatagramPacket receiveCommandPacket = new DatagramPacket(receiveCommandBuffer, receiveCommandBuffer.length);

                    socket.receive(receiveCommandPacket);

                    byte[] dataArray = receiveCommandPacket.getData();

                    String str = new String(dataArray, "UTF-8");

                    int startIndex = str.indexOf("@");
                    int endIndex = str.lastIndexOf("@");

                    if(startIndex == 0)
                    {
                        startIndex = 1;
                    }

                    Log.d(TAG, "str: "+str);
                    Log.d(TAG, "str length: "+str.length());

                    Log.e(TAG, "startIndex: "+startIndex);
                    Log.e(TAG, "endIndex: "+endIndex);

                    String showMessage = str.substring(1, endIndex);

                    ipAddress = receiveCommandPacket.getAddress().getHostAddress();
                    port = receiveCommandPacket.getPort();

//                    Log.e(TAG, "receive ip: "+ipAddress);
//                    Log.e(TAG, "receive port: "+port);

                    if(handler != null)
                    {
                        Message message = new Message();
                        message.what = GET_MESSAGE;
                        message.obj = showMessage;

                        handler.sendMessage(message);
                    }

                    receiveCommandBuffer = null;
                    receiveCommandPacket = null;

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            stopReceiveClientMessage();
        }
    };

    private CVCameraWrapper.CameraFrame cameraFrame = null;

    private void sendData(byte[] data, String inpAddress, int port)
    {
        try {
            DatagramPacket packet = new DatagramPacket(data, data.length
                    , InetAddress.getByName(inpAddress), port);

            socket.send(packet);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void setCurrentCameraFrame(CVCameraWrapper.CameraFrame cameraFrame)
    {
        this.cameraFrame = cameraFrame;
    }

    private Runnable sendCameraFrameThread = new Runnable()
    {
        @Override
        public void run() {

            DatagramSocket streamSocket = null;

            try {
                streamSocket = new DatagramSocket(Constants.DEFAULT_VIDEO_PORT);
            } catch (SocketException e) {
                e.printStackTrace();
                Log.e(TAG, "DatagramSocket error: "+e.getMessage());
            }

            while (isStartSendVideo)
            {
                if(cameraFrame != null)
                {
                    if(ipAddress != null && ipAddress.isEmpty() == false)
                    {
                        cameraFrame.prepareSendData(Constants.BUFFER_SIZE);

                        byte[] imageDataArray = cameraFrame.dataArray;
                        int frameId = cameraFrame.imageIdentify;
                        long frameCreateTime = cameraFrame.createTime;

                        if(imageDataArray != null)
                        {
                            Log.e(TAG, "imageDataArray length: "+imageDataArray.length);

                            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream(Constants.BUFFER_SIZE);
                            byteArrayOutputStream.write(imageDataArray, 0, imageDataArray.length);

                            try {
                                byteArrayOutputStream.flush();

                                ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(byteArrayOutputStream.toByteArray());

                                long sendStartTime = System.currentTimeMillis();

                                int readIndex = 0;

                                int sendTotalCount = 0;

                                if(imageDataArray.length % Constants.BUFFER_SIZE != 0)
                                {
                                    sendTotalCount = (imageDataArray.length / Constants.BUFFER_SIZE) + 1;
                                }

                                byte idByte = (byte) frameId;

//                                Log.e(TAG, "sendTotalCount: "+sendTotalCount);

                                for(int i = 0 ; i < sendTotalCount ; i++)
                                {
                                    int serialNumber = i;
                                    byte[] sendDataBuffer = new byte[Constants.SEND_DATA_BUFFER_SIZE];

                                    readIndex = byteArrayInputStream.read(sendDataBuffer, 0, Constants.BUFFER_SIZE);

                                    sendDataBuffer[1024] = idByte;

                                    if(serialNumber == 256)
                                    {
                                        serialNumber = 0;
                                    }

                                    sendDataBuffer[1025] = (byte)serialNumber;


                                    try {
                                        DatagramPacket packet = new DatagramPacket(sendDataBuffer, sendDataBuffer.length
                                                , InetAddress.getByName(ipAddress), Constants.DEFAULT_VIDEO_PORT);
                                        streamSocket.send(packet);
//                                        Log.e(TAG, "DatagramPacket send frame success");
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                        Log.e(TAG, "DatagramPacket error: "+e.getMessage());
                                    }

//                                    sendData(sendDataBuffer, ipAddress, port);

                                    sendDataBuffer = null;
                                }

                                long sendEndTime = System.currentTimeMillis();

                                long totalTime = sendEndTime - sendStartTime;

                                Log.e(TAG, "total send time: "+totalTime);

//                                if(commissioner != null)
//                                {
//                                    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
//
//                                    Message message = new Message();
//                                    message.what = Commissioner.GET_TIME_INFO;
//
//                                    Bundle timeInfo = new Bundle();
//                                    timeInfo.putInt("imageLenght", imageDataArray.length);
//                                    timeInfo.putLong("imageCreateTime", frameCreateTime);
//                                    timeInfo.putLong("totalSendTime", totalTime);
//                                    timeInfo.putString("imageCreateTimeString", dateFormat.format(new Date(frameCreateTime)));
//
//                                    message.setData(timeInfo);
//
//                                    commissioner.handler.sendMessage(message);
//                                }

//                                targetIp = null;
//                                targetPort = -1000;

                                byteArrayOutputStream.reset();
                                byteArrayInputStream.reset();

                                byteArrayInputStream.close();
                                byteArrayOutputStream.close();

                                byteArrayOutputStream = null;
                                byteArrayInputStream = null;

                                try {
                                    sendVideoDataThread.sleep(30);
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }


                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }
            }
        }
    };

    public String getPhoneIpAddresss()
    {
        String sAddr = "";

        try
        {
            for (Enumeration<NetworkInterface> enumInterfaces = NetworkInterface.getNetworkInterfaces(); enumInterfaces.hasMoreElements(); )
            {
                // Get next network interface
                NetworkInterface networkInterface = enumInterfaces.nextElement();

                for (Enumeration<InetAddress> enumIPAddrs = networkInterface.getInetAddresses(); enumIPAddrs.hasMoreElements(); )
                {
                    // Get next IP address of this interface
                    InetAddress inetAddr = enumIPAddrs.nextElement();

                    // Exclude loopback address
                    if (!inetAddr.isLoopbackAddress())
                    {
                        if (sAddr != "")
                        {
                            sAddr += ",";
                        }
                        sAddr += "(" + networkInterface.getDisplayName() + ")" + inetAddr.getHostAddress().toString();
                    }
                }
            }
        }
        catch (SocketException e)
        {
            e.printStackTrace();
        }

        return sAddr;
    }
}
