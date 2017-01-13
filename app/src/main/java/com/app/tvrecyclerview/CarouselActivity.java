package com.app.tvrecyclerview;

import android.graphics.Rect;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Toast;

import app.com.tvrecyclerview.TvRecyclerView;

public class CarouselActivity extends AppCompatActivity {

    private TvRecyclerView mTvRecyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_carousel);
        mTvRecyclerView = (TvRecyclerView) findViewById(R.id.tv_recycler_view);
        init();
    }

    private void init() {
        GridLayoutManager manager = new GridLayoutManager(CarouselActivity.this, 1);
        manager.setOrientation(LinearLayoutManager.HORIZONTAL);
        manager.supportsPredictiveItemAnimations();
        mTvRecyclerView.setLayoutManager(manager);

        mTvRecyclerView.setLayoutManager(manager);
        int itemSpace = getResources().
                getDimensionPixelSize(R.dimen.recyclerView_item_space1);
        mTvRecyclerView.addItemDecoration(new SpaceItemDecoration(itemSpace));
        DefaultItemAnimator animator = new DefaultItemAnimator();
        mTvRecyclerView.setItemAnimator(animator);
        RecyclerViewAdapter1 mAdapter = new RecyclerViewAdapter1(CarouselActivity.this);
        mTvRecyclerView.setAdapter(mAdapter);

        mTvRecyclerView.setOnItemStateListener(new TvRecyclerView.OnItemStateListener() {
            @Override
            public void onItemViewClick(View view, int position) {
                Toast.makeText(CarouselActivity.this,
                        ContantUtil.TEST_DATAS[position], Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onItemViewFocused(View view, int position) {
                view.animate().scaleX(1.4f).scaleY(1.4f).setDuration(150).start();
            }
        });
    }

    private class SpaceItemDecoration extends RecyclerView.ItemDecoration {

        private int space;

        SpaceItemDecoration(int space) {
            this.space = space;
        }

        @Override
        public void getItemOffsets(Rect outRect, View view, RecyclerView parent,
                                   RecyclerView.State state) {
            outRect.left = space;
        }
    }
}
