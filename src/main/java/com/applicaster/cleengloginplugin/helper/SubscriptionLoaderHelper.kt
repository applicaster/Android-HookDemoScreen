package com.applicaster.cleengloginplugin.helper

import android.content.Context
import com.applicaster.cleengloginplugin.remote.Params
import com.applicaster.cleengloginplugin.remote.WebService
import com.applicaster.cleengloginplugin.views.BaseActivity
import com.applicaster.util.StringUtil
import rx.Observable
import rx.android.schedulers.AndroidSchedulers
import java.util.concurrent.TimeUnit

class SubscriptionLoaderHelper constructor(val context: Context, val userToken: String, val authID: String, val maxTimeForRequestInSecond: Long, val interval: Long, val callback: (WebService.Status, String?) -> Unit) {

    fun load() {

        showLoading()

        val params = Params()
        params["token"] = userToken
        if (StringUtil.isNotEmpty(authID)) {
            params["byAuthId"] = "1"
            params["offers"] = authID
        }

        //TODO make the network request other place than the subscribe() since it runs on UI Thread (unnecessary thread switching).
        Observable.interval(interval, TimeUnit.SECONDS)
                .takeUntil {
                    val timeElapsed = (it + 1) * interval
                    return@takeUntil timeElapsed >= maxTimeForRequestInSecond
                }
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe {

                    CleengManager.fetchAvailableSubscriptions(context, params) { status, response ->

                        //TODO also check if access is granted on the specific item user purchased
                        if (status == WebService.Status.Success) {
                            dismissLoading()
                            callback(status, response)
                        }
                    }
                }
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