package com.example.guswn.allthatlyrics.Home.Frag2_social;

import com.example.guswn.allthatlyrics.Home.Frag1_friends.FollowingResponse;
import com.example.guswn.allthatlyrics.Home.Frag4_chat.ChatResponse;

import org.json.JSONArray;

import java.util.List;
import java.util.Map;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.FieldMap;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.PartMap;

public interface SocialAPI {
    @Multipart
    @POST("Social/upload_social_img.php")
    Call<SocialUploadResponse> uploadMultiFiles(
            @Part List< MultipartBody.Part> Files,
            @Part("filter") JSONArray Filter,
            @Part("count") RequestBody file_count,
            @Part("MY_IDX") RequestBody MY_IDX,
            @Part("MY_NAME") RequestBody MY_NAME,
            @Part("time") RequestBody time ,
            @Part("content") RequestBody content
    );
    @GET("Social/getjson_socialhistory.php")
    Call<SocialUploadResponse> getSocialHistoryList();

    @FormUrlEncoded
    @POST("Social/make_liked_unliked.php")
    Call<SocialUploadResponse> make_liked_unliked(
            @FieldMap Map<String,String> fields
    );

    @FormUrlEncoded
    @POST("Social/make_marked_unmarked.php")
    Call<SocialUploadResponse> make_marked_unmarked(
            @FieldMap Map<String,String> fields
    );
    @FormUrlEncoded
    @POST("Social/delete_social_history.php")
    Call<SocialUploadResponse> delete_socialhistory(
            @FieldMap Map<String,String> fields
    );
}