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

    // Seçilen kitabın ID'sini tutmak için
    private int secilenKitapID = -1;

    public KitapEkrani() {
        setTitle("Kitap Yönetim Paneli");
        setSize(1000, 600);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));

        // ===========================================================================
        // SOL PANEL (FORM ALANI) - İnceltilmiş ve Yukarı Sabitlenmiş Tasarım
        // ===========================================================================

        // 1. Form Bileşenleri Paneli
        JPanel pnlForm = new JPanel(new GridLayout(7, 2, 5, 5)); // 7 Satır, 5px boşluk
        pnlForm.setBorder(BorderFactory.createTitledBorder("Kitap Bilgileri"));

        // Standart boyut (Kutuların yüksekliğini 25px ile sınırla)
        Dimension txtBoyut = new Dimension(150, 25);

        // Bileşenleri Ekle
        pnlForm.add(new JLabel("Kitap Adı:"));
        txtAd = new JTextField(); txtAd.setPreferredSize(txtBoyut);
        pnlForm.add(txtAd);

        pnlForm.add(new JLabel("Yazar:"));
        txtYazar = new JTextField(); txtYazar.setPreferredSize(txtBoyut);
        pnlForm.add(txtYazar);

        pnlForm.add(new JLabel("Kategori:"));
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

        // Boşluk (Düzen düzgün dursun diye)
        pnlForm.add(new JLabel("")); pnlForm.add(new JLabel(""));

        // 2. Butonlar Paneli
        JPanel pnlButonlar = new JPanel(new GridLayout(1, 3, 5, 0));
        pnlButonlar.setPreferredSize(new Dimension(300, 35)); // Buton yüksekliği

        JButton btnEkle = new JButton("EKLE");
        JButton btnGuncelle = new JButton("GÜNCELLE");
        JButton btnSil = new JButton("SİL");

        btnEkle.setBackground(new Color(100, 200, 100)); // Yeşil
        btnGuncelle.setBackground(new Color(100, 150, 200)); // Mavi
        btnSil.setBackground(new Color(200, 100, 100)); // Kırmızı

        pnlButonlar.add(btnEkle);
        pnlButonlar.add(btnGuncelle);
        pnlButonlar.add(btnSil);

        // 3. SOL KONTEYNER (Formu yukarı sabitlemek için Wrapper)
        JPanel pnlSolContainer = new JPanel(new BorderLayout());
        pnlSolContainer.setPreferredSize(new Dimension(320, 0)); // Sol panel genişliği
        pnlSolContainer.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Form ve Butonları birleştirip Kuzeye (NORTH) koyuyoruz ki aşağı uzamasın
        JPanel pnlKuzeyBirlestirici = new JPanel(new BorderLayout(0, 10));
        pnlKuzeyBirlestirici.add(pnlForm, BorderLayout.CENTER);
        pnlKuzeyBirlestirici.add(pnlButonlar, BorderLayout.SOUTH);

        pnlSolContainer.add(pnlKuzeyBirlestirici, BorderLayout.NORTH);

        add(pnlSolContainer, BorderLayout.WEST);

        // ===========================================================================
        // ORTA PANEL (TABLO VE ARAMA)
        // ===========================================================================
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

        // ===========================================================================
        // OLAYLAR (ACTIONS)
        // ===========================================================================

        // Tabloya Tıklama
        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int satir = table.getSelectedRow();
                if (satir > -1) {
                    secilenKitapID = (int) model.getValueAt(satir, 0);
                    txtAd.setText(model.getValueAt(satir, 1).toString());
                    txtYazar.setText(model.getValueAt(satir, 2).toString());
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

    // ===========================================================================
    // VERİTABANI METOTLARI
    // ===========================================================================

    private Connection baglantiAl() throws Exception {
        return DriverManager.getConnection("jdbc:mysql://localhost:3306/kütüphanedb?useUnicode=true&characterEncoding=utf8", "root", "");
    }

    // Mevcut 'kitaplariListele' metodunu bununla değiştir:
    private void kitaplariListele(String aranan) {
        try (Connection conn = baglantiAl()) {

            // Eski SQL yerine artık Prosedür çağırıyoruz
            String sql = "{ call sp_KitapAra(?, ?) }";
            CallableStatement cs = conn.prepareCall(sql);

            // 1. Parametre: Arama Metni (Metin kutusundan gelen)
            cs.setString(1, aranan);

            // 2. Parametre: Kategori (Şimdilik 'Hepsi' gönderiyoruz, ileride filtre ekleyebilirsin)
            // Eğer formdaki kategoriyi kullanmak istersen: cbKategori.getSelectedItem().toString() yazabilirsin.
            cs.setString(2, "Hepsi");

            ResultSet rs = cs.executeQuery();

            // Tabloyu Temizle ve Doldur
            model.setRowCount(0);
            model.setColumnIdentifiers(new String[]{"ID", "Kitap Adı", "Yazar", "Kategori", "Yayınevi", "Yıl", "Toplam", "Mevcut"});

            while (rs.next()) {
                model.addRow(new Object[]{
                        rs.getInt("KitapID"),
                        rs.getString("KitapAdi"),
                        rs.getString("Yazar"),
                        rs.getString("Kategori"),
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
        try (Connection conn = baglantiAl()) {
            String sql = "INSERT INTO KITAP (KitapAdi, Yazar, Kategori, Yayinevi, BasimYili, ToplamAdet, MevcutAdet) VALUES (?,?,?,?,?,?,?)";
            PreparedStatement ps = conn.prepareStatement(sql);

            ps.setString(1, txtAd.getText());
            ps.setString(2, txtYazar.getText());
            ps.setString(3, cbKategori.getSelectedItem().toString());
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
        try (Connection conn = baglantiAl()) {
            String sql = "UPDATE KITAP SET KitapAdi=?, Yazar=?, Kategori=?, Yayinevi=?, BasimYili=?, ToplamAdet=? WHERE KitapID=?";
            PreparedStatement ps = conn.prepareStatement(sql);

            ps.setString(1, txtAd.getText());
            ps.setString(2, txtYazar.getText());
            ps.setString(3, cbKategori.getSelectedItem().toString());
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

        try (Connection conn = baglantiAl()) {
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