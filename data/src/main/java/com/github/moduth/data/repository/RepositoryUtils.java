package com.github.moduth.data.repository;

import com.github.moduth.data.exception.NetworkConnectionException;
import com.github.moduth.data.exception.ResponseException;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.github.moduth.domain.model.MrResponse;
import com.morecruit.ext.component.logger.Logger;

import rx.Observable;

/**
 * @author markzhai on 16/2/28
 * @version 1.0.0
 */
public class RepositoryUtils {

    private static final String TAG = "RepositoryUtils";

    private static Gson mGson = new GsonBuilder()
            .setDateFormat("yyyy-MM-dd HH:mm:ss").create();

    public static <T> Observable<T> extractData(Observable<MrResponse> observable, Class<T> clazz) {
        return observable.flatMap(response -> {
            if (response == null) {
                return Observable.error(new NetworkConnectionException());
            } else if (response.getStatusCode() == ResponseException.STATUS_CODE_SUCCESS) {
                return Observable.just(mGson.fromJson(mGson.toJson(response.data), clazz));
            } else {
                Logger.e(TAG, response.data);
                return Observable.error(new ResponseException(response));
            }
        });
    }

}
