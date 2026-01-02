import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.*;

public class KitapEkrani extends JFrame {

    private JTextField txtAd, txtYazar, txtYayinevi, txtYil, txtAdet, txtAra;
    private JComboBox<String> cbKategori;
    private JTable table;
    private DefaultTableModel model;

    private int secilenKitapID = -1;

    public KitapEkrani() {
        setTitle("Kitap Yönetim Paneli");
        setSize(1000, 600);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));

        JPanel pnlForm = new JPanel(new GridLayout(7, 2, 5, 5));
        pnlForm.setBorder(BorderFactory.createTitledBorder("Kitap Bilgileri"));

        Dimension txtBoyut = new Dimension(150, 25);

        pnlForm.add(new JLabel("Kitap Adı:"));
        txtAd = new JTextField(); txtAd.setPreferredSize(txtBoyut);
        pnlForm.add(txtAd);

        pnlForm.add(new JLabel("Yazar:"));
        txtYazar = new JTextField(); txtYazar.setPreferredSize(txtBoyut);
        pnlForm.add(txtYazar);

        pnlForm.add(new JLabel("Kategori:"));
        // NOT: Bu isimlerin veritabanındaki KATEGORI tablosunda aynen yazılı olması gerekir!
        String[] kategoriler = {"Roman", "Bilim", "Tarih", "Yazılım", "Felsefe", "Çocuk", "Diğer"};
        cbKategori = new JComboBox<>(kategoriler);
        cbKategori.setBackground(Color.WHITE);
        cbKategori.setPreferredSize(txtBoyut);
        pnlForm.add(cbKategori);

        pnlForm.add(new JLabel("Yayınevi:"));
        txtYayinevi = new JTextField(); txtYayinevi.setPreferredSize(txtBoyut);
        pnlForm.add(txtYayinevi);

        pnlForm.add(new JLabel("Basım Yılı:"));
        txtYil = new JTextField(); txtYil.setPreferredSize(txtBoyut);
        pnlForm.add(txtYil);

        pnlForm.add(new JLabel("Toplam Adet:"));
        txtAdet = new JTextField(); txtAdet.setPreferredSize(txtBoyut);
        pnlForm.add(txtAdet);

        pnlForm.add(new JLabel("")); pnlForm.add(new JLabel(""));

        JPanel pnlButonlar = new JPanel(new GridLayout(1, 3, 5, 0));
        pnlButonlar.setPreferredSize(new Dimension(300, 35));

        JButton btnEkle = new JButton("EKLE");
        JButton btnGuncelle = new JButton("GÜNCELLE");
        JButton btnSil = new JButton("SİL");

        btnEkle.setBackground(new Color(100, 200, 100));
        btnGuncelle.setBackground(new Color(100, 150, 200));
        btnSil.setBackground(new Color(200, 100, 100));

        pnlButonlar.add(btnEkle);
        pnlButonlar.add(btnGuncelle);
        pnlButonlar.add(btnSil);

        JPanel pnlSolContainer = new JPanel(new BorderLayout());
        pnlSolContainer.setPreferredSize(new Dimension(320, 0));
        pnlSolContainer.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JPanel pnlKuzeyBirlestirici = new JPanel(new BorderLayout(0, 10));
        pnlKuzeyBirlestirici.add(pnlForm, BorderLayout.CENTER);
        pnlKuzeyBirlestirici.add(pnlButonlar, BorderLayout.SOUTH);

        pnlSolContainer.add(pnlKuzeyBirlestirici, BorderLayout.NORTH);

        add(pnlSolContainer, BorderLayout.WEST);

        JPanel pnlOrta = new JPanel(new BorderLayout(10, 10));
        pnlOrta.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 10));

        // Arama Paneli
        JPanel pnlArama = new JPanel(new FlowLayout(FlowLayout.LEFT));
        pnlArama.add(new JLabel("Kitap Ara (Ad veya Yazar):"));
        txtAra = new JTextField(20);
        JButton btnAra = new JButton("Bul");
        JButton btnYenile = new JButton("Tümünü Listele");

        pnlArama.add(txtAra);
        pnlArama.add(btnAra);
        pnlArama.add(btnYenile);

        pnlOrta.add(pnlArama, BorderLayout.NORTH);

        // Tablo
        model = new DefaultTableModel();
        table = new JTable(model);
        pnlOrta.add(new JScrollPane(table), BorderLayout.CENTER);

        add(pnlOrta, BorderLayout.CENTER);

        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int satir = table.getSelectedRow();
                if (satir > -1) {
                    secilenKitapID = (int) model.getValueAt(satir, 0);
                    txtAd.setText(model.getValueAt(satir, 1).toString());
                    txtYazar.setText(model.getValueAt(satir, 2).toString());
                    // Tablodan gelen kategori ismini combobox'ta seçili hale getir
                    cbKategori.setSelectedItem(model.getValueAt(satir, 3).toString());
                    txtYayinevi.setText(model.getValueAt(satir, 4).toString());
                    txtYil.setText(model.getValueAt(satir, 5).toString());
                    txtAdet.setText(model.getValueAt(satir, 6).toString());
                }
            }
        });

        btnEkle.addActionListener(e -> kitapEkle());
        btnGuncelle.addActionListener(e -> kitapGuncelle());
        btnSil.addActionListener(e -> kitapSil());
        btnAra.addActionListener(e -> kitaplariListele(txtAra.getText()));
        btnYenile.addActionListener(e -> {
            txtAra.setText("");
            kitaplariListele("");
            temizle();
        });

        // Başlangıçta listele
        kitaplariListele("");
    }

    private Connection baglanti() throws Exception {
        return DriverManager.getConnection("jdbc:mysql://localhost:3306/kütüphanedb?useUnicode=true&characterEncoding=utf8", "root", "");
    }

    // --- YENİ EKLENEN YARDIMCI METOT ---
    // Veritabanından isme göre ID bulur.
    private int kategoriIdBul(String kategoriAdi) {
        int id = 0;
        try (Connection conn = baglanti()) {
            String sql = "SELECT KategoriID FROM KATEGORI WHERE KategoriAdi = ?";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, kategoriAdi);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                id = rs.getInt("KategoriID");
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return id;
    }

    private void kitaplariListele(String aranan) {
        try (Connection conn = baglanti()) {

            String sql = "{ call sp_KitapAra(?, ?) }";
            CallableStatement cs = conn.prepareCall(sql);

            cs.setString(1, aranan);

            // --- DÜZELTME: Artık String 'Hepsi' değil, int 0 gönderiyoruz ---
            cs.setInt(2, 0);

            ResultSet rs = cs.executeQuery();

            model.setRowCount(0);
            model.setColumnIdentifiers(new String[]{"ID", "Kitap Adı", "Yazar", "Kategori", "Yayınevi", "Yıl", "Toplam", "Mevcut"});

            while (rs.next()) {
                model.addRow(new Object[]{
                        rs.getInt("KitapID"),
                        rs.getString("KitapAdi"),
                        rs.getString("Yazar"),
                        // --- DÜZELTME: SP'den gelen sütun adı 'KategoriAdi' oldu ---
                        rs.getString("KategoriAdi"),
                        rs.getString("Yayinevi"),
                        rs.getInt("BasimYili"),
                        rs.getInt("ToplamAdet"),
                        rs.getInt("MevcutAdet")
                });
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Arama Hatası: " + ex.getMessage());
        }
    }

    private void kitapEkle() {
        try (Connection conn = baglanti()) {
            // --- DÜZELTME: Sütun adı KategoriID oldu ---
            String sql = "INSERT INTO KITAP (KitapAdi, Yazar, KategoriID, Yayinevi, BasimYili, ToplamAdet, MevcutAdet) VALUES (?,?,?,?,?,?,?)";
            PreparedStatement ps = conn.prepareStatement(sql);

            ps.setString(1, txtAd.getText());
            ps.setString(2, txtYazar.getText());

            // --- DÜZELTME: İsmi ID'ye çevirip kaydediyoruz ---
            int kID = kategoriIdBul(cbKategori.getSelectedItem().toString());
            if (kID == 0) {
                JOptionPane.showMessageDialog(this, "Hata: Seçilen kategori veritabanında bulunamadı!");
                return;
            }
            ps.setInt(3, kID);

            ps.setString(4, txtYayinevi.getText());
            ps.setInt(5, Integer.parseInt(txtYil.getText()));
            int adet = Integer.parseInt(txtAdet.getText());
            ps.setInt(6, adet);
            ps.setInt(7, adet); // Başlangıçta Mevcut = Toplam

            ps.executeUpdate();
            JOptionPane.showMessageDialog(this, "Kitap Başarıyla Eklendi!");
            kitaplariListele("");
            temizle();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Ekleme Hatası! Alanları kontrol ediniz.\n" + ex.getMessage());
        }
    }

    private void kitapGuncelle() {
        if (secilenKitapID == -1) {
            JOptionPane.showMessageDialog(this, "Lütfen tablodan bir kitap seçin.");
            return;
        }
        try (Connection conn = baglanti()) {
            // --- DÜZELTME: Sütun adı KategoriID oldu ---
            String sql = "UPDATE KITAP SET KitapAdi=?, Yazar=?, KategoriID=?, Yayinevi=?, BasimYili=?, ToplamAdet=? WHERE KitapID=?";
            PreparedStatement ps = conn.prepareStatement(sql);

            ps.setString(1, txtAd.getText());
            ps.setString(2, txtYazar.getText());

            // --- DÜZELTME: İsmi ID'ye çevirip güncelliyoruz ---
            int kID = kategoriIdBul(cbKategori.getSelectedItem().toString());
            if (kID == 0) {
                JOptionPane.showMessageDialog(this, "Hata: Seçilen kategori veritabanında bulunamadı!");
                return;
            }
            ps.setInt(3, kID);

            ps.setString(4, txtYayinevi.getText());
            ps.setInt(5, Integer.parseInt(txtYil.getText()));
            ps.setInt(6, Integer.parseInt(txtAdet.getText()));
            ps.setInt(7, secilenKitapID);

            ps.executeUpdate();
            JOptionPane.showMessageDialog(this, "Kitap Güncellendi!");
            kitaplariListele("");
            temizle();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Güncelleme Hatası: " + ex.getMessage());
        }
    }

    private void kitapSil() {
        if (secilenKitapID == -1) {
            JOptionPane.showMessageDialog(this, "Lütfen silinecek kitabı seçin.");
            return;
        }

        int onay = JOptionPane.showConfirmDialog(this, "Kitabı silmek istediğinize emin misiniz?", "Onay", JOptionPane.YES_NO_OPTION);
        if (onay != JOptionPane.YES_OPTION) return;

        try (Connection conn = baglanti()) {
            String sql = "DELETE FROM KITAP WHERE KitapID=?";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, secilenKitapID);
            ps.executeUpdate();

            JOptionPane.showMessageDialog(this, "Kitap Silindi!");
            kitaplariListele("");
            temizle();
        } catch (SQLException ex) {
            if (ex.getErrorCode() == 1451) {
                JOptionPane.showMessageDialog(this, "UYARI: Bu kitap şu an ödünçte veya işlem görmüş.\nVeri bütünlüğü için silemezsiniz.");
            } else {
                JOptionPane.showMessageDialog(this, "Hata: " + ex.getMessage());
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Hata: " + ex.getMessage());
        }
    }

    private void temizle() {
        txtAd.setText("");
        txtYazar.setText("");
        txtYayinevi.setText("");
        txtYil.setText("");
        txtAdet.setText("");
        cbKategori.setSelectedIndex(0);
        secilenKitapID = -1;
    }
}