# Sakarya Üniversitesi Bilgisayar Mühendisliği

# BSM515 Nesneye Dayalı Programlama Dilleri

# Mehmet Fatih Yıldız - Y255012031

## Repository

🔗 [GitHub'da Görüntüle](https://github.com/mfatihyildiz/ndpProject)

# Kuaför Yönetim Sistemi

Spring Boot ile geliştirilmiş, randevuların, çalışanların, hizmetlerin ve salon operasyonlarının yönetimini sağlayan web tabanlı bir kuaför yönetim sistemidir.

## Özellikler

- **Kullanıcı Yönetimi**: Yönetici (Admin), Çalışan (Employee) ve Müşteri (Customer) rolleri için destek
- **Randevu Planlama**: Uygunluk kontrolüyle randevu oluşturma ve yönetme
- **Salon Yönetimi**: Çalışma saatleriyle birlikte birden fazla salonu yönetme
- **Çalışan Yönetimi**: Çalışanların, yeteneklerinin ve çalışma programlarının takibi
- **Hizmet Yönetimi**: Fiyat ve süre bilgileriyle kuaför hizmetlerini yönetme
- **Uygunluk Sistemi**: Çalışanların uygunluk durumlarını ve çalışma saatlerini takip etme

## Kullanılan Teknolojiler

- **Backend**: Java 17, Spring Boot 3.5.6
- **Veritabanı**: PostgreSQL
- **Güvenlik**: Spring Security
- **Frontend**: Thymeleaf, HTML, CSS
- **Derleme Aracı**: Maven

## Proje Yapısı

```
src/
├── main/
│   ├── java/
│   │   └── com/example/ndpproject/
│   │       ├── controller/    # Controller sınıfları
│   │       ├── entity/        # JPA entity sınıfları
│   │       ├── repository/    # Veri erişim katmanı
│   │       ├── service/       # İş mantığı katmanı
│   │       └── security/      # Güvenlik yapılandırması
│   └── resources/
│       ├── templates/         # Thymeleaf şablonları
│       └── static/            # CSS ve statik dosyalar
```

## Varsayılan Roller

- **Admin**: Sisteme tam erişim; salonları, çalışanları ve randevuları yönetebilir
- **Employee (Çalışan)**: Sadece kendi randevularını görüntüleyebilir ve yönetebilir
- **Customer (Müşteri)**: Kendi randevularını oluşturabilir ve görüntüleyebilir  
