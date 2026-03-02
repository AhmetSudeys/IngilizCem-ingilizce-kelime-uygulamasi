package com.example.ingilizcekelimekasasi.domain.usecase

import com.example.ingilizcekelimekasasi.domain.model.WordItem
import com.example.ingilizcekelimekasasi.domain.repository.WordRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Tekrar vakti gelmiş kelimeleri getiren kullanım senaryosu.
 *
 * Quiz ekranı bu Use Case'i kullanarak `nextReviewDate ≤ şu an`
 * koşulunu sağlayan kelimeleri alır. En acil kelimeler başta olmak
 * üzere sıralı bir liste döner.
 *
 * Flow döndüğü için veritabanındaki değişiklikler (ör. bir kelime
 * çalışıldığında nextReviewDate güncellenmesi) otomatik olarak
 * UI'a yansır.
 *
 * @property wordRepository Kelime veritabanı erişim katmanı.
 */
class GetWordsDueForReviewUseCase @Inject constructor(
    private val wordRepository: WordRepository
) {
    /**
     * @param currentTimestamp Şu anki zaman damgası (epoch ms).
     *                        Varsayılan: [System.currentTimeMillis].
     * @return Tekrar vakti gelmiş kelimelerin reaktif akışı.
     */
    operator fun invoke(
        currentTimestamp: Long = System.currentTimeMillis()
    ): Flow<List<WordItem>> {
        return wordRepository.getWordsDueForReview(currentTimestamp)
    }
}
