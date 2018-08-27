package com.applicaster.cleengloginplugin.helper

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.util.Log
import com.applicaster.billing.v3.handlers.APIabSetupFinishedHandler
import com.applicaster.billing.v3.handlers.APQueryInventoryFinishedHandler
import com.applicaster.billing.v3.util.APBillingUtil
import com.applicaster.billing.v3.util.IabHelper
import com.applicaster.billing.v3.util.IabResult
import com.applicaster.billing.v3.util.Purchase
import com.applicaster.cleengloginplugin.models.PurchaseItem
import com.applicaster.cleengloginplugin.remote.WebService
import com.applicaster.util.StringUtil

class IAPManager(private val mContext: Context, var callback: (WebService.Status, String?) -> Unit) {

    private val TAG = APBillingUtil::class.java.simpleName
    private var mHelper: IabHelper? = null

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

    fun getInventory(productIds: List<String>?, handler: APQueryInventoryFinishedHandler) {
        mHelper?.queryInventoryAsync(true, productIds, IabHelper.QueryInventoryFinishedListener { result, inventory ->
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
        mHelper?.launchSubscriptionPurchaseFlow(mContext as Activity, productId, APBillingUtil.PURCHASE_REQUEST_CODE) { result: IabResult?, info: Purchase? ->
            if (result != null) {
                if (result.isSuccess) {
                    //purchase success need to subscribe to Cleeng
                    if (info != null && StringUtil.isNotEmpty(CleengManager.currentUser?.token)) {

                        //TODO constructor is crazy long. change to Builder pattern
                        val purchaseItem = PurchaseItem(info.token, info.sku, info.signature, info.purchaseTime, info.purchaseState, info.packageName, info.originalJson, info.orderId, info.itemType, info.developerPayload)

                        CleengManager.subscribe(CleengManager.currentUser?.token!!, authID, purchaseItem, mContext) { status, response ->
                            if (status == WebService.Status.Success) {
                                loadSubscriptions(authID)
                            } else {
                                callback(status, response)
                            }
                        }
                    }
                }
            }
        }
    }

    fun loadSubscriptions(authID: String) {

        val token = CleengManager.currentUser?.token

        if (StringUtil.isNotEmpty(token)) {

            val subscriptionHelper = SubscriptionLoaderHelper(mContext, token!!, authID, 60, 5) { status, response ->
                callback(status, response)
            }
            subscriptionHelper.load()
        }
    }

    fun handleActivityResult(requestCode: Int, resultCode: Int, data: Intent?): Boolean? {
        return mHelper?.handleActivityResult(requestCode, resultCode, data)
    }
}