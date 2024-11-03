package org.autojs.autojs.ui.project

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import android.widget.LinearLayout
import android.widget.Toast
import androidx.annotation.StringRes
import androidx.fragment.app.DialogFragment
import org.autojs.autojs.R
import org.autojs.autojs.databinding.DialogVerifyKeyStoreBinding

open class VerifyKeyStoreDialog(
    private val callback: Callback,
    private val keyStore: KeyStore
) : DialogFragment() {

    private lateinit var binding: DialogVerifyKeyStoreBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = DialogVerifyKeyStoreBinding.inflate(inflater)
        return binding.root
    }

    @SuppressLint("SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        dialog?.window?.setLayout(
            (resources.displayMetrics.widthPixels * 0.85f).toInt(),
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        dialog?.setCanceledOnTouchOutside(true)

        binding.filePath.text = keyStore.absolutePath

        if (keyStore.verified) {
            binding.imgVerifyState.setImageResource(R.drawable.ic_key_store_verified)
            binding.textVerifyState.text = getString(R.string.text_verified)
            binding.password.setText(keyStore.password)
            binding.alias.setText(keyStore.alias)
            binding.aliasPassword.setText(keyStore.aliasPassword)
        } else {
            binding.imgVerifyState.setImageResource(R.drawable.ic_key_store_unverified)
            binding.textVerifyState.text = getString(R.string.text_unverified)
        }

        binding.verify.setOnClickListener {
            var error = false
            val password = binding.password.text.toString()
            val alias = binding.alias.text.toString()
            val aliasPassword = binding.aliasPassword.text.toString()

            // 检查密码是否符合要求
            when {
                password.isEmpty() -> {
                    binding.password.error = getString(R.string.error_password_empty)
                    error = true
                }

                password.length < 6 -> {
                    binding.password.error = getString(R.string.error_password_min_length)
                    error = true
                }

                else -> binding.password.error = null
            }

            // 检查别名密码是否符合要求
            when {
                aliasPassword.isEmpty() -> {
                    binding.aliasPassword.error = getString(R.string.error_password_empty)
                    error = true
                }

                aliasPassword.length < 6 -> {
                    binding.aliasPassword.error = getString(R.string.error_password_min_length)
                    error = true
                }

                else -> binding.aliasPassword.error = null
            }

            // 检查别名是否符合要求
            if (alias.isEmpty()) {
                binding.alias.error = getString(R.string.error_alias_empty)
                error = true
            } else {
                binding.alias.error = null
            }

            if (error) return@setOnClickListener

            val configs = VerifyKeyStoreConfigs(
                password = password,
                alias = alias,
                aliasPassword = aliasPassword,
            )
            callback.onVerifyButtonClicked(configs, keyStore)
            dismiss()
        }

        binding.cancel.setOnClickListener {
            dismiss()
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return super.onCreateDialog(savedInstanceState).apply {
            window?.apply {
                setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
                setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN)
            }
        }
    }

    private fun showMessage(@StringRes message: Int) {
        activity?.runOnUiThread {
            Toast.makeText(context, getString(message), Toast.LENGTH_SHORT).show()
        }
    }

    fun hideKeyboard() {
        val imm: InputMethodManager =
            requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        if (imm.isActive) imm.toggleSoftInput(0, InputMethodManager.HIDE_NOT_ALWAYS)
    }

    fun isShowing() = dialog?.isShowing ?: false

    data class VerifyKeyStoreConfigs(
        val password: String,
        val alias: String,
        val aliasPassword: String,
    )

    interface Callback {
        fun onVerifyButtonClicked(configs: VerifyKeyStoreConfigs, keyStore: KeyStore)
    }
}

