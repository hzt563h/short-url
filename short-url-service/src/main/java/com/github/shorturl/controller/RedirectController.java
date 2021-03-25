package com.github.shorturl.controller;

import com.github.shorturl.manager.ShortUrlManager;
import com.github.shorturl.response.ShortUrlVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@RestController
@Slf4j
@RequestMapping("/d")
public class RedirectController {
    @Autowired
    private ShortUrlManager shortUrlManager;

    @GetMapping(value = "/{hash}")
    public String redirect(HttpServletRequest request, HttpServletResponse response, @PathVariable("hash") String hash) throws IOException {
        log.info("====================请求地址:[{}]===============" , request.getRequestURL());
        ShortUrlVO shortUrlVO = shortUrlManager.getRealUrlByHash(hash);
        if(null == shortUrlVO){
            log.error("短链接不存在,hash[{}]", hash);
            return "短链接不存在!";
        }
        response.sendRedirect(shortUrlVO.getUrl());
        return "";
    }
}
