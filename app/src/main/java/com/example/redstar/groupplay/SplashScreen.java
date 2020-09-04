package com.example.redstar.groupplay;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.EditText;

public class SplashScreen extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);

        EditText main_title = (EditText) findViewById(R.id.main_title);
        EditText main_sectitle = (EditText) findViewById(R.id.main_sectitle);

        main_title.setFocusable(false);
        main_title.setClickable(false);
        main_sectitle.setFocusable(false);
        main_sectitle.setClickable(false);

        Thread mythread = new Thread(){
            public void run(){
                try
                {
                    sleep(500);
                }
                catch(InterruptedException e){
                    e.printStackTrace();
                }
                finally {
                    //Toast.makeText(SplashScreen.this,"Called",Toast.LENGTH_LONG).show();
                    Intent i= new Intent(SplashScreen.this,MainActivity.class);
                    startActivity(i);
                }
            }
        };
        mythread.start();
    }
}
