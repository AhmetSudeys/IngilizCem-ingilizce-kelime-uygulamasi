package com.example.ingilizcekelimekasasi.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Öğrenme verisi tablosu — SM2 (Spaced Repetition) algoritmasının
 * çalışması için gerekli dinamik parametreleri tutar.
 *
 * Her [WordEntity] kaydıyla **1:1** ilişkiye sahiptir.
 * [wordId] sütunu hem Foreign Key hem de Unique kısıtına sahiptir,
 * böylece bir kelimeye birden fazla öğrenme kaydı bağlanamaz.
 *
 * Word silindiğinde ilgili öğrenme verisi de CASCADE ile otomatik silinir.
 *
 * ### SM2 Parametreleri:
 * - **Easiness Factor (EF):** Kelimenin ne kadar kolay hatırlandığını gösteren katsayı.
 *   Başlangıç değeri 2.5, minimum 1.3 olabilir. Yükseldikçe kelime daha kolay kabul edilir.
 * - **Interval:** Bir sonraki tekrar için beklenecek gün sayısı.
 * - **Repetitions:** Kelimenin art arda başarıyla hatırlanma sayısı.
 *   Kullanıcı kelimeyi bilemezse (quality < 3) sıfırlanır.
 * - **Next Review Date:** Kelimenin tekrar sorulacağı tarih (epoch ms).
 * - **Last Reviewed Date:** Son tekrar yapılma zamanı.
 *
 * @property learningId Otomatik artan birincil anahtar.
 * @property wordId [WordEntity] tablosuyla 1:1 bağlantı (Foreign Key + Unique).
 * @property easinessFactor SM2 kolaylık faktörü. Başlangıç: 2.5f, Min: 1.3f.
 * @property interval Gün cinsinden tekrar aralığı. Başlangıç: 0.
 * @property repetitions Art arda doğru hatırlama sayısı. Başlangıç: 0.
 * @property nextReviewDate Bir sonraki tekrar tarihi (epoch ms). Varsayılan: oluşturulma anı.
 * @property lastReviewedDate En son tekrar yapılma tarihi (epoch ms). Null ise hiç tekrar edilmemiş.
 */
@Entity(
    tableName = "learning_data",
    foreignKeys = [
        ForeignKey(
            entity = WordEntity::class,
            parentColumns = ["wordId"],
            childColumns = ["wordId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["wordId"], unique = true)]
)
data class LearningDataEntity(
    @PrimaryKey(autoGenerate = true)
    val learningId: Long = 0,

    val wordId: Long,

    val easinessFactor: Float = 2.5f,

    val interval: Int = 0,

    val repetitions: Int = 0,

    val nextReviewDate: Long = System.currentTimeMillis(),

    val lastReviewedDate: Long? = null
)
