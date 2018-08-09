package com.applicaster.cleengloginplugin.models

import android.util.Base64
import com.applicaster.cleengloginplugin.helper.CleengUtil
import java.io.UnsupportedEncodingException
import java.nio.charset.Charset
import org.json.JSONObject


class Offer(val offerID: String?,
            val token: String?,
            val authId: String?) {

    fun valid(): Boolean {
       return CleengUtil.isTokenValid(token);
    }

}
