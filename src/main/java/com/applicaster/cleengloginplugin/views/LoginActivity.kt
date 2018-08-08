package com.applicaster.cleengloginplugin.views

import android.content.Context
import android.content.Intent
import android.view.View.GONE
import com.applicaster.cleengloginplugin.*;
import com.applicaster.cleengloginplugin.R
import com.applicaster.cleengloginplugin.helper.CleengManager
import com.applicaster.cleengloginplugin.helper.CustomizationHelper
import com.applicaster.cleengloginplugin.helper.PluginConfigurationHelper
import com.applicaster.cleengloginplugin.remote.WebService
import com.applicaster.plugin_manager.login.LoginManager
import com.applicaster.util.StringUtil
import kotlinx.android.synthetic.main.additional_auth.*
import kotlinx.android.synthetic.main.login_activity.*
import kotlinx.android.synthetic.main.user_input.*

class LoginActivity : BaseActivity() {

    override fun getContentViewResId(): Int {
        return R.layout.login_activity;
    }

    override fun customize() {
        super.customize()
        CustomizationHelper.updateTextView(this, R.id.enter_details, LOGIN_DETAILS)
        CustomizationHelper.updateTextView(this, R.id.sign_up_action_text, SIGN_UP_LABEL_TEXT)
        CustomizationHelper.updateTextView(this, R.id.sign_up_text, NO_ACCOUNT_HINT)

        CustomizationHelper.updateEditTextView(this, R.id.input_email, DEFAULT_1, true);
        CustomizationHelper.updateEditTextView(this, R.id.input_password, DEFAULT_2, true);
        CustomizationHelper.updateTextView(this, R.id.forgot_password, RESET_PASSWORD_ACTION_TEXT)
        CustomizationHelper.updateButtonViewText(this, R.id.action_button, SIGN_IN_BUTTON)

        updateViews();

        action_button.setOnClickListener {
            val user = this.getUserFromInput() ?: return@setOnClickListener

            this.showLoading()
            CleengManager.login(user, this) { status: WebService.Status, response: String? ->
                this.dismissLoading()

                if (status == WebService.Status.Success) {
                    //notify that login finished
                    //need to save token?????
//                    LoginManager.notifyEvent(this,LoginManager.RequestType.LOGIN,true);
                    SubscriptionsActivity.launchSubscriptionsActivity(this);
                    this.finish()
                } else {
                    this.showError(status, response)
                }
            }
        }

        sign_up_hint.setOnClickListener {
            SignUpActivity.launchSignUpActivity(this);
            this.finish()
        }

        bottom_bar_container.setOnClickListener{
            RestoreActivity.launchRestoreActivity(this)
            this.finish()
        }

        forgot_password.setOnClickListener{
            ResetPasswordActivity.launchResetActivity(this)
        }
    }

    fun updateViews() {
            if( !StringUtil.booleanValue(PluginConfigurationHelper.getConfigurationValue(FACEBOOK_LOGIN_AVAILABLE) as String)){
                facebook_button.visibility = GONE;
                google_button.visibility = GONE;
            }
    }

    companion object {
        fun launchLogin(context: Context) {
            context.startActivity(Intent(context, LoginActivity::class.java))
        }
    }
}