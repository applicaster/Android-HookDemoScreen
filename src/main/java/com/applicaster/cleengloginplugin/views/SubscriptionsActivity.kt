package com.applicaster.cleengloginplugin.views

import android.app.LauncherActivity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import com.applicaster.cleengloginplugin.*
import com.applicaster.cleengloginplugin.helper.CleengManager
import com.applicaster.cleengloginplugin.helper.CustomizationHelper
import com.applicaster.cleengloginplugin.helper.IAPManager
import com.applicaster.cleengloginplugin.models.Subscription
import com.applicaster.cleengloginplugin.remote.Params
import com.applicaster.cleengloginplugin.remote.WebService
import com.applicaster.model.APModel
import com.applicaster.plugin_manager.login.LoginManager
import com.applicaster.plugin_manager.playersmanager.Playable
import com.applicaster.util.OSUtil
import kotlinx.android.synthetic.main.account_sign_text_with_action.*
import kotlinx.android.synthetic.main.subscription_activity.*
import kotlinx.android.synthetic.main.subscription_item.view.*

class SubscriptionsActivity: BaseActivity() {

    private var fromStartUp: Boolean = false
    private var mPlayable: Playable? = null
    private val iapManager = IAPManager(this)

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
        if (CleengManager.availableSubscriptions.count() == 0 || playable != null) {
            CleengManager.fetchAvailableSubscriptions(this, params) { status: WebService.Status, response: String? ->
                this.dismissLoading()

                if (status == WebService.Status.Success) {
                    CleengManager.parseAvailableSubscriptions(status, response)
                    presentSubscriptions()
                } else {
                    showError(status, response)
                }
            }
        } else {
            this.presentSubscriptions()
        }
    }

    override fun customize() {
        super.customize()
        CustomizationHelper.updateTextView(this, R.id.title, SUBSCRIPTION_TITLE,"CleengLoginTitle")
        CustomizationHelper.updateTextView(this, R.id.sign_up_action_text, SIGN_IN_LABEL_TEXT,"CleengLoginActionText")
        CustomizationHelper.updateTextView(this, R.id.sign_up_text, HAVE_ACCOUNT, "CleengLoginActionDescriptionText")
        CustomizationHelper.updateTextView(this, R.id.legal_bottom_bar_text, LOGIN_LEGAL,"CleengLoginLegalText")

        sign_up_action_text.setOnClickListener {
            LoginActivity.launchLogin(this, null)
        }
        //CustomizationHelper.updateBgColor(this, R.id.legal_bottom_bar, "cleeng_login_bottom_legal_background_color")

        updateViews()
    }

    fun updateViews() {
        fromStartUp = intent.getBooleanExtra(SUBSCRIPTION_FROM_START_UP, false)

        if(!fromStartUp){
            subscription_sign_up_hint.visibility = View.GONE
        }
    }

    private fun presentSubscriptions() {
        for (subscription in CleengManager.availableSubscriptions) {
            subscriptionsContainer.addView(this.getSubscriptionView(subscription,subscriptionsContainer))
        }
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
        subscriptionView.purchase_button.text =  getString(R.string.subscribe_for)+subscription.price.toString()
        CustomizationHelper.updateTextStyle(this,subscriptionView.purchase_button, "CleengLoginSubscriptionText")
        var purchaseBtnBackground = OSUtil.getDrawableResourceIdentifier("cleeng_login_subscribe_button")
        if (purchaseBtnBackground != 0)
            subscriptionView.purchase_button.setBackgroundResource(purchaseBtnBackground)
        subscriptionView.purchase_button.setOnClickListener {

            iapManager.init(subscription.androidProductId, subscription.authID)
        }

        return subscriptionView
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        iapManager.handleActivityResult(requestCode, resultCode, data)
    }

    companion object {

        private const val SUBSCRIPTION_FROM_START_UP = "from_Start_up"

        fun launchSubscriptionsActivity(context: Context, playable: Playable?, fromStartUp: Boolean = false) {
            val intent = Intent(context, SubscriptionsActivity::class.java)
            if (playable is APModel) {
                intent.putExtra("authIds", playable.authorization_providers_ids)
                intent.putExtra(PLAYABLE, playable)
            }
            intent.putExtra(SUBSCRIPTION_FROM_START_UP, fromStartUp);
            context.startActivity(intent)
        }
    }
}
