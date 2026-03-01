package com.zhaoguhong.shortlink.server.util;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.util.StringUtils;

/**
 * 客户端IP解析工具。
 *
 * @author zhaoguhong
 * @date 2026/3/1
 */
public final class ClientIpUtils {

    private ClientIpUtils() {
    }

    public static String resolveClientIp(HttpServletRequest request) {
        String ip = firstValidIp(request.getHeader("X-Forwarded-For"));
        if (StringUtils.hasText(ip)) {
            return ip;
        }
        ip = firstValidIp(request.getHeader("X-Real-IP"));
        if (StringUtils.hasText(ip)) {
            return ip;
        }
        return request.getRemoteAddr();
    }

    private static String firstValidIp(String headerValue) {
        if (!StringUtils.hasText(headerValue)) {
            return null;
        }
        String[] candidates = headerValue.split(",");
        for (String candidate : candidates) {
            String ip = candidate.trim();
            if (StringUtils.hasText(ip) && !"unknown".equalsIgnoreCase(ip)) {
                return ip;
            }
        }
        return null;
    }
}
