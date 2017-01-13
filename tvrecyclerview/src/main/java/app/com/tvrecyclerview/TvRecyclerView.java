package app.com.tvrecyclerview;


import android.app.Activity;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.util.Log;
import android.view.FocusFinder;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Scroller;

public class TvRecyclerView extends RecyclerView {

    public static final String TAG = "TvRecyclerView";

    private FocusBorderView mFocusBorderView;

    protected Drawable mDrawableFocus;
    public boolean mIsDrawFocusMoveAnim;
    protected float mSelectedScaleValue;
    private float mFocusMoveAnimScale;

    private int mSelectedPosition;
    private View mNextFocused;
    private boolean mInLayout;

    private int mFocusFrameLeft;
    private int mFocusFrameTop;
    private int mFocusFrameRight;
    private int mFocusFrameBottom;

    protected boolean mReceivedInvokeKeyDown;
    protected View mSelectedItem;
    private OnItemStateListener mItemStateListener;
    private Scroller mScrollerFocusMoveAnim;
    private boolean mIsFollowScroll;

    private int mScreenWidth;

    public TvRecyclerView(Context context) {
        this(context, null);
    }

    public TvRecyclerView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public TvRecyclerView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
        setAttributeSet(attrs);
    }

    private void init() {
        mScrollerFocusMoveAnim = new Scroller(getContext());
        mIsDrawFocusMoveAnim = false;
        mReceivedInvokeKeyDown = false;
        mSelectedPosition = 0;
        mNextFocused = null;
        mInLayout = false;
        mIsFollowScroll = false;

        mFocusFrameLeft = 22;
        mFocusFrameTop = 22;
        mFocusFrameRight = 22;
        mFocusFrameBottom = 22;
        mScreenWidth = getContext().getResources().getDisplayMetrics().widthPixels;
        setDescendantFocusability(ViewGroup.FOCUS_BEFORE_DESCENDANTS);
    }

    private void setAttributeSet(AttributeSet attrs) {
        TypedArray typeArray = getContext().obtainStyledAttributes(attrs, R.styleable.TvRecyclerView);
        int type = typeArray.getInteger(R.styleable.TvRecyclerView_scrollMode, 0);
        if (type == 1) {
            mIsFollowScroll = true;
        }
        final Drawable drawable = typeArray.getDrawable(R.styleable.TvRecyclerView_focusDrawable);
        if (drawable != null) {
            setFocusDrawable(drawable);
        }

        mSelectedScaleValue = typeArray.getFloat(R.styleable.TvRecyclerView_focusScale, 1.04f);
        mFocusMoveAnimScale = typeArray.getFloat(R.styleable.TvRecyclerView_moveScale, 1.0f);

        boolean isFocus = typeArray.getBoolean(R.styleable.TvRecyclerView_isInterceptFocus, true);
        if (isFocus && drawable == null) {
            mDrawableFocus = ContextCompat.getDrawable(getContext(), R.drawable.default_focus);
        }
        if (!isFocus) {
            mSelectedScaleValue = 1.0f;
            mFocusMoveAnimScale = 1.0f;
        }
        typeArray.recycle();
    }

    private void addFlyBorderView(Context context) {
        if (mFocusBorderView == null) {
            mFocusBorderView = new FocusBorderView(context);
            ((Activity) context).getWindow().addContentView(mFocusBorderView,
                    new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
            mFocusBorderView.setSelectPadding(mFocusFrameLeft, mFocusFrameTop,
                    mFocusFrameRight, mFocusFrameBottom);
        }
    }

    public void setOnItemStateListener(OnItemStateListener listener) {
        mItemStateListener = listener;
    }

    public OnItemStateListener getOnItemStateListener() {
        return mItemStateListener;
    }

    public void setFocusDrawable(Drawable focusDrawable) {
        mDrawableFocus = focusDrawable;
    }

    /**
     * When call this method, you must ensure that the location of the view has been inflate
     * @param position selected item position
     */
    public void setItemSelected(int position) {
        View selectedView = getChildAt(position);
        if (selectedView == null) {
            return;
        }
        mSelectedItem = selectedView;
        mSelectedPosition = position;
        boolean visibleChild = isVisibleChild(mSelectedItem);
        boolean halfVisibleChild = isHalfVisibleChild(mSelectedItem);
        if (!visibleChild || halfVisibleChild) {
            int dx;
            if (mSelectedItem.getLeft() > getWidth() / 2) {
                dx = mSelectedItem.getLeft() + mSelectedItem.getWidth() / 2 - mScreenWidth / 2;
                smoothScrollBy(dx, 0);
            } else {
                dx = mSelectedItem.getRight() + mSelectedItem.getWidth() / 2 - mScreenWidth / 2;
                smoothScrollBy(dx, 0);
            }
        }
        mFocusBorderView.startFocusAnim();
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        addFlyBorderView(getContext());
    }

    @Override
    protected void onFocusChanged(boolean gainFocus, int direction, Rect previouslyFocusedRect) {
        super.onFocusChanged(gainFocus, direction, previouslyFocusedRect);
        if (mFocusBorderView == null) {
            return;
        }
        mFocusBorderView.setTvRecyclerView(this);
        if (gainFocus) {
            mFocusBorderView.bringToFront();
        }
        if (mSelectedItem != null) {
            if (gainFocus) {
                mSelectedItem.setSelected(true);
            } else {
                mSelectedItem.setSelected(false);
            }
            if (gainFocus && !mInLayout) {
                mFocusBorderView.startFocusAnim();
            }
        }
        if (!gainFocus) {
            mFocusBorderView.dismissFocus();
        }
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        mInLayout = true;
        super.onLayout(changed, l, t, r, b);
        mInLayout = false;
    }

    @Override
    protected void dispatchDraw(Canvas canvas) {
        super.dispatchDraw(canvas);
        if (mFocusBorderView != null && mFocusBorderView.getTvRecyclerView() != null) {
            mFocusBorderView.invalidate();
        }
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        if (event.getAction() == KeyEvent.ACTION_DOWN) {
            int keyCode = event.getKeyCode();
            if (mSelectedItem == null) {
                mSelectedItem = getChildAt(mSelectedPosition);
            }
            try {
                if (keyCode == KeyEvent.KEYCODE_DPAD_LEFT) {
                    mNextFocused = FocusFinder.getInstance().findNextFocus(this, mSelectedItem, View.FOCUS_LEFT);
                } else if (keyCode == KeyEvent.KEYCODE_DPAD_RIGHT) {
                    mNextFocused = FocusFinder.getInstance().findNextFocus(this, mSelectedItem, View.FOCUS_RIGHT);
                } else if (keyCode == KeyEvent.KEYCODE_DPAD_UP) {
                    mNextFocused = FocusFinder.getInstance().findNextFocus(this, mSelectedItem, View.FOCUS_UP);
                } else if (keyCode == KeyEvent.KEYCODE_DPAD_DOWN) {
                    mNextFocused = FocusFinder.getInstance().findNextFocus(this, mSelectedItem, View.FOCUS_DOWN);
                }
            } catch (Exception e) {
                Log.i(TAG, "dispatchKeyEvent: get next focus item error: " + e.getMessage());
                mNextFocused = null;
            }
        }
        return super.dispatchKeyEvent(event);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_DPAD_LEFT:
            case KeyEvent.KEYCODE_DPAD_UP:
            case KeyEvent.KEYCODE_DPAD_RIGHT:
            case KeyEvent.KEYCODE_DPAD_DOWN:
                if (processMoves(keyCode)) {
                    return true;
                }
                break;

            case KeyEvent.KEYCODE_DPAD_CENTER:
            case KeyEvent.KEYCODE_ENTER:
                mReceivedInvokeKeyDown = true;
                break;

            default:
                break;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_DPAD_CENTER:
            case KeyEvent.KEYCODE_ENTER: {
                if (mReceivedInvokeKeyDown) {
                    if ((getAdapter() != null) && (mSelectedItem != null)) {
                        if (mItemStateListener != null) {
                            mFocusBorderView.startClickAnim();
                            mItemStateListener.onItemViewClick(mSelectedItem, mSelectedPosition);
                        }
                    }
                }
                mReceivedInvokeKeyDown = false;
                return true;
            }
        }
        return super.onKeyUp(keyCode, event);
    }

    @Override
    public void computeScroll() {
        if (mScrollerFocusMoveAnim.computeScrollOffset()) {
            if (mIsDrawFocusMoveAnim) {
                mFocusMoveAnimScale = ((float) (mScrollerFocusMoveAnim.getCurrX())) / 100;
            }
            postInvalidate();
        } else {
            if (mIsDrawFocusMoveAnim) {
                if (mNextFocused != null) {
                    mSelectedItem = mNextFocused;
                    mSelectedPosition = indexOfChild(mSelectedItem);
                }
                mIsDrawFocusMoveAnim = false;
                setLayerType(View.LAYER_TYPE_SOFTWARE, null);
                postInvalidate();
                if (mItemStateListener != null) {
                    mItemStateListener.onItemViewFocused(mSelectedItem, mSelectedPosition);
                }
            }
        }
    }

    private boolean processMoves(int keycode) {
        if (mNextFocused == null || !hasFocus()) {
            return false;
        } else {
            if (mIsDrawFocusMoveAnim) {
                return true;
            }
            if (mNextFocused == null) {
                return false;
            }

            if (!mIsFollowScroll) {
                boolean isVisible = isVisibleChild(mNextFocused);
                boolean isHalfVisible = isHalfVisibleChild(mNextFocused);
                if (isHalfVisible || !isVisible) {
                    if (keycode == KeyEvent.KEYCODE_DPAD_RIGHT) {
                        smoothScrollBy(getWidth() / 2, 0);
                    } else if (keycode == KeyEvent.KEYCODE_DPAD_LEFT) {
                        smoothScrollBy(-getWidth() / 2, 0);
                    }
                }
            } else {
                boolean isOver = isOverHalfScreen(mSelectedItem, keycode);
                if (isOver) {
                    if (keycode == KeyEvent.KEYCODE_DPAD_RIGHT) {
                        int dx = mNextFocused.getLeft() +
                                mNextFocused.getWidth() / 2 - mScreenWidth / 2;
                        smoothScrollBy(dx, 0);
                    } else if (keycode == KeyEvent.KEYCODE_DPAD_LEFT) {
                        int dx = mNextFocused.getRight() -
                                mNextFocused.getWidth() / 2 - mScreenWidth / 2;
                        smoothScrollBy(dx, 0);
                    }
                }
            }
            startFocusMoveAnim();
            return true;
        }
    }

    private boolean isHalfVisibleChild(View child) {
        if (child != null) {
            Rect ret = new Rect();
            boolean isVisible = child.getLocalVisibleRect(ret);
            if (isVisible && (ret.width() < child.getWidth())) {
                return true;
            }
        }
        return false;
    }

    private boolean isVisibleChild(View child) {
        if (child != null) {
            Rect ret = new Rect();
            return child.getLocalVisibleRect(ret);
        }
        return false;
    }

    private boolean isOverHalfScreen(View child, int keycode) {
        Rect ret = new Rect();
        boolean visibleRect = child.getGlobalVisibleRect(ret);
        if (visibleRect && keycode == KeyEvent.KEYCODE_DPAD_RIGHT) {
            if (ret.right > mScreenWidth / 2) {
                return true;
            }
        } else if (visibleRect && keycode == KeyEvent.KEYCODE_DPAD_LEFT) {
            if (ret.left < mScreenWidth / 2) {
                return true;
            }
        }
        return false;
    }

    private void startFocusMoveAnim() {
        setLayerType(View.LAYER_TYPE_NONE, null);
        mIsDrawFocusMoveAnim = true;
        mScrollerFocusMoveAnim.startScroll(0, 0, 100, 100, 200);
        invalidate();
    }

    public int getSelectedPosition() {
        return mSelectedPosition;
    }

    public float getSelectedScaleValue() {
        return mSelectedScaleValue;
    }

    public Drawable getDrawableFocus() {
        return mDrawableFocus;
    }

    public View getNextFocusView() {
        return mNextFocused;
    }

    public float getFocusMoveAnimScale() {
        return mFocusMoveAnimScale;
    }

    public void setSelectPadding(int left, int top, int right, int bottom) {
        mFocusFrameLeft = left;
        mFocusFrameTop = top;
        mFocusFrameRight = right;
        mFocusFrameBottom = bottom;

        if (null != mFocusBorderView) {
            mFocusBorderView.setSelectPadding(mFocusFrameLeft, mFocusFrameTop,
                    mFocusFrameRight, mFocusFrameBottom);
        }
    }

    public interface OnItemStateListener {
        void onItemViewClick(View view, int position);
        void onItemViewFocused(View view, int position);
    }
}
