package com.applicaster.cleengloginplugin.views

import android.app.ProgressDialog
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import com.applicaster.cleengloginplugin.*
import com.applicaster.cleengloginplugin.R
import com.applicaster.cleengloginplugin.helper.CustomizationHelper
import com.applicaster.cleengloginplugin.models.User
import com.applicaster.cleengloginplugin.remote.WebService
import com.applicaster.model.APUser
import com.applicaster.util.FacebookUtil
import com.applicaster.util.asynctask.AsyncTaskListener
import org.json.JSONObject
import java.util.*

abstract class BaseActivity : AppCompatActivity() {

    abstract fun getContentViewResId(): Int

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        this.setContentView(this.getContentViewResId())
        this.customize()
    }

    open fun customize() {
        this.customizeNavigationBar()
    }

    fun customizeNavigationBar() {
        val navigationBarContainer = this.findViewById(R.id.toolbar) as View?
                ?: return

        val backButton = navigationBarContainer.findViewById<ImageView>(R.id.toolbar_back_button)
        if (backButton != null) {
            CustomizationHelper.updateImageView(backButton,"cleeng_login_back_button" );
            backButton.setOnClickListener { this.finish() }
        }
    }


    fun dismissLoading() {
    }

    fun showLoading() {
    }


    fun showError(status: WebService.Status, response: String?) {
        var internalResponse = JSONObject(response)
        val localizationKeys = when (status) {
            WebService.Status.Unknown -> Arrays.asList(LOGIN_INTERNAL_ERROR_TITLE, LOGIN_INTERNAL_ERROR_MESSAGE)

            WebService.Status.WrongCredentials -> Arrays.asList(LOGIN_CREDENTAIL_ERROR_TITLE, LOGIN_CREDENTAIL_ERROR_MESSAGE)
            WebService.Status.InternalError -> when (internalResponse.get("code")) {
                3 -> Arrays.asList(LOGIN_CREDENTAIL_ERROR_TITLE, "Invalid publisher token")
                5 -> Arrays.asList(LOGIN_CREDENTAIL_ERROR_TITLE, "Access denied")
                10 -> Arrays.asList("cleeng_login_error_invalid_email_credentials_title", "cleeng_login_error_invalid_email_credentials_message")
                11 -> Arrays.asList(LOGIN_CREDENTAIL_ERROR_TITLE, "cleeng_login_error_credentials_message")
                12 -> Arrays.asList("cleeng_login_error_expired_title", "cleeng_login_error_expired_message")
                13 -> Arrays.asList("cleeng_login_error_existing_user_credentials_title", "cleeng_login_error_existing_user_credentials_message")
                15 -> Arrays.asList("cleeng_login_error_invalid_credentials_title", "cleeng_login_error_invalid_credentials_message")
                else -> {
                    Arrays.asList("cleeng_login_error_internal_title", "cleeng_login_error_internal_message")
                }
            }
            else -> return
        }

//        this.showAlertDialog(
//                ViewCustomization(
//                        textFont = "CleengLoginAlertTitle",
//                        localization = localizationKeys[0]),
//                ViewCustomization(
//                        textFont = "CleengLoginAlertDescription",
//                        localization = localizationKeys[1]),
//                {})
    }

//    fun showAlertDialog(titleCustomization: ViewCustomization, subtitleCustomization: ViewCustomization, callback: () -> Unit) {
//        val dialog = AlertDialog(this,
//                ViewCustomization(
//                        bgColor = "cleeng_login_alert_component"),
//                titleCustomization,
//                subtitleCustomization,
//                ViewCustomization(
//                        bgImage = "cleeng_login_confirm_button",
//                        textFont = "CleengLoginConfirmButtonText",
//                        localization = "cleeng_login_confirm_button"),
//                callback)
//
//        dialog.show()
//    }


    fun getUserFromInput(): User? {
        val emailEditText = this.findViewById(R.id.input_email) as? EditText ?: return null
        val email = emailEditText.text.takeIf { it.isNotEmpty() } ?: return null
        val passwordEditText = this.findViewById(R.id.input_password) as? EditText ?: return null
        val password = passwordEditText.text.takeIf { it.isNotEmpty() } ?: return null

        return User(
                email.toString(),
                password.toString(),
                null,
                null)
    }

    protected fun fetchUserData(): User? {
        var user: User? = null
        FacebookUtil.loadUserInfo(object : AsyncTaskListener<APUser> {

            override fun handleException(e :Exception) {
            }

            override fun onTaskComplete(result : APUser) {
                user = User(
                        result.email,
                        null,
                        result.id,
                        null)

            }

            override fun onTaskStart() {

            }
        })

        return user
    }

}