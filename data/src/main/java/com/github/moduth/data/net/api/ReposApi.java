package com.github.moduth.data.net.api;


import com.github.moduth.domain.model.repos.ReposEntity;

import java.util.List;

import retrofit2.http.GET;
import retrofit2.http.Path;
import rx.Observable;

/**
 * Created by Abner on 16/6/17.
 * Email nimengbo@gmail.com
 * github https://github.com/nimengbo
 */
public interface ReposApi {


    @GET("users/{user}/repos")
    Observable<List<ReposEntity>> login(@Path("user") String user);
}
