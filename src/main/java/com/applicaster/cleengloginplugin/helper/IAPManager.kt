package com.applicaster.cleengloginplugin.helper

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.Toast
import com.applicaster.billing.v3.handlers.APIabSetupFinishedHandler
import com.applicaster.billing.v3.util.*
import com.applicaster.cleengloginplugin.ITEM_TYPE_INAPP
import com.applicaster.cleengloginplugin.models.PurchaseItem
import com.applicaster.cleengloginplugin.remote.WebService
import com.applicaster.plugin_manager.login.LoginManager
import com.applicaster.util.StringUtil
import kotlin.collections.HashMap

class IAPManager(private val mContext: Context) {

    private val TAG = APBillingUtil::class.java.simpleName
    private var mHelper: IabHelper? = null

    fun init(setupFinishedHandler: APIabSetupFinishedHandler) {
        mHelper = APBillingUtil.initBillingHelper(mContext, setupFinishedHandler)
    }

    fun getInventory(productIds: List<String>?, handler: (Boolean) -> Unit) {
        mHelper?.queryInventoryAsync(true, productIds) { result, inventory ->
            if (result.isFailure) {
                handler(false)
                Log.d(TAG, "Query Inventory Failed")
            } else {
                if (inventory != null) {
                    val allProductsIds = inventory.allSkus
                    if (allProductsIds.size > 0) {

                        val items = HashMap<String, SkuDetails>()

                        for (productId in allProductsIds) {
                            val product = inventory.getSkuDetails(productId)
                            if (product != null) {
                                items[productId] = product
                            }
                        }

                        for (subscription in CleengManager.availableSubscriptions)   {
                            val item = items[subscription.androidProductId]
                            subscription.description = item?.description
                            subscription.price = item?.price
                            subscription.title = item?.title
                            subscription.type = item?.type
                        }

                    }
                    val purchasedProductsIds = inventory.allOwnedSkus
                    if (purchasedProductsIds.size > 0) {
                        for (productId in purchasedProductsIds) {
                            val purchase = inventory.getPurchase(productId)
                            if (purchase != null) {
                                val purchaseItem = PurchaseItem.Builder().build(purchase)
                                CleengManager.purchasedItems[productId] = purchaseItem
                            }
                        }
                    }
                    handler(true)
                }
                handler(false)
            }
        }
    }

    fun startPurchase(productId: String, itemId: String, isAuthId: Boolean, itemType: String?, callback: (Boolean) -> Unit) {
        if (StringUtil.isNotEmpty(itemType) && ITEM_TYPE_INAPP == itemType) {
            mHelper?.launchPurchaseFlow(mContext as Activity, productId, APBillingUtil.PURCHASE_REQUEST_CODE) { result, info ->
                if (result.isSuccess) {
                    continuePurchaseFlow(info, productId, itemId, isAuthId)
                }
                callback(result.isSuccess)
            }
        } else {
            mHelper?.launchSubscriptionPurchaseFlow(mContext as Activity, productId, APBillingUtil.PURCHASE_REQUEST_CODE) { result, info ->
                if (result.isSuccess) {
                    continuePurchaseFlow(info, productId, itemId, isAuthId)
                } else {
                    Toast.makeText(mContext, result.message, Toast.LENGTH_LONG).show()
                }
            }
        }

    }

    private fun continuePurchaseFlow(info: Purchase?, productId: String, itemId: String, isAuthId: Boolean) {
        //purchase success need to subscribe to Cleeng
        if (info != null && StringUtil.isNotEmpty(CleengManager.currentUser?.token)) {

            val purchaseItem = PurchaseItem.Builder().build(info)
            CleengManager.purchasedItems[info.sku] = purchaseItem
            CleengManager.subscribe(CleengManager.currentUser?.token!!, itemId, purchaseItem, mContext, isAuthId) { status, response ->
                if (status == WebService.Status.Success) {
                    loadSubscriptions(itemId, productId, isAuthId)
                }
            }
        }
    }

    private fun loadSubscriptions(itemId: String, productId: String, isAuthId: Boolean) {

        val token = CleengManager.currentUser?.token

        if (StringUtil.isNotEmpty(token)) {
            val subscriptionHelper = SubscriptionLoaderHelper(mContext, productId, token!!, itemId, isAuthId,60, 5) { isSuccess ->

                if ((isSuccess && mContext is Activity)) {
                    LoginManager.notifyEvent(mContext, LoginManager.RequestType.LOGIN, true)
                    mContext.finish()
                }
            }
            subscriptionHelper.load()
        }
    }

    fun handleActivityResult(requestCode: Int, resultCode: Int, data: Intent?): Boolean? {
        return mHelper?.handleActivityResult(requestCode, resultCode, data)
    }
}