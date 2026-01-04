package com.dwin.common_app.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.dwin.common_app.R;

public class TextSpinnerAdapter extends BaseAdapter {
    private Context context;
    private String[] items;
    private LayoutInflater inflater;

    public TextSpinnerAdapter(Context context, String[] items) {
        this.context = context;
        this.items = items;
        inflater = LayoutInflater.from(context);
    }

    @Override
    public int getCount() {
        return items == null ? 0 : items.length;
    }

    @Override
    public Object getItem(int position) {
        return items[position];
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.list_item_text, parent, false);
            holder = new ViewHolder();
            holder.icon = convertView.findViewById(R.id.icon);
            holder.text = convertView.findViewById(R.id.text);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        String item = items[position];
        if (item != null)
            holder.text.setText(item);

        return convertView;
    }

    static class ViewHolder {
        ImageView icon;
        TextView text;
    }

    public void setList(String[] list) {
        this.items = list;
    }

    public String[] getList() {
        return this.items;
    }
}
