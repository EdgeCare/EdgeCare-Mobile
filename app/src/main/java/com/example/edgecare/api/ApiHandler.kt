package com.example.edgecare.api

import com.example.edgecare.apiService
import com.example.edgecare.models.PostRequest
import com.example.edgecare.models.PostResponse

fun sendUserMessage(message: String, onResult: (PostResponse?) -> Unit) {
    val messageRequest = PostRequest(type = "user message", body = message)

    apiService.sendUserMessage(messageRequest).enqueue(object : retrofit2.Callback<PostResponse> {
        override fun onResponse(
            call: retrofit2.Call<PostResponse>,
            response: retrofit2.Response<PostResponse>
        ) {
            if (response.isSuccessful) {
                println("response.isSuccessful")
            } else {
                println("response.isNotSuccessful")
            }
            response.body()?.let { onResult(it) }
        }

        override fun onFailure(call: retrofit2.Call<PostResponse>, t: Throwable) {
            println("messageRequest Failed")
            onResult(null)
        }
    })
}
