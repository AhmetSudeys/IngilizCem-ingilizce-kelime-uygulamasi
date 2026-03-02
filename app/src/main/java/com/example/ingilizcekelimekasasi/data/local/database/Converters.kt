package com.example.ingilizcekelimekasasi.data.local.database

import androidx.room.TypeConverter
import java.util.Date

/**
 * Room veritabanı için tip dönüştürücüler.
 *
 * Room, [Date] gibi karmaşık tipleri doğrudan saklayamaz.
 * Bu sınıf, veritabanı sütunlarında saklanan ilkel tipler ile
 * Kotlin/Java nesneleri arasındaki dönüşümleri sağlar.
 *
 * ### Dönüşüm Stratejisi:
 * - **Date ↔ Long:** Tarihler, epoch milisaniye (Unix timestamp)
 *   olarak `Long` tipinde saklanır. Bu yaklaşım zaman dilimi
 *   bağımsızdır ve sıralama/karşılaştırma işlemlerinde verimlidir.
 *
 * > **Not:** Projede tarih alanları zaten `Long` olarak tanımlandığından,
 * > bu converter'lar ihtiyaç duyulduğunda (örn. domain modellerde `Date`
 * > kullanılırsa) hazır olacaktır. Mevcut entity'lerde doğrudan `Long`
 * > kullanıldığı için şu an aktif olarak tetiklenmez ancak genişleme
 * > durumları için veritabanına kayıtlıdır.
 */
class Converters {

    /**
     * [Long] tipindeki epoch milisaniye değerini [Date] nesnesine dönüştürür.
     *
     * @param timestamp Epoch milisaniye değeri veya null.
     * @return Karşılık gelen [Date] nesnesi veya null.
     */
    @TypeConverter
    fun fromTimestamp(timestamp: Long?): Date? {
        return timestamp?.let { Date(it) }
    }

    /**
     * [Date] nesnesini epoch milisaniye ([Long]) değerine dönüştürür.
     *
     * @param date Dönüştürülecek tarih nesnesi veya null.
     * @return Epoch milisaniye değeri veya null.
     */
    @TypeConverter
    fun dateToTimestamp(date: Date?): Long? {
        return date?.time
    }
}
