package com.applicaster.cleengloginplugin.views

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.applicaster.billing.v3.handlers.APIabSetupFinishedHandler
import com.applicaster.billing.v3.util.APBillingUtil
import com.applicaster.cleengloginplugin.*
import com.applicaster.cleengloginplugin.helper.CleengManager
import com.applicaster.cleengloginplugin.helper.CleengUtil
import com.applicaster.cleengloginplugin.helper.CustomizationHelper
import com.applicaster.cleengloginplugin.helper.IAPManager
import com.applicaster.cleengloginplugin.helper.PluginConfigurationHelper
import com.applicaster.cleengloginplugin.models.Subscription
import com.applicaster.cleengloginplugin.models.ownedSubscriptions
import com.applicaster.cleengloginplugin.remote.Params
import com.applicaster.cleengloginplugin.remote.WebService
import com.applicaster.model.APModel
import com.applicaster.plugin_manager.login.LoginManager
import com.applicaster.plugin_manager.playersmanager.Playable
import com.applicaster.util.OSUtil
import com.applicaster.util.StringUtil
import kotlinx.android.synthetic.main.account_sign_text_with_action.*
import kotlinx.android.synthetic.main.subscription_activity.*
import kotlinx.android.synthetic.main.subscription_item.view.*

class SubscriptionsActivity: BaseActivity() {

    private var mPlayable: Playable? = null
    private val iapManager = IAPManager(this)
    var isActiveUser :Boolean = false

    override fun getContentViewResId(): Int {
        return R.layout.subscription_activity;
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        mPlayable = intent.getSerializableExtra(PLAYABLE) as Playable?

        val params = Params()
        if (mPlayable != null) {
            val authIds = CleengManager.getAuthIds(mPlayable)
            if(authIds != null && !authIds.isEmpty()) {
                params["offers"] = authIds.joinToString().replace("\\s".toRegex(), "")
                params["byAuthId"] = "1"
            }
        }
        if (CleengManager.availableSubscriptions.count() == 0) {
            showLoading()
            CleengManager.fetchAvailableSubscriptions(this, params) { status: WebService.Status, response: String? ->

                if (status == WebService.Status.Success) {
                    CleengManager.parseAvailableSubscriptions(response, this)
                } else {
                    showError(status, response)
                }
            }
        } else {
            presentSubscriptions()
        }
    }

    override fun onResume() {
        super.onResume()
        updateViews()
    }

    override fun customize() {
        super.customize()
        CustomizationHelper.updateTextView(this, R.id.title, SUBSCRIPTION_TITLE,"CleengLoginTitle")
        CustomizationHelper.updateTextView(this, R.id.sign_up_action_text, SIGN_IN_LABEL_TEXT,"CleengLoginActionText")
        CustomizationHelper.updateTextView(this, R.id.sign_up_text, HAVE_ACCOUNT, "CleengLoginActionDescriptionText")
        CustomizationHelper.updateTextView(this, R.id.legal_bottom_bar_text, LOGIN_LEGAL,"CleengLoginLegalText")

        sign_up_action_text.setOnClickListener {
            LoginActivity.launchLogin(this, null, trigger)
        }
    }

    fun updateViews() {
        isActiveUser = StringUtil.isNotEmpty(CleengManager.currentUser?.token) && CleengManager.userHasValidToken()
        if (isActiveUser)
            subscription_sign_up_hint.visibility = View.GONE
    }

    fun presentSubscriptions() {
        dismissLoading()
        for (subscription in CleengManager.availableSubscriptions) {
            if (isActiveUser) {
                //ignore purchased items
                if (CleengManager.purchasedItems.contains(subscription.androidProductId)) {
                    if(checkCleengUser(subscription)) {
                        LoginManager.notifyEvent(this, LoginManager.RequestType.LOGIN, true)
                        finish()
                    }
                } else {
                    subscriptionsContainer.addView(this.getSubscriptionView(subscription, subscriptionsContainer))
                }
            } else {
                subscriptionsContainer.addView(this.getSubscriptionView(subscription, subscriptionsContainer))
            }
        }
    }

    private fun checkCleengUser(subscription: Subscription): Boolean {
        val ownedSubs = CleengUtil.getUser()?.ownedSubscriptions
        if(ownedSubs != null && ownedSubs.isNotEmpty())
            return ownedSubs.contains(subscription)
        return false
    }

    private fun getSubscriptionView(subscription: Subscription, container: ViewGroup): View {
        val subscriptionView = this.layoutInflater.inflate(R.layout.subscription_item, container, false)

        var subsViewBackground = OSUtil.getDrawableResourceIdentifier("cleeng_login_subscription_component")
        if (subsViewBackground != 0) {
            subscriptionView.text_container.setBackgroundResource(subsViewBackground)
        }
        var badgeBackground = OSUtil.getDrawableResourceIdentifier("cleeng_login_promotion_icon")
        if (badgeBackground != 0)
            subscriptionView.badge_text_view.setImageResource(badgeBackground)

        subscriptionView.title_text_view.text = subscription.title
        CustomizationHelper.updateTextStyle(this, subscriptionView.title_text_view, "CleengLoginSubscriptionTitle")
        subscriptionView.description_text_view.text = subscription.description
        CustomizationHelper.updateTextStyle(this, subscriptionView.description_text_view, "CleengLoginSubscriptionDetailsText")
        val price = "${PluginConfigurationHelper.getConfigurationValue("cleeng_login_subscribe")} ${subscription.price}"
        subscriptionView.purchase_button.text =  price
        CustomizationHelper.updateTextStyle(this,subscriptionView.purchase_button, "CleengLoginSubscriptionText")
        var purchaseBtnBackground = OSUtil.getDrawableResourceIdentifier("cleeng_login_subscribe_button")
        if (purchaseBtnBackground != 0)
            subscriptionView.purchase_button.setBackgroundResource(purchaseBtnBackground)
        subscriptionView.purchase_button.setOnClickListener {
            var itemId = subscription.authID
            val isAuthId = StringUtil.isNotEmpty(itemId)
            if (!isAuthId) itemId = subscription.id

            if (StringUtil.isEmpty(CleengManager.currentUser?.token)) {
                val purchaseData = HashMap<String, String>()
                purchaseData["androidProductId"] = subscription.androidProductId
                purchaseData["authID"] = subscription.authID
                purchaseData["offerId"] = subscription.id
                purchaseData["itemType"] = subscription.type ?: ITEM_TYPE_SUBS
                SignUpActivity.launchSignUpActivity(this, playable, trigger, true, purchaseData)
            } else {
                iapManager.init(object : APIabSetupFinishedHandler {

                    override fun onIabSetupSucceeded() {
                        iapManager.startPurchase(subscription.androidProductId, itemId, isAuthId, subscription.type) {succeed ->
                            if (!succeed)
                                Toast.makeText(this@SubscriptionsActivity, "Failed or Canceled", Toast.LENGTH_LONG).show()

                        }
                    }

                    override fun onIabSetupFailed() {
                        APBillingUtil.showInappBillingNotSupportedDialog(this@SubscriptionsActivity)
                    }
                })
            }
        }

        return subscriptionView
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        iapManager.handleActivityResult(requestCode, resultCode, data)
    }

    companion object {

        fun launchSubscriptionsActivity(context: Context, playable: Playable?, trigger: String?) {
            val intent = Intent(context, SubscriptionsActivity::class.java)
            if (playable is APModel) {
                intent.putExtra("authIds", playable.authorization_providers_ids)
                intent.putExtra(PLAYABLE, playable)
            }
            if (trigger != null) intent.putExtra(TRIGGER, trigger)
            context.startActivity(intent)
        }
    }
}
