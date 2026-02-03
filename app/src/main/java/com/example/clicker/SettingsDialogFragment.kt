package com.example.clicker

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.TextView
import androidx.fragment.app.DialogFragment

class SettingsDialogFragment : DialogFragment() {

    private lateinit var soundStatusText: TextView
    private lateinit var soundToggleContainer: FrameLayout
    private lateinit var soundToggleThumb: View
    private var isSoundEnabled = true

    var onSoundToggle: ((Boolean) -> Unit)? = null

    companion object {
        fun newInstance(isSoundEnabled: Boolean): SettingsDialogFragment {
            val fragment = SettingsDialogFragment()
            val args = Bundle()
            args.putBoolean("sound_enabled", isSoundEnabled)
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        isSoundEnabled = arguments?.getBoolean("sound_enabled", true) ?: true
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        return dialog
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.activity_seting_dialog, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initViews(view)
        setupSoundToggle()
        updateSoundDisplay()
    }

    private fun initViews(view: View) {
        soundStatusText = view.findViewById(R.id.sound_status_text)
        soundToggleContainer = view.findViewById(R.id.sound_toggle_indicator)
        soundToggleThumb = view.findViewById(R.id.sound_toggle_thumb)

        val soundToggleContainer = view.findViewById<View>(R.id.sound_toggle_container)
        soundToggleContainer.setOnClickListener {
            toggleSound()
        }

        // Закрытие диалога при клике вне его области
        view.findViewById<View>(R.id.dialog_main_container).setOnClickListener {
            // Ничего не делаем, чтобы диалог не закрывался при клике внутри
        }
    }

    private fun setupSoundToggle() {
        soundToggleContainer.setOnClickListener {
            toggleSound()
        }
    }

    private fun toggleSound() {
        isSoundEnabled = !isSoundEnabled
        updateSoundDisplay()
        onSoundToggle?.invoke(isSoundEnabled)
    }

    private fun updateSoundDisplay() {
        if (isSoundEnabled) {
            soundStatusText.text = requireContext().getString(R.string.sound_enabled)
            soundStatusText.setTextColor(requireContext().getColor(R.color.success_color))
            soundToggleContainer.setBackgroundResource(R.drawable.toggle_background_enabled)

            // Позиционируем переключатель вправо (включено)
            val params = soundToggleThumb.layoutParams as FrameLayout.LayoutParams
            params.gravity = android.view.Gravity.END
            soundToggleThumb.layoutParams = params
        } else {
            soundStatusText.text = requireContext().getString(R.string.sound_disabled)
            soundStatusText.setTextColor(requireContext().getColor(R.color.text_secondary))
            soundToggleContainer.setBackgroundResource(R.drawable.toggle_background_disabled)

            // Позиционируем переключатель влево (выключено)
            val params = soundToggleThumb.layoutParams as FrameLayout.LayoutParams
            params.gravity = android.view.Gravity.START
            soundToggleThumb.layoutParams = params
        }
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
    }
}