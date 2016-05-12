package cn.modificator.circleindicator;

import android.content.Context;
import android.content.res.TypedArray;
import android.database.DataSetObserver;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.support.annotation.ColorInt;
import android.support.annotation.FloatRange;
import android.support.annotation.IntDef;
import android.support.annotation.IntRange;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.View;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Created by modificator on 16-5-11.
 */
public class CircleIndicator extends View implements ViewPager.OnPageChangeListener {

    public static final int GRAVITY_FILL = 0;
    public static final int GRAVITY_CENTER = 1;
    public static final int GRAVITY_LEFT = 2;
    public static final int GRAVITY_RIGHT = 3;

    private ViewPager mViewPager;
    //point 对齐方式
//    @PointGravity
    private int pointGravity = 0;
    //圆环宽度百分比
    private float ringWidth = 0.2f;
    //点的背景色
    private int pointBgColor = 0xffaaaaaa;
    //圆环前景色
    private int ringColor = 0xff000000;
    //viewpager的页码
    private int position = 0;
    //Value from [0, 1) indicating the offset from the page at position.
    float positionOffset = 0;
    //画笔
    private Paint paint;
    //观察viewpager 页面数变化
    DataSetObserver dataSetObserver;

    @IntDef(flag = false, value = {GRAVITY_FILL, GRAVITY_CENTER, GRAVITY_LEFT, GRAVITY_RIGHT})
    @Retention(RetentionPolicy.SOURCE)
    public @interface PointGravity {
    }

    public CircleIndicator(Context context) {
        this(context, null);
    }

    public CircleIndicator(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CircleIndicator(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        //获取属性
        TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.indicator, defStyleAttr, 0);
        pointGravity = ta.getInt(R.styleable.indicator_pointGravity, pointGravity);
        ringWidth = ta.getFraction(R.styleable.indicator_ringWidth, 1, 1, ringWidth);
        pointBgColor = ta.getColor(R.styleable.indicator_pointBgColor, pointBgColor);
        ringColor = ta.getColor(R.styleable.indicator_ringColor, ringColor);
        ta.recycle();
        init();
    }

    /**
     * 初始化画笔
     */
    private void init() {
        paint = new Paint();
        paint.setStyle(Paint.Style.STROKE);
        //当viewpager 页面删除或增加的时候实时改变指示器
        dataSetObserver = new DataSetObserver() {
            @Override
            public void onChanged() {
                super.onChanged();
                invalidate();
            }

            @Override
            public void onInvalidated() {
                super.onInvalidated();
                invalidate();
            }
        };
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (mViewPager == null || mViewPager.getAdapter() == null || mViewPager.getAdapter().getCount() == 0)
            return;
        //绘制个数
        int pointCount = mViewPager.getAdapter().getCount();
        //获取直径
        float pointSize = Math.min(getWidth() * 1f / pointCount, getHeight());
        //设置画笔大小
        paint.setStrokeWidth(pointSize * ringWidth / 2f);
        //计算绘制矩形宽度，如果大于View宽度则设置为填充 pointCount + 1 是为两边留出空位
        if ((pointCount * pointSize + (pointCount + 1) * pointSize / 3) > getWidth())
            pointGravity = GRAVITY_FILL;

        drawBgPoint(canvas, pointCount, pointSize);
        drawRingLine(canvas, pointCount, pointSize);
    }

    /**
     * 绘制背景
     *
     * @param canvas
     * @param pointCount
     * @param pointSize
     */
    private void drawBgPoint(Canvas canvas, int pointCount, float pointSize) {
        //设置画笔样式为填充
        paint.setStyle(Paint.Style.FILL);
        //设置背景色
        paint.setColor(pointBgColor);
        if (pointGravity == GRAVITY_FILL) {
            float boxWidth = getWidth() * 1f / pointCount;
            for (int i = 0; i < pointCount; i++) {
                float circleCenterX = boxWidth * i + (boxWidth / 2f);
                float circleCenterY = getHeight() / 2f;
                canvas.drawCircle(circleCenterX, circleCenterY, pointSize / 2f, paint);
            }
        } else {
            //这个变量现在的值是 第一个点到view左边的距离
            float drawLeft = getWidth() - (pointCount * pointSize + (pointCount - 1) * pointSize / 3);
            if (pointGravity == GRAVITY_CENTER) {
                //居中，so 一边一半
                drawLeft = drawLeft / 2;
            } else if (pointGravity == GRAVITY_RIGHT) {
                //右对齐，最右边留下点距离
                drawLeft = drawLeft - pointSize / 3;
            } else {
                //最对齐，
                drawLeft = pointSize / 3;
            }

            //背景圆的中心点
            float circleCenterY = getHeight() / 2f;
            float circleCenterX = 0;
            for (int i = 0; i < pointCount; i++) {
                circleCenterX = drawLeft + pointSize / 2;// + pointSize * 13 / 10 * (i - 1);//* i + pointSize / 3 * (i - 1) + pointSize * i;
                drawLeft = drawLeft + pointSize * 13 / 10;
                canvas.drawCircle(circleCenterX, circleCenterY, pointSize / 2f, paint);
            }
        }
    }

    private void drawRingLine(Canvas canvas, int pointCount, float pointSize) {
        paint.setStyle(Paint.Style.STROKE);
        paint.setColor(ringColor);
        //计算运动路径长度
        double pathLength = pointSize * Math.PI + (pointGravity == GRAVITY_FILL ? (getWidth() * 1f / pointCount) : (pointSize * 13 / 10));
        //计算最第一个点左侧x坐标
        float drawLeft = getWidth() - (pointCount * pointSize + (pointCount - 1) * pointSize / 3);

        //position * pointSize * 13 / 10 这个是点的宽度和中间间距的长度
        if (pointGravity == GRAVITY_CENTER) {
            drawLeft = drawLeft / 2 + position * pointSize * 13 / 10;
        } else if (pointGravity == GRAVITY_RIGHT) {
            drawLeft = drawLeft - pointSize / 3 + position * pointSize * 13 / 10;
        } else {
            drawLeft = pointSize / 3 + position * pointSize * 13 / 10;
        }
        //fill的时候确定中心点
        float boxWidth = getWidth() * 1f / pointCount;
        //画笔宽度恩，要把圈环画到背景里面需要用到
        float paintWidth = paint.getStrokeWidth();

        //计算第一个圆环的绘制范围
        RectF lastRectF = new RectF();
        float lastCenterX = pointGravity == GRAVITY_FILL ? (boxWidth * position + (boxWidth / 2)) : (drawLeft + pointSize / 2);
        float lastCenterY = getHeight() / 2f;
        lastRectF.left = lastCenterX - pointSize / 2 + paintWidth / 2;
        lastRectF.top = lastCenterY - pointSize / 2 + paintWidth / 2;
        lastRectF.right = lastCenterX + pointSize / 2 - paintWidth / 2;
        lastRectF.bottom = lastCenterY + pointSize / 2 - paintWidth / 2;
        //计算第一个圆环绘制角度，保证第二个圆环和下面的线联动
        float lastAngle = (float) (360 * ((pointSize * Math.PI / pathLength - positionOffset) / (pointSize * Math.PI / pathLength)));
        //避免圆环继续往回画
        lastAngle = lastAngle > 0 ? lastAngle : 0;
        canvas.drawArc(lastRectF, 90.0f, lastAngle, false, paint);

        //计算第二个圆环的绘制范围
        RectF nextRectF = new RectF();
        float nextCenterX = pointGravity == GRAVITY_FILL ? (boxWidth * (position + 1) + (boxWidth / 2)) : (drawLeft + pointSize / 2 + pointSize * 13 / 10);
        float nextCenterY = getHeight() / 2f;
        nextRectF.left = nextCenterX - pointSize / 2 + paintWidth / 2;
        nextRectF.top = nextCenterY - pointSize / 2 + paintWidth / 2;
        nextRectF.right = nextCenterX + pointSize / 2 - paintWidth / 2;
        nextRectF.bottom = nextCenterY + pointSize / 2 - paintWidth / 2;
        float nextAngle = (float) (360 * (((1 - positionOffset) - pointSize * Math.PI / pathLength) / (pointSize * Math.PI / pathLength)));
        nextAngle = nextAngle < 0 ? nextAngle : 0;
        canvas.drawArc(nextRectF, 90.0f, nextAngle, false, paint);

        //计算底下的线的绘制范围
        float lineTop = nextRectF.bottom - paint.getStrokeWidth() / 2;
        float lineRight = Double.valueOf(lastCenterX + pathLength * positionOffset).floatValue();
        float lineLeft = Double.valueOf(lineRight - pointSize * Math.PI).floatValue();
        float lineBottom = lineTop + paint.getStrokeWidth();
        //避免绘制超出两个圆心
        lineLeft = lineLeft > nextCenterX ? nextCenterX : lineLeft < lastCenterX ? lastCenterX : lineLeft;
        lineRight = lineRight > nextCenterX ? nextCenterX : lineRight;

        //别问我为啥用path ， line根本满足不了好伐
        Path linePath = new Path();
        linePath.moveTo(lineLeft, lineTop);
        linePath.lineTo(lineRight, lineTop);
        linePath.lineTo(lineRight, lineBottom);
        linePath.lineTo(lineLeft, lineBottom);

        //将画笔影响降到最低，不然会超出，虽然还是会超出1像素
        paint.setStrokeWidth(1);
        paint.setStyle(Paint.Style.FILL);
        canvas.drawPath(linePath, paint);
    }

    /**
     * 设置viewpager滑动监听
     * @param viewpager
     */
    public void setViewPager(ViewPager viewpager) {
        this.mViewPager = viewpager;
        mViewPager.addOnPageChangeListener(this);
        mViewPager.getAdapter().registerDataSetObserver(dataSetObserver);
    }

    @Override
    protected void onVisibilityChanged(View changedView, int visibility) {
        super.onVisibilityChanged(changedView, visibility);
        if (mViewPager != null && mViewPager.getAdapter() != null)
            if (visibility == VISIBLE) {
                mViewPager.addOnPageChangeListener(this);
                try {//上面设置观察者后在设置会挂掉
                    mViewPager.getAdapter().registerDataSetObserver(dataSetObserver);
                } catch (Exception e) {
                }
            } else {
                mViewPager.removeOnPageChangeListener(this);
                mViewPager.getAdapter().unregisterDataSetObserver(dataSetObserver);
            }
    }

    /**
     * 画笔宽度百分比
     *
     * @param ringWidth
     */
    public void setRingWidth(@FloatRange(from = 0, to = 1)
                             float ringWidth) {
        this.ringWidth = ringWidth;
        invalidate();
    }

    /**
     * 设置对齐方式
     *
     * @param pointGravity
     */
    public void setPointGravity(@PointGravity
                                int pointGravity) {
        this.pointGravity = pointGravity;
        invalidate();
    }

    /**
     * 设置圆环颜色
     *
     * @param ringColor 0x00000000 - 0xffffffff
     */
    public void setRingColor(@ColorInt
                             @IntRange(from = 0x00000000, to = 0xffffffff)
                             int ringColor) {
        this.ringColor = ringColor;
    }

    /**
     * 设置point 背景色
     *
     * @param pointBgColor 0x00000000 - 0xffffffff
     */
    public void setPointBgColor(@ColorInt
                                @IntRange(from = 0x00000000, to = 0xffffffff)
                                int pointBgColor) {
        this.pointBgColor = pointBgColor;
    }

    public float getRingWidth() {
        return ringWidth;
    }

    public int getPointGravity() {
        return pointGravity;
    }

    public int getRingColor() {
        return ringColor;
    }

    public int getPointBgColor() {
        return pointBgColor;
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
        this.position = position;
        this.positionOffset = positionOffset;
        this.invalidate();
    }

    @Override
    public void onPageSelected(int position) {

    }

    @Override
    public void onPageScrollStateChanged(int state) {

    }
}
