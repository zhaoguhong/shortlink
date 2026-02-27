package com.zhaoguhong.shortlink.common.exception;

/**
 * 业务异常，包含可返回给前端的业务错误码。
 *
 * @author zhaoguhong
 * @date 2026/2/27
 */
public class BizException extends RuntimeException {

    private final int code;

    public BizException(int code, String message) {
        super(message);
        this.code = code;
    }

    public BizException(String message) {
        this(1, message);
    }

    public int getCode() {
        return code;
    }
}
