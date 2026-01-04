package com.dwin.common_app.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.dwin.common_app.R;
import com.dwin.common_app.bean.FuncBean;

import java.util.List;

public class FunctionAdapter extends RecyclerView.Adapter<FunctionAdapter.FuncViewHolder> {
    private final List<FuncBean> funcBeanList;
    private final OnItemClickListener onItemClickListener;

    public FunctionAdapter(List<FuncBean> funcBeanList, OnItemClickListener mOnItemClickListener) {
        this.funcBeanList = funcBeanList;
        this.onItemClickListener = mOnItemClickListener;
    }

    @NonNull
    @Override
    public FuncViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_func_layout, parent, false);
        return new FuncViewHolder(view, onItemClickListener);
    }

    @Override
    public void onBindViewHolder(@NonNull FuncViewHolder holder, int position) {
        FuncBean currentBean = funcBeanList.get(position);
        holder.nameTextView.setText(currentBean.getName());
        holder.iconImageView.setImageResource(currentBean.getIconResId()); // 设置图标资源
    }

    @Override
    public int getItemCount() {
        return funcBeanList.size();
    }

    public interface OnItemClickListener {
        void onItemClick(View view, int position);
    }

    public static class FuncViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView idTextView;
        TextView nameTextView;
        ImageView iconImageView;
        private OnItemClickListener mListener;

        public FuncViewHolder(View itemView, OnItemClickListener listener) {
            super(itemView);
            nameTextView = itemView.findViewById(R.id.ItemName);
            iconImageView = itemView.findViewById(R.id.ItemIcon);
            mListener = listener;
            itemView.setOnClickListener(this); // 绑定点击事件到根视图
        }

        @Override
        public void onClick(View view) {
            if (mListener != null) {
                mListener.onItemClick(view, getAdapterPosition());
            }
        }
    }
}