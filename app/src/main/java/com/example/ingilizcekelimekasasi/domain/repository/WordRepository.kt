package com.example.ingilizcekelimekasasi.domain.repository

import com.example.ingilizcekelimekasasi.domain.model.WordItem
import kotlinx.coroutines.flow.Flow

/**
 * Kelime işlemleri için domain katmanı sözleşmesi (contract).
 *
 * Bu interface, veri kaynağının (Room, API vb.) nasıl çalıştığını
 * bilmeksizin ViewModel/UseCase katmanının hangi işlemlere ihtiyaç
 * duyduğunu tanımlar. Clean Architecture'da **Dependency Inversion**
 * prensibini sağlar:
 * - Domain katmanı bu interface'i **tanımlar**.
 * - Data katmanı bu interface'i **implemente eder**.
 * - UI/ViewModel katmanı bu interface'e **bağımlıdır** (somut sınıfa değil).
 *
 * ### Threading Stratejisi:
 * - Yazma işlemleri (insert/update/delete): `suspend` fonksiyonlar.
 * - Okuma işlemleri: [Flow] ile reaktif veri akışı.
 *   Flow, veritabanı değiştiğinde otomatik yeniden emit eder.
 */
interface WordRepository {

    // ══════════════════════════════════════════════════════════════
    // EKLEME
    // ══════════════════════════════════════════════════════════════

    /**
     * Yeni bir kelime ve bağlı SM2 öğrenme verisini atomik olarak ekler.
     *
     * Kelime eklendikten sonra, SM2 başlangıç değerleriyle
     * (EF=2.5, interval=0, repetitions=0) bir öğrenme kaydı
     * otomatik oluşturulur.
     *
     * @param englishWord İngilizce kelime veya kelime öbeği.
     * @param turkishMeaning Türkçe karşılığı.
     * @param exampleSentence Örnek cümle (opsiyonel).
     * @param sourceType Ekleme yöntemi: "OCR" veya "MANUAL".
     */
    suspend fun addWord(
        englishWord: String,
        turkishMeaning: String,
        exampleSentence: String? = null,
        sourceType: String = "MANUAL"
    )

    // ══════════════════════════════════════════════════════════════
    // OKUMA (Reaktif — Flow)
    // ══════════════════════════════════════════════════════════════

    /**
     * Tüm kelimeleri SM2 verileriyle birlikte getirir.
     *
     * **Kasam** ekranı bu akışı gözlemler. Yeni kelime eklendiğinde,
     * silindiğinde veya güncellendiğinde otomatik yeniden emit eder.
     *
     * @return En yeniden eskiye sıralanmış kelime listesi akışı.
     */
    fun getAllWords(): Flow<List<WordItem>>

    /**
     * Tekrar vakti gelmiş kelimeleri getirir (SM2 quiz sorgusu).
     *
     * `nextReviewDate ≤ currentTimestamp` koşulunu sağlayan kelimeleri
     * en acil olandan başlayarak sıralı döndürür.
     *
     * @param currentTimestamp Şu anki zaman damgası (epoch ms).
     * @return Quiz için hazır kelime listesi akışı.
     */
    fun getWordsDueForReview(currentTimestamp: Long): Flow<List<WordItem>>

    /**
     * Belirli bir kelimeyi ID'siyle getirir.
     *
     * @param wordId Aranan kelimenin kimliği.
     * @return Bulunan kelime veya null.
     */
    suspend fun getWordById(wordId: Long): WordItem?

    /**
     * Toplam kelime sayısını reaktif olarak döndürür.
     *
     * @return Kelime sayısı akışı.
     */
    fun getTotalWordCount(): Flow<Int>

    /**
     * Bugün tekrar edilmesi gereken kelime sayısını döndürür.
     *
     * @param currentTimestamp Şu anki zaman damgası (epoch ms).
     * @return Bekleyen quiz kelimesi sayısı akışı.
     */
    fun getDueWordCount(currentTimestamp: Long): Flow<Int>

    // ══════════════════════════════════════════════════════════════
    // GÜNCELLEME
    // ══════════════════════════════════════════════════════════════

    /**
     * Quiz sonrasında SM2 algoritmasının hesapladığı yeni değerleri kaydeder.
     *
     * @param wordItem Güncellenmiş SM2 parametrelerini içeren domain modeli.
     *                 [WordItem.learningId] eşleşen kayıt güncellenir.
     */
    suspend fun updateLearningData(wordItem: WordItem)

    /**
     * Bir kelimenin statik bilgilerini günceller.
     *
     * @param wordItem Güncellenmiş kelime bilgilerini içeren domain modeli.
     */
    suspend fun updateWord(wordItem: WordItem)

    // ══════════════════════════════════════════════════════════════
    // SİLME
    // ══════════════════════════════════════════════════════════════

    /**
     * Bir kelimeyi ID'si ile siler. CASCADE ile öğrenme verisi de silinir.
     *
     * @param wordId Silinecek kelimenin kimliği.
     */
    suspend fun deleteWord(wordId: Long)
}
