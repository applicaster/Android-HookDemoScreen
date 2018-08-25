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

    val baseUrl = "https://applicaster-cleeng-sso.herokuapp.com"

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

        val sr = object : JsonObjectRequest(Request.Method.POST, url, params, Response.Listener<JSONObject> { response ->
            callback(getStatusFromCode(200),response.toString())
        }, Response.ErrorListener { error ->
            callback(getStatusFromCode(error.networkResponse.statusCode),String(error.networkResponse.data))
        }) {

            override fun getBodyContentType(): String {
                return "application/x-www-form-urlencoded"
            }
        }
        queue.add(sr)
    }


    private fun getStatusFromCode(code: Int): WebService.Status {
        var result = WebService.Status.Unknown

        if (code == 200) {
            result = WebService.Status.Success
        } else if (code == 400) {
            result = WebService.Status.InvalidParameters
        } else if (code == 401) {
            result = WebService.Status.WrongCredentials
        } else if (code == 500) {
            result = WebService.Status.InternalError
        }
        return result
    }
}
