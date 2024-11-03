package org.autojs.autojs.ui.project

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import com.afollestad.materialdialogs.DialogAction
import com.afollestad.materialdialogs.MaterialDialog
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
    private lateinit var keyStoreAdapter: KeyStoreAdaptor
    private lateinit var viewModel: ManageKeyStoreActivityViewModel

    companion object {
        fun startActivity(context: Context) {
            Intent(context, ManageKeyStoreActivity::class.java).apply {}.also {
                ContextCompat.startActivity(context, it, null)
            }
        }
    }

    private val newKeyStoreDialogCallback = object : NewKeyStoreDialog.Callback {
        override fun onConfirmButtonClicked(configs: NewKeyStoreDialog.NewKeyStoreConfigs) {
            createKeyStore(configs)
        }
    }

    private val keyStoreAdapterCallback = object : KeyStoreAdaptor.KeyStoreAdapterCallback {
        override fun onDeleteButtonClicked(keyStore: KeyStore) {
            MaterialDialog.Builder(this@ManageKeyStoreActivity)
                .title(getString(R.string.text_are_you_sure_to_delete, keyStore.filename))
                .positiveText(R.string.ok)
                .negativeText(R.string.cancel)
                .onPositive { _: MaterialDialog, _: DialogAction ->
                    deleteKeyStore(keyStore)
                }
                .show()
        }

        override fun onVerifyButtonClicked(keyStore: KeyStore) {
            TODO("Not yet implemented")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityManageKeyStoreBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setToolbarAsBack(getString(R.string.text_manage_key_store))

        viewModel = ViewModelProvider(
            this,
            ManageKeyStoreActivityViewModel.Factory(this)
        )[ManageKeyStoreActivityViewModel::class.java]

        binding.fab.setOnClickListener {
            NewKeyStoreDialog(newKeyStoreDialogCallback).show(supportFragmentManager, null)
        }

        keyStoreAdapter = KeyStoreAdaptor(keyStoreAdapterCallback)
        binding.recyclerViewKeyStore.apply {
            adapter = keyStoreAdapter
            layoutManager = LinearLayoutManager(this@ManageKeyStoreActivity)
            itemAnimator = DefaultItemAnimator()
        }

        viewModel.allKeyStores.observe(this@ManageKeyStoreActivity) {
            keyStoreAdapter.submitList(it)
        }

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

        viewModel.updateAllKeyStoresFromFiles(filteredFiles)
    }

    fun createKeyStore(configs: NewKeyStoreDialog.NewKeyStoreConfigs) {
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
            viewModel.upsertKeyStore(
                KeyStore(
                    absolutePath = file.absolutePath,
                    filename = file.name,
                    password = configs.password.toString(),
                    alias = configs.alias,
                    aliasPassword = configs.aliasPassword.toString(),
                    verified = true
                )
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

    fun deleteKeyStore(keyStore: KeyStore) {
        keyStore.absolutePath.let {
            try {
                File(it).delete()
                viewModel.deleteKeyStore(keyStore)
                Toast.makeText(
                    this@ManageKeyStoreActivity,
                    getString(R.string.text_already_delete) + " " + keyStore.filename,
                    Toast.LENGTH_SHORT
                ).show()
            } catch (e: Exception) {
                Toast.makeText(
                    this@ManageKeyStoreActivity,
                    getString(R.string.text_delete_failed) + ": " + e.message,
                    Toast.LENGTH_SHORT
                ).show()
                Log.e(TAG, "Delete Key Store failed: ", e)
                return
            }
        }
    }
}
