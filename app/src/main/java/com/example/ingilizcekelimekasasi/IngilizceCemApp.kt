package com.example.ingilizcekelimekasasi

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

/**
 * İngilizCem Kasası uygulamasının kök Application sınıfı.
 *
 * `@HiltAndroidApp` anotasyonu, Hilt'in dependency injection
 * container'ını (bileşen ağacını) bu sınıf üzerinden başlatır.
 * Uygulama açıldığında Hilt otomatik olarak:
 * 1. [SingletonComponent] oluşturur (uygulama ömrü boyunca yaşar).
 * 2. Tüm `@Module` sınıflarındaki bağımlılıkları hazırlar.
 * 3. `@Inject` ile işaretlenmiş alanları/constructor'ları doldurur.
 *
 * > **ÖNEMLİ:** Bu sınıf `AndroidManifest.xml` dosyasında
 * > `android:name=".IngilizceCemApp"` olarak tanımlanmalıdır.
 * > Aksi halde Hilt çalışmaz ve uygulama başlatılamaz.
 */
@HiltAndroidApp
class IngilizceCemApp : Application()
