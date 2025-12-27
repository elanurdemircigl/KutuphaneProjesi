import javax.swing.*;
import java.awt.*;

public class AnaMenu extends JFrame {

    public AnaMenu(String kullaniciRolu) {
        // --- PENCERE AYARLARI ---
        setTitle("Kütüphane Yönetim Sistemi - Ana Menü");
        setSize(800, 500);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));

        // --- 1. ÜST KISIM: HOŞGELDİNİZ METNİ ---
        JLabel lblBilgi = new JLabel("Hoş geldiniz, Yetki: " + kullaniciRolu, SwingConstants.CENTER);
        lblBilgi.setFont(new Font("Arial", Font.BOLD, 22));
        lblBilgi.setBorder(BorderFactory.createEmptyBorder(20, 10, 10, 10));
        add(lblBilgi, BorderLayout.NORTH);

        // --- 2. ORTA KISIM: BUTONLAR (4 Satır x 2 Sütun) ---
        JPanel pnlButonlar = new JPanel(new GridLayout(4, 2, 20, 20));
        pnlButonlar.setBorder(BorderFactory.createEmptyBorder(10, 50, 40, 50));

        Font btnFont = new Font("Arial", Font.BOLD, 14);

        // 1. Üye Yönetimi
        JButton btnUye = new JButton("Üye Yönetimi");
        btnUye.setFont(btnFont);
        btnUye.addActionListener(e -> new UyeEkrani().setVisible(true));
        pnlButonlar.add(btnUye);

        // 2. Kitap Yönetimi
        JButton btnKitap = new JButton("Kitap Yönetimi");
        btnKitap.setFont(btnFont);
        btnKitap.addActionListener(e -> new KitapEkrani().setVisible(true));
        pnlButonlar.add(btnKitap);

        // 3. Ödünç Verme Ekranı (DÜZELTİLDİ: Senin dosya adına göre)
        JButton btnOduncVer = new JButton("Ödünç Verme Ekranı");
        btnOduncVer.setFont(btnFont);
        // Listende 'OduncVermeEkrani' yok, 'OduncEkrani' var. O yüzden bunu çağırıyoruz.
        btnOduncVer.addActionListener(e -> new OduncEkrani().setVisible(true));
        pnlButonlar.add(btnOduncVer);

        // 4. Kitap Teslim Alma
        JButton btnTeslimAl = new JButton("Kitap Teslim Alma");
        btnTeslimAl.setFont(btnFont);
        btnTeslimAl.addActionListener(e -> new TeslimAlmaEkrani().setVisible(true));
        pnlButonlar.add(btnTeslimAl);

        // 5. Ceza Görüntüleme
        JButton btnCeza = new JButton("Ceza Görüntüleme");
        btnCeza.setFont(btnFont);
        btnCeza.addActionListener(e -> {
            RaporEkrani ekran = new RaporEkrani();
            ekran.setVisible(true);
            ekran.sekmeSec(0);
            JOptionPane.showMessageDialog(this, "Cezaları 'Hazır İstatistikler' sekmesinden görebilirsiniz.");
        });
        pnlButonlar.add(btnCeza);

        // 6. Raporlar
        JButton btnRapor = new JButton("Raporlar");
        btnRapor.setFont(btnFont);
        btnRapor.addActionListener(e -> {
            RaporEkrani ekran = new RaporEkrani();
            ekran.setVisible(true);
            ekran.sekmeSec(0);
        });
        pnlButonlar.add(btnRapor);

        // 7. Dinamik Sorgu
        JButton btnDinamik = new JButton("Dinamik Sorgu Ekranı");
        btnDinamik.setBackground(new Color(200, 230, 255));
        btnDinamik.setFont(btnFont);
        btnDinamik.addActionListener(e -> {
            RaporEkrani ekran = new RaporEkrani();
            ekran.setVisible(true);
            ekran.sekmeSec(1);
        });
        pnlButonlar.add(btnDinamik);

        // 8. Çıkış
        JButton btnCikis = new JButton("Çıkış Yap");
        btnCikis.setFont(btnFont);
        btnCikis.setForeground(Color.RED);
        btnCikis.addActionListener(e -> {
            this.dispose();
            new GirisEkrani().setVisible(true);
        });
        pnlButonlar.add(btnCikis);

        add(pnlButonlar, BorderLayout.CENTER);
    }
}