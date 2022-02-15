package org.lance.common;

import lombok.Data;

@Data
public class AnimeException extends Exception {

    private static final long serialVersionUID = 3113398967393655595L;

    private int code;

    private String message;

    public AnimeException() {
    }

    public AnimeException(int code, String message) {
        this.code = code;
        this.message = message;
    }
}
