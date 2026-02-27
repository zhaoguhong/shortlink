package com.zhaoguhong.shortlink.common.util;

import java.net.URI;
import java.net.URISyntaxException;

/**
 * URL 校验工具类，仅做 URL 合法性判断。
 *
 * @author zhaoguhong
 * @date 2026/2/28
 */
public final class UrlValidationUtils {

    private UrlValidationUtils() {
    }

    public static boolean isValidHttpOrHttpsUrl(String url) {
        if (url == null || url.trim().isEmpty()) {
            return false;
        }
        try {
            URI uri = new URI(url);
            String scheme = uri.getScheme();
            return "http".equalsIgnoreCase(scheme) || "https".equalsIgnoreCase(scheme);
        } catch (URISyntaxException e) {
            return false;
        }
    }
}
