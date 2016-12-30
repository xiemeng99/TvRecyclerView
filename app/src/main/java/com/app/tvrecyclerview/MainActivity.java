package com.app.tvrecyclerview;

import android.graphics.Rect;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import app.com.tvrecyclerview.TvRecyclerView;

public class MainActivity extends AppCompatActivity {

    private TvRecyclerView mTvRecyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mTvRecyclerView = (TvRecyclerView) findViewById(R.id.tv_recycler_view);
        init();
    }

    private void init() {
        GridLayoutManager manager = new GridLayoutManager(MainActivity.this, 3);
        manager.setOrientation(LinearLayoutManager.HORIZONTAL);
        manager.supportsPredictiveItemAnimations();
        mTvRecyclerView.setLayoutManager(manager);
        int itemSpace = getResources().
                getDimensionPixelSize(R.dimen.recyclerView_item_space);
        mTvRecyclerView.addItemDecoration(new SpaceItemDecoration(itemSpace));
        DefaultItemAnimator animator = new DefaultItemAnimator();
        mTvRecyclerView.setItemAnimator(animator);
        RecyclerViewAdapter mAdapter = new RecyclerViewAdapter(MainActivity.this);
        mTvRecyclerView.setAdapter(mAdapter);
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
            outRect.top = space;
        }
    }
}
