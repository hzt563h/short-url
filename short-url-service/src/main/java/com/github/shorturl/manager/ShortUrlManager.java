package com.github.shorturl.manager;

import com.github.shorturl.domain.ShortUrl;
import com.github.shorturl.exception.ApiException;
import com.github.shorturl.filter.BloomFilterHelper;
import com.github.shorturl.filter.RedisBloomFilter;
import com.github.shorturl.vo.ShortUrlVO;
import com.github.shorturl.service.RedisService;
import com.github.shorturl.service.ShortUrlService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@Slf4j
@RefreshScope//自动刷新
public class ShortUrlManager {
    @Autowired
    private ShortUrlService shortUrlService;

    @Autowired
    private RedisBloomFilter redisBloomFilter;

    @Autowired
    private RedisService redisService;

    @Autowired
    private BloomFilterHelper bloomFilterHelper;

    private static final String URL_DUPLICATED = "[DUPLICATED]";

    @Value("${url.protocol:https}")
    private String URL_PREFIX;


    @Transactional(rollbackFor = Exception.class)
    public ShortUrlVO generateShortUrl(String url) {
        //判断 url 是否是Http https 开头
        if(StringUtils.isBlank(url)){
            throw new ApiException("参数错误");
        }
        url = StringUtils.trim(url).toLowerCase();
        if(!isStartWithHttpOrHttps(url)){
            url = appendHttp2Head(url,URL_PREFIX);
        }

        //这里多台机器可能出现并发问题，查询->插入，可能会出现问题，但是有数据库唯一索引保护
        String hash = shortUrlService.generateShortUrl(url);
        //计算多差次拼接才能生成不重复的 hash value
        int count = 0;


        while(true){
            if(count > 5){
                throw new ApiException("重试拼接url 超过限制次数");
            }
            //从 BloomFilter 查看是否存在
            boolean mightContain =  redisBloomFilter.includeByBloomFilter(bloomFilterHelper, "bloom", hash);
            if(!mightContain){
                redisBloomFilter.addByBloomFilter(bloomFilterHelper,"bloom",hash);
                log.info("============生成短链接，判断短链接不存在,可以生成对应关系!===============");
                break;
            }

            //hash 相同且长链接相同
            ShortUrl dbShortUrl = shortUrlService.findByHashValueFromLocalCache(hash);
            if(dbShortUrl.getUrl().equals(url)){
                log.info("============短链接已存在!===============");
                return new ShortUrlVO(dbShortUrl.getHashValue(), dbShortUrl.getUrl());
            }else{
                log.warn("=======hashValue:[{}],DBUrl:[{}],currentUrl:[{}]",
                        hash,dbShortUrl.getUrl(),url);
                url = url + URL_DUPLICATED;
                hash = shortUrlService.generateShortUrl(url);
                log.warn("=======重新拼接hash:[{}],currentUrl:[{}]", hash, url);
            }
            count++;
            log.info("===========================url重复拼接字符串，次数:[{}]",count);
        }
        ShortUrl saveBean = new ShortUrl();
        try {
            //入库
            saveBean.setHashValue(hash);
            saveBean.setUrl(url);
            shortUrlService.saveShortUrl(saveBean);
            //redis数据缓存12小时
            redisService.set(hash,saveBean,60*60*12);
        } catch (Exception e) {
            log.error("重复插入问题e:",e);
            throw new ApiException("重复插入");
        }
        return new ShortUrlVO(saveBean.getHashValue(), saveBean.getUrl());
    }


    public ShortUrlVO getRealUrlByHash(String hash) {
        //get Url by hash
        ShortUrl shortUrl = shortUrlService.findByHashValueFromLocalCache(hash);
        if(null == shortUrl){
            return null;
        }
        String realUrl = shortUrl.getUrl().replace(URL_DUPLICATED,"");
        return new ShortUrlVO(shortUrl.getHashValue(), realUrl);
    }

    public static boolean isStartWithHttpOrHttps(String url) {
       String regex = "^((https|http)?://)";
        Pattern p = Pattern.compile(regex);
        Matcher matcher = p.matcher(url);
        return matcher.find();
    }

    /**
     * url 开头拼接 http
     * @param url
     * @return
     */
    private String appendHttp2Head(String url, String prefix) {
        StringBuilder stringBuilder = new StringBuilder(prefix).append("://");
        stringBuilder.append(url);
        return stringBuilder.toString();
    }


    /**
     * 是否是有效的 url
     * @param urls
     * @return
     */
    public boolean isValidUrl(String urls) {
        //设置正则表达式
        String regex = "^(https?|ftp|file)://[-a-zA-Z0-9+&@#/%?=~_|!:,.;]*[-a-zA-Z0-9+&@#/%=~_|]";
        //对比
        Pattern pat = Pattern.compile(regex.trim());
        Matcher mat = pat.matcher(urls.trim());
        //判断是否匹配
        return mat.matches();
    }
}
