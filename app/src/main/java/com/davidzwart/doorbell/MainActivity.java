package com.davidzwart.doorbell;

import android.app.Activity;
import android.content.Context;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ListView;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity{

    UDP_client UDPjetser = new UDP_client();
    ListView lv;
    Model[] modelItems;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        lv = (ListView) findViewById(R.id.listView1);
        String[] check_items = getResources().getStringArray(R.array.house_parts);
        modelItems = new Model[check_items.length];
        int i = 0;
        for (String item : check_items) {
            modelItems[i] = new Model(item);    //Default value of 1
            i++;
        }
        CustomAdapter adapter = new CustomAdapter(this, modelItems);
        lv.setAdapter(adapter);

        UDPjetser.sendBroadcast("Hello from the other side");
    }

    public void UDPButtonAllListener(View v) {
        Toast.makeText(getApplicationContext(), "Doorbel  not responding.", Toast.LENGTH_SHORT).show();
        UDPjetser.sendBroadcast("Trying to doorbel here!");
    }

    public void UDPButtonPartsListener(View v) {
        Toast.makeText(getApplicationContext(), "Doorbel-devices not responding.", Toast.LENGTH_SHORT).show();
        UDPjetser.sendBroadcast("Yup thats the stuff!");
    }
}