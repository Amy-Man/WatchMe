package com.avivamiriammandel.watchme.rest;

import com.avivamiriammandel.watchme.model.MoviesResponse;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface Service {
    @GET("movie/popular")
            Call<MoviesResponse>getPopularMovies
            (@Query("api_key") String apiKey);

    @GET("movie/topRated")
            Call<MoviesResponse>getTopRatedMovies
            (@Query("api_key") String apiKey);
}
