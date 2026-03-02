package com.example.ingilizcekelimekasasi.di

import android.content.Context
import androidx.room.Room
import com.example.ingilizcekelimekasasi.data.local.dao.WordDao
import com.example.ingilizcekelimekasasi.data.local.database.AppDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Room veritabanı bağımlılıklarını sağlayan Hilt modülü.
 *
 * Bu modül [SingletonComponent]'e yüklenir, yani sağladığı
 * bağımlılıklar uygulama ömrü boyunca tek bir instance olarak yaşar.
 *
 * ### Sağlanan Bağımlılıklar:
 * - [AppDatabase]: Room veritabanı instance'ı (Singleton).
 * - [WordDao]: Kelime işlemleri için Data Access Object.
 *
 * ### Neden Singleton?
 * Room veritabanı oluşturma maliyetli bir işlemdir.
 * `Room.databaseBuilder()` her çağrıldığında yeni bir instance oluşturur.
 * Bu nedenle tüm uygulama boyunca **tek** bir instance kullanılmalıdır.
 */
@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    /**
     * [AppDatabase] instance'ını oluşturur ve sağlar.
     *
     * Veritabanı ismi `ingilizcem_kasasi_db` olarak belirlenir.
     * `fallbackToDestructiveMigration()` sayesinde şema versiyonu
     * değiştiğinde eski veriler silinip temiz tablo oluşturulur.
     * (Prodüksiyon için ileride migration stratejisi eklenecek.)
     *
     * @param context Uygulama context'i (Hilt tarafından inject edilir).
     * @return Singleton [AppDatabase] instance'ı.
     */
    @Provides
    @Singleton
    fun provideDatabase(
        @ApplicationContext context: Context
    ): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "ingilizcem_kasasi_db"
        )
            .fallbackToDestructiveMigration()
            .build()
    }

    /**
     * [AppDatabase]'den [WordDao] instance'ını çıkarır ve sağlar.
     *
     * DAO, database instance'ının abstract fonksiyonu olduğundan
     * ayrıca Singleton anotasyonu gerekmez — database zaten Singleton.
     *
     * @param database Room veritabanı instance'ı.
     * @return [WordDao] implementasyonu.
     */
    @Provides
    @Singleton
    fun provideWordDao(database: AppDatabase): WordDao {
        return database.wordDao()
    }
}
