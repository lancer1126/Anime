package org.lance.network.http.common;

import lombok.Data;

@Data
public class ResponseMessage {
    private int statusCode;
    private String body;
}
