package com.example.redstar.groupplay;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.InetAddress;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    FloatingActionButton connectwifi,createwifi;
    EditText txt;
    Button musicpage,devices;
    WifiManager wifiobj;
    public int isroot=0;//root can control group 1-hotstop -1-receiver

    private int ASK_MULTIPLE_PERMISSION_REQUEST_CODE = 1;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //txt = (EditText) findViewById(R.id.txt);


        wifiobj = (WifiManager)getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        connectwifi = (FloatingActionButton) findViewById(R.id.fab2);
        createwifi = (FloatingActionButton) findViewById(R.id.fab1);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!Settings.System.canWrite(getApplicationContext())) {
                Intent intent = new Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS, Uri.parse("package:" + getPackageName()));
                startActivityForResult(intent, 200);
            }
        }

        if (android.os.Build.VERSION.SDK_INT >= 23) {
            requestPermissions(new String[]{
                            Manifest.permission.READ_EXTERNAL_STORAGE,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE,
                            Manifest.permission.INTERNET,
                            Manifest.permission.WRITE_SETTINGS,
                            Manifest.permission.ACCESS_NETWORK_STATE,
                            Manifest.permission.CHANGE_NETWORK_STATE,
                            Manifest.permission.ACCESS_WIFI_STATE,
                            Manifest.permission.CHANGE_WIFI_STATE,},
                    ASK_MULTIPLE_PERMISSION_REQUEST_CODE);
        }



        final Intent openfindmusic = new Intent(this,findmusic.class);
        musicpage=(Button)findViewById(R.id.musicpage);
        musicpage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                startActivity(openfindmusic);
            }
        });

        devices=(Button)findViewById(R.id.devicelist);
        devices.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {

                //long now = new Date().getTime();
                //for (int i=0;i<5;i++)
                //txt.setText(txt.getText().toString()+" "+now+"\n");
                //Toast.makeText(getApplicationContext(),now+" datetime",Toast.LENGTH_LONG).show();

                ArrayList<String> templist = getClientList();
                if(templist.size()!=0)
                for (String s:templist)
                {
                    Toast.makeText(getApplicationContext(),s,Toast.LENGTH_SHORT).show();
                }
                else
                    Toast.makeText(getApplicationContext(),"no one is connected",Toast.LENGTH_SHORT).show();
            }
            });

        connectwifi.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                wifiobj.setWifiEnabled(true);
                connectwifi.setImageResource(R.drawable.wifi);
                createwifi.setImageResource(R.drawable.hotspotoff);
                isroot=0;
                Toast.makeText(getApplicationContext(),"WIFI enabled",Toast.LENGTH_SHORT).show();
                startActivity(new Intent(Settings.ACTION_WIFI_SETTINGS));

            }
        });

        createwifi.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(wifiobj.isWifiEnabled())
                {
                    wifiobj.setWifiEnabled(false);

                }
                connectwifi.setImageResource(R.drawable.wifioff);
                createwifi.setImageResource(R.drawable.hotspot);

                WifiConfiguration netConfig = new WifiConfiguration();
                netConfig.SSID = "GroupPlay";

                netConfig.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.OPEN);
                netConfig.allowedProtocols.set(WifiConfiguration.Protocol.RSN);
                netConfig.allowedProtocols.set(WifiConfiguration.Protocol.WPA);
                netConfig.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);

                try{
                    Method setWifiApMethod = wifiobj.getClass().getMethod("setWifiApEnabled", WifiConfiguration.class, boolean.class);
                    boolean apstatus=(Boolean) setWifiApMethod.invoke(wifiobj, netConfig,true);

                    Method isWifiApEnabledmethod = wifiobj.getClass().getMethod("isWifiApEnabled");
                    while(!(Boolean)isWifiApEnabledmethod.invoke(wifiobj)){};
                    Method getWifiApStateMethod = wifiobj.getClass().getMethod("getWifiApState");
                    int apstate=(Integer)getWifiApStateMethod.invoke(wifiobj);
                    Method getWifiApConfigurationMethod = wifiobj.getClass().getMethod("getWifiApConfiguration");
                    netConfig=(WifiConfiguration)getWifiApConfigurationMethod.invoke(wifiobj);
                    isroot=1;
                    Log.e("CLIENT", "\nSSID:"+netConfig.SSID+"\nPassword:"+netConfig.preSharedKey+"\n");
                    Toast.makeText(getApplicationContext(),"GroupPlay HotSpot Created ",Toast.LENGTH_SHORT).show();

                } catch (Exception e) {
                    Log.e("this is error"+this.getClass().toString(), "", e);
                }

            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        ConnectivityManager connManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo mWifi = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);

        if (mWifi.isConnected())
        {
            isroot=-1;
            Intent i = new Intent(MainActivity.this,Receiver.class);
            startActivity(i);
        }
    }

    public ArrayList<String> getClientList() {

        final ArrayList<String> list = new ArrayList();

        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                BufferedReader br = null;
                boolean isFirstLine = true;

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
                            // this is network call so we cant do that on UI thread, so i take background thread.
                            if (isReachable) {
                                Log.d("Device Information", ipAddress + " : "
                                        + macAddress);
                                //added afterwards for receiving names of available clients..
                                //but by adding this names to array list, the ip addresses is lost. so do something.
                                /*try {
                                    Socket socket = new Socket();
                                    //receive from port 5006 and timeout is 5s.
                                    socket.connect(new InetSocketAddress(ipAddress, 50001), 50002);
                                    BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                                    String givenName = reader.readLine();
                                    reader.close();
                                    socket.close();
                                    Log.i("TAG", givenName);
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }*/
                                //Assigning values to final array or array list is perfectly fine.

                                //arr.add(ipAddress);
                                list.add(ipAddress);

                                InetAddress inetAddress = InetAddress.getByName(ipAddress);
                                String hostName = inetAddress.getHostName();
                                String canonicalHostname = inetAddress.getCanonicalHostName();

                                //  Toast.makeText(context,hostName+canonicalHostname,Toast.LENGTH_LONG).show();

                            }

                        }

                    }

                } catch (Exception e) {

                    e.printStackTrace();
                } finally {
                    try {
                        br.close();
                    } catch (IOException e) {

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

            e.printStackTrace();
        }
        return list;

    }
}
