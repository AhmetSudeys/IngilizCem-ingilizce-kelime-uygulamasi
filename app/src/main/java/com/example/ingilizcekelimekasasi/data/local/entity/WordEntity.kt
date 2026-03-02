package com.example.ingilizcekelimekasasi.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Kelime tablosu — Bir kelimenin değişmeyen (statik) bilgilerini tutar.
 *
 * Bu entity, 3NF (Üçüncü Normal Form) kurallarına uygun olarak
 * yalnızca kelimenin kendine ait sabit niteliklerini barındırır.
 * Öğrenme sürecine ait dinamik veriler [LearningDataEntity] tablosunda saklanır.
 *
 * @property wordId Otomatik artan birincil anahtar.
 * @property englishWord İngilizce kelime veya kelime öbeği.
 * @property turkishMeaning Kelimenin Türkçe karşılığı.
 * @property exampleSentence Kelimenin kullanıldığı örnek bir İngilizce cümle (opsiyonel).
 * @property sourceType Kelimenin eklenme yöntemi: "OCR" (kamera taraması) veya "MANUAL" (elle giriş).
 * @property createdAt Kelimenin veritabanına eklendiği zaman damgası (epoch milisaniye).
 */
@Entity(tableName = "words")
data class WordEntity(
    @PrimaryKey(autoGenerate = true)
    val wordId: Long = 0,

    val englishWord: String,

    val turkishMeaning: String,

    val exampleSentence: String? = null,

    val sourceType: String = "MANUAL",

    val createdAt: Long = System.currentTimeMillis()
)
