package org.autojs.autojs.ui.project

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

//参考:
// https://developer.android.com/training/data-storage/room?hl=zh-cn
// https://www.bilibili.com/video/BV1ct411K7tp

@Entity
data class KeyStore(
    @PrimaryKey val absolutePath: String,  // 密钥库绝对路径，主键，唯一标识
    @ColumnInfo(name = "filename") val filename: String = "",  // 文件名
    @ColumnInfo(name = "password") val password: String = "",  // 密码
    @ColumnInfo(name = "alias") val alias: String = "",  // 别名
    @ColumnInfo(name = "alias_password") val aliasPassword: String = "",  // 别名密码
    @ColumnInfo(name = "verified") val verified: Boolean = false  // 验证状态，默认为 false
) {
    // 重写 toString() 方法以便在 UI 组件中使用 KeyStore 对象，但只显示文件名
    override fun toString(): String = filename
}
