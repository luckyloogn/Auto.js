package org.autojs.autojs.build

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Query
import androidx.room.Upsert


@Dao
interface KeyStoreDao {
    /**
     * 根据绝对路径获取 KeyStore。
     *
     * @param absolutePath 要获取的 KeyStore 的绝对路径。
     * @return 与给定绝对路径关联的 KeyStore，如果未找到则返回 null。
     */
    @Query("SELECT * FROM keystore WHERE absolutePath = :absolutePath LIMIT 1")
    suspend fun getByAbsolutePath(absolutePath: String): KeyStore?

    /**
     * 插入或更新一个或多个 KeyStore。
     *
     * @param keyStores 要插入或更新的 KeyStore。
     */
    @Upsert
    suspend fun upsert(vararg keyStores: KeyStore)

    /**
     * 获取所有 KeyStore。
     *
     * @return 数据库中所有 KeyStore 的列表。
     */
    @Query("SELECT * FROM keystore")
    suspend fun getAll(): List<KeyStore>

    /**
     * 根据绝对路径删除一个或多个 KeyStore。
     *
     * @param keyStores 要删除的 KeyStore。
     */
    @Delete
    suspend fun delete(vararg keyStores: KeyStore)

    /**
     * 根据绝对路径删除 KeyStore。
     *
     * @param absolutePath 要删除的 KeyStore 的绝对路径。
     * @return 删除的 KeyStore 数量。此值应始终为 1。
     */
    @Query("DELETE FROM keystore WHERE absolutePath = :absolutePath")
    suspend fun deleteByAbsolutePath(absolutePath: String): Int

    /**
     * 删除所有 KeyStore。
     */
    @Query("DELETE FROM keystore")
    suspend fun deleteAll()
}