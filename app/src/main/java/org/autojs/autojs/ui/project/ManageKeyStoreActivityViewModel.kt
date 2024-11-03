package org.autojs.autojs.ui.project

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import java.io.File

class ManageKeyStoreActivityViewModel(context: Context) : ViewModel() {
    private var keyStoreDao: KeyStoreDao

    private val _allKeyStores = MutableLiveData<List<KeyStore>>()
    val allKeyStores: LiveData<List<KeyStore>> get() = _allKeyStores

    init {
        val keyStoreDatabase = KeyStoreDatabase.getDatabase(context)
        keyStoreDao = keyStoreDatabase.keyStoreDao()
    }

    /**
     * 根据提供的文件列表更新 KeyStore 对象。
     *
     * 该函数检查每个文件，如果在仓库中找到相应的 KeyStore，则返回该对象；
     * 否则创建一个新的 KeyStore。最后，将更新的 KeyStore 列表赋值给 `_allKeyStores`。
     *
     * @param files 要更新或创建的文件列表。
     */
    fun updateAllKeyStoresFromFiles(files: Array<File>) {
        viewModelScope.launch {
            val updatedKeyStores = files.map { file ->
                keyStoreDao.getByAbsolutePath(file.absolutePath) ?: KeyStore(
                    absolutePath = file.absolutePath,
                    filename = file.name
                )
            }

            _allKeyStores.value = updatedKeyStores
        }
    }

    /**
     * 插入或更新 KeyStore。
     *
     * @param keyStore 要插入或更新的 KeyStore。
     */
    fun upsertKeyStore(keyStore: KeyStore) {
        viewModelScope.launch {
            // 插入或更新数据库中的 KeyStore
            keyStoreDao.upsert(keyStore)

            // 获取当前的 KeyStore 列表
            val currentKeyStores = _allKeyStores.value ?: emptyList()

            // 创建一个新的列表，替换或添加 KeyStore
            val updatedKeyStores = if (currentKeyStores.any { it.absolutePath == keyStore.absolutePath }) {
                currentKeyStores.map {
                    if (keyStore.absolutePath == it.absolutePath) {
                        keyStore // 替换为更新后的 KeyStore
                    } else {
                        it // 保持不变
                    }
                }
            } else {
                currentKeyStores + keyStore // 添加新 KeyStore
            }

            // 更新 LiveData 的值
            _allKeyStores.value = updatedKeyStores
        }
    }

    /**
     * 删除 KeyStore。
     *
     * @param keyStore 要删除的 KeyStore。
     */
    fun deleteKeyStore(keyStore: KeyStore) {
        viewModelScope.launch {
            keyStoreDao.delete(keyStore)

            val currentKeyStores = _allKeyStores.value ?: emptyList()
            val updatedKeyStores = currentKeyStores.filter { it != keyStore }
            _allKeyStores.value = updatedKeyStores
        }
    }

    class Factory(private val context: Context) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return ManageKeyStoreActivityViewModel(context) as T
        }
    }
}