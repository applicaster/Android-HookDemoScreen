package com.applicaster.cleengloginplugin.models

data class Subscription(val id: String,
                   val androidProductId: String,
                   val authID: String) {


    var title: String? = null
    var description: String? = null
    var price: String? = null

}
