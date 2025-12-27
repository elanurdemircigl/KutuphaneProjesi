import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.*;

public class RaporEkrani extends JFrame {
    private JTable table;
    private DefaultTableModel model;
    private JTextField txtKitapAd, txtYazar, txtYilMin, txtYilMax, txtBaslangic, txtBitis, txtUyeID;
    private JComboBox<String> cbKategori;
    private JCheckBox chkSadeceMevcut;
    private JTabbedPane tabs;

    public RaporEkrani() {
        setTitle("Kütüphane Rapor ve Dinamik Sorgu Paneli");
        setSize(1000, 750);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        // --- TABLO ---
        model = new DefaultTableModel();
        table = new JTable(model);
        add(new JScrollPane(table), BorderLayout.CENTER);

        // --- ALT PANEL: EXCEL BUTONU ---
        JPanel pnlAlt = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton btnExcel = new JButton("Excel'e Aktar (CSV)");
        btnExcel.setBackground(new Color(34, 139, 34)); // Orman Yeşili
        btnExcel.setForeground(Color.WHITE);
        pnlAlt.add(btnExcel);
        add(pnlAlt, BorderLayout.SOUTH);

        tabs = new JTabbedPane();

        // =================================================================
        // SEKME 1: STATİK RAPORLAR (Tasarımı İyileştirildi)
        // =================================================================
        JPanel pnlStatik = new JPanel(new GridLayout(3, 1, 10, 10)); // Satırlar arası boşluk artırıldı
        pnlStatik.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10)); // Kenar boşluğu

        // 1. Panel: Üye Özet
        JPanel pnlOzet = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 10));
        pnlOzet.setBorder(BorderFactory.createTitledBorder("Üye Özet Raporu"));
        txtUyeID = new JTextField(10);
        JButton btnOzet = new JButton("Özet Getir");
        pnlOzet.add(new JLabel("Üye ID:"));
        pnlOzet.add(txtUyeID);
        pnlOzet.add(btnOzet);

        // 2. Panel: Tarih Aralığı
        JPanel pnlTarih = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 10));
        pnlTarih.setBorder(BorderFactory.createTitledBorder("Tarih Aralığı Ödünç Raporu"));
        txtBaslangic = new JTextField("2024-01-01", 10);
        txtBitis = new JTextField("2025-12-31", 10);
        JButton btnTarih = new JButton("Sorgula");
        pnlTarih.add(new JLabel("Başlangıç:")); pnlTarih.add(txtBaslangic);
        pnlTarih.add(new JLabel("Bitiş:")); pnlTarih.add(txtBitis); pnlTarih.add(btnTarih);

        // 3. Panel: Hazır Butonlar
        JPanel pnlButonlar = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 10));
        pnlButonlar.setBorder(BorderFactory.createTitledBorder("Hızlı Raporlar"));
        JButton btnGeciken = new JButton("Geciken Kitaplar");
        btnGeciken.setBackground(new Color(220, 53, 69)); // Kırmızı
        btnGeciken.setForeground(Color.WHITE);

        JButton btnPopuler = new JButton("En Çok Ödünç Alınanlar");
        btnPopuler.setBackground(new Color(255, 193, 7)); // Sarı

        pnlButonlar.add(btnGeciken);
        pnlButonlar.add(btnPopuler);

        pnlStatik.add(pnlOzet); pnlStatik.add(pnlTarih); pnlStatik.add(pnlButonlar);
        tabs.addTab("Statik Raporlar", pnlStatik);

        // =================================================================
        // SEKME 2: DİNAMİK SORGU (Tamamen Düzeltildi - GridBagLayout)
        // =================================================================
        JPanel pnlDinamik = new JPanel(new GridBagLayout());
        pnlDinamik.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5); // Bileşenler arası boşluk
        gbc.fill = GridBagConstraints.HORIZONTAL; // Kutular yatayda genişlesin

        // --- SATIR 1 ---
        // Kitap Adı
        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 0.1;
        pnlDinamik.add(new JLabel("Kitap Adı:"), gbc);

        gbc.gridx = 1; gbc.gridy = 0; gbc.weightx = 0.4;
        txtKitapAd = new JTextField();
        pnlDinamik.add(txtKitapAd, gbc);

        // Yazar
        gbc.gridx = 2; gbc.gridy = 0; gbc.weightx = 0.1;
        pnlDinamik.add(new JLabel("Yazar:"), gbc);

        gbc.gridx = 3; gbc.gridy = 0; gbc.weightx = 0.4;
        txtYazar = new JTextField();
        pnlDinamik.add(txtYazar, gbc);

        // --- SATIR 2 ---
        // Kategori
        gbc.gridx = 0; gbc.gridy = 1; gbc.weightx = 0.1;
        pnlDinamik.add(new JLabel("Kategori:"), gbc);

        gbc.gridx = 1; gbc.gridy = 1; gbc.weightx = 0.4;
        cbKategori = new JComboBox<>(new String[]{"Hepsi", "Roman", "Bilim", "Tarih", "Yazılım"});
        pnlDinamik.add(cbKategori, gbc);

        // Min Yıl
        gbc.gridx = 2; gbc.gridy = 1; gbc.weightx = 0.1;
        pnlDinamik.add(new JLabel("Min Yıl:"), gbc);

        gbc.gridx = 3; gbc.gridy = 1; gbc.weightx = 0.4;
        txtYilMin = new JTextField();
        pnlDinamik.add(txtYilMin, gbc);

        // --- SATIR 3 ---
        // Checkbox (Sadece Mevcut)
        gbc.gridx = 0; gbc.gridy = 2; gbc.weightx = 0.1;
        // Boş etiket (hizalamak için)
        pnlDinamik.add(new JLabel(""), gbc);

        gbc.gridx = 1; gbc.gridy = 2; gbc.weightx = 0.4;
        chkSadeceMevcut = new JCheckBox("Sadece Mevcut Kitaplar");
        pnlDinamik.add(chkSadeceMevcut, gbc);

        // Max Yıl
        gbc.gridx = 2; gbc.gridy = 2; gbc.weightx = 0.1;
        pnlDinamik.add(new JLabel("Max Yıl:"), gbc);

        gbc.gridx = 3; gbc.gridy = 2; gbc.weightx = 0.4;
        txtYilMax = new JTextField();
        pnlDinamik.add(txtYilMax, gbc);

        // --- SATIR 4: BUTON ---
        gbc.gridx = 0; gbc.gridy = 3;
        gbc.gridwidth = 4; // Buton tüm genişliği kaplasın
        gbc.ipady = 15; // Buton biraz yüksek olsun
        JButton btnDinamik = new JButton("DİNAMİK ARA");
        btnDinamik.setBackground(new Color(0, 123, 255)); // Mavi
        btnDinamik.setForeground(Color.WHITE);
        btnDinamik.setFont(new Font("Arial", Font.BOLD, 14));
        pnlDinamik.add(btnDinamik, gbc);

        tabs.addTab("Dinamik Sorgu", pnlDinamik);
        add(tabs, BorderLayout.NORTH);

        // --- AKSİYONLAR (Değişmedi) ---
        btnOzet.addActionListener(e -> uyeOzetGetir());
        btnTarih.addActionListener(e -> tarihRaporuGetir());
        btnGeciken.addActionListener(e -> raporGetir("SELECT U.Ad, U.Soyad, K.KitapAdi, O.SonTeslimTarihi, DATEDIFF(NOW(), O.SonTeslimTarihi) as GecikmeGun FROM ODUNC O JOIN UYE U ON O.UyeID = U.UyeID JOIN KITAP K ON O.KitapID = K.KitapID WHERE O.TeslimTarihi IS NULL AND O.SonTeslimTarihi < NOW()"));
        btnPopuler.addActionListener(e -> raporGetir("SELECT K.KitapAdi, K.Yazar, COUNT(O.OduncID) as OduncSayisi FROM ODUNC O JOIN KITAP K ON O.KitapID = K.KitapID GROUP BY K.KitapAdi, K.Yazar ORDER BY OduncSayisi DESC"));
        btnDinamik.addActionListener(e -> dinamikAramaYap());
        btnExcel.addActionListener(e -> excelDisaAktar());
    }

    // --- SEKME DEĞİŞTİRME METODU ---
    public void setTab(int index) {
        if (tabs != null) {
            tabs.setSelectedIndex(index);
        }
    }

    // --- METOTLAR (Excel, Veritabanı vb. - Aynı Kaldı) ---

    private void excelDisaAktar() {
        try (FileWriter fw = new FileWriter("Kutuphane_Rapor.csv")) {
            for (int i = 0; i < model.getColumnCount(); i++) {
                fw.write(model.getColumnName(i) + (i == model.getColumnCount() - 1 ? "" : ","));
            }
            fw.write("\n");
            for (int i = 0; i < model.getRowCount(); i++) {
                for (int j = 0; j < model.getColumnCount(); j++) {
                    Object val = model.getValueAt(i, j);
                    fw.write((val != null ? val.toString() : "") + (j == model.getColumnCount() - 1 ? "" : ","));
                }
                fw.write("\n");
            }
            JOptionPane.showMessageDialog(this, "Rapor 'Kutuphane_Rapor.csv' adıyla kaydedildi!");
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this, "Hata: " + ex.getMessage());
        }
    }

    private Connection baglantiAl() throws Exception {
        return DriverManager.getConnection("jdbc:mysql://localhost:3306/kütüphanedb?useUnicode=true&characterEncoding=utf8", "root", "");
    }

    private void uyeOzetGetir() {
        if(txtUyeID.getText().isEmpty()) return;
        try (Connection conn = baglantiAl(); CallableStatement cs = conn.prepareCall("{call sp_UyeOzetRapor(?)}")) {
            cs.setInt(1, Integer.parseInt(txtUyeID.getText()));
            tabloyuDoldur(cs.executeQuery());
        } catch (Exception ex) { JOptionPane.showMessageDialog(this, "Hata: " + ex.getMessage()); }
    }

    private void tarihRaporuGetir() {
        String sql = "SELECT U.Ad, U.Soyad, K.KitapAdi, O.OduncTarihi, O.SonTeslimTarihi FROM ODUNC O JOIN UYE U ON O.UyeID = U.UyeID JOIN KITAP K ON O.KitapID = K.KitapID WHERE O.OduncTarihi BETWEEN ? AND ?";
        try (Connection conn = baglantiAl(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, txtBaslangic.getText());
            ps.setString(2, txtBitis.getText());
            tabloyuDoldur(ps.executeQuery());
        } catch (Exception ex) { JOptionPane.showMessageDialog(this, "Hata: " + ex.getMessage()); }
    }

    private void dinamikAramaYap() {
        StringBuilder sql = new StringBuilder("SELECT * FROM KITAP WHERE 1=1");
        if (!txtKitapAd.getText().isEmpty()) sql.append(" AND KitapAdi LIKE '%").append(txtKitapAd.getText()).append("%'");
        if (!txtYazar.getText().isEmpty()) sql.append(" AND Yazar LIKE '%").append(txtYazar.getText()).append("%'");
        if (cbKategori.getSelectedIndex() > 0) sql.append(" AND Kategori = '").append(cbKategori.getSelectedItem()).append("'");
        if (!txtYilMin.getText().isEmpty()) sql.append(" AND BasimYili >= ").append(txtYilMin.getText());
        if (!txtYilMax.getText().isEmpty()) sql.append(" AND BasimYili <= ").append(txtYilMax.getText());
        if (chkSadeceMevcut.isSelected()) sql.append(" AND MevcutAdet > 0");
        raporGetir(sql.toString());
    }

    private void raporGetir(String sql) {
        try (Connection conn = baglantiAl(); Statement stmt = conn.createStatement()) {
            tabloyuDoldur(stmt.executeQuery(sql));
        } catch (Exception ex) { JOptionPane.showMessageDialog(this, "Hata: " + ex.getMessage()); }
    }

    private void tabloyuDoldur(ResultSet rs) throws SQLException {
        ResultSetMetaData md = rs.getMetaData();
        int cols = md.getColumnCount();
        model.setRowCount(0); model.setColumnCount(0);
        for (int i = 1; i <= cols; i++) model.addColumn(md.getColumnName(i));
        while (rs.next()) {
            Object[] row = new Object[cols];
            for (int i = 1; i <= cols; i++) row[i-1] = rs.getObject(i);
            model.addRow(row);
        }
    }
}