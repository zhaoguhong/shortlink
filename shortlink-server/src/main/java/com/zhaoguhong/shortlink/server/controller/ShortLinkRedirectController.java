package com.zhaoguhong.shortlink.server.controller;

import com.zhaoguhong.shortlink.server.entity.ShortLink;
import com.zhaoguhong.shortlink.server.service.ShortLinkService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

/**
 * 处理短码重定向请求。
 *
 * @author zhaoguhong
 * @date 2026/2/27
 */
@RestController
public class ShortLinkRedirectController {

    private final ShortLinkService shortLinkService;

    public ShortLinkRedirectController(ShortLinkService shortLinkService) {
        this.shortLinkService = shortLinkService;
    }

    @GetMapping("/{code}")
    public ResponseEntity<Void> redirect(@PathVariable("code") String code, HttpServletRequest request) {
        ShortLink link = shortLinkService.getByCode(code);

        String ip = request.getRemoteAddr();
        String ua = request.getHeader("User-Agent");
        String visitorKey = ip + "|" + ua;
        shortLinkService.recordAccess(link.getId(), visitorKey);

        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.LOCATION, link.getOriginalUrl());
        return ResponseEntity.status(HttpStatus.FOUND).headers(headers).build();
    }
}
