package com.humannote.me.views;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Color;
import android.os.Build;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.humannote.me.entities.Language;

/**
 * Created by JOHN on 2016/1/3.
 */
public class WeekView extends LinearLayout {
    private Context mContext;
    private String[] weekTitles;

    public WeekView(Context context) {
        this(context, null);
    }

    public WeekView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public WeekView(Context context, AttributeSet attrs, int defStyleAttr) {
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
        weekTitles = Language.getLanguage(mContext).weekTitles();
        LinearLayout layout = new LinearLayout(mContext);
        layout.setOrientation(HORIZONTAL);
        LayoutParams llParams = new LayoutParams(0, LayoutParams.WRAP_CONTENT, 1);
        llParams.setMargins(0, 30, 0, 30);
        View topLine = new View(mContext);
        LayoutParams lineParams = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 1);
        topLine.setBackgroundColor(0xffdddddd);
        addView(topLine, lineParams);
        for (int i = 0; i < weekTitles.length; i++) {
            TextView tv = new TextView(mContext);
            tv.setText(weekTitles[i]);
            tv.setGravity(Gravity.CENTER);
            tv.setTextColor(0xffc3c3c3);
            layout.addView(tv, llParams);
        }
        addView(layout);
        View bottomLine = new View(mContext);
        bottomLine.setBackgroundColor(0xffdddddd);
        addView(bottomLine, lineParams);
    }

    public void setColor(int color) {
        //setBackgroundColor(color);
        setBackgroundColor(0xffffffff);
    }
}
