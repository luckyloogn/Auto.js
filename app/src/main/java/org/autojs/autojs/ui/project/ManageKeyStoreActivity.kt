package org.autojs.autojs.ui.project

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.annotation.StringRes
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import com.afollestad.materialdialogs.DialogAction
import com.afollestad.materialdialogs.MaterialDialog
import com.mcal.apksigner.CertCreator
import com.mcal.apksigner.utils.DistinguishedNameValues
import com.mcal.apksigner.utils.KeyStoreHelper
import org.autojs.autojs.Pref
import org.autojs.autojs.R
import org.autojs.autojs.databinding.ActivityManageKeyStoreBinding
import org.autojs.autojs.ui.BaseActivity
import org.autojs.autojs.ui.project.NewKeyStoreDialog.NewKeyStoreConfigs
import org.autojs.autojs.ui.project.VerifyKeyStoreDialog.VerifyKeyStoreConfigs
import java.io.File
import java.io.IOException

class ManageKeyStoreActivity : BaseActivity() {

    private val TAG = "ManageKeyStoreActivity"
    private lateinit var binding: ActivityManageKeyStoreBinding
    private lateinit var keyStoreAdapter: KeyStoreAdaptor
    private lateinit var keyStoreViewModel: KeyStoreViewModel

    companion object {
        fun startActivity(context: Context) {
            Intent(context, ManageKeyStoreActivity::class.java).apply {}.also {
                ContextCompat.startActivity(context, it, null)
            }
        }
    }

    private val newKeyStoreDialogCallback = object : NewKeyStoreDialog.Callback {
        override fun onConfirmButtonClicked(configs: NewKeyStoreConfigs) {
            createKeyStore(configs)
        }
    }

    private val verifyKeyStoreDialog = object : VerifyKeyStoreDialog.Callback {
        override fun onVerifyButtonClicked(
            configs: VerifyKeyStoreConfigs, keyStore: KeyStore
        ) {
            verifyKeyStore(configs, keyStore)
        }
    }

    private val keyStoreAdapterCallback = object : KeyStoreAdaptor.KeyStoreAdapterCallback {
        override fun onDeleteButtonClicked(keyStore: KeyStore) {
            MaterialDialog.Builder(this@ManageKeyStoreActivity)
                .title(getString(R.string.text_are_you_sure_to_delete, keyStore.filename))
                .positiveText(R.string.ok).negativeText(R.string.cancel)
                .onPositive { _: MaterialDialog, _: DialogAction ->
                    deleteKeyStore(keyStore)
                }.show()
        }

        override fun onVerifyButtonClicked(keyStore: KeyStore) {
            VerifyKeyStoreDialog(verifyKeyStoreDialog, keyStore).show(supportFragmentManager, null)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityManageKeyStoreBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setToolbarAsBack(getString(R.string.text_manage_key_store))

        keyStoreViewModel =
            ViewModelProvider(this, KeyStoreViewModel.Factory(this))[KeyStoreViewModel::class.java]

        binding.fab.setOnClickListener {
            NewKeyStoreDialog(newKeyStoreDialogCallback).show(supportFragmentManager, null)
        }

        keyStoreAdapter = KeyStoreAdaptor(keyStoreAdapterCallback)
        binding.recyclerViewKeyStore.apply {
            adapter = keyStoreAdapter
            layoutManager = LinearLayoutManager(this@ManageKeyStoreActivity)
            itemAnimator = DefaultItemAnimator()
        }

        keyStoreViewModel.allKeyStores.observe(this@ManageKeyStoreActivity) {
            keyStoreAdapter.submitList(it.toList())
        }

        loadKeyStores()
    }

    override fun onResume() {
        super.onResume()
        loadKeyStores()
    }

    private fun loadKeyStores() {
        val path = File(Pref.getKeyStorePath())
        if (!path.isDirectory) {
            return
        }

        val filteredFiles = path.listFiles { _, name ->
            name.endsWith(".bks") || name.endsWith(".jks")
        } ?: emptyArray()

        keyStoreViewModel.updateAllKeyStoresFromFiles(filteredFiles)
    }

    fun createKeyStore(configs: NewKeyStoreConfigs) {
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
                configs.password.toCharArray(),
                "RSA",
                2048,
                configs.alias,
                configs.aliasPassword.toCharArray(),
                configs.signatureAlgorithm,
                configs.validityYears,
                distinguishedNameValues
            )
            val newKeyStore = KeyStore(
                absolutePath = file.absolutePath,
                filename = file.name,
                password = configs.password,
                alias = configs.alias,
                aliasPassword = configs.aliasPassword,
                verified = true
            )
            keyStoreViewModel.upsertKeyStore(newKeyStore)
            showToast(R.string.success_create_key_store)
        } catch (e: IOException) {
            showToast(getString(R.string.error_create_key_store) + " " + e.message)
        } catch (e: Exception) {
            showToast(getString(R.string.error_create_key_store) + " " + e.message)
        }
    }

    fun deleteKeyStore(keyStore: KeyStore) {
        keyStore.absolutePath.let {
            try {
                File(it).delete()
                keyStoreViewModel.deleteKeyStore(keyStore)
                showToast(getString(R.string.text_already_delete) + " " + keyStore.filename)
            } catch (e: Exception) {
                showToast(getString(R.string.text_delete_failed) + ": " + e.message)
            }
        }
    }

    fun verifyKeyStore(
        configs: VerifyKeyStoreConfigs, keyStore: KeyStore
    ) {
        // 尝试加载 KeyStore
        val tmpKeyStore = try {
            KeyStoreHelper.loadKeyStore(File(keyStore.absolutePath), configs.password.toCharArray())
        } catch (e: Exception) {
            null
        }

        if (tmpKeyStore == null) {
            showToast(R.string.text_verify_failed)
            return
        }

        // 尝试获取密钥
        val tmpKey = try {
            tmpKeyStore.getKey(configs.alias, configs.aliasPassword.toCharArray())
        } catch (e: Exception) {
            null
        }

        if (tmpKey == null) {
            showToast(R.string.text_verify_failed)
            return
        }

        val verifiedKeyStore = KeyStore(
            absolutePath = keyStore.absolutePath,
            filename = keyStore.filename,
            password = configs.password,
            alias = configs.alias,
            aliasPassword = configs.aliasPassword,
            verified = true
        )
        keyStoreViewModel.upsertKeyStore(verifiedKeyStore)
        showToast(R.string.text_verify_success)
    }

    private fun showToast(@StringRes messageResId: Int) {
        Toast.makeText(this, getString(messageResId), Toast.LENGTH_SHORT).show()
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}