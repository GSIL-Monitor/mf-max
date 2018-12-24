package com.mryx.matrix.publish.core.utils;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static jdk.nashorn.internal.objects.NativeMath.log;

/**
 * @author dinglu
 * @date 2018/9/17
 */
public class FileUtil {

    private static final Logger logger = LoggerFactory.getLogger(FileUtil.class);

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
            StringBuilder result = new StringBuilder();
            File file = new File(uri);
            long length = file.length();
            if (lastTimeFileSize == 0L || lastTimeFileSize == null) {
                fileAndRealTimeSizeMap.put(uri, 1L);
                lastTimeFileSize = 1L;
            }
            if (length <= 0L) {
                return "";
            }

            if (length < lastTimeFileSize) {
                return "";
            } else {
                long readStart = System.currentTimeMillis();
                RandomAccessFile accessFile = new RandomAccessFile(file, "rw");
                accessFile.seek(lastTimeFileSize - 1);
                String tmp;
                while ((tmp = accessFile.readLine()) != null) {
                    if (tmp.contains("Downloaded") || tmp.contains("Progress") || tmp.contains("emitted") || tmp.contains("Asset")) {
                        continue;
                    }
                    result.append(new String(tmp.getBytes("ISO-8859-1"), "utf-8")).append("\n");
                }
                fileAndRealTimeSizeMap.put(uri, length);
                accessFile.close();
                long readFinish = System.currentTimeMillis();

                logger.debug("read file {} cost {} ms, read file size {} bytes.", file.getAbsolutePath(), (readFinish - readStart), (length - lastTimeFileSize));
            }
            return result.toString();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return "读取日志异常";
        } catch (IOException e) {
            e.printStackTrace();
            return "读取日志异常";
        }
    }
}