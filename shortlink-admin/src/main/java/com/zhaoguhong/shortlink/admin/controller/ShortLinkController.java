package com.zhaoguhong.shortlink.admin.controller;

import com.zhaoguhong.shortlink.common.entity.ShortLink;
import com.zhaoguhong.shortlink.admin.generator.ShortCodeGeneratorRouter;
import com.zhaoguhong.shortlink.admin.mapper.ShortLinkMapper;
import com.zhaoguhong.shortlink.common.exception.BizException;
import com.zhaoguhong.shortlink.common.util.UrlValidationUtils;
import com.zhaoguhong.shortlink.common.web.ApiResponse;
import jakarta.validation.Valid;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 管理端短链增删改查接口。
 *
 * @author zhaoguhong
 * @date 2026/2/27
 */
@RestController
@RequestMapping("/api/links")
public class ShortLinkController {

    private static final int DEFAULT_LINK_STATUS = 1;
    private static final int ERROR_CODE_BAD_REQUEST = 400;
    private static final int ERROR_CODE_NOT_FOUND = 404;
    private static final int ERROR_CODE_INTERNAL = 500;
    private static final int CODE_GENERATE_MAX_RETRY = 8;

    private final ShortLinkMapper shortLinkMapper;
    private final ShortCodeGeneratorRouter shortCodeGeneratorRouter;

    public ShortLinkController(ShortLinkMapper shortLinkMapper, ShortCodeGeneratorRouter shortCodeGeneratorRouter) {
        this.shortLinkMapper = shortLinkMapper;
        this.shortCodeGeneratorRouter = shortCodeGeneratorRouter;
    }

    @PostMapping
    public ApiResponse<ShortLink> create(@Valid @RequestBody ShortLink link) {
        validateUrl(link.getOriginalUrl());
        link.setStatus(ObjectUtils.defaultIfNull(link.getStatus(), DEFAULT_LINK_STATUS));
        createWithGeneratedCode(link);
        return ApiResponse.success(link);
    }

    @PostMapping("/{id}/update")
    public ApiResponse<ShortLink> update(@PathVariable("id") Long id, @Valid @RequestBody ShortLink link) {
        validateUrl(link.getOriginalUrl());
        link.setId(id);
        int affectedRows = shortLinkMapper.update(link);
        if (affectedRows == 0) {
            throw new BizException(ERROR_CODE_NOT_FOUND, "短链不存在");
        }
        return ApiResponse.success(link);
    }

    @PostMapping("/{id}/delete")
    public ApiResponse<Void> delete(@PathVariable("id") Long id) {
        int affectedRows = shortLinkMapper.delete(id);
        if (affectedRows == 0) {
            throw new BizException(ERROR_CODE_NOT_FOUND, "短链不存在");
        }
        return ApiResponse.success();
    }

    @GetMapping("/{id}")
    public ApiResponse<ShortLink> detail(@PathVariable("id") Long id) {
        ShortLink link = shortLinkMapper.findById(id);
        if (link == null) {
            throw new BizException(ERROR_CODE_NOT_FOUND, "短链不存在");
        }
        return ApiResponse.success(link);
    }

    @GetMapping
    public ApiResponse<List<ShortLink>> list() {
        List<ShortLink> list = shortLinkMapper.findAll();
        return ApiResponse.success(list);
    }

    private void validateUrl(String url) {
        if (url == null || url.trim().isEmpty()) {
            throw new BizException(ERROR_CODE_BAD_REQUEST, "原始链接不能为空");
        }
        if (!UrlValidationUtils.isValidHttpOrHttpsUrl(url)) {
            throw new BizException(ERROR_CODE_BAD_REQUEST, "原始链接格式非法，仅支持 http/https");
        }
    }

    private void createWithGeneratedCode(ShortLink link) {
        for (int i = 0; i < CODE_GENERATE_MAX_RETRY; i++) {
            link.setCode(shortCodeGeneratorRouter.generate(link.getOriginalUrl(), i));
            try {
                shortLinkMapper.insert(link);
                return;
            } catch (DuplicateKeyException ignored) {
                // 短码冲突时重试，避免唯一索引冲突导致创建失败。
            }
        }
        throw new BizException(ERROR_CODE_INTERNAL, "短链码生成失败，请稍后重试");
    }
}
