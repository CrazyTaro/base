package com.taro.base.api.base;

import android.support.annotation.NonNull;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

/**
 * Created by taro on 16/12/23.
 */

public class ApiManager {
    public static final String END_POINT = "";

    private static Retrofit mRetrofit;
    private static ApiManager mInstance;

    public static synchronized final ApiManager getInstance() {
        if (mInstance == null) {
            mInstance = new ApiManager();
        }
        return mInstance;
    }

    public static final ApiException throwApiException(ApiResult result) throws ApiException {
        throw new ApiException(result);
    }

    public <T> Observable<ApiResult<T>> validResult(@NonNull Observable<ApiResult<T>> obs) {
        return obs.doOnNext(new Action1<ApiResult<T>>() {
            @Override
            public void call(ApiResult<T> tApiResult) {
                if (tApiResult.status_code != 200) {
                    throwApiException(tApiResult);
                }
            }
        });
    }

    public <T> Observable<ApiResult<T>> schedulersToUI(@NonNull Observable<ApiResult<T>> obs) {
        Observable<ApiResult<T>> obser = validResult(obs);
        return obser
                .subscribeOn(Schedulers.io())
                .unsubscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }

    public <T> T createService(Class<T> clazz) {
        return mRetrofit.create(clazz);
    }

    private ApiManager() {
        Retrofit.Builder builder = new Retrofit.Builder();
        builder.baseUrl(END_POINT);
        builder.addConverterFactory(GsonConverterFactory.create());
        builder.addCallAdapterFactory(RxJavaCallAdapterFactory.createWithScheduler(Schedulers.io()));
        builder.client(createClient());
        mRetrofit = builder.build();
    }

    private OkHttpClient createClient() {
        OkHttpClient.Builder builder = new OkHttpClient.Builder();
        HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor();
        interceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
        builder.addNetworkInterceptor(new Interceptor() {
            @Override
            public Response intercept(Chain chain) throws IOException {
                //TODO:添加header等
                String token = "";
                //若token不存在则不添加token
                if (token == null || token.length() <= 0) {
                    return chain.proceed(chain.request());
                } else {
                    //若token存在则在header中添加上token
                    Request.Builder newBuilder = chain.request().newBuilder();
                    newBuilder.addHeader("Authorization", "Bearer \\".concat(token));
                    return chain.proceed(newBuilder.build());
                }
            }
        });
        builder.addNetworkInterceptor(interceptor);
        return builder.build();
    }
}
