package com.humannote.me.views;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.os.Handler;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.LinearLayout;

import com.humannote.me.interfaces.IPick;
import com.humannote.me.interfaces.OnDateSelected;
import com.humannote.me.interfaces.OnDrawDayListener;
import com.humannote.me.interfaces.OnTitleClickListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * 日期选择器
 *
 * @author AigeStudio 2015-05-21
 */
public class DatePicker extends LinearLayout implements IPick, MonthView.OnPageChangeListener, MonthView.OnSizeChangedListener {
    private MonthView monthView;
    //private TitleView titleView;
    private TitlesView titlesView;
    private WeekView weekView;
    private int year;
    private int month;
    private OnDateSelected onDateSelected;
    private OnDrawDayListener onDrawDayListener;
    private Context mContext;
    private boolean isSelectToday = false;
    private Date selectDate = new Date(System.currentTimeMillis());
    private OnTitleClickListener onTitleClickListener;

    public DatePicker(Context context) {
        this(context, null);
    }

    public DatePicker(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.mContext = context;
        this.initView();
    }

    /**
     *
     */
    private void initView() {
        setBackgroundColor(Color.WHITE);
        setOrientation(VERTICAL);

        LayoutParams llParams = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);

        //titleView = new TitleView(context);
        //addView(titleView, llParams);

        titlesView = new TitlesView(this.mContext);
        addView(titlesView, llParams);

        weekView = new WeekView(this.mContext);
        addView(weekView);

        monthView = new MonthView(this.mContext);
        monthView.setOnPageChangeListener(this);
        monthView.setOnSizeChangedListener(this);
        addView(monthView, llParams);
        titlesView.setListener(new TitlesView.OnTitleEventListener() {
            @Override
            public void onLastMonth() {
                monthView.setLastMonth(null);
            }

            @Override
            public void onNextMonth() {
                monthView.setNextMonth(null);
            }

            @Override
            public void onTitleClick() {
                if (onTitleClickListener != null) {
                    onTitleClickListener.onClick();
                }
            }
        });
    }

    @Override
    public void setOnDateSelected(OnDateSelected onDateSelected) {
        titlesView.setOnDateSelected(onDateSelected, monthView);
        this.onDateSelected = onDateSelected;
    }

    /**
     * @param onDrawDayListener
     */
    public void setOnDrawDayListener(OnDrawDayListener onDrawDayListener) {
        this.onDrawDayListener = onDrawDayListener;
    }

    @Override
    public void setColor(int color) {
        //titleView.setColor(color);
        weekView.setColor(color);
        monthView.setColorMain(color);
    }

    @Override
    public void isLunarDisplay(boolean display) {
        monthView.setLunarShow(display);
    }

    @Override
    public void isSelectToday(boolean display) {
        this.isSelectToday = display;
    }

    @Override
    public void setSelectDate(Date date) {
        this.monthView.setSelectDate(date);
    }

    @Override
    public void setSelectDate(List<Date> listDate) {
        for (Date date : listDate) {
            this.monthView.setSelectDate(date);
        }
    }

    @Override
    public void setOnTitleClick(OnTitleClickListener listener) {
        this.onTitleClickListener = listener;
    }

    @Override
    public void onMonthChange(int month) {
        this.month = month;
        this.titlesView.setMonth(month);
    }

    @Override
    public void onYearChange(int year) {
        this.year = year;
        this.titlesView.setYear(year);
    }

    @Override
    public void onDateChange(int year, int month) {
        this.year = year;
        this.month = month;
        this.titlesView.setDate(year, month);
        Log.d("ABC", year + "-" + month + "-01");
    }

    @Override
    public void onSelectDate(Date date) {
        this.titlesView.setDate(date);
        this.selectDate = date;
        if (this.onDateSelected != null) {
            final List<String> listDate = new ArrayList<>();
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            String strDate = sdf.format(date);
            listDate.add(strDate);
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    onDateSelected.selected(listDate);
                }
            }, 600);
        }
    }

    @Override
    public void onDrawDate(Canvas canvas, float x, float y, Date date) {
        if (this.onDrawDayListener != null) {
            this.onDrawDayListener.onDrawDay(canvas, x, y, date);
        }
    }

    /**
     * @return
     */
    public Date getSelectDate() {
        return this.selectDate;
    }

    /**
     * @param date
     */
    public void initDatepicker(Date date) {
        this.monthView.setCurrentDate(date);
        this.titlesView.setDate(date);
        this.selectDate = date;
    }

    @Override
    public void onSizeChanged(int size) {
        // 目前只设置当前日期选中
        // TODO后续的待办事项
        if (!this.isSelectToday) {
            return;
        }
        long time = System.currentTimeMillis();
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date(time));
        int currentYear = calendar.get(Calendar.YEAR);
        int currentMonth = calendar.get(Calendar.MONTH) + 1;
        if (this.year == currentYear && this.month == currentMonth) {
            this.monthView.setSelectDate(new Date(time));
        }
    }
}
