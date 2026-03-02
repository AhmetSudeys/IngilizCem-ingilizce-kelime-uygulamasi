package com.example.ingilizcekelimekasasi.domain.usecase

import com.example.ingilizcekelimekasasi.domain.repository.WordRepository
import javax.inject.Inject

/**
 * Quiz sonrasında bir kelimenin SM2 öğrenme verilerini güncelleyen kullanım senaryosu.
 *
 * Bu Use Case, uygulamadaki en kritik iş mantığını orkestre eder:
 * 1. Repository'den kelimeyi çeker.
 * 2. [Sm2Algorithm] ile yeni öğrenme parametrelerini hesaplatır.
 * 3. Güncellenmiş verileri Repository üzerinden veritabanına yazar.
 *
 * ### Akış Diyagramı:
 * ```
 * Quiz Ekranı → quality (0-5)
 *       │
 *       ▼
 * ReviewWordUseCase
 *       │
 *       ├── Repository.getWordById(wordId)
 *       │         │
 *       │         ▼
 *       ├── Sm2Algorithm.calculateNextReview(quality, interval, reps, EF)
 *       │         │
 *       │         ▼ Sm2Result
 *       └── Repository.updateLearningData(updatedWordItem)
 * ```
 *
 * @property wordRepository Kelime veritabanı erişim katmanı.
 * @property sm2Algorithm SM2 hesaplama motoru.
 */
class ReviewWordUseCase @Inject constructor(
    private val wordRepository: WordRepository,
    private val sm2Algorithm: Sm2Algorithm
) {
    /**
     * Bir kelimeyi kullanıcının geri bildirimine göre günceller.
     *
     * @param wordId Güncellenen kelimenin kimliği.
     * @param quality Kullanıcının kalite puanı (0-5).
     *                - 0: Tamamen unuttum
     *                - 3: Zar zor hatırladım
     *                - 5: Anında bildim
     * @throws IllegalStateException Kelime bulunamazsa.
     * @throws IllegalArgumentException [quality] 0-5 aralığında değilse.
     */
    suspend operator fun invoke(wordId: Long, quality: Int) {
        // 1. Kelimeyi veritabanından al
        val wordItem = wordRepository.getWordById(wordId)
            ?: throw IllegalStateException(
                "Güncellenecek kelime bulunamadı: wordId=$wordId"
            )

        // 2. SM2 algoritmasıyla yeni parametreleri hesapla
        val result = sm2Algorithm.calculateNextReview(
            quality = quality,
            previousInterval = wordItem.interval,
            previousRepetitions = wordItem.repetitions,
            previousEasinessFactor = wordItem.easinessFactor
        )

        // 3. Domain modelini güncellenmiş değerlerle kopyala
        val updatedWordItem = wordItem.copy(
            interval = result.interval,
            repetitions = result.repetitions,
            easinessFactor = result.easinessFactor,
            nextReviewDate = result.nextReviewDate,
            lastReviewedDate = System.currentTimeMillis()
        )

        // 4. Veritabanına yaz
        wordRepository.updateLearningData(updatedWordItem)
    }
}
