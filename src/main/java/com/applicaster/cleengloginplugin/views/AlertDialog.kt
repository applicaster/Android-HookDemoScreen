package com.applicaster.cleengloginplugin.views

import android.app.Dialog
import android.os.Build
import android.os.Bundle
import android.view.Window
import android.widget.TextView
import com.applicaster.cleengloginplugin.R
import com.applicaster.cleengloginplugin.helper.PluginConfigurationHelper
import com.applicaster.util.OSUtil
import kotlinx.android.synthetic.main.alert.*

class AlertDialog(private val baseActivity: BaseActivity,
                  private val title: String,
                  private val subtitle: String,
                  private val callback: (() -> Unit)?)
    : Dialog(baseActivity, android.R.style.Theme_Translucent) {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        this.requestWindowFeature(Window.FEATURE_NO_TITLE)
        this.setCancelable(false)
        this.setContentView(R.layout.alert)

        val alertBgResource = OSUtil.getColorResourceIdentifier("cleeng_login_alert_component")
        if (alertBgResource != 0)
            alertContainer.setBackgroundResource(alertBgResource)

        setAlertTextStyle (titleTextView , "CleengLoginAlertTitle")
        titleTextView.text = PluginConfigurationHelper.getConfigurationValue(title)

        setAlertTextStyle (subtitleTextView, "CleengLoginAlertDescription")
        subtitleTextView.text = PluginConfigurationHelper.getConfigurationValue(subtitle)

        val btnBgResource = OSUtil.getDrawableResourceIdentifier("cleeng_login_confirm_button")
        if (btnBgResource != 0)
            closeButton.setBackgroundResource(btnBgResource)

        closeButton.text = PluginConfigurationHelper.getConfigurationValue("cleeng_login_confirm_button")
        val closeBtnTextStyle = OSUtil.getStyleResourceIdentifier("CleengLoginConfirmButtonText")
        if (closeBtnTextStyle != 0) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                closeButton.setTextAppearance(closeBtnTextStyle)
            } else {
                closeButton.setTextAppearance(context, closeBtnTextStyle)
            }
        }
        closeButton.setOnClickListener {
            this.dismiss()
            callback?.invoke()
        }
    }

    private fun setAlertTextStyle(view: TextView, key: String) {
        val titleStyle = OSUtil.getStyleResourceIdentifier(key)
        if (titleStyle != 0) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                view.setTextAppearance(titleStyle)
            } else {
                view.setTextAppearance(context, titleStyle)
            }
        }
    }
}
