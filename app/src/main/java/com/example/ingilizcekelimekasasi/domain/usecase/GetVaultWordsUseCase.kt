package com.example.ingilizcekelimekasasi.domain.usecase

import com.example.ingilizcekelimekasasi.domain.model.WordItem
import com.example.ingilizcekelimekasasi.domain.repository.WordRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Kasam (kelime havuzu) ekranı için tüm kelimeleri getiren kullanım senaryosu.
 *
 * Kaydedilen tüm kelimeleri öğrenme verileriyle birlikte
 * oluşturulma tarihine göre sıralı (en yeni başta) olarak döner.
 *
 * Flow döndüğü için yeni kelime eklendiğinde, silindiğinde
 * veya güncellendiğinde Kasam ekranı otomatik güncellenir.
 *
 * @property wordRepository Kelime veritabanı erişim katmanı.
 */
class GetVaultWordsUseCase @Inject constructor(
    private val wordRepository: WordRepository
) {
    /**
     * @return Tüm kelimelerin reaktif akışı (en yeniden eskiye sıralı).
     */
    operator fun invoke(): Flow<List<WordItem>> {
        return wordRepository.getAllWords()
    }
}
