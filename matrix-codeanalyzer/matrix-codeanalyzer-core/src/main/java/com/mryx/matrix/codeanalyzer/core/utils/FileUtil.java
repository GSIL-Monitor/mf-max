package com.mryx.matrix.codeanalyzer.core.utils;

import org.springframework.util.StringUtils;

import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * @author dinglu
 * @date 2018/9/17
 */
public class FileUtil {
    static Map<String, Long> fileAndRealTimeSizeMap = new HashMap<>();


    public static void uploadFile(byte[] file, String filePath, String fileName) throws Exception {
        File targetFile = new File(filePath);
        if (!targetFile.exists()) {
            targetFile.mkdirs();
        }
        FileOutputStream out = new FileOutputStream(filePath + fileName);
        out.write(file);
        out.flush();
        out.close();
    }

    public static boolean deleteFile(String fileName) {
        File file = new File(fileName);
        // 如果文件路径所对应的文件存在，并且是一个文件，则直接删除
        if (file.exists() && file.isFile()) {
            if (file.delete()) {
                return true;
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    public static String renameToUUID(String fileName) {
        return UUID.randomUUID() + "." + fileName.substring(fileName.lastIndexOf(".") + 1);
    }

    public static String initLog(String key) {
        fileAndRealTimeSizeMap.put(key, 0L);
        return realTimeReadFile(key);
    }

    public static String realTimeReadFile(String uri) {
        try {
            if (StringUtils.isEmpty(uri)) {
                throw new IllegalStateException("File Read error");
            }
            Long lastTimeFileSize = fileAndRealTimeSizeMap.get(uri);
            String result = "";
            File file = new File(uri);
            long length = file.length();
            if (lastTimeFileSize == 0L || lastTimeFileSize == null) {
                fileAndRealTimeSizeMap.put(uri, 1L);
                lastTimeFileSize = 1L;
            }

            if (length < lastTimeFileSize) {
                throw new IllegalStateException("File Read error");
            } else {
                RandomAccessFile accessFile = new RandomAccessFile(file, "rw");
                accessFile.seek(lastTimeFileSize - 1);
                String tmp;
                while ((tmp = accessFile.readLine()) != null) {
                    result += new String(tmp.getBytes("ISO-8859-1"), "utf-8") + "\n";
                }
                fileAndRealTimeSizeMap.put(uri, length);
                accessFile.close();
            }
            return result;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return "读取日志异常";
        } catch (IOException e) {
            e.printStackTrace();
            return "读取日志异常";
        }
    }
}