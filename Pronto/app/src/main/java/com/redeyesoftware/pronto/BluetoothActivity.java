package com.redeyesoftware.pronto;

/**
 * Created by George on 20/11/2016.
 */

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.EditText;
import android.widget.Button;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Set;
import java.util.UUID;

public class BluetoothActivity extends AppCompatActivity {

    private static BluetoothActivity me;

    TextView myLabel;
    EditText myTextbox;
    BluetoothAdapter mBluetoothAdapter;
    BluetoothSocket mmSocket;
    BluetoothDevice mmDevice;
    OutputStream mmOutputStream;
    InputStream mmInputStream;
    Thread workerThread;
    byte[] readBuffer;
    int readBufferPosition;
    int counter;
    volatile boolean stopWorker;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bluetooth);

        me = this;

        try {
            findBT();
            openBT();
        } catch (IOException ex) {
            ex.printStackTrace();
        }

    }

    private void refresh() {
        SharedPreferences prefs = getSharedPreferences("PrefsFile", MODE_PRIVATE);
        String token = prefs.getString("accessToken", "ERROR: DID NOT READ");
        NetworkingUtility.getComments("/inbox/main/", token, 30, 20, "1150546131643551", "fillTiva", new String[]{
                "author_id", "msg_id", "text", "timestamp", "likes", "bookmarks"
        });
    }

    public static void sendCommentsToTiva() {
        try {
            for (int i = 0; i < NetworkingUtility.comments.length; i++) {
                String time = TimeStampConverter.getDate(Long.parseLong(NetworkingUtility.comments[i][3]));
                boolean iLiked = NetworkingUtility.comments[i][4].indexOf(LoginActivity.getId()) != -1;
                boolean iBookmarked = NetworkingUtility.comments[i][5].indexOf(LoginActivity.getId()) != -1;
                int numLikes = NetworkingUtility.comments[i][4].length() - NetworkingUtility.comments[i][4].replace(",", "").length();
                int numBookmarks = NetworkingUtility.comments[i][5].length() - NetworkingUtility.comments[i][5].replace(",", "").length();
                if (numLikes > 0) numLikes++;
                if (numBookmarks > 0) numBookmarks++;
                if (numLikes == 0 && NetworkingUtility.comments[i][4].length() > 4) numLikes = 1;
                if (numBookmarks == 0 && NetworkingUtility.comments[i][5].length() > 4)
                    numBookmarks = 1;
                //Comment cmt = new Comment(me.parentAcivity, NetworkingUtility.comments[i][1], NetworkingUtility.comments[i][2], NetworkingUtility.comments[i][0], time, numLikes, iLiked, numBookmarks, iBookmarked, false);
                me.sendData(NetworkingUtility.comments[i][2]);
            }
        } catch (Exception ex) {
            me.showMessage("Bluetooth Connection Failed");
        }
    }

    void findBT() {
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if(mBluetoothAdapter == null) {
            showMessage("This device does not support Bluetooth");
        }

        if(!mBluetoothAdapter.isEnabled()) {
            Intent enableBluetooth = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBluetooth, 0);
        }

        Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();
        if(pairedDevices.size() > 0) {
            for(BluetoothDevice device : pairedDevices) {
                if(device.getName().equals("HC-05")) {
                    mmDevice = device;
                    showMessage("Pronto Receiver Found");
                    break;
                }
            }
        }

    }

    void openBT() throws IOException {
        UUID uuid = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb"); //Standard //SerialPortService ID
        mmSocket = mmDevice.createRfcommSocketToServiceRecord(uuid);
        mmSocket.connect();
        mmOutputStream = mmSocket.getOutputStream();
        mmInputStream = mmSocket.getInputStream();
        beginListenForData();
        refresh();
        showMessage("Bluetooth Connection Opened");
    }

    void beginListenForData() {
        final Handler handler = new Handler();
        final byte delimiter = 10; //This is the ASCII code for a newline character

        stopWorker = false;
        readBufferPosition = 0;
        readBuffer = new byte[1024];
        workerThread = new Thread(new Runnable() {
            public void run() {
                while(!Thread.currentThread().isInterrupted() && !stopWorker) {
                    try {
                        int bytesAvailable = mmInputStream.available();
                        if(bytesAvailable > 0) {
                            byte[] packetBytes = new byte[bytesAvailable];
                            mmInputStream.read(packetBytes);
                            for(int i=0;i<bytesAvailable;i++) {
                                byte b = packetBytes[i];
                                if(b == delimiter) {
                                    byte[] encodedBytes = new byte[readBufferPosition];
                                    System.arraycopy(readBuffer, 0, encodedBytes, 0, encodedBytes.length);
                                    final String data = new String(encodedBytes, "US-ASCII");
                                    readBufferPosition = 0;

                                    handler.post(new Runnable() {
                                        public void run() {
                                            showMessage(data);
                                        }
                                    });
                                }
                                else {
                                    readBuffer[readBufferPosition++] = b;
                                }
                            }
                        }
                    }
                    catch (IOException ex) {
                        stopWorker = true;
                    }
                }
            }
        });

        workerThread.start();
    }

    void sendData(String msg) throws Exception {
        msg += "\n";
        mmOutputStream.write(msg.getBytes());
    }

    void closeBT() throws IOException {
        stopWorker = true;
        mmOutputStream.close();
        mmInputStream.close();
        mmSocket.close();
    }

    private void showMessage(String theMsg) {
        Toast msg = Toast.makeText(getBaseContext(), theMsg, (Toast.LENGTH_SHORT));
        msg.show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            sendData("CMD:Finished");
        } catch (Exception ex) {
            me.showMessage("Bluetooth Connection Failed");
        }
        try {
            closeBT();
        }
        catch (IOException ex) { }
    }
}