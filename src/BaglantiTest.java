import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class BaglantiTest {
    public static void main(String[] args) {
        // XAMPP varsayılan ayarları:
        // Kullanıcı: root
        // Şifre: (boş, yani hiçbir şey yazmıyoruz)
        String url = "jdbc:mysql://localhost:3306/kütüphanedb?useUnicode=true&characterEncoding=utf8";
        String kullanici = "root";
        String sifre = "";

        try {
            // Bağlantıyı kurmaya çalışıyoruz
            Connection baglanti = DriverManager.getConnection(url, kullanici, sifre);

            if (baglanti != null) {
                System.out.println("--------------------------------------------");
                System.out.println("BAŞARILI! Veritabanına bağlandın.");
                System.out.println("--------------------------------------------");
            }
        } catch (SQLException e) {
            System.out.println("HATA VAR! Bağlanamadı.");
            System.out.println("Hata Mesajı: " + e.getMessage());
        }
    }
}