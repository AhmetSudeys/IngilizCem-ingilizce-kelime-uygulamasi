package com.example.ingilizcekelimekasasi.domain.model

/**
 * UI katmanında kullanılan temiz domain modeli.
 *
 * Room entity'lerini ([WordEntity] + [LearningDataEntity]) doğrudan UI'a taşımak
 * yerine bu model kullanılır. Bu sayede:
 * - UI katmanı veritabanı detaylarından bağımsızlaşır (Clean Architecture).
 * - Repository katmanında mapping yapılarak gereksiz alanlar filtrelenir.
 * - İleride entity yapısı değişse bile UI etkilenmez.
 *
 * ### SM2 Parametreleri:
 * - [easinessFactor]: Kelimenin zorluk katsayısı (2.5 başlangıç, min 1.3).
 * - [interval]: Sonraki tekrar için gün cinsinden bekleme süresi.
 * - [repetitions]: Art arda başarılı hatırlama sayısı.
 * - [nextReviewDate]: Kelimenin tekrar sorulacağı tarih (epoch ms).
 *
 * @property wordId Kelimenin benzersiz kimliği (DB'den gelir).
 * @property englishWord İngilizce kelime veya kelime öbeği.
 * @property turkishMeaning Türkçe karşılığı.
 * @property exampleSentence Örnek İngilizce cümle (opsiyonel).
 * @property sourceType Eklenme yöntemi: "OCR" veya "MANUAL".
 * @property createdAt Kelimenin eklenme tarihi (epoch ms).
 * @property easinessFactor SM2 kolaylık faktörü.
 * @property interval Tekrar aralığı (gün).
 * @property repetitions Başarılı tekrar sayacı.
 * @property nextReviewDate Bir sonraki tekrar tarihi (epoch ms).
 * @property lastReviewedDate Son tekrar yapılma tarihi (epoch ms, null = hiç tekrar yok).
 * @property learningId Öğrenme kaydının kimliği (güncelleme işlemleri için).
 */
data class WordItem(
    val wordId: Long,
    val englishWord: String,
    val turkishMeaning: String,
    val exampleSentence: String?,
    val sourceType: String,
    val createdAt: Long,

    // SM2 Parametreleri
    val easinessFactor: Float,
    val interval: Int,
    val repetitions: Int,
    val nextReviewDate: Long,
    val lastReviewedDate: Long?,
    val learningId: Long
) {
    /**
     * Kelimenin tekrar vaktinin gelip gelmediğini kontrol eder.
     *
     * @param currentTimestamp Şu anki zaman damgası (epoch ms).
     * @return `true` ise kelimenin tekrar edilmesi gerekiyor.
     */
    fun isDueForReview(currentTimestamp: Long = System.currentTimeMillis()): Boolean {
        return nextReviewDate <= currentTimestamp
    }

    /**
     * Kelimenin hiç tekrar edilip edilmediğini kontrol eder.
     *
     * @return `true` ise kelime henüz hiç çalışılmamış (yeni eklenen).
     */
    val isNew: Boolean
        get() = repetitions == 0 && lastReviewedDate == null
}
