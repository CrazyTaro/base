package com.taro.base.api.base;

/**
 * Created by taro on 16/11/8.
 */

public class ApiResult<T> {
    public int status_code;
    public String message;
    public T data;

    public String getMessage() {
        return message;
    }
}
