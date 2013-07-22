package org.readium.sdk.android.launcher;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.app.Activity;
import android.content.Intent;

public class MainActivity extends Activity {

    private static final int STOPSPLASH = 0;
    private static final long SPLASHTIME = 500;

    // handler for splash screen
    private Handler splashHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
            case STOPSPLASH:
                Intent intent = new Intent(getApplicationContext(),
                        ContainerList.class);
                startActivity(intent);
                MainActivity.this.finish();
                break;
            }
            super.handleMessage(msg);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Message msg = new Message();
        msg.what = STOPSPLASH;
        splashHandler.sendMessageDelayed(msg, SPLASHTIME);
    }

}
