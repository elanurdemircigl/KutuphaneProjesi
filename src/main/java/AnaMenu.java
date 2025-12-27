import javax.swing.*;
import java.awt.*;

public class AnaMenu extends JFrame {

    public AnaMenu(String kullaniciRolu) {
        // --- PENCERE AYARLARI ---
        setTitle("Kütüphane Yönetim Sistemi - Ana Menü");
        setSize(700, 500); // Butonlar arttığı için boyutu biraz büyüttük
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null); // Ekranın ortasında açılır
        setLayout(new BorderLayout(10, 10)); // Elemanlar arası 10px boşluk

        // --- 1. ÜST KISIM: HOŞGELDİNİZ METNİ ---
        // Ödevde istenen: "Hoş geldiniz, [KullanıcıAdı]"
        JLabel lblBilgi = new JLabel("Hoş geldiniz, Yetki: " + kullaniciRolu, SwingConstants.CENTER);
        lblBilgi.setFont(new Font("Arial", Font.BOLD, 18));
        lblBilgi.setBorder(BorderFactory.createEmptyBorder(20, 10, 10, 10)); // Kenar boşluğu
        add(lblBilgi, BorderLayout.NORTH);

        // --- 2. ORTA KISIM: BUTONLAR ---
        // Toplam 7 buton var, düzenli durması için 4 Satır x 2 Sütun yapıyoruz
        JPanel pnlButonlar = new JPanel(new GridLayout(4, 2, 15, 15));
        pnlButonlar.setBorder(BorderFactory.createEmptyBorder(10, 40, 40, 40)); // İçerden boşluk

        // 1. Üye Yönetimi
        JButton btnUye = new JButton("Üye Yönetimi");
        btnUye.setFont(new Font("Arial", Font.PLAIN, 14));
        btnUye.addActionListener(e -> new UyeEkrani().setVisible(true));
        pnlButonlar.add(btnUye);

        // 2. Kitap Yönetimi
        JButton btnKitap = new JButton("Kitap Yönetimi");
        btnKitap.setFont(new Font("Arial", Font.PLAIN, 14));
        btnKitap.addActionListener(e -> new KitapEkrani().setVisible(true));
        pnlButonlar.add(btnKitap);

        // 3. Ödünç İşlemleri
        JButton btnOdunc = new JButton("Ödünç İşlemleri");
        btnOdunc.setFont(new Font("Arial", Font.PLAIN, 14));
        btnOdunc.addActionListener(e -> new OduncEkrani().setVisible(true));
        pnlButonlar.add(btnOdunc);

        // 4. Ceza Görüntüleme
        // (Not: Cezalar, Rapor ekranındaki 'Geciken Kitaplar' kısmında görünüyor)
        JButton btnCeza = new JButton("Ceza Görüntüleme");
        btnCeza.setFont(new Font("Arial", Font.PLAIN, 14));
        btnCeza.addActionListener(e -> {
            new CezaEkrani().setVisible(true);
        });
        pnlButonlar.add(btnCeza);

        // 5. Raporlar
        JButton btnRapor = new JButton("Raporlar");
        btnRapor.setFont(new Font("Arial", Font.PLAIN, 14));
        btnRapor.addActionListener(e -> new RaporEkrani().setVisible(true));
        pnlButonlar.add(btnRapor);

        // 6. Dinamik Sorgu Ekranı
        // (Not: Bu da Rapor Ekranının 2. sekmesinde)
        JButton btnDinamik = new JButton("Dinamik Sorgu Ekranı");
        btnDinamik.setBackground(new Color(200, 230, 255)); // Dikkat çeksin diye hafif mavi
        btnDinamik.setFont(new Font("Arial", Font.BOLD, 14));
        btnDinamik.addActionListener(e -> {
            new RaporEkrani().setVisible(true);
            JOptionPane.showMessageDialog(this, "Lütfen üstteki 'Detaylı Kitap Arama' sekmesine geçiniz.");
        });
        pnlButonlar.add(btnDinamik);

        // 7. Çıkış Butonu
        JButton btnCikis = new JButton("Çıkış Yap");
        btnCikis.setFont(new Font("Arial", Font.BOLD, 14));
        btnCikis.setForeground(Color.RED);
        btnCikis.addActionListener(e -> {
            this.dispose(); // Menüyü kapat
            new GirisEkrani().setVisible(true); // Girişe geri dön
        });
        pnlButonlar.add(btnCikis);

        // 8. Boşluk (Simetri bozulmasın diye boş bir etiket ekliyoruz)
        pnlButonlar.add(new JLabel(""));

        add(pnlButonlar, BorderLayout.CENTER);
    }
}