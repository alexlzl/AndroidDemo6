package com.demo.library;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.RectF;
import android.os.Handler;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;


public class LoadingView extends View {

    //绘制圆环进度条画笔
    private Paint mPaintProgress;
    //绘制圆环背景画笔
    private Paint mPaintLoaded;
    //圆环所在范围的矩形
    private RectF mRectFProgress;
    //中心点坐标
    private int mCenterX, mCenterY;
    //绘制圆环进度条颜色
    private int mDefaultProgressColor = 0xFF1FB3FF;
    //尾部进度
    private int mProgressFoot = 0;
    //头部进度
    private int mProgressHead = 0;
    //进度最大值
    private int maxProgress = 100;
    //是否正在加载
    private boolean isLoading;
    //头部速度大于尾部
    private boolean isChanse;
    //旋转时间
    private static int PROGRESS_DELAY = 5;
    //加载成功动画路径
    private Path mPathSuccess;
    //加载失败动画路径
    private Path mPathFailed;
    //加载成功路径坐标
    private float pathX, pathY;
    //加载失败路径坐标
    private float pathX2, pathY2;
    //控件宽高
    private int minSide;
    //内边框
    private static float padding = 20.0f;
    //更新圆环进度的handler
    private Handler mHandlerLoading;
    private Handler mHandlerFailed;
    private Handler mHandlerSuccess;
    //加载线程
    private Runnable mRunnableLoading;
    private Runnable mRunnableSuccess;
    private Runnable mRunnableFailed;
    //    private boolean isClear;
    private boolean isSuccess;


    public LoadingView(Context context) {
        super(context);
        init();
    }

    public LoadingView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public LoadingView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    /**
     * Describe: 初始化
     * <p>
     * Author: lzl
     * <p>
     * Time: 2016/11/5 下午6:22
     */
    private void init() {
        initHandler();
        initLoadingRunnable();
        initLoadSuccessRunnable();
        initLoadFailedRunnable();
        initPaintProgress();
        initPaintProgressBg();
        mRectFProgress = new RectF();
        mPathSuccess = new Path();
        mPathFailed = new Path();
        startLoading();
    }

    /**
     * Describe: 初始化handler
     * <p>
     * Author: lzl
     * <p>
     * Time: 2016/11/5 下午6:36
     */
    private void initHandler() {
        mHandlerLoading = new Handler();
        mHandlerFailed = new Handler();
        mHandlerSuccess = new Handler();
    }

    /**
     * Describe: 更新圆形进度任务
     * <p>
     * Author: lzl
     * <p>
     * Time: 2016/11/5 下午6:37
     */
    private void initLoadingRunnable() {

        mRunnableLoading = new Runnable() {
            @Override
            public void run() {
                if (isLoading) {

                    if (mProgressHead > mProgressFoot + 95) {
                        isChanse = true;
                    }

                    if (mProgressHead < mProgressFoot + 5) {
                        isChanse = false;
                    }
                    if (isChanse) {
                        mProgressFoot += 2;
                        mProgressHead += 1;
                    } else {
                        mProgressFoot += 1;
                        mProgressHead += 2;
                    }
                    if (mProgressHead >= maxProgress) {
                        mProgressHead = 0;
                        mProgressFoot = mProgressFoot - maxProgress;
                    }
                    setProgressFoot(mProgressFoot);
                    setProgressHead(mProgressHead);
                    mHandlerLoading.postDelayed(mRunnableLoading, PROGRESS_DELAY);
                }
            }
        };

    }

    /**
     * Describe: 初始化绘制进度圆环的画笔
     * <p>
     * Author: lzl
     * <p>
     * Time: 2016/11/5 下午6:19
     */
    private void initPaintProgress() {
        mPaintProgress = new Paint();
        mPaintProgress.setAntiAlias(true);
        mPaintProgress.setColor(mDefaultProgressColor);
        mPaintProgress.setStyle(Paint.Style.STROKE);
        mPaintProgress.setStrokeWidth(12.0f);
    }

    /**
     * Describe: 初始化绘制圆环背景的画笔
     * <p>
     * Author: lzl
     * <p>
     * Time: 2016/11/5 下午6:21
     */
    private void initPaintProgressBg() {
        mPaintLoaded = new Paint();
        mPaintLoaded.setAntiAlias(true);
        mPaintLoaded.setColor(Color.WHITE);
        mPaintLoaded.setStyle(Paint.Style.STROKE);
        mPaintLoaded.setStrokeWidth(13.0f);
    }

    private void setProgressHead(int progress) {
        this.mProgressHead = progress;
        postInvalidate();
    }

    private void setProgressFoot(int progress) {
        this.mProgressFoot = progress;
        postInvalidate();
    }

    /**
     * Describe: 更新加载成功路径数据
     * <p>
     * Author: lzl
     * <p>
     * Time: 2016/11/5 下午6:37
     */
    private void initLoadSuccessRunnable() {
        mRunnableSuccess = new Runnable() {
            @Override
            public void run() {
                if (pathX < mCenterX - 10) {
                    /**
                     * 第一部分路径参数
                     */
                    pathX += 5.5f;
                    pathY += 5.0f;
                    setPaint1LineTo(pathX, pathY);
                    mHandlerSuccess.postDelayed(mRunnableSuccess, PROGRESS_DELAY);
                } else if (pathX < minSide - mCenterX / 4 - padding - 5) {
                    /**
                     * 第二部分路径参数
                     */
                    pathX += 5.0f;
                    pathY -= 5.5f;
                    setPaint1LineTo(pathX, pathY);
                    mHandlerSuccess.postDelayed(mRunnableSuccess, PROGRESS_DELAY);
                }
            }
        };
    }

    /**
     * Describe: 更新加载失败路径数据
     * <p>
     * Author: lzl
     * <p>
     * Time: 2016/11/5 下午6:37
     */
    private void initLoadFailedRunnable() {

        mRunnableFailed = new Runnable() {
            @Override
            public void run() {
                if (pathX < minSide - mCenterX / 3 - padding - 20) {
                    /**
                     * 左侧路径位置变化
                     */
                    pathX += 5.0f;
                    pathY += 5.0f;
                    setPaint1LineTo(pathX, pathY);
                    mHandlerFailed.postDelayed(mRunnableFailed, PROGRESS_DELAY);
                } else if (pathX2 > mCenterX / 3 + padding + 20) {
                    /**
                     * 右侧路径位置变化
                     */
                    pathX2 -= 5.0f;
                    pathY2 += 5.0f;
                    setPaint2LineTo(pathX2, pathY2);
                    mHandlerFailed.postDelayed(mRunnableFailed, PROGRESS_DELAY);
                }
            }
        };

    }

    /**
     * Describe: 连接路径起点和下一个点的连线
     * <p>
     * Author: lzl
     * <p>
     * Time: 2016/11/7 下午2:13
     */
    private void setPaint1LineTo(float x, float y) {
        this.mPathSuccess.lineTo(x, y);
        postInvalidate();
    }

    private void setPaint2LineTo(float x, float y) {
        this.mPathFailed.lineTo(x, y);
        postInvalidate();
    }


    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int width = MeasureSpec.getSize(widthMeasureSpec);
        int height = MeasureSpec.getSize(heightMeasureSpec);
        minSide = width < height ? width : height;
        this.setMeasuredDimension(width, height);
        //圆弧中心坐标
        mCenterX = minSide / 2;
        mCenterY = minSide / 2;
        mRectFProgress.set(padding, padding, minSide - padding, minSide - padding);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
//        if (isClear) {
//            /**
//             * 每次调用加载失败或者加载成功绘制，先将画布之前的绘制内容初始化
//             */
////            clearCanvas(canvas);
//            if (isSuccess){
//                mPathSuccess.reset();
//                initPaintProgress();
//                canvas.drawPath(mPathSuccess, mPaintProgress);
//                pathX = padding + mCenterX / 4;
//                pathY = mCenterY;
//                //将路径移动到加载成功路径的起点位置
//                mPathSuccess.moveTo(pathX, pathY);
//                isClear=false;
//            }else{
//                mPathSuccess.reset();
//                mPathFailed.reset();
//                initPaintProgress();
//                canvas.drawPath(mPathSuccess, mPaintProgress);
//                canvas.drawPath(mPathFailed, mPaintProgress);
//                /**
//                 * 加载失败左侧路径
//                 */
//                pathX = padding + mCenterX / 3 + 20;
//                pathY = padding + mCenterX / 3 + 20;
//                mPathSuccess.moveTo(pathX, pathY);
//                /**
//                 * 加载失败右侧路径
//                 */
//                pathX2 = minSide - mCenterX / 3 - padding - 20;
//                pathY2 = padding + mCenterX / 3 + 20;
//                mPathFailed.moveTo(pathX2, pathY2);
//                isClear=false;
//            }
//        }
        if (isLoading) {
            /**
             * 绘制加载中路径==============
             */

//            initPaintProgress();
            canvas.drawArc(mRectFProgress, 0, 360, false, mPaintLoaded);
            canvas.drawArc(mRectFProgress, calculateProgressFoot(), calculateProgressHead(), false, mPaintProgress);
        } else {
            /**
             * 绘制加载完成路径:圆环
             */
            canvas.drawArc(mRectFProgress, 0, 360, false, mPaintProgress);
        }
        if (isSuccess) {
            /**
             * 绘制加载成功路径======================
             */
//            initPaintProgress();
            canvas.drawPath(mPathSuccess, mPaintProgress);
        } else {
            /**
             * 绘制加载失败路径=================
             */
//            initPaintProgress();

            canvas.drawPath(mPathSuccess, mPaintProgress);
            canvas.drawPath(mPathFailed, mPaintProgress);
        }


    }

    /**
     * Describe: 清屏
     * <p>
     * Author: lzl
     * <p>
     * Time: 2016/11/7 下午6:27
     */
    private void clearCanvas(Canvas canvas) {
//        mPaintProgress.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
//        canvas.drawPaint(mPaintProgress);
//        mPaintProgress.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC));
        canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
//        isClear = false;
    }

    /**
     * Describe: 计算弧形结束时的角度:最大值为100，占据360度的百分比
     * <p>
     * Author: lzl
     * <p>
     * Time: 2016/11/7 下午1:42
     */
    private int calculateProgressHead() {
        return (360 * (mProgressHead - mProgressFoot)) / maxProgress;
    }

    /**
     * Describe: 计算弧形开始时的弧度
     * <p>
     * Author: lzl
     * <p>
     * Time: 2016/11/7 下午1:44
     */
    private int calculateProgressFoot() {
        return (360 * mProgressFoot) / maxProgress;
    }

    /**
     * Describe: 加载完成
     * <p>
     * Author: lzl
     * <p>
     * Time: 2016/11/7 下午1:50
     */
    public void loadingFinished(boolean result, String color) {
        setProgressColor(color);
        isLoading = false;
        if (result) {
            loadSuccess();
        } else {
            loadFailed();
        }
    }

    public void setProgressColor(String color) {
        if (!TextUtils.isEmpty(color)) {
            mPaintProgress.setColor(Color.parseColor(color));
        }


    }


    /**
     * Describe: 加载完成
     * <p>
     * Author: lzl
     * <p>
     * Time: 2016/11/7 下午1:50
     */
    public void loadingComplete(boolean result) {
        isLoading = false;
        if (result) {
            loadSuccess();
        } else {
            loadFailed();
        }
    }

    /**
     * Describe: 启动加载失败路径动画:2个path路径
     * <p>
     * Author: lzl
     * <p>
     * Time: 2016/11/7 下午2:35
     */
    private void loadFailed() {
//        isClear = true;
        isSuccess = false;
        mPathSuccess.reset();
        mPathFailed.reset();
        /**
         * 加载失败左侧路径
         */
        pathX = padding + mCenterX / 3 + 20;
        pathY = padding + mCenterX / 3 + 20;
        mPathSuccess.moveTo(pathX, pathY);
        /**
         * 加载失败右侧路径
         */
        pathX2 = minSide - mCenterX / 3 - padding - 20;
        pathY2 = padding + mCenterX / 3 + 20;
        mPathFailed.moveTo(pathX2, pathY2);
        mHandlerFailed.removeCallbacksAndMessages(null);
        mHandlerFailed.postDelayed(mRunnableFailed, PROGRESS_DELAY);
    }

    /**
     * Describe: 启动加载成功路径动画:1个path路径
     * <p>
     * Author: lzl
     * <p>
     * Time: 2016/11/7 下午2:35
     */
    private void loadSuccess() {
//        isClear = true;
        isSuccess = true;
        mPathSuccess.reset();
        //加载成功路径起点坐标
        pathX = padding + mCenterX / 4;
        pathY = mCenterY;
        //将路径移动到加载成功路径的起点位置
        mPathSuccess.moveTo(pathX, pathY);
        mHandlerSuccess.removeCallbacksAndMessages(null);
        mHandlerSuccess.postDelayed(mRunnableSuccess, PROGRESS_DELAY);
    }

    /**
     * Describe: 启动旋转加载动画
     * <p>
     * Author: lzl
     * <p>
     * Time: 2016/11/5 下午5:59
     */
    public void startLoading() {
        isLoading = true;
        isChanse = false;
        mPathFailed.reset();
        mPathSuccess.reset();
        mHandlerLoading.removeCallbacksAndMessages(null);
        mHandlerLoading.postDelayed(mRunnableLoading, PROGRESS_DELAY);
    }


}
