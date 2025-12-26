import javax.swing.*;
import java.awt.*;

public class AnaMenu extends JFrame {

    public AnaMenu(String kullaniciRolu) {
        // Pencere Ayarları
        setTitle("Kütüphane Yönetim Sistemi - Ana Menü");
        setSize(600, 400);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null); // Ortada açılır
        setLayout(new GridLayout(3, 2, 10, 10)); // 3 Satır, 2 Sütunlu ızgara düzeni

        // 1. Üye Yönetimi Butonu
        JButton btnUye = new JButton("Üye Yönetimi");
        btnUye.addActionListener(e -> {
            JOptionPane.showMessageDialog(this, "Üye Ekranı açılacak...");
            // Buraya ileride: new UyeEkrani().setVisible(true); yazacağız
        });
        add(btnUye);

        // 2. Kitap Yönetimi Butonu
        JButton btnKitap = new JButton("Kitap Yönetimi");
        btnKitap.addActionListener(e -> {
            new KitapEkrani().setVisible(true);
        });
        add(btnKitap);

        // 3. Ödünç Verme / Teslim Alma
        JButton btnOdunc = new JButton("Ödünç / Teslim İşlemleri");
        btnOdunc.addActionListener(e -> {
            JOptionPane.showMessageDialog(this, "Ödünç Ekranı açılacak...");
        });
        add(btnOdunc);

        // 4. Cezalar ve Raporlar
        JButton btnRapor = new JButton("Cezalar ve Raporlar");
        btnRapor.addActionListener(e -> {
            JOptionPane.showMessageDialog(this, "Rapor Ekranı açılacak...");
        });
        add(btnRapor);

        // 5. Hakkında / Bilgi (Hoşgeldiniz Kısmı)
        JLabel lblBilgi = new JLabel("Hoşgeldiniz, Yetki: " + kullaniciRolu, SwingConstants.CENTER);
        lblBilgi.setFont(new Font("Arial", Font.BOLD, 14));
        add(lblBilgi);

        // 6. Çıkış Butonu
        JButton btnCikis = new JButton("Çıkış Yap");
        btnCikis.addActionListener(e -> {
            this.dispose(); // Menüyü kapat
            new GirisEkrani().setVisible(true); // Girişe geri dön
        });
        add(btnCikis);
    }
}