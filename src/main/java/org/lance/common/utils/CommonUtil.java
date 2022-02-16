package org.lance.common.utils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import org.apache.commons.lang3.RandomStringUtils;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.regex.Pattern;

public class CommonUtil {

    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyyMMddHHmmssSSS");
    private static final Pattern FILE_PATTERN = Pattern.compile("[\\\\/:*?\"\'\\s+<>|]");

    public static String IDGenerator() {
        String time = DATE_FORMAT.format(new Date());
        String randomStr = RandomStringUtils.randomNumeric(6);
        return time + randomStr;
    }

    public static String clearInvalidChars(String content) {
        content = FILE_PATTERN.matcher(content).replaceAll("");
        while (content.startsWith("-") || content.startsWith("#")) { // linux invalid char
            content = content.substring(1);
        }
        return content;
    }

    public static <T> T parseJSONToEntity(String jsonStr, TypeReference<T> typeReference) {
        return JSON.parseObject(jsonStr, typeReference);
    }
}
