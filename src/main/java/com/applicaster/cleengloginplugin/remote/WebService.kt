package com.applicaster.cleengloginplugin.remote

import android.content.Context
import android.util.Log
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.applicaster.cleengloginplugin.*
import com.applicaster.cleengloginplugin.helper.PluginConfigurationHelper
import com.applicaster.util.StringUtil
import org.json.JSONObject


typealias Params = HashMap<String, String>
typealias JsonParams = JSONObject

class WebService {

    enum class ApiRequest(val endpoint: String) {
        Login("login"),
        Register("register"),
        Subscriptions("subscriptions"),
        SyncPurchases("subscription"),
        ResetPassword("passwordReset"),
        ExtendToken("extendToken")
    }

    enum class Status {
        Unknown,
        Success,
        WrongCredentials,
        UserExists,
        InvalidParameters,
        InternalError
    }

    private val baseUrl = "https://applicaster-cleeng-sso.herokuapp.com"

    fun performApiRequest(apiRequest: ApiRequest, params: Params?, context: Context, callback: (Status, String?) -> Unit) {
        val queue = Volley.newRequestQueue(context)
        val url = this.baseUrl + "/${apiRequest.endpoint}"

        val sr = object : StringRequest(Request.Method.POST, url, Response.Listener { response ->
            callback(getStatusFromCode(200),response)
        }, Response.ErrorListener { error ->
            callback(getStatusFromCode(error.networkResponse.statusCode),String(error.networkResponse.data))
        }) {
            override fun getParams(): Params {
                val finalParams = params ?: Params()
                val publisherId = PluginConfigurationHelper.getConfigurationValue(LOGIN_PUBLISHER_ID)
                if (publisherId != null && StringUtil.isNotEmpty(publisherId))
                    finalParams["publisherId"] = publisherId
                return finalParams
            }

            override fun getBodyContentType(): String {
                return "application/x-www-form-urlencoded"
            }
        }
        queue.add(sr)
    }

    fun performApiJSONRequest(apiRequest: ApiRequest, params: JsonParams?, context: Context, callback: (Status, String?) -> Unit) {
        val queue = Volley.newRequestQueue(context)
        val url = this.baseUrl + "/${apiRequest.endpoint}"

        val finalParams = params ?: JsonParams()
        val publisherId = PluginConfigurationHelper.getConfigurationValue(LOGIN_PUBLISHER_ID)
        if (publisherId != null && StringUtil.isNotEmpty(publisherId))
            finalParams.put("publisherId", publisherId)

        val sr = JsonObjectRequest(Request.Method.POST, url, params, Response.Listener<JSONObject> {
            callback(getStatusFromCode(200), it.toString())
        }, Response.ErrorListener {
            callback(getStatusFromCode(it.networkResponse.statusCode), String(it.networkResponse.data))
        })

        queue.add(sr)
    }

    private fun getStatusFromCode(code: Int): WebService.Status {

        return when (code) {
            200 -> WebService.Status.Success
            400 -> WebService.Status.InvalidParameters
            401 -> WebService.Status.WrongCredentials
            500 -> WebService.Status.InternalError
            else -> WebService.Status.Unknown
        }
    }
}
