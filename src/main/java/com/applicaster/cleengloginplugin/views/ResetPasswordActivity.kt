package com.applicaster.cleengloginplugin.views

import android.content.Context
import android.content.Intent
import android.view.View
import android.widget.EditText
import com.applicaster.cleengloginplugin.*;
import com.applicaster.cleengloginplugin.helper.CleengManager
import com.applicaster.cleengloginplugin.helper.CustomizationHelper
import com.applicaster.cleengloginplugin.remote.WebService
import kotlinx.android.synthetic.main.reset_password_activity.*
import kotlinx.android.synthetic.main.user_input.*

class ResetPasswordActivity : BaseActivity() {

    override fun getContentViewResId(): Int {
        return R.layout.reset_password_activity;
    }

    override fun customize() {
        super.customize()

        CustomizationHelper.updateTextView(this, R.id.title, RESET_PASSWORD_TITLE, "CleengLoginTitle")
        CustomizationHelper.updateTextView(this, R.id.description, RESET_PASSWORD_DESCRIPTION, "CleengLoginDescriptionText")
        CustomizationHelper.updateTextView(this, R.id.bottom_bar_action_text, RESET_HELP_ACTION,"CleengLoginActionText")
        CustomizationHelper.updateTextView(this, R.id.bottom_bar_title, RESET_HELP_TEXT,"CleengLoginActionDescriptionText")

        CustomizationHelper.updateEditTextView(this, R.id.restore_email, EMAIL_PLACEHOLDER, true)
        CustomizationHelper.updateButtonViewText(this, R.id.reset_button, RESET_BUTTON,"CleengLoginActionDescriptionText")
        CustomizationHelper.updateBgResource(this,R.id.reset_button,"cleeng_login_sign_in_button")

        reset_button.setOnClickListener {
            val emailEditText = this.findViewById(R.id.restore_email) as EditText
            val email = emailEditText.text.takeIf { it.isNotEmpty() }

            this.showLoading()
            CleengManager.resetPassword(email.toString(),this) { status: WebService.Status, response: String? ->
                this.dismissLoading()
                if (status == WebService.Status.Success) {
                    LoginActivity.launchLogin(this, null)
                } else {
                    showError(status, response)
                }

            }
        }

        reset_bottom_bar_container.setOnClickListener{
            //clicking on the bottom bar in restore
            // What should we do here???????
        }

    }

    companion object {
        fun launchResetActivity(context: Context) {
            context.startActivity(Intent(context, ResetPasswordActivity::class.java))
        }
    }
}