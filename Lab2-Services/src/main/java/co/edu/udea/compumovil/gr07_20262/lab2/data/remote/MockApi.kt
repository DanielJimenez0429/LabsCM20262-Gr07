package co.edu.udea.compumovil.gr07_20262.lab2.data.remote

import com.google.gson.annotations.SerializedName
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

// ---- GET /snacks: datos que complementan al snack ----
data class SnackInfoDto(
    @SerializedName("id") val id: String = "",
    @SerializedName("name") val name: String = "",
    @SerializedName("description") val description: String? = null,
    @SerializedName("ingredients") val ingredients: String? = null,
    @SerializedName("calories") val calories: Int = 0,
    @SerializedName("rating") val rating: Double = 0.0,
)

// ---- POST /orders: pedido del checkout ----
data class OrderRequest(
    @SerializedName("items") val items: String,
    @SerializedName("itemCount") val itemCount: Int,
    @SerializedName("total") val total: Long,
    @SerializedName("status") val status: String,
)

data class OrderResponse(
    @SerializedName("id") val id: String = "",
)

interface MockApi {
    @GET("snacks")
    suspend fun getSnacks(): List<SnackInfoDto>

    @POST("orders")
    suspend fun createOrder(@Body order: OrderRequest): OrderResponse
}
object ApiClient {
    private const val BASE_URL = "https://6aff8fd47fc363ccff0941b8.mockapi.io/"

    val api: MockApi by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL.trimEnd('/') + "/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(MockApi::class.java)
    }
}