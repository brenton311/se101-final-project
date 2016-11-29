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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.util.ArrayList;
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
        NetworkingUtility.getComments("/inbox/feed/", token, 30, 20, "1150546131643551","", "fillTiva", new String[]{
                "author_id", "msg_id", "text", "likes", "bookmarks"
        });
    }

    public static void sendCommentsToTiva() {
        try {
            JSONArray jsonCommentArray = new JSONArray();
            for (int i = 0; i < NetworkingUtility.comments.length; i++) {
                jsonCommentArray.put(createJSONforComment(i));
            }
            me.sendData(jsonCommentArray.toString());
        } catch (Exception ex) {
            me.showMessage("Bluetooth Connection Failed");
        }
    }

    private static JSONObject createJSONforComment(int index) {
        boolean iLiked = NetworkingUtility.comments[index][3].indexOf(LoginActivity.getId()) != -1;
        boolean iBookmarked = NetworkingUtility.comments[index][4].indexOf(LoginActivity.getId()) != -1;
        JSONObject comment = new JSONObject();
        try {
            comment.put("author_id", NetworkingUtility.comments[index][0]);
            comment.put("msg_id", NetworkingUtility.comments[index][1]);
            comment.put("text", NetworkingUtility.comments[index][2]);
            comment.put("i_liked", iLiked);
            comment.put("i_bookmarked", iBookmarked);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return comment;
    }

    void findBT() {
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if(mBluetoothAdapter == null) {
            showMessage("This device does not support Bluetooth");
        }

        //Todo: fix the bug with bluetooth disabled
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

                                    Log.d("Debug:", "Message received from bluetooth: " + data);
                                    //note need to not include last chat when getting token because \n is included at end of data

                                    SharedPreferences prefs = getSharedPreferences("PrefsFile", MODE_PRIVATE);
                                    String token = prefs.getString("accessToken", "ERROR: DID NOT READ");
                                    if (data.substring(0,4).equals("LIKE")) {
                                        NetworkingUtility.post("/msg/like/", new String[]{"access_token","msg_id"}, new String[]{token,data.substring(5,data.length()-1)});
                                    } else if (data.substring(0,4).equals("BKMK")) {
                                        NetworkingUtility.getComments("/inbox/feed/", token, 1, 1, "1150546131643551",data.substring(5,data.length()-1), "updateBookmarkFromBluetooth", new String[]{
                                                "author_id", "msg_id", "text", "timestamp", "likes", "bookmarks", "attachments"
                                        });
                                        NetworkingUtility.post("/msg/bookmark/", new String[]{"access_token","msg_id"}, new String[]{token,data.substring(5,data.length()-1)});
                                    } else if (data.substring(0,4).equals("DISL")) {
                                        NetworkingUtility.post("/msg/dislike/", new String[]{"access_token", "msg_id"}, new String[]{token,data.substring(5,data.length()-1)});
                                    }

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
        //Toast msg = Toast.makeText(getBaseContext(), theMsg, (Toast.LENGTH_SHORT));
        //msg.show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        MainPage.updateFragments();
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

    public static void updateBookmarkFromBluetooth() {
        ArrayList<SerializableBookmark> bookmarkList = new ArrayList<SerializableBookmark>();

        File bookmarksFile = new File(me.getFilesDir().getPath().toString() + "/SavedProntoBookmarks.txt");
        if (bookmarksFile.exists()) {
            Log.d("Debug", "comment found file");
            try {
                FileInputStream fileIn = new FileInputStream(bookmarksFile);
                ObjectInputStream in = new ObjectInputStream(fileIn);
                bookmarkList = (ArrayList<SerializableBookmark>) in.readObject();
                //Log.i("palval", "dir.exists()");
                in.close();
                fileIn.close();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            Log.d("Debug", "comment did not find file");
            try {
                bookmarksFile.createNewFile(); // if file already exists will do nothing
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        String date = TimeStampConverter.getDate(Long.parseLong(NetworkingUtility.comments[0][3]));
        boolean iLiked = NetworkingUtility.comments[0][4].indexOf(LoginActivity.getId()) != -1;
        boolean iBookmarked = NetworkingUtility.comments[0][5].indexOf(LoginActivity.getId()) != -1;
        int numLikes = NetworkingUtility.comments[0][4].length() - NetworkingUtility.comments[0][4].replace(",", "").length();
        int numBookmarks =  NetworkingUtility.comments[0][5].length() - NetworkingUtility.comments[0][5].replace(",", "").length();
        if (numLikes>0) numLikes++;
        if (numBookmarks>0) numBookmarks++;
        if (numLikes==0 && NetworkingUtility.comments[0][4].length()>4) numLikes=1;
        if (numBookmarks==0 && NetworkingUtility.comments[0][5].length()>4) numBookmarks=1;

        if (iBookmarked) {//delete
            for (int i=0; i<bookmarkList.size();i++) {
                if(bookmarkList.get(i).getMessageID().equals(NetworkingUtility.comments[0][1])) {
                    bookmarkList.remove(i);
                }
            }
        } else {//create
            SerializableBookmark newBookmark = new SerializableBookmark(NetworkingUtility.comments[0][1], NetworkingUtility.comments[0][2], NetworkingUtility.comments[0][0], date, numLikes, iLiked, numBookmarks+1, NetworkingUtility.comments[0][6]);
            bookmarkList.add(newBookmark);
        }

        try {
            FileOutputStream fileOut = new FileOutputStream(bookmarksFile);
            ObjectOutputStream out = new ObjectOutputStream(fileOut);
            out.writeObject(bookmarkList);//this is only possible becuase SerializableBookmark implements Serializable
            out.close();
            fileOut.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        Log.d("Debug", "Bookmark successfully added/deleted");

    }
}