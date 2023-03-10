package com.humannote.me.views;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Region;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.OvalShape;
import android.os.Build;
import android.text.TextPaint;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.widget.Scroller;

import com.humannote.me.bizs.CalendarBiz;
import com.humannote.me.entities.BGCircle;
import com.humannote.me.utils.DateUtils;

import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 月视图
 *
 * @author AigeStudio 2015-05-21
 */
@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class MonthView extends View implements ValueAnimator.AnimatorUpdateListener {
    private Paint mPaint;
    private TextPaint mTextPaint;
    private Scroller mScroller;

    private CalendarBiz mCalendarBiz;

    private OnPageChangeListener onPageChangeListener;
    private OnSizeChangedListener onSizeChangedListener;

    private int sizeBase;
    private int lastPointX;
    private int lastMoveX;
    private int width, height;
    private int criticalWidth;
    private int index;
    private int lastMonth, currentMonth, nextMonth;
    private int lastYear, currentYear, nextYear;
    private int animZoomOut1, animZoomIn1, animZoomOut2;
    private int circleRadius;
    private int colorMain = 0xFFE95344;

    private float textSizeGregorian, textSizeLunar;
    private float offsetYLunar;

    private boolean isLunarShow = true;

    private EventType mEventType;

    private Map<Integer, List<Region>> calendarRegion = new HashMap<>();
    private Region[][] mRegion = new Region[6][7];
    private Map<String, BGCircle> circlesAppear = new HashMap<>();
    private Map<String, BGCircle> circlesDisappear = new HashMap<>();
    private List<String> dateSelected = new ArrayList<>();
    private Context mContext;
    private HashMap<String, Integer> otherMonthDays = new HashMap<>();

    private enum EventType {
        SINGLE, MULTIPLE
    }

    /**
     * 页面改变监听接口
     */
    public interface OnPageChangeListener {
        /**
         * 月份改变回调方法
         *
         * @param month 当前页面显示的月份
         */
        void onMonthChange(int month);

        /**
         * 年份改变回调方法
         *
         * @param year 当前页面显示的年份
         */
        void onYearChange(int year);

        /**
         * 日期
         *
         * @param year
         * @param month
         */
        void onDateChange(int year, int month);

        /**
         * @param date
         */
        void onSelectDate(Date date);

        /**
         * @param canvas
         * @param x
         * @param y
         * @param date
         */
        void onDrawDate(Canvas canvas, float x, float y, Date date);
    }

    /**
     * 尺寸改变监听接口
     * 当月视图的基准边改变时需要回调给标题视图
     */
    public interface OnSizeChangedListener {
        /**
         * 尺寸改变回调方法
         *
         * @param size 改变后的基准边
         */
        void onSizeChanged(int size);
    }

    public MonthView(Context context) {
        this(context, null);
    }

    public MonthView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public MonthView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mContext = context;
        this.init(new Date(System.currentTimeMillis()));
    }

    /**
     * @param date
     */
    private void init(Date date) {
        mEventType = EventType.SINGLE;
        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.DITHER_FLAG);
        mTextPaint = new TextPaint(Paint.ANTI_ALIAS_FLAG | Paint.DITHER_FLAG | Paint.DEV_KERN_TEXT_FLAG);
        mTextPaint.setTextAlign(Paint.Align.CENTER);
        mScroller = new Scroller(mContext);
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        currentYear = calendar.get(Calendar.YEAR);
        currentMonth = calendar.get(Calendar.MONTH) + 1;
        mCalendarBiz = new CalendarBiz(index, currentYear, currentMonth);
        buildCalendarRegion();
        computeDate();
    }

    /**
     */
    public void setCurrentDate(Date date) {
        try {
            this.init(date);
            this.invalidate();
            // 清空选中的日期
            for (final String item : dateSelected) {
                BGCircle circle = this.circlesAppear.get(item);
                ValueAnimator animScale = ObjectAnimator.ofInt(circle, "radius", circleRadius, 0);
                animScale.setDuration(250);
                animScale.setInterpolator(new AccelerateInterpolator());
                animScale.addUpdateListener(this);
                animScale.addListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        circlesDisappear.remove(item);
                    }
                });
                animScale.start();
                this.circlesDisappear.put(item, circle);
                this.circlesAppear.remove(item);
            }
            this.dateSelected.clear();
            this.setSelectDate(date);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
     * @param eventType
     */
    public void setEventType(EventType eventType) {
        this.mEventType = eventType;
    }

    /**
     * 设置页面改变时的监听器
     *
     * @param onPageChangeListener ...
     */
    public void setOnPageChangeListener(OnPageChangeListener onPageChangeListener) {
        this.onPageChangeListener = onPageChangeListener;
        if (null != this.onPageChangeListener) {
            this.onPageChangeListener.onYearChange(currentYear);
            this.onPageChangeListener.onMonthChange(currentMonth);
            this.onPageChangeListener.onDateChange(currentYear, currentMonth);
        }
    }

    /**
     * 设置尺寸改变时的监听器
     *
     * @param onSizeChangedListener ...
     */
    public void setOnSizeChangedListener(OnSizeChangedListener onSizeChangedListener) {
        this.onSizeChangedListener = onSizeChangedListener;
    }

    /**
     * 获取选择了的日期
     *
     * @return 选择了的日期列表
     */
    public List<String> getDateSelected() {
        return dateSelected;
    }

    /**
     * 设置是否显示农历
     *
     * @param isLunarShow ...
     */
    public void setLunarShow(boolean isLunarShow) {
        this.isLunarShow = isLunarShow;
        invalidate();
    }

    /**
     * 设置主色调
     *
     * @param colorMain ...
     */
    public void setColorMain(int colorMain) {
        this.colorMain = colorMain;
        invalidate();
    }

    private void computeDate() {
        nextYear = lastYear = currentYear;

        nextMonth = currentMonth + 1;
        lastMonth = currentMonth - 1;

        if (null != onPageChangeListener) {
            onPageChangeListener.onYearChange(currentYear);
        }
        if (currentMonth == 12) {
            nextYear++;

            mCalendarBiz.buildSolarTerm(nextYear);
            if (null != onPageChangeListener) {
                onPageChangeListener.onYearChange(nextYear);
            }
            nextMonth = 1;
        }
        if (currentMonth == 1) {
            lastYear--;

            mCalendarBiz.buildSolarTerm(lastYear);
            if (null != onPageChangeListener) {
                onPageChangeListener.onYearChange(lastYear);
            }
            lastMonth = 12;
        }
    }

    private void buildCalendarRegion() {
        if (!calendarRegion.containsKey(index)) {
            List<Region> regions = new ArrayList<>();
            calendarRegion.put(index, regions);
        }
    }

    private String[][] gregorianToLunar(String[][] gregorian, int year, int month) {
        String[][] lunar = new String[6][7];
        int row, tempYear, tempMonth;
        for (int i = 0; i < gregorian.length; i++) {
            row = i;
            for (int j = 0; j < gregorian[i].length; j++) {
                tempYear = year;
                tempMonth = month;
                String str = gregorian[i][j];
                if (null == str) {
                    str = "";
                } else {
                    int day = Integer.parseInt(str);
                    if (row == 0 && day > 20) {
                        tempMonth = tempMonth - 1;
                        if (tempMonth == 0) {
                            tempYear = tempYear - 1;
                            tempMonth = 12;
                        }
                    } else if (row >= 4 && day < 20) {
                        tempMonth = tempMonth + 1;
                        if (tempMonth > 12) {
                            tempYear = tempYear + 1;
                            tempMonth = 1;
                        }
                    }
                    str = mCalendarBiz.gregorianToLunar(tempYear, tempMonth, day);
                }
                lunar[i][j] = str;
            }
        }
        return lunar;
    }

    private BGCircle createCircle(float x, float y) {
        OvalShape circle = new OvalShape();
        circle.resize(0, 0);
        ShapeDrawable drawable = new ShapeDrawable(circle);
        BGCircle circle1 = new BGCircle(drawable);
        circle1.setX(x);
        circle1.setY(y);
        drawable.getPaint().setColor(0xFFDCDCDC);
        return circle1;
    }

    /**
     * @param x
     * @param y
     */
    private void defineContainRegion(int x, int y) {
        try {
            String selectDate = "";
            if (EventType.MULTIPLE == this.mEventType) {
                selectDate = this.multipleDate(x, y);
            } else {
                selectDate = this.singleDate(x, y);
            }
            if (null != onPageChangeListener && !TextUtils.isEmpty(selectDate)) {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                java.util.Date date = sdf.parse(selectDate);
                this.onPageChangeListener.onSelectDate(date);
            }
        } catch (Exception ex) {

        }
    }

    /**
     * 单选日期
     *
     * @param x
     * @param y
     */
    private String singleDate(int x, int y) {
        String selectDate = "";
        for (int i = 0; i < mRegion.length; i++) {
            for (int j = 0; j < mRegion[i].length; j++) {
                Region region = mRegion[i][j];
                if (null == mCalendarBiz.getGregorianCreated().get(index)[i][j]) {
                    continue;
                }
                if (region.contains(x, y)) {
                    List<Region> regions = calendarRegion.get(index);
                    if (regions.contains(region)) {
                        regions.remove(region);
                    } else {
                        regions.add(region);
                    }
                    for (final String item : dateSelected) {
                        BGCircle circle = circlesAppear.get(item);
                        ValueAnimator animScale = ObjectAnimator.ofInt(circle, "radius", circleRadius, 0);
                        animScale.setDuration(250);
                        animScale.setInterpolator(new AccelerateInterpolator());
                        animScale.addUpdateListener(this);
                        animScale.addListener(new AnimatorListenerAdapter() {
                            @Override
                            public void onAnimationEnd(Animator animation) {
                                circlesDisappear.remove(item);
                            }
                        });
                        animScale.start();
                        circlesDisappear.put(item, circle);
                        circlesAppear.remove(item);
                    }
                    dateSelected.clear();
                    //String date = currentYear + "-" + currentMonth + "-" + mCalendarBiz.getGregorianCreated().get(index)[i][j];
                    selectDate = MessageFormat.format("{0}-{1}-{2}", String.valueOf(currentYear), currentMonth, mCalendarBiz.getGregorianCreated().get(index)[i][j]);
                    String key = MessageFormat.format("{0}_{1}_{2}_{3}", String.valueOf(this.currentYear), this.currentMonth, i, j);
                    if (this.otherMonthDays.containsKey(key)) {
                        int year = this.currentYear;
                        int month = this.otherMonthDays.get(key);
                        if (month < 1) {
                            year = year - 1;
                            month = 12;
                        }
                        if (month > 12) {
                            year = year + 1;
                            month = 1;
                        }
                        selectDate = MessageFormat.format("{0}-{1}-{2}", String.valueOf(year), month, mCalendarBiz.getGregorianCreated().get(index)[i][j]);
                    }
                    dateSelected.add(selectDate);
                    BGCircle circle = createCircle(region.getBounds().centerX() + index * sizeBase, region.getBounds().centerY());
                    ValueAnimator animScale1 = ObjectAnimator.ofInt(circle, "radius", 0, animZoomOut1);
                    animScale1.setDuration(250);
                    animScale1.setInterpolator(new DecelerateInterpolator());
                    animScale1.addUpdateListener(this);

                    ValueAnimator animScale2 = ObjectAnimator.ofInt(circle, "radius", animZoomOut1, animZoomIn1);
                    animScale2.setDuration(100);
                    animScale2.setInterpolator(new AccelerateInterpolator());
                    animScale2.addUpdateListener(this);

                    ValueAnimator animScale3 = ObjectAnimator.ofInt(circle, "radius", animZoomIn1, animZoomOut2);
                    animScale3.setDuration(150);
                    animScale3.setInterpolator(new DecelerateInterpolator());
                    animScale3.addUpdateListener(this);

                    ValueAnimator animScale4 = ObjectAnimator.ofInt(circle, "radius", animZoomOut2, circleRadius);
                    animScale4.setDuration(50);
                    animScale4.setInterpolator(new AccelerateInterpolator());
                    animScale4.addUpdateListener(this);
                    AnimatorSet animSet = new AnimatorSet();
                    animSet.playSequentially(animScale1, animScale2, animScale3, animScale4);
                    animSet.start();
                    circlesAppear.put(selectDate, circle);
                }
            }
        }
        return selectDate;
    }

    /**
     * 多选日期操作
     *
     * @param x
     * @param y
     */
    private String multipleDate(int x, int y) {
        String selectDate = "";
        for (int i = 0; i < mRegion.length; i++) {
            for (int j = 0; j < mRegion[i].length; j++) {
                Region region = mRegion[i][j];
                if (null == mCalendarBiz.getGregorianCreated().get(index)[i][j]) {
                    continue;
                }
                if (region.contains(x, y)) {
                    List<Region> regions = calendarRegion.get(index);
                    if (regions.contains(region)) {
                        regions.remove(region);
                    } else {
                        regions.add(region);
                    }
                    final String date = currentYear + "-" + currentMonth + "-" + mCalendarBiz.getGregorianCreated().get(index)[i][j];
                    selectDate = MessageFormat.format("{0}-{1}-{2}", String.valueOf(currentYear), currentMonth, mCalendarBiz.getGregorianCreated().get(index)[i][j]);
                    if (dateSelected.contains(date)) {
                        dateSelected.remove(date);
                        BGCircle circle = circlesAppear.get(date);
                        ValueAnimator animScale = ObjectAnimator.ofInt(circle, "radius", circleRadius, 0);
                        animScale.setDuration(250);
                        animScale.setInterpolator(new AccelerateInterpolator());
                        animScale.addUpdateListener(this);
                        animScale.addListener(new AnimatorListenerAdapter() {
                            @Override
                            public void onAnimationEnd(Animator animation) {
                                circlesDisappear.remove(date);
                            }
                        });
                        animScale.start();
                        circlesDisappear.put(date, circle);
                        circlesAppear.remove(date);
                    } else {
                        dateSelected.add(date);
                        BGCircle circle = createCircle(region.getBounds().centerX() + index * sizeBase, region.getBounds().centerY());
                        ValueAnimator animScale1 = ObjectAnimator.ofInt(circle, "radius", 0, animZoomOut1);
                        animScale1.setDuration(250);
                        animScale1.setInterpolator(new DecelerateInterpolator());
                        animScale1.addUpdateListener(this);

                        ValueAnimator animScale2 = ObjectAnimator.ofInt(circle, "radius", animZoomOut1, animZoomIn1);
                        animScale2.setDuration(100);
                        animScale2.setInterpolator(new AccelerateInterpolator());
                        animScale2.addUpdateListener(this);

                        ValueAnimator animScale3 = ObjectAnimator.ofInt(circle, "radius", animZoomIn1, animZoomOut2);
                        animScale3.setDuration(150);
                        animScale3.setInterpolator(new DecelerateInterpolator());
                        animScale3.addUpdateListener(this);

                        ValueAnimator animScale4 = ObjectAnimator.ofInt(circle, "radius", animZoomOut2, circleRadius);
                        animScale4.setDuration(50);
                        animScale4.setInterpolator(new AccelerateInterpolator());
                        animScale4.addUpdateListener(this);
                        AnimatorSet animSet = new AnimatorSet();
                        animSet.playSequentially(animScale1, animScale2, animScale3, animScale4);
                        animSet.start();
                        circlesAppear.put(date, circle);
                    }
                }
            }
        }
        return selectDate;
    }

    /**
     * 设置选中的日期
     *
     * @param date
     */
    public void setSelectDate(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH) + 1;
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        String selectKey = MessageFormat.format("{0}-{1}-{2}", String.valueOf(year), month, day);
        // 如果已存在，则直接返回
        if (dateSelected.contains(selectKey)) {
            return;
        }
        int row;
        for (int i = 0; i < mRegion.length; i++) {
            row = i;
            for (int j = 0; j < mRegion[i].length; j++) {
                Region region = mRegion[i][j];
                String key = MessageFormat.format("{0}-{1}-{2}", String.valueOf(this.currentYear), this.currentMonth, this.mCalendarBiz.getGregorianCreated().get(index)[i][j]);
                String str = this.mCalendarBiz.getGregorianCreated().get(index)[i][j];
                int days = Integer.parseInt(str);
                if (row == 0 && days > 20) {
                    key = MessageFormat.format("{0}-{1}-{2}", String.valueOf(this.currentYear), this.currentMonth - 1, this.mCalendarBiz.getGregorianCreated().get(index)[i][j]);
                }
                if (row >= 4 && days < 20) {
                    key = MessageFormat.format("{0}-{1}-{2}", String.valueOf(this.currentYear), this.currentMonth + 1, this.mCalendarBiz.getGregorianCreated().get(index)[i][j]);
                }
                if (selectKey.equals(key) && region != null) {
                    dateSelected.add(key);
                    BGCircle circle = createCircle(region.getBounds().centerX() + index * sizeBase, region.getBounds().centerY());
                    ValueAnimator animScale1 = ObjectAnimator.ofInt(circle, "radius", 0, animZoomOut1);
                    animScale1.setDuration(250);
                    animScale1.setInterpolator(new DecelerateInterpolator());
                    animScale1.addUpdateListener(this);

                    ValueAnimator animScale2 = ObjectAnimator.ofInt(circle, "radius", animZoomOut1, animZoomIn1);
                    animScale2.setDuration(100);
                    animScale2.setInterpolator(new AccelerateInterpolator());
                    animScale2.addUpdateListener(this);

                    ValueAnimator animScale3 = ObjectAnimator.ofInt(circle, "radius", animZoomIn1, animZoomOut2);
                    animScale3.setDuration(150);
                    animScale3.setInterpolator(new DecelerateInterpolator());
                    animScale3.addUpdateListener(this);

                    ValueAnimator animScale4 = ObjectAnimator.ofInt(circle, "radius", animZoomOut2, circleRadius);
                    animScale4.setDuration(50);
                    animScale4.setInterpolator(new AccelerateInterpolator());
                    animScale4.addUpdateListener(this);
                    AnimatorSet animSet = new AnimatorSet();
                    animSet.playSequentially(animScale1, animScale2, animScale3, animScale4);
                    animSet.start();
                    circlesAppear.put(key, circle);
                    return;
                }
            }
        }
    }

    public void smoothScrollTo(int fx, int fy) {
        int dx = fx - mScroller.getFinalX();
        int dy = fy - mScroller.getFinalY();
        smoothScrollBy(dx, dy);
    }

    public void smoothScrollTo(int fx, int fy, int duration) {
        int dx = fx - mScroller.getFinalX();
        int dy = fy - mScroller.getFinalY();
        smoothScrollBy(dx, dy, duration);
    }

    private void smoothScrollBy(int dx, int dy) {
        this.smoothScrollBy(dx, dy, 500);
    }

    private void smoothScrollBy(int dx, int dy, int duration) {
        mScroller.startScroll(mScroller.getFinalX(), mScroller.getFinalY(), dx, dy, duration);
        invalidate();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                lastPointX = (int) event.getX();
                break;
            case MotionEvent.ACTION_MOVE:
                int totalMoveX = (int) (lastPointX - event.getX()) + lastMoveX;
                smoothScrollTo(totalMoveX, 0);
                break;
            case MotionEvent.ACTION_UP:
                if (Math.abs(lastPointX - event.getX()) > 10) {
                    if (lastPointX > event.getX()) {// 下一月
                        setNextMonth(event);
                    } else if (lastPointX < event.getX()) {// 上一月
                        setLastMonth(event);
                    }
                } else {
                    defineContainRegion((int) event.getX(), (int) event.getY());
                }
                break;
        }
        return true;
    }

    @Override
    public void computeScroll() {
        if (mScroller.computeScrollOffset()) {
            scrollTo(mScroller.getCurrX(), mScroller.getCurrY());
            invalidate();
        } else {
            requestLayout();
        }
    }

    /**
     * 设置为上一月
     */
    public void setLastMonth(MotionEvent event) {
        if (event == null || Math.abs(lastPointX - event.getX()) >= criticalWidth) {
            index--;
            currentMonth = (currentMonth - 1) % 12;
            if (currentMonth == 0) {
                currentMonth = 12;
                currentYear--;
                mCalendarBiz.buildSolarTerm(currentYear);
            }
            computeDate();
            if (null != onPageChangeListener) {
                onPageChangeListener.onMonthChange(currentMonth);
                onPageChangeListener.onYearChange(currentYear);
                onPageChangeListener.onDateChange(currentYear, currentMonth);
            }
            buildCalendarRegion();
        }
        if (event == null) {
            // 通过按纽点击，放慢滚动速度
            smoothScrollTo(width * index, 0, 1000);
        } else {
            smoothScrollTo(width * index, 0);
        }
        lastMoveX = width * index;
    }

    /**
     * 设置为下一月
     *
     * @param event
     */
    public void setNextMonth(MotionEvent event) {
        if (event == null || Math.abs(lastPointX - event.getX()) >= criticalWidth) {
            index++;
            currentMonth = (currentMonth + 1) % 13;
            if (currentMonth == 0) {
                currentMonth = 1;
                currentYear++;

                mCalendarBiz.buildSolarTerm(currentYear);
            }
            computeDate();
            if (null != onPageChangeListener) {
                onPageChangeListener.onMonthChange(currentMonth);
                onPageChangeListener.onYearChange(currentYear);
                onPageChangeListener.onDateChange(currentYear, currentMonth);
            }
            buildCalendarRegion();
        }
        if (event == null) {
            // 通过按纽点击，放慢滚动速度
            smoothScrollTo(width * index, 0, 1000);
        } else {
            smoothScrollTo(width * index, 0);
        }
        lastMoveX = width * index;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int measureWidth = MeasureSpec.getSize(widthMeasureSpec);

        String[][] currentGregorian = mCalendarBiz.getGregorianCreated().get(index);

        if (null == currentGregorian[4][0]) {
            setMeasuredDimension(measureWidth, (int) (measureWidth * 4 / 7F));
        } else if (null == currentGregorian[5][0]) {
            setMeasuredDimension(measureWidth, (int) (measureWidth * 5 / 7F));
        } else {
            setMeasuredDimension(measureWidth, (int) (measureWidth * 6 / 7F));
        }
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        width = w;
        height = h;

        criticalWidth = (int) (1F / 5F * width);

        sizeBase = width;

//        if (null != onSizeChangedListener) {
//            onSizeChangedListener.onSizeChanged(sizeBase);
//        }
        int sizeCell = (int) (sizeBase / 7F);
        circleRadius = sizeCell;
        animZoomOut1 = (int) (sizeCell * 1.2F);
        animZoomIn1 = (int) (sizeCell * 0.8F);
        animZoomOut2 = (int) (sizeCell * 1.1F);

        textSizeGregorian = sizeBase / 20F;
        mTextPaint.setTextSize(textSizeGregorian);

        float gregorianH = mTextPaint.getFontMetrics().bottom - mTextPaint.getFontMetrics().top;

        textSizeLunar = sizeBase / 35F;
        mTextPaint.setTextSize(textSizeLunar);

        float lunarH = mTextPaint.getFontMetrics().bottom - mTextPaint.getFontMetrics().top;

        offsetYLunar = (((Math.abs(mTextPaint.ascent() + mTextPaint.descent())) / 2) + lunarH / 2 + gregorianH / 2)
                * 3F / 4F;

        for (int i = 0; i < mRegion.length; i++) {
            for (int j = 0; j < mRegion[i].length; j++) {
                Region region = new Region();
                region.set((j * sizeCell), (i * sizeCell), sizeCell + (j * sizeCell), sizeCell + (i * sizeCell));
                mRegion[i][j] = region;
            }
        }
        if (null != onSizeChangedListener) {
            onSizeChangedListener.onSizeChanged(sizeBase);
        }
    }

    @Override
    public void onAnimationUpdate(ValueAnimator animation) {
        this.invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        drawCircle(canvas);
        drawMonths(canvas);
    }

    private void drawCircle(Canvas canvas) {
        for (String s : circlesDisappear.keySet()) {
            BGCircle circle = circlesDisappear.get(s);
            drawBGCircle(canvas, circle);
        }
        for (String s : circlesAppear.keySet()) {
            BGCircle circle = circlesAppear.get(s);
            drawBGCircle(canvas, circle);
        }
    }

    private void drawBGCircle(Canvas canvas, BGCircle circle) {
        canvas.save();
        canvas.translate(circle.getX() - circle.getRadius() / 2, circle.getY() - circle.getRadius() / 2);
        circle.getShape().getShape().resize(circle.getRadius(), circle.getRadius());
        circle.getShape().draw(canvas);
        canvas.restore();
    }

    private void drawMonths(Canvas canvas) {
        try {
            drawMonth(canvas, (index - 1) * sizeBase, lastYear, lastMonth, false);
            drawMonth(canvas, index * sizeBase, currentYear, currentMonth, true);
            drawMonth(canvas, (index + 1) * sizeBase, nextYear, nextMonth, false);
        } catch (Exception ex) {

        }
    }

    private void drawMonth(Canvas canvas, float offsetX, final int year, final int month, boolean isCurrent) {
        canvas.save();
        canvas.translate(offsetX, 0);

        int current = (int) (offsetX / sizeBase);
        mTextPaint.setTextSize(textSizeGregorian);
        mTextPaint.setColor(Color.BLACK);

        String[][] gregorianCurrent = mCalendarBiz.getGregorianCreated().get(current);

        if (null == gregorianCurrent) {
            gregorianCurrent = mCalendarBiz.buildGregorian(year, month);
        }
        int row;
        for (int i = 0; i < gregorianCurrent.length; i++) {
            row = i;
            for (int j = 0; j < gregorianCurrent[i].length; j++) {
                String str = gregorianCurrent[i][j];
                int day = Integer.parseInt(str);
                Rect rect = mRegion[i][j].getBounds();
                if (isCurrent) {
                    if (onPageChangeListener != null) {
                        String strDate = MessageFormat.format("{0}-{1}-{2}", String.valueOf(year), month, this.mCalendarBiz.getGregorianCreated().get(index)[i][j]);
                        if ((row == 0 && day > 20) || (row >= 4 && day < 20)) {
                            int tempYear = year;
                            int tempMonth = month;
                            if (row == 0 && day > 20) {
                                tempYear = month == 1 ? tempYear - 1 : tempYear;
                                tempMonth = month == 1 ? 12 : tempMonth - 1;
                                strDate = MessageFormat.format("{0}-{1}-{2}", String.valueOf(tempYear), tempMonth, this.mCalendarBiz.getGregorianCreated().get(index)[i][j]);
                            }
                            if (row >= 4 && day < 20) {
                                tempYear = month == 12 ? tempYear + 1 : tempYear;
                                tempMonth = month == 12 ? 1 : tempMonth + 1;
                                strDate = MessageFormat.format("{0}-{1}-{2}", String.valueOf(tempYear), tempMonth, this.mCalendarBiz.getGregorianCreated().get(index)[i][j]);
                            }
                        }
                        Date date = DateUtils.stringToDate(strDate);
                        onPageChangeListener.onDrawDate(canvas, rect.centerX(), rect.centerY(), date);
                    }
                }
                if (null == str) {
                    str = "";
                }
                mTextPaint.setColor(0xFF585858);
                if ((row == 0 && day > 20) || (row >= 4 && day < 20)) {
                    mTextPaint.setColor(0xFFCCCCCC);
                    if (row == 0 && day > 20) {
                        otherMonthDays.put(MessageFormat.format("{0}_{1}_{2}_{3}", String.valueOf(year), month, i, j), month - 1);
                    }
                    if (row >= 4 && day < 20) {
                        otherMonthDays.put(MessageFormat.format("{0}_{1}_{2}_{3}", String.valueOf(year), month, i, j), month + 1);
                    }
                }
                canvas.drawText(str, mRegion[i][j].getBounds().centerX(), mRegion[i][j].getBounds().centerY(), mTextPaint);
            }
        }
        if (isLunarShow) {
            String[][] lunarCurrent = mCalendarBiz.getLunarCreated().get(current);
            if (null == lunarCurrent) {
                lunarCurrent = gregorianToLunar(gregorianCurrent, year, month);
            }
            mTextPaint.setTextSize(textSizeLunar);
            for (int i = 0; i < lunarCurrent.length; i++) {
                row = i;
                for (int j = 0; j < lunarCurrent[i].length; j++) {
                    String str = lunarCurrent[i][j];
                    mTextPaint.setColor(Color.GRAY);
                    int day = Integer.parseInt(gregorianCurrent[i][j]);
                    if ((row == 0 && day > 20) || (row >= 4 && day < 20)) {
                        mTextPaint.setColor(0xFFCCCCCC);
                    } else {
                        if (str.contains(" ")) {
                            str.trim();
                            mTextPaint.setColor(colorMain);
                        }
                    }
                    canvas.drawText(str, mRegion[i][j].getBounds().centerX(), mRegion[i][j].getBounds().centerY() + offsetYLunar, mTextPaint);
                }
            }
            mCalendarBiz.getLunarCreated().put(current, lunarCurrent);
        }
        mCalendarBiz.getGregorianCreated().put(current, gregorianCurrent);
        canvas.restore();
    }
}
