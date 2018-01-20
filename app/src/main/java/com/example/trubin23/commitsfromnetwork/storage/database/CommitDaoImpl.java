package com.example.trubin23.commitsfromnetwork.storage.database;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.example.trubin23.commitsfromnetwork.storage.model.CommitStorage;
import com.example.trubin23.commitsfromnetwork.storage.model.RepoStorage;

import java.util.List;

import static com.example.trubin23.commitsfromnetwork.storage.database.RepoDao.COLUMN_REPO_ID;
import static com.example.trubin23.commitsfromnetwork.storage.database.RepoDao.TABLE_REPO;

/**
 * Created by Andrey on 06.01.2018.
 */

public class CommitDaoImpl implements CommitDao {

    private static final String TAG = CommitDaoImpl.class.getSimpleName();

    static final String COMMIT_CREATE_TABLE = "CREATE TABLE " + TABLE_COMMIT + "("
            + COLUMN_COMMIT_SHA + " TEXT PRIMARY KEY, "
            + COLUMN_COMMIT_MESSAGE + " TEXT, "
            + COLUMN_COMMIT_DATE + " TEXT, "
            + COLUMN_COMMIT_REPO_ID + " INTEGER, "
            + "FOREIGN KEY (" + COLUMN_COMMIT_REPO_ID + ") REFERENCES " + TABLE_REPO + "(" + COLUMN_REPO_ID + "))";

    private DatabaseHelper mDbOpenHelper;

    public CommitDaoImpl(@NonNull DatabaseHelper dbOpenHelper) {
        mDbOpenHelper = dbOpenHelper;
    }

    @Override
    public void insertCommits(@NonNull List<CommitStorage> commits) {
        SQLiteDatabase db = mDbOpenHelper.getWritableDatabase();
        db.beginTransaction();
        try {
            for (CommitStorage commit : commits) {
                if (commit.getRepoId() == null) {
                    continue;
                }

                ContentValues values = new ContentValues();
                values.put(COLUMN_COMMIT_SHA, commit.getSha());
                values.put(COLUMN_COMMIT_MESSAGE, commit.getCommitDescription().getMessage());
                values.put(COLUMN_COMMIT_DATE, commit.getCommitDescription().getAuthor().getDate());
                values.put(COLUMN_COMMIT_REPO_ID, commit.getRepoId());

                db.insertWithOnConflict(TABLE_COMMIT, null, values, SQLiteDatabase.CONFLICT_IGNORE);
            }

            db.setTransactionSuccessful();
        } catch (Exception e) {
            Log.e(TAG, "void insertCommits(@NonNull List<CommitStorage> commits)", e);
        } finally {
            db.endTransaction();
        }
    }

    @Nullable
    @Override
    public Cursor getCommits(@NonNull RepoStorage repo) {
        Cursor cursor = null;

        SQLiteDatabase db = mDbOpenHelper.getReadableDatabase();
        db.beginTransaction();
        try {
            String whereClause = COLUMN_COMMIT_REPO_ID + " = ?";
            String[] whereArgs = new String[]{String.valueOf(repo.getId())};

            cursor = db.query(TABLE_COMMIT, COLUMNS_COMMIT, whereClause, whereArgs,
                    null, null, null);

            db.setTransactionSuccessful();
        } catch (Exception e) {
            Log.e(TAG, "Cursor getCommits(@NonNull RepoStorage repo)", e);
        } finally {
            db.endTransaction();
        }

        return cursor;
    }
}
