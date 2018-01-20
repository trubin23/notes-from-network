package com.example.trubin23.commitsfromnetwork.storage.model;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.squareup.moshi.Json;

/**
 * Created by Andrey on 01.01.2018.
 */

public class CommitStorage {

    @Json(name = "sha")
    private String mSha;
    @Json(name = "commit")
    private CommitDescription mCommitDescription;

    private Long mRepoId = null;

    @NonNull
    public String getSha() {
        return mSha;
    }

    public void setSha(@NonNull String sha) {
        mSha = sha;
    }

    @NonNull
    public CommitDescription getCommitDescription() {
        return mCommitDescription;
    }

    public void setCommitDescription(@NonNull CommitDescription commitDescription) {
        mCommitDescription = commitDescription;
    }

    @Nullable
    public Long getRepoId() {
        return mRepoId;
    }

    public void setRepoId(@Nullable Long repoId) {
        mRepoId = repoId;
    }
}
