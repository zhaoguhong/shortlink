package com.zhaoguhong.shortlink.admin.controller;

import com.zhaoguhong.shortlink.admin.entity.ShortLink;
import com.zhaoguhong.shortlink.admin.mapper.ShortLinkMapper;
import com.zhaoguhong.shortlink.common.web.ApiResponse;
import jakarta.validation.Valid;
import org.apache.commons.lang3.ObjectUtils;
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

    private final ShortLinkMapper shortLinkMapper;

    public ShortLinkController(ShortLinkMapper shortLinkMapper) {
        this.shortLinkMapper = shortLinkMapper;
    }

    @PostMapping
    public ApiResponse<ShortLink> create(@Valid @RequestBody ShortLink link) {
        link.setStatus(ObjectUtils.defaultIfNull(link.getStatus(), DEFAULT_LINK_STATUS));
        shortLinkMapper.insert(link);
        return ApiResponse.success(link);
    }

    @PutMapping("/{id}")
    public ApiResponse<ShortLink> update(@PathVariable("id") Long id, @Valid @RequestBody ShortLink link) {
        link.setId(id);
        shortLinkMapper.update(link);
        return ApiResponse.success(link);
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable("id") Long id) {
        shortLinkMapper.delete(id);
        return ApiResponse.success();
    }

    @GetMapping("/{id}")
    public ApiResponse<ShortLink> detail(@PathVariable("id") Long id) {
        ShortLink link = shortLinkMapper.findById(id);
        return ApiResponse.success(link);
    }

    @GetMapping
    public ApiResponse<List<ShortLink>> list() {
        List<ShortLink> list = shortLinkMapper.findAll();
        return ApiResponse.success(list);
    }
}
