package com.example.trubin23.commitsfromnetwork.storage.network;

import android.support.annotation.NonNull;
import com.example.trubin23.commitsfromnetwork.storage.model.CommitStorage;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import retrofit2.Callback;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.moshi.MoshiConverterFactory;

import java.util.List;

/**
 * Created by Andrey on 31.12.2017.
 */

public class RetrofitClient {

    private static final String TAG = RetrofitClient.class.getSimpleName();
    private static final String BASE_URL = "https://api.github.com/";

    private static SOService mSOService = null;

    @NonNull
    private static SOService getSOService() {
        if(mSOService == null) {
            OkHttpClient httpClient = new OkHttpClient().newBuilder().addInterceptor(
                    chain -> {
                        Request request = chain.request();
                        Request newRequest = request.newBuilder()
                                .addHeader("User-Agent", "test-agent")
                                .build();

                        return chain.proceed(newRequest);
                    }
            ).build();

            Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .client(httpClient)
                    .addConverterFactory(MoshiConverterFactory.create())
                    .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                    .build();

            mSOService = retrofit.create(SOService.class);
        }
        return mSOService;
    }

    public static void getCommits(@NonNull String repoName, @NonNull Callback<List<CommitStorage>> callback) {
        SOService soService = getSOService();
        soService.getCommits(repoName).enqueue(callback);
    }
}