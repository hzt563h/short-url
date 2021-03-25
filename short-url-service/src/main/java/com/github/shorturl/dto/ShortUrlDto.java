package com.github.shorturl.dto;


import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
public class ShortUrlDto {
    /**
     * 要生成的短链接地址
     */
    @NotNull
    private String url;


}
