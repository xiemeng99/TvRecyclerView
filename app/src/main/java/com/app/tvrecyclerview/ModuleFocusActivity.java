package com.app.tvrecyclerview;

import android.graphics.Rect;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Toast;

import app.com.tvrecyclerview.ModuleLayoutManager;
import app.com.tvrecyclerview.TvRecyclerView;

public class ModuleFocusActivity extends AppCompatActivity {

    private TvRecyclerView mTvRecyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_module);
        mTvRecyclerView = (TvRecyclerView) findViewById(R.id.tv_recycler_view);
        init();
    }

    private void init() {
        ModuleLayoutManager manager = new ModuleLayoutManager(2, LinearLayoutManager.HORIZONTAL);
        mTvRecyclerView.setLayoutManager(manager);

        int itemSpace = getResources().
                getDimensionPixelSize(R.dimen.recyclerView_item_space);
        mTvRecyclerView.addItemDecoration(new SpaceItemDecoration(itemSpace));
        ModuleAdapter mAdapter = new ModuleAdapter(ModuleFocusActivity.this);
        mTvRecyclerView.setAdapter(mAdapter);

        mTvRecyclerView.setOnItemStateListener(new TvRecyclerView.OnItemStateListener() {
            @Override
            public void onItemViewClick(View view, int position) {
                Toast.makeText(ModuleFocusActivity.this,
                        ContantUtil.TEST_DATAS[position], Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onItemViewFocusChanged(boolean gainFocus, View view, int position) {
            }
        });
        mTvRecyclerView.setSelectPadding(35, 34, 35, 38);
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
