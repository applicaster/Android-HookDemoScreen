package com.applicaster.cleengloginplugin.helper

import android.content.Context
import android.util.Log
import com.applicaster.cleengloginplugin.remote.Params
import com.applicaster.cleengloginplugin.remote.WebService
import com.applicaster.util.StringUtil
import rx.Observable
import java.util.concurrent.TimeUnit

class SubscriptionLoaderHelper constructor(val context: Context, val userToken: String, val authID: String, val maxTimeForRequestInSecond: Long, val interval: Long, val callback: (WebService.Status, String?) -> Unit) {

    fun load() {

        val params = Params()
        params["token"] = userToken
        if (StringUtil.isNotEmpty(authID)) {
            params["byAuthId"] = "1"
            params["offers"] = authID
        }

        Observable.interval(interval, TimeUnit.SECONDS)
                .takeUntil {
                    val timeElapsed = (it + 1) * interval
                    return@takeUntil timeElapsed >= maxTimeForRequestInSecond
                }.doOnCompleted {
                    Log.d("TK", "DONE")
                }.subscribe {
                    CleengManager.fetchAvailableSubscriptions(context, params) { status: WebService.Status, response: String? ->
                        //TODO add loop exit conditions when access is granted
                        callback(status, response)
                    }
                }
    }
}