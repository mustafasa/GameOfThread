package com.mustafa.gameofthread;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    TextView tx;
    Button AsynBtn, ThreadBtn, serviceBtn, stopservice;
    boolean status = false;
    int counter = 0;
    private ProgressBar progress;
    myAsyn asyn;
    //for thread
    Thread th;
    Handler Hand;
    MyReceiver myReceiver;

    @Override
    protected void onStop() {
        // TODO Auto-generated method stub
        unregisterReceiver(myReceiver);
        super.onStop();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        tx = (TextView) findViewById(R.id.textview);
        progress = (ProgressBar) findViewById(R.id.progressBar1);

        AsynBtn = (Button) findViewById(R.id.asynBtn);
        ThreadBtn = (Button) findViewById(R.id.ThreadBtn);
        serviceBtn = (Button) findViewById(R.id.serviceBtn);
        stopservice = (Button) findViewById(R.id.stopserviceBtn);


        AsynBtn.setOnClickListener(this);//when we say this we need to implements View.OnClickListener so its like mehtod to be passed
        ThreadBtn.setOnClickListener(this);
        serviceBtn.setOnClickListener(this);
        stopservice.setOnClickListener(this);


        th = new Thread(new mythread());
        //handler message older way for thread to recievm sg
        Hand = new Handler(getApplicationContext().getMainLooper());

        ///custom broadcst reciever
        myReceiver = new MyReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(InnerService.MY_ACTION);//getting service by name
        registerReceiver(myReceiver, intentFilter);//here imclubing service with broadcast

        //for thead with class altought it can be same as thread withoutclass
        //but yeah im experiment something like this
        //getting issues
//        Hand =new Handler(){
//            @Override
//            public void handleMessage(Message msg){
//                progress.setProgress(msg.arg1);
//              //  tx.setText("Thread " +msg.arg1);
//            }
//
//        };


    }


    //Button Handler
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.asynBtn:
                status = true;
                asyn = new myAsyn();
                asyn.execute(counter);
                //can stop loop by this and it act as break for thread.
                // asyn.cancel(true);
                break;
            case R.id.ThreadBtn:
                status = true;
                th.start();
                break;
            case R.id.serviceBtn:
                Intent ser = new Intent(getBaseContext(), InnerService.class);
                ser.putExtra("counter", " " + counter);
                startService(ser);
            case R.id.stopserviceBtn:
                Intent stp = new Intent(getBaseContext(), InnerService.class);
                stopService(new Intent(stp));


        }


    }

    //thread with runnable with seprate class
    class mythread implements Runnable {

        @Override
        public void run() {


            while (status) {
                counter++;
                try {
                    if (counter > 9) {
                        status = false;
                        counter = 0;
                    }

                    Thread.sleep(1000);
                    //so using handler
                    Hand.post(new Runnable() {
                        @Override
                        public void run() {
                            tx.setText("Thread " + counter);
                            progress.setProgress(counter);

                        }
                    });

                    //communicating with handler new way but issues
//                    Message msg = Message.obtain();
//                    msg.arg1 = counter;
//                    Hand.sendMessage(msg);


                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                // Log.i("MyThread", counter + " " + Thread.currentThread().getId());
                //if i try directly yo accessui thread it will shoot an error we need handler classs
                //tx.setText(counter);


            }
            ;
        }
    }


    //asyntask example
    //Asyntask(passing value,retruning value,after complete task return value)
    private class myAsyn extends AsyncTask<Integer, Integer, Integer> {
        //to do stuff before getting in background
        @Override
        protected void onPreExecute() {
            super.onPreExecute();


        }

        //now ur in background and enjoying it
        @Override
        protected Integer doInBackground(Integer... params) {

            while (status) {

                try {
                    Thread.sleep(1000);
                    counter++;
                    if (counter > 9) {
                        status = false;
                        // counter = 0;
                    }
                    //this is passing to onProgressUpdate
                    publishProgress(counter);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

            }
            return counter;
        }

        //this is used to update ui while in asyncingggg
        @Override
        protected void onProgressUpdate(Integer... val) {
            super.onProgressUpdate(val);
            progress.setProgress(val[0]);
            tx.setText("Async " + val[0]);
        }

        //once done with asycning is done then thiss guy will help to update on ui
        @Override
        protected void onPostExecute(Integer integer) {
            super.onPostExecute(integer);

            tx.setText("Async Stop " + counter);
        }
    }



    //inner service created with thread init
    public static class InnerService extends Service

    {
        final static String MY_ACTION = "MY_ACTION";

        @Nullable
        @Override
        public IBinder onBind(Intent intent) {
            return null;
        }

        @Override
        public int onStartCommand(Intent intent, int flags, int startId) {
            String userID = intent.getExtras().getString("counter");
            // Let it continue running until it is stopped.
            Toast.makeText(this, "Service Started " + userID, Toast.LENGTH_LONG).show();
            MyThread myThread = new MyThread();
            myThread.start();
            return START_STICKY;
        }

        @Override
        public void onDestroy() {
            super.onDestroy();
            Toast.makeText(this, "Service Destroyed", Toast.LENGTH_LONG).show();
        }

        public class MyThread extends Thread {

            @Override
            public void run() {
                // TODO Auto-generated method stub
                for (int i = 0; i < 10; i++) {
                    try {
                        Thread.sleep(1000);
                        Intent intent = new Intent();
                        intent.setAction(MY_ACTION);

                        intent.putExtra("DATAPASSED", i);

                        sendBroadcast(intent);
                    } catch (InterruptedException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }
                stopSelf();
            }

        }
    }
//created custom boradcasr to recevie events from services
    private class MyReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context arg0, Intent arg1) {
            // TODO Auto-generated method stub

            int datapassed = arg1.getIntExtra("DATAPASSED", 0);
            progress.setProgress(datapassed);
            tx.setText("Service "+datapassed);



        }
    }

/*
//Thread and handler example. without seprate class
    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.start:
                status=true;
                new Thread(new Runnable(){
                    @Override
                    public void run() {

                        while(status){
                            counter++;
                            try{
                                Thread.sleep(1000);
                                //you can use view class to post on main ui
                                tx.post(new Runnable() {
                                    @Override
                                    public void run() {

                                        progress.setProgress(counter);
                                    }
                                });

                            }
                            catch(InterruptedException e){                            }

                            Log.i("MyThread",counter+" "+Thread.currentThread().getId());
                            //if i try directly yo accessui thread it will shoot an error we need handler classs
                            //tx.setText(counter);

                            //so using handler
                            Hand.post(new Runnable(){
                                @Override
                                public void run() {
                                    tx.setText(" "+counter);

                                }
                            });
                        }
                    }
                }).start();
                break;

            case R.id.stop:
                status=false;
                break;

        }
    }

*/

/* creating ANR Error
    @Override
    public void onClick(View v) {
    switch (v.getId()){
        case R.id.start:
            status=true;
            while(status){
                Log.i("hh"," "+Thread.currentThread().getId());
            }
            break;

        case R.id.stop:
            break;

    }
    }
*/



}

