package com.example.ingilizcekelimekasasi.data.repository

import com.example.ingilizcekelimekasasi.data.local.dao.WordDao
import com.example.ingilizcekelimekasasi.data.local.entity.LearningDataEntity
import com.example.ingilizcekelimekasasi.data.local.entity.WordEntity
import com.example.ingilizcekelimekasasi.data.local.entity.WordWithLearningData
import com.example.ingilizcekelimekasasi.domain.model.WordItem
import com.example.ingilizcekelimekasasi.domain.repository.WordRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/**
 * [WordRepository] interface'inin somut implementasyonu.
 *
 * Room [WordDao] ile domain katmanı arasında köprü görevi görür.
 * Tüm veri dönüşümleri (Entity ↔ Domain Model) bu katmanda yapılır;
 * böylece ne DAO domain modellerini bilir, ne de ViewModel entity'leri.
 *
 * ### Mapping Stratejisi:
 * - **Entity → Domain:** [WordWithLearningData.toDomainModel] extension fonksiyonu
 *   ile Room sonuçları [WordItem]'e dönüştürülür.
 * - **Domain → Entity:** [WordItem.toLearningDataEntity] ve [WordItem.toWordEntity]
 *   extension fonksiyonları ile güncelleme işlemleri entity'ye çevrilir.
 *
 * @property wordDao Room veritabanı erişim nesnesi (Hilt tarafından inject edilir).
 */
@Singleton
class WordRepositoryImpl @Inject constructor(
    private val wordDao: WordDao
) : WordRepository {

    // ══════════════════════════════════════════════════════════════
    // EKLEME
    // ══════════════════════════════════════════════════════════════

    override suspend fun addWord(
        englishWord: String,
        turkishMeaning: String,
        exampleSentence: String?,
        sourceType: String
    ) {
        val wordEntity = WordEntity(
            englishWord = englishWord,
            turkishMeaning = turkishMeaning,
            exampleSentence = exampleSentence,
            sourceType = sourceType
        )
        wordDao.insertWordWithLearningData(wordEntity)
    }

    // ══════════════════════════════════════════════════════════════
    // OKUMA (Reaktif — Flow + Mapping)
    // ══════════════════════════════════════════════════════════════

    override fun getAllWords(): Flow<List<WordItem>> {
        return wordDao.getAllWordsWithLearningData()
            .map { list -> list.map { it.toDomainModel() } }
    }

    override fun getWordsDueForReview(currentTimestamp: Long): Flow<List<WordItem>> {
        return wordDao.getWordsDueForReview(currentTimestamp)
            .map { list -> list.map { it.toDomainModel() } }
    }

    override suspend fun getWordById(wordId: Long): WordItem? {
        return wordDao.getWordWithLearningDataById(wordId)?.toDomainModel()
    }

    override fun getTotalWordCount(): Flow<Int> {
        return wordDao.getTotalWordCount()
    }

    override fun getDueWordCount(currentTimestamp: Long): Flow<Int> {
        return wordDao.getDueWordCount(currentTimestamp)
    }

    // ══════════════════════════════════════════════════════════════
    // GÜNCELLEME
    // ══════════════════════════════════════════════════════════════

    override suspend fun updateLearningData(wordItem: WordItem) {
        wordDao.updateLearningData(wordItem.toLearningDataEntity())
    }

    override suspend fun updateWord(wordItem: WordItem) {
        wordDao.updateWord(wordItem.toWordEntity())
    }

    // ══════════════════════════════════════════════════════════════
    // SİLME
    // ══════════════════════════════════════════════════════════════

    override suspend fun deleteWord(wordId: Long) {
        wordDao.deleteWordById(wordId)
    }
}

// ══════════════════════════════════════════════════════════════════
// MAPPER EXTENSION FONKSİYONLARI
// ══════════════════════════════════════════════════════════════════

/**
 * Room ilişkisel sonucunu ([WordWithLearningData]) domain modeline ([WordItem]) dönüştürür.
 *
 * Bu fonksiyon, veritabanının iç yapısını (entity isimleri, sütun adları)
 * domain katmanından gizler. Yeni bir alan eklendiğinde yalnızca bu
 * mapper güncellenir; ViewModel ve UI kodu etkilenmez.
 */
internal fun WordWithLearningData.toDomainModel(): WordItem {
    return WordItem(
        wordId = word.wordId,
        englishWord = word.englishWord,
        turkishMeaning = word.turkishMeaning,
        exampleSentence = word.exampleSentence,
        sourceType = word.sourceType,
        createdAt = word.createdAt,
        easinessFactor = learningData.easinessFactor,
        interval = learningData.interval,
        repetitions = learningData.repetitions,
        nextReviewDate = learningData.nextReviewDate,
        lastReviewedDate = learningData.lastReviewedDate,
        learningId = learningData.learningId
    )
}

/**
 * Domain modelini ([WordItem]) SM2 öğrenme entity'sine ([LearningDataEntity]) dönüştürür.
 *
 * Quiz sonrasında SM2 algoritmasının hesapladığı yeni değerleri
 * veritabanına yazmak için kullanılır.
 */
internal fun WordItem.toLearningDataEntity(): LearningDataEntity {
    return LearningDataEntity(
        learningId = learningId,
        wordId = wordId,
        easinessFactor = easinessFactor,
        interval = interval,
        repetitions = repetitions,
        nextReviewDate = nextReviewDate,
        lastReviewedDate = lastReviewedDate
    )
}

/**
 * Domain modelini ([WordItem]) kelime entity'sine ([WordEntity]) dönüştürür.
 *
 * Kelimenin statik bilgilerini (İngilizce, Türkçe, örnek cümle)
 * güncellemek için kullanılır.
 */
internal fun WordItem.toWordEntity(): WordEntity {
    return WordEntity(
        wordId = wordId,
        englishWord = englishWord,
        turkishMeaning = turkishMeaning,
        exampleSentence = exampleSentence,
        sourceType = sourceType,
        createdAt = createdAt
    )
}
