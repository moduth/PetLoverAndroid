package com.github.moduth.petlover.mapper;

import com.github.moduth.domain.model.repos.ReposEntity;
import com.github.moduth.petlover.internal.di.PerActivity;
import com.github.moduth.petlover.model.ReposModel;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

/**
 * Created by Abner on 16/6/16.
 * Email nimengbo@gmail.com
 * github https://github.com/nimengbo
 */
@PerActivity
public class ReposDataMapper {

    @Inject
    public ReposDataMapper(){
    }

    public List<ReposModel> transform(List<ReposEntity> reposEntities){
        if(reposEntities == null){
            throw new IllegalArgumentException("Cannot transform a null value");
        }
        List<ReposModel> reposModels = new ArrayList<>();
        for(ReposEntity reposEntity : reposEntities){
            ReposModel reposModel = new ReposModel();
            reposModel.setId(reposEntity.id);
            reposModel.setReposName(reposEntity.fullName);
            reposModels.add(reposModel);
        }

        return reposModels;
    }
}
