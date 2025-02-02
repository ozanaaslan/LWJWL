package com.github.ozanaaslan.lwjwl.web.endpoint;

import com.github.ozanaaslan.lwjwl.LWJWL;
import com.github.ozanaaslan.lwjwl.web.endpoint.response.ContentType;
import com.github.ozanaaslan.lwjwl.web.endpoint.response.Response;
import com.github.ozanaaslan.lwjwl.web.session.Cookie;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import lombok.Getter;
import lombok.SneakyThrows;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public abstract class EndpointController implements IEndpoint, HttpHandler {


    @Getter
    private HashMap<String, String> queryParameters;
    @Getter
    private HashMap<String, String> bodyParameters;
    @Getter
    private List<Cookie> cookies;

    @Getter
    private String requesterAddress;
    @Getter
    private byte[] requestBody;
    @Getter
    private String query = "";

    @Getter
    private HttpExchange exchange;

    public EndpointController() {
    }


    @SneakyThrows
    public void setCookie(String key, Object value) {
        Cookie cookie = new Cookie(key, value.toString());
        setCookie(cookie);
    }

    @SneakyThrows
    public void setCookie(Cookie cookie) {
        exchange.getResponseHeaders().add("Set-Cookie", cookie.toString());
    }

    @SneakyThrows
    public void removeCookie(String key) {
        setCookie(key, null);
    }

    public Cookie getCookie(String key) {
        List<Cookie> cookies;
        if ((cookies = this.cookies) != null) {
            for (Cookie cookie : cookies) {
                if (cookie.getKey().equalsIgnoreCase(key)) {
                    return cookie;
                }
            }
        }
        return null;
    }

    private List<Cookie> getCookiez() {
        List<String> cook = null;
        if ((cook = exchange.getRequestHeaders().get("Cookie")) != null) {
            String[] cooks = cook.toString().replaceAll("\\[", "").replaceAll("]", "").split("; ");
            List<Cookie> cookies = new ArrayList();
            for (String cooki : cooks) {
                try {
                    cookies.add(new Cookie(cooki.split("=")[0], cooki.split("=")[1]));
                } catch (IndexOutOfBoundsException ioobe) {
                    cookies.add(new Cookie(cooki.split("=")[0], ""));
                }
            }
            return cookies;
        }
        return null;
    }

    private void setContentType(ContentType contentType) {
        exchange.getResponseHeaders().add("Content-Type", contentType.getContentType());
    }

    private void setHttpExchange(HttpExchange httpExchange) {
        this.exchange = httpExchange;
        this.requesterAddress = httpExchange.getRemoteAddress().getAddress().getHostAddress();
        this.requestBody = readAllBytes(httpExchange.getRequestBody());
        this.query = httpExchange.getRequestURI().getQuery();

        this.cookies = getCookiez();
        this.queryParameters = getQueryMap();
        this.bodyParameters = getBodyMap();
    }

    protected HashMap<String, String> getQueryMap() {
        if (getQuery() != null && !getQuery().equals(""))
            return getMapFromString(getQuery());
        return null;
    }

    protected HashMap<String, String> getBodyMap() {
        return getMapFromString(decode(new String(this.requestBody)));
    }

    protected HashMap<String, String> getMapFromString(String string) {
        String[] quers = string.split("&");
        HashMap<String, String> hashMap = new HashMap<>();
        for (String str : quers)
            hashMap.put(str.split("=")[0], str.split("=").length == 2 ? str.split("=")[1] : null);
        return hashMap;
    }

    private String decode(String s) {
        return URLDecoder.decode(s, StandardCharsets.UTF_8);
    }

    @SneakyThrows
    private byte[] readAllBytes(InputStream inputStream) {
        byte[] bytes = new byte[inputStream.available()];
        int read = 0;

        while (inputStream.available() > 0) {
            bytes[read] = (byte) inputStream.read();
            read++;
        }
        return bytes;
    }

    @SneakyThrows
    public void respond(int responseCode, byte[] responseBytes) {
        exchange.sendResponseHeaders(responseCode, responseBytes.length);
        exchange.getResponseBody().write(responseBytes);
        exchange.getResponseBody().flush();
        exchange.close();
    }

    public void respond(Response response) {
        if (response == null) {
            respond(500, "Internal Server Error");
            return;
        }

        switch (response.getResponseType()) {
            case FILE:
                fileResponse(response.getFile());
                return;

            case TEXT:
                respond(response.getStatusCode(), new String(response.getResponse()));
                return;

            case BYTES:
                if (response.getContentType() != null) {
                    respond(response.getStatusCode(), response.getContentType(), response.getResponse());
                    return;
                }
                respond(response.getStatusCode(), response.getResponse());
                return;

            case REDIRECT:
                redirect(response.getRedirect().getUrl());
                return;

            case DOWNLOAD:
                fileDownloadResponse(response.getFile());
        }

    }

    @SneakyThrows
    private void fileDownloadResponse(File file) {
        exchange.getResponseHeaders().add("Content-Disposition", "attachment; filename=" + file.getName());
        fileResponse(file);
    }

    @SneakyThrows
    private void fileResponse(File file) {
        exchange.sendResponseHeaders(200, file.length());
        Files.copy(file.toPath(), exchange.getResponseBody());
        exchange.getResponseBody().flush();
        exchange.close();
    }

    private void redirect(String url) {
        respond(200, "<meta http-equiv=\"Refresh\" content=\"0; url=" + url + "\">");
    }

    @SneakyThrows
    private void respond(int responseCode, String response) {
        respond(responseCode, ContentType.TEXT_HTML, response.getBytes());
    }

    /**
     * Sets the content type and then responds with the given response code and response.
     *
     * @param responseCode The HTTP response code.
     * @param contentType The type of content you're sending back.
     * @param response The response body.
     */
    private void respond(int responseCode, ContentType contentType, byte[] response) {
        setContentType(contentType);
        respond(responseCode, response);
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        setHttpExchange(exchange);
        LWJWL.getLwjwl().getLogger().info("Handling request from " + requesterAddress + " for "
                + exchange.getRequestURI() + " with method " + exchange.getRequestMethod());
        Response r = handle(this);
        respond(r);
    }
}
