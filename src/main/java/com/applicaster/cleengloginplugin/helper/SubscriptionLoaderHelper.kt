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

        Observable.interval(interval, TimeUnit.SECONDS)
                .take((maxTimeForRequestInSecond / interval).toInt())
                .flatMap {

                    Observable.create(Observable.OnSubscribe<Boolean> { subscriber ->

                        CleengManager.fetchAvailableSubscriptions(context, params) { status, response ->

                            //TODO also check if access is granted on the specific item user purchased
                            if (status == WebService.Status.Success) {
                                callback(status, response)
                                subscriber.onNext(true)
                            } else {
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
                .doOnCompleted {
                    dismissLoading()
                }
                .subscribe()
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