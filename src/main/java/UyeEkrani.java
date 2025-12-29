import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.*;

public class UyeEkrani extends JFrame {
    private JTable table;
    private DefaultTableModel model;
    private JTextField txtAd, txtSoyad, txtTelefon, txtEmail;

    public UyeEkrani() {
        setTitle("Üye Yönetimi");
        setSize(800, 500);
        setLayout(null);
        setLocationRelativeTo(null);

        // --- Giriş Alanları ---
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

        // --- Butonlar ---
        JButton btnEkle = new JButton("Üye Ekle");
        btnEkle.setBounds(20, 160, 100, 30);
        add(btnEkle);

        JButton btnSil = new JButton("Seçileni Sil");
        btnSil.setBounds(130, 160, 120, 30);
        add(btnSil);

        JButton btnGuncelle = new JButton("Güncelle");
        btnGuncelle.setBounds(20, 200, 100, 30);
        add(btnGuncelle);

        // --- Arama Bölümü ---
        JLabel lblAra = new JLabel("Ara:");
        lblAra.setBounds(260, 20, 40, 25);
        add(lblAra);

        JTextField txtAra = new JTextField();
        txtAra.setBounds(300, 20, 150, 25);
        add(txtAra);

        JButton btnAra = new JButton("Bul");
        btnAra.setBounds(460, 20, 60, 25);
        add(btnAra);

        // --- Tablo Yapısı ---
        model = new DefaultTableModel();
        model.setColumnIdentifiers(new String[]{"ID", "Ad", "Soyad", "Telefon", "Email", "Borç"});

        table = new JTable(model);
        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBounds(260, 60, 500, 380);
        add(scrollPane);

        // --- Olay Dinleyiciler ---
        btnEkle.addActionListener(e -> uyeEkle());
        btnSil.addActionListener(e -> uyeSil());
        btnGuncelle.addActionListener(e -> uyeGuncelle());
        btnAra.addActionListener(e -> uyeListele(txtAra.getText()));

        // Tabloya tıklayınca verileri kutulara doldurma özelliği
        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int seciliSatir = table.getSelectedRow();
                if (seciliSatir != -1) {
                    txtAd.setText(model.getValueAt(seciliSatir, 1).toString());
                    txtSoyad.setText(model.getValueAt(seciliSatir, 2).toString());
                    txtTelefon.setText(model.getValueAt(seciliSatir, 3).toString());
                    txtEmail.setText(model.getValueAt(seciliSatir, 4).toString());
                }
            }
        });

        uyeListele(""); // Başlangıçta tüm üyeleri getir
    }

    private Connection baglantiAl() throws Exception {
        // 'kutuphanedb' olan hata 'kütüphanedb' olarak düzeltildi
        String url = "jdbc:mysql://localhost:3306/kütüphanedb?useUnicode=true&characterEncoding=utf8";
        return DriverManager.getConnection(url, "root", "");
    }

    private void uyeListele(String aranan) {
        try {
            model.setRowCount(0);
            Connection conn = baglantiAl();
            // Arama sorgusu Ad, Soyad ve Email kolonlarını kapsayacak şekilde güncellendi
            String sql = "SELECT * FROM uye WHERE Ad LIKE ? OR Soyad LIKE ? OR Email LIKE ?";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, "%" + aranan + "%");
            ps.setString(2, "%" + aranan + "%");
            ps.setString(3, "%" + aranan + "%");
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
            JOptionPane.showMessageDialog(this, "Listeleme Hatası: " + ex.getMessage());
        }
    }

    private void uyeEkle() {
        try {
            Connection conn = baglantiAl();
            String sql = "INSERT INTO uye (Ad, Soyad, Telefon, Email) VALUES (?, ?, ?, ?)";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, txtAd.getText());
            ps.setString(2, txtSoyad.getText());
            ps.setString(3, txtTelefon.getText());
            ps.setString(4, txtEmail.getText());

            ps.executeUpdate();
            conn.close();
            JOptionPane.showMessageDialog(this, "Üye Başarıyla Eklendi!");
            uyeListele("");
            formuTemizle();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Ekleme Hatası: " + ex.getMessage());
        }
    }

    private void uyeGuncelle() {
        int seciliSatir = table.getSelectedRow();
        if (seciliSatir == -1) {
            JOptionPane.showMessageDialog(this, "Lütfen güncellemek için tablodan bir üye seçin.");
            return;
        }

        int id = (int) model.getValueAt(seciliSatir, 0);

        try {
            Connection conn = baglantiAl();
            String sql = "UPDATE uye SET Ad=?, Soyad=?, Telefon=?, Email=? WHERE UyeID=?";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, txtAd.getText());
            ps.setString(2, txtSoyad.getText());
            ps.setString(3, txtTelefon.getText());
            ps.setString(4, txtEmail.getText());
            ps.setInt(5, id);

            ps.executeUpdate();
            conn.close();
            JOptionPane.showMessageDialog(this, "Üye Bilgileri Güncellendi!");
            uyeListele("");
            formuTemizle();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Güncelleme Hatası: " + ex.getMessage());
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
            String sql = "DELETE FROM uye WHERE UyeID = ?";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, id);

            ps.executeUpdate();
            conn.close();
            JOptionPane.showMessageDialog(this, "Üye Silindi.");
            uyeListele("");
            formuTemizle();
        } catch (Exception ex) {
            // Veritabanındaki TR_UYE_DELETE_BLOCK tetikleyicisi burada devreye girer
            JOptionPane.showMessageDialog(this, "Silinemedi! Borcu veya aktif kaydı olabilir.\nDetay: " + ex.getMessage());
        }
    }

    private void formuTemizle() {
        txtAd.setText("");
        txtSoyad.setText("");
        txtTelefon.setText("");
        txtEmail.setText("");
        table.clearSelection();
    }
}