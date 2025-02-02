package com.github.ozanaaslan.lwjwl.web.endpoint.response;

import lombok.Getter;

public class URL {

    @Getter
    private final String url;

    public URL(String url) {
        this.url = url;
    }

}
