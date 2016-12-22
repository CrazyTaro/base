package com.taro.base.api.base;

/**
 * Created by taro on 16/10/31.
 */

public class ApiException extends RuntimeException {
    private String mErrorMsg = null;

    public ApiException() {

    }

    public ApiException(Object obj) {
        if (obj == null) {
            mErrorMsg = "未知错误";
        } else {
            if (obj instanceof ApiResult) {
                ApiResult result = (ApiResult) obj;
                mErrorMsg = result.getMessage();
            } else {
                mErrorMsg = String.valueOf(obj);
            }
        }
    }

    @Override
    public String getMessage() {
        if (mErrorMsg != null) {
            return mErrorMsg;
        } else {
            return super.getMessage();
        }
    }
}
