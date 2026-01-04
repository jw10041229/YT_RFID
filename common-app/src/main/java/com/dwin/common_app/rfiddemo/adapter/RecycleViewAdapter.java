package com.dwin.common_app.rfiddemo.adapter;


import android.annotation.SuppressLint;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.dwin.common_app.R;
import com.dwin.common_app.rfiddemo.entity.TagInfo;

import java.util.List;


public class RecycleViewAdapter extends RecyclerView.Adapter<RecycleViewAdapter.ViewHolder> {
    private List<TagInfo> mTagList;
    private Integer thisPosition = null;

    public Integer getThisPosition() {
        return thisPosition;
    }

    public void setThisPosition(Integer thisPosition) {
        this.thisPosition = thisPosition;
    }


    class ViewHolder extends RecyclerView.ViewHolder {
        TextView index;
        TextView type;
        TextView epc;
        TextView tid;
        TextView rssi;
        TextView count;
        TextView temp;

        public ViewHolder(final View view) {
            super(view);
            index = (TextView) view.findViewById(R.id.index);
            type = (TextView) view.findViewById(R.id.type);
            epc = (TextView) view.findViewById(R.id.epc);
            tid = (TextView) view.findViewById(R.id.tid);
            rssi = (TextView) view.findViewById(R.id.rssi);
            count = (TextView) view.findViewById(R.id.count);
            temp = view.findViewById(R.id.temp);
        }
    }

    public RecycleViewAdapter(List<TagInfo> list) {
        mTagList = list;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.rfid_recycle_item, parent, false);
        final ViewHolder holder = new ViewHolder(view);
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TagInfo tag = mTagList.get(holder.getAdapterPosition());
                System.out.println(tag);
                setThisPosition(holder.getAdapterPosition());
                notifyDataSetChanged();

            }
        });

        return holder;
    }

    @SuppressLint("DefaultLocale")
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        TagInfo tag = mTagList.get(position);
        holder.index.setText(tag.getIndex().toString());
        holder.type.setText(tag.getType());
        holder.epc.setText(tag.getEpc());
        holder.tid.setText(tag.getTid());
        holder.rssi.setText(tag.getRssi());
        holder.count.setText(tag.getCount().toString());
        holder.temp.setText(String.format("%.2f", tag.getCtesiusLtu31() * 0.01));
        if (getThisPosition() != null && position == getThisPosition()) {
            holder.itemView.setBackgroundColor(Color.rgb(135, 206, 235));
        } else {
            holder.itemView.setBackgroundColor(Color.WHITE);
        }
    }

    @Override
    public int getItemCount() {
        return mTagList.size();
    }

}
