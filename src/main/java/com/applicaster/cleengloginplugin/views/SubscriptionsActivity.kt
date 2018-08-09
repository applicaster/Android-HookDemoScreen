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
import com.applicaster.model.APModel
import com.applicaster.plugin_manager.playersmanager.Playable
import com.applicaster.util.OSUtil
import kotlinx.android.synthetic.main.bottom_bar.*
import kotlinx.android.synthetic.main.subscription_activity.*
import kotlinx.android.synthetic.main.subscription_item.view.*

class SubscriptionsActivity: BaseActivity() {

     var fromStartUp: Boolean = false;

    override fun getContentViewResId(): Int {
        return R.layout.subscription_activity;
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        fromStartUp = intent.getBooleanExtra(SUBSCIPTION_FROM_START_UP, false);

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
        if(fromStartUp){
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
        subscriptionView.item_title.text = subscription.title
        subscriptionView.item_description.text = subscription.description
        subscriptionView.item_price.text = "SUBSCRIBE FOR $" +subscription.price.toString()
        subscriptionView.purchaseButton.radius = OSUtil.convertDPToPixels(20).toFloat()
        subscriptionView.purchaseButton.setOnClickListener {

            val iapManager = IAPManager(this)
            iapManager.init(subscription.androidProductId)
        }

        return subscriptionView

    }


    companion object {

        val SUBSCIPTION_FROM_START_UP ="from_Start_up"

        fun launchSubscriptionsActivity(context: Context, fromStartUp: Boolean) {
            this.launchSubscriptionsActivity(context,null,fromStartUp)
        }

        fun launchSubscriptionsActivity(context: Context, playable: Playable?, fromStartUp: Boolean = false) {
            val intent = Intent(context,SubscriptionsActivity::class.java)
            if (playable != null && playable is APModel) {
                intent.putExtra("authIds", playable.authorization_providers_ids)
                intent.putExtra(PLAYABLE, playable)
            }
            intent.putExtra(SUBSCIPTION_FROM_START_UP,fromStartUp);
            context.startActivity(intent)
        }
    }
}