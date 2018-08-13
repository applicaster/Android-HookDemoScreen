package com.applicaster.cleengloginplugin.helper

import android.content.Context
import android.os.Handler
import com.applicaster.cleengloginplugin.models.PurchaseItem
import com.applicaster.cleengloginplugin.remote.Params
import com.applicaster.cleengloginplugin.remote.WebService

class SubscriptionLoaderHelper constructor(var context: Context, var userToken: String, var authID: String, var purchaseItem: PurchaseItem, var maxTimeForRequestInSecond: Int, var interval: Int,var  callback: (WebService.Status, String?) -> Unit ) {
    private val webService = WebService()
    var handler = Handler()


    fun load(intervalInSeccond: Int = 0){
        handler.postDelayed({
            subscribe(userToken, authID,  purchaseItem, context){ status: WebService.Status, response: String? ->
                if (status != WebService.Status.Success) {
                    if(maxTimeForRequestInSecond > 0) {
                        maxTimeForRequestInSecond -= intervalInSeccond;
                        load(interval);
                    }else{
                        callback(status, response)
                    }
                }

            }
        }, intervalInSeccond.toLong())
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




}