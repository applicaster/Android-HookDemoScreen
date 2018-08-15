package com.applicaster.cleengloginplugin.views

import android.content.Context
import android.content.Intent
import android.view.View.GONE
import android.widget.Button
import com.applicaster.cleengloginplugin.*;
import com.applicaster.cleengloginplugin.R
import com.applicaster.cleengloginplugin.helper.CleengManager
import com.applicaster.cleengloginplugin.helper.CustomizationHelper
import com.applicaster.cleengloginplugin.helper.PluginConfigurationHelper
import com.applicaster.cleengloginplugin.remote.WebService
import com.applicaster.plugin_manager.login.LoginManager
import com.applicaster.plugin_manager.playersmanager.Playable
import com.applicaster.util.FacebookUtil
import com.applicaster.util.StringUtil
import com.applicaster.util.facebook.listeners.FBAuthoriziationListener
import com.applicaster.util.facebook.permissions.APPermissionsType
import kotlinx.android.synthetic.main.additional_auth.*
import kotlinx.android.synthetic.main.login_activity.*
import kotlinx.android.synthetic.main.user_input.*

class LoginActivity : BaseActivity() {

    override fun getContentViewResId(): Int {
        return R.layout.login_activity;
    }

    override fun customize() {
        super.customize()
        CustomizationHelper.updateImageView(this, R.id.app_logo, "cleeng_login_logo")
        CustomizationHelper.updateTextView(this, R.id.enter_details, LOGIN_DETAILS, "CleengLoginDecriptionText")
        CustomizationHelper.updateTextView(this, R.id.sign_up_action_text, SIGN_UP_LABEL_TEXT, "CleengLoginActionText")
        CustomizationHelper.updateTextView(this, R.id.sign_up_text, NO_ACCOUNT_HINT, "CleengLoginActionDescriptionText")


        CustomizationHelper.updateEditTextView(this, R.id.input_email, EMAIL_PLACEHOLDER, true)
        CustomizationHelper.updateEditTextView(this, R.id.input_password, PASSWORD_PLACEHOLDER, true)
        CustomizationHelper.updateTextView(this, R.id.forgot_password, RESET_PASSWORD_ACTION_TEXT, "CleengLoginActionDescriptionText")
        CustomizationHelper.updateButtonViewText(this, R.id.action_button, SIGN_IN_BUTTON)



        updateViews()

        action_button.setOnClickListener {
            val user = this.getUserFromInput() ?: return@setOnClickListener

            this.showLoading()
            CleengManager.login(user, this) { status: WebService.Status, response: String?  ->
                this.dismissLoading()

                if (status == WebService.Status.Success) {
                    loginSuccessful()
                } else {
                    this.showError(status, response)
                }
            }
        }

        sign_up_hint.setOnClickListener {
            SignUpActivity.launchSignUpActivity(this, playable)
            this.finish()

        }

        bottom_bar_container.setOnClickListener{
            RestoreActivity.launchRestoreActivity(this)
        }

        forgot_password.setOnClickListener{
            ResetPasswordActivity.launchResetActivity(this)
        }
    }

    fun updateViews() {
            if( !StringUtil.booleanValue(PluginConfigurationHelper.getConfigurationValue(FACEBOOK_LOGIN_AVAILABLE) as String)){
                facebook_button.visibility = GONE
                google_button.visibility = GONE
            } else {
                CustomizationHelper.updateBgResource(this,R.id.facebook_button,"cleeng_login_facebook_button")
                CustomizationHelper.updateButtonViewText(this, R.id.fb_btn, "cleeng_login_facebook_button")
                CustomizationHelper.updateBgResource(this,R.id.google_button,"cleeng_login_facebook_button")
                facebook_button.findViewById<Button>(R.id.fb_btn).setOnClickListener {
                    FacebookUtil.updateTokenIfNeeded(this, APPermissionsType.Custom, object : FBAuthoriziationListener {

                        override fun onError(error: Exception) {
                            //We could show a message error in case we would like it
                        }

                        override fun onSuccess() {
                            val user = fetchUserData()
                            if (user != null) {
                                showLoading()
                                CleengManager.login(user, this@LoginActivity) { status: WebService.Status, response: String?  ->
                                    dismissLoading()

                                    if (status == WebService.Status.Success) {
                                       loginSuccessful()

                                    } else {
                                        showError(status, response)
                                    }
                                }
                            }

                        }

                        override fun onCancel() {
                        }
                    })
                }
            }
    }

    private fun loginSuccessful() {
        if(CleengManager.userHasActiveOffer() && isPlayableSupported()) {
                LoginManager.notifyEvent(this,LoginManager.RequestType.LOGIN, true)
        }else{
            SubscriptionsActivity.launchSubscriptionsActivity(this, playable)
        }
        finish()
    }

    companion object {
        fun launchLogin(context: Context, playable: Playable?) {
            val intent = Intent(context,LoginActivity::class.java)
            if (playable != null) {
                intent.putExtra(PLAYABLE, playable)
            }
            context.startActivity(intent)
        }
    }
}