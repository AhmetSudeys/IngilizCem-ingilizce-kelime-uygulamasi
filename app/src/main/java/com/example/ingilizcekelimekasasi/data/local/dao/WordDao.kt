package com.example.ingilizcekelimekasasi.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.example.ingilizcekelimekasasi.data.local.entity.LearningDataEntity
import com.example.ingilizcekelimekasasi.data.local.entity.WordEntity
import com.example.ingilizcekelimekasasi.data.local.entity.WordWithLearningData
import kotlinx.coroutines.flow.Flow

/**
 * Kelime veritabanı işlemleri için Data Access Object.
 *
 * Tüm yazma (insert/update/delete) işlemleri `suspend` fonksiyonlarla,
 * tüm okuma işlemleri ise reaktif [Flow] ile tanımlanmıştır.
 *
 * ### Tasarım Kararları:
 * - **@Transaction ile atomik ekleme:** [insertWordWithLearningData] fonksiyonu,
 *   bir kelime ve ona bağlı öğrenme verisini tek bir veritabanı işlemi
 *   (transaction) içinde ekler. Bu sayede yarım kalmış veri oluşması engellenir.
 * - **Flow ile reaktif okuma:** UI katmanı, veritabanı değişikliklerini
 *   otomatik olarak gözlemler; ekstra yenileme çağrısı gerekmez.
 * - **SM2 sorgusu:** [getWordsDueForReview] fonksiyonu, tekrar vakti
 *   gelmiş kelimeleri timestamp karşılaştırmasıyla filtreler.
 */
@Dao
interface WordDao {

    // ══════════════════════════════════════════════════════════════
    // EKLEME İŞLEMLERİ
    // ══════════════════════════════════════════════════════════════

    /**
     * Veritabanına yeni bir kelime kaydı ekler.
     *
     * @param word Eklenecek kelime entity'si.
     * @return Eklenen satırın otomatik üretilen [WordEntity.wordId] değeri.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWord(word: WordEntity): Long

    /**
     * Bir kelimeye ait öğrenme verisi kaydı ekler.
     *
     * @param learningData Eklenecek öğrenme verisi entity'si.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLearningData(learningData: LearningDataEntity)

    /**
     * Bir kelimeyi ve ona bağlı SM2 öğrenme verisini **atomik** olarak ekler.
     *
     * Önce [WordEntity] eklenir ve dönen `wordId` değeri alınır.
     * Ardından bu `wordId` ile [LearningDataEntity] oluşturulup eklenir.
     * Her iki işlem tek bir transaction içinde gerçekleşir;
     * hata durumunda tüm değişiklikler geri alınır (rollback).
     *
     * @param word Eklenecek kelime (wordId = 0 olmalı, auto-generate edilir).
     * @param learningData Opsiyonel öğrenme verisi. Verilmezse varsayılan
     *                     SM2 başlangıç değerleri (EF=2.5, interval=0, repetitions=0) kullanılır.
     */
    @Transaction
    suspend fun insertWordWithLearningData(
        word: WordEntity,
        learningData: LearningDataEntity = LearningDataEntity(wordId = 0)
    ) {
        val generatedWordId = insertWord(word)
        insertLearningData(learningData.copy(wordId = generatedWordId))
    }

    // ══════════════════════════════════════════════════════════════
    // OKUMA İŞLEMLERİ (Reaktif — Flow)
    // ══════════════════════════════════════════════════════════════

    /**
     * Tüm kelimeleri öğrenme verileriyle birlikte getirir.
     *
     * **Kasam** ekranında kullanılır. Kelimeler oluşturulma tarihine göre
     * en yeniden en eskiye sıralanır.
     *
     * @return Tüm kelime + öğrenme verisi çiftlerinin reaktif akışı.
     */
    @Transaction
    @Query("SELECT * FROM words ORDER BY createdAt DESC")
    fun getAllWordsWithLearningData(): Flow<List<WordWithLearningData>>

    /**
     * SM2 Algoritması için **en kritik sorgu**.
     *
     * Tekrar vakti gelmiş (nextReviewDate ≤ şu anki zaman) kelimeleri
     * öğrenme verileriyle birlikte getirir. Quiz ekranı bu sorguyu kullanarak
     * o gün çalışılması gereken kelimeleri belirler.
     *
     * Sonuçlar en acil olandan (en eski nextReviewDate) en yeniye doğru sıralanır.
     *
     * @param currentTimestamp Şu anki zaman damgası (epoch ms). Genellikle
     *                        `System.currentTimeMillis()` ile sağlanır.
     * @return Tekrar vakti gelmiş kelime + öğrenme verisi çiftlerinin reaktif akışı.
     */
    @Transaction
    @Query(
        """
        SELECT w.* FROM words w
        INNER JOIN learning_data ld ON w.wordId = ld.wordId
        WHERE ld.nextReviewDate <= :currentTimestamp
        ORDER BY ld.nextReviewDate ASC
        """
    )
    fun getWordsDueForReview(currentTimestamp: Long): Flow<List<WordWithLearningData>>

    /**
     * Belirli bir kelimeyi ID'sine göre öğrenme verisiyle birlikte getirir.
     *
     * @param wordId Aranan kelimenin birincil anahtarı.
     * @return Kelime + öğrenme verisi çifti ya da null (bulunamazsa).
     */
    @Transaction
    @Query("SELECT * FROM words WHERE wordId = :wordId")
    suspend fun getWordWithLearningDataById(wordId: Long): WordWithLearningData?

    /**
     * Veritabanındaki toplam kelime sayısını reaktif olarak döndürür.
     *
     * Dashboard ekranındaki istatistik kartları için kullanılır.
     *
     * @return Toplam kelime sayısının reaktif akışı.
     */
    @Query("SELECT COUNT(*) FROM words")
    fun getTotalWordCount(): Flow<Int>

    /**
     * Bugün tekrar edilmesi gereken kelime sayısını döndürür.
     *
     * Dashboard'daki bildirim rozeti (badge) ve WorkManager
     * bildirim kararları için kullanılır.
     *
     * @param currentTimestamp Şu anki zaman damgası (epoch ms).
     * @return Tekrar bekleyen kelime sayısının reaktif akışı.
     */
    @Query(
        """
        SELECT COUNT(*) FROM learning_data
        WHERE nextReviewDate <= :currentTimestamp
        """
    )
    fun getDueWordCount(currentTimestamp: Long): Flow<Int>

    // ══════════════════════════════════════════════════════════════
    // GÜNCELLEME İŞLEMLERİ
    // ══════════════════════════════════════════════════════════════

    /**
     * Bir kelimenin öğrenme verisini günceller.
     *
     * Quiz ekranında kullanıcı cevap verdikten sonra SM2 algoritması
     * yeni EF, interval, repetitions ve nextReviewDate değerlerini hesaplar.
     * Bu fonksiyon hesaplanan değerleri veritabanına yazar.
     *
     * @param learningData Güncellenmiş SM2 parametrelerini içeren entity.
     *                     [LearningDataEntity.learningId] eşleşen kayıt güncellenir.
     */
    @Update
    suspend fun updateLearningData(learningData: LearningDataEntity)

    /**
     * Bir kelime kaydını günceller (İngilizce, Türkçe anlam, örnek cümle vb.).
     *
     * @param word Güncellenmiş kelime entity'si.
     */
    @Update
    suspend fun updateWord(word: WordEntity)

    // ══════════════════════════════════════════════════════════════
    // SİLME İŞLEMLERİ
    // ══════════════════════════════════════════════════════════════

    /**
     * Bir kelimeyi veritabanından siler.
     *
     * Foreign Key üzerindeki `CASCADE` kuralı sayesinde,
     * ilişkili [LearningDataEntity] kaydı da otomatik olarak silinir.
     *
     * @param word Silinecek kelime entity'si.
     */
    @Delete
    suspend fun deleteWord(word: WordEntity)

    /**
     * Belirli bir kelimeyi ID'sine göre siler.
     *
     * @param wordId Silinecek kelimenin birincil anahtarı.
     */
    @Query("DELETE FROM words WHERE wordId = :wordId")
    suspend fun deleteWordById(wordId: Long)
}
