package com.avivamiriammandel.watchme.error;

import com.avivamiriammandel.watchme.model.MoviesResponse;
import com.avivamiriammandel.watchme.rest.Client;


import java.io.IOException;
import java.lang.annotation.Annotation;

import okhttp3.ResponseBody;
import retrofit2.Converter;
import retrofit2.Response;

import static com.avivamiriammandel.watchme.rest.Client.getClient;

/**
 * Created by aviva.miriam on 25 מרץ 2018.
 */

public class ErrorUtils {
    public static ApiError parseError(Response<MoviesResponse> response) {
        Converter<ResponseBody, ApiError> converter =
                getClient()
                        .responseBodyConverter(ApiError.class, new Annotation[0]);
        ApiError error;
        try {
            error = converter.convert(response.errorBody());
        } catch (IOException e) {
            return new ApiError();
        }
        return error;
    }
}
