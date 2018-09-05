package com.applicaster.cleengloginplugin

import android.content.Context
import com.applicaster.cleengloginplugin.helper.CleengManager
import com.applicaster.cleengloginplugin.helper.CleengUtil
import com.applicaster.cleengloginplugin.helper.PluginConfigurationHelper
import com.applicaster.cleengloginplugin.views.LoginActivity
import com.applicaster.cleengloginplugin.views.SignUpActivity
import com.applicaster.cleengloginplugin.views.SubscriptionsActivity
import com.applicaster.plugin_manager.hook.HookListener
import com.applicaster.plugin_manager.login.LoginContract
import com.applicaster.plugin_manager.login.LoginManager
import com.applicaster.plugin_manager.playersmanager.Playable
import com.applicaster.util.StringUtil

class CleengLoginPlugin : LoginContract {

    override fun logout(context: Context?, additionalParams: MutableMap<Any?, Any?>?, callback: LoginContract.Callback?) {
        CleengManager.logout()
    }

    override fun executeOnApplicationReady(context: Context?, listener: HookListener?) {
        listener?.onHookFinished()
    }

    override fun executeOnStartup(context: Context, listener: HookListener) {
        val showOnAppLaunch = StringUtil.booleanValue(PluginConfigurationHelper.getConfigurationValue(START_ON_LAUNCH) as String)
        if (showOnAppLaunch && !CleengManager.userHasValidToken()) {
            // TODO register to broadcast and finish hook after successful login (implemented LoginContract now)
            openFirstScreen(context, null)
        } else {
            listener.onHookFinished()
        }
    }

    override fun login(context: Context, additionalParams: MutableMap<Any?, Any?>?, callback: LoginContract.Callback?) {
        openFirstScreen(context, null)
    }

    override fun login(context: Context, playable: Playable?, additionalParams: MutableMap<Any?, Any?>?, callback: LoginContract.Callback) {

        LoginManager.registerToEvent(context, LoginManager.RequestType.LOGIN, LoginManager.LoginContractBroadcasterReceiver {
            callback.onResult(it)
        })

        if (CleengManager.userHasValidToken()) {
            //if user has valid token open subscription screen (user already logged in)
            SubscriptionsActivity.launchSubscriptionsActivity(context, playable)
        } else {
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

    override fun setPluginConfigurationParams(params: MutableMap<Any?, Any?>?) {
        PluginConfigurationHelper.setConfigurationMap(params as Map<String, String>)
    }

    private fun openFirstScreen(context: Context, playable: Playable?) {
        val firstScreenValue = PluginConfigurationHelper.getConfigurationValue(LOGIN_FIRST_SCREEN)
        when (firstScreenValue) {
            "subscriptions" -> SubscriptionsActivity.launchSubscriptionsActivity(context, playable)
            "login" -> LoginActivity.launchLogin(context, playable)
            "sign up" -> SignUpActivity.launchSignUpActivity(context, playable)
            else -> LoginActivity.launchLogin(context, playable)
        }
    }

    // region NOT IMPLEMENTED

    override fun getToken(): String {
        return CleengUtil.getUser()?.token ?: ""
    }

    override fun setToken(token: String?) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun handlePluginScheme(context: Context?, data: MutableMap<String, String>?): Boolean {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    // endregion
}
