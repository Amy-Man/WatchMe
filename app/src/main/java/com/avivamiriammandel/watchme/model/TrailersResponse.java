package com.avivamiriammandel.watchme.model;

import android.os.Parcel;
import android.os.Parcelable;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class TrailersResponse implements Serializable, Parcelable {

    @SerializedName("id")
    @Expose
    private Integer id;
    @SerializedName("results")
    @Expose
    private List<Trailer> results = null;
    private final static long serialVersionUID = -8596847499304344072L;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public List<Trailer> getResults() {
        return results;
    }

    public void setResults(List<Trailer> results) {
        this.results = results;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeValue(this.id);
        dest.writeList(this.results);
    }

    public TrailersResponse() {
    }

    protected TrailersResponse(Parcel in) {
        this.id = (Integer) in.readValue(Integer.class.getClassLoader());
        this.results = new ArrayList<Trailer>();
        in.readList(this.results, Trailer.class.getClassLoader());
    }

    public static final Parcelable.Creator<TrailersResponse> CREATOR = new Parcelable.Creator<TrailersResponse>() {
        @Override
        public TrailersResponse createFromParcel(Parcel source) {
            return new TrailersResponse(source);
        }

        @Override
        public TrailersResponse[] newArray(int size) {
            return new TrailersResponse[size];
        }
    };
}
