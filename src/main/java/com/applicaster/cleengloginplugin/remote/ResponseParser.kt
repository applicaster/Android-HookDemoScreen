package com.applicaster.cleengloginplugin.remote

import android.util.Log
import com.applicaster.cleengloginplugin.models.Subscription
import com.applicaster.util.StringUtil
import org.json.JSONArray
import org.json.JSONObject


class ResponseParser {
    var status = WebService.Status.Unknown

    var token: String? = null
    var jwts: ArrayList<String>? = null
    var availableSubscriptions: ArrayList<Subscription>? = null
    var ongoingSubscriptionIds: ArrayList<String>? = null

    fun handleLoginResponse(status: WebService.Status, data: String?) {
        this.status = status

        if (this.status == WebService.Status.Success) {
            val json = try { JSONArray(data) } catch (e: Exception) { return }

            //save the 'empty' token.
            for (i in 0 until json.length()) {
                val jsonToken = json.getJSONObject(i)
                if (StringUtil.isEmpty(jsonToken.getString("offerId"))) {
                    this.token = jsonToken.getString("token")
                } else this.parseOngoingSubscriptions(jsonToken.getString("offerId"))
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

    private fun parseOngoingSubscriptions(data: String?) {
        var parseData = try { JSONObject(data) } catch (e: Exception) { return }

        val ongoingSubscriptionIds = ArrayList<String>()
        ongoingSubscriptionIds.add(parseData.getString("offerId"))

        val jwts = ArrayList<String>()
        jwts.add(parseData.getString("token"))

        this.ongoingSubscriptionIds = ongoingSubscriptionIds
        this.jwts = jwts
    }

    private fun parseAvailableSubscriptions(data: String?) {
        val availableSubscriptions = ArrayList<Subscription>()

        val json = try { JSONArray(data) } catch (e: Exception) { return }

        for (i in 0 until json.length()) {
            val jsonSubscription = json.getJSONObject(i)

            availableSubscriptions.add(Subscription(
                    jsonSubscription.optString("title"),
                    jsonSubscription.optString("description"),
                    jsonSubscription.optDouble("price"),
                    jsonSubscription.optString("id"),
                    jsonSubscription.optString("androidProductId")))
        }

        this.availableSubscriptions = availableSubscriptions
    }
}
