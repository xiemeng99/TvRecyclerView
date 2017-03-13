package app.com.tvrecyclerview;

import android.graphics.PointF;
import android.support.annotation.NonNull;
import android.support.v4.view.ViewCompat;
import android.support.v7.widget.OrientationHelper;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;


public class ModuleLayoutManager extends RecyclerView.LayoutManager implements
        RecyclerView.SmoothScroller.ScrollVectorProvider {

    private static final String TAG = "ModuleLayoutManager";

    private static final int HORIZONTAL = OrientationHelper.HORIZONTAL;

    private static final int VERTICAL = OrientationHelper.VERTICAL;

    private final static int LAYOUT_START = -1;

    private final static int LAYOUT_END = 1;

    private int mOrientation;

    private boolean mShouldReverseLayout = false;
    private boolean mReverseLayout = false;

    @NonNull
    private OrientationHelper mPrimaryOrientation;
    @NonNull
    private OrientationHelper mSecondaryOrientation;


    private int mVerticalOffset;//竖直偏移量 每次换行时，要根据这个offset判断
    private int mFirstVisiPos;//屏幕可见的第一个View的Position
    private int mLastVisiPos;//屏幕可见的最后一个View的Position

    private int mNumRows;

    private final int mOriItemWidth;
    private final int mOriItemHeight;

    public ModuleLayoutManager(int rowCount, int orientation) {
        mOrientation = orientation;
        mOriItemWidth = 380;
        mOriItemHeight = 380;
        mNumRows = rowCount;
    }

    @Override
    public RecyclerView.LayoutParams generateDefaultLayoutParams() {
        return new RecyclerView.LayoutParams(
                RecyclerView.LayoutParams.WRAP_CONTENT,
                RecyclerView.LayoutParams.WRAP_CONTENT);
    }

    @Override
    public void onLayoutChildren(RecyclerView.Recycler recycler, RecyclerView.State state) {
        layoutChildren(recycler, state);
    }

    private void layoutChildren(RecyclerView.Recycler recycler, RecyclerView.State state) {
        if (getItemCount() == 0) {
            detachAndScrapAttachedViews(recycler);
            return;
        }
        if (getChildCount() == 0 && state.isPreLayout()) { //state.isPreLayout()是支持动画的
            return;
        }
        //onLayoutChildren方法在RecyclerView 初始化时 会执行两遍
        detachAndScrapAttachedViews(recycler);

        mVerticalOffset = 0;
        mFirstVisiPos = 0;
        mLastVisiPos = getItemCount();

        fill(recycler, state);
    }

    private void fill(RecyclerView.Recycler recycler, RecyclerView.State state) {
        int leftOffset;
        int topOffset;
        int minPos = mFirstVisiPos;
        mLastVisiPos = getItemCount() - 1;

        for (int i = minPos; i <= mLastVisiPos; i++) {
            View child = recycler.getViewForPosition(i);
            addView(child);
            int itemWidth = getItemWidth(i);
            int itemHeight = getItemHeight(i);
            measureChild(child, itemWidth, itemHeight);
            int itemStartPos = getItemStartIndex(i);
            int lastPos = itemStartPos / mNumRows;
            int topPos = itemStartPos % mNumRows;
            leftOffset = getDecoratedMeasurementHorizontal(child) * lastPos;
            topOffset = getDecoratedMeasurementVertical(child) * topPos;
            layoutDecoratedWithMargins(child, leftOffset, topOffset, leftOffset + getDecoratedMeasurementHorizontal(child),
                    topOffset + getDecoratedMeasurementVertical(child));
        }
    }

    public void measureChild(View child, int childWidth, int childHeight) {
        final RecyclerView.LayoutParams lp = (RecyclerView.LayoutParams) child.getLayoutParams();
        int width = Math.max(0, childWidth - lp.leftMargin - lp.rightMargin - getPaddingLeft() - getPaddingRight());
        int height = Math.max(0, childHeight - lp.topMargin - lp.bottomMargin - getPaddingTop() - getPaddingBottom());
        int childWidthSpec = View.MeasureSpec.makeMeasureSpec(width, View.MeasureSpec.EXACTLY);
        int childHeightSpec = View.MeasureSpec.makeMeasureSpec(height, View.MeasureSpec.EXACTLY);
        child.measure(childWidthSpec, childHeightSpec);
    }

    private int getItemWidth(int position) {
        return getItemColumnSize(position) * mOriItemWidth;
    }

    private int getItemHeight(int position) {
        return getItemRowSize(position) * mOriItemHeight;
    }

    private int getItemStartIndex(int position) {
        if (position == 0) {
            return 0;
        }
        if (position == 1) {
            return 2;
        }
        if (position == 2) {
            return 3;
        }
        if (position == 3) {
            return 4;
        }
        if (position == 4) {
            return 6;
        }
        return 0;
    }

    private int getItemRowSize(int position) {
        if (position == 0) {
            return 2;
        }
        if (position == 1) {
            return 1;
        }
        if (position == 2) {
            return 1;
        }
        if (position == 3) {
            return 2;
        }
        if (position == 4) {
            return 2;
        }
        return 1;
    }

    private int getItemColumnSize(int position) {
        return 1;
    }

    private int getDecoratedMeasurementHorizontal(View view) {
        final RecyclerView.LayoutParams params = (RecyclerView.LayoutParams)
                view.getLayoutParams();
        Log.i(TAG, "getDecoratedMeasurementHorizontal: ==leftMargin==" + params.leftMargin + "==rightMargin==" + params.rightMargin);
        return getDecoratedMeasuredWidth(view) + params.leftMargin
                + params.rightMargin;
    }

    private int getDecoratedMeasurementVertical(View view) {
        final RecyclerView.LayoutParams params = (RecyclerView.LayoutParams)
                view.getLayoutParams();
        Log.i(TAG, "getDecoratedMeasurementVertical: ==topMargin==" + params.topMargin + "==bottomMargin==" + params.bottomMargin);
        return getDecoratedMeasuredHeight(view) + params.topMargin
                + params.bottomMargin;
    }

    public int getVerticalSpace() {
        return getHeight() - getPaddingTop() - getPaddingBottom();
    }

    private int getHorizontalSpace() {
        return getWidth() - getPaddingLeft() - getPaddingRight();
    }

    @Override
    public void onLayoutCompleted(RecyclerView.State state) {
        super.onLayoutCompleted(state);
    }

    public void setOrientation(int orientation) {
        if (orientation != HORIZONTAL && orientation != VERTICAL) {
            throw new IllegalArgumentException("invalid orientation.");
        }
        assertNotInLayoutOrScroll(null);
        if (orientation == mOrientation) {
            return;
        }
        mOrientation = orientation;
        requestLayout();
    }

    public void setReverseLayout(boolean reverseLayout) {
        assertNotInLayoutOrScroll(null);
        mReverseLayout = reverseLayout;
        requestLayout();
    }

    private void resolveShouldLayoutReverse() {
        // A == B is the same result, but we rather keep it readable
        if (mOrientation == VERTICAL || !isLayoutRTL()) {
            mShouldReverseLayout = mReverseLayout;
        } else {
            mShouldReverseLayout = !mReverseLayout;
        }
    }

    private int getFirstChildPosition() {
        final int childCount = getChildCount();
        return childCount == 0 ? 0 : getPosition(getChildAt(0));
    }

    private boolean isLayoutRTL() {
        return getLayoutDirection() == ViewCompat.LAYOUT_DIRECTION_RTL;
    }

    private int calculateScrollDirectionForPosition(int position) {
        if (getChildCount() == 0) {
            return mShouldReverseLayout ? LAYOUT_END : LAYOUT_START;
        }
        final int firstChildPos = getFirstChildPosition();
        return position < firstChildPos != mShouldReverseLayout ? LAYOUT_START : LAYOUT_END;
    }

    @Override
    public PointF computeScrollVectorForPosition(int targetPosition) {
        final int direction = calculateScrollDirectionForPosition(targetPosition);
        PointF outVector = new PointF();
        if (direction == 0) {
            return null;
        }
        if (mOrientation == HORIZONTAL) {
            outVector.x = direction;
            outVector.y = 0;
        } else {
            outVector.x = 0;
            outVector.y = direction;
        }
        return outVector;
    }

}