package com.zhaoguhong.shortlink.server.mapper;

import com.zhaoguhong.shortlink.server.entity.ShortLink;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 服务端短链查询数据访问接口。
 *
 * @author zhaoguhong
 * @date 2026/2/27
 */
@Mapper
public interface ShortLinkMapper {

    /**
     * 按短码查询短链记录。
     *
     * @param code 短码
     * @return 短链实体
     */
    @Select("select id, code, original_url as originalUrl, status, expire_time as expireTime, create_time as createTime, update_time as updateTime " +
            "from short_link where code = #{code} limit 1")
    ShortLink findByCode(@Param("code") String code);
}
