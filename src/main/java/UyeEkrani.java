import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.sql.*;

public class UyeEkrani extends JFrame {
    private JTable table;
    private DefaultTableModel model;

    // Veri Giriş Alanları
    private JTextField txtAd, txtSoyad, txtTelefon, txtEmail;

    public UyeEkrani() {
        setTitle("Üye Yönetimi");
        setSize(800, 500);
        setLayout(null);
        setLocationRelativeTo(null);

        // --- SOL TARAF: FORM ALANI ---
        JLabel lbl1 = new JLabel("Ad:");
        lbl1.setBounds(20, 20, 80, 25);
        add(lbl1);

        txtAd = new JTextField();
        txtAd.setBounds(80, 20, 150, 25);
        add(txtAd);

        JLabel lbl2 = new JLabel("Soyad:");
        lbl2.setBounds(20, 50, 80, 25);
        add(lbl2);

        txtSoyad = new JTextField();
        txtSoyad.setBounds(80, 50, 150, 25);
        add(txtSoyad);

        JLabel lbl3 = new JLabel("Telefon:");
        lbl3.setBounds(20, 80, 80, 25);
        add(lbl3);

        txtTelefon = new JTextField();
        txtTelefon.setBounds(80, 80, 150, 25);
        add(txtTelefon);

        JLabel lbl4 = new JLabel("E-mail:");
        lbl4.setBounds(20, 110, 80, 25);
        add(lbl4);

        txtEmail = new JTextField();
        txtEmail.setBounds(80, 110, 150, 25);
        add(txtEmail);

        // --- BUTONLAR ---
        JButton btnEkle = new JButton("Üye Ekle");
        btnEkle.setBounds(20, 160, 100, 30);
        add(btnEkle);

        JButton btnSil = new JButton("Seçileni Sil");
        btnSil.setBounds(130, 160, 120, 30);
        add(btnSil);

        // Arama Kutusu
        JLabel lblAra = new JLabel("Ara:");
        lblAra.setBounds(260, 20, 40, 25);
        add(lblAra);

        JTextField txtAra = new JTextField();
        txtAra.setBounds(300, 20, 150, 25);
        add(txtAra);

        JButton btnAra = new JButton("Bul");
        btnAra.setBounds(460, 20, 60, 25);
        add(btnAra);

        // --- SAĞ TARAF: TABLO (LİSTE) ---
        model = new DefaultTableModel();
        // Kolon Başlıkları
        model.setColumnIdentifiers(new String[]{"ID", "Ad", "Soyad", "Telefon", "Email", "Borç"});

        table = new JTable(model);
        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBounds(260, 60, 500, 380);
        add(scrollPane);

        // --- İŞLEMLER ---
        btnEkle.addActionListener(e -> uyeEkle());
        btnSil.addActionListener(e -> uyeSil());
        btnAra.addActionListener(e -> uyeListele(txtAra.getText()));

        // Açılışta listele
        uyeListele("");
    }

    // --- VERİTABANI METOTLARI ---
    private Connection baglantiAl() throws Exception {
        String url = "jdbc:mysql://localhost:3306/kütüphanedb?useUnicode=true&characterEncoding=utf8";
        return DriverManager.getConnection(url, "root", "");
    }

    private void uyeListele(String aranan) {
        try {
            model.setRowCount(0);
            Connection conn = baglantiAl();

            String sql = "SELECT * FROM UYE WHERE Ad LIKE ? OR Soyad LIKE ?";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, "%" + aranan + "%");
            ps.setString(2, "%" + aranan + "%");

            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                model.addRow(new Object[]{
                        rs.getInt("UyeID"),
                        rs.getString("Ad"),
                        rs.getString("Soyad"),
                        rs.getString("Telefon"),
                        rs.getString("Email"),
                        rs.getDouble("ToplamBorc")
                });
            }
            conn.close();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Hata: " + ex.getMessage());
        }
    }

    private void uyeEkle() {
        try {
            Connection conn = baglantiAl();
            String sql = "INSERT INTO UYE (Ad, Soyad, Telefon, Email) VALUES (?, ?, ?, ?)";
            PreparedStatement ps = conn.prepareStatement(sql);

            ps.setString(1, txtAd.getText());
            ps.setString(2, txtSoyad.getText());
            ps.setString(3, txtTelefon.getText());
            ps.setString(4, txtEmail.getText());

            ps.executeUpdate();
            conn.close();

            JOptionPane.showMessageDialog(this, "Üye Eklendi!");
            uyeListele("");

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Hata: " + ex.getMessage());
        }
    }

    private void uyeSil() {
        int seciliSatir = table.getSelectedRow();
        if (seciliSatir == -1) {
            JOptionPane.showMessageDialog(this, "Lütfen silinecek üyeyi seçin.");
            return;
        }

        int id = (int) model.getValueAt(seciliSatir, 0);

        try {
            Connection conn = baglantiAl();
            String sql = "DELETE FROM UYE WHERE UyeID = ?";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, id);

            int sonuc = ps.executeUpdate();
            conn.close();

            if (sonuc > 0) {
                JOptionPane.showMessageDialog(this, "Üye Silindi.");
                uyeListele("");
            }

        } catch (Exception ex) {
            // BURADA TETİKLEYİCİ DEVREYE GİRECEK
            // Eğer üyenin borcu varsa veritabanı hata verecek, biz de burada göstereceğiz.
            JOptionPane.showMessageDialog(this, "Silinemedi! (Borcu veya aktif ödüncü olabilir)\nDetay: " + ex.getMessage());
        }
    }
}