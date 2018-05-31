package com.avivamiriammandel.watchme.viewmodel;

import android.arch.lifecycle.ViewModel;
import android.arch.lifecycle.ViewModelProvider;

import com.avivamiriammandel.watchme.database.AppDatabase;



public class MovieViewModelFactory extends ViewModelProvider.NewInstanceFactory{
    AppDatabase db;
    int movieid;

    public MovieViewModelFactory(AppDatabase db, int movieid) {
        this.db = db;
        this.movieid = movieid;
    }


    @Override
    public <T extends ViewModel> T create(Class<T> modelClass) {
        //noinspection unchecked
        return (T) new MovieViewModel(db, movieid);
    }
}
