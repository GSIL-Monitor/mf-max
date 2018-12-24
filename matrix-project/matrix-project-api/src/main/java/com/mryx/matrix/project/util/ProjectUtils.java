package com.mryx.matrix.project.util;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @author pengcheng
 * @description
 * @email pengcheng@missfresh.cn
 * @date 2018-12-11 22:16
 **/
public class ProjectUtils {
    private static final String DEFAULT_FORMAT = "";
    private static SimpleDateFormat simpleDateFormat = new SimpleDateFormat(DEFAULT_FORMAT);

    public static Date string2Date(String source) {
        return string2Date(source, null);
    }

    public static Date string2Date(String source, String format) {
        try {
            if (format == null || format.isEmpty()) {
                return simpleDateFormat.parse(source);
            }
            return new SimpleDateFormat(format).parse(source);
        } catch (Exception e) {
            return null;
        }
    }
}
