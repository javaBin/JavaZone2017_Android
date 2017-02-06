package no.javazone.service;

import java.util.List;

import no.javazone.archframework.model.domain.Session;
import retrofit2.Call;
import retrofit2.http.GET;

public interface DataBootstrapApiEndpoint {

    @GET("allSessions/javazone_2016")
    Call<String> getSessionsDebug();

    @GET("allSessions/javazone_2017")
    Call<String> getSessionsRelease();
}
