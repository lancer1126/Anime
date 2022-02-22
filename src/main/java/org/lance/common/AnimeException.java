package org.lance.common;

import lombok.Data;

public class AnimeException extends RuntimeException {

    private static final long serialVersionUID = 3113398967393655595L;

    private int code;

    private String message;

    public AnimeException(String message) {
        super(message);
    }

    public AnimeException(int code, String message) {
        super(message);
        this.code = code;
    }
}
