package com.yesserly.wordly.models.retrofit;

import com.google.gson.JsonElement;
import com.yesserly.wordly.models.pojo.definitions.WordDefinition;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;

public interface WordService {

    @GET("{word}")
    Call<WordDefinition[]> getDefinition(@Path("word") String word);

}
