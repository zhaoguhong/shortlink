package com.zhaoguhong.shortlink.admin.mapper;

import com.zhaoguhong.shortlink.common.entity.ShortLink;
import org.apache.ibatis.annotations.*;

import java.util.List;

/**
 * 管理端短链数据访问接口。
 *
 * @author zhaoguhong
 * @date 2026/2/27
 */
@Mapper
public interface ShortLinkMapper {

    /**
     * 新增短链记录。
     *
     * @param link 短链实体
     * @return 影响行数
     */
    @Insert("insert into short_link(code, original_url, status, expire_time, create_time, update_time) " +
            "values(#{code}, #{originalUrl}, #{status}, #{expireTime}, now(), now())")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(ShortLink link);

    /**
     * 按主键更新短链记录。
     *
     * @param link 短链实体
     * @return 影响行数
     */
    @Update("update short_link set original_url=#{originalUrl}, status=#{status}, expire_time=#{expireTime}, update_time=now() where id=#{id}")
    int update(ShortLink link);

    /**
     * 按主键删除短链记录。
     *
     * @param id 短链ID
     * @return 影响行数
     */
    @Delete("delete from short_link where id=#{id}")
    int delete(@Param("id") Long id);

    /**
     * 按主键查询短链详情。
     *
     * @param id 短链ID
     * @return 短链实体
     */
    @Select("select id, code, original_url as originalUrl, status, expire_time as expireTime, create_time as createTime, update_time as updateTime " +
            "from short_link where id = #{id}")
    ShortLink findById(@Param("id") Long id);

    /**
     * 查询全部短链记录。
     *
     * @return 短链列表
     */
    @Select("select id, code, original_url as originalUrl, status, expire_time as expireTime, create_time as createTime, update_time as updateTime from short_link")
    List<ShortLink> findAll();
}
