package com.applicaster.cleengloginplugin.helper

import android.content.Context
import com.applicaster.billing.utils.PurchaseHandler
import com.applicaster.cleengloginplugin.models.Subscription
import com.applicaster.cleengloginplugin.models.User
import com.applicaster.cleengloginplugin.remote.Params
import com.applicaster.cleengloginplugin.remote.ResponseParser
import com.applicaster.cleengloginplugin.remote.WebService

object CleengManager {

    private val webService = WebService()

    //save all availableSubscription
    val availableSubscriptions = emptyList<Subscription>().toMutableList()

    //save current user cached with the user offers.
    var currentUser: User? = null
    var ongoingSubscriptions = emptyList<Subscription>().toMutableList()

    fun register(user: User, context: Context, callback: (WebService.Status, String?) -> Unit) {
        if (user.email == null) {
            return
        }

        val params = Params()
        params["email"] = user.email

        if (user.facebookId != null) {
            params["facebookId"] = user.facebookId
        } else if (user.password != null) {
            params["password"] = user.password
        } else {
            return
        }

        // default values
        params["country"] = "US"
        params["locale"] = "en_US"
        params["currency"] = "USD"

        this.webService.performApiRequest(WebService.ApiRequest.Register, params, context) { status: WebService.Status, response: String? ->
            val responseParser = ResponseParser()
            responseParser.handleLoginResponse(status, response)

            if (responseParser.status == WebService.Status.Success) {
                setUser(User(user.email, "", user.facebookId, responseParser.token,responseParser.offers));
            }

            callback(status, response)
        }
    }

    fun login(user: User, context: Context, callback: (WebService.Status, String?) -> Unit) {
        if (user.email == null) {
            return
        }

        val params = Params()
        params["email"] = user.email

        if (user.facebookId != null) {
            params["facebookId"] = user.facebookId
        } else if (user.password != null) {
            params["password"] = user.password
        } else {
            return
        }

        this.webService.performApiRequest(WebService.ApiRequest.Login, params, context) { status: WebService.Status, response: String? ->
            val responseParser = ResponseParser()
            responseParser.handleLoginResponse(status, response)

            if (responseParser.status == WebService.Status.Success) {
                this.currentUser = User(user.email, "", user.facebookId, responseParser.token,responseParser.offers)
            }

            callback(status, response)
        }
    }

    fun fetchAvailableSubscriptions(context: Context, callback: (WebService.Status, String?) -> Unit) {
        this.availableSubscriptions.clear()

        this.webService.performApiRequest(WebService.ApiRequest.Subscriptions, null, context) { status: WebService.Status, response: String? ->
            val responseParser = ResponseParser()
            responseParser.handleAvailableSubscriptionsResponse(status, response)

            if (responseParser.status == WebService.Status.Success) {
                if (responseParser.availableSubscriptions != null) {
                    this.availableSubscriptions.addAll(responseParser.availableSubscriptions!!)
                }
            }

            callback(status, response)
        }
    }

    fun subscribe(subscription: Subscription, context: Context, callback: (WebService.Status, String?) -> Unit) {
        var purchaseHandler: PurchaseHandler
        //present IAP, wait for callback

        val params = Params()
        params["offerId"] = ""
        params["token"] = ""

        params["productId"] = ""
        params["purchaseToken"] = ""
        params["packageName"] = ""
        params["orderId"] = ""
        params["purchaseState"] = ""
        params["purchaseTime"] = ""
        params["developerPayload"] = ""

        this.webService.performApiRequest(WebService.ApiRequest.SyncPurchases, params, context) { status: WebService.Status, response: String? ->
            if (status == WebService.Status.Success) {
                this.handleUpdatedSubscriptionsState(listOf(subscription.id))
            }

            callback(status, response)
        }
    }

//    fun restoreSubscriptions(user: User, context: Context, callback: (WebService.Status, String?) -> Unit) {
//        this.login(user, context) { status: WebService.Status, response: String? ->
//            if (status == WebService.Status.Success) {
//                // perform restore purchases, wait for callback
//                //TODO getInventory!
//                val subscription = Subscription(
//                        "",
//                        "",
//                        0.0,
//                        "create the cleeng offer id from the known info",
//                        "product id")
//
//                this.subscribe(subscription, context, callback)
//            }
//            else {
//                callback(status, response)
//            }
//        }
//    }

    fun isOngoingSubscription(subscription: Subscription): Boolean {
        return this.ongoingSubscriptions.contains(subscription)
    }

    private fun handleUpdatedSubscriptionsState(ongoingSubscriptionIds: List<String>?) {
        this.ongoingSubscriptions.clear()

        for (subscriptionId in ongoingSubscriptionIds ?: return) {
            for (availableSubscription in this.availableSubscriptions) {
                if (availableSubscription.id == subscriptionId) {
                    this.ongoingSubscriptions.add(availableSubscription)
                    break
                }
            }
        }

        this.ongoingSubscriptions = this.ongoingSubscriptions.distinct().toMutableList()
    }

    fun userHasActiveOffer(): Boolean {
        for (offer in currentUser?.userOffers ?: return false) {
            if(offer.valid()) {
                return true;
            }
        }
        return false;
    }

    fun setUser(user: User?){
        this.currentUser = user
        CleengUtil.setUser(user)
    }

    private fun getUser(): User? {
        if( currentUser != null ) return currentUser;
        this.currentUser = CleengUtil.getUser();
        return  currentUser;
    }

    fun userHasValidToken(): Boolean {
        var user = getUser()
        if(user == null) return false
        return  CleengUtil.isTokenValid(user?.token);
    }



}
