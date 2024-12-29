package com.example.edgecare.api

import com.example.edgecare.models.PostRequest
import com.example.edgecare.models.PostResponse
import retrofit2.Call
import retrofit2.http.*

interface ApiService {

    @POST("sendMessage")
    fun sendUserMessage(@Body messageRequest: PostRequest): Call<PostResponse>


    ///// EXAMPLE USAGE /////
    // GET Request to fetch all posts
    @GET("posts")
    fun getPosts(): Call<List<PostResponse>>

    // GET Request to fetch a specific post by ID
    @GET("posts/{id}")
    fun getPostById(@Path("id") id: Int): Call<PostResponse>

    // POST Request to create a new post
    @POST("posts")
    fun createPost(@Body post: PostRequest): Call<PostResponse>

    // PUT Request to update an existing post
    @PUT("posts/{id}")
    fun updatePost(@Path("id") id: Int, @Body post: PostRequest): Call<PostResponse>

    // DELETE Request to delete a post by ID
    @DELETE("posts/{id}")
    fun deletePost(@Path("id") id: Int): Call<Void>
}
