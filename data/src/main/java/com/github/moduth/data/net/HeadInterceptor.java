package com.github.moduth.data.net;

import com.morecruit.ext.Ext;
import com.morecruit.ext.component.info.Network;
import com.morecruit.ext.utils.ViewUtils;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

/**
 * 设置请求的头拦截器
 */
public class HeadInterceptor implements Interceptor {

    private static final String TAG = "HeadInterceptor";

    /**
     * APPID	INT	应用ID (iOS: 1, Android: 2)
     * APPVER	STRING	客户端版本号，例如 1.2.1
     * VUSER	STRING	用户凭证（登录、注册后获得的一个哈希字符串）
     * 客户端统一采用 POST 键值对方式提交数据给服务端
     * Content-Type: multipart/form-data;
     * 或者
     * Content-Type: application/x-www-form-urlencoded
     */
    @Override
    public Response intercept(Chain chain) throws IOException {
        Request original = chain.request();

        Request request = original.newBuilder()
                .method(original.method(), original.body())
//                .addHeader("Content-Type", "multipart/form-data")
                //.addHeader("Accept", "application/vnd.github.v3+json")
                // iOS: 1, Android: 2
                .addHeader("APPID", "2")
                // 客户端版本号，例如 1.2.1
                .addHeader("APPVER", Ext.g().getVersionName())
                .addHeader("APP-BUILD-NO", Ext.g().getBuilderNumber())
                // 用户凭证（登录、注册后获得的一个哈希字符串）
                .addHeader("VUSER", MrService.vuser)
                .addHeader("C-NETWORK", Network.getType().getName())
                .addHeader("C-SCREEN-SCALE", String.valueOf(ViewUtils.getDensity())) //缩放比 1/2/3
                .addHeader("C-PIC-MODE", "1") //图片浏览模式 0:无图 1:自适应 2:原图
                .addHeader("C-SCREEN-WIDTH", String.valueOf(Ext.g().getScreenWidth()))
                .addHeader("C-SCREEN-HEIGHT", String.valueOf(Ext.g().getScreenHeight()))
                .build();

//        Logger.d(TAG, String.format("Sending request %s", toGetUrl(request)));

        return chain.proceed(request);
    }
}
