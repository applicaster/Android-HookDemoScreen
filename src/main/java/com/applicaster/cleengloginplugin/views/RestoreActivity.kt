package com.applicaster.cleengloginplugin.views

import android.content.Context
import android.content.Intent
import android.util.Log
import com.applicaster.billing.v3.handlers.APIabSetupFinishedHandler
import com.applicaster.billing.v3.util.APBillingUtil
import com.applicaster.cleengloginplugin.*
import com.applicaster.cleengloginplugin.helper.CleengManager
import com.applicaster.cleengloginplugin.helper.CustomizationHelper
import com.applicaster.cleengloginplugin.helper.IAPManager
import com.applicaster.cleengloginplugin.helper.SubscriptionLoaderHelper
import com.applicaster.cleengloginplugin.models.Subscription
import com.applicaster.cleengloginplugin.models.User
import com.applicaster.cleengloginplugin.remote.WebService
import com.applicaster.model.APModel
import com.applicaster.plugin_manager.login.LoginManager
import com.applicaster.plugin_manager.playersmanager.Playable
import com.applicaster.util.OSUtil
import kotlinx.android.synthetic.main.restore_activity.*
import kotlinx.android.synthetic.main.user_input.*
import rx.Observable
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers

class RestoreActivity : BaseActivity() {

    private val iapManager = IAPManager(this)

    override fun getContentViewResId(): Int {
        return R.layout.restore_activity;
    }

    override fun customize() {
        super.customize()

        CustomizationHelper.updateTextView(this, R.id.restore_title, RESTORE_TITLE, "CleengLoginTitle")
        CustomizationHelper.updateTextView(this, R.id.restore_description, RESTORE_DESCRIPTION, "CleengLoginDescriptionText")
        CustomizationHelper.updateTextView(this, R.id.bottom_bar_action_text, RESTORE_HELP_ACTION, "CleengLoginActionText")
        CustomizationHelper.updateTextView(this, R.id.bottom_bar_title, RESTORE_HELP_TEXT, "CleengLoginActionDescriptionText")
        CustomizationHelper.updateEditTextView(this, R.id.input_email, EMAIL_PLACEHOLDER, true)
        CustomizationHelper.updateEditTextView(this, R.id.input_password, PASSWORD_PLACEHOLDER, true)
        CustomizationHelper.updateTextView(this, R.id.forgot_password, RESET_PASSWORD_ACTION_TEXT, "CleengLoginActionDescriptionText")
        CustomizationHelper.updateButtonViewText(this, R.id.action_button, RESTORE_BUTTON, "CleengLoginRestoreButtonText")
        CustomizationHelper.updateBgResource(this, R.id.action_button, "cleeng_login_restore_button")

        val underlineRes = OSUtil.getDrawableResourceIdentifier("cleeng_login_signin_component")
        if (underlineRes != 0) {
            input_email.setBackgroundResource(underlineRes)
            input_password.setBackgroundResource(underlineRes)
        }

        action_button.setOnClickListener {
            val user = this.getUserFromInput() ?: return@setOnClickListener

            showLoading()

            if (CleengManager.availableSubscriptions.count() == 0) {

                CleengManager.fetchAvailableSubscriptions(this, null) { status, response ->

                    if (status == WebService.Status.Success) {
                        CleengManager.parseAvailableSubscriptions(response, this)
                        doRestore(user)
                    }
                }
            } else {
                doRestore(user)
            }
        }

        restore_bottom_bar_container.setOnClickListener {
            //clicking on the bottom bar in restore
            // What should we do here???????
        }

        forgot_password.setOnClickListener {
            ResetPasswordActivity.launchResetActivity(this, trigger)
        }
    }

    private fun doRestore(user: User) {

        val authProviders = (playable as? APModel)?.authorization_providers_ids

        iapManager.init(object : APIabSetupFinishedHandler {

            override fun onIabSetupSucceeded() {

                val itemList = mutableListOf<String>()
                CleengManager.availableSubscriptions.forEach { subscription ->
                    itemList.add(subscription.androidProductId)
                }

                iapManager.getInventory(itemList) { isSuccess ->

                    if (isSuccess) {

                        CleengManager.register(user, this@RestoreActivity) { status, response ->

                            if (status == WebService.Status.Success) {

                                Observable.from(CleengManager.availableSubscriptions)
                                        .filter { item ->
                                            CleengManager.purchasedItems.containsKey(item.androidProductId)
                                        }
                                        .concatMap { subscription ->

                                            return@concatMap Observable.create(Observable.OnSubscribe<Subscription> { subscriber ->

                                                val purchase = CleengManager.purchasedItems[subscription.androidProductId]

                                                CleengManager.subscribe(CleengManager.currentUser?.token!!, subscription.authID, purchase!!, this@RestoreActivity, true) { status, _ ->

                                                    if (status == WebService.Status.Success && authProviders != null && authProviders.contains(subscription.authID)) {
                                                        subscriber.onNext(subscription)
                                                    }

                                                    subscriber.onCompleted()
                                                }
                                            })
                                        }
                                        .subscribeOn(Schedulers.io())
                                        .observeOn(AndroidSchedulers.mainThread())
                                        .subscribe({ item ->
                                            itemPurchased = item
                                        }, { error ->
                                            Log.d(TAG, error.localizedMessage)
                                        }, {
                                            restoreCompleted()
                                        })
                            } else {
                                dismissLoading()
                                if (response != null) {
                                    showAlertDialog("Restore Your Account", response)
                                }
                            }
                        }
                    }
                }
            }

            override fun onIabSetupFailed() {
                APBillingUtil.showInappBillingNotSupportedDialog(this@RestoreActivity)
            }
        })
    }

    private fun restoreCompleted() {

        val subscriptionCopy = itemPurchased
        if (subscriptionCopy == null) {
            dismissLoading()
            //TODO replace hardcoded string
            showAlertDialog("Restore Your Account", "Unfortunately there are no purchases to restore") {
                SignUpActivity.launchSignUpActivity(this@RestoreActivity, playable, "Restore")
                finish()
            }
        } else {

            Log.d("TK", "SubscriptionLoaderHelper params = $subscriptionCopy, ")

            SubscriptionLoaderHelper(this@RestoreActivity, subscriptionCopy.androidProductId, CleengManager.currentUser?.token!!, subscriptionCopy.authID, true, 60, 5) { isSuccess ->
                if (isSuccess) {
                    LoginManager.notifyEvent(this@RestoreActivity, LoginManager.RequestType.LOGIN, true)
                    finish()
                } else {
                    dismissLoading()
                }
            }.load()
        }
    }

    companion object {

        private const val TAG = "RestoreActivity"
        private var itemPurchased: Subscription? = null

        fun launchRestoreActivity(context: Context, playable: Playable?) {
            val intent = Intent(context, RestoreActivity::class.java)
            if (playable != null && playable is APModel) {
                intent.putExtra(PLAYABLE, playable)
            }
            context.startActivity(intent)
        }
    }
}