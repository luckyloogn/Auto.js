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
    @ColumnInfo(name = "filename") var filename: String,  // 文件名
    @ColumnInfo(name = "password") var password: String? = null,  // 密码
    @ColumnInfo(name = "alias") var alias: String? = null,  // 别名
    @ColumnInfo(name = "alias_password") var aliasPassword: String? = null,  // 别名密码
    @ColumnInfo(name = "verified") var verified: Boolean = false  // 验证状态，默认为 false
)
