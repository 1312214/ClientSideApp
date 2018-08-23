package com.duyhoang.clientsideappusingrandomnumberservice;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private final String TAG = MainActivity.class.getSimpleName();
    private final int GET_RANDOM_NUMBER_CODE = 1;

    TextView txtShowingNumber;
    Button btnBindService, btnUnbindService, btnGetRdNumber;

    Intent serviceIntent;
    Messenger randomNumberRequestedMessenger;
    Messenger randomNumberReceivedMessenger;
    Boolean isServiceBound;
    int randomNumberValue;
    private ServiceConnection serviceConnection;


    class RandomNumberReceivedHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case GET_RANDOM_NUMBER_CODE:
                    randomNumberValue = msg.arg1;
                    txtShowingNumber.setText(String.valueOf(randomNumberValue));
                    break;
            }
            super.handleMessage(msg);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        txtShowingNumber = findViewById(R.id.text_showing_random_number);
        btnBindService = findViewById(R.id.button_bind_service);
        btnUnbindService = findViewById(R.id.button_unbind_service);
        btnGetRdNumber = findViewById(R.id.button_get_random_number);

        btnBindService.setOnClickListener(this);
        btnUnbindService.setOnClickListener(this);
        btnGetRdNumber.setOnClickListener(this);
        isServiceBound = false;

        serviceConnection = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
                randomNumberRequestedMessenger = new Messenger(iBinder);
                randomNumberReceivedMessenger = new Messenger(new RandomNumberReceivedHandler());
                isServiceBound = true;
            }

            @Override
            public void onServiceDisconnected(ComponentName componentName) {
                randomNumberRequestedMessenger = null;
                randomNumberReceivedMessenger = null;
                isServiceBound = false;
            }
        };

        serviceIntent = new Intent();
        serviceIntent.setComponent(new ComponentName("com.duyhoang.servicesideapp", "com.duyhoang.servicesideapp.RandomNumberService"));
        serviceIntent.setPackage(getPackageName());

    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.button_bind_service:
                bindRandomNumberRemoteService();
                break;
            case R.id.button_unbind_service:
                unbindRandomNumberRemoteService();
                break;
            case R.id.button_get_random_number:
                fetchRandomNumberFromRemoteService();
                break;
        }
    }

    private void fetchRandomNumberFromRemoteService() {
        if (isServiceBound) {
            Message requestedMsg = Message.obtain(null, GET_RANDOM_NUMBER_CODE);
            requestedMsg.replyTo = randomNumberReceivedMessenger;
            try {
                randomNumberRequestedMessenger.send(requestedMsg);
            } catch (RemoteException e) {
                Log.e(TAG, e.getMessage());
            }
        } else
            Toast.makeText(this, "Service unbounded", Toast.LENGTH_SHORT).show();

    }

    private void unbindRandomNumberRemoteService() {
        unbindService(serviceConnection);
        isServiceBound = false;
        Toast.makeText(this, "Service unbound", Toast.LENGTH_SHORT).show();
    }

    private void bindRandomNumberRemoteService() {
        bindService(serviceIntent, serviceConnection, BIND_AUTO_CREATE);
        Toast.makeText(this, "Service bound", Toast.LENGTH_SHORT).show();
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        serviceConnection = null;
    }
}
