import javax.swing.*;
import java.awt.*;

public class AnaMenu extends JFrame {

    public AnaMenu(String kullaniciRolu) {
        // --- PENCERE AYARLARI ---
        setTitle("Kütüphane Yönetim Sistemi - Ana Menü");
        setSize(700, 500);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));

        // --- 1. ÜST KISIM: HOŞGELDİNİZ METNİ ---
        // Ödev Gereksinimi 4.2: Hoş geldiniz, [KullanıcıAdı] metni [cite: 73]
        JLabel lblBilgi = new JLabel("Hoş geldiniz, Yetki: " + kullaniciRolu, SwingConstants.CENTER);
        lblBilgi.setFont(new Font("Arial", Font.BOLD, 18));
        lblBilgi.setBorder(BorderFactory.createEmptyBorder(20, 10, 10, 10));
        add(lblBilgi, BorderLayout.NORTH);

        // --- 2. ORTA KISIM: BUTONLAR ---
        JPanel pnlButonlar = new JPanel(new GridLayout(4, 2, 15, 15));
        pnlButonlar.setBorder(BorderFactory.createEmptyBorder(10, 40, 40, 40));

        // 1. Üye Yönetimi (Madde 4.3) [cite: 75, 83]
        JButton btnUye = new JButton("Üye Yönetimi");
        btnUye.setFont(new Font("Arial", Font.PLAIN, 14));
        btnUye.addActionListener(e -> new UyeEkrani().setVisible(true));
        pnlButonlar.add(btnUye);

        // 2. Kitap Yönetimi (Madde 4.4) [cite: 76, 92]
        JButton btnKitap = new JButton("Kitap Yönetimi");
        btnKitap.setFont(new Font("Arial", Font.PLAIN, 14));
        btnKitap.addActionListener(e -> new KitapEkrani().setVisible(true));
        pnlButonlar.add(btnKitap);

        // 3. Ödünç İşlemleri (Madde 4.5 & 4.6) [cite: 77, 104, 118]
        JButton btnOdunc = new JButton("Ödünç İşlemleri");
        btnOdunc.setFont(new Font("Arial", Font.PLAIN, 14));
        btnOdunc.addActionListener(e -> new OduncEkrani().setVisible(true));
        pnlButonlar.add(btnOdunc);

        // 4. Ceza Görüntüleme (Madde 4.7) [cite: 78, 133]
        JButton btnCeza = new JButton("Ceza Görüntüleme");
        btnCeza.setFont(new Font("Arial", Font.PLAIN, 14));
        btnCeza.addActionListener(e -> new CezaEkrani().setVisible(true));
        pnlButonlar.add(btnCeza);

        // 5. Raporlar (Madde 5.1) [cite: 80, 142]
        JButton btnRapor = new JButton("Raporlar");
        btnRapor.setFont(new Font("Arial", Font.PLAIN, 14));
        btnRapor.addActionListener(e -> new RaporEkrani().setVisible(true));
        pnlButonlar.add(btnRapor);

        // 6. DİNAMİK SORGU EKRANI (DÜZELTİLDİ - Madde 5.2) [cite: 81, 155]
        JButton btnDinamik = new JButton("Dinamik Sorgu Ekranı");
        btnDinamik.setBackground(new Color(200, 230, 255));
        btnDinamik.setFont(new Font("Arial", Font.BOLD, 14));
        btnDinamik.addActionListener(e -> {
            // Mesaj kutusu kaldırıldı, doğrudan ilgili sekmeyi açıyoruz
            RaporEkrani raporEkrani = new RaporEkrani();
            raporEkrani.setTab(1); // 1. indeks 'Dinamik Sorgu' sekmesidir
            raporEkrani.setVisible(true);
        });
        pnlButonlar.add(btnDinamik);

        // 7. Çıkış Butonu [cite: 82]
        JButton btnCikis = new JButton("Çıkış Yap");
        btnCikis.setFont(new Font("Arial", Font.BOLD, 14));
        btnCikis.setForeground(Color.RED);
        btnCikis.addActionListener(e -> {
            this.dispose();
            new GirisEkrani().setVisible(true);
        });
        pnlButonlar.add(btnCikis);

        // 8. Boşluk (Simetri için)
        pnlButonlar.add(new JLabel(""));

        add(pnlButonlar, BorderLayout.CENTER);
    }
}