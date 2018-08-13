package com.applicaster.cleengloginplugin.models

import com.applicaster.cleengloginplugin.helper.CleengUtil


class Offer(val offerID: String?,
            val token: String?,
            val authId: String?) {

    fun valid(): Boolean {
       return CleengUtil.isTokenValid(token);
    }

}
