package app.com.tvrecyclerview;

import android.graphics.PointF;
import android.graphics.Rect;
import android.support.v7.widget.OrientationHelper;
import android.support.v7.widget.RecyclerView;
import android.util.SparseArray;
import android.view.View;
import android.view.ViewGroup;


public abstract class ModuleLayoutManager extends RecyclerView.LayoutManager implements
        RecyclerView.SmoothScroller.ScrollVectorProvider {

    private static final String TAG = "ModuleLayoutManager";

    private static final int BASE_ITEM_DEFAULT_SIZE = 380;

    private static final int HORIZONTAL = OrientationHelper.HORIZONTAL;

    private static final int VERTICAL = OrientationHelper.VERTICAL;

    private final static int LAYOUT_START = -1;

    private final static int LAYOUT_END = 1;

    private int mOrientation;
    private SparseArray<Rect> mItemsRect;

    private int mHorizontalOffset;

    private int mNumRows;

    private final int mOriItemWidth;
    private final int mOriItemHeight;
    private int mTotalWidth;
    // re-used variable to acquire decor insets from RecyclerView
    private final Rect mDecorInsets = new Rect();

    public ModuleLayoutManager(int rowCount, int orientation) {
        mOrientation = orientation;
        mOriItemWidth = BASE_ITEM_DEFAULT_SIZE;
        mOriItemHeight = BASE_ITEM_DEFAULT_SIZE;
        mNumRows = rowCount;
        mItemsRect = new SparseArray<>();
    }

    public ModuleLayoutManager(int rowCount, int orientation, int baseItemWidth, int baseItemHeight) {
        mOrientation = orientation;
        mOriItemWidth = baseItemWidth;
        mOriItemHeight = baseItemHeight;
        mNumRows = rowCount;
        mItemsRect = new SparseArray<>();
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

        detachAndScrapAttachedViews(recycler);

        mHorizontalOffset = 0;
        fill(recycler, state);
    }

    private void fill(RecyclerView.Recycler recycler, RecyclerView.State state) {
        int leftOffset;
        int topOffset;
        for (int i = 0; i < getItemCount(); i++) {
            View child = recycler.getViewForPosition(i);
            calculateItemDecorationsForChild(child, mDecorInsets);
            addView(child);
            measureChild(child, getItemWidth(i), getItemHeight(i));

            // change leftOffset
            int itemStartPos = getItemStartIndex(i);
            int lastPos = itemStartPos / mNumRows;
            if (lastPos == 0) {
                leftOffset = getDecoratedMeasurementHorizontal(child) * lastPos ;
            } else {
                leftOffset = (mOriItemWidth + getChildHorizontalPadding(child)) * lastPos;
            }
            int topPos = itemStartPos % mNumRows;
            if (topPos == 0) {
                topOffset = getDecoratedMeasurementVertical(child) * topPos + getPaddingTop();
            } else {
                topOffset = getDecoratedMeasurementVertical(child) * topPos;
            }

            //calculate width includes margin
            if (topOffset + getDecoratedMeasurementVertical(child) <= getVerticalSpace()) {
                layoutDecoratedWithMargins(
                        child,
                        leftOffset,
                        topOffset,
                        leftOffset + getDecoratedMeasurementHorizontal(child),
                        topOffset + getDecoratedMeasurementVertical(child));
                mTotalWidth = leftOffset + getDecoratedMeasurementHorizontal(child);
                Rect frame = mItemsRect.get(i);
                if (frame == null) {
                    frame = new Rect();
                }
                frame.set(leftOffset + mHorizontalOffset,
                        topOffset,
                        leftOffset + getDecoratedMeasurementHorizontal(child),
                        topOffset + getDecoratedMeasurementVertical(child));
                // Save the current Bound field data for the item view
                mItemsRect.put(i, frame);
            }
        }
    }

    private void recycleAndFillItems(RecyclerView.Recycler recycler, RecyclerView.State state) {
        if (state.isPreLayout()) {
            return;
        }

        Rect displayFrame = new Rect(mHorizontalOffset, 0,
                mHorizontalOffset + getHorizontalSpace(), getVerticalSpace());

        //item view that slide out of the screen will return Recycle cache
        Rect childFrame = new Rect();
        int childCount = getChildCount();
        for (int i = 0; i < childCount; i++) {
            View child = getChildAt(i);
            childFrame.left = getDecoratedLeft(child);
            childFrame.top = getDecoratedTop(child);
            childFrame.right = getDecoratedRight(child);
            childFrame.bottom = getDecoratedBottom(child);
            if (!Rect.intersects(displayFrame, childFrame)) {
                removeAndRecycleView(child, recycler);
            }
        }

        //Re-display the subview that needs to appear on the screen
        for (int i = 0; i < getItemCount(); i++) {
            if (Rect.intersects(displayFrame, mItemsRect.get(i))) {
                View scrap = recycler.getViewForPosition(i);
                measureChild(scrap, getItemWidth(i), getItemHeight(i));
                addView(scrap);
                Rect frame = mItemsRect.get(i);
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
        if (dx == 0 || getChildCount() == 0) {
            return 0;
        }
        detachAndScrapAttachedViews(recycler);

        int realOffset = dx;
        if (mHorizontalOffset + dx < 0) {
            realOffset -= mHorizontalOffset;

        } else if (mHorizontalOffset + dx > mTotalWidth - getHorizontalSpace()){
            realOffset = mTotalWidth - getHorizontalSpace() - mHorizontalOffset;
        }
        mHorizontalOffset += realOffset;

        offsetChildrenHorizontal(realOffset);
        recycleAndFillItems(recycler, state);
        return realOffset;
    }

    private int getItemWidth(int position) {
        int itemColumnSize = getItemColumnSize(position);
        return itemColumnSize * mOriItemWidth
                + (itemColumnSize - 1) * (mDecorInsets.left + mDecorInsets.right);
    }

    private int getItemHeight(int position) {
        int itemRowSize = getItemRowSize(position);
        return itemRowSize * mOriItemHeight
                + (itemRowSize - 1) * (mDecorInsets.bottom + mDecorInsets.top);
    }

    protected abstract int getItemStartIndex(int position);

    protected abstract int getItemRowSize(int position);

    protected abstract int getItemColumnSize(int position);

    private int getDecoratedMeasurementHorizontal(View child) {
        final RecyclerView.MarginLayoutParams params = (RecyclerView.LayoutParams)
                child.getLayoutParams();
        return getDecoratedMeasuredWidth(child) + params.leftMargin
                + params.rightMargin;
    }

    private int getChildHorizontalPadding(View child) {
        final RecyclerView.MarginLayoutParams params = (RecyclerView.LayoutParams)
                child.getLayoutParams();
        return getDecoratedMeasuredWidth(child) + params.leftMargin
                + params.rightMargin - child.getMeasuredWidth();
    }

    private int getDecoratedMeasurementVertical(View view) {
        final RecyclerView.MarginLayoutParams params = (RecyclerView.LayoutParams)
                view.getLayoutParams();
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

    private int getFirstChildPosition() {
        final int childCount = getChildCount();
        return childCount == 0 ? 0 : getPosition(getChildAt(0));
    }

    private int calculateScrollDirectionForPosition(int position) {
        if (getChildCount() == 0) {
            return LAYOUT_START;
        }
        final int firstChildPos = getFirstChildPosition();
        return position < firstChildPos ? LAYOUT_START : LAYOUT_END;
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