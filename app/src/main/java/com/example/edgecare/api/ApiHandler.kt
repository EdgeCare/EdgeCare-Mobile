package com.example.edgecare.api

import com.example.edgecare.apiService
import com.example.edgecare.models.LoginRequest
import com.example.edgecare.models.LoginResponse
import com.example.edgecare.models.UserQuestionRequest
import com.example.edgecare.models.UserQuestionResponse

fun sendUserMessage(message: String, onResult: (UserQuestionResponse?) -> Unit) {
    val messageRequest = UserQuestionRequest(content = message)

    apiService.sendUserMessage(messageRequest).enqueue(object : retrofit2.Callback<UserQuestionResponse> {
        override fun onResponse(
            call: retrofit2.Call<UserQuestionResponse>,
            response: retrofit2.Response<UserQuestionResponse>
        ) {
            println("----------------------------response")
            println(response)
            if (response.isSuccessful) {
                println("response.isSuccessful")
            } else {
                println("response.isNotSuccessful")
            }
            response.body()?.let { onResult(it) }
        }

        override fun onFailure(call: retrofit2.Call<UserQuestionResponse>, t: Throwable) {
            println("messageRequest Failed")
            onResult(null)
        }
    })
}

fun userLogin(email: String, password: String, onResult: (success: Boolean, message: String?) -> Unit) {
    val loginRequest = LoginRequest(email, password)
    val loginCall = apiService.userLogIn(loginRequest)

    loginCall.enqueue(object : retrofit2.Callback<LoginResponse> {
        override fun onResponse(
            call: retrofit2.Call<LoginResponse>,
            response: retrofit2.Response<LoginResponse>
        ) {
            if (response.isSuccessful) {
                onResult(true, response.body()?.message)
            } else {
                onResult(false, "Login failed: ${response.errorBody()?.string()}")
            }
        }

        override fun onFailure(call: retrofit2.Call<LoginResponse>, t: Throwable) {
            onResult(false, t.message)
        }
    })
}