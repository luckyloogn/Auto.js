package org.autojs.autojs.ui.project

import android.annotation.SuppressLint
import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.LinearLayout
import androidx.fragment.app.DialogFragment
import org.autojs.autojs.R
import org.autojs.autojs.databinding.DialogNewKeyStoreBinding

open class NewKeyStoreDialog(
    private val callback: Callback
) : DialogFragment() {

    private lateinit var binding: DialogNewKeyStoreBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = DialogNewKeyStoreBinding.inflate(inflater)
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

        binding.confirm.setOnClickListener {
            var error = false
            val filename = binding.filename.text.toString()
            val password = binding.password.text.toString()
            val alias = binding.alias.text.toString()
            val aliasPassword = binding.aliasPassword.text.toString()
            var valvalidityYears = 25

            // 检查文件名是否符合Android命名规格
            when {
                filename.isEmpty() -> {
                    binding.filename.error = getString(R.string.error_filename_empty)
                    error = true
                }

                !containsSpecialCharacters(filename) -> {
                    binding.filename.error = getString(R.string.error_invalid_filename)
                    error = true
                }

                filename.length > 255 -> {
                    binding.filename.error = getString(R.string.error_filename_too_long)
                    error = true
                }

                else -> binding.filename.error = null
            }

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

            // 检查有效期是否符合要求
            if (binding.validityYears.text.toString().isEmpty()) {
                binding.validityYears.error = getString(R.string.error_validity_years_empty)
                error = true
            } else {
                val years = binding.validityYears.text.toString().toInt()
                if (years == 0) {
                    binding.validityYears.error = getString(R.string.error_validity_years_zero)
                    error = true
                } else {
                    binding.validityYears.error = null
                    valvalidityYears = years
                }
            }


            val firstAndLastName = binding.firstAndLastName.text.toString()

            val organization = binding.organization.text.toString()
            val organizationalUnit = binding.organizationalUnit.text.toString()

            val countryCode = binding.countryCode.text.toString()
            val stateOrProvince = binding.stateOrProvince.text.toString()
            val cityOrLocality = binding.cityOrLocality.text.toString()
            val street = binding.street.text.toString()

            if (firstAndLastName.isEmpty() && organization.isEmpty() &&
                organizationalUnit.isEmpty() && stateOrProvince.isEmpty() &&
                cityOrLocality.isEmpty() && street.isEmpty() && countryCode.isEmpty()
            ) {
                binding.firstAndLastName.error = getString(R.string.error_all_certificate_issuer_fields_empty)
                binding.organization.error = getString(R.string.error_all_certificate_issuer_fields_empty)
                binding.organizationalUnit.error = getString(R.string.error_all_certificate_issuer_fields_empty)
                binding.countryCode.error = getString(R.string.error_all_certificate_issuer_fields_empty)
                binding.stateOrProvince.error = getString(R.string.error_all_certificate_issuer_fields_empty)
                binding.cityOrLocality.error = getString(R.string.error_all_certificate_issuer_fields_empty)
                binding.street.error = getString(R.string.error_all_certificate_issuer_fields_empty)
                error = true
            } else {
                binding.firstAndLastName.error = null
                binding.organization.error = null
                binding.organizationalUnit.error = null
                binding.countryCode.error = null
                binding.stateOrProvince.error = null
                binding.cityOrLocality.error = null
                binding.street.error = null
            }

            // 检查国家代码是否符合要求 (ISO3166-1-Alpha-2: https://countrycodedata.com/)
            val countryCodeRegex = "^[A-Z]{2}$".toRegex()
            if (countryCode.isNotEmpty() && !countryCodeRegex.matches(countryCode)) {
                binding.countryCode.error = getString(R.string.error_invalid_country_code)
                error = true
            }

            if (error) return@setOnClickListener

            val suffix = getString(
                if (binding.typeJks.isChecked) R.string.text_new_key_store_jks
                else R.string.text_new_key_store_bks
            ).lowercase()

            val signatureAlgorithm = binding.signatureAlgorithm.selectedItem.toString()

            val configs = NewKeyStoreConfigs(
                filename = "$filename.$suffix",
                password = password,
                alias = alias,
                aliasPassword = aliasPassword,
                signatureAlgorithm = signatureAlgorithm,
                validityYears = valvalidityYears,
                firstAndLastName = firstAndLastName,
                organization = organization,
                organizationalUnit = organizationalUnit,
                countryCode = countryCode,
                stateOrProvince = stateOrProvince,
                cityOrLocality = cityOrLocality,
                street = street
            )
            callback.onConfirmButtonClicked(configs)
            dismiss()
        }

        binding.cancel.setOnClickListener {
            dismiss()
        }

        binding.moreOptions.setOnCheckedChangeListener { _, isChecked ->
            binding.moreOptionsContainer.visibility = if (isChecked) View.VISIBLE else View.GONE
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

    private fun containsSpecialCharacters(fileName: String): Boolean {
        // 定义不允许的字符
        val invalidCharacters = listOf("\\", "/", ":", "*", "?", "\"", "<", ">", "|")

        // 检查文件名是否包含无效字符
        for (char in invalidCharacters) {
            if (fileName.contains(char)) {
                return false
            }
        }

        return true
    }

    data class NewKeyStoreConfigs(
        val filename: String,
        val password: String,
        val alias: String,
        val aliasPassword: String,
        val signatureAlgorithm: String,
        val validityYears: Int,
        val firstAndLastName: String,
        val organizationalUnit: String,
        val organization: String,
        val countryCode: String,
        val stateOrProvince: String,
        val cityOrLocality: String,
        val street: String,
    )


    interface Callback {
        fun onConfirmButtonClicked(configs: NewKeyStoreConfigs)
    }
}

