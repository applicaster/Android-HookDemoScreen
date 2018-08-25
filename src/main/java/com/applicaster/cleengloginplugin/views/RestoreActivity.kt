package com.applicaster.cleengloginplugin.views

import android.content.Context
import android.content.Intent
import android.util.Log
import com.applicaster.billing.v3.handlers.APQueryInventoryFinishedHandler
import com.applicaster.billing.v3.util.Purchase
import com.applicaster.cleengloginplugin.*;
import com.applicaster.cleengloginplugin.R
import com.applicaster.cleengloginplugin.helper.CleengManager
import com.applicaster.cleengloginplugin.helper.CustomizationHelper
import com.applicaster.cleengloginplugin.helper.IAPManager
import com.applicaster.cleengloginplugin.helper.SubscriptionLoaderHelper
import com.applicaster.cleengloginplugin.remote.WebService
import com.applicaster.plugin_manager.login.LoginManager
import kotlinx.android.synthetic.main.restore_activity.*
import kotlinx.android.synthetic.main.user_input.*

class RestoreActivity : BaseActivity() {

    private val iapManager = IAPManager(this) { status: WebService.Status, response: String? ->
    }

    override fun getContentViewResId(): Int {
        return R.layout.restore_activity;
    }

    override fun customize() {
        super.customize()

        CustomizationHelper.updateTextView(this, R.id.restore_title, RESTORE_TITLE,"CleengLoginTitle")
        CustomizationHelper.updateTextView(this, R.id.restore_description, RESTORE_DESCRIPTION,"CleengLoginDescriptionText")
        CustomizationHelper.updateTextView(this, R.id.bottom_bar_action_text, RESTORE_HELP_ACTION,"CleengLoginActionText")
        CustomizationHelper.updateTextView(this, R.id.bottom_bar_title, RESTORE_HELP_TEXT,"CleengLoginActionDescriptionText")

        CustomizationHelper.updateEditTextView(this, R.id.input_email, EMAIL_PLACEHOLDER, true)
        CustomizationHelper.updateEditTextView(this, R.id.input_password, PASSWORD_PLACEHOLDER, true)
        CustomizationHelper.updateTextView(this, R.id.forgot_password, RESET_PASSWORD_ACTION_TEXT, "CleengLoginActionDescriptionText")
        CustomizationHelper.updateButtonViewText(this, R.id.action_button, RESTORE_BUTTON,"CleengLoginRestoreButtonText")
        CustomizationHelper.updateBgResource(this,R.id.action_button,"cleeng_login_restore_button")


        action_button.setOnClickListener {
            val user = this.getUserFromInput() ?: return@setOnClickListener

            this.showLoading()
            CleengManager.register(user, this) { status: WebService.Status, response: String? ->
                this.dismissLoading()

                iapManager.getInventory(null , handler)
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

    private val handler = object :APQueryInventoryFinishedHandler {
        override fun onInventoryQueryFailed() {
            Log.e("HEKP", "onInventoryQueryFailed!!")
        }

        override fun onUnconsumedPurchaseFound(purchase: Purchase?): Boolean {
            iapManager.loadSubscriptions(purchase,null)
            return false
        }

        override fun onInventoryEmpty() {
            Log.e("HEKP", "EMPTY!!")
        }

    }

}