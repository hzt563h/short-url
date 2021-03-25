package com.github.shorturl.service.impl;

import com.github.shorturl.domain.ShortUrl;
import com.github.shorturl.mapper.ShortUrlMapper;
import com.github.shorturl.service.RedisService;
import com.github.shorturl.service.ShortUrlService;
import com.github.shorturl.utils.MathUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.MurmurHash3;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
public class ShortUrlServiceImpl implements ShortUrlService {

    @Autowired
    private ShortUrlMapper shortUrlMapper;

    @Autowired
    private RedisService redisService;

    @Override
    public String generateShortUrl(String url) {
        if (StringUtils.isBlank(url)) {
            throw new RuntimeException("请输入正确url");
        }
        long hash = MurmurHash3.hash32x86(url.getBytes());
        return MathUtils._10_to_62(hash);
    }

    @Override
    public int saveShortUrl(ShortUrl shortUrl) {
        return shortUrlMapper.insert(shortUrl);
    }

    @Override
    public ShortUrl findByHashValueFromDB(String hashValue) {
        return shortUrlMapper.findByHashValue(hashValue);
    }

    @Override
    @Cacheable(cacheNames = "shortUrl", key= "#hashValue")
    public ShortUrl findByHashValueFromLocalCache(String hashValue) {
        ShortUrl shortUrl = (ShortUrl)redisService.get(hashValue);
        if (shortUrl !=null){
            return shortUrl;
        }
        return shortUrlMapper.findByHashValue(hashValue);
    }

    @Override
    public List<String> findAllHashValue() {
        return shortUrlMapper.findAllHashValue();
    }
}

