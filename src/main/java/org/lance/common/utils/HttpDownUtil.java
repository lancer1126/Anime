package org.lance.common.utils;

import java.util.HashMap;
import java.util.Map;

public class HttpDownUtil {

    private static final long ONE_KB = 1024;

    private static final long ONE_MB = 1024 * ONE_KB;

    public static Map<String, String> addRangeForHeader(Map<String, String> headers, long startOffset) {
        // deep copy
        HashMap<String, String> newHeaders = new HashMap<>(headers);
        newHeaders.put("Range", String.format("bytes=%d-", startOffset));
        return newHeaders;
    }
}
