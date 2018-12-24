package com.mryx.matrix.common.util;

import com.mryx.matrix.common.enums.DateFormatType;

import java.text.DateFormat;
import java.text.SimpleDateFormat;

/**
 * Date 操作类
 *
 * @author supeng
 * @date 2018/09/28
 */
public class DateFormatUtil {

    private static ThreadLocal<DateFormat> threadLocal = new ThreadLocal<DateFormat>();

    /**
     * 自定义format
     * @param format
     * @return
     */
    public static DateFormat getFormatter(String format) {
        DateFormat dateFormat = threadLocal.get();
        if(dateFormat==null){
            dateFormat = new SimpleDateFormat(format);
            threadLocal.set(dateFormat);
        }
        return dateFormat;
    }

    /**
     *
     * @param dateFormatType
     * @return
     */
    public static DateFormat getFormatter(DateFormatType dateFormatType) {
        DateFormat dateFormat = threadLocal.get();
        if(dateFormat==null){
            dateFormat = new SimpleDateFormat(dateFormatType.getFormat());
            threadLocal.set(dateFormat);
        }
        return dateFormat;
    }

}
