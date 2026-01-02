import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class GirisEkrani extends JFrame {
    private JTextField txtKullaniciAdi;
    private JPasswordField txtSifre;

    public GirisEkrani() {
        // Pencere Ayarları
        setTitle("Kütüphane Otomasyonu - Giriş");
        setSize(400, 250);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(null);

        // Kullanıcı Adı Bileşenleri
        JLabel lblKadi = new JLabel("Kullanıcı Adı:");
        lblKadi.setBounds(50, 50, 100, 30);
        add(lblKadi);

        txtKullaniciAdi = new JTextField();
        txtKullaniciAdi.setBounds(150, 50, 150, 30);
        add(txtKullaniciAdi);

        // Şifre Bileşenleri
        JLabel lblSifre = new JLabel("Şifre:");
        lblSifre.setBounds(50, 100, 100, 30);
        add(lblSifre);

        txtSifre = new JPasswordField();
        txtSifre.setBounds(150, 100, 150, 30);
        add(txtSifre);

        // Giriş Butonu
        JButton btnGiris = new JButton("Giriş Yap");
        btnGiris.setBounds(150, 150, 150, 40);
        add(btnGiris);

        // Buton Tıklama Olayı
        btnGiris.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                girisYap();
            }
        });
    }

    private void girisYap() {
        String kAdi = txtKullaniciAdi.getText();
        String sifre = new String(txtSifre.getPassword());

        // Veritabanı Bağlantı Bilgileri
        String url = "jdbc:mysql://localhost:3306/kütüphanedb?useUnicode=true&characterEncoding=utf8";
        String dbUser = "root";
        String dbPass = "";

        try {
            Connection baglanti = DriverManager.getConnection(url, dbUser, dbPass);

            // SQL Sorgusu
            String sql = "SELECT * FROM KULLANICI WHERE KullaniciAdi = ? AND Sifre = ?";
            PreparedStatement sorgu = baglanti.prepareStatement(sql);
            sorgu.setString(1, kAdi);
            sorgu.setString(2, sifre);

            ResultSet sonuc = sorgu.executeQuery();

            if (sonuc.next()) {
                // Giriş Başarılı ise Rolü Alıyoruz
                String rol = sonuc.getString("Rol");

                // --- GÜNCELLEDİĞİMİZ MESAJ KUTUSU ---
                JOptionPane.showMessageDialog(this,
                        "Giriş Başarılı! Rol: " + rol,
                        "Message",
                        JOptionPane.INFORMATION_MESSAGE);

                // Mevcut ekranı kapat ve Ana Menüyü aç
                this.dispose();
                new AnaMenu(rol).setVisible(true);
            } else {
                // Hatalı Giriş Durumu
                JOptionPane.showMessageDialog(this,
                        "Hatalı Kullanıcı Adı veya Şifre!",
                        "Hata",
                        JOptionPane.ERROR_MESSAGE);
            }

            baglanti.close();

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Bağlantı Hatası: " + ex.getMessage());
        }
    }

    public static void main(String[] args) {
        // Look and Feel ayarı (Sistemin kendi pencere stilini kullanması için - Opsiyonel)
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }

        SwingUtilities.invokeLater(() -> {
            new GirisEkrani().setVisible(true);
        });
    }
}