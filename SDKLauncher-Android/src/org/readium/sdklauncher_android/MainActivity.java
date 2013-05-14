package org.readium.sdklauncher_android;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.app.Activity;
//import android.view.Menu;
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

    // @Override
    // public boolean onCreateOptionsMenu(Menu menu) {
    // // Inflate the menu; this adds items to the action bar if it is present.
    // //getMenuInflater().inflate(R.menu.main, menu);
    // return true;
    // }

}
