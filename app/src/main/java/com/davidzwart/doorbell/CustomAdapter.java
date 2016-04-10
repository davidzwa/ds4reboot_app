package com.davidzwart.doorbell;
import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;

public class CustomAdapter extends ArrayAdapter<Model>{
        Model[] modelItems = null;
        Context context;
        public CustomAdapter(Context context, Model[] resource) {
                super(context,R.layout.row,resource);
                // Auto-generated constructor stub
                this.context = context;
                this.modelItems = resource;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
                LayoutInflater inflater = ((Activity)context).getLayoutInflater();
                convertView     = inflater.inflate(R.layout.row, parent, false);
                CheckBox cb     = (CheckBox) convertView.findViewById(R.id.checkBox1);
                cb.setText(modelItems[position].getName());
                if(modelItems[position].getValue() == 1)
                        cb.setChecked(true);
                else
                        cb.setChecked(false);

                return convertView;

        }
}