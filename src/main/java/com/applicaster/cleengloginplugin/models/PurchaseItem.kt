package com.applicaster.cleengloginplugin.models

data class PurchaseItem(val token: String,
                   val sku: String,
                   val signature: String,
                   val purchaseTime: Long,
                   val purchaseState: Int,
                   val packageName: String,
                   val originalJson: String,
                   val orderId: String,
                   val itemType: String,
                   val developerPayload: String) {

    class Builder {
        private lateinit var token: String
        private var sku: String = ""
        private var signature: String = ""
        private var purchaseTime: Long = -1
        private var purchaseState: Int = -1
        private var packageName: String = ""
        private var originalJson: String = ""
        private var orderId: String = ""
        private var itemType: String = ""
        private var developerPayload: String = ""


        fun token(token: String) = apply { this.token = token }
        fun sku(sku: String) = apply { this.sku = sku }
        fun signature(signature: String) = apply { this.signature = signature }
        fun purchaseTime(purchaseTime: Long) = apply { this.purchaseTime = purchaseTime }
        fun purchaseState(purchaseState: Int) = apply { this.purchaseState = purchaseState }
        fun packageName(packageName: String) = apply { this.packageName = packageName }
        fun originalJson(originalJson: String) = apply { this.originalJson = originalJson }
        fun orderId(orderId: String) = apply { this.orderId = orderId }
        fun itemType(itemType: String) = apply { this.itemType = itemType }
        fun developerPayload(developerPayload: String) = apply { this.developerPayload = developerPayload }


        fun build() = PurchaseItem(
                token,
                sku,
                signature,
                purchaseTime,
                purchaseState,
                packageName,
                originalJson,
                orderId,
                itemType,
                developerPayload
        )
    }
}