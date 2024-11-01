package org.autojs.autojs.ui.project

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.mcal.apksigner.CertCreator
import com.mcal.apksigner.utils.DistinguishedNameValues
import org.autojs.autojs.Pref
import org.autojs.autojs.R
import org.autojs.autojs.databinding.ActivityManageKeyStoreBinding
import org.autojs.autojs.ui.BaseActivity
import java.io.File
import java.io.IOException

class ManageKeyStoreActivity : BaseActivity() {

    private val TAG = "ManageKeyStoreActivity"
    private lateinit var binding: ActivityManageKeyStoreBinding

    companion object {
        fun startActivity(context: Context) {
            Intent(context, ManageKeyStoreActivity::class.java).apply {}.also {
                ContextCompat.startActivity(context, it, null)
            }
        }
    }

    private val newKeyStoreDialogCallback = object : NewKeyStoreDialog.Callback {
        override fun onConfirmButtonClicked(configs: NewKeyStoreDialog.NewKeyStoreConfigs) {
            val keyStorePath = File(Pref.getKeyStorePath())
            keyStorePath.mkdirs()
            val file = File(keyStorePath, configs.filename)

            val distinguishedNameValues = DistinguishedNameValues().apply {
                setCommonName(configs.firstAndLastName)
                setOrganization(configs.organization)
                setOrganizationalUnit(configs.organizationalUnit)
                setCountry(configs.countryCode)
                setState(configs.stateOrProvince)
                setLocality(configs.cityOrLocality)
                setStreet(configs.street)
            }

            try {
                CertCreator.createKeystoreAndKey(
                    file,
                    configs.password,
                    "RSA",
                    2048,
                    configs.alias,
                    configs.aliasPassword,
                    configs.signatureAlgorithm,
                    configs.validityYears,
                    distinguishedNameValues
                )

                Toast.makeText(
                    this@ManageKeyStoreActivity,
                    getString(R.string.success_create_key_store),
                    Toast.LENGTH_SHORT
                ).show()
                Log.d(TAG, "Create Key Store success")
            } catch (e: IOException) {
                Toast.makeText(
                    this@ManageKeyStoreActivity,
                    getString(R.string.error_create_key_store) + " " + e.message,
                    Toast.LENGTH_SHORT
                ).show()
                Log.e(TAG, "Create Key Store failed: ", e)
            } catch (e: Exception) {
                Toast.makeText(
                    this@ManageKeyStoreActivity,
                    getString(R.string.error_create_key_store) + " " + e.message,
                    Toast.LENGTH_SHORT
                ).show()
                Log.e(TAG, "Create Key Store failed: ", e)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityManageKeyStoreBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setToolbarAsBack(getString(R.string.text_manage_key_store))

        binding.fab.setOnClickListener {
            NewKeyStoreDialog(newKeyStoreDialogCallback).show(supportFragmentManager, null)
        }
    }
}
