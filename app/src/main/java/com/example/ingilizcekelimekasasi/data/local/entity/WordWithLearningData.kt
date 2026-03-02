package com.example.ingilizcekelimekasasi.data.local.entity

import androidx.room.Embedded
import androidx.room.Relation

/**
 * Kelime ve öğrenme verisini tek bir nesnede birleştiren ilişkisel POJO.
 *
 * Room'un `@Relation` mekanizmasını kullanarak [WordEntity] ile
 * [LearningDataEntity] arasındaki 1:1 ilişkiyi UI katmanına
 * tek parça halinde sunar.
 *
 * Bu sınıf bir `@Entity` değildir; yalnızca DAO sorgularının
 * dönüş tipi olarak kullanılır (veri okuma amaçlı).
 *
 * ### Kullanım Örneği:
 * ```kotlin
 * @Transaction
 * @Query("SELECT * FROM words")
 * fun getAllWordsWithLearningData(): Flow<List<WordWithLearningData>>
 * ```
 *
 * @property word Kelimenin statik bilgileri (İngilizce, Türkçe anlam vb.).
 * @property learningData SM2 algoritması parametreleri (EF, interval, tekrar tarihi vb.).
 */
data class WordWithLearningData(
    @Embedded
    val word: WordEntity,

    @Relation(
        parentColumn = "wordId",
        entityColumn = "wordId"
    )
    val learningData: LearningDataEntity
)
