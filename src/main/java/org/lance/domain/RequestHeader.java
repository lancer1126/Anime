package org.lance.domain;

import lombok.Data;

import java.util.HashMap;
import java.util.Map;

@Data
public class RequestHeader {
    private int code;
    private Map<String, String> headers;

    public RequestHeader() {};

    public RequestHeader(Map<String, String> headers) {
        this.headers = headers;
    }
}
