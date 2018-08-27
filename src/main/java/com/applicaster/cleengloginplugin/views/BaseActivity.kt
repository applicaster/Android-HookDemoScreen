package com.applicaster.cleengloginplugin.views

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import com.applicaster.cleengloginplugin.*
import com.applicaster.cleengloginplugin.R
import com.applicaster.cleengloginplugin.helper.CleengManager
import com.applicaster.cleengloginplugin.helper.CustomizationHelper
import com.applicaster.cleengloginplugin.models.User
import com.applicaster.cleengloginplugin.remote.WebService
import com.applicaster.model.APUser
import com.applicaster.plugin_manager.login.LoginManager
import com.applicaster.plugin_manager.playersmanager.Playable
import com.applicaster.util.FacebookUtil
import com.applicaster.util.asynctask.AsyncTaskListener
import kotlinx.android.synthetic.main.login_activity.*
import org.json.JSONObject
import java.util.*

abstract class BaseActivity : AppCompatActivity() {

    var playable: Playable? = null
    private lateinit var progressBar: View

    abstract fun getContentViewResId(): Int

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        playable = intent.getSerializableExtra(PLAYABLE) as Playable?

        this.setContentView(this.getContentViewResId())
        progressBar = findViewById(R.id.progress_bar)
        this.customize()
    }

    open fun customize() {
        CustomizationHelper.updateBgResource(this,R.id.background_image, "cleeng_login_background_image")
        this.customizeNavigationBar()
    }

    fun customizeNavigationBar() {
        val navigationBarContainer = this.findViewById(R.id.toolbar) as View?
                ?: return

        val backButton = navigationBarContainer.findViewById<ImageView>(R.id.toolbar_back_button)
        if (backButton != null) {
            CustomizationHelper.updateImageView(backButton,"cleeng_login_back_button" )
            backButton.setOnClickListener {
                closeLoginPluginFromHook()
                this.finish()
            }
        }
        val closeButton = navigationBarContainer.findViewById<ImageView>(R.id.toolbar_close_button)
        if (closeButton != null) {
            CustomizationHelper.updateImageView(closeButton,"cleeng_login_close_button" )
            closeButton.setOnClickListener {
                closeLoginPluginFromHook()
                this.finish()
            }
        }
    }

    /***
     * When login launched from Hook we should notify that login is closed in order to continue the into flow by calling hookFinish
     */
    private fun closeLoginPluginFromHook() {
        LoginManager.notifyEvent(this, LoginManager.RequestType.CLOSED, false);
    }


    fun dismissLoading() {
        progressBar.visibility = View.GONE
    }

    fun showLoading() {
        progressBar.visibility = View.VISIBLE
    }

    /**
     * check if login launched with playable item, if yes and the user have offer that comply the playable
     * return true, else return false.
     */
    fun isPlayableSupported(): Boolean {
        if(playable == null) return true;
        return CleengManager.isItemLocked(playable)
    }

    fun showError(status: WebService.Status, response: String?) {
        val internalResponse = JSONObject(response)
        val errorCode = if(internalResponse.has("code")) internalResponse.get("code") else -1

        val localizationKeys = when (status) {
            WebService.Status.Unknown -> Arrays.asList(LOGIN_INTERNAL_ERROR_TITLE, LOGIN_INTERNAL_ERROR_MESSAGE)
            WebService.Status.WrongCredentials -> Arrays.asList(LOGIN_CREDENTAIL_ERROR_TITLE, LOGIN_CREDENTAIL_ERROR_MESSAGE)
            WebService.Status.InternalError -> when (errorCode) {
                3 -> Arrays.asList(LOGIN_CREDENTAIL_ERROR_TITLE, "Invalid publisher token")
                5 -> Arrays.asList(LOGIN_CREDENTAIL_ERROR_TITLE, "Access denied")
                10 -> Arrays.asList("cleeng_login_error_invalid_email_credentials_title", "cleeng_login_error_invalid_email_credentials_message")
                11 -> Arrays.asList(LOGIN_CREDENTAIL_ERROR_TITLE, "cleeng_login_error_credentials_message")
                12 -> Arrays.asList("cleeng_login_error_expired_title", "cleeng_login_error_expired_message")
                13 -> Arrays.asList("cleeng_login_error_existing_user_credentials_title", "cleeng_login_error_existing_user_credentials_message")
                15 -> Arrays.asList("cleeng_login_error_invalid_credentials_title", "cleeng_login_error_invalid_credentials_message")
                else -> Arrays.asList("cleeng_login_error_internal_title", "cleeng_login_error_internal_message")
            }
            else -> return
        }

        this.showAlertDialog(localizationKeys[0], localizationKeys[1],{})
    }

    fun showAlertDialog(title: String, subtitle: String, callback: () -> Unit) {
        val dialog = AlertDialog(this, title, subtitle, callback)
        dialog.show()
    }


    fun getUserFromInput(): User? {
        val emailEditText = this.findViewById(R.id.input_email) as? EditText ?: return null
        val email = emailEditText.text.takeIf { it.isNotEmpty() } ?: return null
        val passwordEditText = this.findViewById(R.id.input_password) as? EditText ?: return null
        val password = passwordEditText.text.takeIf { it.isNotEmpty() } ?: return null

        return User(
                email.toString(),
                password.toString(),
                null,
                null, null, null)
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
                        null, null, null)

            }

            override fun onTaskStart() {

            }
        })

        return user
    }

    override fun onBackPressed() {
        super.onBackPressed()
        closeLoginPluginFromHook();
    }
}