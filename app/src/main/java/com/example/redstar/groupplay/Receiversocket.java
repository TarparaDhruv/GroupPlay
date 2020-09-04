package com.example.redstar.groupplay;

/**
 * Created by Redstar on 07-Nov-17.
 */

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.*;
import java.net.ServerSocket;
import java.util.ArrayList;

public class Receiversocket extends AsyncTask<Void,Void,Void> {

    private static Context context;
    private static Activity activity;
    private boolean xceptionFlag = false;
    private java.net.ServerSocket ss;
    ServerSocket sss;
    ArrayList<File> files;
    static String finalpath;
    long timetoplay;
    boolean received;

    Receiversocket(Context c, Activity a){
        context = c;
        activity = a;
    }

    @SuppressLint("LongLogTag")
    @Override
    protected Void doInBackground(Void... voids) {
        try {

            //ServerSocket ss = new ServerSocket(5004);
            //this is done instead of above line because it was giving error of address is already in use.
            ss = new ServerSocket(50000);
            sss = new ServerSocket(49999);
            //ss.setReuseAddress(true);
            //ss.bind(new InetSocketAddress(50000));

            System.out.println("waiting for accept");
            Socket socket = ss.accept();
            System.out.println("Accepted!");

            DataInputStream dis = new DataInputStream(new BufferedInputStream(socket.getInputStream()));
            //DataOutputStream dos = new DataOutputStream(new BufferedOutputStream(socket.getOutputStream()));

            System.out.println("flag is false, receiving file");

                int number = dis.readInt();
                files = new ArrayList<>(number);
                System.out.println("Number of Files to be received: " +number);

                ArrayList<Long> fileSize = new ArrayList<>(number);

                for(int i = 0; i < number ;i++){
                    long size = dis.readLong();
                    System.out.println(size);
                    fileSize.add(size);
                }
                //read file names, add files to arraylist
                for(int i = 0; i< number;i++){
                    File file = new File(dis.readUTF());
                    files.add(file);
                }
                int n = 0;
                byte[]buf = new byte[4092];

                //outer loop, executes one for each file
                for(int i = 0; i < files.size();i++){

                    System.out.println("Receiving file: " + files.get(i).getName());

                    //Create new Folder for our app, if it is not there and store received files there in our separate folder.
                    File folder = new File(Environment.getExternalStorageDirectory() +File.separator+ "GroupPlay");
                    boolean success = true;
                    if (!folder.exists()) {
                        success = folder.mkdirs();
                        if (success) {
                            System.out.println("new folder created");
                        }
                    }
                    else
                    {
                        System.out.println("already folder exists");
                    }
                    //create a new fileoutputstream for each new file
                    FileOutputStream fos = new FileOutputStream(Environment.getExternalStorageDirectory()+File.separator+"GroupPlay/" +files.get(i).getName());
                    finalpath=Environment.getExternalStorageDirectory()+File.separator+"GroupPlay/" +files.get(i).getName();
                    //read file
                    while (fileSize.get(i) > 0 && (n = dis.read(buf, 0, (int)Math.min(buf.length, fileSize.get(i)))) != -1)
                    {
                        fos.write(buf,0,n);
                        long x = fileSize.get(i);
                        x = x-n;
                        fileSize.set(i,x);
                    }
                    fos.close();
                    System.out.println("File is received");

                    timetoplay = dis.readLong();
                    //read countdown timer data
                }

        } catch (IOException e) {
            // TODO Auto-generated catch block
            xceptionFlag = true;
            Log.e("Error in receiving file: ",e.toString());
            e.printStackTrace();
        }

        try {
            Socket ssocket =  sss.accept();
            DataInputStream dis = new DataInputStream(new BufferedInputStream(ssocket.getInputStream()));
            received = dis.readBoolean();
            Log.i("Status received : ", received+" from serversocket");
            System.out.println("File received successfully stored at : "+finalpath+" and will play in "+timetoplay+" milisec");

        }catch (IOException e) {
            Log.i("Status received : ", received+" from serversocket");
            e.printStackTrace();
        }

        Log.i("==== the end of read ==", "==");
        try{
            if(!ss.isClosed()){
                ss.close();
            }
            if(!sss.isClosed()){
                sss.close();
            }
        }
        catch (Exception e){
            xceptionFlag = true;
            Log.e("Error in Receiversocket closing",e.toString());
            e.printStackTrace();
        }
        return null;
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        super.onPostExecute(aVoid);
        if (!xceptionFlag){
            //Toast.makeText(context,"files Received Successfully!!",Toast.LENGTH_SHORT).show();
            //Toast.makeText(context,"before command",Toast.LENGTH_SHORT).show();
            //Uri musicuri = ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI);
            try {
                if(received)
                {
                    Log.i("Status : ","in post execute in receiver socket");
                    new Receiver().playSongFromReceiverSocket(finalpath,timetoplay);
                    Log.i("Status : ","song play called");

                    new Receiver().executeAgain();
                    Log.i("Status true : ","execute again called");
                }
                else
                {
                    Log.i("Status false : ","execute again called");
                    new Receiver().executeAgain();
                }
                }catch (Exception e){
                Log.e("error in recesocket : ",e.toString());
                e.printStackTrace();
            }
        }
        else{
            System.out.println("Error : resolve context error and remove coomecnt from toastz");
            //Toast.makeText(context,"Something went wrong. see log",Toast.LENGTH_LONG).show();
        }
    }
}
