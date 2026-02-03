package com.example.clicker

import android.media.MediaPlayer
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.animation.DecelerateInterpolator
import android.view.animation.OvershootInterpolator
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.constraintlayout.widget.ConstraintLayout

class MainActivity : AppCompatActivity() {

    private var points = 0
    private var pointsPerClick = 1
    private var pointsPerSecond = 0
    private var multiplier = 1

    private lateinit var pointsText: TextView
    private lateinit var incomeText: TextView
    private lateinit var multiplierText: TextView
    private lateinit var flagCard: CardView
    private lateinit var flagImage: ImageView
    private lateinit var mainLayout: ConstraintLayout
    private lateinit var energyProgress: ProgressBar
    private lateinit var energyText: TextView

    private lateinit var floatingPointsAnimator: FloatingPointsAnimator
    private lateinit var shopManager: ShopManager
    private lateinit var energyManager: EnergyManager
    private var shopDialogFragment: ShopDialogFragment? = null

    // Добавляем MediaPlayer для звуков
    private lateinit var clickSoundPlayer: MediaPlayer
    private var isSoundEnabled = true // Флаг включения/выключения звука

    private val handler = Handler(Looper.getMainLooper())
    private val autoClickerRunnable = object : Runnable {
        override fun run() {
            if (pointsPerSecond > 0) {
                addPoints(pointsPerSecond)
            }
            handler.postDelayed(this, 1000)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        initViews()
        initManagers()
        initSound() // Инициализируем звук
        setupClickListeners()
        loadGameState()
        updateFromShop()
        startAutoClicker()
    }

    private fun initViews() {
        pointsText = findViewById(R.id.points_text)
        incomeText = findViewById(R.id.income_text)
        multiplierText = findViewById(R.id.multiplier_text)
        flagCard = findViewById(R.id.flag_card)
        flagImage = findViewById(R.id.flag_image)
        mainLayout = findViewById(R.id.main_layout)
        energyProgress = findViewById(R.id.energy_progress)
        energyText = findViewById(R.id.energy_text)
    }

    private fun initManagers() {
        floatingPointsAnimator = FloatingPointsAnimator(this)
        shopManager = ShopManager(this)
        energyManager = EnergyManager(this)

        // Инициализируем EnergyManager с View
        energyManager.initViews(energyProgress, energyText)

        shopManager.loadShopState()
    }

    private fun initSound() {
        // Инициализируем MediaPlayer для звука клика
        clickSoundPlayer = MediaPlayer.create(this, R.raw.click)

        // Настраиваем параметры воспроизведения
        clickSoundPlayer.setOnCompletionListener {
            // Возвращаем позицию на начало после завершения воспроизведения
            it.seekTo(0)
        }

        // Загружаем настройки звука
        loadSoundSettings()
    }

    private fun loadSoundSettings() {
        val prefs = getSharedPreferences("settings", MODE_PRIVATE)
        isSoundEnabled = prefs.getBoolean("sound_enabled", true)
    }

    private fun saveSoundSettings() {
        val prefs = getSharedPreferences("settings", MODE_PRIVATE)
        prefs.edit().putBoolean("sound_enabled", isSoundEnabled).apply()
    }

    private fun setupClickListeners() {
        // Клик по CardView (флагу)
        flagCard.apply {
            isClickable = true
            setOnClickListener {
                // Проверяем наличие энергии
                if (!energyManager.canClick()) {
                    showNoEnergyToast()
                    return@setOnClickListener
                }

                // Списываем энергию
                if (energyManager.spendEnergyForClick()) {
                    // Воспроизводим звук клика
                    playClickSound()

                    val clickValue = (pointsPerClick * multiplier).toInt()
                    addPoints(clickValue)

                    // Используем аниматор
                    val rootView = window.decorView.rootView as FrameLayout
                    floatingPointsAnimator.show(
                        anchorView = flagCard,
                        points = clickValue,
                        rootView = rootView
                    )

                    animateFlagClick()
                } else {
                    showNoEnergyToast()
                }
            }
        }

        // Кнопка магазина
        findViewById<View>(R.id.shop_button).setOnClickListener {
            // Воспроизводим звук при открытии магазина
            playClickSound()
            showShopDialog()
        }

        // Кнопка настроек
        findViewById<View>(R.id.settings_button).setOnClickListener {
            // Воспроизводим звук при открытии настроек
            playClickSound()
            showSettingsDialog()
        }
    }

    private fun playClickSound() {
        if (isSoundEnabled) {
            try {
                if (!clickSoundPlayer.isPlaying) {
                    clickSoundPlayer.start()
                } else {
                    // Если звук уже играет, перезапускаем
                    clickSoundPlayer.pause()
                    clickSoundPlayer.seekTo(0)
                    clickSoundPlayer.start()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun showNoEnergyToast() {
        Toast.makeText(
            this,
            resources.getString(R.string.no_energy),
            Toast.LENGTH_SHORT
        ).show()
    }

    private fun showSettingsDialog() {
        val settingsDialog = SettingsDialogFragment.newInstance(isSoundEnabled)

        settingsDialog.onSoundToggle = { soundEnabled ->
            isSoundEnabled = soundEnabled
            saveSoundSettings()
            Toast.makeText(
                this,
                if (soundEnabled) "Звук включен" else "Звук выключен",
                Toast.LENGTH_SHORT
            ).show()
        }

        settingsDialog.show(supportFragmentManager, "settings_dialog")
    }

    private fun showShopDialog() {
        shopDialogFragment = ShopDialogFragment.newInstance(points)

        shopDialogFragment?.onPointsUpdated = { spentPoints ->
            points -= spentPoints
            updateDisplay()
            saveGameState()
            shopDialogFragment?.updateCurrentPoints(points)
            updateFromShop()  // Важно: обновляем параметры игры
        }

        shopDialogFragment?.onFlagChanged = { flagId ->
            // Обновляем флаг
            updateFlagFromShop()
            shopDialogFragment?.updateCurrentPoints(points)

            // Обновляем все параметры
            updateFromShop()
        }

        // Добавьте этот callback для обновления состояния
        shopDialogFragment?.onShopStateUpdated = { updatedShopManager ->
            // Обновляем наш shopManager
            shopManager = updatedShopManager
            // Обновляем игровые параметры
            updateFromShop()
        }

        shopDialogFragment?.show(supportFragmentManager, "shop_dialog")
    }

    private fun updateFromShop() {
        // Обновляем множитель
        val shopMultiplier = shopManager.getClickMultiplier()
        multiplier = if (shopMultiplier > 1.0) shopMultiplier.toInt() else 1

        // Обновляем автодоход
        pointsPerSecond = shopManager.getAutoIncome()

        // Обновляем базовый клик
        val clickMultiplierUpgrade = shopManager.upgrades.find {
            it.upgradeType == UpgradeType.CLICK_MULTIPLIER
        }
        pointsPerClick = 1 + (clickMultiplierUpgrade?.currentLevel ?: 0)

        // Обновляем флаг
        updateFlagFromShop()

        // Обновляем энергию на основе улучшений
        updateEnergyFromUpgrades()

        updateDisplay()

        // Сохраняем состояние
        saveGameState()
    }

    private fun updateEnergyFromUpgrades() {
        val capacityLevel = shopManager.getEnergyCapacityLevel()
        val regenLevel = shopManager.getEnergyRegenLevel()

        // Обновляем энергоменеджер с новыми уровнями улучшений
        energyManager.updateFromUpgrades(
            capacityLevel = capacityLevel,
            regenLevel = regenLevel,
            criticalLevel = 0, // Пока 0, можно добавить позже
            stormLevel = 0,
            saverLevel = 0,
            powerLevel = 0
        )
    }

    private fun updateFlagFromShop() {
        val equippedFlag = shopManager.getEquippedFlag()
        flagImage.setImageResource(equippedFlag.drawableResId)
    }

    private fun addPoints(amount: Int) {
        points += amount
        updateDisplay()
        saveGameState()
    }

    private fun updateDisplay() {
        pointsText.text = formatNumber(points) + " очков"
        incomeText.text = "${pointsPerSecond * multiplier}/сек"
        multiplierText.text = "x$multiplier"
    }

    private fun formatNumber(number: Int): String {
        return when {
            number >= 1_000_000 -> "${(number / 1_000_000.0).format(1)}M"
            number >= 1_000 -> "${(number / 1_000.0).format(1)}K"
            else -> number.toString()
        }
    }

    private fun Double.format(digits: Int) = "%.${digits}f".format(this).replace(",", ".")

    /**
     * Анимация клика по флагу без искажения углов
     */
    private fun animateFlagClick() {
        flagCard.animate()
            .scaleX(0.92f)
            .scaleY(0.92f)
            .setDuration(80)
            .setInterpolator(DecelerateInterpolator())
            .withEndAction {
                flagCard.animate()
                    .scaleX(1f)
                    .scaleY(1f)
                    .setDuration(200)
                    .setInterpolator(OvershootInterpolator(0.4f))
                    .start()
            }
            .start()

        flagImage.animate()
            .scaleX(0.95f)
            .scaleY(0.95f)
            .setDuration(50)
            .withEndAction {
                flagImage.animate()
                    .scaleX(1f)
                    .scaleY(1f)
                    .setDuration(180)
                    .start()
            }
            .start()
    }

    private fun startAutoClicker() {
        handler.postDelayed(autoClickerRunnable, 1000)
    }

    private fun saveGameState() {
        val prefs = getSharedPreferences("game", MODE_PRIVATE)
        prefs.edit().apply {
            putInt("points", points)
            apply()
        }

        shopManager.saveShopState()
    }

    private fun loadGameState() {
        val prefs = getSharedPreferences("game", MODE_PRIVATE)
        points = prefs.getInt("points", 0)
    }

    override fun onPause() {
        super.onPause()
        shopManager.saveShopState()
        saveGameState()

        // Освобождаем ресурсы MediaPlayer
        if (::clickSoundPlayer.isInitialized) {
            clickSoundPlayer.release()
        }
    }

    override fun onResume() {
        super.onResume()
        loadGameState()
        shopManager.loadShopState()  // Загружаем состояние магазина
        updateFromShop()  // Обновляем параметры
        updateDisplay()

        // Переинициализируем звук
        if (!::clickSoundPlayer.isInitialized || !clickSoundPlayer.isPlaying) {
            initSound()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacks(autoClickerRunnable)
        energyManager.stop()  // Останавливаем регенерацию энергии
        shopDialogFragment?.dismiss()

        // Освобождаем ресурсы MediaPlayer
        if (::clickSoundPlayer.isInitialized) {
            clickSoundPlayer.release()
        }
    }
}