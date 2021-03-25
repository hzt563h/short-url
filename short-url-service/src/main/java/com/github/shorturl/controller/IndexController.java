package com.github.shorturl.controller;

import com.github.shorturl.manager.ShortUrlManager;
import com.github.shorturl.request.GenerateShortUrlRequest;
import com.github.shorturl.response.Response;
import com.github.shorturl.response.ShortUrlVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@Controller
@RequestMapping("/d")
@Slf4j
public class IndexController {

    @Autowired
    private ShortUrlManager shortUrlManager;

    @PostMapping("/generateShortUrl")
    @ResponseBody
    public Response<String> generateShortUrl(@RequestBody @Valid GenerateShortUrlRequest request) {
        if(!shortUrlManager.isValidUrl(request.getUrl())){
            log.error("无效的url:[{}]",request.getUrl());
           return Response.failed("-1", "无效的url");
        }
        return shortUrlManager.generateShortUrl(request.getUrl());
    }

    @GetMapping("/getByHash/{hashValue}")
    @ResponseBody
    public Response<String> getByHash(@PathVariable("hashValue") String hash) {
        log.info("====================请求hash:[{}]===============" , hash);
        ShortUrlVO shortUrlVO = shortUrlManager.getRealUrlByHash(hash);
        if(null == shortUrlVO){
            log.error("短链接不存在,hash[{}]", hash);
            return Response.failed("-1", "短链接不存在");
        }
        return Response.success(shortUrlVO.getUrl());
    }
}
