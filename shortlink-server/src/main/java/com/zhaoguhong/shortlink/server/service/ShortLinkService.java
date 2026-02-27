package com.zhaoguhong.shortlink.server.service;

import com.zhaoguhong.shortlink.server.entity.ShortLink;

/**
 * 短链核心服务接口。
 *
 * @author zhaoguhong
 * @date 2026/2/27
 */
public interface ShortLinkService {

    /**
     * 根据短码查询短链信息。
     *
     * @param code 短码
     * @return 短链实体
     */
    ShortLink getByCode(String code);

    /**
     * 记录访问行为并更新统计信息。
     *
     * @param linkId 短链ID
     * @param visitorKey 访客标识
     */
    void recordAccess(Long linkId, String visitorKey);
}
