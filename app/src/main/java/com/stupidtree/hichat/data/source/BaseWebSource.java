package com.stupidtree.hichat.data.source;

import retrofit2.Retrofit;

public abstract class BaseWebSource<S> {
    Retrofit retrofit;
    protected S service;

    public BaseWebSource(Retrofit retrofit) {
        this.retrofit = retrofit;
        service = retrofit.create(getServiceClass());
    }

    protected abstract Class<S> getServiceClass();
}
