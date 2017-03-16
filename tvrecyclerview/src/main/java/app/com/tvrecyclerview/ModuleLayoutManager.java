package app.com.tvrecyclerview;

import android.graphics.PointF;
import android.graphics.Rect;
import android.support.v4.view.ViewCompat;
import android.support.v7.widget.OrientationHelper;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.util.SparseArray;
import android.view.View;
import android.view.ViewGroup;


public class ModuleLayoutManager extends RecyclerView.LayoutManager implements
        RecyclerView.SmoothScroller.ScrollVectorProvider {

    private static final String TAG = "ModuleLayoutManager";

    private static final int HORIZONTAL = OrientationHelper.HORIZONTAL;

    private static final int VERTICAL = OrientationHelper.VERTICAL;

    private final static int LAYOUT_START = -1;

    private final static int LAYOUT_END = 1;

    private int mOrientation;
    private SparseArray<Rect> mItemRects;

    private boolean mShouldReverseLayout = false;
    private boolean mReverseLayout = false;


    private int mHorizontalOffset;//竖直偏移量 每次换行时，要根据这个offset判断
    private int mFirstVisitPos;//屏幕可见的第一个View的Position
    private int mLastVisitPos;//屏幕可见的最后一个View的Position

    private int mNumRows;

    private final int mOriItemWidth;
    private final int mOriItemHeight;

    public ModuleLayoutManager(int rowCount, int orientation) {
        mOrientation = orientation;
        mOriItemWidth = 380;
        mOriItemHeight = 380;
        mNumRows = rowCount;
        mItemRects = new SparseArray<>();
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
        if (getChildCount() == 0 && state.isPreLayout()) {
            return;
        }
        //onLayoutChildren方法在RecyclerView 初始化时 会执行两遍
        detachAndScrapAttachedViews(recycler);

        mHorizontalOffset = 0;
        mFirstVisitPos = 0;
        mLastVisitPos = getItemCount();

        fill(recycler, state);
    }

    private void fill(RecyclerView.Recycler recycler, RecyclerView.State state) {
        int leftOffset;
        int topOffset;
        for (int i = 0; i < getItemCount(); i++) {
            View child = recycler.getViewForPosition(i);
            addView(child);
            measureChild(child, getItemWidth(i), getItemHeight(i));

            // change leftOffset
            int itemStartPos = getItemStartIndex(i);
            int lastPos = itemStartPos / mNumRows;
            leftOffset = getDecoratedMeasurementHorizontal(child) * lastPos + getPaddingLeft();
            int topPos = itemStartPos % mNumRows;
            topOffset = getDecoratedMeasurementVertical(child) * topPos + getPaddingTop();

            //计算宽度 包括margin
            if (topOffset + getDecoratedMeasurementVertical(child) <= getVerticalSpace()) {
                layoutDecoratedWithMargins(
                        child,
                        leftOffset,
                        topOffset,
                        leftOffset + getDecoratedMeasurementHorizontal(child),
                        topOffset + getDecoratedMeasurementVertical(child));

                Rect frame = mItemRects.get(i);
                if (frame == null) {
                    frame = new Rect();
                }
                frame.set(leftOffset + mHorizontalOffset,
                        topOffset,
                        leftOffset + getDecoratedMeasurementVertical(child),
                        topOffset + getDecoratedMeasurementVertical(child));
                // 将当前的Item的Rect边界数据保存
                mItemRects.put(i, frame);
            }
        }
        recycleAndFillItems(recycler, state);
    }

    private int fill(RecyclerView.Recycler recycler, RecyclerView.State state, int dx) {
        if (getChildCount() > 0) {
            for (int i = getChildCount() - 1; i >= 0; --i) {
                View child = getChildAt(i);
                if (dx > 0) {  //回收左滑越界的view
                    if (getDecoratedRight(child) - dx < getPaddingLeft()) {
                        removeAndRecycleView(child, recycler);
                        mFirstVisitPos++;
                    }
                } else if (dx < 0) { //回收右滑越界的view
                    if (getDecoratedLeft(child) - dx > getWidth() - getPaddingRight()) {
                        removeAndRecycleView(child, recycler);
                        mLastVisitPos--;
                    }
                }
            }
        }

        int leftOffset;
        int topOffset;
        // layout children
        if (dx >= 0) {
            for (int i = mFirstVisitPos; i < getItemCount(); i++) {
                View child = recycler.getViewForPosition(i);
                addView(child);
                measureChild(child, getItemWidth(i), getItemHeight(i));

                // change leftOffset
                int itemStartPos = getItemStartIndex(i);
                int lastPos = itemStartPos / mNumRows;
                leftOffset = getDecoratedMeasurementHorizontal(child) * lastPos + getPaddingLeft();
                int topPos = itemStartPos % mNumRows;
                topOffset = getDecoratedMeasurementVertical(child) * topPos + getPaddingTop();

                //计算宽度 包括margin
                if (leftOffset <= getHorizontalSpace() &&
                        topOffset + getDecoratedMeasurementVertical(child) <= getVerticalSpace()) {
                    layoutDecoratedWithMargins(
                            child,
                            leftOffset,
                            topOffset,
                            leftOffset + getDecoratedMeasurementHorizontal(child),
                            topOffset + getDecoratedMeasurementVertical(child));

                    // 保存Rect 供逆序layout用
                    Rect rect = new Rect(
                            leftOffset + mHorizontalOffset,
                            topOffset,
                            leftOffset + getDecoratedMeasurementVertical(child),
                            topOffset + getDecoratedMeasurementVertical(child));
                    mItemRects.put(i, rect);
                    mLastVisitPos = i;
                } else if (leftOffset > getHorizontalSpace()) {
                    break;
                }
            }
            View lastChild = getChildAt(mLastVisitPos);
            if (mLastVisitPos <= getItemCount() - 1) {
                int gap = getWidth() - getPaddingRight() - getDecoratedRight(lastChild);
                if (gap > 0) {
                    dx -= gap;
                }
            }
        } else {
            /**
             * 利用Rect保存子View边界
             正序排列时，保存每个子View的Rect，逆序时，直接拿出来layout。
             */
            int maxPos = getItemCount() - 1;
            mFirstVisitPos = 0;
            if (getChildCount() > 0) {
                View firstView = getChildAt(0);
                maxPos = getPosition(firstView) - 1;
            }

            Log.i(TAG, "fill: ====maxPos====" + maxPos);
            for (int i = maxPos; i >= mFirstVisitPos; --i) {
                Rect rect = mItemRects.get(i);

                if (rect.right - dx < getPaddingRight()) {
                    mFirstVisitPos = i + 1;
                    break;
                } else {
                    View child = recycler.getViewForPosition(i);
                    addView(child, 0);//将View添加至RecyclerView中，childIndex为1，但是View的位置还是由layout的位置决定
                    measureChild(child, getItemWidth(i), getItemHeight(i));
                    layoutDecoratedWithMargins(child, rect.left - mHorizontalOffset,
                            rect.top, rect.right, rect.bottom);
                }
            }
        }

        Log.d("TAG", "count= [" + getChildCount() + "]" + ",[recycler.getScrapList().size():"
                + recycler.getScrapList().size() + ", dx:" + dx + ", mHorizontalOffset" + mHorizontalOffset + ", ");
        return dx;
    }

    /**
     * 回收不需要的Item，并且将需要显示的Item从缓存中取出
     */
    private void recycleAndFillItems(RecyclerView.Recycler recycler, RecyclerView.State state) {
        if (state.isPreLayout()) {
            return;
        }

        // 当前scroll offset状态下的显示区域
        Rect displayFrame = new Rect(mHorizontalOffset, 0,
                getHorizontalSpace(), mHorizontalOffset + getVerticalSpace());

        /**
         * 将滑出屏幕的Items回收到Recycle缓存中
         */
        Rect childFrame = new Rect();
        for (int i = 0; i < getChildCount(); i++) {
            View child = getChildAt(i);
            childFrame.left = getDecoratedLeft(child);
            childFrame.top = getDecoratedTop(child);
            childFrame.right = getDecoratedRight(child);
            childFrame.bottom = getDecoratedBottom(child);
            //如果Item没有在显示区域，就说明需要回收
            if (!Rect.intersects(displayFrame, childFrame)) {
                //回收掉滑出屏幕的View
                removeAndRecycleView(child, recycler);

            }
        }

        //重新显示需要出现在屏幕的子View
        for (int i = 0; i < getItemCount(); i++) {

            if (Rect.intersects(displayFrame, mItemRects.get(i))) {

                View scrap = recycler.getViewForPosition(i);
                measureChild(scrap, getItemWidth(i), getItemHeight(i));
                addView(scrap);

                Rect frame = mItemRects.get(i);
                layoutDecorated(scrap,
                        frame.left - mHorizontalOffset,
                        frame.top,
                        frame.right - mHorizontalOffset,
                        frame.bottom);
            }
        }
    }

    public void measureChild(View child, int childWidth, int childHeight) {
        final ViewGroup.MarginLayoutParams lp = (ViewGroup.MarginLayoutParams) child.getLayoutParams();
        int width = Math.max(0, childWidth - lp.leftMargin - lp.rightMargin);
        int height = Math.max(0, childHeight - lp.topMargin - lp.bottomMargin);
        int childWidthSpec = View.MeasureSpec.makeMeasureSpec(width, View.MeasureSpec.EXACTLY);
        int childHeightSpec = View.MeasureSpec.makeMeasureSpec(height, View.MeasureSpec.EXACTLY);
        child.measure(childWidthSpec, childHeightSpec);
    }

    @Override
    public boolean canScrollHorizontally() {
        return true;
    }

    @Override
    public int scrollHorizontallyBy(int dx, RecyclerView.Recycler recycler, RecyclerView.State state) {
        //位移0、没有子View 当然不移动
        if (dx == 0 || getChildCount() == 0) {
            return 0;
        }
        Log.i(TAG, "scrollHorizontallyBy: ===dx==" + dx + "===getChildCount==" + getChildCount());
        int realOffset = dx; //实际滑动的距离， 可能会在边界处被修复
        //边界修复代码
        if (realOffset > 0) {
            if (mLastVisitPos <= getItemCount() - 1) {
                View lastChild = getChildAt(mLastVisitPos);
                int gap = getWidth() - getPaddingRight() - getDecoratedRight(lastChild);
                if (gap > 0) {
                    realOffset = -gap;
                } else if (gap == 0) {
                    realOffset = 0;
                } else {
                    realOffset = Math.min(realOffset, -gap);
                }
            }
        }
        realOffset = fill(recycler, state, realOffset);//先填充，再位移。
        //mHorizontalOffset += realOffset;//累加实际滑动距离
        offsetChildrenHorizontal(-realOffset);//滑动
        return realOffset;
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
        if (position == 5) {
            return 8;
        }
        if (position == 6) {
            return 9;
        }
        if (position == 7) {
            return 10;
        }
        if (position == 8) {
            return 12;
        }
        if (position == 9) {
            return 14;
        }
        if (position == 10) {
            return 15;
        }
        if (position == 11) {
            return 16;
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
        if (position == 5) {
            return 1;
        }
        if (position == 6) {
            return 1;
        }
        if (position == 7) {
            return 2;
        }
        if (position == 8) {
            return 2;
        }
        if (position == 9) {
            return 1;
        }
        if (position == 10) {
            return 1;
        }
        if (position == 11) {
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
        //Log.i(TAG, "getDecoratedMeasurementHorizontal: ==leftMargin==" + params.leftMargin + "==rightMargin==" + params.rightMargin);
        return getDecoratedMeasuredWidth(view) + params.leftMargin
                + params.rightMargin;
    }

    private int getDecoratedMeasurementVertical(View view) {
        final RecyclerView.LayoutParams params = (RecyclerView.LayoutParams)
                view.getLayoutParams();
        //Log.i(TAG, "getDecoratedMeasurementVertical: ==topMargin==" + params.topMargin + "==bottomMargin==" + params.bottomMargin);
        return getDecoratedMeasuredHeight(view) + params.topMargin
                + params.bottomMargin;
    }

    private int getVerticalSpace() {
        return getHeight() - getPaddingTop() - getPaddingBottom();
    }

    private int getHorizontalSpace() {
        return getWidth() - getPaddingLeft() - getPaddingRight();
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