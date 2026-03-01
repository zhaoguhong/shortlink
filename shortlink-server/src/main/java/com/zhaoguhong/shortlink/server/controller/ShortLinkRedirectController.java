package com.zhaoguhong.shortlink.server.controller;

import com.zhaoguhong.shortlink.common.entity.ShortLink;
import com.zhaoguhong.shortlink.server.service.ShortLinkService;
import com.zhaoguhong.shortlink.server.util.ClientIpUtils;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

/**
 * 处理短码重定向请求。
 *
 * @author zhaoguhong
 * @date 2026/2/27
 */
@RestController
public class ShortLinkRedirectController {

    private static final Logger log = LoggerFactory.getLogger(ShortLinkRedirectController.class);
    private static final String VISITOR_COOKIE_NAME = "visitor_id";

    private final ShortLinkService shortLinkService;

    public ShortLinkRedirectController(ShortLinkService shortLinkService) {
        this.shortLinkService = shortLinkService;
    }

    /**
     * 根据短码查找原始链接，记录访问统计并返回302重定向响应。
     *
     * @param code 短码
     * @param visitorId 访客Cookie标识（不存在时为null）
     * @param request HTTP请求
     * @return 重定向响应
     */
    @GetMapping("/{code}")
    public ResponseEntity<Void> redirect(@PathVariable("code") String code,
                                         @CookieValue(value = VISITOR_COOKIE_NAME, required = false) String visitorId,
                                         HttpServletRequest request) {
        ShortLink link = shortLinkService.getByCode(code);
        if (link == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        HttpHeaders headers = new HttpHeaders();
        String visitorKey;
        if (StringUtils.hasText(visitorId)) {
            // 优先使用cookie标识，提升同一访客跨网络场景下的稳定性。
            visitorKey = "vid:" + visitorId;
        } else {
            // cookie不存在时回退为IP+UA，并补发visitor_id用于后续请求识别。
            String ip = ClientIpUtils.resolveClientIp(request);
            String ua = request.getHeader("User-Agent");
            visitorKey = "ipua:" + ip + "|" + (ua == null ? "" : ua);

            ResponseCookie cookie = ResponseCookie.from(VISITOR_COOKIE_NAME, UUID.randomUUID().toString())
                    .path("/")
                    .httpOnly(true)
                    .secure(request.isSecure())
                    .sameSite("Lax")
                    .maxAge(60L * 60 * 24 * 365)
                    .build();
            headers.add(HttpHeaders.SET_COOKIE, cookie.toString());
        }
        try {
            shortLinkService.recordAccess(link.getId(), visitorKey);
        } catch (Exception e) {
            log.warn("record access failed, code={}", code, e);
        }

        headers.add(HttpHeaders.LOCATION, link.getOriginalUrl());
        return ResponseEntity.status(HttpStatus.FOUND).headers(headers).build();
    }

}
