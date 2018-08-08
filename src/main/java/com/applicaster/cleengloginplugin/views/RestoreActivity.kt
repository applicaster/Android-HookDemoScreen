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
import kotlinx.android.synthetic.main.restore_activity.*
import kotlinx.android.synthetic.main.user_input.*

class RestoreActivity : BaseActivity() {

    override fun getContentViewResId(): Int {
        return R.layout.restore_activity;
    }

    override fun customize() {
        super.customize()

        CustomizationHelper.updateTextView(this, R.id.restore_title, RESTORE_TITLE)
        CustomizationHelper.updateTextView(this, R.id.restore_description, RESTORE_DESCRIPTION)
        CustomizationHelper.updateTextView(this, R.id.bottom_bar_action_text, RESTORE_HELP_ACTION)
        CustomizationHelper.updateTextView(this, R.id.bottom_bar_title, RESTORE_HELP_TEXT)

        CustomizationHelper.updateEditTextView(this, R.id.input_email, DEFAULT_1, true);
        CustomizationHelper.updateEditTextView(this, R.id.input_password, DEFAULT_2, true);
        CustomizationHelper.updateTextView(this, R.id.forgot_password, RESET_PASSWORD_ACTION_TEXT)
        CustomizationHelper.updateButtonViewText(this, R.id.action_button, RESTORE_BUTTON)

        action_button.setOnClickListener {
            val user = this.getUserFromInput() ?: return@setOnClickListener

            this.showLoading()
            //should we register???? from restore, how can i restore the acount?
            CleengManager.register(user, this) { status: WebService.Status, response: String? ->
                this.dismissLoading()
                //continue flow
            }
        }

        restore_bottom_bar_container.setOnClickListener{
            //clicking on the bottom bar in restore
            // What should we do here???????
        }

        forgot_password.setOnClickListener{
            ResetPasswordActivity.launchResetActivity(this)
        }
    }


    companion object {
        fun launchRestoreActivity(context: Context) {
            context.startActivity(Intent(context, RestoreActivity::class.java))
        }
    }
}