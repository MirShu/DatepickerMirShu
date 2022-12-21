package com.humannote.me.views;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.humannote.me.bizs.LunarCalendar;
import com.humannote.me.datepicker.R;
import com.humannote.me.entities.Language;
import com.humannote.me.interfaces.OnDateSelected;

import java.text.DecimalFormat;
import java.text.MessageFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * Created by JOHN on 2016/1/3.
 */
public class TitlesView extends LinearLayout {
    private Context mContext;
    private TextView tv_solar_calendar;
    private TextView tv_lunar_calendar;
    private String[] monthTitles;
    private OnDateSelected mOnDateSelected;
    private MonthView monthView;
    private int month;
    private int year;
    private int currentYear;
    private int currentMonth;
    private int currentDay;
    private DecimalFormat df = new DecimalFormat("00");
    private OnTitleEventListener listener;
    private ImageView iv_last;
    private ImageView iv_next;
    private LinearLayout ll_title;

    public TitlesView(Context context) {
        this(context, null);
    }

    public TitlesView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public TitlesView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.mContext = context;
        this.initView();
    }

    /**
     *
     */
    private void initView() {
        setColor(0xFFE95344);
        setOrientation(VERTICAL);
        LayoutParams llParams = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT, 1);
        LayoutInflater layoutInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View layout = layoutInflater.inflate(R.layout.view_title, null);
        addView(layout, llParams);
        monthTitles = Language.getLanguage(this.mContext).monthTitles();
        Calendar calendar = Calendar.getInstance();
        currentYear = calendar.get(Calendar.YEAR);
        currentMonth = calendar.get(Calendar.MONTH) + 1;
        currentDay = calendar.get(Calendar.DAY_OF_MONTH);
        this.tv_solar_calendar = (TextView) layout.findViewById(R.id.tv_solar_calendar);
        this.tv_lunar_calendar = (TextView) layout.findViewById(R.id.tv_lunar_calendar);
        this.iv_last = (ImageView) layout.findViewById(R.id.iv_last);
        this.iv_next = (ImageView) layout.findViewById(R.id.iv_next);
        this.ll_title = (LinearLayout) layout.findViewById(R.id.ll_title);
        this.bindListener();
    }

    /**
     *
     */
    private void bindListener() {
        this.iv_last.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (listener != null) {
                    listener.onLastMonth();
                }
            }
        });
        this.iv_next.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (listener != null) {
                    listener.onNextMonth();
                }
            }
        });
        this.ll_title.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (listener != null) {
                    listener.onTitleClick();
                }
            }
        });
    }

    public void setColor(int color) {
        //setBackgroundColor(color);
        setBackgroundColor(0xffffffff);
    }

    public void setOnDateSelected(OnDateSelected onDateSelected, MonthView monthView) {
        mOnDateSelected = onDateSelected;
        this.monthView = monthView;
    }

    /**
     * @param month
     */
    public void setMonth(int month) {
        this.month = month;
    }

    /**
     * @param year
     */
    public void setYear(int year) {
        this.year = year;
    }

    /**
     * @param year
     * @param month
     */
    public void setDate(int year, int month) {
        this.year = year;
        this.month = month;
        this.showDate();
    }

    /**
     * @param date
     */
    public void setDate(Date date) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd ");
        String strDate = sdf.format(date);
        this.tv_solar_calendar.setText(strDate);
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        LunarCalendar lunarCalendar = new LunarCalendar(calendar);
        String lunar = lunarCalendar.toString();
        this.tv_lunar_calendar.setText(lunar);
    }

    /**
     *
     */
    private void showDate() {
        try {
            int day = 1;
            if (this.year == this.currentYear && this.month == this.currentMonth) {
                day = this.currentDay;
            }
            String strDate = MessageFormat.format("{0}-{1}-{2}", String.valueOf(year), month, day);
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            java.util.Date date = sdf.parse(strDate);
            this.setDate(date);
        } catch (ParseException e) {
            System.out.println(e.getMessage());
        }
    }

    /**
     * @param listener
     */
    public void setListener(OnTitleEventListener listener) {
        this.listener = listener;
    }

    public interface OnTitleEventListener {

        /**
         * 上一月
         */
        void onLastMonth();

        /**
         * 下一月
         */
        void onNextMonth();

        /**
         * 点击标题
         */
        void onTitleClick();
    }
}
