package com.applicaster.cleengloginplugin

import android.content.Context
import com.applicaster.plugin_manager.hook.HookListener
import com.applicaster.plugin_manager.login.LoginContract
import com.applicaster.plugin_manager.playersmanager.Playable

class CleengLogin : LoginContract {

    override fun login(context: Context?, additionalParams: MutableMap<Any?, Any?>?, callback: LoginContract.Callback?) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun login(context: Context?, playable: Playable?, additionalParams: MutableMap<Any?, Any?>?, callback: LoginContract.Callback?) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun isItemLocked(model: Any?): Boolean {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun isTokenValid(): Boolean {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun setToken(token: String?) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun executeOnStartup(context: Context?, listener: HookListener?) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getToken(): String {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun setPluginConfigurationParams(params: MutableMap<Any?, Any?>?) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun handlePluginScheme(context: Context?, data: MutableMap<String, String>?): Boolean {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun executeOnApplicationReady(context: Context?, listener: HookListener?) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun logout(context: Context?, additionalParams: MutableMap<Any?, Any?>?, callback: LoginContract.Callback?) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}