package com.github.moduth.domain.repository;




import com.github.moduth.domain.model.user.ReposEntity;

import java.util.List;

import rx.Observable;

/**
 * Created by Abner on 16/6/17.
 * Email nimengbo@gmail.com
 * github https://github.com/nimengbo
 */
public interface ReposRepository {

    Observable<List<ReposEntity>> getReposList(String user);
}
