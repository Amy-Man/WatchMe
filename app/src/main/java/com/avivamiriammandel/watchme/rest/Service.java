package com.avivamiriammandel.watchme.rest;

import com.avivamiriammandel.watchme.model.MoviesResponse;
import com.avivamiriammandel.watchme.model.TrailersResponse;
import com.avivamiriammandel.watchme.model.ReviewsResponse;


import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface Service {
    @GET("movie/popular")
            Call<MoviesResponse>getPopularMovies
            (@Query("api_key") String apiKey);

    @GET("movie/top_rated")
            Call<MoviesResponse>getTopRatedMovies
            (@Query("api_key") String apiKey);
    @GET("movie/{movie_id}/videos")
            Call<TrailersResponse>getMovieTrailers
            (@Path("movie_id") int movieId,
             @Query("api_key") String apiKey);

    @GET("movie/{movie_id}/reviews")
            Call<ReviewsResponse>getMovieReviews
            (@Path("movie_id") int movieId,
             @Query("api_key") String apiKey);

}
