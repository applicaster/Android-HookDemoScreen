package com.applicaster.cleengloginplugin.remote

import android.util.Log
import com.applicaster.cleengloginplugin.models.Offer
import com.applicaster.cleengloginplugin.models.Subscription
import com.applicaster.util.StringUtil
import org.json.JSONArray
import org.json.JSONObject


class ResponseParser {
    var status = WebService.Status.Unknown

    var token: String? = null
    var availableSubscriptions: ArrayList<Subscription>? = null
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

    fun handleAvailableSubscriptionsResponse(status: WebService.Status, data: String?) {
        this.status = status

        if (this.status == WebService.Status.Success) {
            this.parseAvailableSubscriptions(data)
        }
    }

    private fun parseOngoingSubscriptions(data: JSONObject) {
        var offerId: String = data.getString("offerId")
        var token: String =  data.getString("token")
        var authId: String = data.getString("authId")
        offers.add(Offer(offerId,token,authId))
    }

    private fun parseAvailableSubscriptions(data: String?) {
        val availableSubscriptions = ArrayList<Subscription>()

        val json = try { JSONArray(data) } catch (e: Exception) { return }

        for (i in 0 until json.length()) {
            var jsonSubscription = json.getJSONObject(i)

            availableSubscriptions.add(Subscription(
                    jsonSubscription.optString("title"),
                    jsonSubscription.optString("description"),
                    jsonSubscription.optDouble("price"),
                    jsonSubscription.optString("id"),
                    jsonSubscription.optString("androidProductId"),
                    jsonSubscription.optString("authId")
            ))

        }

        this.availableSubscriptions = availableSubscriptions
    }
}
