package com.applicaster.cleengloginplugin.helper

import android.app.Activity
import android.content.Context
import android.os.Build
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import com.applicaster.cleengloginplugin.views.SignUpActivity
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
        fun updateImageView(activity : Activity, id: Int , key : String) {
            val imageView = activity.findViewById(id) as ImageView? ?: return
            val iconDrawableRedId = OSUtil.getDrawableResourceIdentifier(key)
            if(iconDrawableRedId != 0){
                imageView.setImageResource(iconDrawableRedId)
            }
        }

        @JvmStatic
        fun updateTextView(activity : Activity, id: Int , key : String , style: String?) {
            val textView = activity.findViewById(id) as TextView? ?: return
            var textValue = PluginConfigurationHelper.getConfigurationValue(key)
            textValue = if (StringUtil.isNotEmpty(textValue))  textValue else key
            textView.text = textValue

            if(style != null)
                updateTextStyle(activity, textView, style)

        }

        @JvmStatic
        fun updateEditTextView(activity : Activity, id: Int , key : String , isHint: Boolean) {
            val editText = activity.findViewById(id) as EditText? ?: return
            var textValue = PluginConfigurationHelper.getConfigurationValue(key)
            textValue = if (StringUtil.isNotEmpty(textValue))  textValue else key
            if (!isHint) {
                editText.setText(textValue)
            } else{
                editText.hint = textValue
            }

            updateTextStyle(activity, editText, "CleengLoginDefaultText")
        }

        @JvmStatic
        fun updateButtonViewText(activity: Activity, id: Int, key: String, style: String?) {
            val button = activity.findViewById(id) as Button? ?: return
            var textValue = PluginConfigurationHelper.getConfigurationValue(key)
            textValue = if (StringUtil.isNotEmpty(textValue))  textValue else key
            button.text = textValue

            if (style != null)
                updateTextStyle(activity, button, style)
        }

        @JvmStatic
        fun updateBgResource(activity: Activity, id: Int, key: String) {
            val view = activity.findViewById(id) as View? ?: return
            val bgDrawableRedId = OSUtil.getDrawableResourceIdentifier(key)
                if (bgDrawableRedId != 0) {
                    view.setBackgroundResource(bgDrawableRedId)
                }
        }

        @JvmStatic
        fun updateBgColor(activity: Activity, id: Int, key: String) {
            val view = activity.findViewById(id) as View? ?: return
            val bgColorId = OSUtil.getColorResourceIdentifier(key)
            if (bgColorId != 0) {
                view.setBackgroundColor(bgColorId)
            }
        }

        @JvmStatic
        fun updateButtonStyle(activity: Activity, id: Int, key: String) {
            val view = activity.findViewById(id) as Button? ?: return
            val bgDrawableRedId = OSUtil.getStylableResourceIdentifier(key)
            if (bgDrawableRedId != 0) {
                view.setTextAppearance(activity, bgDrawableRedId)
            }
        }

        @JvmStatic
        fun updateTextStyle(context: Context, view: TextView, key: String) {
            val styleRedId = OSUtil.getStyleResourceIdentifier(key)
            if (styleRedId != 0) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    view.setTextAppearance(styleRedId)
                } else {
                    view.setTextAppearance(context, styleRedId)
                }
            }
        }

    }
}
