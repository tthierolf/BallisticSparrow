package blindmanballistics.ballisticsparrow;

import android.bluetooth.*;
import android.content.Context;
import android.os.Handler;

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
    private Context mContext;
    private final char STX = 0x02;
    private final char ETX = 0x03;
    private Thread workerThread;
    private byte[] readBuffer;
    private int readBufferPosition;
    private volatile boolean stopWorker;
    private List<MessageReceiver> mReceivers;

    public CommunicationManager(Context c){
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
        for(BluetoothDevice device : mBluetoothManager.getAdapter().getBondedDevices()){
            if(device.getName().equalsIgnoreCase("blindmanballistics")){
                mDevice = device;
                break;
            }
        }
        if(mDevice != null) {
            try {
                mSocket = mDevice.createRfcommSocketToServiceRecord(mDevice.getUuids()[0].getUuid());
                mSocket.connect();
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
                                if(b == ETX)
                                {
                                    byte[] encodedBytes = new byte[readBufferPosition];
                                    System.arraycopy(readBuffer, 0, encodedBytes, 0, encodedBytes.length);
                                    final String data = new String(encodedBytes, "US-ASCII");
                                    readBufferPosition = 0;

                                    handler.post(new Runnable()
                                    {
                                        public void run()
                                        {
                                            for(MessageReceiver rec : mReceivers){
                                                rec.receiveMessage(data.substring(3, data.length()-1));
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
        sendCommand("RL");
    }

    public void rotateRight(){
        sendCommand("RR");
    }

    public void stopRotation(){
        sendCommand("RS");
    }

    public void incrementRotateRight(){
        sendCommand("IRR");
    }

    public void incrementRotateLeft(){
        sendCommand("IRL");
    }

    public void raiseBarrel(){
        sendCommand("BU");
    }

    public void lowerBarrel(){
        sendCommand("BD");
    }

    public void stopBarrel(){
        sendCommand("BS");
    }

    public void incrementRaiseBarrel(){
        sendCommand("IBU");
    }

    public void incrementLowerBarrel(){
        sendCommand("IBD");
    }

    public void fire(int power){
        sendCommand("FG(" + power + ")");
    }

    public void reload(){
        sendCommand("RG");
    }

    public void queryPower(){
        sendCommand("RV");
    }
}
