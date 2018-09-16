package com.applicaster.cleengloginplugin.models

import android.support.annotation.Keep

@Keep
data class User(val email: String?,
           val password: String?,
           val facebookId: String?,
           var token: String?,
           var userOffers: ArrayList<Offer> = arrayListOf(),
           var ownedSubscriptions: ArrayList<Subscription> = arrayListOf())

var ownedSubscriptions: ArrayList<Subscription> = ArrayList()