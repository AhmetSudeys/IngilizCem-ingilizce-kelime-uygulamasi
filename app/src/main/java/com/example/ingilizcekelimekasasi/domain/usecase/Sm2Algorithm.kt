package com.example.ingilizcekelimekasasi.domain.usecase

import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.max
import kotlin.math.roundToInt

/**
 * SM2 algoritmasının hesaplama sonuçlarını taşıyan veri sınıfı.
 *
 * [Sm2Algorithm.calculateNextReview] fonksiyonunun dönüş tipidir.
 * İçerdiği değerler doğrudan [LearningDataEntity]'ye yazılmak üzere tasarlanmıştır.
 *
 * @property interval Bir sonraki tekrar için beklenecek gün sayısı.
 * @property repetitions Art arda başarılı hatırlama sayacı.
 * @property easinessFactor Güncellenmiş kolaylık faktörü (min 1.3).
 * @property nextReviewDate Bir sonraki tekrar tarihi (epoch milisaniye).
 */
data class Sm2Result(
    val interval: Int,
    val repetitions: Int,
    val easinessFactor: Float,
    val nextReviewDate: Long
)

/**
 * SuperMemo-2 (SM2) Aralıklı Tekrar Algoritması.
 *
 * Piotr Woźniak tarafından 1987'de geliştirilen bu algoritma,
 * uzun süreli hafıza oluşturmak için bilimsel olarak kanıtlanmış
 * en etkili öğrenme yöntemlerinden biridir.
 *
 * ### Çalışma Prensibi:
 * Kullanıcı bir kelimeyi ne kadar kolay hatırladığını 0-5 arası puanlar.
 * Algoritma bu geri bildirime göre:
 * 1. **Kolaylık Faktörünü (EF)** günceller — kelime ne kadar kolay/zor.
 * 2. **Tekrar Aralığını (Interval)** hesaplar — kaç gün sonra tekrar sorulacak.
 * 3. **Tekrar Sayacını (Repetitions)** yönetir — ardışık başarı serisi.
 *
 * ### Kalite Puanları:
 * | Puan | Anlam | Kullanıcı Hissi |
 * |------|-------|-----------------|
 * | 0 | Tamamen unuttum | "Hiç bilmiyorum" |
 * | 1 | Yanlış, doğruyu görünce hatırladım | "Ah, buydu!" |
 * | 2 | Yanlış, doğruyu görünce kolay geldi | "Biliyordum aslında" |
 * | 3 | Doğru, ama çok zorlandım | "Zar zor hatırladım" |
 * | 4 | Doğru, biraz düşündüm | "Düşündükten sonra buldum" |
 * | 5 | Doğru, anında bildim | "Çok kolaydı!" |
 *
 * ### Matematiksel Formüller (Standart SM2):
 *
 * **EF Güncellemesi:**
 * ```
 * EF' = EF + (0.1 - (5 - q) * (0.08 + (5 - q) * 0.02))
 * EF' = max(EF', 1.3)  // Alt sınır güvenliği
 * ```
 *
 * **Interval Hesaplaması (quality ≥ 3):**
 * ```
 * n=1 → I(1) = 1 gün
 * n=2 → I(2) = 6 gün
 * n>2 → I(n) = round(I(n-1) * EF)
 * ```
 *
 * **Başarısızlık Durumu (quality < 3):**
 * ```
 * repetitions = 0  (seri kırılır)
 * interval = 1     (ertesi gün tekrar)
 * EF değişmez      (cezalandırılmaz)
 * ```
 *
 * > Bu sınıf **saf Kotlin** kodudur; Android framework bağımlılığı yoktur.
 * > Bu sayede JVM unit testleriyle kolayca doğrulanabilir.
 *
 * @see Sm2Result Hesaplama sonuçlarını taşıyan veri sınıfı.
 * @see <a href="https://www.supermemo.com/en/blog/application-of-a-computer-to-improve-the-results-obtained-in-working-with-the-supermemo-method">
 *   Orijinal SM2 Makalesi (P. Woźniak, 1990)</a>
 */
@Singleton
class Sm2Algorithm @Inject constructor() {

    companion object {
        /** Kolaylık faktörünün düşebileceği minimum değer. */
        const val MIN_EASINESS_FACTOR = 1.3f

        /** Yeni eklenen kelimelerin varsayılan kolaylık faktörü. */
        const val DEFAULT_EASINESS_FACTOR = 2.5f

        /** Bir günün milisaniye cinsinden değeri (interval → timestamp dönüşümü için). */
        private const val ONE_DAY_MS = 86_400_000L
    }

    /**
     * SM2 algoritmasının ana hesaplama fonksiyonu.
     *
     * Kullanıcının quiz geri bildirimine göre yeni öğrenme parametrelerini
     * hesaplar. Fonksiyon tamamen **pure** (yan etkisiz) ve **deterministik**tir;
     * aynı girdiler her zaman aynı çıktıyı üretir.
     *
     * ### Algoritma Akışı:
     * ```
     * quality >= 3 (başarılı)          quality < 3 (başarısız)
     *         │                                │
     *    repetitions++                   repetitions = 0
     *         │                           interval = 1
     *    ┌────┴────┐                     EF değişmez
     *    │ rep=1 → I=1                        │
     *    │ rep=2 → I=6                  nextReview = yarın
     *    │ rep>2 → I=I*EF
     *    └────┬────┘
     *     EF güncelle
     *         │
     *   nextReview = bugün + I gün
     * ```
     *
     * @param quality Kullanıcının kalite puanı (0-5 arası, dahil).
     *                0-2: başarısız, 3-5: başarılı.
     * @param previousInterval Önceki tekrar aralığı (gün cinsinden).
     * @param previousRepetitions Önceki ardışık başarılı tekrar sayısı.
     * @param previousEasinessFactor Önceki kolaylık faktörü.
     * @return Güncellenmiş SM2 parametrelerini içeren [Sm2Result].
     *
     * @throws IllegalArgumentException [quality] 0-5 aralığında değilse.
     */
    fun calculateNextReview(
        quality: Int,
        previousInterval: Int,
        previousRepetitions: Int,
        previousEasinessFactor: Float
    ): Sm2Result {
        require(quality in 0..5) {
            "Kalite puanı 0-5 arasında olmalıdır, verilen: $quality"
        }

        val newRepetitions: Int
        val newInterval: Int
        val newEasinessFactor: Float

        if (quality >= 3) {
            // ── BAŞARILI HATIRLAMA ─────────────────────────────
            // Tekrar sayacını artır ve aralığı hesapla.
            newRepetitions = previousRepetitions + 1

            newInterval = when (newRepetitions) {
                1 -> 1                  // İlk başarılı tekrar: 1 gün sonra
                2 -> 6                  // İkinci başarılı tekrar: 6 gün sonra
                else -> {               // Üçüncü ve sonrası: önceki × EF
                    (previousInterval * previousEasinessFactor).roundToInt()
                }
            }

            // EF güncelleme formülü (Standart SM2)
            // EF' = EF + (0.1 - (5-q) * (0.08 + (5-q) * 0.02))
            val qualityDiff = 5 - quality
            newEasinessFactor = max(
                MIN_EASINESS_FACTOR,
                previousEasinessFactor + (0.1f - qualityDiff * (0.08f + qualityDiff * 0.02f))
            )
        } else {
            // ── BAŞARISIZ HATIRLAMA ────────────────────────────
            // Seri kırılır, ertesi gün tekrar sorulur.
            // EF cezalandırılmaz (standart SM2 davranışı).
            newRepetitions = 0
            newInterval = 1
            newEasinessFactor = previousEasinessFactor
        }

        // Bir sonraki tekrar tarihini hesapla (şu an + interval gün)
        val nextReviewDate = System.currentTimeMillis() + (newInterval * ONE_DAY_MS)

        return Sm2Result(
            interval = newInterval,
            repetitions = newRepetitions,
            easinessFactor = newEasinessFactor,
            nextReviewDate = nextReviewDate
        )
    }
}
