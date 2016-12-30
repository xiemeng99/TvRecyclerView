package com.app.tvrecyclerview;

import android.content.Context;
import android.graphics.drawable.GradientDrawable;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;


public class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private Context mContext;
    private RecyclerViewStateListener mListener;

    RecyclerViewAdapter(Context context) {
        mContext = context;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new RecyclerViewHolder(View.inflate(mContext, R.layout.item_recyclerview, null));
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, final int position) {
        final RecyclerViewHolder viewHolder = (RecyclerViewHolder) holder;
        viewHolder.mName.setText(ContantUtil.TEST_DATAS[position]);
        GradientDrawable drawable =(GradientDrawable)viewHolder.mFrameLayout.getBackground();
        drawable.setColor(ContextCompat.getColor(mContext, ContantUtil.getRandColor()));
    }

    @Override
    public int getItemCount() {
        return ContantUtil.TEST_DATAS.length;
    }

    void setRecyclerViewStateListener(RecyclerViewStateListener listener) {
        mListener = listener;
    }


    private class RecyclerViewHolder extends RecyclerView.ViewHolder
            implements View.OnClickListener, View.OnFocusChangeListener {

        FrameLayout mFrameLayout;
        TextView mName;

        RecyclerViewHolder(View itemView) {
            super(itemView);
            mName = (TextView) itemView.findViewById(R.id.tv_item_tip);
            mFrameLayout = (FrameLayout) itemView.findViewById(R.id.fl_main_layout);
            mFrameLayout.setOnClickListener(this);
            mFrameLayout.setOnFocusChangeListener(this);
        }

        @Override
        public void onClick(View v) {
            if (mListener != null) {
                mListener.onItemClick(v, getAdapterPosition());
            }
        }


        @Override
        public void onFocusChange(View v, boolean hasFocus) {
            if (mListener != null) {
                mListener.onItemFocusChange(v, hasFocus, getAdapterPosition());
            }
        }
    }

    interface RecyclerViewStateListener {
        void onItemClick(View v, int position);
        void onItemFocusChange(View v, boolean hasFocus, int position);
    }
}
