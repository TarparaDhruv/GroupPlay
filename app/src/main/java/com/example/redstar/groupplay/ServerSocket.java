package com.example.redstar.groupplay;

/**
 * Created by Redstar on 07-Nov-17.
 */

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.util.ArrayList;


public class ServerSocket extends AsyncTask<Void,Void,Void> {

    private ArrayList<String> a;
    private Context context;
    private Activity activity;
    private ArrayList<String> listtosend;
    private long passedid;
    private String wholePath;
    private boolean xceptionFlag = false;
    private Socket socket;
    private String hostName,canonicalHostname;
    private String givenName;
    long timetoplay=0;

    ServerSocket(Context context, Activity act, String path, long id){
        this.context = context;
        this.activity = act;
        this.wholePath = path;
        this.passedid = id;
    }

    @Override
    protected void onProgressUpdate(Void... values) {
        super.onProgressUpdate(values);
    }

    @SuppressLint("LongLogTag")
    @Override
    protected Void doInBackground(Void... voids) {

        System.out.println("array list");
        ArrayList<File> files = new ArrayList<>();
        System.out.println(" array about to create.");

        files.add(new File(wholePath));
        System.out.println("file created..");

        //long x = files.get(0).length()*8;
        //x=x/1400000;

        listtosend = getClientList();
        timetoplay=listtosend.size()*10;
        //timetoplay = new Location().getTime();
        for(String ipaddress:listtosend)
        {
            //Toast.makeText(context,"sending file to "+ipaddress,Toast.LENGTH_SHORT).show();

            try {
                //socket.setReuseAddress(true);
                socket = new Socket(ipaddress, 50000);

                System.out.println("Connecting to : "+ipaddress);
                DataInputStream dis = new DataInputStream(new BufferedInputStream(socket.getInputStream()));
                DataOutputStream dos = new DataOutputStream(new BufferedOutputStream(socket.getOutputStream()));

                //write the number of files to the server
                dos.writeInt(files.size());
                dos.flush();
                //write file size
                for (int i = 0; i < files.size(); i++) {
                    int file_size = Integer.parseInt(String.valueOf(files.get(i).length()));
                    dos.writeLong(file_size);
                    dos.flush();
                }
                //write file names
                for (int i = 0; i < files.size(); i++) {
                    dos.writeUTF(files.get(i).getName());
                    dos.flush();
                }

                //buffer for file writing, to declare inside or outside loop?
                int n = 0;//used in while loop for sending
                byte[] buf = new byte[4092];
                //outer loop, executes one for each file
                for (int i = 0; i < files.size(); i++) {

                    //create new fileinputstream for each file
                    FileInputStream fis = new FileInputStream(files.get(i));

                    //write file to dos
                    while ((n = fis.read(buf)) != -1) {
                        dos.write(buf, 0, n);
                        dos.flush();
                    }
                    System.out.println("Whole File is flushed");
                }
                //write timetoplay
                dos.writeLong(timetoplay);
                timetoplay-=10;
                //timetoplay-=x;
                dos.flush();
                dos.close();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                xceptionFlag = true;
                e.printStackTrace();
                //Log.e("error before closing",e.toString());
            }
        }

        for (String ipaddress : listtosend)
        {
            try {
                socket = new Socket(ipaddress, 49999);
                System.out.println("Connecting for flag to : "+ipaddress);
                DataOutputStream dos = new DataOutputStream(new BufferedOutputStream(socket.getOutputStream()));

                //write the number of files to the server
                dos.writeBoolean(true);
                dos.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }

        }

        Log.i("===end of start ====", "==");
        try{
            if(socket!=null && !socket.isClosed()){
                socket.close();
            }
        }
        catch (Exception e){
            xceptionFlag = true;
            Log.e("error in closing server socket:",e.toString());
            e.printStackTrace();
        }
        return null;
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        super.onPostExecute(aVoid);

        if(xceptionFlag){
            Toast.makeText(context,"Something went wrong see log.e.",Toast.LENGTH_LONG).show();
        }
        else{
            Toast.makeText(context,"Sent successfull! & service is called",Toast.LENGTH_SHORT).show();
            new findmusic().playSongFromFind(wholePath,passedid,timetoplay);
        }
    }

    public ArrayList<String> getClientList() {

        final ArrayList<String> temp = new ArrayList();

        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                BufferedReader br = null;
                boolean isFirstLine = true;
                int i=0;

                try {
                    br = new BufferedReader(new FileReader("/proc/net/arp"));
                    String line;

                    while ((line = br.readLine()) != null) {
                        if (isFirstLine) {
                            isFirstLine = false;
                            continue;
                        }

                        String[] splitted = line.split(" +");

                        if (splitted != null && splitted.length >= 4) {

                            String ipAddress = splitted[0];
                            String macAddress = splitted[3];

                            boolean isReachable = InetAddress.getByName(
                                    splitted[0]).isReachable(500);
                            // this is network call so we cant do that on UI thread, so i(kaushal28) take background thread.
                            if (isReachable) {
                                Log.d("Device Information", ipAddress + " : "
                                        + macAddress);
                                //added afterwards for receiving names of available clients..
                                //but by adding this names to array list, the ip addresses is lost. so do something.
                               /*try {
                                    Socket socket = new Socket();
                                    //receive from port 5006 and timeout is 5s.
                                    socket.connect(new InetSocketAddress(ipAddress, 5006), 5000);
                                    BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                                    givenName = reader.readLine();
                                    reader.close();
                                    socket.close();
                                    Log.i("TAG", givenName);
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }*/
                                //Assigning values to final array or array list is perfectly fine.

                                //arr.add(ipAddress);
                                temp.add(ipAddress);

                                InetAddress inetAddress = InetAddress.getByName(ipAddress);
                                hostName = inetAddress.getHostName();
                                canonicalHostname = inetAddress.getCanonicalHostName();

                                //  Toast.makeText(context,hostName+canonicalHostname,Toast.LENGTH_LONG).show();

                            }

                        }

                    }

                } catch (Exception e) {
                    xceptionFlag = true;
                    e.printStackTrace();
                } finally {
                    try {
                        br.close();
                    } catch (IOException e) {
                        xceptionFlag = true;
                        e.printStackTrace();
                    }
                }
            }
        });
        thread.start();

        //Wait util thread is completed. And then return array.
        //       Otherwise it'll return null array or array list or what ever.
        try{
            thread.join();
        }
        catch (Exception e){
            xceptionFlag = true;
            e.printStackTrace();
        }
        return temp;

    }
}