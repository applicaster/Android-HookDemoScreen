package com.applicaster.cleengloginplugin.views

import android.content.Context
import android.content.Intent
import android.view.View
import android.widget.Button
import com.applicaster.cleengloginplugin.*;
import com.applicaster.cleengloginplugin.R
import com.applicaster.cleengloginplugin.helper.CleengManager
import com.applicaster.cleengloginplugin.helper.CustomizationHelper
import com.applicaster.cleengloginplugin.helper.PluginConfigurationHelper
import com.applicaster.cleengloginplugin.remote.WebService
import com.applicaster.util.FacebookUtil
import com.applicaster.util.StringUtil
import com.applicaster.util.facebook.listeners.FBAuthoriziationListener
import com.applicaster.util.facebook.permissions.APPermissionsType
import kotlinx.android.synthetic.main.additional_auth.*
import kotlinx.android.synthetic.main.new_account_activiy.*
import kotlinx.android.synthetic.main.user_input.*

class SignUpActivity : BaseActivity() {

    override fun getContentViewResId(): Int {
        return R.layout.new_account_activiy;
    }

    override fun customize() {
        super.customize()
        CustomizationHelper.updateTextView(this, R.id.enter_details, NEW_ACCOUNT_TITLE)
        CustomizationHelper.updateTextView(this, R.id.sign_up_action_text, SIGN_IN_LABEL_TEXT)
        CustomizationHelper.updateTextView(this, R.id.sign_up_text, ALREADY_HAVE_ACCOUNT_HINT)

        CustomizationHelper.updateEditTextView(this, R.id.input_email, DEFAULT_1, true);
        CustomizationHelper.updateEditTextView(this, R.id.input_password, DEFAULT_2, true);
        CustomizationHelper.updateTextView(this, R.id.forgot_password, RESET_PASSWORD_ACTION_TEXT)
        CustomizationHelper.updateButtonViewText(this, R.id.action_button, SIGN_UP_BUTTON)

        updateViews();

        action_button.setOnClickListener {
            val user = this.getUserFromInput() ?: return@setOnClickListener

            this.showLoading()
            CleengManager.register(user, this) { status: WebService.Status, response: String? ->
                this.dismissLoading()

                if (status == WebService.Status.Success) {
                    SubscriptionsActivity.launchSubscriptionsActivity(this@SignUpActivity)
                    this.finish()
                } else {
                    this.showError(status, response)
                }
            }
        }

        sign_in_hint.setOnClickListener{
            LoginActivity.launchLogin(this);
            this.finish()
        }

        bottom_bar_container_new_account.setOnClickListener{
            RestoreActivity.launchRestoreActivity(this)
            this.finish()
       }
    }

    fun updateViews() {
        if( !StringUtil.booleanValue(PluginConfigurationHelper.getConfigurationValue(FACEBOOK_LOGIN_AVAILABLE) as String)){
            facebook_button.visibility = View.GONE
            google_button.visibility = View.GONE
        } else {
            CustomizationHelper.updateBgResource(this,R.id.facebook_button,"cleeng_login_facebook_button")
            CustomizationHelper.updateButtonViewText(this, R.id.fb_btn, "cleeng_login_facebook_button")
//            facebook_btn.setTextAppearance(OSUtil.getStyleResourceIdentifier("CleengLoginFacebookButtonText"))
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
                            CleengManager.register(user, this@SignUpActivity) { status: WebService.Status, response: String? ->
                                dismissLoading()

                                if (status == WebService.Status.Success) {
                                    SubscriptionsActivity.launchSubscriptionsActivity(this@SignUpActivity)
                                    finish()
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
        forgot_password.visibility = View.INVISIBLE;
    }

    companion object {
        fun launchSignUpActivity(context: Context) {
            context.startActivity(Intent(context, SignUpActivity::class.java))
        }
    }
}