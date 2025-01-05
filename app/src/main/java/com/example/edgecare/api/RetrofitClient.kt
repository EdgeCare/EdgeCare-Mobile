import com.example.edgecare.api.LangFlowApi
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitClient {
    private const val BASE_URL = "https://api.langflow.astra.datastax.com" // Replace with your API URL
    private const val APPLICATION_TOKEN = "AstraCS:MhIUSzxQhlrAOKOpmjyqUCoc:91f5dbeb3f66b34622a2baee48c240d64cd1ba7cb548386d24cbd5873911abf1"

    private val logger = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val client = OkHttpClient.Builder()
        .connectTimeout(300, TimeUnit.SECONDS) // Connection timeout
        .readTimeout(300, TimeUnit.SECONDS)    // Read timeout
        .writeTimeout(300, TimeUnit.SECONDS)   // Write timeout
        .addInterceptor(logger)              // Add logging interceptor
        .addInterceptor { chain ->
            val request = chain.request().newBuilder()
                .addHeader("Authorization", "Bearer $APPLICATION_TOKEN")
                .build()
            chain.proceed(request)
        }
        .addInterceptor(logger)
        .build()

    val api: LangFlowApi by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(LangFlowApi::class.java)
    }

}
