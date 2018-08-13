package com.applicaster.cleengloginplugin.helper

import android.content.Context
import android.os.Handler
import com.applicaster.cleengloginplugin.models.PurchaseItem
import com.applicaster.cleengloginplugin.remote.Params
import com.applicaster.cleengloginplugin.remote.WebService

class SubscriptionLoaderHelper constructor(var context: Context, var userToken: String, var authID: String, var purchaseItem: PurchaseItem, var maxTimeForRequestInSecond: Int, var interval: Int,var  callback: (WebService.Status, String?) -> Unit ) {
    private val webService = WebService()
    var handler = Handler()


    fun load(intervalInSeccond: Int = 0) {
        handler.postDelayed({
            CleengManager.subscribe(userToken, authID, purchaseItem, context) { status: WebService.Status, response: String? ->
                if (status != WebService.Status.Success) {
                    if (maxTimeForRequestInSecond > 0) {
                        maxTimeForRequestInSecond -= intervalInSeccond;
                        load(interval);
                    } else {
                        callback(status, response)
                    }
                }

            }
        }, intervalInSeccond.toLong())
    }

}