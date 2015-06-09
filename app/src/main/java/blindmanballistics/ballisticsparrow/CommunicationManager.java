package blindmanballistics.ballisticsparrow;

import android.app.Activity;
import android.bluetooth.*;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Handler;
import android.util.Log;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Tyler on 2/21/2015.
 */
public class CommunicationManager {

    private BluetoothManager mBluetoothManager;
    private BluetoothDevice mDevice;
    private BluetoothSocket mSocket;
    private OutputStream mOutputStream;
    private InputStream mInputStream;
    private Activity mContext;
    private final char STX = 0x02;
    private final char ETX = 0x03;
    private Thread workerThread;
    private byte[] readBuffer;
    private int readBufferPosition;
    private volatile boolean stopWorker;
    private List<MessageReceiver> mReceivers;

    public CommunicationManager(Activity c){
        mContext = c;
        mBluetoothManager = (BluetoothManager) c.getSystemService(c.BLUETOOTH_SERVICE);
        mReceivers = new ArrayList<MessageReceiver>();
    }

    public void registerReceiver(MessageReceiver receiver){
        if(!mReceivers.contains(receiver)){
            mReceivers.add(receiver);
        }
    }

    public boolean removeReceiver(MessageReceiver receiver){
        return mReceivers.remove(receiver);
    }

    public boolean connectToWeaponSystem(){
        mDevice = null;
        for(BluetoothDevice device : mBluetoothManager.getAdapter().getBondedDevices()){
            if(device.getName().equalsIgnoreCase("blindmanballistics")){
                mDevice = device;
                break;
            }
        }
        if(mDevice != null) {
            try {
                if (mSocket != null){
                    mSocket.close();
                }
                mSocket = mDevice.createRfcommSocketToServiceRecord(mDevice.getUuids()[0].getUuid());
                mSocket.connect();
                Log.d("Bluetooth", "Socket connected to " + mSocket.getRemoteDevice());
                mOutputStream = mSocket.getOutputStream();
                mInputStream = mSocket.getInputStream();
                beginListenForData();
            }
            catch (IOException e) {
                return false;
            }
        }
        return true;
    }

    public void shutdown(){
        try {
            stopWorker = true;
            if(workerThread != null) {
                workerThread.interrupt();
            }
            if(mSocket.isConnected()) {
                mSocket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void sendCommand(String command){
        if(mSocket != null && mSocket.isConnected()){
            try {
                StringBuffer msg = new StringBuffer();
                msg.append(STX);
                msg.append(String.format("%02d", command.length()+1));
                msg.append(command);
                msg.append(ETX);
                Log.d("Bluetooth", "Sent message: " + msg);
                byte[] data = msg.toString().getBytes();
                mOutputStream.write(data);
            }
            catch (IOException e) {

            }
        }
    }

    private void beginListenForData()
    {
        final Handler handler = new Handler();
        final byte delimiter = 10; //This is the ASCII code for a newline character

        stopWorker = false;
        readBufferPosition = 0;
        readBuffer = new byte[1024];
        workerThread = new Thread(new Runnable()
        {
            public void run()
            {
                while(!Thread.currentThread().isInterrupted() && !stopWorker)
                {
                    try
                    {
                        int bytesAvailable = mInputStream.available();
                        if(bytesAvailable > 0)
                        {
                            byte[] packetBytes = new byte[bytesAvailable];
                            mInputStream.read(packetBytes);
                            for(int i=0;i<bytesAvailable;i++)
                            {
                                byte b = packetBytes[i];
                                //Log.d("Bluetooth RX", ""+b);
                                if(b == ETX)
                                {
                                    byte[] encodedBytes = new byte[readBufferPosition];
                                    System.arraycopy(readBuffer, 0, encodedBytes, 0, encodedBytes.length);
                                    final String data = new String(encodedBytes, "US-ASCII");
                                    readBufferPosition = 0;
                                    Log.d("Bluetooth", "Received message: " + data);
                                    mContext.runOnUiThread(new Runnable() {
                                        public void run() {
                                            for(MessageReceiver rec : mReceivers){
                                                rec.receiveMessage(data.substring(3, data.length()));
                                            };
                                        }
                                    });
                                }
                                else
                                {
                                    readBuffer[readBufferPosition++] = b;
                                }
                            }
                        }
                    }
                    catch (IOException ex)
                    {
                        stopWorker = true;
                    }
                }
            }
        });

        workerThread.start();
    }

    public void rotateLeft(){
        sendCommand("RL(0)");
    }

    public void rotateRight(){
        sendCommand("RR(0)");
    }

    public void stopRotation(){
        sendCommand("RS(0)");
    }

    public void incrementRotateRight(){
        sendCommand("IRR(0)");
    }

    public void incrementRotateLeft(){
        sendCommand("IRL(0)");
    }

    public void raiseBarrel(){
        sendCommand("BU(0)");
    }

    public void lowerBarrel(){
        sendCommand("BD(0)");
    }

    public void stopBarrel(){
        sendCommand("BS(0)");
    }

    public void incrementRaiseBarrel(){
        sendCommand("IBU(0)");
    }

    public void incrementLowerBarrel(){
        sendCommand("IBD(0)");
    }

    public void fire(int power){
        sendCommand("FG(" + power + ")");
    }

    public void reload(){
        sendCommand("RG(0)");
    }

    public void queryPower(){
        sendCommand("RV(0)");
    }
}
