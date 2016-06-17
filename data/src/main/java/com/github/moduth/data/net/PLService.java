package com.github.moduth.data.net;

import com.morecruit.ext.component.logger.Logger;

import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import okhttp3.Cookie;
import okhttp3.CookieJar;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

public final class PLService {

    private static final String TAG = "PLService";

    private static final String API_DEV_URL = "https://api.github.com/";
    private static final String API_PRODUCT_URL = "https://api.github.com/";

    private static final String WEB_DEV_URL = "http://your.api.com";
    private static final String WEB_PRODUCT_URL = "http://your.api.com";

    private static final boolean IS_DEV = true;

    private static PLService mInstance;

    private static PLService mSyncInstance;

    public static String vuser = "";

    private Retrofit mRetrofit;

    public static String getActiveHttpScheme() {
        return IS_DEV ? WEB_DEV_URL : WEB_PRODUCT_URL;
    }

    public static PLService getInstance() {
        if (mInstance == null) {
            synchronized (PLService.class) {
                if (mInstance == null) {
                    mInstance = new PLService();
                }
            }
        }
        return mInstance;
    }

    public static PLService getSynchronousInstance() {
        if (mSyncInstance == null) {
            synchronized (PLService.class) {
                if (mSyncInstance == null) {
                    mSyncInstance = new PLService(false);
                }
            }
        }
        return mSyncInstance;
    }

    public <T> T createApi(Class<T> clazz) {
        return mRetrofit.create(clazz);
    }

    private PLService() {
        this(true);
    }

    private PLService(boolean useRxJava) {
        Retrofit.Builder builder = new Retrofit.Builder()
                .baseUrl(IS_DEV ? API_DEV_URL : API_PRODUCT_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .client(getClient());
        if (useRxJava) {
            builder.addCallAdapterFactory(RxJavaCallAdapterFactory.create());
        }
        mRetrofit = builder.build();
    }

    private OkHttpClient getClient() {
        HttpLoggingInterceptor logging = new HttpLoggingInterceptor();

        if (IS_DEV) {
            logging.setLevel(HttpLoggingInterceptor.Level.BODY);
        } else {
            logging.setLevel(HttpLoggingInterceptor.Level.BASIC);
        }

        SSLSocketFactory sslSocketFactory = null;

        try {
            // Create a trust manager that does not validate certificate chains
            final TrustManager[] trustAllCerts = new TrustManager[]{
                    new X509TrustManager() {
                        @Override
                        public void checkClientTrusted(X509Certificate[] chain, String authType)
                                throws CertificateException {
                        }

                        @Override
                        public void checkServerTrusted(X509Certificate[] chain, String authType)
                                throws CertificateException {
                        }

                        @Override
                        public X509Certificate[] getAcceptedIssuers() {
                            return new X509Certificate[]{};
                        }
                    }
            };

            // Install the all-trusting trust manager
            final SSLContext sslContext = SSLContext.getInstance("SSL");
            sslContext.init(null, trustAllCerts, new java.security.SecureRandom());
            // Create an ssl socket factory with our all-trusting manager
            sslSocketFactory = sslContext.getSocketFactory();
        } catch (Exception e) {
            Logger.e(TAG, e.getMessage());
        }

        return new OkHttpClient.Builder()
                .addInterceptor(new HeadInterceptor())
                .addInterceptor(logging)
                .connectTimeout(10, TimeUnit.SECONDS)
                .readTimeout(20, TimeUnit.SECONDS)
                .sslSocketFactory(sslSocketFactory)
                .hostnameVerifier((hostname, session) -> true)
                .cookieJar(new CookieJar() {
                    private final HashMap<HttpUrl, List<Cookie>> cookieStore = new HashMap<>();

                    @Override
                    public void saveFromResponse(HttpUrl url, List<Cookie> cookies) {
                        cookieStore.put(HttpUrl.parse(url.host()), cookies);
                    }

                    @Override
                    public List<Cookie> loadForRequest(HttpUrl url) {
                        List<Cookie> cookies = cookieStore.get(HttpUrl.parse(url.host()));
                        return cookies != null ? cookies : new ArrayList<>();
                    }
                })
                .build();
    }

//    Authenticator mAuthenticator = new Authenticator() {
//        @Override public Request authenticate(Route route, Response response)
//                throws IOException {
//            Your.sToken = service.refreshToken();
//            return response.request().newBuilder()
//                    .addHeader("Authorization", Your.sToken)
//                    .build();
//        }
//    }
}
