BAĞLANTI NOTLARI
1. Veritabanı Kurulumu:
   Ekli 'kütüphanedb.sql' dosyasını phpMyAdmin üzerinden import ediniz.
   Veritabanı adı 'kütüphanedb' olarak ayarlanmıştır.

2. Veritabanı Bağlantı Ayarları:
   Proje, varsayılan XAMPP/MySQL ayarları ile çalışacak şekilde yapılandırılmıştır:
   
   - Host: localhost:3306
   - Veritabanı Adı: kütüphanedb
   - Kullanıcı Adı: root
   - Şifre: (Boş)

3. Ayarları Değiştirme:
   Eğer bilgisayarınızda MySQL şifresi varsa veya farklı bir kullanıcı adı kullanıyorsanız;
   Java kaynak kodları içerisindeki sınıflarda bulunan 'baglanti()' metodundaki
   aşağıdaki alanları kendi sisteminize göre güncelleyiniz:

   String dbUser = "root";  //Kullanıcı adı
   String dbPass = "";      //Şifre
   
4. Uygulama Giriş Bilgileri (Test Hesabı):
   Uygulama açıldığında test etmek için aşağıdaki yetkili hesap bilgilerini kullanabilirsiniz:

   - Kullanıcı Adı: elanur
   - Şifre: 1234
                                          veya
   - Kullanıcı Adı: duru
   - Şifre: 1234
