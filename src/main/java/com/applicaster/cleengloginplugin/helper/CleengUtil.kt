package com.applicaster.cleengloginplugin.helper

import android.util.Base64
import android.util.Log
import com.applicaster.cleengloginplugin.models.Offer
import com.applicaster.cleengloginplugin.models.Subscription
import com.applicaster.cleengloginplugin.models.User
import com.applicaster.util.PreferenceUtil
import com.applicaster.util.StringUtil
import com.applicaster.util.serialization.SerializationUtils
import org.json.JSONException
import org.json.JSONObject
import java.io.UnsupportedEncodingException
import java.nio.charset.Charset

class CleengUtil {

    companion object {
        @JvmStatic
        fun isTokenValid(jwtToken: String?): Boolean {
            val tokenEncoded = (jwtToken?.split(".") as List<String>)[1]
            val dataDec = Base64.decode(tokenEncoded, Base64.DEFAULT)
            try {
                val decodedString = String(dataDec, Charset.forName("UTF-8"))
                val jwt = JSONObject(decodedString)
                val expr = jwt.getLong("exp")
                val currentTime = System.currentTimeMillis()

                if (expr * 1000 > currentTime) {
                    return true
                }

            } catch (e: UnsupportedEncodingException) {
                e.printStackTrace()
            }
            return false
        }

        @JvmStatic
        fun getUser(): User? {
            val userJson = PreferenceUtil.getInstance().getStringPref("USER", "")

            if (StringUtil.isNotEmpty(userJson)) {
                
                try {
                    val user = JSONObject(userJson)
                    val userOfferJson = user.getJSONArray("userOffers")
                    val offers: ArrayList<Offer> = ArrayList()            //save the 'empty' token.
                    for (i in 0 until userOfferJson.length()) {

                        with(userOfferJson.getJSONObject(i)) {

                            offers.add(Offer(
                                    getString("offerID"),
                                    getString("token"),
                                    getString("authId")))
                        }
                    }

                    return User(
                            user.getString("email"),
                            null,
                            user.optString("facebookId"),
                            user.getString("token"),
                            offers)

                } catch (e: JSONException) {
                    Log.d("CleengUtil", e.toString())
                }
            }

            return null
        }

        @JvmStatic
        fun setUser(user: User?) {
            val userJson = SerializationUtils.toJson(user, User::class.java)
            PreferenceUtil.getInstance().setStringPref("USER", userJson)
        }

        @JvmStatic
        fun addSubscriptionToUser(subscription: Subscription) {
            getUser()?.ownedSubscriptions?.add(subscription)
        }
    }
}
