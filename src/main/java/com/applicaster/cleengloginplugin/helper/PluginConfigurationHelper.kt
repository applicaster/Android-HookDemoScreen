package com.applicaster.cleengloginplugin.helper;

object PluginConfigurationHelper {
    private var plguinConfiguration: Map<String, String>? = null

    public fun setConfigurationMap(plguinConfiguration: Map<String, String>?) {
            this.plguinConfiguration = plguinConfiguration
            }

    public fun getConfigurationValue(key: String): String? {
            return this.plguinConfiguration?.get(key)
            }

}