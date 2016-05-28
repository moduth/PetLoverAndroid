package com.github.moduth.domain.repository;




import com.github.moduth.domain.model.user.Vuser;

import rx.Observable;

/**
 * Created by markzhai on 16/2/27
 *
 * @author markzhai
 * @version 1.0.0
 */
public interface UserRepository {

    Observable<Vuser> login(String mobile, String password);
}
