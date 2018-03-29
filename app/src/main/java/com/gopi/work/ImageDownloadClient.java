package com.gopi.work;


import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Streaming;
import retrofit2.http.Url;

/**
 * Created by gopinath.a on 29/3/18.
 */

public interface ImageDownloadClient {
    @GET
    Call<ResponseBody> downloadFile(@Url String img);
}
