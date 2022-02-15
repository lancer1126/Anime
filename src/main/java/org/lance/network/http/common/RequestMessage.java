package org.lance.network.http.common;

import lombok.Data;

import java.util.HashMap;
import java.util.Map;

@Data
public class RequestMessage {
    private String uri;
    private Map<String, String> headers = new HashMap<>();

    public RequestMessage() {
    }

    public RequestMessage(String uri, Map<String, String> headers) {
        super();
        this.uri = uri;
        this.headers = headers;
    }
}
