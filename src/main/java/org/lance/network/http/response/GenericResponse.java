package org.lance.network.http.response;

import lombok.Data;

@Data
public class GenericResponse {
    private Integer code;
    private String message;
}
