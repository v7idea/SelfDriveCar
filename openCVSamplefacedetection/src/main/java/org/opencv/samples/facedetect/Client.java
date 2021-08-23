package org.opencv.samples.facedetect;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.ImageView;

import com.v7idea.tool.Constants;

import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;

public class Client
{
    public static final String DEFAULT_IP = "192.168.43.1";

    private final static String TAG = "Client";
    private final static int BUFFER_SIZE = 1026;
    public final static int GET_MESSAGE = 0;
    public final static int GET_IMAGE = 1;

    private String ipAddress = "";
    private int port = 5050;

    private DatagramSocket streamSocket = null;

    private DatagramSocket socket = null;
    private DatagramPacket socketPacket = null;
    private static ByteArrayOutputStream buffer = null;
    private byte[] receiver = null;

    private Thread receiveMessage = null;
    private Thread sendMessageThread = null;
    private Thread receiveStreamThread = null;

    public boolean isStartReceive = false;
    public boolean isStartSend = false;

    private boolean isActive = false;
    private boolean isRecord = false;
    private boolean isCheck = false;
    private boolean isRequestParameter = false;
    private boolean isStartStream = false;

    private boolean isLeftReferenceLine = false;
    private boolean isRightReferenceLine = false;

    private Handler handler = null;

    public ImageView showImage = null;

    private String command = null;

    public JSONObject currentDetectionParameter = null;

    public Client()
    {

        this.receiver = new byte[BUFFER_SIZE];
        this.socketPacket = new DatagramPacket(receiver,receiver.length);

        this.buffer = new ByteArrayOutputStream();
    }

    public void init(){
        try {

            this.socket = new DatagramSocket(Constants.DEFAULT_PORT);
            this.socket.setReceiveBufferSize(207000);

            this.streamSocket = new DatagramSocket(Constants.DEFAULT_VIDEO_PORT);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void release(){
        if(this.socket != null){
            this.socket.close();
            this.socket = null;
        }

        if(this.streamSocket != null){
            this.streamSocket.close();
            this.streamSocket = null;
        }
    }

    public void setIsActive(boolean isActive)
    {
        this.isActive = isActive;
    }

    public void setIsRecord(boolean isRecord){
        this.isRecord = isRecord;
    }

    public void setLeftReferenceLine(boolean isSet){
        this.isLeftReferenceLine = isSet;
    }

    public void setRightReferenceLine(boolean isSet){
        this.isRightReferenceLine = isSet;
    }

    public void setCheckConnectedStatus(boolean isCheck)
    {
        this.isCheck = isCheck;
    }

    public void setRequestParameter(boolean isRequestParameter)
    {
        this.isRequestParameter = isRequestParameter;
    }

    public void setStartStream(boolean isStartStream)
    {
        this.isStartStream = isStartStream;

        if(this.isStartStream)
        {
            if(receiveStreamThread == null)
            {
                receiveStreamThread = new Thread(receiveCameraFrameRunnable);
            }

            receiveStreamThread.start();
        }
        else
        {
            if(receiveStreamThread != null)
            {
                receiveStreamThread.interrupt();
            }

            receiveStreamThread = null;
        }
    }

    public void setIpAddress(String ipAddress)
    {
        this.ipAddress = ipAddress;
    }

    public void setPort(int port)
    {
        this.port = port;
    }

    public void setHandler(Handler handler)
    {
        this.handler = handler;
    }

    public void startSendMessage()
    {
        isStartSend = true;

        if(this.sendMessageThread == null)
        {
            this.sendMessageThread = new Thread(sendMessageRunnable);
        }

        this.sendMessageThread.start();
    }

    public void stopSendMessage()
    {
        isStartSend = false;

        if(this.sendMessageThread != null){
            this.sendMessageThread.interrupt();
        }

        this.sendMessageThread = null;
    }

    private void sendMessage(String message){

        String sendData = "@" + message + "@";

        byte[] data = sendData.getBytes();

        try {
            DatagramPacket packet = new DatagramPacket(data, data.length
                    , InetAddress.getByName(ipAddress), port);

            socket.send(packet);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void startReceiveClientMessage()
    {
        isStartReceive = true;

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
        }

        this.receiveMessage = null;
    }

    private Runnable sendMessageRunnable = new Runnable() {
        @Override
        public void run() {
            while (isStartSend)
            {
                if(ipAddress != null && ipAddress.isEmpty() == false)
                {
                    command = Command.isActive(isActive) + ";" + Command.checkConnectedState(isCheck)
                            + ";" + Command.requestDetectionParameter(isRequestParameter)
                            + ";" + Command.streamVideo(isStartStream)
                            + ";" + Command.isLeftReferentLine(isLeftReferenceLine)
                            + ";" + Command.isRightReferentLine(isRightReferenceLine)
                            + ";" + Command.isRecordVideo(isRecord);

                    if(currentDetectionParameter != null){
                        command = command +";" + "parameterSetting:" + currentDetectionParameter.toString();
                    }

                    Log.e(TAG, "command: "+command);

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

    private int badFrameNum = 0;
    private int packetNum = 0;
    private int frameCheckID;

    public static  byte[] SOI_MARKER = { (byte) 0xFF, (byte) 0xD8 };
    private static  byte[] EOF_MARKER = { (byte) 0xFF, (byte) 0xD9 };
    private int frameNum = 0;

    long startTime = 0l;

    public Bitmap receiveBitmap = null;

    private Runnable receiveCameraFrameRunnable = new Runnable() {
        @Override
        public void run() {

            int oldSerialNumber = -1;
            int checkID = 0;
            boolean ifDropFrame = false;

            long endTime = 0l;

            try {

                buffer = new ByteArrayOutputStream();

                while(isStartStream) {

                    streamSocket.receive(socketPacket);

                    int getReceiverPort = socketPacket.getPort();
//                    Log.e(TAG, "getReceiverPort: "+getReceiverPort);

                    checkID = (int)receiver[1024];
                    int serialNumber = (int)receiver[1025];

                    if((serialNumber - oldSerialNumber) != 1)
                    {
                        ifDropFrame = true;
                    }

                    if(serialNumber == 255) {
                        oldSerialNumber = -1;
                    } else {
                        oldSerialNumber = serialNumber;
                    }

                    int ffd9 = getByteIndex(receiver,EOF_MARKER,0);
                    int ffd8 = getByteIndex(receiver,SOI_MARKER,0);

//                    Log.d(TAG, "ffd8: "+ffd8);
//                    Log.d(TAG, "ffd9: "+ffd9);

                    if(ffd8 > -1)
                    {
                        Log.d(TAG, "ffd8: "+ffd8);

                        startTime = System.currentTimeMillis();
                    }

                    if(ffd9 > -1)
                    {
                        Log.d(TAG, "ffd9: "+ffd9);
                    }

                    if(ffd9 > 0 && buffer.size() > 0)
                    {
                        buffer.write(receiver, 0, ffd9 + 2);

                        if(ifDropFrame == false) {

                            buffer.flush();
                            // rc.sendMessage(handler.obtainMessage(1, buffer.toByteArray()));
                            if(showImage != null && buffer.size() > 0) {

                                oldSerialNumber = -1;

                                byte[] showImageArray = buffer.toByteArray();

                                Log.e(TAG, "showImageArray length : " + showImageArray.length);

                                if(showImage != null)
                                {
                                    receiveBitmap = BitmapFactory.decodeByteArray(showImageArray, 0, showImageArray.length);

                                    if(receiveBitmap != null)
                                    {
                                        endTime = System.currentTimeMillis();

                                        long totalTime = endTime - startTime;

                                        Log.e(TAG, "total millis : "+totalTime);

//                                        showImage.setImageBitmap(receiveImage);
                                        if(handler != null)
                                        {
                                            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");

                                            Message message = new Message();
                                            message.what = GET_IMAGE;

//                                            Bundle timeInfo = new Bundle();
//                                            timeInfo.putInt("imageLenght", showImageArray.length);
//                                            timeInfo.putLong("imageCreateTime", endTime);
//                                            timeInfo.putLong("totalSendTime", totalTime);
//                                            timeInfo.putString("imageCreateTimeString", dateFormat.format(new Date(endTime)));
//
//                                            message.setData(timeInfo);

                                            handler.sendMessage(message);
                                        }
                                    }
                                }


                                // Log.d("New image is created","size:" + buffer.toByteArray().length);
                                buffer.close();
                                buffer = new ByteArrayOutputStream();
                                frameNum++;

                            } else {


                                buffer.close();
                                buffer = new ByteArrayOutputStream();

                            }

                        } else {

                            oldSerialNumber = -1;

                            buffer.close();
                            buffer = new ByteArrayOutputStream();
                            badFrameNum++;
                        }

                        ifDropFrame = false;
                    }

                    if(ffd8 > 0 && ffd9 < ffd8) {

                        buffer.reset();
                        buffer.close();
                        buffer = new ByteArrayOutputStream();
                        buffer.write(receiver, ffd8, 1024 - ffd8);
                        // Log.d("find ffd8","start!");

                    } else {

                        if(ffd9 <= 0) {

                            buffer.write(receiver, 0, 1024);

                        }

                    }

//                    if(checkID != frameCheckID)
//                    {
//
//                    }
//                    else
//                    {
//                        buffer.close();
//                        buffer = null;
//                        buffer = new ByteArrayOutputStream();
//
//                        badFrameNum++;
//                    }

                    frameCheckID = checkID;
                }
            } catch (SocketException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    };

    private Runnable receiveData = new Runnable() {
        @Override
        public void run() {

            while (isStartReceive)
            {
                byte[] receiveCommandBuffer = new byte[2600];
                DatagramPacket receiveCommandPacket = new DatagramPacket(receiveCommandBuffer, receiveCommandBuffer.length);

                try
                {
                    if(receiveCommandPacket != null)
                    {
                        socket.receive(receiveCommandPacket);

                        Log.e(TAG, "get message !!");

                        byte[] dataArray = receiveCommandPacket.getData();

                        if(dataArray != null)
                        {
                            Log.e(TAG, "dataArray length is: " + dataArray.length);
                        }

                        String str = new String(dataArray, "UTF-8");

                        Log.e(TAG, "str: "+str);

                        int startIndex = str.indexOf("@");
                        int endIndex = str.lastIndexOf("@");

                        if(startIndex == 0)
                        {
                            startIndex = 1;
                        }

                        Log.e(TAG, "startIndex: "+startIndex);
                        Log.e(TAG, "endIndex: "+endIndex);

                        if(startIndex > -1 && endIndex > -1)
                        {
                            String showMessage = str.substring(1, endIndex);

                            if(handler != null)
                            {
                                Message message = new Message();
                                message.what = GET_MESSAGE;
                                message.obj = showMessage;

                                handler.sendMessage(message);
                            }
                        }
                    }

//                    // 如果不是空訊息
//                    String replyMessage = "client I get it !!";
//
//                    byte[] data = replyMessage.getBytes();
//
//                    DatagramPacket packet = new DatagramPacket(data, data.length
//                            , socketPacket.getAddress(), socketPacket.getPort());
//                    socket.send(packet);

                } catch (IOException e) {
                    e.printStackTrace();
                }

                receiveCommandBuffer = null;
                receiveCommandPacket = null;
            }

            stopReceiveClientMessage();
        }
    };


    public static boolean isConnectInternet(Context context) {
        boolean isConnect = false;

        ConnectivityManager getNetwordType = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo nowNetworkType = getNetwordType.getActiveNetworkInfo();

        if (nowNetworkType != null) {
            int type = nowNetworkType.getType();
            String typeName = nowNetworkType.getTypeName();
            int subtype = nowNetworkType.getSubtype();
            String subtypeName = nowNetworkType.getSubtypeName();

            Log.e(TAG, "type: "+type);
            Log.d(TAG, "typeName: "+typeName);
            Log.d(TAG, "subtype: "+subtype);
            Log.e(TAG, "subtypeName: "+subtypeName);

            isConnect = nowNetworkType.isConnectedOrConnecting();
        } else {
            Log.d(TAG, "not connect to internet !!");
        }

        nowNetworkType = null;
        getNetwordType = null;

        return isConnect;
    }

    /**
     *  Convenience method to convert a byte array to a hex string.
     *
     * @param  data  the byte[] to convert
     * @return String the converted byte[]
     */
    public static String bytesToHex(byte[] data) {
        StringBuffer buf = new StringBuffer();
        for (int i = 0; i < data.length; i++) {
            buf.append(byteToHex(data[i]).toUpperCase());
        }
        return (buf.toString());
    }

    /**
     *  method to convert a byte to a hex string.
     *
     * @param  data  the byte to convert
     * @return String the converted byte
     */
    public static String byteToHex(byte data) {
        StringBuffer buf = new StringBuffer();
        buf.append(toHexChar((data >>> 4) & 0x0F));
        buf.append(toHexChar(data & 0x0F));
        return buf.toString();
    }


    /**
     *  Convenience method to convert an int to a hex char.
     *
     * @param  i  the int to convert
     * @return char the converted char
     */
    public static char toHexChar(int i) {
        if ((0 <= i) && (i <= 9)) {
            return (char) ('0' + i);
        } else {
            return (char) ('a' + (i - 10));
        }
    }

    public static int getByteIndex(byte[] source, byte[] checkString, int offset) {

        int length = -1;
        int SequenceIndex = 0;
        boolean needToCheck = false;

        for(int i = offset; i < source.length; i ++) {

            if(source[i] == checkString[SequenceIndex]) {

                SequenceIndex = 0;
                i ++;
                needToCheck = true;
                SequenceIndex += 1;

                while (i < source.length && SequenceIndex < checkString.length && needToCheck == true) {

                    if(source[i] == checkString[SequenceIndex]) {

                        SequenceIndex ++;
                        i ++;

                    } else {

                        SequenceIndex = 0;
                        needToCheck = false;
                    }
                }

                // check if get to index

                if(SequenceIndex >= checkString.length) {

                    length = i - checkString.length;
                    i = source.length;
                }
            }
        }

//		Log.d("Compare Data","get Length = " + length);

        return length;
    }
}
