package com.idreamo.rrtoyewx.sticklayoutlibrary;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.PointF;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.widget.LinearLayout;

/**
 * Created by Rrtoyewx on 16/3/13.
 */
public class StickLayout extends LinearLayout {
    public static final String TAG = "StickLayout";
    public static final boolean debug = true;

    private View mHeaderStickView;
    private View mFooterStickView;
    private View mContentView;

    private int mTouchSlop;
    private ValueAnimator mHeaderReboundAnimator;
    private ValueAnimator mFooterReboundAnimator;

    private boolean mIsHeader = false;
    private boolean mIsFooter = false;

    private PointF mPrePoint;
    private PointF mCurPoint;

    public StickLayout(Context context) {
        this(context, null, 0);
    }

    public StickLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public StickLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        View view = LayoutInflater.from(context).inflate(R.layout.total_sticklayout, null);
        this.addView(view);

        init(context);
    }

    private void init(Context context) {
        mTouchSlop = ViewConfiguration.get(context).getScaledTouchSlop();
        mPrePoint = new PointF();
        mCurPoint = new PointF();

    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        mHeaderStickView = findViewById(R.id.sticklayout_header);
        mFooterStickView = findViewById(R.id.sticklayout_footer);
        mContentView = findViewById(R.id.sticklayout_content);

        setViewLayoutParams(mHeaderStickView, 0);
        setViewLayoutParams(mFooterStickView, 0);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int action = event.getAction() & MotionEvent.ACTION_MASK;
        switch (action) {
            case MotionEvent.ACTION_DOWN:
            case MotionEvent.ACTION_CANCEL:
                restore();

                mPrePoint.x = event.getX();
                mPrePoint.y = event.getY();
                break;
            case MotionEvent.ACTION_MOVE:
                mCurPoint.x = event.getX();
                mCurPoint.y = event.getY();
                float calculationDistance = calculation();

                if (mIsHeader) {
                    setViewLayoutParams(mHeaderStickView, calculationDistance);
                }

                if (mIsFooter) {
                    setViewLayoutParams(mFooterStickView, -calculationDistance);
                }

                break;
            case MotionEvent.ACTION_UP:
                mCurPoint.x = event.getX();
                mCurPoint.y = event.getY();
                calculation();
                if (mIsHeader) {
                    startHeaderReboundAnimation();
                }

                if (mIsFooter) {
                    startFooterReboundAnimation();
                }

                break;
        }
        return true;
    }

    private float calculation() {
        float distanceX = mCurPoint.x - mPrePoint.x;
        float distanceY = mCurPoint.y - mPrePoint.y;
        if (Math.abs(distanceY) > Math.abs(distanceX) && Math.abs(distanceY) > mTouchSlop) {
            mIsHeader = (distanceY > 0);
            mIsFooter = !(distanceY > 0);
            return distanceY;
        }
        return 0;
    }

    private void startHeaderReboundAnimation() {
        ViewGroup.LayoutParams layoutParams = mHeaderStickView.getLayoutParams();
        float height = layoutParams.height;
        mHeaderReboundAnimator = ValueAnimator.ofFloat(height, 0).setDuration(1000);
        mHeaderReboundAnimator.start();
        mHeaderReboundAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float animatedFraction = animation.getAnimatedFraction();
                float animatedValue = (float) animation.getAnimatedValue();

                setViewLayoutParams(mHeaderStickView, (animatedFraction <= 0) ? 0 : animatedValue);
            }
        });
    }

    private void startFooterReboundAnimation() {
        ViewGroup.LayoutParams layoutParams = mFooterStickView.getLayoutParams();
        float height = layoutParams.height;
        mFooterReboundAnimator = ValueAnimator.ofFloat(height, 0).setDuration(1000);
        mFooterReboundAnimator.start();
        mFooterReboundAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float animatedFraction = animation.getAnimatedFraction();
                float animatedValue = (float) animation.getAnimatedValue();

                setViewLayoutParams(mFooterStickView, (animatedFraction <= 0) ? 0 : animatedValue);
            }
        });
    }


    private void setViewLayoutParams(View view, float height) {
        ViewGroup.LayoutParams layoutParams = view.getLayoutParams();
        layoutParams.height = (int) height;
        view.setLayoutParams(layoutParams);
    }


    private void restore() {
        if (mPrePoint != null) {
            mPrePoint.x = 0;
            mPrePoint.y = 0;
        }

        if (mCurPoint != null) {
            mCurPoint.x = 0;
            mCurPoint.y = 0;
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        if (mHeaderReboundAnimator != null) {
            mHeaderReboundAnimator.cancel();
            mHeaderReboundAnimator = null;
        }

        if (mFooterReboundAnimator != null) {
            mFooterReboundAnimator.cancel();
            mFooterReboundAnimator = null;
        }
        super.onDetachedFromWindow();
    }
}
