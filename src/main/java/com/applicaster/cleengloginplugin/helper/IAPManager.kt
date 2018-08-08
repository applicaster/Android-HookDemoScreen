package com.applicaster.cleengloginplugin.helper


import android.app.Activity
import android.content.Context
import android.util.Log
import com.applicaster.billing.utils.PurchaseHandler
import com.applicaster.billing.v3.handlers.APIabSetupFinishedHandler
import com.applicaster.billing.v3.util.APBillingUtil
import com.applicaster.billing.v3.util.IabHelper

class IAPManager(private val mContext: Context) {

    enum class Action {
        startPurchase,
        getInventory
    }

    private var mHelper: IabHelper? = null
    var mPurchaseHandler: PurchaseHandler?  = null
    var mListener: PurchaseHandler.PurchaseHandlerI? = null

    fun init(productId: String) {
        mHelper = APBillingUtil.initBillingHelper(mContext, object : APIabSetupFinishedHandler {

            override fun onIabSetupSucceeded() {
                startPurchase(productId)
            }

            override fun onIabSetupFailed() {
                APBillingUtil.showInappBillingNotSupportedDialog(mContext)
            }
        })
    }

    fun getInventory() {
        //TODO getInventory
    }

    fun startPurchase(productId: String) {
        mHelper!!.launchSubscriptionPurchaseFlow(mContext as Activity, productId, APBillingUtil.PURCHASE_REQUEST_CODE, null, null)
    }
}

