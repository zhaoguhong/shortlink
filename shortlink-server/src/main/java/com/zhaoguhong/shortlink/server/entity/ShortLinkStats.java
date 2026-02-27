package com.zhaoguhong.shortlink.server.entity;

import java.time.LocalDateTime;

/**
 * 短链访问统计实体。
 *
 * @author zhaoguhong
 * @date 2026/2/27
 */
public class ShortLinkStats {

    private Long id;
    private Long linkId;
    private Long totalPv;
    private Long totalUv;
    private LocalDateTime recentAccessTime;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getLinkId() {
        return linkId;
    }

    public void setLinkId(Long linkId) {
        this.linkId = linkId;
    }

    public Long getTotalPv() {
        return totalPv;
    }

    public void setTotalPv(Long totalPv) {
        this.totalPv = totalPv;
    }

    public Long getTotalUv() {
        return totalUv;
    }

    public void setTotalUv(Long totalUv) {
        this.totalUv = totalUv;
    }

    public LocalDateTime getRecentAccessTime() {
        return recentAccessTime;
    }

    public void setRecentAccessTime(LocalDateTime recentAccessTime) {
        this.recentAccessTime = recentAccessTime;
    }

    public LocalDateTime getCreateTime() {
        return createTime;
    }

    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }

    public LocalDateTime getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(LocalDateTime updateTime) {
        this.updateTime = updateTime;
    }
}
