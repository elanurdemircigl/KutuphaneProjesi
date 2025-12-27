import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;

public class RaporEkrani extends JFrame {
    private JTable table;
    private DefaultTableModel model;
    private JTextField txtKitapAd, txtYazar, txtYilMin, txtYilMax, txtBaslangic, txtBitis, txtUyeID;
    private JComboBox<String> cbKategori;
    private JCheckBox chkSadeceMevcut;

    public RaporEkrani() {
        setTitle("Kütüphane Rapor ve Dinamik Sorgu Paneli");
        setSize(1000, 700);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        model = new DefaultTableModel();
        table = new JTable(model);
        add(new JScrollPane(table), BorderLayout.CENTER);

        JTabbedPane tabs = new JTabbedPane();

        // --- SEKME 1: ZORUNLU STATİK RAPORLAR ---
        JPanel pnlStatik = new JPanel(new GridLayout(3, 1, 5, 5));

        // 1. Üye Özet Raporu (Zorunlu Prosedür Çağrısı)
        JPanel pnlOzet = new JPanel(new FlowLayout(FlowLayout.LEFT));
        pnlOzet.setBorder(BorderFactory.createTitledBorder("Üye Özet Raporu (sp_UyeOzetRapor)"));
        txtUyeID = new JTextField(5);
        JButton btnOzet = new JButton("Özet Getir");
        pnlOzet.add(new JLabel("Üye ID:")); pnlOzet.add(txtUyeID); pnlOzet.add(btnOzet);

        // 2. Tarih Aralığı Raporu (Zorunlu Rapor 1)
        JPanel pnlTarih = new JPanel(new FlowLayout(FlowLayout.LEFT));
        pnlTarih.setBorder(BorderFactory.createTitledBorder("Tarih Aralığı Ödünç Raporu"));
        txtBaslangic = new JTextField("2024-01-01", 10);
        txtBitis = new JTextField("2025-12-31", 10);
        JButton btnTarih = new JButton("Sorgula");
        pnlTarih.add(new JLabel("Başlangıç:")); pnlTarih.add(txtBaslangic);
        pnlTarih.add(new JLabel("Bitiş:")); pnlTarih.add(txtBitis); pnlTarih.add(btnTarih);

        // 3. Diğer Hazır Raporlar (Zorunlu Rapor 2 ve 3)
        JPanel pnlButonlar = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton btnGeciken = new JButton("Geciken Kitaplar");
        JButton btnPopuler = new JButton("En Çok Ödünç Alınanlar");
        pnlButonlar.add(btnGeciken); pnlButonlar.add(btnPopuler);

        pnlStatik.add(pnlOzet); pnlStatik.add(pnlTarih); pnlStatik.add(pnlButonlar);
        tabs.addTab("Statik Raporlar", pnlStatik);

        // --- SEKME 2: DİNAMİK SORGU EKRANI (Zorunlu Madde 5.2) ---
        JPanel pnlDinamik = new JPanel(new GridLayout(4, 4, 10, 10));
        pnlDinamik.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        pnlDinamik.add(new JLabel("Kitap Adı:")); txtKitapAd = new JTextField(); pnlDinamik.add(txtKitapAd);
        pnlDinamik.add(new JLabel("Yazar:")); txtYazar = new JTextField(); pnlDinamik.add(txtYazar);
        pnlDinamik.add(new JLabel("Kategori:"));
        cbKategori = new JComboBox<>(new String[]{"Hepsi", "Roman", "Bilim", "Tarih", "Yazılım"});
        pnlDinamik.add(cbKategori);
        pnlDinamik.add(new JLabel("Min Yıl:")); txtYilMin = new JTextField(); pnlDinamik.add(txtYilMin);
        pnlDinamik.add(new JLabel("Max Yıl:")); txtYilMax = new JTextField(); pnlDinamik.add(txtYilMax);
        chkSadeceMevcut = new JCheckBox("Sadece Mevcut Kitaplar"); pnlDinamik.add(chkSadeceMevcut);

        JButton btnDinamik = new JButton("DİNAMİK ARA");
        btnDinamik.setBackground(Color.BLUE); btnDinamik.setForeground(Color.WHITE);
        pnlDinamik.add(btnDinamik);

        tabs.addTab("Dinamik Sorgu", pnlDinamik);
        add(tabs, BorderLayout.NORTH);

        // --- AKSİYONLAR ---
        btnOzet.addActionListener(e -> uyeOzetGetir());
        btnTarih.addActionListener(e -> tarihRaporuGetir());
        btnGeciken.addActionListener(e -> raporGetir("SELECT U.Ad, U.Soyad, K.KitapAdi, O.SonTeslimTarihi FROM ODUNC O JOIN UYE U ON O.UyeID = U.UyeID JOIN KITAP K ON O.KitapID = K.KitapID WHERE O.TeslimTarihi IS NULL AND O.SonTeslimTarihi < NOW()"));
        btnPopuler.addActionListener(e -> raporGetir("SELECT K.KitapAdi, COUNT(O.OduncID) as Adet FROM ODUNC O JOIN KITAP K ON O.KitapID = K.KitapID GROUP BY K.KitapAdi ORDER BY Adet DESC"));
        btnDinamik.addActionListener(e -> dinamikAramaYap());
    }

    private Connection baglantiAl() throws Exception {
        return DriverManager.getConnection("jdbc:mysql://localhost:3306/kütüphanedb?useUnicode=true&characterEncoding=utf8", "root", "");
    }

    private void uyeOzetGetir() {
        try (Connection conn = baglantiAl();
             CallableStatement cs = conn.prepareCall("{call sp_UyeOzetRapor(?)}")) {
            cs.setInt(1, Integer.parseInt(txtUyeID.getText()));
            tabloyuDoldur(cs.executeQuery());
        } catch (Exception ex) { JOptionPane.showMessageDialog(this, "Hata: " + ex.getMessage()); }
    }

    private void tarihRaporuGetir() {
        String sql = "SELECT U.Ad, U.Soyad, K.KitapAdi, O.OduncTarihi FROM ODUNC O JOIN UYE U ON O.UyeID = U.UyeID JOIN KITAP K ON O.KitapID = K.KitapID WHERE O.OduncTarihi BETWEEN ? AND ?";
        try (Connection conn = baglantiAl();
             PreparedStatement ps = conn.prepareStatement(sql)) {
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