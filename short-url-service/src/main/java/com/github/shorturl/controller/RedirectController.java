package com.github.shorturl.controller;

import com.github.shorturl.manager.ShortUrlManager;
import com.github.shorturl.vo.ShortUrlVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;


import java.net.URI;

@RestController
@Slf4j
@RequestMapping("/d")
public class RedirectController {
    @Autowired
    private ShortUrlManager shortUrlManager;

    @GetMapping(value = "/{hash}")
    @ResponseStatus(HttpStatus.FOUND)
    public Mono<Void> redirect(ServerHttpResponse response, @PathVariable("hash") String hash) throws Exception {
        //log.info("====================请求地址:[{}]===============" , response.get());

        ShortUrlVO shortUrlVO = shortUrlManager.getRealUrlByHash(hash);
        if(null == shortUrlVO){
            log.error("短链接不存在,hash[{}]", hash);
            throw new Exception("短链接不存在");
        }
        return Mono.fromRunnable(()->{
            response.setStatusCode(HttpStatus.FOUND);
            response.getHeaders().setLocation(URI.create(shortUrlVO.getUrl()));
        });
    }
}
