package com.framework.boot.common.exception;

/**
 * 自定义基础异常
 *
 * @author linjianbin
 * @date 2020/12/21 10:06 下午
 */
public class BaseException extends RuntimeException{

    private String msg;

    private String code;

    public BaseException(String msg) {
        super(msg);
        this.msg = msg;
    }

    public BaseException(String msg, Throwable e) {
        super(msg, e);
        this.msg = msg;
    }

    public BaseException(String code, String msg) {
        super(msg);
        this.msg = msg;
        this.code = code;
    }

    public BaseException(String code, String msg, Throwable e) {
        super(msg, e);
        this.msg = msg;
        this.code = code;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }
}