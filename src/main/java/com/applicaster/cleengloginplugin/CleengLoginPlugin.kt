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
import com.applicaster.plugin_manager.login.LoginManager
import com.applicaster.plugin_manager.playersmanager.Playable
import com.applicaster.util.StringUtil

class CleengLoginPlugin :  BaseLoginContract(), ApplicationLoaderHookUpI {

    override fun executeOnStartup(context: Context, listener: HookListener) {
        val showOnAppLaunch = StringUtil.booleanValue(this.pluginParams[START_ON_LAUNCH] as String)
        if ((showOnAppLaunch || true) && CleengManager.userHasValidToken()) {
            val loginManagerBroadcastReceiver = LoginManager.LoginContractBroadcasterReceiver() {
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
        openFirstScreen(context, playable)
    }

    override fun isItemLocked(model: Any?): Boolean {
        TODO("not implemented") //isItemComply?
    }

    override fun isTokenValid(): Boolean {
        //val fbToken = FacebookUtil.isTokenValid(CustomFBPermissions.getInstance())
        return super.isTokenValid()
    }

    override fun logout(context: Context?, additionalParams: MutableMap<Any?, Any?>?) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun login(context: Context?, additionalParams: MutableMap<Any?, Any?>?) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }


    override fun setPluginConfigurationParams(params: MutableMap<Any?, Any?>?) {
        super.setPluginConfigurationParams(params)
        PluginConfigurationHelper.setConfigurationMap(params as Map<String, String>)
    }

    private fun openFirstScreen(context: Context, playable: Playable?) {
        val firstScreenValue = this.pluginParams[LOGIN_FIRST_SCREEN]
        when (firstScreenValue) {
            "subscription" -> SubscriptionsActivity.launchSubscriptionsActivity(context, playable)
            "login" -> LoginActivity.launchLogin(context, playable)
            "sign up" -> SignUpActivity.launchSignUpActivity(context, playable)
            else -> LoginActivity.launchLogin(context, playable)
        }
    }
}