package com.applicaster.cleengloginplugin.helper

import android.content.Context
import android.os.Handler
import com.applicaster.billing.utils.PurchaseHandler
import com.applicaster.cleengloginplugin.models.Subscription
import com.applicaster.cleengloginplugin.remote.Params
import com.applicaster.cleengloginplugin.remote.WebService

class SubscriptionLoaderHelper constructor(var context: Context,var subscription: Subscription,var maxTimeForRequestInSecond: Int,var interval: Int ) {
    private val webService = WebService()
    var handler = Handler()


    fun load(intervalInSeccond: Int = 0){
        handler.postDelayed({
            subscribe(subscription, context){ status: WebService.Status, response: String? ->
                if (status != WebService.Status.Success) {
                    if(maxTimeForRequestInSecond > 0) {
                        maxTimeForRequestInSecond -= intervalInSeccond;
                        load(interval);
                    }else{
                        //could not update user subscription and x minutes is over.
                    }
                }

            }
        }, intervalInSeccond.toLong())
    }




    fun subscribe(userToken: String, subscription: Subscription, context: Context, callback: (WebService.Status, String?) -> Unit) {
        var purchaseHandler: PurchaseHandler
        //present IAP, wait for callback

        val params = Params()
        params["offerId"] = ""
        params["token"] = userToken //the empty token

        params["productId"] = subscription.androidProductId
        params["purchaseToken"] = ""
        params["packageName"] = ""
        params["orderId"] = ""
        params["purchaseState"] = ""
        params["purchaseTime"] = ""
        params["developerPayload"] = ""

        this.webService.performApiRequest(WebService.ApiRequest.SyncPurchases, params, context) { status: WebService.Status, response: String? ->
            if (status == WebService.Status.Success) {
                //do we need to add the new offer to the user object?
                //do we need to add user again/ and get it with the new offer that user have?

                //we can load the subscriptions again through CleenManager
            }
            callback(status, response)
        }
    }




}