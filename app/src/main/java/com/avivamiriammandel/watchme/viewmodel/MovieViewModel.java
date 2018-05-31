package com.avivamiriammandel.watchme.viewmodel;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.ViewModel;

import com.avivamiriammandel.watchme.database.AppDatabase;
import com.avivamiriammandel.watchme.model.Movie;



public class MovieViewModel extends ViewModel {


    private LiveData<Movie> movie;

    public MovieViewModel(AppDatabase db, int movieId) {
        movie = db.movieDao().loadMovieById(movieId);
    }

    public LiveData<Movie> getMovie() {
        return movie;
    }
    }
