package fingertip.creditease.com.test;


import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;


public class RevealAnimation {
    //动画时间
    private final static int ANIM_DURATION = 500;
    //非动画状态
    private final static int MODE_UNINIT = -1;
    //扩散动画
    private final static int MODE_SPREAD = 0;
    //收缩动画
    private final static int MODE_SHRINK = 1;
    //动画层视图
    private RevealAnimationView revealAnimationView;
    //执行动画的activity
    private Activity mStartActivity;
    //执行动画的activity根视图
    private ViewGroup mDecorView;
    private int mColorStart;
    private int mColorEnd;
    private int targetViewWidth;
    private int targetViewHeight;
    private int[] targetLocation = new int[2];
    //控制返回页面时是否执行动画
    private boolean isNeedBackAnimation;

    public RevealAnimation(Activity activity) {
        mStartActivity = activity;
        revealAnimationView = new RevealAnimationView(mStartActivity);
        mDecorView = (ViewGroup) mStartActivity.getWindow().getDecorView();
    }


    public RevealAnimation animationView(final View view) {
        /**
         * 动画层视图添加到根视图
         */
        final ViewGroup.LayoutParams bgParams = new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        mDecorView.addView(revealAnimationView, bgParams);
        view.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                view.getLocationInWindow(targetLocation);
                targetViewWidth = view.getWidth();
                targetViewHeight = view.getHeight();
//                int circleRadius = (targetViewHeight > targetViewWidth ? targetViewWidth : targetViewHeight) / 2;
//                revealAnimationView.setmTargetCircleRadius(circleRadius);
                revealAnimationView.setCenter(targetLocation[0] + targetViewWidth / 2, targetLocation[1] + targetViewHeight / 2);
            }
        });
        return this;
    }

    public RevealAnimation setAnimType(int type) {
        revealAnimationView.setmAnimType(type);
        return this;
    }

    /**
     * Describe: 设置启动动画的会追颜色
     * <p>
     * Author: lzl
     * <p>
     * Time: 2016/11/9 下午4:24
     */
    public RevealAnimation setStartAnimationColor(int color) {
        mColorStart = color;
        revealAnimationView.setmSpreadColor(color);
        return this;
    }

    /**
     * Describe: 设置返回动画的绘制颜色
     * <p>
     * Author: lzl
     * <p>
     * Time: 2016/11/9 下午4:24
     */
    public RevealAnimation setEndAnimationColor(int color) {
        mColorEnd = color;
        revealAnimationView.setmShrinkColor(color);
        return this;
    }


    /**
     * Describe: 启动页面跳转
     * <p>
     * Author: lzl
     * <p>
     * Time: 2016/11/9 下午1:58
     */
    public RevealAnimation startActivity(final Intent intent, final boolean isNeedFinish) {
        revealAnimationView.setSwitchAnimCallback(new RevealAnimCallback() {
            @Override
            public void onAnimationStart() {
                /**
                 * 动画开始===============
                 */
                switch (revealAnimationView.getmAnimType()) {
                    case MODE_SPREAD:
                        revealAnimationView.setClickable(true);
                        break;

                    case MODE_SHRINK:
                        revealAnimationView.setClickable(false);
                        break;
                }
            }

            @Override
            public void onAnimationEnd() {
                /**
                 * 动画结束==================
                 */
                switch (revealAnimationView.getmAnimType()) {
                    case MODE_SPREAD:
                        mStartActivity.startActivity(intent);
                        //禁止系统切换动画
                        mStartActivity.overridePendingTransition(0, 0);
                        if (isNeedFinish) {
                            mStartActivity.finish();
                        } else {
                            mDecorView.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    if (isNeedBackAnimation) {
                                        revealAnimationView.resetAnimParam();
                                        mDecorView.removeView(revealAnimationView);
                                    } else {
                                        /**
                                         * 扩张动画执行完成后，设置状态为返回执行收缩动画
                                         */
                                        isNeedBackAnimation = true;
                                    }
                                }
                            }, 200);
                        }
                        break;

                    case MODE_SHRINK:

                        break;
                }

            }

            @Override
            public void onAnimationUpdate(int progress) {

            }
        });
        return this;
    }

    /**
     * Describe: 启动动画
     * <p>
     * Author: lzl
     * <p>
     * Time: 2016/11/9 下午2:57
     */
    public void start() {
        if (revealAnimationView.getmAnimType() == MODE_SPREAD) {
            /**
             * 启动扩散动画
             */
            revealAnimationView.startSpreadAnim();
        } else if (revealAnimationView.getmAnimType() == MODE_SHRINK) {
            /**
             * 启动收缩动画
             */
            revealAnimationView.startShrinkAnim();
        }
    }

    public RevealAnimation setCustomEndCallBack(RevealAnimCallback callback) {
        revealAnimationView.setSwitchAnimCallback(callback);
        return this;
    }

    public RevealAnimation addContainerView(final View view, final RevealAnimCallback callback) {
        revealAnimationView.setSwitchAnimCallback(new RevealAnimCallback() {
            @Override
            public void onAnimationStart() {
                switch (revealAnimationView.getmAnimType()) {
                    case MODE_SPREAD:
                        revealAnimationView.setClickable(true);
                        break;

                    case MODE_SHRINK:
                        revealAnimationView.setClickable(false);
                        break;
                }
            }

            @Override
            public void onAnimationEnd() {
                if (view.getParent() != null)
                    return;
                final ViewGroup.LayoutParams layoutParams = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
                switch (revealAnimationView.getmAnimType()) {
                    case MODE_SPREAD:
                        mDecorView.addView(view);
                        callback.onAnimationEnd();
                        break;

                    case MODE_SHRINK:
                        revealAnimationView.setSwitchAnimCallback(null);
                        break;
                }
            }

            @Override
            public void onAnimationUpdate(int progress) {
                callback.onAnimationUpdate(progress);
            }
        });
        return this;
    }

    public RevealAnimation removeContainerView(View view) {
        if (mDecorView != null && view != null) {
            mDecorView.removeView(view);
        }
        return this;
    }


    public boolean isShrinkBack() {
        return isNeedBackAnimation;
    }

    /**
     * Describe: 设置是否需要返回动画:扩张动画未执行前设置为false，避免刚进入activity在onResume触发
     * <p>
     * Author: lzl
     * <p>
     * Time: 2016/11/9 下午2:52
     */
    public RevealAnimation setIsNeedBackAnimation(boolean isShrinkBack) {
        isNeedBackAnimation = isShrinkBack;
        return this;
    }

    public boolean getIsNeedBackAnimation() {
        return isNeedBackAnimation;
    }

    public RevealAnimation setIsWaitingResume(boolean isWaitingResume) {
        this.isNeedBackAnimation = isWaitingResume;
        return this;
    }

    public int getTargetViewWidth() {
        return targetViewWidth;
    }

    public void setTargetViewWidth(int targetViewWidth) {
        this.targetViewWidth = targetViewWidth;
    }

    public int getTargetViewHeight() {
        return targetViewHeight;
    }

    public void setTargetViewHeight(int targetViewHeight) {
        this.targetViewHeight = targetViewHeight;
    }

    /**
     * Describe: 动画层视图
     * <p>
     * Author: lzl
     * <p>
     * Time: 2016/11/9 下午3:00
     */
    public class RevealAnimationView extends View {

        private int mCenterX;
        private int mCenterY;
        private int mSpreadColor;
        private int mShrinkColor;
        private int mAnimType;
        //绘制圆形的半径
        private int mRadius;
        private int mDuration;
        private Paint mSpreadPaint;
        private Paint mShrinkPaint;
        private ValueAnimator mAnimator;
        private int mTargetCircleRadius;
        private int mScreenLength;


        private boolean isAnimationReady = false;

        private RevealAnimCallback mRevealAnimCallback;

        public void setSwitchAnimCallback(RevealAnimCallback callback) {
            mRevealAnimCallback = callback;
        }

        public RevealAnimationView(Context context) {
            this(context, null);
        }

        public RevealAnimationView(Context context, AttributeSet attrs) {
            this(context, attrs, 0);
        }

        public RevealAnimationView(Context context, AttributeSet attrs, int defStyle) {
            super(context, attrs, defStyle);
            initDraw();
            initAnimation();
        }

        /**
         * Describe: 初始化绘制属性
         * <p>
         * Author: lzl
         * <p>
         * Time: 2016/11/9 下午3:23
         */

        private void initDraw() {
            mAnimType = MODE_UNINIT;
            mSpreadPaint = new Paint();
            mSpreadPaint.setStrokeWidth(1);
            mSpreadPaint.setStyle(Paint.Style.STROKE);
            mSpreadPaint.setColor(Color.BLUE);
            mShrinkPaint = new Paint();
            mShrinkPaint.setStrokeWidth(1);
            mShrinkPaint.setStyle(Paint.Style.STROKE);
            mShrinkPaint.setColor(Color.BLUE);
            mDuration = ANIM_DURATION;
            //获取对角线长度
            mScreenLength = (int) Math.sqrt(Math.pow(getResources().getDisplayMetrics().heightPixels, 2)
                    + Math.pow(getResources().getDisplayMetrics().widthPixels, 2));
        }

        /**
         * Describe: 重置动画绘制参数
         * <p>
         * Author: lzl
         * <p>
         * Time: 2016/11/9 下午3:33
         */
        public void resetAnimParam() {
            mAnimType = -1;
            mRadius = 0;
            mSpreadPaint.setStrokeWidth(mRadius);
            mShrinkPaint.setStrokeWidth(mRadius);
            invalidate();
        }

        /**
         * Describe: 初始化动画
         * <p>
         * Author: lzl
         * <p>
         * Time: 2016/11/9 下午3:48
         */
        private void initAnimation() {
            isAnimationReady = true;
            mAnimator = ValueAnimator.ofInt(0, mScreenLength);
            mAnimator.setDuration(mDuration);
            mAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                int lastFactor = 0;

                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    int factor = (int) animation.getAnimatedValue();
                    mRadius = factor;
                    invalidate();
                    if (mRevealAnimCallback != null && lastFactor != factor) {
                        mRevealAnimCallback.onAnimationUpdate(factor * 100 / mScreenLength);
                        lastFactor = factor;
                    }
                }
            });
            mAnimator.addListener(new AnimatorListenerAdapter() {

                @Override
                public void onAnimationStart(Animator animation) {
                    /**
                     * 动画启动
                     */
                    if (mRevealAnimCallback != null) {
                        mRevealAnimCallback.onAnimationStart();
                        if (mAnimType == MODE_SPREAD) {
                            setVisibility(VISIBLE);
                        }
                    }
                }

                @Override
                public void onAnimationEnd(Animator animation) {
                    /**
                     * 动画结束
                     */
                    if (mRevealAnimCallback != null) {
                        mRevealAnimCallback.onAnimationEnd();
                        if (mAnimType == MODE_SHRINK) {
                            setVisibility(GONE);
                        }
                    }
                }
            });
        }

        private void startSpreadAnim() {
            mAnimator.start();
        }

        private void startShrinkAnim() {
            mAnimator.reverse();
        }

        public void setCenter(int centerX, int centerY) {
            mCenterX = centerX;
            mCenterY = centerY;
        }

        @Override
        protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);
            if (getmAnimType() == MODE_UNINIT) {
                return;
            }
            if (!isAnimationReady) {
                initAnimation();
            }
            switch (getmAnimType()) {
                case MODE_SPREAD:
                    mSpreadPaint.setStrokeWidth(mRadius);
                    canvas.drawCircle(mCenterX, mCenterY, mRadius / 2 + mTargetCircleRadius, mSpreadPaint);
                    break;
                case MODE_SHRINK:
                    mShrinkPaint.setStrokeWidth(mRadius);
                    canvas.drawCircle(mCenterX, mCenterY, mRadius / 2 + mTargetCircleRadius, mShrinkPaint);
                    break;
            }
        }

        private void setAlpha(int alpha) {
            mSpreadPaint.setAlpha(alpha);
            mShrinkPaint.setAlpha(alpha);
        }

        public int getmRadius() {
            return mRadius;
        }

        public void setmRadius(int mRadius) {
            this.mRadius = mRadius;
        }

        public int getmDuration() {
            return mDuration;
        }

        public void setmDuration(int mDuration) {
            this.mDuration = mDuration;
            isAnimationReady = false;
        }

        public int getmSpreadColor() {
            return mSpreadColor;
        }

        public void setmSpreadColor(int color) {
            mSpreadPaint.setColor(color);
            this.mSpreadColor = color;
        }

        public int getmShrinkColor() {
            return mShrinkColor;
        }

        public void setmShrinkColor(int color) {
            mShrinkPaint.setColor(color);
            this.mShrinkColor = color;
        }

        public int getmAnimType() {
            return mAnimType;
        }

        public void setmAnimType(int mAnimType) {
            this.mAnimType = mAnimType;
        }

        public int getmTargetCircleRadius() {
            return mTargetCircleRadius;
        }

        public void setmTargetCircleRadius(int mTargetCircleRadius) {
            this.mTargetCircleRadius = mTargetCircleRadius;
        }
    }

    /**
     * Describe: 动画监听事件
     * <p>
     * Author: lzl
     * <p>
     * Time: 2016/11/9 下午3:05
     */

    public interface RevealAnimCallback {
        void onAnimationStart();

        void onAnimationEnd();

        void onAnimationUpdate(int progress);
    }
}

