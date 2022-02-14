package org.lance.pojo;

import lombok.Data;

import java.util.HashMap;
import java.util.Map;

@Data
public class RequestHeader {
    HashMap<String, String> headers;

    public RequestHeader() {};

    public RequestHeader(HashMap<String, String> headers) {
        this.headers = headers;
    }
}
