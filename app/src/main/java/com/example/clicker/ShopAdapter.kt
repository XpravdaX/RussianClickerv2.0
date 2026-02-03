package com.example.clicker

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView

class ShopAdapter(
    private val flags: List<ShopItem.FlagItem>,
    private val upgrades: List<ShopItem.UpgradeItem>,
    private val energyUpgrades: List<ShopItem.UpgradeItem>,
    private val shopManager: ShopManager,
    private val onUpdatePoints: (itemPrice: Int) -> Boolean,
    private val onFlagChanged: (flagId: Int) -> Unit
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        private const val TYPE_FLAGS = 0
        private const val TYPE_UPGRADES = 1
        private const val TYPE_ENERGY = 2
    }

    abstract class ShopViewHolder(view: View) : RecyclerView.ViewHolder(view)

    class FlagsViewHolder(view: View) : ShopViewHolder(view) {
        val flag1Status: TextView = view.findViewById(R.id.flag1_status)
        val buyFlag2: Button = view.findViewById(R.id.buy_flag2)
        val buyFlag3: Button = view.findViewById(R.id.buy_flag3)
        val buyFlag4: Button = view.findViewById(R.id.buy_flag4)
        val buyFlag5: Button = view.findViewById(R.id.buy_flag5)
    }

    class UpgradesViewHolder(view: View) : ShopViewHolder(view) {
        val upgrade1Desc: TextView = view.findViewById(R.id.upgrade1_desc)
        val upgrade1Cost: TextView = view.findViewById(R.id.upgrade1_cost)
        val upgrade1Level: TextView = view.findViewById(R.id.upgrade1_level)
        val buyUpgrade1: TextView = view.findViewById(R.id.buy_upgrade1)

        val upgrade2Desc: TextView = view.findViewById(R.id.upgrade2_desc)
        val upgrade2Cost: TextView = view.findViewById(R.id.upgrade2_cost)
        val upgrade2Level: TextView = view.findViewById(R.id.upgrade2_level)
        val buyUpgrade2: TextView = view.findViewById(R.id.buy_upgrade2)

        val upgrade3Desc: TextView = view.findViewById(R.id.upgrade3_desc)
        val upgrade3Cost: TextView = view.findViewById(R.id.upgrade3_cost)
        val upgrade3Level: TextView = view.findViewById(R.id.upgrade3_level)
        val buyUpgrade3: TextView = view.findViewById(R.id.buy_upgrade3)
    }

    class EnergyViewHolder(view: View) : ShopViewHolder(view) {
        // Ёмкость энергии
        val energyCapacityDesc: TextView = view.findViewById(R.id.energy_capacity_desc)
        val energyCapacityCost: TextView = view.findViewById(R.id.energy_capacity_cost)
        val energyCapacityLevel: TextView = view.findViewById(R.id.energy_capacity_level)
        val buyEnergyCapacity: TextView = view.findViewById(R.id.buy_energy_capacity)

        // Регенерация энергии
        val energyRegenDesc: TextView = view.findViewById(R.id.energy_regen_desc)
        val energyRegenCost: TextView = view.findViewById(R.id.energy_regen_cost)
        val energyRegenLevel: TextView = view.findViewById(R.id.energy_regen_level)
        val buyEnergyRegen: TextView = view.findViewById(R.id.buy_energy_regen)
    }

    override fun getItemViewType(position: Int): Int {
        return when (position) {
            0 -> TYPE_FLAGS
            1 -> TYPE_UPGRADES
            2 -> TYPE_ENERGY
            else -> TYPE_FLAGS
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ShopViewHolder {
        return when (viewType) {
            TYPE_FLAGS -> {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.tab_flags, parent, false)
                FlagsViewHolder(view)
            }
            TYPE_UPGRADES -> {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.tab_upgrades, parent, false)
                UpgradesViewHolder(view)
            }
            TYPE_ENERGY -> {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.tab_energy, parent, false)
                EnergyViewHolder(view)
            }
            else -> throw IllegalArgumentException("Invalid view type")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is FlagsViewHolder -> bindFlagsTab(holder)
            is UpgradesViewHolder -> bindUpgradesTab(holder)
            is EnergyViewHolder -> bindEnergyTab(holder)
        }
    }

    private fun bindFlagsTab(holder: FlagsViewHolder) {
        // Существующий код для флагов...
        val context = holder.itemView.context

        // Флаг 1 (стандартный) - всегда доступен
        updateFlagButton(holder.flag1Status, flags[0])

        // Обработчик для флага 1
        holder.flag1Status.setOnClickListener {
            if (shopManager.setEquippedFlag(flags[0].id)) {
                updateAllFlagButtons(holder)
                onFlagChanged(flags[0].id)
            }
        }

        // Флаг 2
        val flag2 = flags[1]
        updateFlagButton(holder.buyFlag2, flag2)
        holder.buyFlag2.setOnClickListener {
            if (flag2.isPurchased) {
                // Экипировать
                if (shopManager.setEquippedFlag(flag2.id)) {
                    updateAllFlagButtons(holder)
                    onFlagChanged(flag2.id)
                    shopManager.saveShopState()
                }
            } else {
                // Купить
                if (onUpdatePoints(flag2.price)) {
                    if (shopManager.purchaseFlag(flag2.id, flag2.price)) {
                        // Автоматически экипируем после покупки
                        shopManager.setEquippedFlag(flag2.id)
                        updateAllFlagButtons(holder)
                        onFlagChanged(flag2.id)
                        shopManager.saveShopState()
                        Toast.makeText(context, "Флаг куплен и экипирован!", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(context, context.getString(R.string.not_enough_points), Toast.LENGTH_SHORT).show()
                }
            }
        }

        // Флаг 3
        val flag3 = flags[2]
        updateFlagButton(holder.buyFlag3, flag3)
        holder.buyFlag3.setOnClickListener {
            if (flag3.isPurchased) {
                if (shopManager.setEquippedFlag(flag3.id)) {
                    updateAllFlagButtons(holder)
                    onFlagChanged(flag3.id)
                    shopManager.saveShopState()
                }
            } else {
                if (onUpdatePoints(flag3.price)) {
                    if (shopManager.purchaseFlag(flag3.id, flag3.price)) {
                        shopManager.setEquippedFlag(flag3.id)
                        updateAllFlagButtons(holder)
                        shopManager.saveShopState()
                        onFlagChanged(flag3.id)
                        Toast.makeText(context, "Флаг куплен и экипирован!", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(context, context.getString(R.string.not_enough_points), Toast.LENGTH_SHORT).show()
                }
            }
        }

        // Флаг 4
        val flag4 = flags[3]
        updateFlagButton(holder.buyFlag4, flag4)
        holder.buyFlag4.setOnClickListener {
            if (flag4.isPurchased) {
                if (shopManager.setEquippedFlag(flag4.id)) {
                    updateAllFlagButtons(holder)
                    onFlagChanged(flag4.id)
                    shopManager.saveShopState()
                }
            } else {
                if (onUpdatePoints(flag4.price)) {
                    if (shopManager.purchaseFlag(flag4.id, flag4.price)) {
                        shopManager.setEquippedFlag(flag4.id)
                        updateAllFlagButtons(holder)
                        shopManager.saveShopState()
                        onFlagChanged(flag4.id)
                        Toast.makeText(context, "Флаг куплен и экипирован!", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(context, context.getString(R.string.not_enough_points), Toast.LENGTH_SHORT).show()
                }
            }
        }

        // Флаг 5
        val flag5 = flags[4]
        updateFlagButton(holder.buyFlag5, flag5)
        holder.buyFlag5.setOnClickListener {
            if (flag5.isPurchased) {
                if (shopManager.setEquippedFlag(flag5.id)) {
                    updateAllFlagButtons(holder)
                    onFlagChanged(flag5.id)
                    shopManager.saveShopState()
                }
            } else {
                if (onUpdatePoints(flag5.price)) {
                    if (shopManager.purchaseFlag(flag5.id, flag5.price)) {
                        shopManager.setEquippedFlag(flag5.id)
                        updateAllFlagButtons(holder)
                        shopManager.saveShopState()
                        onFlagChanged(flag5.id)
                        Toast.makeText(context, "Флаг куплен и экипирован!", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(context, context.getString(R.string.not_enough_points), Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun updateAllFlagButtons(holder: FlagsViewHolder) {
        // Обновляем все кнопки флагов
        updateFlagButton(holder.flag1Status, flags[0])

        for (i in 1..4) {
            val flag = flags[i]
            val button = when (i) {
                1 -> holder.buyFlag2
                2 -> holder.buyFlag3
                3 -> holder.buyFlag4
                4 -> holder.buyFlag5
                else -> continue
            }
            updateFlagButton(button, flag)
        }
    }

    private fun updateFlagButton(button: TextView, flag: ShopItem.FlagItem) {
        val context = button.context

        if (flag.isPurchased) {
            if (flag.isEquipped) {
                button.text = "Экипировано"
                button.setTextColor(context.getColor(R.color.success_color))
                button.textSize = 16f
                button.isEnabled = false
                button.setBackgroundColor(context.getColor(android.R.color.transparent))
            } else {
                button.text = "Экипировать"
                button.setTextColor(context.getColor(R.color.text_primary))
                button.textSize = 14f
                button.isEnabled = true
                button.setBackgroundResource(R.drawable.button_border)
            }
        } else {
            button.text = "${context.getString(R.string.buy_button)} (${flag.price})"
            button.setTextColor(context.getColor(R.color.text_primary))
            button.textSize = 14f
            button.isEnabled = true
            button.setBackgroundResource(R.drawable.button_border)
        }
    }

    private fun bindUpgradesTab(holder: UpgradesViewHolder) {
        val context = holder.itemView.context

        // Улучшение 1 (Автокликер)
        val upgrade1 = upgrades[0]
        updateUpgradeDisplay(holder, 0, upgrade1)

        holder.buyUpgrade1.setOnClickListener {
            if (onUpdatePoints(upgrade1.currentPrice)) {
                if (shopManager.purchaseUpgrade(upgrade1.id, upgrade1.currentPrice)) {
                    updateUpgradeDisplay(holder, 0, upgrade1)
                    Toast.makeText(context, "Улучшение куплено!", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(context, context.getString(R.string.not_enough_points), Toast.LENGTH_SHORT).show()
            }
        }

        // Улучшение 2 (Множитель кликов)
        val upgrade2 = upgrades[1]
        updateUpgradeDisplay(holder, 1, upgrade2)

        holder.buyUpgrade2.setOnClickListener {
            if (onUpdatePoints(upgrade2.currentPrice)) {
                if (shopManager.purchaseUpgrade(upgrade2.id, upgrade2.currentPrice)) {
                    updateUpgradeDisplay(holder, 1, upgrade2)
                    Toast.makeText(context, "Улучшение куплено!", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(context, context.getString(R.string.not_enough_points), Toast.LENGTH_SHORT).show()
            }
        }

        // Улучшение 3 (Буст доходов)
        val upgrade3 = upgrades[2]
        updateUpgradeDisplay(holder, 2, upgrade3)

        holder.buyUpgrade3.setOnClickListener {
            if (onUpdatePoints(upgrade3.currentPrice)) {
                if (shopManager.purchaseUpgrade(upgrade3.id, upgrade3.currentPrice)) {
                    updateUpgradeDisplay(holder, 2, upgrade3)
                    Toast.makeText(context, "Улучшение куплено!", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(context, context.getString(R.string.not_enough_points), Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun bindEnergyTab(holder: EnergyViewHolder) {
        val context = holder.itemView.context

        // Ёмкость энергии
        val capacityUpgrade = energyUpgrades[0]
        updateEnergyUpgradeDisplay(holder, 0, capacityUpgrade)

        holder.buyEnergyCapacity.setOnClickListener {
            if (onUpdatePoints(capacityUpgrade.currentPrice)) {
                if (shopManager.purchaseUpgrade(capacityUpgrade.id, capacityUpgrade.currentPrice)) {
                    updateEnergyUpgradeDisplay(holder, 0, capacityUpgrade)
                    Toast.makeText(context, "Ёмкость энергии улучшена!", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(context, context.getString(R.string.not_enough_points), Toast.LENGTH_SHORT).show()
            }
        }

        // Регенерация энергии
        val regenUpgrade = energyUpgrades[1]
        updateEnergyUpgradeDisplay(holder, 1, regenUpgrade)

        holder.buyEnergyRegen.setOnClickListener {
            if (onUpdatePoints(regenUpgrade.currentPrice)) {
                if (shopManager.purchaseUpgrade(regenUpgrade.id, regenUpgrade.currentPrice)) {
                    updateEnergyUpgradeDisplay(holder, 1, regenUpgrade)
                    Toast.makeText(context, "Регенерация улучшена!", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(context, context.getString(R.string.not_enough_points), Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun updateEnergyUpgradeDisplay(holder: EnergyViewHolder, index: Int, upgrade: ShopItem.UpgradeItem) {
        val context = holder.itemView.context
        val button: TextView
        val descText: String
        val costText: String
        val levelText: String

        when (index) {
            0 -> { // Ёмкость энергии
                button = holder.buyEnergyCapacity
                val bonusCapacity = upgrade.currentLevel * 25 // +25 за уровень
                descText = context.getString(R.string.energy_upgrade_desc, bonusCapacity)
                costText = context.getString(R.string.upgrade_cost, upgrade.currentPrice)
                levelText = context.getString(R.string.upgrade_level, upgrade.currentLevel, upgrade.maxLevel)
                holder.energyCapacityDesc.text = descText
                holder.energyCapacityCost.text = costText
                holder.energyCapacityLevel.text = levelText
            }
            1 -> { // Регенерация энергии
                button = holder.buyEnergyRegen
                val regenTime = maxOf(1, 5 - upgrade.currentLevel) // 5,4,3,2,1 сек
                descText = context.getString(R.string.energy_regen_desc, regenTime)
                costText = context.getString(R.string.upgrade_cost, upgrade.currentPrice)
                levelText = context.getString(R.string.upgrade_level, upgrade.currentLevel, upgrade.maxLevel)
                holder.energyRegenDesc.text = descText
                holder.energyRegenCost.text = costText
                holder.energyRegenLevel.text = levelText
            }
            else -> return
        }

        // Обновляем кнопку
        if (upgrade.currentLevel >= upgrade.maxLevel) {
            button.text = context.getString(R.string.max_level)
            button.setTextColor(context.getColor(R.color.success_color))
            button.textSize = 14f
            button.isEnabled = false
            button.setBackgroundColor(context.getColor(android.R.color.transparent))
        } else {
            button.text = "${context.getString(R.string.buy_button)} (${upgrade.currentPrice})"
            button.setTextColor(context.getColor(R.color.text_primary))
            button.textSize = 14f
            button.isEnabled = true
            button.setBackgroundResource(R.drawable.button_border)
        }
    }

    private fun updateUpgradeDisplay(holder: UpgradesViewHolder, index: Int, upgrade: ShopItem.UpgradeItem) {
        val context = holder.itemView.context
        val button: TextView
        val descText: String
        val costText: String
        val levelText: String

        when (index) {
            0 -> {
                button = holder.buyUpgrade1
                descText = context.getString(R.string.auto_clicker_desc, upgrade.currentLevel + 1)
                costText = context.getString(R.string.upgrade_cost, upgrade.currentPrice)
                levelText = context.getString(R.string.upgrade_level, upgrade.currentLevel, upgrade.maxLevel)
                holder.upgrade1Desc.text = descText
                holder.upgrade1Cost.text = costText
                holder.upgrade1Level.text = levelText
            }
            1 -> {
                button = holder.buyUpgrade2
                val multiplierValue = 1.0 + upgrade.currentLevel * 0.5
                descText = context.getString(R.string.click_multiplier_desc, multiplierValue)
                costText = context.getString(R.string.upgrade_cost, upgrade.currentPrice)
                levelText = context.getString(R.string.upgrade_level, upgrade.currentLevel, upgrade.maxLevel)
                holder.upgrade2Desc.text = descText
                holder.upgrade2Cost.text = costText
                holder.upgrade2Level.text = levelText
            }
            2 -> {
                button = holder.buyUpgrade3
                descText = context.getString(R.string.income_boost_desc, (upgrade.currentLevel + 1) * 2)
                costText = context.getString(R.string.upgrade_cost, upgrade.currentPrice)
                levelText = context.getString(R.string.upgrade_level, upgrade.currentLevel, upgrade.maxLevel)
                holder.upgrade3Desc.text = descText
                holder.upgrade3Cost.text = costText
                holder.upgrade3Level.text = levelText
            }
            else -> return
        }

        // Обновляем кнопку
        if (upgrade.currentLevel >= upgrade.maxLevel) {
            button.text = context.getString(R.string.max_level)
            button.setTextColor(context.getColor(R.color.success_color))
            button.textSize = 14f
            button.isEnabled = false
            button.setBackgroundColor(context.getColor(android.R.color.transparent))
        } else {
            button.text = "${context.getString(R.string.buy_button)} (${upgrade.currentPrice})"
            button.setTextColor(context.getColor(R.color.text_primary))
            button.textSize = 14f
            button.isEnabled = true
            button.setBackgroundResource(R.drawable.button_border)
        }
    }

    override fun getItemCount(): Int = 3 // Теперь 3 вкладки
}