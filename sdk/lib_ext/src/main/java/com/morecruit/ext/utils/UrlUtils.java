package com.morecruit.ext.utils;

import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

/**
 * @author markzhai on 16/3/1
 * @version 1.0.0
 */
public final class UrlUtils {

    private UrlUtils() {
        // static usage.
    }

    /**
     * Check whether the url is a network url.
     *
     * @return True if the url is a network url.
     */
    public static boolean isNetworkUrl(String url) {
        if (url == null || url.length() == 0) {
            return false;
        }
        return isHttpUrl(url) || isHttpsUrl(url);
    }

    /**
     * Check whether the url is a http: url.
     *
     * @return True if the url is a http: url.
     */
    public static boolean isHttpUrl(String url) {
        return (null != url) &&
                (url.length() > 6) &&
                StringUtils.startsWithIgnoreCase(url, "http://");
    }

    /**
     * Check whether the url is a https: url
     *
     * @return True if the url is a https: url.
     */
    public static boolean isHttpsUrl(String url) {
        return (null != url) &&
                (url.length() > 7) &&
                StringUtils.startsWithIgnoreCase(url, "https://");
    }

    /**
     * 由host ＋ 参数创建Url
     *
     * @param host   主机地址
     * @param params 参数
     * @return 拼接好的URL地址
     */
    public static String buildUrl(String host, Map<String, Object> params) {
        if (params == null || params.size() == 0) {
            return host;
        }

        ArrayList<NameValuePair> paramPairs = new ArrayList<NameValuePair>(params.size());
        for (String key : params.keySet()) {
            paramPairs.add(new BasicNameValuePair(key, String.valueOf(params.get(key))));
        }

        StringBuilder url = new StringBuilder(host);
        if (paramPairs.size() > 0) {
            url.append(url.indexOf("?") == -1 ? "?" : "&").append(URLEncodedUtils.format(paramPairs, HTTP.UTF_8));
        }
        return url.toString();
    }

    /**
     * 解析url中的参数
     *
     * @param url 用于解析的Url
     * @return 参数的map
     */
    public static Map<String, Object> getParams(String url) {
        Map<String, Object> params = new LinkedHashMap<>();

        if (!StringUtils.isEmpty(url) && url.contains("?")) {
            url = url.substring(url.indexOf('?') + 1);
            String[] parameter = url.split("&");
            for (String param : parameter) {
                String[] entry = param.split("=");
                String key = entry[0];
                String value = (entry.length == 2) ? entry[1] : "";
                params.put(key, value);
            }
        }

        return params;
    }

    /**
     * 解析主机地址
     *
     * @param url 用于解析的Url
     * @return 主机地址
     */
    public static String getHost(String url) {
        if (!StringUtils.isEmpty(url) && url.indexOf('?') > 0) {
            return url.substring(0, url.indexOf('?'));
        }

        return url;
    }

    /**
     * 从url解析文件名 如：http://www.baidu.com/xxx.jpg 解析的结果为:xxx.jpg
     *
     * @param url 用于解析的Url
     * @return 文件名
     */
    public static String getFileName(String url) {
        return FileUtils.getFileName(url);
    }

    /**
     * 删除参数
     *
     * @param url      用于处理的url
     * @param paramKey 需要删除的Key值
     * @return 处理后的url
     */
    public static String removeParam(String url, String paramKey) {
        String host = getHost(url);
        Map<String, Object> params = getParams(url);
        params.remove(paramKey);

        return buildUrl(host, params);
    }

    /**
     * 删除参数
     *
     * @param url       用于处理的url
     * @param paramKeys 需要删除的Key值 Set
     * @return 处理后的url
     */
    public static String removeParams(String url, Set<String> paramKeys) {
        String host = getHost(url);
        Map<String, Object> params = getParams(url);
        for (String key : paramKeys) {
            params.remove(key);
        }
        return buildUrl(host, params);
    }
}
