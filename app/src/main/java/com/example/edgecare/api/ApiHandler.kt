package com.example.edgecare.api

import android.content.Context
import android.widget.Toast
import com.example.edgecare.apiService
import com.example.edgecare.models.ChatNameResponse
import com.example.edgecare.models.ReportAnalysisResponse
import com.example.edgecare.models.SampleQuestionResponse
import com.example.edgecare.models.UserCreateRequest
import com.example.edgecare.models.TokenResponse
import com.example.edgecare.models.UserPersona
import com.example.edgecare.models.UserQuestionRequest
import com.example.edgecare.models.UserQuestionResponse
import com.example.edgecare.utils.AuthUtils

fun sendUserMessage( chatId: Long, message: String, healthReports:String, context: Context, onResult: (response: UserQuestionResponse?) -> Unit) {
    //TODO- Set userID and token
    val auth = AuthUtils()
    auth.isTokenValid()
    if(auth.getToken() == null){
        Toast.makeText(context, "Please Login again", Toast.LENGTH_SHORT).show()
    }
    else {
        val messageRequest = UserQuestionRequest(
            userId = auth.getToken()!!.userId.toInt(),
            chatId = chatId,
            token = auth.getToken()!!.accessToken,
            content = message,
            healthReports = healthReports
        )
        apiService.sendUserMessage(messageRequest)
            .enqueue(object : retrofit2.Callback<UserQuestionResponse> {
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
}

fun sendUserPersona(persona: String,context: Context, onResult: (response: Boolean?) -> Unit) {
    //TODO- Set and token
    val auth = AuthUtils()
    auth.isTokenValid()
    if(auth.getToken() == null){
        Toast.makeText(context, "Please Login again", Toast.LENGTH_SHORT).show()
    }
    else {
        val personaDetails = UserPersona(auth.getToken()!!.userId, persona, "Token")
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
}

fun getChatName(chatId: Long, context: Context, onResult: (response: ChatNameResponse?) -> Unit){
    val auth = AuthUtils()
    auth.isTokenValid()
    if(auth.getToken() == null){
        Toast.makeText(context, "Please Login again", Toast.LENGTH_SHORT).show()
    }
    else{
        apiService.getChatName(auth.getToken()!!.userId.toInt(),chatId,auth.getToken()!!.accessToken).enqueue(object : retrofit2.Callback<ChatNameResponse> {
            override fun onResponse(
                call: retrofit2.Call<ChatNameResponse>,
                response: retrofit2.Response<ChatNameResponse>
            ) {
                println("response = $response")
                if (response.isSuccessful) {
                    onResult(response.body())
                } else {
                    onResult(null)
                }
            }

            override fun onFailure(call: retrofit2.Call<ChatNameResponse>, t: Throwable) {
                println("persona save request Failed")
                onResult(null)
            }
        })
    }
}

fun getReportAnalysis(healthReport: String, context: Context, onResult: (response: ReportAnalysisResponse?) -> Unit){
    val auth = AuthUtils()
    auth.isTokenValid()
    if(auth.getToken() == null){
        Toast.makeText(context, "Please Login again", Toast.LENGTH_SHORT).show()
    }
    else{
        apiService.getReportAnalysis(auth.getToken()!!.userId.toInt(),healthReport,auth.getToken()!!.accessToken).enqueue(object : retrofit2.Callback<ReportAnalysisResponse> {
            override fun onResponse(
                call: retrofit2.Call<ReportAnalysisResponse>,
                response: retrofit2.Response<ReportAnalysisResponse>
            ) {
                println("response = $response")
                if (response.isSuccessful) {
                    onResult(response.body())
                } else {
                    onResult(null)
                }
            }

            override fun onFailure(call: retrofit2.Call<ReportAnalysisResponse>, t: Throwable) {
                println("Heath Report analysis failed")
                onResult(null)
            }
        })
    }
}

fun getSampleQuestions( context: Context, onResult: (response: SampleQuestionResponse?) -> Unit){
    val auth = AuthUtils()
    auth.isTokenValid()
    if(auth.getToken() == null){
        Toast.makeText(context, "Please Login again", Toast.LENGTH_SHORT).show()
    }
    else{
        apiService.getSampleQuestions(auth.getToken()!!.userId.toInt(),auth.getToken()!!.accessToken).enqueue(object : retrofit2.Callback<SampleQuestionResponse> {
            override fun onResponse(
                call: retrofit2.Call<SampleQuestionResponse>,
                response: retrofit2.Response<SampleQuestionResponse>
            ) {
                println("response = $response")
                if (response.isSuccessful) {
                    onResult(response.body())
                } else {
                    onResult(null)
                }
            }

            override fun onFailure(call: retrofit2.Call<SampleQuestionResponse>, t: Throwable) {
                println("sample question request failed")
                onResult(null)
            }
        })
    }
}

fun userLogin(email: String, password: String, onResult: (success: Boolean, message: String?,token:String?,userId:String) -> Unit) {
    val loginRequest = UserCreateRequest(email, password)
    val loginCall = apiService.userLogIn(loginRequest)

    loginCall.enqueue(object : retrofit2.Callback<TokenResponse> {
        override fun onResponse(
            call: retrofit2.Call<TokenResponse>,
            response: retrofit2.Response<TokenResponse>
        ) {
            if (response.isSuccessful) {
                response.body()
                    ?.let { onResult(true, it.message, it.token, it.userId) }
            } else {
                onResult(false, "Login failed: ${response.errorBody()?.string()}","","")
            }
        }

        override fun onFailure(call: retrofit2.Call<TokenResponse>, t: Throwable) {
            onResult(false, t.message,"","")
        }
    })
}

fun userSignUp(email: String, password: String, onResult: (success: Boolean, message: String?,userId:String?,token:String?) -> Unit) {
    val signupRequest = UserCreateRequest(email, password)
    val signupCall = apiService.userSignUp(signupRequest)

    signupCall.enqueue(object : retrofit2.Callback<TokenResponse> {
        override fun onResponse(
            call: retrofit2.Call<TokenResponse>,
            response: retrofit2.Response<TokenResponse>
        ) {
            if (response.isSuccessful) {
                onResult(true, response.body()?.message,response.body()?.userId,response.body()?.token)
            } else {
                onResult(false, response.errorBody()?.string(),"","")
            }
        }

        override fun onFailure(call: retrofit2.Call<TokenResponse>, t: Throwable) {
            onResult(false, t.message,"","")
        }
    })
}