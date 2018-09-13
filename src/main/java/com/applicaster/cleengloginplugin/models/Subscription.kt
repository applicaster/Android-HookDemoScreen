package com.applicaster.cleengloginplugin.models

import android.support.annotation.Keep

@Keep
data class Subscription(val id: String,
                   val androidProductId: String,
                   val authID: String) {


    var title: String? = null
    var description: String? = null
    var price: String? = null

}
