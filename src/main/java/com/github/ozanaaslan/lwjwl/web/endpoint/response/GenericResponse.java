package com.github.ozanaaslan.lwjwl.web.endpoint.response;

import org.json.JSONObject;

public class GenericResponse {

    private final int status;
    private final String message;
    private final String topic;
    private final long timestamp;
    private final String details;

    // Constructor
    public GenericResponse(int status, String message, String topic, String details) {
        this.status = status;
        this.message = message;
        this.topic = topic;
        this.timestamp = System.currentTimeMillis();
        this.details = details;
    }

    // Convert to JSON string
    public String toJson() {
        JSONObject json = new JSONObject();
        json.put("status", status);
        json.put("message", message);
        json.put("error", topic);
        json.put("timestamp", timestamp);
        json.put("details", details);
        return json.toString();
    }

}
