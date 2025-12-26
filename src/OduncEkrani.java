import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;

public class OduncEkrani extends JFrame {

    // --- BİLEŞENLER ---
    private JTabbedPane tabbedPane;

    // TAB 1: Ödünç Verme
    private JTextField txtUyeID, txtKitapID, txtPersonelID;

    // TAB 2: Teslim Alma
    private JTable tableTeslim;
    private DefaultTableModel modelTeslim;

    public OduncEkrani() {
        setTitle("Ödünç ve İade İşlemleri");
        setSize(800, 600);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        tabbedPane = new JTabbedPane();

        // 1. Sekme: Ödünç Verme Paneli
        JPanel panelOdunc = new JPanel(null);
        panelOduncHazirla(panelOdunc);
        tabbedPane.addTab("Ödünç Ver", panelOdunc);

        // 2. Sekme: Teslim Alma Paneli
        JPanel panelTeslim = new JPanel(null);
        panelTeslimHazirla(panelTeslim);
        tabbedPane.addTab("Teslim Al (İade)", panelTeslim);

        add(tabbedPane, BorderLayout.CENTER);
    }

    // --- TAB 1: ÖDÜNÇ VERME TASARIMI ---
    private void panelOduncHazirla(JPanel p) {
        JLabel lblUye = new JLabel("Üye ID:");
        lblUye.setBounds(50, 50, 100, 25);
        p.add(lblUye);

        txtUyeID = new JTextField();
        txtUyeID.setBounds(150, 50, 150, 25);
        p.add(txtUyeID);

        // İpucu butonu (Üye ID bilmiyorsa listeden bakması için)
        JButton btnUyeBul = new JButton("?");
        btnUyeBul.setBounds(310, 50, 45, 25);
        btnUyeBul.addActionListener(e -> JOptionPane.showMessageDialog(this, "Üye ID'yi 'Üye Yönetimi' ekranından öğrenebilirsiniz."));
        p.add(btnUyeBul);

        JLabel lblKitap = new JLabel("Kitap ID:");
        lblKitap.setBounds(50, 90, 100, 25);
        p.add(lblKitap);

        txtKitapID = new JTextField();
        txtKitapID.setBounds(150, 90, 150, 25);
        p.add(txtKitapID);

        JLabel lblPer = new JLabel("Personel ID:");
        lblPer.setBounds(50, 130, 100, 25);
        p.add(lblPer);

        txtPersonelID = new JTextField("1"); // Varsayılan 1 (Admin)
        txtPersonelID.setBounds(150, 130, 150, 25);
        txtPersonelID.setEditable(false); // Genelde giriş yapan kişi olur
        p.add(txtPersonelID);

        JButton btnVer = new JButton("Ödünç Ver (Prosedürü Çalıştır)");
        btnVer.setBounds(150, 180, 250, 40);
        btnVer.setBackground(new Color(100, 200, 100)); // Yeşilimsi
        p.add(btnVer);

        // İŞLEM: Prosedürü Çağır
        btnVer.addActionListener(e -> islemOduncVer());
    }

    // --- TAB 2: TESLİM ALMA TASARIMI ---
    private void panelTeslimHazirla(JPanel p) {
        // Tablo
        modelTeslim = new DefaultTableModel();
        modelTeslim.setColumnIdentifiers(new String[]{"Ödünç ID", "Üye", "Kitap", "Veriliş Tarihi", "Son Teslim"});

        tableTeslim = new JTable(modelTeslim);
        JScrollPane scroll = new JScrollPane(tableTeslim);
        scroll.setBounds(20, 20, 740, 400);
        p.add(scroll);

        JButton btnTeslimAl = new JButton("Seçili Kitabı Teslim Al");
        btnTeslimAl.setBounds(20, 440, 200, 40);
        btnTeslimAl.setBackground(new Color(200, 100, 100)); // Kırmızımsı
        p.add(btnTeslimAl);

        JButton btnYenile = new JButton("Listeyi Yenile");
        btnYenile.setBounds(240, 440, 150, 40);
        p.add(btnYenile);

        // İşlemler
        btnTeslimAl.addActionListener(e -> islemTeslimAl());
        btnYenile.addActionListener(e -> aktifOduncleriListele());

        // İlk açılışta listele
        aktifOduncleriListele();
    }

    // --- VERİTABANI: STORED PROCEDURE ÇAĞIRMA (Kritik Kısım) ---

    private void islemOduncVer() {
        try {
            Connection conn = baglantiAl();
            // Java'dan SQL Prosedürü çağırma komutu: { call ProsedürAdı(?, ?, ?) }
            String sql = "{ call sp_YeniOduncVer(?, ?, ?) }";
            CallableStatement cstmt = conn.prepareCall(sql);

            // Parametreleri ayarla
            cstmt.setInt(1, Integer.parseInt(txtUyeID.getText()));   // UyeID
            cstmt.setInt(2, Integer.parseInt(txtKitapID.getText())); // KitapID
            cstmt.setInt(3, Integer.parseInt(txtPersonelID.getText())); // PersonelID

            cstmt.execute(); // Çalıştır
            conn.close();

            JOptionPane.showMessageDialog(this, "İşlem Başarılı! Kitap ödünç verildi.\nStok otomatik düştü.");
            aktifOduncleriListele(); // Diğer sekmeyi güncelle

        } catch (SQLException ex) {
            // Veritabanından gelen özel hataları (Stok yok vb.) göster
            JOptionPane.showMessageDialog(this, "HATA: " + ex.getMessage());
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Giriş Hatası: ID'ler sayı olmalı.");
        }
    }

    private void islemTeslimAl() {
        int seciliSatir = tableTeslim.getSelectedRow();
        if (seciliSatir == -1) {
            JOptionPane.showMessageDialog(this, "Lütfen teslim alınacak satırı seçin.");
            return;
        }

        int oduncID = (int) modelTeslim.getValueAt(seciliSatir, 0); // 0. kolon ID

        try {
            Connection conn = baglantiAl();
            // Teslim alma prosedürünü çağır
            String sql = "{ call sp_KitapTeslimAl(?) }";
            CallableStatement cstmt = conn.prepareCall(sql);

            cstmt.setInt(1, oduncID);

            cstmt.execute();
            conn.close();

            JOptionPane.showMessageDialog(this, "Kitap Teslim Alındı.\nVarsa ceza otomatik kesildi.");
            aktifOduncleriListele(); // Listeyi yenile

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Hata: " + ex.getMessage());
        }
    }

    private void aktifOduncleriListele() {
        // Sadece teslim edilmemiş (Aktif) kitapları getirir
        try {
            modelTeslim.setRowCount(0);
            Connection conn = baglantiAl();

            // Kullanıcı adlarını görmek için JOIN işlemi yapıyoruz
            String sql = "SELECT O.OduncID, CONCAT(U.Ad,' ',U.Soyad) as UyeAd, K.KitapAdi, O.OduncTarihi, O.SonTeslimTarihi " +
                    "FROM ODUNC O " +
                    "JOIN UYE U ON O.UyeID = U.UyeID " +
                    "JOIN KITAP K ON O.KitapID = K.KitapID " +
                    "WHERE O.TeslimTarihi IS NULL";

            PreparedStatement ps = conn.prepareStatement(sql);
            ResultSet rs = ps.executeQuery();

            while(rs.next()) {
                modelTeslim.addRow(new Object[] {
                        rs.getInt("OduncID"),
                        rs.getString("UyeAd"),
                        rs.getString("KitapAdi"),
                        rs.getDate("OduncTarihi"),
                        rs.getDate("SonTeslimTarihi")
                });
            }
            conn.close();

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private Connection baglantiAl() throws Exception {
        String url = "jdbc:mysql://localhost:3306/kütüphanedb?useUnicode=true&characterEncoding=utf8";
        return DriverManager.getConnection(url, "root", "");
    }
}