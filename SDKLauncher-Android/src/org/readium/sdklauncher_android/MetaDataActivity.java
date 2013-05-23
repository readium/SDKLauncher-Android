package org.readium.sdklauncher_android;

import java.util.ArrayList;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class MetaDataActivity extends Activity {
    private Context context;
    private Button back;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.meta_data);

        context = this;
        back = (Button) findViewById(R.id.backToBookView);
        
        Intent intent = getIntent();
        if (intent.getFlags() == Intent.FLAG_ACTIVITY_NEW_TASK) {
            Bundle extras = intent.getExtras();
            if (extras != null) {
                String value = extras.getString("bookname");
                back.setText(value);
            }
        }
        //TODO:Get Meta Data ......

        initListener();
    }

    private void initListener() {
        back.setOnClickListener(new Button.OnClickListener() {

            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }
}
