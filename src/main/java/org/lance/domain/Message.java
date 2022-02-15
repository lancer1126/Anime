package org.lance.domain;

import lombok.Data;

import java.util.Map;

@Data
public class Message {
    private String id;
    private int type;
    private int status;
    private String msg;
    private MessageData data;
}
