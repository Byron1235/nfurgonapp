package com.example.nfurgonapp.Remote

import io.reactivex.rxjava3.core.Observable
import retrofit2.http.GET
import retrofit2.http.Query

interface IGoogleAPI {
    @GET(value = "maps/api/directions/json")
    fun getDirections(
        @Query(value = "mode")mode:String?,
        @Query(value = "transit_routing_preference")transit_routing:String?,
        @Query(value = "origin")from:String?,
        @Query(value = "destination")to:String?,
        @Query(value = "key")key:String?,
        ):Observable<String>?
}