package com.example.clicker

import android.content.Context
import android.content.SharedPreferences

class ShopManager(private val context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences("shop_data", Context.MODE_PRIVATE)

    // Список всех флагов
    val flags = listOf(
        ShopItem.FlagItem(
            id = 1,
            name = context.getString(R.string.default_flag),
            price = 0,
            drawableResId = R.drawable.flag_basic,
            isPurchased = true,
            isEquipped = true
        ),
        ShopItem.FlagItem(
            id = 2,
            name = context.getString(R.string.green_flag),
            price = 100,
            drawableResId = R.drawable.flag_green,
            isPurchased = false,
            isEquipped = false
        ),
        ShopItem.FlagItem(
            id = 3,
            name = context.getString(R.string.blue_flag),
            price = 250,
            drawableResId = R.drawable.flag_blue,
            isPurchased = false,
            isEquipped = false
        ),
        ShopItem.FlagItem(
            id = 4,
            name = context.getString(R.string.red_flag),
            price = 500,
            drawableResId = R.drawable.flag_red,
            isPurchased = false,
            isEquipped = false
        ),
        ShopItem.FlagItem(
            id = 5,
            name = context.getString(R.string.purple_flag),
            price = 1000,
            drawableResId = R.drawable.flag_purple,
            isPurchased = false,
            isEquipped = false
        )
    )

    // Список всех улучшений
    val upgrades = listOf(
        ShopItem.UpgradeItem(
            id = 101,
            name = context.getString(R.string.auto_clicker),
            price = 50,
            iconResId = R.drawable.ic_auto_clicker,
            upgradeType = UpgradeType.AUTO_CLICKER,
            maxLevel = 30,
            isPurchased = false,
            currentLevel = 0,
            currentPrice = 50
        ),
        ShopItem.UpgradeItem(
            id = 102,
            name = context.getString(R.string.click_multiplier),
            price = 100,
            iconResId = R.drawable.ic_click_multiplier,
            upgradeType = UpgradeType.CLICK_MULTIPLIER,
            maxLevel = 15,
            isPurchased = false,
            currentLevel = 0,
            currentPrice = 100
        ),
        ShopItem.UpgradeItem(
            id = 103,
            name = context.getString(R.string.income_boost),
            price = 200,
            iconResId = R.drawable.ic_income_boost,
            upgradeType = UpgradeType.INCOME_BOOST,
            maxLevel = 15,
            isPurchased = false,
            currentLevel = 0,
            currentPrice = 200
        )
    )

    val energyUpgrades = listOf(
        ShopItem.UpgradeItem(
            id = 104,
            name = context.getString(R.string.energy_capacity),
            price = 100,
            iconResId = R.drawable.ic_energy,
            upgradeType = UpgradeType.ENERGY_CAPACITY,
            maxLevel = 30, // 30 уровней для ёмкости
            isPurchased = false,
            currentLevel = 0,
            currentPrice = 100
        ),
        ShopItem.UpgradeItem(
            id = 105,
            name = context.getString(R.string.energy_regen),
            price = 200, // Делаем дороже
            iconResId = R.drawable.ic_energy,
            upgradeType = UpgradeType.ENERGY_REGEN,
            maxLevel = 10, // 10 уровней для регенерации
            isPurchased = false,
            currentLevel = 0,
            currentPrice = 200
        )
    )

    fun refreshShopState() {
        loadShopState()
    }

    // Загрузка состояния из SharedPreferences
    fun loadShopState() {
        // Загружаем флаги
        flags.forEach { flag ->
            if (flag.id != 1) { // Стандартный флаг всегда куплен
                flag.isPurchased = prefs.getBoolean("flag_${flag.id}_purchased", false)
                flag.isEquipped = prefs.getBoolean("flag_${flag.id}_equipped", false)
            }
        }

        // Загружаем обычные улучшения
        upgrades.forEach { upgrade ->
            upgrade.currentLevel = prefs.getInt("upgrade_${upgrade.id}_level", 0)
            upgrade.isPurchased = upgrade.currentLevel > 0
            upgrade.currentPrice = calculateUpgradePrice(upgrade)
        }

        // Загружаем энергетические улучшения
        energyUpgrades.forEach { upgrade ->
            upgrade.currentLevel = prefs.getInt("upgrade_${upgrade.id}_level", 0)
            upgrade.isPurchased = upgrade.currentLevel > 0
            upgrade.currentPrice = calculateUpgradePrice(upgrade)
        }

        // Загружаем текущий экипированный флаг
        val equippedFlagId = prefs.getInt("equipped_flag_id", 1)
        setEquippedFlag(equippedFlagId)
    }

    // Сохранение состояния в SharedPreferences
    fun saveShopState() {
        val editor = prefs.edit()

        // Сохраняем флаги
        flags.forEach { flag ->
            if (flag.id != 1) {
                editor.putBoolean("flag_${flag.id}_purchased", flag.isPurchased)
                editor.putBoolean("flag_${flag.id}_equipped", flag.isEquipped)
            }
        }

        // Сохраняем обычные улучшения
        upgrades.forEach { upgrade ->
            editor.putInt("upgrade_${upgrade.id}_level", upgrade.currentLevel)
        }

        // Сохраняем энергетические улучшения
        energyUpgrades.forEach { upgrade ->
            editor.putInt("upgrade_${upgrade.id}_level", upgrade.currentLevel)
        }

        // Сохраняем экипированный флаг
        val equippedFlag = flags.find { it.isEquipped }
        editor.putInt("equipped_flag_id", equippedFlag?.id ?: 1)

        editor.apply()
    }

    // Покупка флага
    fun purchaseFlag(flagId: Int, currentPoints: Int): Boolean {
        val flag = flags.find { it.id == flagId } ?: return false

        if (flag.isPurchased) return true

        if (currentPoints >= flag.price) {
            flag.isPurchased = true
            // Автоматически экипируем после покупки
            setEquippedFlag(flagId)
            saveShopState()  // Явное сохранение
            return true
        }

        return false
    }

    // Экипировка флага
    fun setEquippedFlag(flagId: Int): Boolean {
        val flag = flags.find { it.id == flagId } ?: return false

        if (!flag.isPurchased) return false

        // Снимаем экипировку со всех флагов
        flags.forEach { it.isEquipped = false }

        // Экипируем выбранный флаг
        flag.isEquipped = true

        saveShopState()
        return true
    }

    // Покупка улучшения
    fun purchaseUpgrade(upgradeId: Int, currentPoints: Int): Boolean {
        // Сначала ищем в обычных улучшениях
        var upgrade = upgrades.find { it.id == upgradeId }

        // Если не нашли, ищем в энергетических
        if (upgrade == null) {
            upgrade = energyUpgrades.find { it.id == upgradeId }
        }

        if (upgrade == null) return false

        if (upgrade.currentLevel >= upgrade.maxLevel) return false

        if (currentPoints >= upgrade.currentPrice) {
            upgrade.currentLevel++
            upgrade.isPurchased = true
            upgrade.currentPrice = calculateUpgradePrice(upgrade)
            saveShopState()
            return true
        }

        return false
    }

    // Расчет цены улучшения
    private fun calculateUpgradePrice(upgrade: ShopItem.UpgradeItem): Int {
        return when (upgrade.upgradeType) {
            UpgradeType.AUTO_CLICKER -> upgrade.price * (upgrade.currentLevel + 1)
            UpgradeType.CLICK_MULTIPLIER -> (upgrade.price * Math.pow(1.5, upgrade.currentLevel.toDouble())).toInt()
            UpgradeType.INCOME_BOOST -> upgrade.price * (upgrade.currentLevel + 1) * 2
            UpgradeType.ENERGY_CAPACITY -> {
                // Ёмкость: цена растет медленнее, т.к. уровней много (30)
                (upgrade.price * Math.pow(1.2, upgrade.currentLevel.toDouble())).toInt()
            }
            UpgradeType.ENERGY_REGEN -> {
                // Регенерация: цена растет быстрее, т.к. уровней мало (10) и это мощное улучшение
                (upgrade.price * Math.pow(1.8, upgrade.currentLevel.toDouble())).toInt()
            }
        }
    }

    // Добавим методы для получения информации об энергии
    fun getEnergyCapacityLevel(): Int {
        return energyUpgrades.find { it.upgradeType == UpgradeType.ENERGY_CAPACITY }?.currentLevel ?: 0
    }

    fun getEnergyRegenLevel(): Int {
        return energyUpgrades.find { it.upgradeType == UpgradeType.ENERGY_REGEN }?.currentLevel ?: 0
    }

    fun getAllEnergyUpgrades(): List<ShopItem.UpgradeItem> {
        return energyUpgrades
    }

    // Получение текущего экипированного флага
    fun getEquippedFlag(): ShopItem.FlagItem {
        return flags.find { it.isEquipped } ?: flags.first()
    }

    // Получение текущего множителя кликов
    fun getClickMultiplier(): Double {
        val upgrade = upgrades.find { it.upgradeType == UpgradeType.CLICK_MULTIPLIER }
        return 1.0 + (upgrade?.currentLevel?.toDouble() ?: 0.0) * 0.5
    }

    // Получение текущего автодохода
    fun getAutoIncome(): Int {
        val autoClicker = upgrades.find { it.upgradeType == UpgradeType.AUTO_CLICKER }
        val incomeBoost = upgrades.find { it.upgradeType == UpgradeType.INCOME_BOOST }

        val autoIncome = (autoClicker?.currentLevel ?: 0) * 1
        val boostIncome = (incomeBoost?.currentLevel ?: 0) * 2

        return autoIncome + boostIncome
    }

    // Сброс всех покупок (для тестирования)
    fun resetPurchases() {
        val editor = prefs.edit()
        editor.clear()
        editor.apply()
        loadShopState()
    }
}