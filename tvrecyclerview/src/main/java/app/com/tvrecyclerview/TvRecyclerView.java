package app.com.tvrecyclerview;


import android.app.Activity;
import android.content.Context;
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

public class TvRecyclerView extends RecyclerView {

    private static final String TAG = "TvRecyclerView";

    private FlyBorderView mFlyBorderView;
    protected Drawable mDrawableFocus;
    protected boolean mIsDrawFocusMoveAnim;
    protected float mFocusMoveAnimScale;

    public TvRecyclerView(Context context) {
        this(context, null);
    }

    public TvRecyclerView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public TvRecyclerView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
        setFocusable(true);
    }

    private void init() {
        mDrawableFocus = ContextCompat.getDrawable(getContext(), R.drawable.focus_2);
        mIsDrawFocusMoveAnim = false;
        mFocusMoveAnimScale = 1;
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        addFlyBorderView(getContext());
    }

    private void addFlyBorderView(Context context) {
        if (null == mFlyBorderView) {
            mFlyBorderView = new FlyBorderView(context, this);
            ((Activity)context).getWindow().addContentView(mFlyBorderView,
                    new LayoutParams(200, 200));
        }
        Log.i(TAG, "addFlyBorderView: [ " + hashCode() + " ]");
    }

    @Override
    protected void dispatchDraw(Canvas canvas) {
        super.dispatchDraw(canvas);
        if (mFlyBorderView != null) {
            mFlyBorderView.invalidate();
        }
        Log.i(TAG, "dispatchDraw: [ " + hashCode() + " ]");
    }

    @Override
    protected void onFocusChanged(boolean gainFocus, int direction, Rect previouslyFocusedRect) {
        super.onFocusChanged(gainFocus, direction, previouslyFocusedRect);
        Log.i(TAG, "onFocusChanged: gainFocus==" + gainFocus + "==direction==" + direction);
        if (gainFocus) {
            mFlyBorderView.bringToFront();
            int focusPos = mFlyBorderView.getFocusPos();
            if (focusPos < 0) {
                ViewGroup.LayoutParams layoutParams = mFlyBorderView.getLayoutParams();
                layoutParams.width = getChildAt(0).getWidth() + 100;
                layoutParams.height = getChildAt(0).getHeight() + 100;
                mFlyBorderView.setLayoutParams(layoutParams);
                focusPos = 0;
            }
            mFlyBorderView.setFocusView(getChildAt(focusPos), focusPos);
            mFlyBorderView.setVisibility(View.VISIBLE);
        } else {
            mFlyBorderView.setVisibility(View.INVISIBLE);
        }
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        if (event.getAction() == KeyEvent.ACTION_DOWN) {
            int keyCode = event.getKeyCode();
            View nextFocused = null;
            if (keyCode == KeyEvent.KEYCODE_DPAD_LEFT) {
                nextFocused = FocusFinder.getInstance().findNextFocus(this, findFocus(), View.FOCUS_LEFT);
            } else if (keyCode == KeyEvent.KEYCODE_DPAD_RIGHT) {
                nextFocused = FocusFinder.getInstance().findNextFocus(this, findFocus(), View.FOCUS_RIGHT);
            } else if (keyCode == KeyEvent.KEYCODE_DPAD_UP) {
                nextFocused = FocusFinder.getInstance().findNextFocus(this, findFocus(), View.FOCUS_UP);
            } else if (keyCode == KeyEvent.KEYCODE_DPAD_DOWN) {
                nextFocused = FocusFinder.getInstance().findNextFocus(this, findFocus(), View.FOCUS_DOWN);
            }

            if (nextFocused != null) {
                mFlyBorderView.setFocusView(nextFocused, getChildAdapterPosition(nextFocused));
            }

        }
        return super.dispatchKeyEvent(event);
    }
}
