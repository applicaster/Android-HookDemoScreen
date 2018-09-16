package com.applicaster.cleengloginplugin.models

import android.support.annotation.Keep
import com.applicaster.cleengloginplugin.helper.CleengUtil

@Keep
data class Offer(val offerID: String?,
            val token: String?,
            val authId: String?) {

    fun valid(): Boolean {
       return CleengUtil.isTokenValid(token);
    }

}
