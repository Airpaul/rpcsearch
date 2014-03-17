package com.baidu.fengchao.stargate.rpcsearch.web.common;

/**
 * author: liangyafei
 * date: 14-3-10
 */
public class CommonResponse<T> {
    private int status = 200;
    private String message;
    private T data;

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
