/**
 * 
 */
package org.readium.sdklauncher_android;

import java.util.ArrayList;

import android.app.ListActivity;
import android.os.Bundle;
import android.widget.ListView;

/**
 * @author chtian
 *
 */
public class ContainerList extends ListActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.container_list);
        
        final ListView view = (ListView)findViewById(R.id.containerList);
        String[] values = new String[]{"Android", "iPhone", "iPod"};
        
        final ArrayList<String> list = new ArrayList<String>();
        for(int i = 0; i < values.length;i++){
            list.add(values[i]);
        }
    }

}
