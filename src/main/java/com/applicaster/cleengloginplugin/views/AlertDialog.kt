package com.applicaster.cleengloginplugin.views

import android.app.Dialog
import android.os.Bundle
import android.view.Window
import android.widget.Button
import android.widget.TextView
import com.applicaster.cleengloginplugin.R
import com.applicaster.cleengloginplugin.helper.PluginConfigurationHelper
import com.applicaster.util.OSUtil



class AlertDialog(private val baseActivity: BaseActivity,
                  private val title: String,
                  private val subtitle: String,
                  private val callback: () -> Unit)
    : Dialog(baseActivity, android.R.style.Theme_Translucent) {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        this.requestWindowFeature(Window.FEATURE_NO_TITLE)
        this.setCancelable(false)
        this.setContentView(R.layout.alert)

        val titleView = this.findViewById(R.id.titleTextView) as TextView
        val subtitleView = this.findViewById(R.id.subtitleTextView) as TextView

        val closeButton = this.findViewById(R.id.closeButton) as Button

        titleView.text = PluginConfigurationHelper.getConfigurationValue(title)
        subtitleView.text = PluginConfigurationHelper.getConfigurationValue(subtitle)

        closeButton.text = PluginConfigurationHelper.getConfigurationValue("cleeng_login_confirm_button")
        val btnBgColor = OSUtil.getColorResourceIdentifier("cleeng_login_confirm_button")
        if (btnBgColor != 0)
            closeButton.setBackgroundColor(btnBgColor)

        closeButton.setOnClickListener {
            this.dismiss()
            this.callback()
        }
    }
}
