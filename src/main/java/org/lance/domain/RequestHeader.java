package org.lance.domain;

import lombok.Data;

import java.util.HashMap;

@Data
public class RequestHeader {
    HashMap<String, String> headers;

    public RequestHeader() {};

    public RequestHeader(HashMap<String, String> headers) {
        this.headers = headers;
    }
}
