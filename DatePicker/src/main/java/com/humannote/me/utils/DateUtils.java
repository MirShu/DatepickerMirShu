package com.humannote.me.utils;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by shiyuankao on 2016/11/28.
 */

public class DateUtils {
    public static SimpleDateFormat format_YYYYMMMDD = new SimpleDateFormat("yyyy-MM-dd");

    //把字符串转为日期
    public static Date stringToDate(String strDate) {
        try {
            return format_YYYYMMMDD.parse(strDate);
        } catch (Exception ex) {
            return new Date();
        }
    }
}
