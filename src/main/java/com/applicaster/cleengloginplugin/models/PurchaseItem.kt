package com.applicaster.cleengloginplugin.models

class PurchaseItem(val token: String,
                   val sku: String,
                   val signature: String,
                   val purchaseTime: Long,
                   val purchaseState: Int,
                   val packageName: String,
                   val originalJson: String,
                   val orderId: String,
                   val itemType: String,
                   val developerPayload: String)