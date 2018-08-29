package com.applicaster.cleengloginplugin.models

class User(val email: String?,
           val password: String?,
           val facebookId: String?,
           var token: String?,
           var userOffers: ArrayList<Offer>?,
           var ownedSubscriptions: ArrayList<Subscription>?)

var ownedSubscriptions: ArrayList<Subscription> = ArrayList()