package com.applicaster.cleengloginplugin.remote

import android.content.Context
import android.util.Log
import com.applicaster.billing.v3.handlers.APIabSetupFinishedHandler
import com.applicaster.billing.v3.util.APBillingUtil
import com.applicaster.billing.v3.util.SkuDetails
import com.applicaster.cleengloginplugin.helper.CleengManager
import com.applicaster.cleengloginplugin.helper.CleengUtil
import com.applicaster.cleengloginplugin.helper.IAPManager
import com.applicaster.cleengloginplugin.models.Offer
import com.applicaster.cleengloginplugin.models.Subscription
import com.applicaster.cleengloginplugin.views.SubscriptionsActivity
import com.applicaster.util.StringUtil
import org.json.JSONArray
import org.json.JSONObject

class ResponseParser {
    var status = WebService.Status.Unknown

    var token: String? = null
    lateinit var iapManager :IAPManager
    var offers: ArrayList<Offer> = ArrayList()

    fun handleLoginResponse(status: WebService.Status, data: String?) {
        this.status = status

        if (this.status == WebService.Status.Success) {
            val json = try { JSONArray(data) } catch (e: Exception) { return }

            //save the 'empty' token.
            for (i in 0 until json.length()) {
                val jsonToken = json.getJSONObject(i)
                if (StringUtil.isEmpty(jsonToken.getString("offerId"))) {
                    this.token = jsonToken.getString("token")
                } else this.parseOngoingSubscriptions(jsonToken)
            }

        } else {
            Log.e("Error", data)
        }
    }

    fun handleAvailableSubscriptionsResponse(data: String?, context: Context) {
        val availableSubscriptions = ArrayList<Subscription>()
        val productIds = ArrayList<String>()
        val json = try { JSONArray(data) } catch (e: Exception) { return }

        for (i in 0 until json.length()) {
            var jsonSubscription = json.getJSONObject(i)

            var subscription = Subscription(
                    jsonSubscription.optString("id"),
                    jsonSubscription.optString("androidProductId"),
                    jsonSubscription.optString("authId"))

            availableSubscriptions.add(subscription)
            productIds.add(jsonSubscription.optString("androidProductId"))
            if (jsonSubscription.optBoolean("accessGranted"))
                this.parseAccessGranted(subscription)
        }

        CleengManager.availableSubscriptions = availableSubscriptions

        iapManager = IAPManager(context)
        iapManager.init(object : APIabSetupFinishedHandler {

            override fun onIabSetupSucceeded() {
                iapManager.getInventory(productIds) { isSuccess ->
                    if (isSuccess) (context as? SubscriptionsActivity)?.presentSubscriptions()
                }
            }

            override fun onIabSetupFailed() {
                APBillingUtil.showInappBillingNotSupportedDialog(context)
            }
        })

    }

    private fun parseOngoingSubscriptions(data: JSONObject) {

        with(data) {

            offers.add(Offer(
                    getString("offerId"),
                    getString("token"),
                    getString("authId")))
        }
    }

    private fun parseAccessGranted(subscription : Subscription) {
        CleengUtil.addSubscriptionToUser(subscription)
    }
}
