package com.opengauss.vo;

import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;

@Data
@Accessors(chain = true)
public class SysResult implements Serializable {

    private static final long serialVersionUID = 3208248423120492873L;
    private Integer status;
    private String msg;
    private Object obj;

    public SysResult(Integer status, String msg, Object obj) {
        this.status = status;
        this.msg = msg;
        this.obj = obj;
    }

    public SysResult() {
    }

    public SysResult(Throwable e) {
        this.status = 201;
        this.msg = e.getMessage();
    }

    //定义成功的静态方法
    public static SysResult ok(String msg, Object obj) {
        return new SysResult(200, msg, obj);
    }

    //表示定义成功的静态方法
    public static SysResult ok() {
        return new SysResult(200, null, null);
    }

    public static SysResult ok(Object data) {
        return new SysResult(200, null, data);
    }

    //定义一个失败的静态方法
    public static SysResult fail(String msg, Object data) {
        return new SysResult(201, msg, data);
    }

    public static SysResult fail() {
        return new SysResult(201, null, null);
    }
}
