package com.example.clicker

import android.graphics.drawable.Drawable

sealed class ShopItem {
    abstract val id: Int
    abstract val name: String
    abstract val price: Int
    abstract var isPurchased: Boolean

    data class FlagItem(
        override val id: Int,
        override val name: String,
        override val price: Int,
        val drawableResId: Int,
        override var isPurchased: Boolean = false,
        var isEquipped: Boolean = false
    ) : ShopItem()

    data class UpgradeItem(
        override val id: Int,
        override val name: String,
        override val price: Int,
        val iconResId: Int,
        val upgradeType: UpgradeType,
        val maxLevel: Int,
        override var isPurchased: Boolean = false,
        var currentLevel: Int = 0,
        var currentPrice: Int = price
    ) : ShopItem()
}

enum class UpgradeType {
    AUTO_CLICKER,       // Автоматический доход
    CLICK_MULTIPLIER,   // Множитель кликов
    INCOME_BOOST,       // Буст дохода
    ENERGY_CAPACITY,    // Ёмкость энергии
    ENERGY_REGEN,       // Регенерация энергии

}