package com.applicaster.cleengloginplugin.helper


import android.app.Activity
import android.content.Context
import android.content.Intent
import android.util.Log
import com.applicaster.billing.v3.handlers.APIabSetupFinishedHandler
import com.applicaster.billing.v3.handlers.APQueryInventoryFinishedHandler
import com.applicaster.billing.v3.util.*
import com.applicaster.cleengloginplugin.models.PurchaseItem
import com.applicaster.cleengloginplugin.remote.ResponseParser
import com.applicaster.cleengloginplugin.remote.WebService
import com.applicaster.util.StringUtil
import com.applicaster.util.serialization.SerializationUtils

class IAPManager(private val mContext: Context,var  callback: (WebService.Status, String?) -> Unit) {

    private val TAG = APBillingUtil::class.java.simpleName

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

    fun getInventory(productIds: List<String>, handler: APQueryInventoryFinishedHandler) {
        mHelper!!.queryInventoryAsync(true, productIds, IabHelper.QueryInventoryFinishedListener { result, inventory ->
            if (result.isFailure) {
                handler.onInventoryQueryFailed()
                Log.d(TAG, "Query Inventory Failed")
                return@QueryInventoryFinishedListener
            } else {
                if (inventory != null) {
                    var isRelatedPurchaseFound = false
                    val ownedProductsIds = inventory.allOwnedSkus
                    if (ownedProductsIds.size > 0) {
                        for (productId in ownedProductsIds) {
                            val purchase = inventory.getPurchase(productId)
                            if (purchase != null) {
                                isRelatedPurchaseFound = isRelatedPurchaseFound || handler.onUnconsumedPurchaseFound(purchase) // This will keep isRelatedPurchaseFound true after the first time it has become true
                            }
                        }
                        if (!isRelatedPurchaseFound) {
                            handler.onInventoryEmpty()
                        }
                    } else {
                        handler.onInventoryEmpty()
                    }
                }
            }
        })
    }

    fun startPurchase(productId: String, authID: String) {
        mHelper!!.launchSubscriptionPurchaseFlow(mContext as Activity, productId, APBillingUtil.PURCHASE_REQUEST_CODE, {
            result: IabResult? ,info: Purchase? ->
            if (result != null) {
                if(result.isSuccess){
                    //purchase success need to subscribe to Cleeng
                    //loadSubscription(info, authID)
                    if(info != null && StringUtil.isNotEmpty(CleengManager.currentUser?.token)) {
                        CleengManager.subscribe(CleengManager.currentUser?.token!!, authID, PurchaseItem(info.token, info.sku, info.signature, info.purchaseTime, info.purchaseState,
                                info.packageName, info.originalJson, info.orderId, info.itemType, info.developerPayload), mContext) { status: WebService.Status, response: String? ->
                            val responseParser = ResponseParser()
                            responseParser.handleLoginResponse(status, response)

                            if (responseParser.status == WebService.Status.Success) {
                                loadSubscriptions(info, authID)
                            }
                        }

                    }
                }
            }
        })
    }

    private fun loadSubscriptions(info: Purchase? , authID: String) {
        if (info != null && StringUtil.isNotEmpty(CleengManager.currentUser?.token)) {

            subscriptionHelper = SubscriptionLoaderHelper(mContext, CleengManager.currentUser?.token!!, authID, PurchaseItem(info.token, info.sku, info.signature, info.purchaseTime, info.purchaseState,
                    info.packageName, info.originalJson, info.orderId, info.itemType, info.developerPayload), 60, 5 ){ status: WebService.Status, response: String?  ->
                    callback(status, response)
            }
            subscriptionHelper?.load(0)

        }
    }

    fun handleActivityResult(requestCode: Int, resultCode: Int, data: Intent?): Boolean {
        return mHelper!!.handleActivityResult(requestCode, resultCode, data)
    }


}

