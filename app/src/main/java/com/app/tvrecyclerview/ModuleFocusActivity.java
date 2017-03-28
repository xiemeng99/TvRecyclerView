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
    public int[] mStartIndex = {0, 2, 3, 5, 8, 9, 10, 11, 12, 14, 17, 18, 20};
    public int[] mItemRowSizes = {2, 1, 2, 1, 1, 1, 1, 1, 2, 1, 1, 2, 1};
    public int[] mItemColumnSizes = {1, 1, 2, 1, 1, 1, 1, 1, 2, 1, 1, 1, 1};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_module);
        mTvRecyclerView = (TvRecyclerView) findViewById(R.id.tv_recycler_view);
        init();
    }

    private void init() {
        ModuleLayoutManager manager = new MyModuleLayoutManager(3, LinearLayoutManager.HORIZONTAL,
                400, 260);
        mTvRecyclerView.setLayoutManager(manager);

        int itemSpace = getResources().
                getDimensionPixelSize(R.dimen.recyclerView_item_space);
        mTvRecyclerView.addItemDecoration(new SpaceItemDecoration(itemSpace));
        ModuleAdapter mAdapter = new ModuleAdapter(ModuleFocusActivity.this, mStartIndex.length);
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
            outRect.bottom = space;
            outRect.left = space;
        }
    }

    class MyModuleLayoutManager extends ModuleLayoutManager {

        MyModuleLayoutManager(int rowCount, int orientation, int baseItemWidth, int baseItemHeight) {
            super(rowCount, orientation, baseItemWidth, baseItemHeight);
        }

        @Override
        protected int getItemStartIndex(int position) {
            if (position < mStartIndex.length) {
                return mStartIndex[position];
            } else {
                return 0;
            }
        }

        @Override
        protected int getItemRowSize(int position) {
            if (position < mItemRowSizes.length) {
                return mItemRowSizes[position];
            } else {
                return 1;
            }
        }

        @Override
        protected int getItemColumnSize(int position) {
            if (position < mItemColumnSizes.length) {
                return mItemColumnSizes[position];
            } else {
                return 1;
            }
        }
    }
}
