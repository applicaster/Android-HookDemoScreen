package com.applicaster.cleengloginplugin.views

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import com.applicaster.cleengloginplugin.*
import com.applicaster.cleengloginplugin.R
import com.applicaster.cleengloginplugin.helper.CleengManager
import com.applicaster.cleengloginplugin.helper.CustomizationHelper
import com.applicaster.cleengloginplugin.helper.IAPManager
import com.applicaster.cleengloginplugin.models.Subscription
import com.applicaster.cleengloginplugin.remote.WebService
import com.applicaster.util.OSUtil
import kotlinx.android.synthetic.main.bottom_bar.*
import kotlinx.android.synthetic.main.subscription_activity.*
import kotlinx.android.synthetic.main.subscription_item.view.*

class SubscriptionsActivity: BaseActivity() {

    override fun getContentViewResId(): Int {
        return R.layout.subscription_activity;
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (CleengManager.availableSubscriptions.count() == 0) {
            CleengManager.fetchAvailableSubscriptions(this) { status: WebService.Status, response: String? ->
                this.dismissLoading()

                if (status == WebService.Status.Success) {
                    this.presentSubscriptions()
                } else {
                    this.showError(status, response)
                }
            }
        } else {
            this.presentSubscriptions()
        }
    }

    override fun customize() {
        super.customize()
        CustomizationHelper.updateTextView(this, R.id.title, SUBSCRIPTION_TITLE)
        CustomizationHelper.updateTextView(this, R.id.sign_up_action_text, SIGN_IN_LABEL_TEXT)
        CustomizationHelper.updateTextView(this, R.id.sign_up_text, ALREADY_HAVE_ACCOUNT_HINT)
        CustomizationHelper.updateTextView(this, R.id.bottom_bar_title, LOGIN_LEGAL)

        updateViews()
    }

    fun updateViews() {
        bottom_bar_action_text.visibility = View.GONE;
    }

    private fun presentSubscriptions() {
        for (subscription in CleengManager.availableSubscriptions) {
            subscriptionsContainer.addView(this.getSubscriptionView(subscription,subscriptionsContainer))
            subscriptionsContainer.addView(this.getSubscriptionView(subscription,subscriptionsContainer))
            subscriptionsContainer.addView(this.getSubscriptionView(subscription,subscriptionsContainer))
        }
    }

    private fun getSubscriptionView(subscription: Subscription, container: ViewGroup): View {
        val subscriptionView = this.layoutInflater.inflate(R.layout.subscription_item, container, false)
        CustomizationHelper.updateTextView(this, R.id.item_title, subscription.title)
        CustomizationHelper.updateTextView(this, R.id.item_description,  subscription.description)
        CustomizationHelper.updateTextView(this, R.id.item_price, "SUBSCRIBE FOR $" +subscription.price.toString())
        subscriptionView.item_title.text = subscription.title
        subscriptionView.item_description.text = subscription.description
        subscriptionView.item_price.text = "SUBSCRIBE FOR $" +subscription.price.toString()
        subscriptionView.purchaseButton.radius = OSUtil.convertDPToPixels(20).toFloat();
        subscriptionView.purchaseButton.setOnClickListener {

            val iapManager = IAPManager(this)
            iapManager.init(subscription.androidProductId)
        }

        return subscriptionView

    }


    companion object {
        fun launchSubscriptionsActivity(context: Context) {
            context.startActivity(Intent(context, SubscriptionsActivity::class.java))
        }
    }
}