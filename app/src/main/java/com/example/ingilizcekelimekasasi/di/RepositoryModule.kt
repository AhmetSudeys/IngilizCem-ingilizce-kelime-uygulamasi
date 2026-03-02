package com.example.ingilizcekelimekasasi.di

import com.example.ingilizcekelimekasasi.data.repository.WordRepositoryImpl
import com.example.ingilizcekelimekasasi.domain.repository.WordRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Repository bağımlılıklarını sağlayan Hilt modülü.
 *
 * `@Binds` kullanarak interface ile implementasyonu bağlar.
 * `@Provides` yerine `@Binds` tercih edilmesinin nedeni:
 * - Daha az boilerplate kod.
 * - Hilt'in derleme zamanında daha verimli kod üretmesi.
 * - [WordRepositoryImpl] zaten `@Inject constructor` ile
 *   bağımlılıklarını aldığı için ekstra yapılandırma gereksiz.
 *
 * ### Dependency Inversion (SOLID - D):
 * Bu modül sayesinde ViewModel'ler somut [WordRepositoryImpl] sınıfına
 * değil, soyut [WordRepository] interface'ine bağımlıdır.
 * Test ortamında fake/mock bir implementasyon kolayca inject edilebilir.
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    /**
     * [WordRepositoryImpl] somut sınıfını [WordRepository] interface'ine bağlar.
     *
     * Hilt, [WordRepository] tipinde bir bağımlılık talep edildiğinde
     * otomatik olarak [WordRepositoryImpl] instance'ını sağlar.
     *
     * @param impl Hilt tarafından oluşturulan [WordRepositoryImpl] instance'ı.
     * @return Aynı instance, [WordRepository] tipiyle.
     */
    @Binds
    @Singleton
    abstract fun bindWordRepository(
        impl: WordRepositoryImpl
    ): WordRepository
}
