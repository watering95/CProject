package com.example.water.cproject;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by watering on 18. 3. 17.
 */

public class ListCodeAdapter extends BaseAdapter {
    private final List<String> list;
    private final LayoutInflater inflater;

    public ListCodeAdapter(Context context, ArrayList<String> data) {
        this.list = data;
        inflater = LayoutInflater.from(context);
    }

    @Override
    public int getCount() {
        return this.list.size();
    }

    @Override
    public Object getItem(int position) {
        return list.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if(convertView == null) {
            convertView = inflater.inflate(R.layout.layout_list, parent, false);
        }

        TextView code = convertView.findViewById(R.id.textView_layout_list);
        code.setText(list.get(position).toString());
        return convertView;
    }
}
