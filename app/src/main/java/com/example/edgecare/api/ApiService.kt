package com.example.edgecare.api

import com.example.edgecare.models.ChatNameResponse
import com.example.edgecare.models.SampleQuestionResponse
import com.example.edgecare.models.UserCreateRequest
import com.example.edgecare.models.TokenResponse
import com.example.edgecare.models.UserPersona
import com.example.edgecare.models.UserQuestionRequest
import com.example.edgecare.models.UserQuestionResponse
import retrofit2.Call
import retrofit2.http.*

interface ApiService {

    @POST("users/userQuestion")
    fun sendUserMessage(@Body messageRequest: UserQuestionRequest): Call<UserQuestionResponse>

    @POST("users/userPersona")
    fun sendUserPersona(@Body userPersona: UserPersona): Call<Boolean>

    @GET("users/chatName")
    fun getChatName(
        @Query("userId") userId: Int,
        @Query("chatId") chatId: Long,
        @Query("token") token: String
    ): Call<ChatNameResponse>

    @GET("users/sampleQuestions")
    fun getSampleQuestions(
        @Query("userId") userId:Int,
        @Query("token") token: String
    ): Call<SampleQuestionResponse>

    @POST("auth/login")
    fun userLogIn(@Body loginRequest: UserCreateRequest): Call<TokenResponse>

    @POST("auth/signup")
    fun userSignUp(@Body signupRequest: UserCreateRequest) : Call<TokenResponse>

    ///// EXAMPLE USAGE /////
    // GET Request to fetch all posts
//    @GET("posts")
//    fun getPosts(): Call<List<PostResponse>>
//
//    // GET Request to fetch a specific post by ID
//    @GET("posts/{id}")
//    fun getPostById(@Path("id") id: Int): Call<PostResponse>
//
//    // POST Request to create a new post
//    @POST("posts")
//    fun createPost(@Body post: PostRequest): Call<PostResponse>
//
//    // PUT Request to update an existing post
//    @PUT("posts/{id}")
//    fun updatePost(@Path("id") id: Int, @Body post: PostRequest): Call<PostResponse>
//
//    // DELETE Request to delete a post by ID
//    @DELETE("posts/{id}")
//    fun deletePost(@Path("id") id: Int): Call<Void>
}
