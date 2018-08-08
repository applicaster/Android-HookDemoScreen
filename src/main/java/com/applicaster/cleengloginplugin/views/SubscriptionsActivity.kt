package com.applicaster.cleengloginplugin.views

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import com.applicaster.cleengloginplugin.*
import com.applicaster.cleengloginplugin.R
import com.applicaster.cleengloginplugin.helper.CleengManager
import com.applicaster.cleengloginplugin.helper.CustomizationHelper
import com.applicaster.cleengloginplugin.helper.IAPManager
import com.applicaster.cleengloginplugin.models.Subscription
import com.applicaster.cleengloginplugin.remote.WebService
import kotlinx.android.synthetic.main.subscription_activity.*

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
    }

    private fun presentSubscriptions() {
        for (subscription in CleengManager.availableSubscriptions) {
            subscriptionsContainer.addView(this.getSubscriptionView(subscription))
        }
    }

    private fun getSubscriptionView(subscription: Subscription): View {
        val subscriptionView = this.layoutInflater.inflate(R.layout.subscription_item, null)

//        this.customizeView(subscriptionView,
//                BaseActivity.ViewCustomization(
//                        bgImage = "cleeng_login_subscription_component"))

//        this.customizeView(subscriptionView.badgeTextView,
//                ViewCustomization(
//                        bgImage = "cleeng_login_promotion_icon"))
//
//        this.customizeView(subscriptionView.titleTextView,
//                ViewCustomization(
//                        textFont = "CleengLoginSubscriptionTitle"))
//
//        this.customizeView(subscriptionView.descriptionTextView,
//                ViewCustomization(
//                        textFont = "CleengLoginSubsriptionDetailsText"))
//
//        this.customizeView(subscriptionView.purchaseButton,
//                ViewCustomization(
//                        bgImage = "cleeng_login_subscribe_button",
//                        textFont = "CleengLoginSubscriptionText"))
//
//        this.customizeForClosedDescriptionContainer(subscriptionView)
//
//        subscriptionView.textContainer.titleTextView.text = subscription.title
//        subscriptionView.textContainer.descriptionTextView.text = subscription.description
//        subscriptionView.purchaseButton.text = "SUBSCRIBE FOR $" +subscription.price.toString()
//
//        this.customizeBottomPanel(
//                ViewCustomization(
//                        bgColor = "cleeng_login_bottom_legal_background_color"),
//                ViewCustomization(
//                        textFont = "CleengLoginLegalText",
//                        localization = "cleeng_login_account"),
//                ViewCustomization())
//
//        subscriptionView.purchaseButton.setOnClickListener {
//
//            val iapManager = IAPManager(this)
//            iapManager.init(subscription.androidProductId)
//        }

        return subscriptionView

    }


    companion object {
        fun launchSubscriptionsActivity(context: Context) {
            context.startActivity(Intent(context, SubscriptionsActivity::class.java))
        }
    }
}