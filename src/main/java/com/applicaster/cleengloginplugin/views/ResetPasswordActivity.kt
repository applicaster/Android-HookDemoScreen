package com.applicaster.cleengloginplugin.views

import android.content.Context
import android.content.Intent
import android.view.View
import com.applicaster.cleengloginplugin.*;
import com.applicaster.cleengloginplugin.helper.CustomizationHelper
import kotlinx.android.synthetic.main.reset_password_activity.*
import kotlinx.android.synthetic.main.user_input.*

class ResetPasswordActivity : BaseActivity() {

    override fun getContentViewResId(): Int {
        return R.layout.reset_password_activity;
    }

    override fun customize() {
        super.customize()

        CustomizationHelper.updateTextView(this, R.id.title, RESET_PASSWORD_TITLE, "CleengLoginTitle")
        CustomizationHelper.updateTextView(this, R.id.description, RESET_PASSWORD_DESCRIPTION, "CleengLoginDecriptionText")
        CustomizationHelper.updateTextView(this, R.id.bottom_bar_action_text, RESET_HELP_ACTION,"CleengLoginActionText")
        CustomizationHelper.updateTextView(this, R.id.bottom_bar_title, RESET_HELP_TEXT,"CleengLoginActionDescriptionText")

        CustomizationHelper.updateEditTextView(this, R.id.input_email, EMAIL_PLACEHOLDER, true)
        CustomizationHelper.updateButtonViewText(this, R.id.action_button, RESET_BUTTON)

        updateViews();

        action_button.setOnClickListener {
            val user = this.getUserFromInput() ?: return@setOnClickListener

            this.showLoading()
            //should we rest???? from reset password, how can i reset password?
//            CleengManager.register(user, this) { status: WebService.Status, response: String? ->
//                this.dismissLoading()
//                //continue flow
//            }
        }

        reset_bottom_bar_container.setOnClickListener{
            //clicking on the bottom bar in restore
            // What should we do here???????
        }

    }

    fun updateViews() {
        input_password.visibility = View.INVISIBLE;
        forgot_password.visibility = View.INVISIBLE;

    }

    companion object {
        fun launchResetActivity(context: Context) {
            context.startActivity(Intent(context, ResetPasswordActivity::class.java))
        }
    }
}