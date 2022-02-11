package org.lance.common;

import lombok.Data;

import java.io.Serializable;

@Data
public class ResultEntity<T> implements Serializable {
    private static final long serialVersionUID = 1064373066413371092L;

    public static final int SUCCESS = 0;

    public static final int FAIL = -1;

    private Integer status; // 0 success -1 fail
    private Integer code;
    private String msg;
    private T data;

    public static <T> ResultEntity<T> success() {
        return new ResultEntity<T>(SUCCESS, 0, "success");
    }

    public static <T> ResultEntity<T> success(T data) {
        return new ResultEntity<T>(SUCCESS, 0, "success", data);
    }

    public static <T> ResultEntity<T> error() {
        return new ResultEntity<T>(FAIL, -1, "fail");
    }

    public static <T> ResultEntity<T> errorWithValue(T data) {
        return new ResultEntity<T>(FAIL, -1, "fail", data);
    }

    public static <T> ResultEntity<T> error(Integer code, String msg) {
        return new ResultEntity<T>(FAIL, code, msg);
    }

    public static <T> ResultEntity<T> error(String msg) {
        return new ResultEntity<T>(FAIL, -1, msg);
    }

    public static <T> ResultEntity<T> exception() {
        return new ResultEntity<T>(FAIL, -500, "system error");
    }

    public ResultEntity() {
    }

    public ResultEntity(Integer status, Integer code, String msg, T data) {
        this.status = status;
        this.code = code;
        this.msg = msg;
        this.data = data;
    }

    public ResultEntity(Integer status, Integer code, String msg) {
        this.status = status;
        this.code = code;
        this.msg = msg;
    }
}
