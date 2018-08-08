package com.applicaster.cleengloginplugin.views

import android.content.Context
import android.content.Intent
import android.view.View
import com.applicaster.cleengloginplugin.*;
import com.applicaster.cleengloginplugin.R
import com.applicaster.cleengloginplugin.helper.CleengManager
import com.applicaster.cleengloginplugin.helper.CustomizationHelper
import com.applicaster.cleengloginplugin.helper.PluginConfigurationHelper
import com.applicaster.cleengloginplugin.remote.WebService
import com.applicaster.util.StringUtil
import kotlinx.android.synthetic.main.additional_auth.*
import kotlinx.android.synthetic.main.login_activity.*
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
        CustomizationHelper.updateTextView(this, R.id.forgot_password, DEFAULT_2)//need to change to the correct text
        CustomizationHelper.updateButtonViewText(this, R.id.action_button, SIGN_UP_BUTTON)

        updateViews();

        action_button.setOnClickListener {
            val user = this.getUserFromInput() ?: return@setOnClickListener

            this.showLoading()
            CleengManager.register(user, this) { status: WebService.Status, response: String? ->
                this.dismissLoading()
                //continue flow
            }
        }

        sign_in_hint.setOnClickListener{
            LoginActivity.launchLogin(this);
            this.finish()
        }

        bottom_bar_container_new_account.setOnClickListener{
              //launch restore
            this.finish()
       }
    }

    fun updateViews() {
        if( !StringUtil.booleanValue(PluginConfigurationHelper.getConfigurationValue(FACEBOOK_LOGIN_AVAILABLE) as String)){
            facebook_button.visibility = View.GONE;
            google_button.visibility = View.GONE;
        }
        forgot_password.visibility = View.GONE;
    }

    companion object {
        fun launchSignUpActivity(context: Context) {
            context.startActivity(Intent(context, SignUpActivity::class.java))
        }
    }
}