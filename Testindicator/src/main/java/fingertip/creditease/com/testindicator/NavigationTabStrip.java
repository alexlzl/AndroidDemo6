

package fingertip.creditease.com.testindicator;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Configuration;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.v4.view.ViewCompat;
import android.support.v4.view.ViewPager;
import android.text.TextPaint;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;
import android.widget.Scroller;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Random;


public class NavigationTabStrip extends View implements ViewPager.OnPageChangeListener {
    private final static int HIGH_QUALITY_FLAGS = Paint.ANTI_ALIAS_FLAG | Paint.DITHER_FLAG;
    private final static String PREVIEW_TITLE = "Title";
    private final static int INVALID_INDEX = -1;
    private final static int DEFAULT_ANIMATION_DURATION = 350;
    private final static float DEFAULT_STRIP_FACTOR = 2.5F;
    private final static float DEFAULT_STRIP_WEIGHT = 10.0F;
    private final static float DEFAULT_CORNER_RADIUS = 5.0F;
    private final static int DEFAULT_INACTIVE_COLOR = Color.GRAY;
    private final static int DEFAULT_ACTIVE_COLOR = Color.WHITE;
    private final static int DEFAULT_STRIP_COLOR = Color.RED;
    private final static int DEFAULT_TITLE_SIZE = 0;
    private final static float TITLE_SIZE_FRACTION = 0.35F;
    private final static float MIN_FRACTION = 0.0F;
    private final static float MAX_FRACTION = 1.0F;
    private final RectF mBounds = new RectF();
    private final RectF mStripBounds = new RectF();
    private final Rect mTitleBounds = new Rect();
    private final ValueAnimator mAnimator = new ValueAnimator();
    private final ArgbEvaluator mColorEvaluator = new ArgbEvaluator();
    private final ResizeInterpolator mResizeInterpolator = new ResizeInterpolator();
    private int mAnimationDuration;
    private String[] mTitles;
    private ViewPager mViewPager;
    private ViewPager.OnPageChangeListener mOnPageChangeListener;
    private int mScrollState;
    private OnTabStripSelectedIndexListener mOnTabStripSelectedIndexListener;
    private ValueAnimator.AnimatorListener mAnimatorListener;
    private float mTabSize;
    private float mTitleSize;
    private StripType mStripType;
    private StripGravity mStripGravity;
    private float mStripWeight;
    private float mCornersRadius;
    private int mLastIndex = INVALID_INDEX;
    private int mIndex = INVALID_INDEX;
    private float mFraction;
    private float mStartStripX;
    private float mEndStripX;
    private float mStripLeft;
    private float mStripRight;
    private boolean mIsViewPagerMode;
    private boolean mIsResizeIn;
    private boolean mIsActionDown;
    private boolean mIsTabActionDown;
    private boolean mIsSetIndexFromTabBar;
    private int mTabTextColorNotChecked;
    private int mTabTextColorChecked;
    private Typeface mTypeface;
    /**
     * Describe: 初始化下划线画笔属性
     * <p>
     * Author: lzl
     * <p>
     * Time: 2016/11/15 下午2:16
     */
    private final Paint mStripPaint = new Paint(HIGH_QUALITY_FLAGS) {
        {
            setStyle(Style.FILL);
        }
    };
    /**
     * Describe: 初始化标题文本画笔属性
     * <p>
     * Author: lzl
     * <p>
     * Time: 2016/11/15 下午2:17
     */
    private final Paint mTitlePaint = new TextPaint(HIGH_QUALITY_FLAGS) {
        {
            setTextAlign(Align.CENTER);
        }
    };

    public NavigationTabStrip(final Context context) {
        this(context, null);
    }

    public NavigationTabStrip(final Context context, final AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public NavigationTabStrip(final Context context, final AttributeSet attrs, final int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    /**
     * Describe: 初始化
     * <p>
     * Author: lzl
     * <p>
     * Time: 2016/11/15 下午2:23
     */
    private void init(Context context, final AttributeSet attrs) {
        //view级别禁止硬件加速
        ViewCompat.setLayerType(this, ViewCompat.LAYER_TYPE_SOFTWARE, null);
        setLayerType(LAYER_TYPE_SOFTWARE, null);

        final TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.NavigationTabStrip);
        try {
            setTabLineColor(
                    typedArray.getColor(R.styleable.NavigationTabStrip_tab_line_color, DEFAULT_STRIP_COLOR)
            );
            setTitleSize(
                    typedArray.getDimension(R.styleable.NavigationTabStrip_tab_text_size, DEFAULT_TITLE_SIZE)
            );
            setTabLineHeight(
                    typedArray.getDimension(R.styleable.NavigationTabStrip_tab_line_height, DEFAULT_STRIP_WEIGHT)
            );
            setStripFactor(
                    typedArray.getFloat(R.styleable.NavigationTabStrip_nts_factor, DEFAULT_STRIP_FACTOR)
            );
            setTabType(
                    typedArray.getInt(R.styleable.NavigationTabStrip_nts_type, StripType.LINE_INDEX)
            );
            setTabPlace(
                    typedArray.getInt(R.styleable.NavigationTabStrip_nts_gravity, StripGravity.BOTTOM_INDEX)
            );

            setTextTypeface(typedArray.getString(R.styleable.NavigationTabStrip_nts_typeface));
            setTabTextNotCheckColor(
                    typedArray.getColor(
                            R.styleable.NavigationTabStrip_tab_text_not_check_color, DEFAULT_INACTIVE_COLOR
                    )
            );
            setTabTextCheckedColor(
                    typedArray.getColor(
                            R.styleable.NavigationTabStrip_tab_text_check_color, DEFAULT_ACTIVE_COLOR
                    )
            );
            setAnimationDuration(
                    typedArray.getInteger(
                            R.styleable.NavigationTabStrip_nts_animation_duration, DEFAULT_ANIMATION_DURATION
                    )
            );
            setCornersRadius(
                    typedArray.getDimension(
                            R.styleable.NavigationTabStrip_nts_corners_radius, DEFAULT_CORNER_RADIUS
                    )
            );
            /**
             * 获取标题资源
             */
            String[] titles = null;
            try {
                final int titlesResId = typedArray.getResourceId(
                        R.styleable.NavigationTabStrip_tab_titles, 0
                );
                titles = (titlesResId == 0) ? null :
                        typedArray.getResources().getStringArray(titlesResId);
            } catch (Exception exception) {
                titles = null;
                exception.printStackTrace();
            } finally {
                if (titles == null) {
                    if (isInEditMode()) {
                        titles = new String[new Random().nextInt(5) + 1];
                        Arrays.fill(titles, PREVIEW_TITLE);
                    } else titles = new String[0];
                }

                setTitles(titles);
            }
            mAnimator.setFloatValues(MIN_FRACTION, MAX_FRACTION);
            mAnimator.setInterpolator(new LinearInterpolator());
            mAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(final ValueAnimator animation) {
                    updateIndicatorPosition((Float) animation.getAnimatedValue());
                }
            });
        } finally {
            typedArray.recycle();
        }
    }

    /**
     * Describe: 更新指示器位置
     * <p>
     * Author: lzl
     * <p>
     * Time: 2016/11/17 下午4:31
     */
    public void setTabIndex(int tabIndex, boolean isForce) {
        if (mAnimator.isRunning()) return;
        if (mTitles.length == 0) return;
        int index = tabIndex;
        boolean force = isForce;
        if (mIndex == INVALID_INDEX) {
            force = true;
        }

        if (index == mIndex) {
            return;
        }
        index = Math.max(0, Math.min(index, mTitles.length - 1));

        mIsResizeIn = index < mIndex;
        mLastIndex = mIndex;
        mIndex = index;

        mIsSetIndexFromTabBar = true;
        if (mIsViewPagerMode) {
            if (mViewPager == null) throw new IllegalStateException("ViewPager is null.");
            mViewPager.setCurrentItem(index, !force);
        }

        // Set startX and endX for animation, where we animate two sides of rect with different interpolation
        mStartStripX = mStripLeft;
        mEndStripX = (mIndex * mTabSize) + (mStripType == StripType.POINT ? mTabSize * 0.5F : 0.0F);

        /// If it force, so update immediately, else animate
        // This happens if we set index onCreate or something like this
        // You can use force param or call this method in some post()
        if (force) {
            updateIndicatorPosition(MAX_FRACTION);
            // Force onPageScrolled listener and refresh VP
            if (mIsViewPagerMode) {
                if (!mViewPager.isFakeDragging()) mViewPager.beginFakeDrag();
                if (mViewPager.isFakeDragging()) {
                    mViewPager.fakeDragBy(0.0F);
                    mViewPager.endFakeDrag();
                }
            }
        } else mAnimator.start();
    }

    /**
     * Describe: 更新指示器位置
     * <p>
     * Author: lzl
     * <p>
     * Time: 2016/11/15 下午3:28
     */

    private void updateIndicatorPosition(final float fraction) {
        // Update general fraction
        mFraction = fraction;

        // Set the strip left side coordinate
        mStripLeft =
                mStartStripX + (mResizeInterpolator.getResizeInterpolation(fraction, mIsResizeIn) *
                        (mEndStripX - mStartStripX));
        // Set the strip right side coordinate
        mStripRight =
                (mStartStripX + (mStripType == StripType.LINE ? mTabSize : mStripWeight)) +
                        (mResizeInterpolator.getResizeInterpolation(fraction, !mIsResizeIn) *
                                (mEndStripX - mStartStripX));

        // Update NTS
        postInvalidate();
    }

    @Override
    public boolean onTouchEvent(final MotionEvent event) {
        // Return if animation is running
        if (mAnimator.isRunning()) return true;
        // If is not idle state, return
        if (mScrollState != ViewPager.SCROLL_STATE_IDLE) return true;

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                // Action down touch
                mIsActionDown = true;
                if (!mIsViewPagerMode) break;
                // Detect if we touch down on tab, later to move
                mIsTabActionDown = (int) (event.getX() / mTabSize) == mIndex;
                break;
            case MotionEvent.ACTION_MOVE:
                // If tab touched, so move
                if (mIsTabActionDown) {
                    mViewPager.setCurrentItem((int) (event.getX() / mTabSize), true);
                    break;
                }
                if (mIsActionDown) break;
            case MotionEvent.ACTION_UP:
                // Press up and set tab index relative to current coordinate
                if (mIsActionDown) setTabIndex((int) (event.getX() / mTabSize));
            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_OUTSIDE:
            default:
                // Reset action touch variables
                mIsTabActionDown = false;
                mIsActionDown = false;
                break;
        }

        return true;
    }

    @SuppressLint("DrawAllocation")
    @Override
    protected void onMeasure(final int widthMeasureSpec, final int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        // Get measure size
        final float width = MeasureSpec.getSize(widthMeasureSpec);
        final float height = MeasureSpec.getSize(heightMeasureSpec);

        // Set bounds for NTS
        mBounds.set(0.0F, 0.0F, width, height);

        if (mTitles.length == 0 || width == 0 || height == 0) return;

        // Get smaller side
        mTabSize = width / (float) mTitles.length;
        if ((int) mTitleSize == DEFAULT_TITLE_SIZE)
            setTitleSize((height - mStripWeight) * TITLE_SIZE_FRACTION);

        // Set start position of strip for preview or on start
        if (isInEditMode() || !mIsViewPagerMode) {
            mIsSetIndexFromTabBar = true;

            // Set random in preview mode
            if (isInEditMode()) mIndex = new Random().nextInt(mTitles.length);

            mStartStripX =
                    (mIndex * mTabSize) + (mStripType == StripType.POINT ? mTabSize * 0.5F : 0.0F);
            mEndStripX = mStartStripX;
            updateIndicatorPosition(MAX_FRACTION);
        }
    }

    @Override
    protected void onDraw(final Canvas canvas) {
        // Set bound of strip
        mStripBounds.set(
                mStripLeft - (mStripType == StripType.POINT ? mStripWeight * 0.5F : 0.0F),
                mStripGravity == StripGravity.BOTTOM ? mBounds.height() - mStripWeight : 0.0F,
                mStripRight - (mStripType == StripType.POINT ? mStripWeight * 0.5F : 0.0F),
                mStripGravity == StripGravity.BOTTOM ? mBounds.height() : mStripWeight
        );

        // 绘制下滑线
        if (mCornersRadius == 0) canvas.drawRect(mStripBounds, mStripPaint);
        else canvas.drawRoundRect(mStripBounds, mCornersRadius, mCornersRadius, mStripPaint);

        // 绘制标题
        for (int i = 0; i < mTitles.length; i++) {
            final String title = mTitles[i];

            final float leftTitleOffset = (mTabSize * i) + (mTabSize * 0.5F);

            mTitlePaint.getTextBounds(title, 0, title.length(), mTitleBounds);
            final float topTitleOffset = (mBounds.height() - mStripWeight) * 0.5F +
                    mTitleBounds.height() * 0.5F - mTitleBounds.bottom;

            // Get interpolated fraction for left last and current tab
            final float interpolation = mResizeInterpolator.getResizeInterpolation(mFraction, true);
            final float lastInterpolation = mResizeInterpolator.getResizeInterpolation(mFraction, false);

            // Check if we handle tab from touch on NTS or from ViewPager
            // There is a strange logic of ViewPager onPageScrolled method, so it is
            if (mIsSetIndexFromTabBar) {
                if (mIndex == i) updateCurrentTitle(interpolation);
                else if (mLastIndex == i) updateLastTitle(lastInterpolation);
                else updateInactiveTitle();
            } else {
                if (i != mIndex && i != mIndex + 1) updateInactiveTitle();
                else if (i == mIndex + 1) updateCurrentTitle(interpolation);
                else if (i == mIndex) updateLastTitle(lastInterpolation);
            }

            canvas.drawText(
                    title, leftTitleOffset,
                    topTitleOffset + (mStripGravity == StripGravity.TOP ? mStripWeight : 0.0F),
                    mTitlePaint
            );
        }
    }

    /**
     * Describe: 设置下滑线画笔颜色
     * <p>
     * Author: lzl
     * <p>
     * Time: 2016/11/15 下午2:24
     */
    public void setTabLineColor(final int color) {
        mStripPaint.setColor(color);
        postInvalidate();
    }

    /**
     * Describe: 设置标题文本大小
     * <p>
     * Author: lzl
     * <p>
     * Time: 2016/11/15 下午2:25
     */
    public void setTitleSize(final float titleSize) {
        mTitleSize = titleSize;
        mTitlePaint.setTextSize(titleSize);
        postInvalidate();
    }

    /**
     * Describe: 设置下滑线的高度
     * <p>
     * Author: lzl
     * <p>
     * Time: 2016/11/15 下午2:29
     */
    public void setTabLineHeight(final float stripWeight) {
        mStripWeight = stripWeight;
        requestLayout();
    }

    public void setStripFactor(final float factor) {
        mResizeInterpolator.setFactor(factor);
    }

    /**
     * Describe: 设置tab类型
     * <p>
     * Author: lzl
     * <p>
     * Time: 2016/11/15 下午2:34
     */
    private void setTabType(final int index) {
        switch (index) {
            case StripType.POINT_INDEX:
                setStripType(StripType.POINT);
                break;
            case StripType.LINE_INDEX:
            default:
                setStripType(StripType.LINE);
                break;
        }
    }

    /**
     * Describe: 设置指示器的位置
     * <p>
     * Author: lzl
     * <p>
     * Time: 2016/11/15 下午2:36
     */
    private void setTabPlace(final int index) {
        switch (index) {
            case StripGravity.TOP_INDEX:
                setStripGravity(StripGravity.TOP);
                break;
            case StripGravity.BOTTOM_INDEX:
            default:
                setStripGravity(StripGravity.BOTTOM);
                break;
        }
    }

    /**
     * Describe: 设置标题字体
     * <p>
     * Author: lzl
     * <p>
     * Time: 2016/11/15 下午2:38
     */
    public void setTextTypeface(final String typeface) {
        if (TextUtils.isEmpty(typeface)) return;

        Typeface tempTypeface;
        try {
            tempTypeface = Typeface.createFromAsset(getContext().getAssets(), typeface);
        } catch (Exception e) {
            tempTypeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL);
            e.printStackTrace();
        }

        setTypeface(tempTypeface);
    }

    /**
     * Describe: 设置未选中的tab标题颜色
     * <p>
     * Author: lzl
     * <p>
     * Time: 2016/11/15 下午2:42
     */
    public void setTabTextNotCheckColor(final int inactiveColor) {
        mTabTextColorNotChecked = inactiveColor;
        postInvalidate();
    }

    /**
     * Describe: 设置tab选中后的文本颜色
     * <p>
     * Author: lzl
     * <p>
     * Time: 2016/11/15 下午2:46
     */
    public void setTabTextCheckedColor(final int activeColor) {
        mTabTextColorChecked = activeColor;
        postInvalidate();
    }

    /**
     * Describe: 设置动画时间
     * <p>
     * Author: lzl
     * <p>
     * Time: 2016/11/15 下午2:48
     */
    public void setAnimationDuration(final int animationDuration) {
        mAnimationDuration = animationDuration;
        mAnimator.setDuration(mAnimationDuration);
        resetScroller();
    }

    private void resetScroller() {
        if (mViewPager == null) return;
        try {
            final Field scrollerField = ViewPager.class.getDeclaredField("mScroller");
            scrollerField.setAccessible(true);
            final ResizeViewPagerScroller scroller = new ResizeViewPagerScroller(getContext());
            scrollerField.set(mViewPager, scroller);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void setCornersRadius(final float cornersRadius) {
        mCornersRadius = cornersRadius;
        postInvalidate();
    }

    public void setTitles(final String... titles) {
        for (int i = 0; i < titles.length; i++) titles[i] = titles[i].toUpperCase();
        mTitles = titles;
        requestLayout();
    }

    public void setTitles(final int... titleResIds) {
        final String[] titles = new String[titleResIds.length];
        for (int i = 0; i < titleResIds.length; i++)
            titles[i] = getResources().getString(titleResIds[i]);
        setTitles(titles);
    }

    public String[] getTitles() {
        return mTitles;
    }

    public int getAnimationDuration() {
        return mAnimationDuration;
    }


    public int getStripColor() {
        return mStripPaint.getColor();
    }

    public StripGravity getStripGravity() {
        return mStripGravity;
    }

    public void setStripGravity(final StripGravity stripGravity) {
        mStripGravity = stripGravity;
        requestLayout();
    }

    public StripType getStripType() {
        return mStripType;
    }


    public void setStripType(final StripType stripType) {
        mStripType = stripType;
        requestLayout();
    }

    public float getStripFactor() {
        return mResizeInterpolator.getFactor();
    }

    public Typeface getTypeface() {
        return mTypeface;
    }

    public void setTypeface(final Typeface typeface) {
        mTypeface = typeface;
        mTitlePaint.setTypeface(typeface);
        postInvalidate();
    }

    public int getActiveColor() {
        return mTabTextColorChecked;
    }

    public int getInactiveColor() {
        return mTabTextColorNotChecked;
    }

    public float getCornersRadius() {
        return mCornersRadius;
    }

    public float getTitleSize() {
        return mTitleSize;
    }


    public OnTabStripSelectedIndexListener getOnTabStripSelectedIndexListener() {
        return mOnTabStripSelectedIndexListener;
    }

    // Set on tab bar selected index listener where you can trigger action onStart or onEnd
    public void setOnTabStripSelectedIndexListener(final OnTabStripSelectedIndexListener onTabStripSelectedIndexListener) {
        mOnTabStripSelectedIndexListener = onTabStripSelectedIndexListener;

        if (mAnimatorListener == null)
            mAnimatorListener = new AnimatorListenerAdapter() {
                @Override
                public void onAnimationStart(final Animator animation) {
                    if (mOnTabStripSelectedIndexListener != null)
                        mOnTabStripSelectedIndexListener.onStartTabSelected(mTitles[mIndex], mIndex);

                    animation.removeListener(this);
                    animation.addListener(this);
                }

                @Override
                public void onAnimationEnd(final Animator animation) {
                    if (mIsViewPagerMode) return;

                    animation.removeListener(this);
                    animation.addListener(this);

                    if (mOnTabStripSelectedIndexListener != null)
                        mOnTabStripSelectedIndexListener.onEndTabSelected(mTitles[mIndex], mIndex);
                }
            };
        mAnimator.removeListener(mAnimatorListener);
        mAnimator.addListener(mAnimatorListener);
    }

    /**
     * Describe: 绑定vp
     * <p>
     * Author: lzl
     * <p>
     * Time: 2016/11/17 下午5:16
     */
    public void setViewPager(final ViewPager viewPager) {
        if (viewPager == null) {
            mIsViewPagerMode = false;
            return;
        }

        if (viewPager.equals(mViewPager)) return;
        if (mViewPager != null) //noinspection deprecation
            mViewPager.setOnPageChangeListener(null);
        if (viewPager.getAdapter() == null)
            throw new IllegalStateException("ViewPager does not provide adapter instance.");

        mIsViewPagerMode = true;
        mViewPager = viewPager;
        mViewPager.addOnPageChangeListener(this);

        resetScroller();
        postInvalidate();
    }

    /**
     * Describe: 绑定指定页面的vp
     * <p>
     * Author: lzl
     * <p>
     * Time: 2016/11/17 下午5:17
     */
    public void setViewPager(final ViewPager viewPager, int index) {
        setViewPager(viewPager);

        mIndex = index;
        if (mIsViewPagerMode) mViewPager.setCurrentItem(index, true);
        postInvalidate();
    }

    public void setOnPageChangeListener(final ViewPager.OnPageChangeListener listener) {
        mOnPageChangeListener = listener;
    }

    public int getTabIndex() {
        return mIndex;
    }

    public void setTabIndex(int index) {
        setTabIndex(index, false);
    }

    // Deselect active index and reset pointer
    public void deselect() {
        mLastIndex = INVALID_INDEX;
        mIndex = INVALID_INDEX;
        mStartStripX = INVALID_INDEX * mTabSize;
        mEndStripX = mStartStripX;
        updateIndicatorPosition(MIN_FRACTION);
    }

    // Update NTS
    private void notifyDataSetChanged() {
        requestLayout();
        postInvalidate();
    }


    // Method to transform current fraction of NTS and position
    private void updateCurrentTitle(final float interpolation) {
        mTitlePaint.setColor(
                (int) mColorEvaluator.evaluate(interpolation, mTabTextColorNotChecked, mTabTextColorChecked)
        );
    }

    // Method to transform last fraction of NTS and position
    private void updateLastTitle(final float lastInterpolation) {
        mTitlePaint.setColor(
                (int) mColorEvaluator.evaluate(lastInterpolation, mTabTextColorChecked, mTabTextColorNotChecked)
        );
    }

    // Method to transform others fraction of NTS and position
    private void updateInactiveTitle() {
        mTitlePaint.setColor(mTabTextColorNotChecked);
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, final int positionOffsetPixels) {
        if (mOnPageChangeListener != null)
            mOnPageChangeListener.onPageScrolled(position, positionOffset, positionOffsetPixels);

        // If we animate, don`t call this
        if (!mIsSetIndexFromTabBar) {
            mIsResizeIn = position < mIndex;
            mLastIndex = mIndex;
            mIndex = position;

            mStartStripX =
                    (position * mTabSize) + (mStripType == StripType.POINT ? mTabSize * 0.5F : 0.0F);
            mEndStripX = mStartStripX + mTabSize;
            updateIndicatorPosition(positionOffset);
        }

        // Stop scrolling on animation end and reset values
        if (!mAnimator.isRunning() && mIsSetIndexFromTabBar) {
            mFraction = MIN_FRACTION;
            mIsSetIndexFromTabBar = false;
        }
    }

    @Override
    public void onPageSelected(final int position) {
        // This method is empty, because we call onPageSelected() when scroll state is idle
    }

    @Override
    public void onPageScrollStateChanged(final int state) {
        // If VP idle, reset to MIN_FRACTION
        mScrollState = state;
        if (state == ViewPager.SCROLL_STATE_IDLE) {
            if (mOnPageChangeListener != null) mOnPageChangeListener.onPageSelected(mIndex);
            if (mIsViewPagerMode && mOnTabStripSelectedIndexListener != null)
                mOnTabStripSelectedIndexListener.onEndTabSelected(mTitles[mIndex], mIndex);
        }

        if (mOnPageChangeListener != null) mOnPageChangeListener.onPageScrollStateChanged(state);
    }

    @Override
    public void onRestoreInstanceState(Parcelable state) {
        final SavedState savedState = (SavedState) state;
        super.onRestoreInstanceState(savedState.getSuperState());
        mIndex = savedState.index;
        requestLayout();
    }

    @Override
    public Parcelable onSaveInstanceState() {
        final Parcelable superState = super.onSaveInstanceState();
        final SavedState savedState = new SavedState(superState);
        savedState.index = mIndex;
        return savedState;
    }

    // Save current index instance
    private static class SavedState extends BaseSavedState {

        private int index;

        public SavedState(Parcelable superState) {
            super(superState);
        }

        private SavedState(Parcel in) {
            super(in);
            index = in.readInt();
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            super.writeToParcel(dest, flags);
            dest.writeInt(index);
        }

        @SuppressWarnings("UnusedDeclaration")
        public static final Creator<SavedState> CREATOR = new Creator<SavedState>() {
            @Override
            public SavedState createFromParcel(Parcel in) {
                return new SavedState(in);
            }

            @Override
            public SavedState[] newArray(int size) {
                return new SavedState[size];
            }
        };
    }

    @Override
    protected void onConfigurationChanged(final Configuration newConfig) {
        // Config view on rotate etc.
        super.onConfigurationChanged(newConfig);
        requestLayout();

        // Refresh strip and state after config changed to current
        final int tempIndex = mIndex;
        deselect();
        post(new Runnable() {
            @Override
            public void run() {
                setTabIndex(tempIndex, true);
            }
        });
    }

    // Custom scroller with custom scroll duration
    private class ResizeViewPagerScroller extends Scroller {

        public ResizeViewPagerScroller(Context context) {
            super(context, new AccelerateDecelerateInterpolator());
        }

        @Override
        public void startScroll(int startX, int startY, int dx, int dy, int duration) {
            super.startScroll(startX, startY, dx, dy, mAnimationDuration);
        }

        @Override
        public void startScroll(int startX, int startY, int dx, int dy) {
            super.startScroll(startX, startY, dx, dy, mAnimationDuration);
        }
    }

    // Resize interpolator to create smooth effect on strip according to inspiration design
    // This is like improved accelerated and decelerated interpolator
    private static class ResizeInterpolator implements Interpolator {

        // Spring factor
        private float mFactor;
        // Check whether side we move
        private boolean mResizeIn;

        public float getFactor() {
            return mFactor;
        }

        public void setFactor(final float factor) {
            mFactor = factor;
        }

        @Override
        public float getInterpolation(final float input) {
            if (mResizeIn) return (float) (1.0F - Math.pow((1.0F - input), 2.0F * mFactor));
            else return (float) (Math.pow(input, 2.0F * mFactor));
        }

        public float getResizeInterpolation(final float input, final boolean resizeIn) {
            mResizeIn = resizeIn;
            return getInterpolation(input);
        }
    }

    // NTS strip type
    public enum StripType {
        LINE, POINT;

        private final static int LINE_INDEX = 0;
        private final static int POINT_INDEX = 1;
    }

    // NTS strip gravity
    public enum StripGravity {
        BOTTOM, TOP;

        private final static int BOTTOM_INDEX = 0;
        private final static int TOP_INDEX = 1;
    }

    // Out listener for selected index
    public interface OnTabStripSelectedIndexListener {
        void onStartTabSelected(final String title, final int index);

        void onEndTabSelected(final String title, final int index);
    }
}
