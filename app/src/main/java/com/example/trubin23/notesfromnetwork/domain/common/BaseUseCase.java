package com.example.trubin23.notesfromnetwork.domain.common;

/**
 * Created by Andrey on 02.01.2018.
 */

public abstract class BaseUseCase<Q extends BaseUseCase.RequestValues, P extends BaseUseCase.ResponseValues> {

    private Q mRequestValues;
    private UseCaseCallback<P> mUseCaseCallback;

    protected abstract void executeUseCase(Q requestValues);

    <T extends RequestValues> void setRequest(Q request) {
        mRequestValues = request;
    }

    void setUseCaseCallback(UseCaseCallback<P> useCaseCallback) {
        mUseCaseCallback = useCaseCallback;
    }

    protected UseCaseCallback<P> getUseCaseCallback(){
        return mUseCaseCallback;
    }

    void run() {
        executeUseCase(mRequestValues);
    }

    public interface RequestValues {
    }

    public interface ResponseValues {
    }

    public interface UseCaseCallback<R> {
        void onSuccess(R response);
        void onError();
    }
}
