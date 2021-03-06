package com.example.trubin23.commitsfromnetwork.data.source;

import android.database.Cursor;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.example.trubin23.commitsfromnetwork.data.Commit;
import com.example.trubin23.commitsfromnetwork.data.source.database.CommitDao;
import com.example.trubin23.commitsfromnetwork.data.source.database.CommitDaoImpl;
import com.example.trubin23.commitsfromnetwork.data.source.database.DatabaseHelper;
import com.example.trubin23.commitsfromnetwork.data.source.database.OwnerDao;
import com.example.trubin23.commitsfromnetwork.data.source.database.OwnerDaoImpl;
import com.example.trubin23.commitsfromnetwork.data.source.database.RepoDao;
import com.example.trubin23.commitsfromnetwork.data.source.database.RepoDaoImpl;
import com.example.trubin23.commitsfromnetwork.data.source.preferences.CommitsSharedPreferences;
import com.example.trubin23.commitsfromnetwork.data.source.remote.RetrofitClient;
import com.example.trubin23.commitsfromnetwork.data.source.remote.model.CommitMapper;
import com.example.trubin23.commitsfromnetwork.data.source.remote.model.load.CommitLoad;
import com.example.trubin23.commitsfromnetwork.util.AppExecutors;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by Andrey on 23.01.2018.
 */

public class CommitsRepository implements CommitsDataSource {

    private static CommitsRepository INSTANCE;

    private AppExecutors mAppExecutors;

    private CommitsSharedPreferences mCommitsSharedPreferences;

    private OwnerDao mOwnerDao;
    private RepoDao mRepoDao;
    private CommitDao mCommitDao;

    private CommitsRepository(@NonNull AppExecutors appExecutors,
                              @NonNull CommitsSharedPreferences commitsSharedPreferences,
                              @NonNull DatabaseHelper databaseHelper) {
        mAppExecutors = appExecutors;

        mCommitsSharedPreferences = commitsSharedPreferences;

        mOwnerDao = new OwnerDaoImpl(databaseHelper);
        mRepoDao = new RepoDaoImpl(databaseHelper);
        mCommitDao = new CommitDaoImpl(databaseHelper);
    }

    public static CommitsRepository getInstance(@NonNull AppExecutors appExecutors,
                                                @NonNull CommitsSharedPreferences commitsSharedPreferences,
                                                @NonNull DatabaseHelper databaseHelper) {
        if (INSTANCE == null) {
            INSTANCE = new CommitsRepository(appExecutors, commitsSharedPreferences, databaseHelper);
        }
        return INSTANCE;
    }

    @Override
    public void savePreference(@NonNull String key, @NonNull String value) {
        Runnable runnable = () -> mCommitsSharedPreferences.putString(key, value);

        mAppExecutors.getSharedPreferencesThread().execute(runnable);
    }

    @Override
    public void getPreference(@NonNull String key, @NonNull GetPreferenceCallback callback) {
        Runnable runnable = () -> {
            String value = mCommitsSharedPreferences.getString(key);
            mAppExecutors.getMainThread().execute(() -> callback.onPreference(value));
        };

        mAppExecutors.getSharedPreferencesThread().execute(runnable);
    }

    @Override
    public void getCommitsDb(@NonNull String owner, @NonNull String repo,
                             @NonNull LoadCommitsCallback callback) {
        Runnable runnable = () -> {
            Long ownerId = mOwnerDao.getOwnerId(owner);
            if (ownerId == null) {
                mAppExecutors.getMainThread().execute(callback::onDataNotAvailable);
                return;
            }

            Long repoId = mRepoDao.getRepo(repo, ownerId);
            if (repoId == null) {
                mAppExecutors.getMainThread().execute(callback::onDataNotAvailable);
                return;
            }

            Cursor cursor = mCommitDao.getCommits(repoId);
            if (cursor == null) {
                mAppExecutors.getMainThread().execute(callback::onDataNotAvailable);
                return;
            }

            List<Commit> commits = new ArrayList<>();
            for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
                String sha = cursor.getString(cursor.getColumnIndex(CommitDao.COLUMN_COMMIT_SHA));
                String message = cursor.getString(cursor.getColumnIndex(CommitDao.COLUMN_COMMIT_MESSAGE));
                String date = cursor.getString(cursor.getColumnIndex(CommitDao.COLUMN_COMMIT_DATE));

                Commit commit = new Commit(sha, message, date);
                commits.add(commit);
            }

            mAppExecutors.getMainThread().execute(() -> callback.onCommitsLoaded(commits));
        };

        mAppExecutors.getDbThread().execute(runnable);
    }

    @Override
    public void insertCommitsDb(@NonNull List<Commit> commits, @NonNull String owner,
                                @NonNull String repo) {
        Runnable runnable = () -> {
            mOwnerDao.insertOwner(owner);
            Long ownerId = mOwnerDao.getOwnerId(owner);
            if (ownerId == null) {
                return;
            }

            mRepoDao.insertRepo(repo, ownerId);
            Long repoId = mRepoDao.getRepo(repo, ownerId);
            if (repoId == null) {
                return;
            }

            mCommitDao.insertCommits(commits, repoId);
        };

        mAppExecutors.getDbThread().execute(runnable);
    }

    @Override
    public void getCommitsNetwork(@NonNull String owner, @NonNull String repo,
                                  @Nullable Integer pageNumber, @Nullable Integer pageSize,
                                  @NonNull LoadCommitsCallback callback) {
        Callback<List<CommitLoad>> retrofitCallback = new Callback<List<CommitLoad>>() {
            @Override
            public void onResponse(Call<List<CommitLoad>> call, Response<List<CommitLoad>> response) {
                List<CommitLoad> commitsLoad = response.body();

                if (response.isSuccessful() && commitsLoad != null) {
                    List<Commit> commits = new ArrayList<>();

                    for (CommitLoad commitLoad : commitsLoad) {
                        Commit commit = CommitMapper.toCommit(commitLoad);
                        commits.add(commit);
                    }

                    mAppExecutors.getMainThread().execute(() -> callback.onCommitsLoaded(commits));
                } else {
                    mAppExecutors.getMainThread().execute(callback::onDataNotAvailable);
                }
            }

            @Override
            public void onFailure(Call<List<CommitLoad>> call, Throwable t) {
                mAppExecutors.getMainThread().execute(callback::onDataNotAvailable);
            }
        };

        Runnable runnable = () -> RetrofitClient.getCommits(
                owner, repo, pageNumber, pageSize, retrofitCallback);

        mAppExecutors.getNetworkThreads().execute(runnable);
    }
}
