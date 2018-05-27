package com.avivamiriammandel.watchme.error;

import android.support.annotation.NonNull;

import com.avivamiriammandel.watchme.model.ReviewsResponse;

import java.io.IOException;
import java.lang.annotation.Annotation;

import okhttp3.ResponseBody;
import retrofit2.Converter;
import retrofit2.Response;

import static com.avivamiriammandel.watchme.rest.Client.getClient;

/**
 * Created by aviva.miriam on 25 מרץ 2018.
 */

public class ReviewErrorUtils {
    public static ApiError parseError(@NonNull Response<ReviewsResponse> response) {
        Converter<ResponseBody, ApiError> converter =
                getClient()
                        .responseBodyConverter(ApiError.class, new Annotation[0]);
        ApiError error;
        try {
            if (null != response.errorBody()) {
                error = converter.convert(response.errorBody());
                return error;
            }
        } catch (IOException e) {
            return new ApiError();
        }
        return new ApiError();
    }
}
