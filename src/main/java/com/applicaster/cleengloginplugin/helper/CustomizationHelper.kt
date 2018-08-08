package com.applicaster.cleengloginplugin.helper

import android.app.Activity
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import com.applicaster.util.OSUtil
import com.applicaster.util.StringUtil

class CustomizationHelper{
    companion object {
        @JvmStatic
        fun updateImageView(imageView : ImageView, key : String ) {
            val iconDrawableRedId = OSUtil.getDrawableResourceIdentifier(key)
            if(imageView != null && iconDrawableRedId != 0){
                imageView.setImageResource(iconDrawableRedId)
           }
        }

        @JvmStatic
        fun updateTextView(activity : Activity, id: Int , key : String ) {
            val textView = activity.findViewById(id) as TextView? ?: return ;
            var textValue = PluginConfigurationHelper.getConfigurationValue(key);
            textValue = if (StringUtil.isNotEmpty(textValue))  textValue else key
            if(textView != null ){
                textView.setText(textValue );
            }
        }

        @JvmStatic
        fun updateEditTextView(activity : Activity, id: Int , key : String , isHint: Boolean) {
            val editText = activity.findViewById(id) as EditText? ?: return ;
            var textValue = PluginConfigurationHelper.getConfigurationValue(key);
            textValue = if (StringUtil.isNotEmpty(textValue))  textValue else key
            if(editText != null ){
                if(!isHint){
                    editText.setText(textValue)
                } else{
                    editText.setHint(textValue)
                };
            }
        }

        fun updateButtonViewText(activity: Activity, id: Int, key: String) {
            val button = activity.findViewById(id) as Button? ?: return ;
            var textValue = PluginConfigurationHelper.getConfigurationValue(key);
            textValue = if (StringUtil.isNotEmpty(textValue))  textValue else key
            if(button != null ){
                button.setText(textValue)
            }
        }

        @JvmStatic
        fun updateBgColor(activity: Activity, id: Int, key: String) {
            val view = activity.findViewById(id) as View? ?: return
            val bgDrawableRedId = OSUtil.getDrawableResourceIdentifier(key)
                if (bgDrawableRedId != 0) {
                    view.setBackgroundResource(bgDrawableRedId)
                }
        }

    }
}
