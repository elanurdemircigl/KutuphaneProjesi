import javax.swing.*;
import java.awt.*;

public class AnaMenu extends JFrame {

    public AnaMenu(String kullaniciRolu) {
        //Pencere ayarları
        setTitle("Kütüphane Yönetim Sistemi - Ana Menü");
        setSize(800, 500);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));

        JLabel lblBilgi = new JLabel("Hoş geldiniz, Yetki: " + kullaniciRolu, SwingConstants.CENTER);
        lblBilgi.setFont(new Font("Arial", Font.BOLD, 22));
        lblBilgi.setBorder(BorderFactory.createEmptyBorder(20, 10, 10, 10));
        add(lblBilgi, BorderLayout.NORTH);

        JPanel pnlButonlar = new JPanel(new GridLayout(4, 2, 20, 20));
        pnlButonlar.setBorder(BorderFactory.createEmptyBorder(10, 50, 40, 50));

        Font btnFont = new Font("Arial", Font.BOLD, 14);

        JButton btnUye = new JButton("Üye Yönetimi");
        btnUye.setFont(btnFont);
        btnUye.addActionListener(e -> new UyeEkrani().setVisible(true));
        pnlButonlar.add(btnUye);

        JButton btnKitap = new JButton("Kitap Yönetimi");
        btnKitap.setFont(btnFont);
        btnKitap.addActionListener(e -> new KitapEkrani().setVisible(true));
        pnlButonlar.add(btnKitap);

        JButton btnOduncVer = new JButton("Ödünç Verme Ekranı");
        btnOduncVer.setFont(btnFont);
        btnOduncVer.addActionListener(e -> new OduncEkrani().setVisible(true));
        pnlButonlar.add(btnOduncVer);

        JButton btnTeslimAl = new JButton("Kitap Teslim Alma");
        btnTeslimAl.setFont(btnFont);
        btnTeslimAl.addActionListener(e -> new TeslimAlmaEkrani().setVisible(true));
        pnlButonlar.add(btnTeslimAl);

        JButton btnCeza = new JButton("Ceza Görüntüleme");
        btnCeza.setFont(btnFont);
        btnCeza.addActionListener(e -> {
            RaporEkrani ekran = new RaporEkrani();
            ekran.setVisible(true);
            ekran.sekmeSec(0);
            JOptionPane.showMessageDialog(this, "Cezaları 'Hazır İstatistikler' sekmesinden görebilirsiniz.");
        });
        pnlButonlar.add(btnCeza);

        JButton btnRapor = new JButton("Raporlar");
        btnRapor.setFont(btnFont);
        btnRapor.addActionListener(e -> {
            RaporEkrani ekran = new RaporEkrani();
            ekran.setVisible(true);
            ekran.sekmeSec(0);
        });
        pnlButonlar.add(btnRapor);

        JButton btnDinamik = new JButton("Dinamik Sorgu Ekranı");
        btnDinamik.setBackground(new Color(200, 230, 255));
        btnDinamik.setFont(btnFont);
        btnDinamik.addActionListener(e -> {
            RaporEkrani ekran = new RaporEkrani();
            ekran.setVisible(true);
            ekran.sekmeSec(1);
        });
        pnlButonlar.add(btnDinamik);

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