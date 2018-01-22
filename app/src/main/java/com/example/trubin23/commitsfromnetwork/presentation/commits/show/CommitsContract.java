package com.example.trubin23.commitsfromnetwork.presentation.commits.show;

import android.support.annotation.NonNull;

import com.example.trubin23.commitsfromnetwork.presentation.common.BaseView;
import com.example.trubin23.commitsfromnetwork.storage.model.Commit;

import java.util.List;

/**
 * Created by Andrey on 31.12.2017.
 */

class CommitsContract {

    interface View extends BaseView {
        void setCommits(@NonNull List<Commit> commitsView);
        void loadFinished();
        void showToast(@NonNull String message);
        void lastPageLoaded();
        void setRepoData(@NonNull String owner, @NonNull String repo);
    }

    interface Presenter {
        void loadCommits(@NonNull String owner, @NonNull String repo,
                         @NonNull Integer pageNumber, @NonNull Integer pageSize);
        void saveRepoData(@NonNull String owner, @NonNull String repo);
        void loadRepoData();
    }
}
