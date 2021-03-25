package com.github.shorturl.mapper;
import java.util.List;

import com.github.shorturl.domain.ShortUrl;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;

public interface ShortUrlMapper extends BaseMapper<ShortUrl> {

    ShortUrl findByHashValue(@Param("hashValue")String hashValue);

    List<String> findAllHashValue();

}