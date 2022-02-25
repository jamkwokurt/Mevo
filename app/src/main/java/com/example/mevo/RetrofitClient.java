package com.example.mevo;

import com.squareup.moshi.Moshi;

import retrofit2.Retrofit;
import retrofit2.converter.moshi.MoshiConverterFactory;

public class RetrofitClient {
    private static RetrofitClient instance = null;
    private MevoApi mevoApi;
    private Moshi moshi = new Moshi.Builder().build();

    private RetrofitClient() {
        Retrofit retrofit = new Retrofit.Builder().baseUrl(MevoApi.BASE_URL)
                .addConverterFactory(MoshiConverterFactory.create(moshi))
                .build();
        mevoApi = retrofit.create(MevoApi.class);
    }

    public static synchronized RetrofitClient getInstance() {
        if (instance == null) {
            instance = new RetrofitClient();
        }
        return instance;
    }

    public MevoApi getMevoApi() {
        return mevoApi;
    }
}
