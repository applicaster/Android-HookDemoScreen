package com.applicaster.cleengloginplugin.helper

import android.content.Context
import com.applicaster.atom.model.APAtomEntry
import com.applicaster.cleengloginplugin.models.PurchaseItem
import com.applicaster.cleengloginplugin.models.Subscription
import com.applicaster.cleengloginplugin.models.User
import com.applicaster.cleengloginplugin.remote.Params
import com.applicaster.cleengloginplugin.remote.ResponseParser
import com.applicaster.cleengloginplugin.remote.WebService
import com.applicaster.model.APModel
import com.applicaster.util.StringUtil

object CleengManager {

    private val webService = WebService()

    //save all availableSubscription
    val availableSubscriptions = emptyList<Subscription>().toMutableList()

    //save current user cached with the user offers.
    var currentUser: User? = null

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
                setUser(User(user.email, "", user.facebookId, responseParser.token,responseParser.offers));
            }

            callback(status, response)
        }
    }

    fun fetchAvailableSubscriptions(context: Context, params: Params?, callback: (WebService.Status, String?) -> Unit) {
        this.availableSubscriptions.clear()

        var finalParams = params ?: Params()
        if(currentUser != null && StringUtil.isNotEmpty(currentUser!!.token)) {
            finalParams["token"] = currentUser!!.token!!
        }

        this.webService.performApiRequest(WebService.ApiRequest.Subscriptions, finalParams, context) { status: WebService.Status, response: String? ->
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

    fun subscribe(userToken: String, authID: String, purchaseItem: PurchaseItem, context: Context, subscribeCallback: (WebService.Status, String?) -> Unit) {

        val params = Params()
        params["authId"] = authID
        params["token"] = userToken //the empty token

        params["productId"] = purchaseItem.sku
        params["purchaseToken"] = purchaseItem.token
        params["packageName"] = purchaseItem.packageName
        params["orderId"] = purchaseItem.orderId
        params["purchaseState"] = purchaseItem.purchaseState.toString()
        params["purchaseTime"] = purchaseItem.purchaseTime.toString()
        params["developerPayload"] = purchaseItem.developerPayload

        this.webService.performApiRequest(WebService.ApiRequest.SyncPurchases, params, context) { status: WebService.Status, response: String? ->
            if (status == WebService.Status.Success) {
                //do we need to add the new offer to the user object?
                //do we need to add user again/ and get it with the new offer that user have?

                //we can load the subscriptions again through CleenManager
            }else {
                subscribeCallback(status, response)
            }
        }
    }


    fun userHasActiveOffer(): Boolean {
        for (offer in currentUser?.userOffers ?: return false) {
            if(offer.valid()) {
                return true
            }
        }
        return false
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

    fun logout() {
        setUser(null);
    }

    /***
     * compare the item auth id with the user offers, return
     * true if user have offer.authId that equal to the item auth id.
     * otherwise return false.
     */
    fun isItemLocked(model: Any?): Boolean {
        if(model is APModel) {
            if (model.authorization_providers_ids == null || model.authorization_providers_ids.isEmpty())
                return true
            for (i in 0 until model.authorization_providers_ids.size) {
                var provider_id = model.authorization_providers_ids[i]
                var isComply = isUserOffersComply(provider_id);
                if(isComply){
                    return true;
                }
            }
        }
        return false
    }

    private fun isUserOffersComply(provider_id: String?): Boolean {
        var offers = getUser()?.userOffers;
        if(offers != null ){
            for (i in 0 until offers.size) {
                var offer = offers[i];
                if(StringUtil.isNotEmpty(offer.authId) && offer.authId.equals(provider_id)){
                    return true;
                }
            }
        }
        return false;
    }

    fun getAuthIds(playable: Any?): Array<out String>? {
        if (playable is APModel) {
            return playable.authorization_providers_ids
        } else if (playable is APAtomEntry) {
            return playable.getExtension("authorization_providers_ids", Array<String>::class.java)
        }

        return null
    }


}
