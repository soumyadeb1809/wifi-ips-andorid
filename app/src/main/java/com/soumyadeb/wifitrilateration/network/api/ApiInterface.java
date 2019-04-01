package com.soumyadeb.wifitrilateration.network.api;


import com.soumyadeb.wifitrilateration.network.dto.UpdateLocationResponse;

import retrofit2.Call;

import retrofit2.http.GET;

import retrofit2.http.Query;

public interface ApiInterface {

    @GET("updateNodeLocation.php")
    Call<UpdateLocationResponse> doUpdateLocation (
            @Query("ssid") String ssid,
            @Query("lat") double lat,
            @Query("lng") double lng
    );

}
