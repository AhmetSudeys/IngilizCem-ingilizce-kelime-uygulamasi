package com.example.ingilizcekelimekasasi.data.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.ingilizcekelimekasasi.data.local.dao.WordDao
import com.example.ingilizcekelimekasasi.data.local.entity.LearningDataEntity
import com.example.ingilizcekelimekasasi.data.local.entity.WordEntity

/**
 * İngilizCem Kasası uygulamasının ana Room veritabanı.
 *
 * Bu sınıf, uygulamadaki tüm yerel veri saklama işlemlerinin
 * merkezi giriş noktasıdır. Singleton olarak [di] katmanında
 * Hilt modülü aracılığıyla oluşturulacaktır.
 *
 * ### Tablolar:
 * - [WordEntity]: Kelimelerin statik bilgileri (İngilizce, Türkçe, kaynak tipi vb.)
 * - [LearningDataEntity]: SM2 algoritma parametreleri (EF, interval, tekrar tarihi vb.)
 *
 * ### Versiyon Geçmişi:
 * - **v1**: İlk sürüm — words ve learning_data tabloları.
 *
 * @see WordDao Veritabanı işlemleri için Data Access Object.
 * @see Converters Tip dönüşüm kuralları.
 */
@Database(
    entities = [
        WordEntity::class,
        LearningDataEntity::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {

    /**
     * Kelime işlemleri için DAO erişim noktası.
     *
     * @return [WordDao] implementasyonu (Room tarafından otomatik üretilir).
     */
    abstract fun wordDao(): WordDao
}
