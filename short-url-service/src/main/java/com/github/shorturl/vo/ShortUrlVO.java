package com.github.shorturl.vo;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ShortUrlVO {
    private String hashValue;

    private String url;
}
