package com.applicaster.cleengloginplugin.views

import android.content.Context
import android.content.Intent
import android.view.View
import android.widget.Button
import com.applicaster.billing.v3.handlers.APIabSetupFinishedHandler
import com.applicaster.billing.v3.util.APBillingUtil
import com.applicaster.cleengloginplugin.*;
import com.applicaster.cleengloginplugin.R
import com.applicaster.cleengloginplugin.helper.*
import com.applicaster.cleengloginplugin.remote.WebService
import com.applicaster.model.APModel
import com.applicaster.plugin_manager.playersmanager.Playable
import com.applicaster.util.FacebookUtil
import com.applicaster.util.OSUtil
import com.applicaster.util.StringUtil
import com.applicaster.util.facebook.listeners.FBAuthoriziationListener
import com.applicaster.util.facebook.permissions.APPermissionsType
import kotlinx.android.synthetic.main.additional_auth.*
import kotlinx.android.synthetic.main.new_account_activiy.*
import kotlinx.android.synthetic.main.user_input.*

class SignUpActivity : BaseActivity() {

    private var continueToPayment: Boolean = false
    private lateinit var extraData: HashMap<String,String>
    private val iapManager = IAPManager(this)

    override fun getContentViewResId(): Int {
        return R.layout.new_account_activiy;
    }

    override fun customize() {
        super.customize()

        continueToPayment = intent.getBooleanExtra("continueToPayment", false)
        if (continueToPayment) {
            extraData = intent.getSerializableExtra("extraData") as HashMap<String, String>
        }

        CustomizationHelper.updateImageView(this, R.id.app_logo, "cleeng_login_logo")
        CustomizationHelper.updateTextView(this, R.id.enter_details, NEW_ACCOUNT_TITLE, "CleengLoginDescriptionText")
        CustomizationHelper.updateTextView(this, R.id.sign_up_action_text, SIGN_IN_LABEL_TEXT, "CleengLoginActionText")
        CustomizationHelper.updateTextView(this, R.id.sign_up_text, ACCOUNT_HINT, "CleengLoginActionDescriptionText")

        CustomizationHelper.updateEditTextView(this, R.id.input_email, EMAIL_PLACEHOLDER, true)
        CustomizationHelper.updateEditTextView(this, R.id.input_password, PASSWORD_PLACEHOLDER, true)
        CustomizationHelper.updateTextView(this, R.id.forgot_password, RESET_PASSWORD_ACTION_TEXT, "CleengLoginActionDescriptionText")
        CustomizationHelper.updateButtonViewText(this, R.id.action_button, SIGN_UP_BUTTON, "CleengLoginSignUpButtonText")
        CustomizationHelper.updateBgResource(this,R.id.action_button,"cleeng_login_sign_up_button")


        val underlineRes = OSUtil.getDrawableResourceIdentifier("cleeng_login_signin_component")
        if (underlineRes != 0) {
            input_email.setBackgroundResource(underlineRes)
            input_password.setBackgroundResource(underlineRes)
        }

        updateViews()

        action_button.setOnClickListener {
            val user = this.getUserFromInput() ?: return@setOnClickListener

            this.showLoading()
            AnalyticsHelper.sendLoginEvent(AnalyticsHelper.START_REGISTRATION, null, trigger ?: "")
            CleengManager.register(user, this) { status: WebService.Status, response: String? ->
                this.dismissLoading()

                if (status == WebService.Status.Success) {
                    if (continueToPayment) {
                        purchase()
                    } else {
                        SubscriptionsActivity.launchSubscriptionsActivity(this@SignUpActivity, playable, trigger)
                    }
                } else {
                    this.showError(status, response)
                    AnalyticsHelper.sendErrorEvent(AnalyticsHelper.REGISTRATION_DOES_NOT_SUCCEED,trigger ?: "", "Error Returned", response)
                }
            }
        }

        sign_in_hint.setOnClickListener{
            LoginActivity.launchLogin(this, playable, trigger)
            this.finish()
        }

        if( !StringUtil.booleanValue(PluginConfigurationHelper.getConfigurationValue(PURCHASE_RESTORE_AVAILABLE) as String)) {
            bottom_bar_container_new_account.visibility = View.GONE

        } else {
            bottom_bar_container_new_account.setOnClickListener{
                RestoreActivity.launchRestoreActivity(this)
                this.finish()
            }
        }
    }

    fun updateViews() {
        if( !StringUtil.booleanValue(PluginConfigurationHelper.getConfigurationValue(FACEBOOK_LOGIN_AVAILABLE) as String)){
            facebook_button.visibility = View.GONE
            google_button.visibility = View.GONE
        } else {
            CustomizationHelper.updateBgResource(this,R.id.google_button,"cleeng_login_facebook_button")
            val googleBtn = google_button.findViewById(R.id.google_btn) as Button
            CustomizationHelper.updateTextStyle(this, googleBtn, "FacebookLoginFacebookButtonText")

            CustomizationHelper.updateBgResource(this,R.id.facebook_button,"cleeng_login_facebook_button")
            CustomizationHelper.updateButtonViewText(this, R.id.fb_btn, "cleeng_login_facebook_button", "FacebookLoginFacebookButtonText")
            facebook_button.findViewById<Button>(R.id.fb_btn).setOnClickListener {
                FacebookUtil.updateTokenIfNeeded(this, APPermissionsType.Custom, object : FBAuthoriziationListener {

                    override fun onError(error: Exception) {
                        AnalyticsHelper.sendErrorEvent(AnalyticsHelper.REGISTRATION_DOES_NOT_SUCCEED,trigger ?: "", "Error Returned", error.message)
                    }

                    override fun onSuccess() {
                        val user = fetchUserData()
                        if (user != null) {
                            showLoading()
                            CleengManager.register(user, this@SignUpActivity) { status: WebService.Status, response: String? ->
                                dismissLoading()

                                if (status == WebService.Status.Success) {
                                    AnalyticsHelper.sendLoginEvent(AnalyticsHelper.REGISTRATION_SUCCEEDS, null, trigger ?: "")
                                    if (continueToPayment) {
                                        purchase()
                                    } else {
                                        SubscriptionsActivity.launchSubscriptionsActivity(this@SignUpActivity, playable, trigger)
                                        finish()
                                    }
                                } else {
                                    showError(status, response)
                                    AnalyticsHelper.sendErrorEvent(AnalyticsHelper.REGISTRATION_DOES_NOT_SUCCEED,trigger ?: "", "Error Returned", response)

                                }
                            }
                        }
                    }

                    override fun onCancel() {
                        AnalyticsHelper.sendErrorEvent(AnalyticsHelper.REGISTRATION_DOES_NOT_SUCCEED,trigger ?: "", "User Cancels")

                    }
                })
            }
        }
        forgot_password.visibility = View.INVISIBLE;
    }

    private fun purchase() {
        showLoading()
        val androidProductId = extraData["androidProductId"]
        var itemId = extraData["authID"]
        val isAuthId = StringUtil.isNotEmpty(itemId)
        if (!isAuthId) itemId = extraData["offerId"]


        if (StringUtil.isNotEmpty(androidProductId) && StringUtil.isNotEmpty(itemId)) {
            iapManager.init(object : APIabSetupFinishedHandler {

                override fun onIabSetupSucceeded() {
                    iapManager.startPurchase(androidProductId!!, itemId!!, isAuthId, extraData["itemType"])
                }

                override fun onIabSetupFailed() {
                    APBillingUtil.showInappBillingNotSupportedDialog(this@SignUpActivity)
                }
            })
        }
    }

    companion object {
        fun launchSignUpActivity(context: Context, playable: Playable?, trigger: String?, continueToPayment: Boolean = false, extraData: HashMap<String, String>? = null) {
            val intent = Intent(context,SignUpActivity::class.java)
            if (playable != null && playable is APModel) {
                intent.putExtra("authIds", playable.authorization_providers_ids)
                intent.putExtra(PLAYABLE, playable)
            }
            if (trigger != null) intent.putExtra(TRIGGER, trigger)
            intent.putExtra("continueToPayment", continueToPayment)
            intent.putExtra("extraData", extraData)
            context.startActivity(intent)
        }
    }
}