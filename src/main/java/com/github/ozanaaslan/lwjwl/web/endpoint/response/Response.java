package com.github.ozanaaslan.lwjwl.web.endpoint.response;

import com.github.ozanaaslan.lwjwl.util.JsonParser;
import lombok.Getter;
import lombok.Setter;

import java.io.File;

/**
 * It's a class that holds the response that will be sent to the client
 */
public class Response {

    @Getter
    @Setter
    private int statusCode;
    @Getter
    @Setter
    private byte[] response;
    @Getter
    @Setter
    private ContentType contentType = null;
    @Getter
    @Setter
    private URL redirect = null;
    @Getter
    @Setter
    private File file = null;
    @Getter
    @Setter
    private ResponseType responseType;

    public Response(Status status, ContentType contentType, String response) {
        this(status.getStatusCode(), contentType, response);
    }

    public Response(Status status, ContentType contentType, byte[] bytes) {
        this.statusCode = status.getStatusCode();
        this.response = bytes;
        this.contentType = contentType;
        this.responseType = ResponseType.BYTES;
    }

    public Response(int status, URL redirectionUrl) {
        this.redirect = redirectionUrl;
        this.statusCode = status;
        this.responseType = ResponseType.REDIRECT;
    }

    public Response(int status, boolean download, File file) {
        this.file = file;
        this.statusCode = status;
        this.responseType = ResponseType.FILE;
        if (download)
            this.responseType = ResponseType.DOWNLOAD;
    }

    public Response(Status status, byte[] bytes) {
        this.response = bytes;
        this.statusCode = status.getStatusCode();
        this.responseType = ResponseType.BYTES;
    }

    public Response(int status, ContentType contentType, String response) {
        this.statusCode = status;
        this.response = response.getBytes();
        this.contentType = contentType;
        this.responseType = ResponseType.TEXT;
    }

    public static Response json(int status, Object o){
        return new Response(status, ContentType.APPLICATION_JSON, JsonParser.toJson(o));
    }

    public static Response plain(int status, String plain){
        return new Response(status, ContentType.TEXT_PLAIN, plain);
    }

    public static Response file(int status, File file){
        return new Response(status, true, file);
    }

    public static Response redirect(int status, String url){
        return new Response(status, new URL(url));
    }

}

