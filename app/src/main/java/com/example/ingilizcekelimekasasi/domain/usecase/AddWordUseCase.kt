package com.example.ingilizcekelimekasasi.domain.usecase

import com.example.ingilizcekelimekasasi.domain.repository.WordRepository
import javax.inject.Inject

/**
 * Yeni bir kelimeyi varsayılan SM2 başlangıç değerleriyle kaydeden kullanım senaryosu.
 *
 * Manuel giriş ve OCR tarama ekranlarından çağrılır. Kelime eklendiğinde
 * Repository katmanındaki `addWord` metodu otomatik olarak:
 * - EF = 2.5 (varsayılan kolaylık)
 * - Interval = 0 (henüz çalışılmamış)
 * - Repetitions = 0 (henüz tekrar yok)
 * - NextReviewDate = şu an (hemen quiz'e dahil olabilir)
 *
 * değerleriyle bir [LearningDataEntity] kaydı oluşturur.
 *
 * @property wordRepository Kelime veritabanı erişim katmanı.
 */
class AddWordUseCase @Inject constructor(
    private val wordRepository: WordRepository
) {
    /**
     * Yeni bir kelimeyi veritabanına kaydeder.
     *
     * @param englishWord İngilizce kelime veya kelime öbeği.
     * @param turkishMeaning Türkçe karşılığı.
     * @param exampleSentence Örnek cümle (opsiyonel).
     * @param sourceType Ekleme yöntemi: "OCR" (kamera) veya "MANUAL" (elle giriş).
     */
    suspend operator fun invoke(
        englishWord: String,
        turkishMeaning: String,
        exampleSentence: String? = null,
        sourceType: String = "MANUAL"
    ) {
        wordRepository.addWord(
            englishWord = englishWord.trim(),
            turkishMeaning = turkishMeaning.trim(),
            exampleSentence = exampleSentence?.trim()?.ifBlank { null },
            sourceType = sourceType
        )
    }
}
