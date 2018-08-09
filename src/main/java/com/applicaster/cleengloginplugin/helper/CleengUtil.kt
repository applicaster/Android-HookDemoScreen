package com.applicaster.cleengloginplugin.helper

import android.util.Base64
import com.applicaster.cleengloginplugin.models.User
import com.applicaster.util.PreferenceUtil
import com.applicaster.util.StringUtil
import com.applicaster.util.serialization.SerializationUtils
import org.json.JSONObject
import java.io.UnsupportedEncodingException
import java.nio.charset.Charset

class CleengUtil{
    companion object {
        @JvmStatic
        fun isTokenValid(jwtToke: String?): Boolean {
            var tokenEncoded = (jwtToke?.split(".") as List<String>)[1]
            val dataDec = Base64.decode(tokenEncoded, Base64.DEFAULT)
            try {
                var decodedString = String(dataDec, Charset.forName("UTF-8"))
                val jwt = JSONObject(decodedString)
                var expr = jwt.getLong("exp")
                var currentTime = System.currentTimeMillis()

                if(expr * 1000 > currentTime){
                    return true
                }

            } catch (e: UnsupportedEncodingException) {
                e.printStackTrace()
            }
            return false;
        }


        @JvmStatic
        fun getUser(): User? {
            var userJson = PreferenceUtil.getInstance().getStringPref("USER", "");
            if ("null".equals(userJson) || StringUtil.isEmpty(userJson)) return null;
            var user = JSONObject(userJson)

            return User(user.getString("email"),null, user.getString("facebookId"), user.getString("token"), null)
        }

        @JvmStatic
        fun setUser(user: User?) {
            var userJson = SerializationUtils.toJson(user,User ::class.java)
            PreferenceUtil.getInstance().setStringPref("USER", userJson);
        }
    }
}