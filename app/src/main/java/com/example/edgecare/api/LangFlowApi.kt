package com.example.edgecare.api
import com.example.edgecare.models.QueryRequest
import com.example.edgecare.models.QueryResponse
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface LangFlowApi {
    @Headers("Content-Type: application/json")
    @POST("/lf/{langflowId}/api/v1/run/{flowId}")
    fun initiateSession(
        @Path("langflowId") langflowId: String,
        @Path("flowId") flowId: String,
        @Query("stream") stream: Boolean = false,
        @Body body: QueryRequest
    ): Call<QueryResponse>
}
