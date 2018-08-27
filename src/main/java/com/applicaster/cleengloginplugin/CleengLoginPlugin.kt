package com.applicaster.cleengloginplugin

import android.content.Context
import com.applicaster.cleengloginplugin.helper.CleengManager
import com.applicaster.cleengloginplugin.helper.PluginConfigurationHelper
import com.applicaster.cleengloginplugin.views.LoginActivity
import com.applicaster.cleengloginplugin.views.SignUpActivity
import com.applicaster.cleengloginplugin.views.SubscriptionsActivity
import com.applicaster.model.APModel
import com.applicaster.plugin_manager.hook.ApplicationLoaderHookUpI
import com.applicaster.plugin_manager.hook.HookListener
import com.applicaster.plugin_manager.login.BaseLoginContract
import com.applicaster.plugin_manager.login.LoginContract
import com.applicaster.plugin_manager.login.LoginManager
import com.applicaster.plugin_manager.playersmanager.Playable
import com.applicaster.util.StringUtil

class CleengLoginPlugin :  BaseLoginContract(), ApplicationLoaderHookUpI {

    override fun executeOnStartup(context: Context, listener: HookListener) {
        val showOnAppLaunch = StringUtil.booleanValue(this.pluginParams[START_ON_LAUNCH] as String)
        if (showOnAppLaunch && !CleengManager.userHasValidToken()) {
            val loginManagerBroadcastReceiver = LoginManager.LoginContractBroadcasterReceiver {
                listener.onHookFinished()
            }
            //need to call onHookFinished in case that user press on back in the login activity
            LoginManager.registerToEvent(context, LoginManager.RequestType.LOGIN, loginManagerBroadcastReceiver)
            LoginManager.registerToEvent(context,LoginManager.RequestType.CLOSED,loginManagerBroadcastReceiver)
            openFirstScreen(context, null)

        } else {
            listener.onHookFinished()
        }
    }

    override fun login(context: Context, playable: Playable?, additionalParams: MutableMap<Any?, Any?>?) {

        if(CleengManager.userHasValidToken()) {
                //if user has valid token open subscription screen (user already logged in)
                SubscriptionsActivity.launchSubscriptionsActivity(context, playable, true)
        }
        else {
              openFirstScreen(context, playable)
        }
    }

    override fun isItemLocked(model: Any?): Boolean {
        return CleengManager.isItemLocked(model)
    }

    override fun isItemLocked(context: Context?, model: Any?, callback: LoginContract.Callback?) {
        val receiver = LoginManager.LoginContractBroadcasterReceiver(callback)
        LoginManager.registerToEvent(context, LoginManager.RequestType.LOCKED, receiver)

        CleengManager.isItemLocked(model) {
            LoginManager.notifyEvent(context, LoginManager.RequestType.LOCKED, it)
        }

    }

    override fun isTokenValid(): Boolean {
        return CleengManager.userHasValidToken()
    }

    override fun logout(context: Context?, additionalParams: MutableMap<Any?, Any?>?) {
        CleengManager.logout()
    }

    override fun login(context: Context?, additionalParams: MutableMap<Any?, Any?>?) {
        if (context != null) {
            openFirstScreen(context, null)
        }
    }

    override fun setPluginConfigurationParams(params: MutableMap<Any?, Any?>?) {
        super.setPluginConfigurationParams(params)
        PluginConfigurationHelper.setConfigurationMap(params as Map<String, String>)
    }

    private fun openFirstScreen(context: Context, playable: Playable?) {
        var firstScreenValue = this.pluginParams[LOGIN_FIRST_SCREEN]
        when (firstScreenValue) {
            "subscription" -> SubscriptionsActivity.launchSubscriptionsActivity(context, playable, true)
            "login" -> LoginActivity.launchLogin(context, playable)
            "sign up" -> SignUpActivity.launchSignUpActivity(context, playable)
            else -> LoginActivity.launchLogin(context, playable)
        }
    }
}
