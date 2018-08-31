package com.applicaster.cleengloginplugin.helper

import android.content.Context
import com.applicaster.app.APProperties
import com.applicaster.atom.model.APAtomEntry
import com.applicaster.cleengloginplugin.models.PurchaseItem
import com.applicaster.cleengloginplugin.models.Subscription
import com.applicaster.cleengloginplugin.models.User
import com.applicaster.cleengloginplugin.remote.JsonParams
import com.applicaster.cleengloginplugin.remote.Params
import com.applicaster.cleengloginplugin.remote.ResponseParser
import com.applicaster.cleengloginplugin.remote.WebService
import com.applicaster.loader.json.APVodItemLoader
import com.applicaster.model.APModel
import com.applicaster.model.APVodItem
import com.applicaster.util.AppData
import com.applicaster.util.StringUtil
import com.applicaster.util.asynctask.AsyncTaskListener

object CleengManager {

    private val webService = WebService()

    //save all availableSubscription
    var availableSubscriptions = emptyList<Subscription>().toMutableList()
    var purchasedItems = emptyMap<String, PurchaseItem>().toMutableMap()

    private lateinit var itemLoader : APVodItemLoader

    //save current user cached with the user offers.
    var currentUser: User? = null

    fun register(user: User, context: Context, callback: (WebService.Status, String?) -> Unit) {
        if (user.email == null) {
            //TODO handle in a better way!
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
                setUser(User(user.email, "", user.facebookId, responseParser.token, responseParser.offers, null))
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
                setUser(User(user.email, "", user.facebookId, responseParser.token, responseParser.offers, null))
            }

            callback(status, response)
        }
    }

    fun fetchAvailableSubscriptions(context: Context, params: Params?, callback: (WebService.Status, String?) -> Unit) {
        this.availableSubscriptions.clear()

        val finalParams = params ?: Params()
        if(currentUser != null && StringUtil.isNotEmpty(currentUser?.token)) {
            finalParams["token"] = currentUser?.token!!
        }

        this.webService.performApiRequest(WebService.ApiRequest.Subscriptions, finalParams, context) { status, response ->
            callback(status, response)
        }
    }

    fun parseAvailableSubscriptions(status: WebService.Status, response: String?, context: Context) {

        val responseParser = ResponseParser()
        responseParser.handleAvailableSubscriptionsResponse(status, response, context)
    }

    fun subscribe(userToken: String, authID: String, purchaseItem: PurchaseItem, context: Context, subscribeCallback: (WebService.Status, String?) -> Unit) {
        val params = JsonParams()
        params.put("authId" , authID)
        params.put("token" , userToken) //the empty token

        val receipt = JsonParams()
        receipt.put("productId", purchaseItem.sku)
        receipt.put("purchaseToken", purchaseItem.token)
        receipt.put("packageName", purchaseItem.packageName)
        receipt.put("orderId", purchaseItem.orderId)
        receipt.put("purchaseState", purchaseItem.purchaseState.toString())
        receipt.put("purchaseTime", purchaseItem.purchaseTime.toString())
        receipt.put("developerPayload", purchaseItem.developerPayload)

        params.put("receipt" , receipt)

        this.webService.performApiJSONRequest(WebService.ApiRequest.SyncPurchases, params, context) { status: WebService.Status, response: String? ->
            if (status == WebService.Status.Success) {
                subscribeCallback(status, response)
            }
        }
    }

    fun resetPassword(email: String, context: Context, callback: (WebService.Status, String?) -> Unit) {
        val params = Params()
        params["email"] = email

        this.webService.performApiRequest(WebService.ApiRequest.ResetPassword, params, context) { status: WebService.Status, response: String? ->
            if (status == WebService.Status.Success) {
                callback(status, response)
            }
        }
    }

    fun extendToken(user: User, context: Context, callback: (WebService.Status, String?) -> Unit) {
        val params = Params()
        if(StringUtil.isNotEmpty(currentUser?.token))
            params["token"] = user.token!!

        this.webService.performApiRequest(WebService.ApiRequest.ExtendToken, params, context) { status: WebService.Status, response: String? ->
            if (status == WebService.Status.Success) {
                callback(status, response)
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
        if( currentUser != null ) return currentUser
        this.currentUser = CleengUtil.getUser()
        return currentUser
    }

    fun userHasValidToken(): Boolean {
        var user: User? = getUser() ?: return false
        return CleengUtil.isTokenValid(user?.token)
    }

    fun logout() {
        setUser(null)
    }

    /***
     * compare the item auth id with the user offers, return
     * true if user have offer.authId that equal to the item auth id.
     * otherwise return false.
     */
    fun isItemLocked(model: Any?): Boolean {

        // If model is not an APModel, item is not locked by default
        if (model !is APModel)
            return false

        if (model.authorization_providers_ids == null || model.authorization_providers_ids.isEmpty())
            return false

            for (i in 0 until model.authorization_providers_ids.size) {
            val providerId = model.authorization_providers_ids[i]
            if (isUserOffersComply(providerId)) {
                return false
            }
        }

        return true
    }

    fun isItemLocked(model: Any?, callback: (Boolean) -> Unit) {
        if (model is APModel)
            callback(this.isItemLocked(model))
        else if (model is String) {
            itemLoader = APVodItemLoader(object : AsyncTaskListener<APVodItem> {

                override fun handleException(e: Exception) {
                    callback(false)
                }
                override fun onTaskStart() {

                }

                override fun onTaskComplete(result: APVodItem) {
                    callback(isItemLocked(itemLoader.bean))
                }
            }, model, AppData.getProperty(APProperties.ACCOUNT_ID_KEY), AppData.getProperty(APProperties.BROADCASTER_ID_KEY))
            itemLoader.loadBean()
        }

    }

    private fun isUserOffersComply(provider_id: String?): Boolean {
        var offers = getUser()?.userOffers
        if(offers != null && offers.isNotEmpty()){
            for (i in 0 until offers.size) {
                var offer = offers[i]
                if(StringUtil.isNotEmpty(offer.authId) && offer.authId.equals(provider_id)){
                    return true
                }
            }
        }
        return false
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
