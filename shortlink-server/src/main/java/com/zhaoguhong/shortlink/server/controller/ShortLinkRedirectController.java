package com.zhaoguhong.shortlink.server.controller;

import com.zhaoguhong.shortlink.common.exception.BizException;
import com.zhaoguhong.shortlink.server.entity.ShortLink;
import com.zhaoguhong.shortlink.server.service.ShortLinkService;
import jakarta.servlet.http.HttpServletRequest;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.net.URISyntaxException;

/**
 * 处理短码重定向请求。
 *
 * @author zhaoguhong
 * @date 2026/2/27
 */
@RestController
public class ShortLinkRedirectController {

    private static final int ERROR_CODE_INTERNAL = 500;
    private static final Logger log = LoggerFactory.getLogger(ShortLinkRedirectController.class);

    private final ShortLinkService shortLinkService;

    public ShortLinkRedirectController(ShortLinkService shortLinkService) {
        this.shortLinkService = shortLinkService;
    }

    @GetMapping("/{code}")
    public ResponseEntity<Void> redirect(@PathVariable("code") String code, HttpServletRequest request) {
        ShortLink link = shortLinkService.getByCode(code);
        validateRedirectUrl(link.getOriginalUrl());

        String ip = request.getRemoteAddr();
        String ua = request.getHeader("User-Agent");
        String visitorKey = ip + "|" + ua;
        try {
            shortLinkService.recordAccess(link.getId(), visitorKey);
        } catch (Exception e) {
            log.warn("record access failed, code={}", code, e);
        }

        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.LOCATION, link.getOriginalUrl());
        return ResponseEntity.status(HttpStatus.FOUND).headers(headers).build();
    }

    private void validateRedirectUrl(String url) {
        if (StringUtils.isBlank(url)) {
            throw new BizException(ERROR_CODE_INTERNAL, "短链目标地址为空");
        }
        try {
            URI uri = new URI(url);
            String scheme = uri.getScheme();
            if (!"http".equalsIgnoreCase(scheme) && !"https".equalsIgnoreCase(scheme)) {
                throw new BizException(ERROR_CODE_INTERNAL, "短链目标地址协议非法");
            }
        } catch (URISyntaxException e) {
            throw new BizException(ERROR_CODE_INTERNAL, "短链目标地址格式非法", e);
        }
    }
}
