package com.nikhilpanju.recyclerviewenhanced;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.content.Context;
import android.graphics.Rect;
import android.os.Handler;
import android.os.Vibrator;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.animation.DecelerateInterpolator;
import android.widget.Button;
import android.widget.ListView;
import android.widget.RelativeLayout;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static android.R.attr.button;
import static android.R.attr.y;

public class RecyclerTouchListener implements RecyclerView.OnItemTouchListener, OnActivityTouchListener {
    private static final String TAG = "RecyclerTouchListener";
    private final Handler handler = new Handler();
    private Activity activity;
    //禁止滑动的视图集合
    private List<Integer> unSwipeableRows;
    //保存item中，独立于item点击事件的view
    private List<Integer> independentViews;
    private List<Integer> unClickableRows;
    private List<Integer> optionViews;
    //获取产生滑动到最小值
    private int touchSlop;
    private int minFlingVel;
    private int maxFlingVel;
    private long ANIMATION_STANDARD = 300;
    private long ANIMATION_CLOSE = 150;
    private RecyclerView rView;
    private int bgWidth = 1, bgWidthLeft = 1; // 1 and not 0 to prevent dividing by zero
    private int mDismissAnimationRefCount = 0;
    private float touchedX;
    private float touchedY;
    private boolean isFgSwiping;//标记是否在滑动
    private int mSwipingSlop;//手指滑动到距离
    private VelocityTracker mVelocityTracker;
    private int touchedPosition;//手势触摸到的item位置
    private View touchedView;
    private boolean mPaused;
    private boolean bgVisible, fgPartialViewClicked;
    private int bgVisiblePosition;
    private View bgVisibleView;
    private boolean isRViewScrolling;
    private int heightOutsideRView, screenHeight, screenWidth;
    private boolean mLongClickPerformed;
    private View fgView;//上层视图
    private View bgView;//下层视图
    //view ID
    private int fgViewID;//上部分视图id
    //下部分视图id
    private int bgViewID, bgViewIDLeft;
    //需要进行隐藏当视图
    private ArrayList<Integer> fadeViews;
    private OnRowClickListener mRowClickListener;
    private OnRowLongClickListener mRowLongClickListener;
    private OnSwipeOptionsClickListener mBgClickListener, mBgClickListenerLeft;
    // user choices
    private boolean clickable = false;
    private boolean longClickable = false;
    private boolean swipeable = false, swipeableLeftOptions = false;
    private int LONG_CLICK_DELAY = 800;
    private boolean longClickVibrate;

    private RecyclerTouchListener() {
    }

    public RecyclerTouchListener(Activity activity, RecyclerView recyclerView) {
        this.activity = activity;
        ViewConfiguration vc = ViewConfiguration.get(recyclerView.getContext());
        touchSlop = vc.getScaledTouchSlop();
        minFlingVel = vc.getScaledMinimumFlingVelocity() * 16;
        maxFlingVel = vc.getScaledMaximumFlingVelocity();
        rView = recyclerView;
        bgVisible = false;
        bgVisiblePosition = -1;
        bgVisibleView = null;
        fgPartialViewClicked = false;
        unSwipeableRows = new ArrayList<>();
        unClickableRows = new ArrayList<>();
        independentViews = new ArrayList<>();
        optionViews = new ArrayList<>();
        fadeViews = new ArrayList<>();
        isRViewScrolling = false;


        rView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {

                setEnabled(newState != RecyclerView.SCROLL_STATE_DRAGGING);


                isRViewScrolling = newState != RecyclerView.SCROLL_STATE_IDLE;
            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {

            }
        });
    }

    /**
     * Describe: 设置滑动手势是否可用
     * <p>
     * Author: lzl
     * <p>
     * Time: 2016/10/25 下午5:05
     */
    public void setEnabled(boolean enabled) {
        mPaused = !enabled;
    }


    /**
     * Describe: 设置可以点击，有回调事件
     * <p>
     * Author: lzl
     * <p>
     * Time: 2016/10/25 下午4:24
     */
    public RecyclerTouchListener setClickable(OnRowClickListener listener) {
        this.clickable = true;
        this.mRowClickListener = listener;
        return this;
    }

    /**
     * Describe: 设置可以点击
     * <p>
     * Author: lzl
     * <p>
     * Time: 2016/10/25 下午5:11
     */
    public RecyclerTouchListener setClickable(boolean clickable) {
        this.clickable = clickable;
        return this;
    }

    /**
     * Describe: 设置是否可以长按，有回调
     * <p>
     * Author: lzl
     * <p>
     * Time: 2016/10/25 下午4:25
     */
    public RecyclerTouchListener setLongClickable(boolean vibrate, OnRowLongClickListener listener) {
        this.longClickable = true;
        this.mRowLongClickListener = listener;
        this.longClickVibrate = vibrate;
        return this;
    }

    /**
     * Describe: 设置是否可以长按
     * <p>
     * Author: lzl
     * <p>
     * Time: 2016/10/25 下午5:10
     */
    public RecyclerTouchListener setLongClickable(boolean longClickable) {
        this.longClickable = longClickable;
        return this;
    }

    /**
     * Describe: 设置ItemView中可以点击的子view
     * <p>
     * Author: lzl
     * <p>
     * Time: 2016/10/25 下午5:09
     */
    public RecyclerTouchListener setIndependentViews(Integer... viewIds) {
        this.independentViews = new ArrayList<>(Arrays.asList(viewIds));
        return this;
    }

    /**
     * Describe: 设置不可点击对item
     * <p>
     * Author: lzl
     * <p>
     * Time: 2016/10/25 下午5:09
     */
    public RecyclerTouchListener setUnClickableRows(Integer... rows) {
        this.unClickableRows = new ArrayList<>(Arrays.asList(rows));
        return this;
    }

    /**
     * Describe: 设置是否可以进行滑动，有回调
     * <p>
     * Author: lzl
     * <p>
     * Time: 2016/10/25 下午5:08
     */
    public RecyclerTouchListener setSwipeable(int foregroundID, int backgroundID, OnSwipeOptionsClickListener listener) {
        this.swipeable = true;
        if (fgViewID != 0 && foregroundID != fgViewID)
            throw new IllegalArgumentException("foregroundID does not match previously set ID");
        fgViewID = foregroundID;
        bgViewID = backgroundID;
        this.mBgClickListener = listener;

        if (activity instanceof RecyclerTouchListenerHelper)
            ((RecyclerTouchListenerHelper) activity).setOnActivityTouchListener(this);

        DisplayMetrics displaymetrics = new DisplayMetrics();
        activity.getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
        screenHeight = displaymetrics.heightPixels;
        screenWidth = displaymetrics.widthPixels;

        return this;
    }

    /**
     * Describe: 设置是否可以进行滑动
     * <p>
     * Author: lzl
     * <p>
     * Time: 2016/10/25 下午2:15
     */
    public RecyclerTouchListener setSwipeable(boolean value) {
        this.swipeable = value;
        if (!value)
            invalidateSwipeOptions();
        return this;
    }

    public RecyclerTouchListener setSwipeOptionViews(Integer... viewIds) {
        this.optionViews = new ArrayList<>(Arrays.asList(viewIds));
        return this;
    }

    /**
     * Describe: 设置不可滑动到位置
     * <p>
     * Author: lzl
     * <p>
     * Time: 2016/10/25 上午11:33
     */
    public RecyclerTouchListener setUnSwipeableRows(Integer... rows) {
        this.unSwipeableRows = new ArrayList<>(Arrays.asList(rows));
        return this;
    }

    /**
     * Describe: 设置当打开底部菜单时哪些视图进行隐藏
     * <p>
     * Author: lzl
     * <p>
     * Time: 2016/10/25 下午2:09
     */
    public RecyclerTouchListener setViewsToFade(Integer... viewIds) {
        this.fadeViews = new ArrayList<>(Arrays.asList(viewIds));
        return this;
    }

    public RecyclerTouchListener setFgFade() {
        if (!fadeViews.contains(fgViewID))
            this.fadeViews.add(fgViewID);
        return this;
    }


    public void invalidateSwipeOptions() {
        bgWidth = 1;
    }

    @Override
    public void onRequestDisallowInterceptTouchEvent(boolean disallowIntercept) {

    }

    @Override
    public boolean onInterceptTouchEvent(RecyclerView rv, MotionEvent motionEvent) {
        return handleRvTouchEvent(motionEvent);
    }

    @Override
    public void onTouchEvent(RecyclerView rv, MotionEvent motionEvent) {
        handleRvTouchEvent(motionEvent);
    }

    /**
     * Describe: 处理rv中的touch事件
     * <p>
     * Author: lzl
     * <p>
     * Time: 2016/10/25 下午4:15
     */
    private boolean handleRvTouchEvent(MotionEvent motionEvent) {
        if (swipeable && bgWidth < 2) {
            if (activity.findViewById(bgViewID) != null)
                bgWidth = activity.findViewById(bgViewID).getWidth();

            heightOutsideRView = screenHeight - rView.getHeight();
        }

        switch (motionEvent.getActionMasked()) {

            case MotionEvent.ACTION_DOWN: {
                if (mPaused) {
                    break;
                }

                Rect rect = new Rect();
                int childCount = rView.getChildCount();
                int[] listViewCoords = new int[2];
                rView.getLocationOnScreen(listViewCoords);
                int x = (int) motionEvent.getRawX() - listViewCoords[0];
                int y = (int) motionEvent.getRawY() - listViewCoords[1];
                float x1 = motionEvent.getX();
                float y1 = motionEvent.getY();
                View child;

                /**
                 * 根据down的坐标点，获取哪个item接收到了touch事件
                 */
                for (int i = 0; i < childCount; i++) {
                    child = rView.getChildAt(i);
                    child.getHitRect(rect);
                    if (rect.contains(x, y)) {
                        touchedView = child;
                        break;
                    }
                }

                if (touchedView != null) {
                    touchedX = motionEvent.getRawX();
                    touchedY = motionEvent.getRawY();
                    touchedPosition = rView.getChildAdapterPosition(touchedView);
                    if (longClickable) {
                        /**
                         * 如果是长按===============
                         */

                        mLongClickPerformed = false;
                        handler.postDelayed(mLongPressed, LONG_CLICK_DELAY);
                    }
                    if (swipeable) {
                        /**
                         * 如果可以滑动
                         */
                        mVelocityTracker = VelocityTracker.obtain();
                        mVelocityTracker.addMovement(motionEvent);
                        //获取上部分视图
                        fgView = touchedView.findViewById(fgViewID);
                        //获取下部分视图
                        bgView = touchedView.findViewById(bgViewID);
                        bgView.setMinimumHeight(fgView.getHeight());
                        if (bgVisible && fgView != null) {
                            handler.removeCallbacks(mLongPressed);
                            x = (int) motionEvent.getRawX();
                            y = (int) motionEvent.getRawY();
                            fgView.getGlobalVisibleRect(rect);
                            fgPartialViewClicked = rect.contains(x, y);
                        } else {
                            fgPartialViewClicked = false;
                        }
                    }
                }
                x = (int) motionEvent.getRawX();
                y = (int) motionEvent.getRawY();
                rView.getHitRect(rect);
                if (swipeable && bgVisible && touchedPosition != bgVisiblePosition) {
                    handler.removeCallbacks(mLongPressed);
                    closeVisibleBG(null);
                }
                break;
            }
            case MotionEvent.ACTION_CANCEL: {
                /**
                 * 手势取消处理=====================================ACTION_CANCEL
                 */
                handler.removeCallbacks(mLongPressed);
                if (mLongClickPerformed || mVelocityTracker == null) {
                    break;
                }
                if (swipeable) {
                    if (touchedView != null && isFgSwiping) {
                        // cancel
                        animateFG(touchedView, Animation.CLOSE, ANIMATION_STANDARD);
                    }
                    mVelocityTracker.recycle();
                    mVelocityTracker = null;
                    isFgSwiping = false;
                    bgView = null;
                }
                touchedX = 0;
                touchedY = 0;
                touchedView = null;
                touchedPosition = ListView.INVALID_POSITION;
                break;
            }
            case MotionEvent.ACTION_UP: {
                /**
                 * 当手指离开屏幕，处理滑动逻辑==========================ACTION_UP
                 */
                handler.removeCallbacks(mLongPressed);
                if (mLongClickPerformed)
                    break;

                if (mVelocityTracker == null && swipeable) {
                    break;
                }
                if (touchedPosition < 0)
                    break;

                boolean swipedLeft = false;//是否向左滑动
                boolean swipedRight = false;//是否向滑动
                boolean swipedLeftProper = false;
                boolean swipedRightProper = false;

                float mFinalDelta = motionEvent.getRawX() - touchedX;
                if (isFgSwiping) {
                    swipedLeft = mFinalDelta < 0;
                    swipedRight = mFinalDelta > 0;
                }
                if (Math.abs(mFinalDelta) > bgWidth / 2 && isFgSwiping) {
                    /**
                     * 适合执行向左或者向右滑动的条件
                     */
                    swipedLeftProper = mFinalDelta < 0;
                    swipedRightProper = mFinalDelta > 0;
                } else if (swipeable) {
                    mVelocityTracker.addMovement(motionEvent);
                    mVelocityTracker.computeCurrentVelocity(1000);
                    float velocityX = mVelocityTracker.getXVelocity();
                    float absVelocityX = Math.abs(velocityX);
                    float absVelocityY = Math.abs(mVelocityTracker.getYVelocity());
                    if (minFlingVel <= absVelocityX && absVelocityX <= maxFlingVel
                            && absVelocityY < absVelocityX && isFgSwiping) {
                        swipedLeftProper = (velocityX < 0) == (mFinalDelta < 0);
                        swipedRightProper = (velocityX > 0) == (mFinalDelta > 0);
                    }
                }
                if (swipeable && !swipedRight && swipedLeftProper && touchedPosition != RecyclerView.NO_POSITION
                        && !unSwipeableRows.contains(touchedPosition) && !bgVisible) {
                    /**
                     * 向左滑动，需要打开菜单的条件==========================
                     */

                    final View downView = touchedView; // touchedView gets null'd before animation ends
                    final int downPosition = touchedPosition;
                    ++mDismissAnimationRefCount;
                    //TODO - speed
                    animateFG(touchedView, Animation.OPEN, ANIMATION_STANDARD);
                    bgVisible = true;
                    bgVisibleView = fgView;
                    bgVisiblePosition = downPosition;
                } else if (swipeable && !swipedLeft && swipedRightProper && touchedPosition != RecyclerView.NO_POSITION
                        && !unSwipeableRows.contains(touchedPosition) && bgVisible) {
                    /**
                     * 菜单可见，向右滑动，需要关闭菜单的条件======================
                     */
                    final View downView = touchedView; // touchedView gets null'd before animation ends
                    final int downPosition = touchedPosition;

                    ++mDismissAnimationRefCount;
                    //TODO - speed
                    animateFG(touchedView, Animation.CLOSE, ANIMATION_STANDARD);
                    bgVisible = false;
                    bgVisibleView = null;
                    bgVisiblePosition = -1;
                } else if (swipeable && swipedLeft && !bgVisible) {
                    /**
                     * 菜单不可见，向左滑动，取消手势=============================
                     */

                    final View tempBgView = bgView;
                    animateFG(touchedView, Animation.CLOSE, ANIMATION_STANDARD, new OnSwipeListener() {
                        @Override
                        public void onSwipeOptionsClosed() {
                            if (tempBgView != null)
                                tempBgView.setVisibility(View.VISIBLE);
                        }

                        @Override
                        public void onSwipeOptionsOpened() {

                        }
                    });

                    bgVisible = false;
                    bgVisibleView = null;
                    bgVisiblePosition = -1;
                } else if (swipeable && swipedRight && bgVisible) {
                    /**
                     * 菜单可见状态，向右滑动，手势取消===============================
                     */
                    animateFG(touchedView, Animation.OPEN, ANIMATION_STANDARD);
                    bgVisible = true;
                    bgVisibleView = fgView;
                    bgVisiblePosition = touchedPosition;
                } else if (swipeable && swipedRight && !bgVisible) {
                    /**
                     * 菜单不可见，向右滑动，取消手势================================
                     */
                    animateFG(touchedView, Animation.CLOSE, ANIMATION_STANDARD);
                    bgVisible = false;
                    bgVisibleView = null;
                    bgVisiblePosition = -1;
                } else if (swipeable && swipedLeft && bgVisible) {
                    /**
                     * 菜单可见，向左滑动，取消手势======================================
                     */
                    animateFG(touchedView, Animation.OPEN, ANIMATION_STANDARD);
                    bgVisible = true;
                    bgVisibleView = fgView;
                    bgVisiblePosition = touchedPosition;
                } else if (!swipedRight && !swipedLeft) {
                    /**
                     * 处理点击事件=====================================
                     */
                    if (swipeable && fgPartialViewClicked) {
                        animateFG(touchedView, Animation.CLOSE, ANIMATION_STANDARD);
                        bgVisible = false;
                        bgVisibleView = null;
                        bgVisiblePosition = -1;
                    } else if (clickable && !bgVisible && touchedPosition >= 0 && !unClickableRows.contains(touchedPosition)
                            && isIndependentViewClicked(motionEvent) && !isRViewScrolling) {
//                        ((Button)touchedView.getTag(100)).performk();
                        touchedView.performClick();
//                        mRowClickListener.onRowClicked(touchedPosition);
                    } else if (clickable && !bgVisible && touchedPosition >= 0 && !unClickableRows.contains(touchedPosition)
                            && !isIndependentViewClicked(motionEvent) && !isRViewScrolling) {
                        /**
                         * 菜单不可见，点击非可点击的子视图
                         */
                        ((Button)touchedView.findViewWithTag("button")).performClick();
//                        touchedView.performClick();
//                        final int independentViewID = getIndependentViewID(motionEvent);
//                        if (independentViewID >= 0)
//                            mRowClickListener.onIndependentViewClicked(independentViewID, touchedPosition);
                    } else if (swipeable && bgVisible && !fgPartialViewClicked) {
                        /**
                         * 处理菜单点击事件
                         */

                        final int optionID = getOptionViewID(motionEvent);
                        if (optionID >= 0 && touchedPosition >= 0) {
                            final int downPosition = touchedPosition;
                            closeVisibleBG(new OnSwipeListener() {
                                @Override
                                public void onSwipeOptionsClosed() {
                                    mBgClickListener.onSwipeOptionClicked(optionID, downPosition);
                                }

                                @Override
                                public void onSwipeOptionsOpened() {

                                }
                            });
                        }
                    }

                }
            }
            if (swipeable) {
                mVelocityTracker.recycle();
                mVelocityTracker = null;
            }
            touchedX = 0;
            touchedY = 0;
            touchedView = null;
            touchedPosition = ListView.INVALID_POSITION;
            isFgSwiping = false;
            bgView = null;

            break;

            case MotionEvent.ACTION_MOVE: {
                /**
                 * 手势移动中============================================ACTION_MOVE
                 */
                if (mLongClickPerformed)
                    break;
                if (mVelocityTracker == null || mPaused || !swipeable) {
                    break;
                }

                mVelocityTracker.addMovement(motionEvent);
                float deltaX = motionEvent.getRawX() - touchedX;
                float deltaY = motionEvent.getRawY() - touchedY;


                if (!isFgSwiping && Math.abs(deltaX) > touchSlop && Math.abs(deltaY) < Math.abs(deltaX) / 2) {
                    /**
                     * 满足滑动条件后
                     */
                    handler.removeCallbacks(mLongPressed);
                    isFgSwiping = true;
                    mSwipingSlop = (deltaX > 0 ? touchSlop : -touchSlop);
                }

                if (swipeable && isFgSwiping && !unSwipeableRows.contains(touchedPosition)) {
                    /**
                     * 满足滑动视图的条件===================1
                     */
                    if (bgView == null) {
                        bgView = touchedView.findViewById(bgViewID);
                        bgView.setVisibility(View.VISIBLE);
                    }

                    if (deltaX < touchSlop && !bgVisible) {
                        /**
                         * 向左滑动=========================2
                         */

                        //上层视图需要移动到距离
                        float translateAmount = deltaX - mSwipingSlop;
                        fgView.setTranslationX(Math.abs(translateAmount) > bgWidth ? -bgWidth : translateAmount);
                        if (fgView.getTranslationX() > 0) {
                            fgView.setTranslationX(0);
                        }


                        if (fadeViews != null) {
                            for (int viewID : fadeViews) {
                                touchedView.findViewById(viewID).setAlpha(1 - (Math.abs(translateAmount) / bgWidth));
                            }
                        }
                    } else if (deltaX > 0 && bgVisible) {
                        /**
                         * 向右边滑动=======================3
                         */
                        if (bgVisible) {
                            float translateAmount = (deltaX - mSwipingSlop) - bgWidth;

                            fgView.setTranslationX(translateAmount > 0 ? 0 : translateAmount);

                            if (fadeViews != null) {
                                for (int viewID : fadeViews) {
                                    touchedView.findViewById(viewID).setAlpha(1 - (Math.abs(translateAmount) / bgWidth));
                                }
                            }
                        } else {
                            float translateAmount = (deltaX - mSwipingSlop) - bgWidth;
                            fgView.setTranslationX(translateAmount > 0 ? 0 : translateAmount);
                            if (fadeViews != null) {
                                for (int viewID : fadeViews) {
                                    touchedView.findViewById(viewID).setAlpha(1 - (Math.abs(translateAmount) / bgWidth));
                                }
                            }
                        }
                    }
                    return true;
                } else if (swipeable && isFgSwiping && unSwipeableRows.contains(touchedPosition)) {
                    /**
                     * 不满足滑动视图的条件==============================2
                     */
                    if (deltaX < touchSlop && !bgVisible) {
                        float translateAmount = deltaX - mSwipingSlop;
                        if (bgView == null)
                            bgView = touchedView.findViewById(bgViewID);

                        if (bgView != null)
                            bgView.setVisibility(View.GONE);

                        fgView.setTranslationX(translateAmount / 5);
                        if (fgView.getTranslationX() > 0) fgView.setTranslationX(0);


                    }
                    return true;
                }
                break;
            }
        }
        return false;
    }

    /**
     * Describe: 打开指定位置的菜单
     * <p>
     * Author: lzl
     * <p>
     * Time: 2016/10/25 下午4:56
     */
    public void openSwipeOptions(int position) {
        if (!swipeable || rView.getChildAt(position) == null
                || unSwipeableRows.contains(position))
            return;
        if (bgWidth < 2) {
            if (activity.findViewById(bgViewID) != null)
                bgWidth = activity.findViewById(bgViewID).getWidth();
            heightOutsideRView = screenHeight - rView.getHeight();
        }
        touchedPosition = position;
        touchedView = rView.getChildAt(position);
        fgView = touchedView.findViewById(fgViewID);
        bgView = touchedView.findViewById(bgViewID);
        bgView.setMinimumHeight(fgView.getHeight());

        closeVisibleBG(null);
        animateFG(touchedView, Animation.OPEN, ANIMATION_STANDARD);
        bgVisible = true;
        bgVisibleView = fgView;
        bgVisiblePosition = touchedPosition;
    }


    /**
     * Describe: 关闭菜单视图
     * <p>
     * Author: lzl
     * <p>
     * Time: 2016/10/25 下午4:58
     */
    public void closeVisibleBG(final OnSwipeListener mSwipeCloseListener) {
        if (bgVisibleView == null) {
            return;
        }
        final ObjectAnimator translateAnimator = ObjectAnimator.ofFloat(bgVisibleView,
                View.TRANSLATION_X, 0f);
        translateAnimator.setDuration(ANIMATION_CLOSE);
        translateAnimator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                if (mSwipeCloseListener != null)
                    mSwipeCloseListener.onSwipeOptionsClosed();
                translateAnimator.removeAllListeners();
            }

            @Override
            public void onAnimationCancel(Animator animation) {
            }

            @Override
            public void onAnimationRepeat(Animator animation) {
            }
        });
        translateAnimator.start();

        animateFadeViews(bgVisibleView, 1f, ANIMATION_CLOSE);
        bgVisible = false;
        bgVisibleView = null;
        bgVisiblePosition = -1;
    }

    /**
     * Describe: 对需要隐藏对视图执行透明度动画
     * <p>
     * Author: lzl
     * <p>
     * Time: 2016/10/25 下午4:59
     */
    private void animateFadeViews(View downView, float alpha, long duration) {
        if (fadeViews != null) {
            for (final int viewID : fadeViews) {
                downView.findViewById(viewID).animate()
                        .alpha(alpha)
                        .setDuration(duration);
            }
        }
    }

    /**
     * Describe: 对上层视图执行移动动画
     * <p>
     * Author: lzl
     * <p>
     * Time: 2016/10/25 下午4:59
     */
    private void animateFG(View downView, Animation animateType, long duration) {
        if (animateType == Animation.OPEN) {
            ObjectAnimator translateAnimator = ObjectAnimator.ofFloat(
                    fgView, View.TRANSLATION_X, -bgWidth);
            translateAnimator.setDuration(duration);
            translateAnimator.setInterpolator(new DecelerateInterpolator(1.5f));
            translateAnimator.start();
            animateFadeViews(downView, 0f, duration);
        } else if (animateType == Animation.CLOSE) {
            ObjectAnimator translateAnimator = ObjectAnimator.ofFloat(
                    fgView, View.TRANSLATION_X, 0f);
            translateAnimator.setDuration(duration);
            translateAnimator.setInterpolator(new DecelerateInterpolator(1.5f));
            translateAnimator.start();
            animateFadeViews(downView, 1f, duration);
        }
    }

    /**
     * Describe: 对上层视图执行移动动画，有回调
     * <p>
     * Author: lzl
     * <p>
     * Time: 2016/10/25 下午5:00
     */
    private void animateFG(View downView, final Animation animateType, long duration,
                           final OnSwipeListener mSwipeCloseListener) {
        final ObjectAnimator translateAnimator;
        if (animateType == Animation.OPEN) {
            translateAnimator = ObjectAnimator.ofFloat(fgView, View.TRANSLATION_X, -bgWidth);
            translateAnimator.setDuration(duration);
            translateAnimator.setInterpolator(new DecelerateInterpolator(1.5f));
            translateAnimator.start();
            animateFadeViews(downView, 0f, duration);
        } else {
            translateAnimator = ObjectAnimator.ofFloat(fgView, View.TRANSLATION_X, 0f);
            translateAnimator.setDuration(duration);
            translateAnimator.setInterpolator(new DecelerateInterpolator(1.5f));
            translateAnimator.start();
            animateFadeViews(downView, 1f, duration);
        }

        translateAnimator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                if (mSwipeCloseListener != null) {
                    if (animateType == Animation.OPEN)
                        mSwipeCloseListener.onSwipeOptionsOpened();
                    else if (animateType == Animation.CLOSE)
                        mSwipeCloseListener.onSwipeOptionsClosed();
                }
                translateAnimator.removeAllListeners();
            }

            @Override
            public void onAnimationCancel(Animator animation) {
            }

            @Override
            public void onAnimationRepeat(Animator animation) {
            }
        });
    }

    private boolean isIndependentViewClicked(MotionEvent motionEvent) {
        for (int i = 0; i < independentViews.size(); i++) {
            if (touchedView != null) {
                Rect rect = new Rect();
                int x = (int) motionEvent.getRawX();
                int y = (int) motionEvent.getRawY();
                touchedView.findViewById(independentViews.get(i)).getGlobalVisibleRect(rect);
                if (rect.contains(x, y)) {
                    return false;
                }
            }
        }
        return true;
    }

    private int getOptionViewID(MotionEvent motionEvent) {
        for (int i = 0; i < optionViews.size(); i++) {
            if (touchedView != null) {
                Rect rect = new Rect();
                int x = (int) motionEvent.getRawX();
                int y = (int) motionEvent.getRawY();
                touchedView.findViewById(optionViews.get(i)).getGlobalVisibleRect(rect);
                if (rect.contains(x, y)) {
                    return optionViews.get(i);
                }
            }
        }
        return -1;
    }

    private int getIndependentViewID(MotionEvent motionEvent) {
        for (int i = 0; i < independentViews.size(); i++) {
            if (touchedView != null) {
                Rect rect = new Rect();
                int x = (int) motionEvent.getRawX();
                int y = (int) motionEvent.getRawY();
                touchedView.findViewById(independentViews.get(i)).getGlobalVisibleRect(rect);
                if (rect.contains(x, y)) {
                    return independentViews.get(i);
                }
            }
        }
        return -1;
    }


    /**
     * Describe: 接收从activity层传人的touch事件，如果touch范围在rv外面，关闭菜单
     * <p>
     * Author: lzl
     * <p>
     * Time: 2016/10/25 下午4:49
     */
    @Override
    public void getTouchCoordinates(MotionEvent ev) {
//        int y = (int) ev.getRawY();
//        if (swipeable && bgVisible && ev.getActionMasked() == MotionEvent.ACTION_DOWN
//                && y < heightOutsideRView) closeVisibleBG(null);
    }

    private enum Animation {
        OPEN, CLOSE
    }

    Runnable mLongPressed = new Runnable() {
        public void run() {
            if (!longClickable)
                return;

            mLongClickPerformed = true;

            if (!bgVisible && touchedPosition >= 0 && !unClickableRows.contains(touchedPosition) && !isRViewScrolling) {
                if (longClickVibrate) {
                    Vibrator vibe = (Vibrator) activity.getSystemService(Context.VIBRATOR_SERVICE);
                    vibe.vibrate(100);
                }
                mRowLongClickListener.onRowLongClicked(touchedPosition);
            }
        }
    };

    public interface OnRowClickListener {
        void onRowClicked(int position);

        void onIndependentViewClicked(int independentViewID, int position);
    }

    public interface OnRowLongClickListener {
        void onRowLongClicked(int position);
    }

    public interface OnSwipeOptionsClickListener {
        void onSwipeOptionClicked(int viewID, int position);
    }

    public interface RecyclerTouchListenerHelper {
        void setOnActivityTouchListener(OnActivityTouchListener listener);
    }

    public interface OnSwipeListener {
        void onSwipeOptionsClosed();

        void onSwipeOptionsOpened();
    }
}
