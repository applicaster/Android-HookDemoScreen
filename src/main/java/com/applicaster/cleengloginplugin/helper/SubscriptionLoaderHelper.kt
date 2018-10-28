package com.applicaster.cleengloginplugin.helper

import android.content.Context
import android.util.Log
import com.applicaster.authprovider.AuthenticationProviderUtil
import com.applicaster.cleengloginplugin.models.Offer
import com.applicaster.cleengloginplugin.remote.Params
import com.applicaster.cleengloginplugin.remote.WebService
import com.applicaster.cleengloginplugin.views.BaseActivity
import org.json.JSONArray
import org.json.JSONException
import rx.Observable
import rx.android.schedulers.AndroidSchedulers
import java.util.concurrent.TimeUnit

class SubscriptionLoaderHelper constructor(val context: Context, val productId: String, val userToken: String, private val itemID: String, private val isAuthId: Boolean, val maxTimeForRequestInSecond: Long, val interval: Long, val callback: (Boolean) -> Unit) {

    lateinit var offerId: String
    fun load() {

        showLoading()

        val params = Params()
        params["token"] = userToken
        if (isAuthId) {
            params["byAuthId"] = "1"
        }
        params["offers"] = itemID

        Observable.interval(0, interval, TimeUnit.SECONDS)
                .take((maxTimeForRequestInSecond / interval).toInt())
                .concatMap {

                    Observable.create(Observable.OnSubscribe<Boolean> { subscriber ->

                        CleengManager.fetchAvailableSubscriptions(context, params) { status, response ->

                            if (status == WebService.Status.Success && isPurchaseSucceed(response, productId)) {
                                subscriber.onNext(true)
                            } else {
                                // Sending false will continue the loop (@takeUntil operator)
                                subscriber.onNext(false)
                            }

                            subscriber.onCompleted()
                        }
                    })
                }
                .takeUntil {
                    return@takeUntil it
                }
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ purchaseSuccess ->

                    if (purchaseSuccess) {
                        addApplicasterToken(offerId, callback)
                    }
                }, {
                    dismissLoading()
                }, {
                    dismissLoading()
                })
    }

    private fun addApplicasterToken(offerId: String, callback: (Boolean) -> Unit) {
        val user = CleengUtil.getUser() ?: return

        CleengManager.extendToken(user, context) { response ->

            try {
                val json = JSONArray(response)

                for (i in 0 until json.length()) {
                    val jsonOffers = json.getJSONObject(i)
                    if (offerId == jsonOffers.optString("offerId")) {
                        AuthenticationProviderUtil.addToken(itemID, jsonOffers.optString("token"))
                        user.userOffers.add(Offer(offerId, jsonOffers.optString("token"), itemID))
                    }
                }

                CleengManager.setUser(user)
                callback(true)

            } catch (e: JSONException) {
                callback(false)
                Log.d("SubscriptionLoader", e.toString())
            }
        }
    }

    private fun isPurchaseSucceed(response: String?, productId: String): Boolean {
        try {
            val json = JSONArray(response)

            for (i in 0 until json.length()) {
                val jsonSubscription = json.getJSONObject(i)
                if (jsonSubscription.optString("androidProductId") == productId) {
                    offerId = jsonSubscription.optString("id")
                    return jsonSubscription.optBoolean("accessGranted")
                }
            }

        } catch (e: JSONException) {
            Log.d("SubscriptionLoader", e.toString())
        }

        return false
    }

    private fun dismissLoading() {

        if (context is BaseActivity) {
            context.dismissLoading()
        }
    }

    private fun showLoading() {

        if (context is BaseActivity) {
            context.showLoading()
        }
    }
}