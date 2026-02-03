package com.example.clicker

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator

class ShopDialogFragment : DialogFragment() {

    private lateinit var shopManager: ShopManager
    private lateinit var adapter: ShopAdapter
    private var currentPoints: Int = 0

    private lateinit var pointsTextView: TextView
    private lateinit var tabLayout: TabLayout
    private lateinit var viewPager: ViewPager2
    private lateinit var closeButton: Button

    var onPointsUpdated: ((Int) -> Unit)? = null
    var onFlagChanged: ((Int) -> Unit)? = null // Изменено: передаем ID флага
    var onShopStateUpdated: ((ShopManager) -> Unit)? = null  // Добавьте этот callback

    companion object {
        private const val ARG_POINTS = "points"

        fun newInstance(points: Int): ShopDialogFragment {
            val fragment = ShopDialogFragment()
            val args = Bundle()
            args.putInt(ARG_POINTS, points)
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        currentPoints = arguments?.getInt(ARG_POINTS) ?: 0
        shopManager = ShopManager(requireContext())
        shopManager.loadShopState()
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = AlertDialog.Builder(requireContext())
        val inflater = LayoutInflater.from(requireContext())
        val view = inflater.inflate(R.layout.activity_shop_dialog, null)

        initViews(view)
        setupViewPager()
        updatePointsDisplay()

        builder.setView(view)
        return builder.create()
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.setLayout(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.WRAP_CONTENT
        )
        dialog?.window?.setBackgroundDrawableResource(android.R.color.transparent)
    }

    private fun initViews(view: View) {
        pointsTextView = view.findViewById(R.id.current_points)
        tabLayout = view.findViewById(R.id.tab_layout)
        viewPager = view.findViewById(R.id.shop_viewpager)
        closeButton = view.findViewById(R.id.close_button)

        closeButton.setOnClickListener {
            dismiss()
        }
    }

    private fun setupViewPager() {
        adapter = ShopAdapter(
            flags = shopManager.flags,
            upgrades = shopManager.upgrades,
            energyUpgrades = shopManager.getAllEnergyUpgrades(), // Добавляем энергетические улучшения
            shopManager = shopManager,
            onUpdatePoints = { itemPrice ->
                if (currentPoints >= itemPrice) {
                    currentPoints -= itemPrice
                    updatePointsDisplay()
                    onPointsUpdated?.invoke(itemPrice)

                    // Обновляем состояние магазина
                    shopManager.loadShopState()  // Перезагружаем состояние
                    onShopStateUpdated?.invoke(shopManager)  // Уведомляем MainActivity

                    true
                } else {
                    false
                }
            },
            onFlagChanged = { flagId ->
                onFlagChanged?.invoke(flagId)
                updatePointsDisplay()

                // Обновляем состояние магазина
                shopManager.loadShopState()
                onShopStateUpdated?.invoke(shopManager)
            }
        )

        viewPager.adapter = adapter

        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            tab.text = when (position) {
                0 -> requireContext().getString(R.string.flags_tab)
                1 -> requireContext().getString(R.string.upgrades_tab)
                2 -> "Энергия"
                else -> ""
            }
        }.attach()
    }


    private fun updatePointsDisplay() {
        pointsTextView.text = requireContext().getString(R.string.current_points, currentPoints)
    }

    fun updateCurrentPoints(points: Int) {
        currentPoints = points
        updatePointsDisplay()
        adapter.notifyDataSetChanged()
    }
}