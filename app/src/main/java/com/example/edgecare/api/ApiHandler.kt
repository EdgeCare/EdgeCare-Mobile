package com.example.edgecare.api

import com.example.edgecare.apiService
import com.example.edgecare.models.UserCreateRequest
import com.example.edgecare.models.TokenResponse
import com.example.edgecare.models.UserPersona
import com.example.edgecare.models.UserQuestionRequest
import com.example.edgecare.models.UserQuestionResponse

fun sendUserMessage( chatId: Long, message: String, healthReports:String, onResult: (response: UserQuestionResponse?) -> Unit) {
    //TODO- Set userID and token

    val messageRequest = UserQuestionRequest(userId = 1, chatId = chatId, token = "Token", content = message, healthReports = healthReports)

    apiService.sendUserMessage(messageRequest).enqueue(object : retrofit2.Callback<UserQuestionResponse> {
        override fun onResponse(
            call: retrofit2.Call<UserQuestionResponse>,
            response: retrofit2.Response<UserQuestionResponse>
        ) {
            println("response = $response")
            if (response.isSuccessful) {
                onResult(response.body())
            } else {
                onResult(null)
            }
        }

        override fun onFailure(call: retrofit2.Call<UserQuestionResponse>, t: Throwable) {
            println("messageRequest Failed")
            onResult(null)
        }
    })
}

fun sendUserPersona(userId: Long, persona: String, onResult: (response: Boolean?) -> Unit) {
    //TODO- Set userID and token
    val personaDetails = UserPersona(userId,persona,"Token")
    apiService.sendUserPersona(personaDetails).enqueue(object : retrofit2.Callback<Boolean> {
        override fun onResponse(
            call: retrofit2.Call<Boolean>,
            response: retrofit2.Response<Boolean>
        ) {
            println("response = $response")
            if (response.isSuccessful) {
                onResult(response.body())
            } else {
                onResult(null)
            }
        }

        override fun onFailure(call: retrofit2.Call<Boolean>, t: Throwable) {
            println("persona save request Failed")
            onResult(null)
        }
    })
}

fun userLogin(email: String, password: String, onResult: (success: Boolean, message: String?) -> Unit) {
    val loginRequest = UserCreateRequest(email, password)
    val loginCall = apiService.userLogIn(loginRequest)

    loginCall.enqueue(object : retrofit2.Callback<TokenResponse> {
        override fun onResponse(
            call: retrofit2.Call<TokenResponse>,
            response: retrofit2.Response<TokenResponse>
        ) {
            if (response.isSuccessful) {
                onResult(true, response.body()?.message)
            } else {
                onResult(false, "Login failed: ${response.errorBody()?.string()}")
            }
        }

        override fun onFailure(call: retrofit2.Call<TokenResponse>, t: Throwable) {
            onResult(false, t.message)
        }
    })
}

fun userSignUp(email: String, password: String, onResult: (success: Boolean, message: String?) -> Unit) {
    val signupRequest = UserCreateRequest(email, password)
    val signupCall = apiService.userSignUp(signupRequest)

    signupCall.enqueue(object : retrofit2.Callback<TokenResponse> {
        override fun onResponse(
            call: retrofit2.Call<TokenResponse>,
            response: retrofit2.Response<TokenResponse>
        ) {
            if (response.isSuccessful) {
                onResult(true, response.body()?.message)
            } else {
                onResult(false, response.errorBody()?.string())
            }
        }

        override fun onFailure(call: retrofit2.Call<TokenResponse>, t: Throwable) {
            onResult(false, t.message)
        }
    })
}