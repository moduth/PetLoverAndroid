package com.morecruit.domain.interactor.user;

import com.morecruit.domain.executor.PostExecutionThread;
import com.morecruit.domain.executor.ThreadExecutor;
import com.morecruit.domain.interactor.UseCase;
import com.morecruit.domain.repository.UserRepository;

import javax.inject.Inject;

import rx.Observable;

/**
 * @author markzhai on 16/3/29
 * @version 1.0.0
 */
public class UpdateProfile extends UseCase<Void> {

    private final UserRepository userRepository;

    private String nickName;
    private int age;
    private int industry1;
    private int industry2;
    private int province;
    private int city;
    private String signature;

    @Inject
    public UpdateProfile(UserRepository userRepository,
                         ThreadExecutor threadExecutor,
                         PostExecutionThread postExecutionThread) {
        super(threadExecutor, postExecutionThread);
        this.userRepository = userRepository;
    }

    public void setNickName(String nickName) {
        this.nickName = nickName;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public void setIndustry1(int industry1) {
        this.industry1 = industry1;
    }

    public void setIndustry2(int industry2) {
        this.industry2 = industry2;
    }

    public void setProvince(int province) {
        this.province = province;
    }

    public void setCity(int city) {
        this.city = city;
    }

    public void setSignature(String signature) {
        this.signature = signature;
    }

    @Override
    public Observable<Void> buildUseCaseObservable() {
        return userRepository.updateProfile(nickName, age, industry1, industry2,
                province, city, signature);
    }
}
