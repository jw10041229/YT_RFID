package com.dwin.common_app.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.dwin.common_app.R;
import com.dwin.common_app.bean.AppInfoBean;

import java.util.List;

public class IconTextSpinnerAdapter extends BaseAdapter {
    private Context context;
    private List<AppInfoBean> items;
    private LayoutInflater inflater;

    public IconTextSpinnerAdapter(Context context, List<AppInfoBean> items) {
        this.context = context;
        this.items = items;
        inflater = LayoutInflater.from(context);
    }

    @Override
    public int getCount() {
        return items.size();
    }

    @Override
    public Object getItem(int position) {
        return items.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.list_item_icon_text, parent, false);
            holder = new ViewHolder();
            holder.icon = convertView.findViewById(R.id.icon);
            holder.text = convertView.findViewById(R.id.text);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        AppInfoBean item = items.get(position);
        if (item == null) {
            holder.icon.setImageDrawable(null);  // 不显示图标
            holder.text.setText("null");  // 显示NULL
        } else {
            if (item.getIcon() != null) {
                holder.icon.setImageDrawable(item.getIcon());
            }
            if (item.getAppName() != null) {
                holder.text.setText(item.getAppName());
            }
        }
        return convertView;
    }

    static class ViewHolder {
        ImageView icon;
        TextView text;
    }

    public void setList(List<AppInfoBean> list) {
        this.items = list;
    }

    public List<AppInfoBean> getList() {
        return this.items;
    }
}
