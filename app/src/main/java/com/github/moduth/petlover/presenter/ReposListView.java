package com.github.moduth.petlover.presenter;

import com.github.moduth.petlover.model.ReposModel;
import com.github.moduth.petlover.view.MvpView;

import java.util.List;

/**
 * Created by Abner on 16/6/16.
 * Email nimengbo@gmail.com
 * github https://github.com/nimengbo
 */
public interface ReposListView extends MvpView {

    void userList(List<ReposModel> userModels);

}
