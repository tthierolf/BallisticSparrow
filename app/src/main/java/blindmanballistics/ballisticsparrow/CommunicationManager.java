package blindmanballistics.ballisticsparrow;

import android.bluetooth.*;
import android.content.Context;
import java.io.*;

/**
 * Created by Tyler on 2/21/2015.
 */
public class CommunicationManager {

    private BluetoothManager mBluetoothManager;
    private BluetoothDevice mDevice;
    private BluetoothSocket mSocket;
    private InputStream mInputStream;
    private Context mContext;

    public CommunicationManager(Context c){
        mContext = c;
        mBluetoothManager = (BluetoothManager) c.getSystemService(c.BLUETOOTH_SERVICE);
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
            }
            catch (IOException e) {
                return false;
            }
        }
        return true;
    }

    private void sendCommand(String command){
        if(mSocket.isConnected()){
            try {
                mSocket.getOutputStream().write(command.getBytes());
            }
            catch (IOException e) {

            }
        }
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
