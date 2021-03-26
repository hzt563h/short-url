package com.github.shorturl.controller;

import com.github.shorturl.api.CommonResult;
import com.github.shorturl.exception.ApiException;
import com.github.shorturl.manager.ShortUrlManager;
import com.github.shorturl.dto.ShortUrlDto;
import com.github.shorturl.vo.ShortUrlVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/d")
@Slf4j
public class IndexController {

    @Autowired
    private ShortUrlManager shortUrlManager;
    @Value("${domain}")
    private String DOMAIN;

    @PostMapping("/generateShortUrl")
    @ResponseBody
    public CommonResult<String> generateShortUrl(@RequestBody ShortUrlDto request) {
        if(!shortUrlManager.isValidUrl(request.getUrl())){
            log.error("无效的url:[{}]",request.getUrl());
           throw new ApiException("无效的url");
        }
        return CommonResult.success(DOMAIN+shortUrlManager.generateShortUrl(request.getUrl()).getHashValue());
    }

    @GetMapping("/getByHash/{hashValue}")
    @ResponseBody
    public CommonResult<String> getByHash(@PathVariable("hashValue") String hash) {
        log.info("====================请求hash:[{}]===============" , hash);
        ShortUrlVO shortUrlVO = shortUrlManager.getRealUrlByHash(hash);
        if(null == shortUrlVO){
            log.error("短链接不存在,hash[{}]", hash);
            throw new ApiException("短链接不存在");
        }
        return CommonResult.success(shortUrlVO.getUrl());
    }
}
