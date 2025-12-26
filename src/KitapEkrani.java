import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.sql.*;

public class KitapEkrani extends JFrame {
    private JTable table;
    private DefaultTableModel model;

    // Veri Giriş Alanları
    private JTextField txtAd, txtYazar, txtKategori, txtYayinevi, txtYil, txtAdet;

    public KitapEkrani() {
        setTitle("Kitap Yönetimi");
        setSize(800, 600);
        setLayout(null);
        setLocationRelativeTo(null);

        // --- SOL TARAF: FORM ALANI ---
        JLabel lbl1 = new JLabel("Kitap Adı:");
        lbl1.setBounds(20, 20, 80, 25);
        add(lbl1);

        txtAd = new JTextField();
        txtAd.setBounds(100, 20, 150, 25);
        add(txtAd);

        JLabel lbl2 = new JLabel("Yazar:");
        lbl2.setBounds(20, 50, 80, 25);
        add(lbl2);

        txtYazar = new JTextField();
        txtYazar.setBounds(100, 50, 150, 25);
        add(txtYazar);

        JLabel lbl3 = new JLabel("Kategori:");
        lbl3.setBounds(20, 80, 80, 25);
        add(lbl3);

        txtKategori = new JTextField();
        txtKategori.setBounds(100, 80, 150, 25);
        add(txtKategori);

        JLabel lbl4 = new JLabel("Yayınevi:");
        lbl4.setBounds(20, 110, 80, 25);
        add(lbl4);

        txtYayinevi = new JTextField();
        txtYayinevi.setBounds(100, 110, 150, 25);
        add(txtYayinevi);

        JLabel lbl5 = new JLabel("Basım Yılı:");
        lbl5.setBounds(20, 140, 80, 25);
        add(lbl5);

        txtYil = new JTextField();
        txtYil.setBounds(100, 140, 150, 25);
        add(txtYil);

        JLabel lbl6 = new JLabel("Adet:");
        lbl6.setBounds(20, 170, 80, 25);
        add(lbl6);

        txtAdet = new JTextField();
        txtAdet.setBounds(100, 170, 150, 25);
        add(txtAdet);

        // --- BUTONLAR ---
        JButton btnEkle = new JButton("Kitap Ekle");
        btnEkle.setBounds(20, 210, 100, 30);
        add(btnEkle);

        JButton btnSil = new JButton("Seçileni Sil");
        btnSil.setBounds(130, 210, 120, 30);
        add(btnSil);

        // Arama Kutusu
        JLabel lblAra = new JLabel("Ara:");
        lblAra.setBounds(280, 20, 40, 25);
        add(lblAra);

        JTextField txtAra = new JTextField();
        txtAra.setBounds(320, 20, 150, 25);
        add(txtAra);

        JButton btnAra = new JButton("Bul");
        btnAra.setBounds(480, 20, 60, 25);
        add(btnAra);

        // --- SAĞ TARAF: TABLO (LİSTE) ---
        model = new DefaultTableModel();
        // Kolon Başlıkları
        model.setColumnIdentifiers(new String[]{"ID", "Kitap Adı", "Yazar", "Kategori", "Mevcut", "Toplam"});

        table = new JTable(model);
        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBounds(280, 60, 480, 450);
        add(scrollPane);

        // --- İŞLEMLER ---

        // 1. Ekle Butonu Tıklanınca
        btnEkle.addActionListener(e -> kitapEkle());

        // 2. Sil Butonu Tıklanınca
        btnSil.addActionListener(e -> kitapSil());

        // 3. Ara Butonu Tıklanınca
        btnAra.addActionListener(e -> kitapListele(txtAra.getText()));

        // Ekran açılınca listeyi doldur
        kitapListele("");
    }

    // --- VERİTABANI METOTLARI ---

    private Connection baglantiAl() throws Exception {
        String url = "jdbc:mysql://localhost:3306/kütüphanedb?useUnicode=true&characterEncoding=utf8";
        // Arkadaşınla şifre farkı varsa burayı kontrol et:
        return DriverManager.getConnection(url, "root", "");
    }

    private void kitapListele(String aranan) {
        try {
            model.setRowCount(0); // Tabloyu temizle
            Connection conn = baglantiAl();

            String sql = "SELECT * FROM KITAP WHERE KitapAdi LIKE ? OR Yazar LIKE ?";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, "%" + aranan + "%");
            ps.setString(2, "%" + aranan + "%");

            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                model.addRow(new Object[]{
                        rs.getInt("KitapID"),
                        rs.getString("KitapAdi"),
                        rs.getString("Yazar"),
                        rs.getString("Kategori"),
                        rs.getInt("MevcutAdet"),
                        rs.getInt("ToplamAdet")
                });
            }
            conn.close();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Hata: " + ex.getMessage());
        }
    }

    private void kitapEkle() {
        try {
            Connection conn = baglantiAl();
            String sql = "INSERT INTO KITAP (KitapAdi, Yazar, Kategori, Yayinevi, BasimYili, ToplamAdet, MevcutAdet) VALUES (?, ?, ?, ?, ?, ?, ?)";
            PreparedStatement ps = conn.prepareStatement(sql);

            ps.setString(1, txtAd.getText());
            ps.setString(2, txtYazar.getText());
            ps.setString(3, txtKategori.getText());
            ps.setString(4, txtYayinevi.getText());
            ps.setInt(5, Integer.parseInt(txtYil.getText()));
            int adet = Integer.parseInt(txtAdet.getText());
            ps.setInt(6, adet);
            ps.setInt(7, adet); // Başlangıçta mevcut = toplam

            ps.executeUpdate();
            conn.close();

            JOptionPane.showMessageDialog(this, "Kitap Eklendi!");
            kitapListele(""); // Listeyi yenile

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Hata: " + ex.getMessage());
        }
    }

    private void kitapSil() {
        int seciliSatir = table.getSelectedRow();
        if (seciliSatir == -1) {
            JOptionPane.showMessageDialog(this, "Lütfen silinecek kitabı seçin.");
            return;
        }

        // ID'yi tablodan al (0. kolon ID)
        int id = (int) model.getValueAt(seciliSatir, 0);

        try {
            Connection conn = baglantiAl();
            // Önce kontrol: Bu kitap ödünçte mi? (Trigger da engelleyebilir ama burada da bakalım)
            // Basit silme komutu:
            String sql = "DELETE FROM KITAP WHERE KitapID = ?";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, id);

            int sonuc = ps.executeUpdate();
            conn.close();

            if (sonuc > 0) {
                JOptionPane.showMessageDialog(this, "Kitap Silindi.");
                kitapListele("");
            }

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Silinemedi! (Ödünç verilmiş olabilir)\nHata: " + ex.getMessage());
        }
    }
}