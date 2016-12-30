package app.com.tvrecyclerview;

import android.content.Context;
import android.graphics.Rect;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;

public class FlyBorderView extends View {

    private static final String TAG = "FlyBorderView";
    private static final int TRAN_DUR_ANIM = 150;

    private TvRecyclerView mTvRecyclerView;
    private View mFocusView;
    private View mSelectView;
    private int mFocusedPos = -1;
    private boolean mIsSelected = false;

    public FlyBorderView(Context context) {
        super(context);
        init();
    }

    public FlyBorderView(Context context, TvRecyclerView tvRecyclerView) {
        super(context);
        mTvRecyclerView = tvRecyclerView;
        init();
    }

    private void init() {
        setBackgroundResource(R.drawable.focus_2);
    }

    public TvRecyclerView getTvRecyclerView() {
        return mTvRecyclerView;
    }

    public void setTvRecyclerView(TvRecyclerView tvRecyclerView) {
        mTvRecyclerView = tvRecyclerView;
    }

    public int getFocusPos() {
        return mFocusedPos;
    }

    public void setFocusPos(int pos) {
        if (pos >= 0) {
            mFocusedPos = pos;
        }
    }

    public View getFocusView() {
        return mFocusView;
    }

    public void setFocusView(View view, int position) {
        if (mFocusedPos == -1 || (mFocusedPos != position && position >= 0)) {
            mFocusedPos = position;
            mFocusView = view;
            runTranslateAnimation(mFocusView);
        }
    }

    public void setSelectView(View view, int position) {
        if (mSelectView != view) {
            mSelectView = view;
            mFocusedPos = position;
            mIsSelected = true;

            runTranslateAnimation(mSelectView);
            mIsSelected = false;
        }
    }

    private void runTranslateAnimation(View toView) {
        Rect fromRect = findLocationWithView(this);
        Rect toRect = findLocationWithView(toView);
        int x = toRect.left - fromRect.left;
        int y = toRect.top - fromRect.top;

        int deltaX = (toView.getWidth() - getWidth()) / 2;
        int deltaY = (toView.getHeight() - getHeight()) / 2;

        x = x + deltaX;
        y = y + deltaY;

        float scaleX = toView.getWidth() / getWidth();
        float scaleY = toView.getHeight() / getHeight();
        flyWhiteBorder(x, y, scaleX, scaleY);

    }

    private void flyWhiteBorder(float x, float y, float scaleX, float scaleY) {
        int duration = TRAN_DUR_ANIM;
        if (mIsSelected) {
            duration = 0;
        }

        animate().scaleX(scaleX).scaleY(scaleY).setDuration(duration).
                setInterpolator(new DecelerateInterpolator()).start();
        animate().translationX(x).translationY(y).setDuration(duration).
                setInterpolator(new DecelerateInterpolator()).start();
    }

    public Rect findLocationWithView(View view) {
        ViewGroup root = (ViewGroup) this.getParent();
        Rect rect = new Rect();
        root.offsetDescendantRectToMyCoords(view, rect);
        return rect;
    }
}
