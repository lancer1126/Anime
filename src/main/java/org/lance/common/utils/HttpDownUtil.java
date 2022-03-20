package org.lance.common.utils;

import org.lance.common.constrants.Global;
import org.lance.core.downloader.IHttpDownloader;
import org.lance.core.downloader.M4SHttpDownloader;

import java.io.File;
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

    public static Map<String, String> addRangeForHeader(Map<String, String> headers, long startOffset, long endOffset) {
        // deep copy
        HashMap<String, String> newHeaders = new HashMap<>(headers);
        if (endOffset == -1 || endOffset == 0) {
            newHeaders.put("Range", String.format("bytes=%d-", startOffset));
        } else {
            newHeaders.put("Range", String.format("bytes=%d-%d", startOffset, endOffset));
        }
        return newHeaders;
    }

    public static String getTaskFilePathWithMp4Suffix(IHttpDownloader downloader) {
        return downloader.getTaskInfo().getFilePath() + File.separator
                + downloader.getTaskInfo().getName() + Global.DEFAULT_VIDEO_SUFFIX;
    }

    public static String getTaskFilePathWithoutSuffix(M4SHttpDownloader downloader) {
        return downloader.getTaskInfo().getFilePath() + File.separator
                + downloader.getTaskInfo().getName();
    }
}
