package com.humannote.me.interfaces;

import android.graphics.Canvas;

import java.util.Date;

/**
 * Created by shiyuankao on 2016/11/28.
 */

public interface OnDrawDayListener {
    void onDrawDay(Canvas canvas, float x, float y, Date date);
}
