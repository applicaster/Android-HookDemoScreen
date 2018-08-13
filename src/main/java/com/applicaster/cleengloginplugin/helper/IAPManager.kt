package com.applicaster.cleengloginplugin.helper


import android.app.Activity
import android.content.Context
import android.content.Intent
import android.util.Log
import com.applicaster.billing.v3.handlers.APIabSetupFinishedHandler
import com.applicaster.billing.v3.util.APBillingUtil
import com.applicaster.billing.v3.util.IabHelper
import com.applicaster.billing.v3.util.IabResult
import com.applicaster.billing.v3.util.Purchase
import com.applicaster.cleengloginplugin.models.PurchaseItem
import com.applicaster.cleengloginplugin.remote.WebService
import com.applicaster.util.StringUtil
import com.applicaster.util.serialization.SerializationUtils

class IAPManager(private val mContext: Context,var  callback: (WebService.Status, String?) -> Unit) {

    val USER_CANCELED = -1005;

    enum class Action {
        startPurchase,
        getInventory
    }

    private var mHelper: IabHelper? = null
    private var subscriptionHelper: SubscriptionLoaderHelper? = null;

    fun init(productId: String, authID: String) {
        mHelper = APBillingUtil.initBillingHelper(mContext, object : APIabSetupFinishedHandler {

            override fun onIabSetupSucceeded() {
                startPurchase(productId, authID)
            }

            override fun onIabSetupFailed() {
                APBillingUtil.showInappBillingNotSupportedDialog(mContext)
            }
        })
    }

    fun getInventory() {
        //TODO getInventory
    }

    fun startPurchase(productId: String, authID: String) {
        mHelper!!.launchSubscriptionPurchaseFlow(mContext as Activity, productId, APBillingUtil.PURCHASE_REQUEST_CODE, {
            result: IabResult? ,info: Purchase? ->
            if (result != null) {
                if(result.isSuccess){
                    //purchase success need to subscribe to Cleeng
                    loadSubscription(info, authID)
                }
            }
        },"")
    }

    private fun loadSubscription(info: Purchase? , authID: String) {
        if (info != null && StringUtil.isNotEmpty(CleengManager.currentUser?.token)) {

            subscriptionHelper = SubscriptionLoaderHelper(mContext, CleengManager.currentUser?.token!!, authID, PurchaseItem(info.token, info.sku, info.signature, info.purchaseTime, info.purchaseState,
                    info.packageName, info.originalJson, info.orderId, info.itemType, info.developerPayload), 60, 5 ){ status: WebService.Status, response: String?  ->
                    callback(status, response)
            }
            subscriptionHelper?.load(0);

        }
    }

    fun handleActivityResult(requestCode: Int, resultCode: Int, data: Intent?): Boolean {
        return mHelper!!.handleActivityResult(requestCode, resultCode, data)
    }


}

