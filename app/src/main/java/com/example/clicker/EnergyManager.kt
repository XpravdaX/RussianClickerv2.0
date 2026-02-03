package com.example.clicker
import android.content.Context
import android.content.SharedPreferences
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView

class EnergyManager(private val context: Context) {

    companion object {
        private const val BASE_ENERGY_CAPACITY = 30
        private const val MAX_ENERGY_CAPACITY = 800
        private const val BASE_ENERGY_REGEN_TIME = 5
        private const val MIN_ENERGY_REGEN_TIME = 1
        private const val ENERGY_PER_CLICK = 1
    }

    private val prefs: SharedPreferences = context.getSharedPreferences("energy_data", Context.MODE_PRIVATE)
    private val handler = Handler(Looper.getMainLooper())

    private lateinit var energyRegenText: TextView

    private var currentEnergy: Int = 0
    private var maxEnergy: Int = BASE_ENERGY_CAPACITY
    private var regenTime: Int = BASE_ENERGY_REGEN_TIME // секунд на 1 энергию
    private var energySaverPercent: Int = 0 // 0-100%
    
    private lateinit var energyProgress: ProgressBar
    private lateinit var energyText: TextView
    
    private val regenRunnable = object : Runnable {
        override fun run() {
            if (currentEnergy < maxEnergy) {
                currentEnergy++
                updateDisplay()
                saveEnergyState()
            }
            handler.postDelayed(this, regenTime * 1000L)
        }
    }

    fun initViews(progressBar: ProgressBar, textView: TextView, regenTextView: TextView? = null) {
        energyProgress = progressBar
        energyText = textView
        if (regenTextView != null) {
            energyRegenText = regenTextView
            energyRegenText.visibility = View.VISIBLE
            updateRegenText()
        }
        loadEnergyState()
        startRegeneration()
    }

    private fun updateRegenText() {
        if (::energyRegenText.isInitialized) {
            energyRegenText.text = "1/$regenTime сек"
        }
    }

    fun canClick(): Boolean {
        val energyCost = if (energySaverPercent > 0) {
            val saved = (ENERGY_PER_CLICK * energySaverPercent / 100f).toInt()
            maxOf(1, ENERGY_PER_CLICK - saved) // Минимум 1 энергия
        } else {
            ENERGY_PER_CLICK
        }
        
        return currentEnergy >= energyCost
    }

    fun spendEnergyForClick(): Boolean {
        val energyCost = if (energySaverPercent > 0) {
            val saved = (ENERGY_PER_CLICK * energySaverPercent / 100f).toInt()
            maxOf(1, ENERGY_PER_CLICK - saved)
        } else {
            ENERGY_PER_CLICK
        }
        
        if (currentEnergy >= energyCost) {
            currentEnergy -= energyCost
            updateDisplay()
            saveEnergyState()
            return true
        }
        return false
    }

    fun getEnergyCost(): Int {
        return if (energySaverPercent > 0) {
            val saved = (ENERGY_PER_CLICK * energySaverPercent / 100f).toInt()
            maxOf(1, ENERGY_PER_CLICK - saved)
        } else {
            ENERGY_PER_CLICK
        }
    }

    fun getCriticalDamage(chance: Int, multiplier: Double): Double? {
        return if ((1..100).random() <= chance) {
            multiplier
        } else {
            null
        }
    }

    fun isDoubleClick(chance: Int): Boolean {
        return (1..100).random() <= chance
    }

    fun updateFromUpgrades(
        capacityLevel: Int,
        regenLevel: Int,
        criticalLevel: Int = 0,
        stormLevel: Int = 0,
        saverLevel: Int = 0,
        powerLevel: Int = 0
    ) {
        // Ёмкость энергии: +25 за уровень
        val capacityBonus = capacityLevel * 25
        maxEnergy = minOf(BASE_ENERGY_CAPACITY + capacityBonus, MAX_ENERGY_CAPACITY)

        // Регенерация: уменьшаем время на 0.4 сек за уровень (мин 1 сек)
        // 10 уровней: 5, 4.6, 4.2, 3.8, 3.4, 3.0, 2.6, 2.2, 1.8, 1.4, 1.0
        val regenReduction = regenLevel * 0.4
        regenTime = maxOf(1, (BASE_ENERGY_REGEN_TIME - regenReduction).toInt())

        updateRegenText() // Обновляем текст

        // Обновляем отображение
        updateDisplay()
        saveEnergyState()
    }

    fun getRegenTimeText(): String {
        return "1/$regenTime сек"
    }

    private fun updateDisplay() {
        energyProgress.max = maxEnergy
        energyProgress.progress = currentEnergy
        energyText.text = "$currentEnergy/$maxEnergy"
    }

    private fun startRegeneration() {
        handler.postDelayed(regenRunnable, regenTime * 1000L)
    }

    private fun loadEnergyState() {
        currentEnergy = prefs.getInt("current_energy", maxEnergy)
        maxEnergy = prefs.getInt("max_energy", BASE_ENERGY_CAPACITY)
        regenTime = prefs.getInt("regen_time", BASE_ENERGY_REGEN_TIME)
        energySaverPercent = prefs.getInt("energy_saver", 0)
        
        updateDisplay()
    }

    private fun saveEnergyState() {
        prefs.edit().apply {
            putInt("current_energy", currentEnergy)
            putInt("max_energy", maxEnergy)
            putInt("regen_time", regenTime)
            putInt("energy_saver", energySaverPercent)
            apply()
        }
    }

    fun resetEnergy() {
        currentEnergy = maxEnergy
        updateDisplay()
        saveEnergyState()
    }

    fun stop() {
        handler.removeCallbacks(regenRunnable)
    }
}