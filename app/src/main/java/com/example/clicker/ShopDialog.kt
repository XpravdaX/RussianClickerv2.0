package com.example.clicker

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator

class ShopDialog(
    context: Context,
    private var currentPoints: Int,
    private val shopManager: ShopManager,
    private val onPointsUpdated: (spentPoints: Int) -> Unit,
    private val onFlagChanged: () -> Unit
) : Dialog(context) {

    private lateinit var pointsTextView: TextView
    private lateinit var tabLayout: TabLayout
    private lateinit var viewPager: ViewPager2
    private lateinit var closeButton: Button

    private lateinit var adapter: ShopAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_shop_dialog)

        // Настраиваем диалог
        window?.setBackgroundDrawableResource(android.R.color.transparent)
        setCancelable(true)

        initViews()
        setupViewPager()
        updatePointsDisplay()
    }

    private fun initViews() {
        pointsTextView = findViewById(R.id.current_points)
        tabLayout = findViewById(R.id.tab_layout)
        viewPager = findViewById(R.id.shop_viewpager)
        closeButton = findViewById(R.id.close_button)

        closeButton.setOnClickListener {
            dismiss()
        }
    }

    private fun setupViewPager() {
        adapter = ShopAdapter(
            flags = shopManager.flags,
            upgrades = shopManager.upgrades,
            energyUpgrades = shopManager.getAllEnergyUpgrades(),
            shopManager = shopManager,
            onUpdatePoints = { itemPrice ->
                // Проверяем, достаточно ли очков
                if (currentPoints >= itemPrice) {
                    // Списываем очки
                    currentPoints -= itemPrice
                    updatePointsDisplay()

                    // Уведомляем MainActivity об обновлении очков
                    onPointsUpdated(itemPrice)

                    true // Покупка возможна
                } else {
                    // Недостаточно очков
                    Toast.makeText(context, context.getString(R.string.not_enough_points), Toast.LENGTH_SHORT).show()
                    false // Покупка невозможна
                }
            },
            onFlagChanged = {
                onFlagChanged()
                updatePointsDisplay()
            }
        )

        viewPager.adapter = adapter

        // Связываем TabLayout с ViewPager2
        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            tab.text = when (position) {
                0 -> context.getString(R.string.flags_tab)
                1 -> context.getString(R.string.upgrades_tab)
                2 -> "Энергия" // Новая вкладка
                else -> ""
            }
        }.attach()
    }

    private fun updatePointsDisplay() {
        pointsTextView.text = context.getString(R.string.current_points, currentPoints)
    }

    fun updateCurrentPoints(points: Int) {
        currentPoints = points
        updatePointsDisplay()
        adapter.notifyDataSetChanged() // Обновляем кнопки покупки
    }
}