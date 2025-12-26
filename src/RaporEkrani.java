
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;

public class RaporEkrani extends JFrame {

    private JTabbedPane tabs;
    private JTable table;
    private DefaultTableModel model;

    // Dinamik Arama Alanları
    private JTextField txtKitapAd, txtYazar, txtKategori, txtYilMin, txtYilMax;

    public RaporEkrani() {
        setTitle("Raporlar ve Detaylı Arama");
        setSize(900, 600);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        // Ortak Tablo (Sonuçlar burada görünecek)
        model = new DefaultTableModel();
        table = new JTable(model);
        JScrollPane scroll = new JScrollPane(table);
        add(scroll, BorderLayout.CENTER);

        // Sekmeler
        tabs = new JTabbedPane();

        // --- SEKME 1: HAZIR RAPORLAR ---
        JPanel pnlHazir = new JPanel();
        pnlHazir.setLayout(new FlowLayout());

        JButton btnGeciken = new JButton("Geciken Kitaplar Raporu");
        btnGeciken.setBackground(Color.RED);
        btnGeciken.setForeground(Color.WHITE);

        JButton btnPopuler = new JButton("En Çok Okunan Kitaplar");
        btnPopuler.setBackground(Color.ORANGE);

        JButton btnTumOdunc = new JButton("Tüm Ödünç Hareketleri");

        pnlHazir.add(btnGeciken);
        pnlHazir.add(btnPopuler);
        pnlHazir.add(btnTumOdunc);

        tabs.addTab("Hazır İstatistikler", pnlHazir);

        // --- SEKME 2: DİNAMİK ARAMA (Dinamik SQL) ---
        JPanel pnlDinamik = new JPanel(new GridLayout(2, 6, 5, 5));

        pnlDinamik.add(new JLabel("Kitap Adı:"));
        txtKitapAd = new JTextField();
        pnlDinamik.add(txtKitapAd);

        pnlDinamik.add(new JLabel("Yazar:"));
        txtYazar = new JTextField();
        pnlDinamik.add(txtYazar);

        pnlDinamik.add(new JLabel("Kategori:"));
        txtKategori = new JTextField();
        pnlDinamik.add(txtKategori);

        pnlDinamik.add(new JLabel("Min Yıl:"));
        txtYilMin = new JTextField();
        pnlDinamik.add(txtYilMin);

        pnlDinamik.add(new JLabel("Max Yıl:"));
        txtYilMax = new JTextField();
        pnlDinamik.add(txtYilMax);

        JButton btnSorgula = new JButton("ÖZEL SORGULA");
        btnSorgula.setBackground(Color.CYAN);
        pnlDinamik.add(btnSorgula);

        tabs.addTab("Detaylı Kitap Arama (Dinamik)", pnlDinamik);

        add(tabs, BorderLayout.NORTH);

        // --- BUTON AKSİYONLARI ---

        // 1. Gecikenler (Teslim Tarihi boş VE Son Teslim bugünden küçük)
        btnGeciken.addActionListener(e -> raporGetir(
                "SELECT U.Ad, U.Soyad, K.KitapAdi, O.SonTeslimTarihi, DATEDIFF(NOW(), O.SonTeslimTarihi) as GecikmeGun " +
                        "FROM ODUNC O " +
                        "JOIN UYE U ON O.UyeID = U.UyeID " +
                        "JOIN KITAP K ON O.KitapID = K.KitapID " +
                        "WHERE O.TeslimTarihi IS NULL AND O.SonTeslimTarihi < NOW()"
        ));

        // 2. En Çok Okunanlar (Group By ve Count)
        btnPopuler.addActionListener(e -> raporGetir(
                "SELECT K.KitapAdi, K.Yazar, COUNT(O.OduncID) as OkunmaSayisi " +
                        "FROM ODUNC O " +
                        "JOIN KITAP K ON O.KitapID = K.KitapID " +
                        "GROUP BY K.KitapAdi, K.Yazar " +
                        "ORDER BY OkunmaSayisi DESC"
        ));

        // 3. Tüm Hareketler
        btnTumOdunc.addActionListener(e -> raporGetir("SELECT * FROM LOG_ISLEM ORDER BY IslemTarihi DESC"));

        // 4. Dinamik Sorgu Butonu
        btnSorgula.addActionListener(e -> dinamikAramaYap());
    }

    // --- METOTLAR ---

    private void raporGetir(String sql) {
        try {
            model.setRowCount(0); // Tabloyu temizle
            model.setColumnCount(0); // Kolonları sıfırla (Çünkü her rapor farklı kolonlu)

            Connection conn = baglantiAl();
            PreparedStatement ps = conn.prepareStatement(sql);
            ResultSet rs = ps.executeQuery();
            ResultSetMetaData metaData = rs.getMetaData();

            // Kolon İsimlerini Otomatik Oluştur
            int kolonSayisi = metaData.getColumnCount();
            for (int i = 1; i <= kolonSayisi; i++) {
                model.addColumn(metaData.getColumnName(i));
            }

            // Verileri Doldur
            while (rs.next()) {
                Object[] satir = new Object[kolonSayisi];
                for (int i = 0; i < kolonSayisi; i++) {
                    satir[i] = rs.getObject(i + 1);
                }
                model.addRow(satir);
            }
            conn.close();

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Hata: " + ex.getMessage());
        }
    }

    // ÖDEVİN EN ÖNEMLİ KISMI: DİNAMİK SQL OLUŞTURMA
    private void dinamikAramaYap() {
        try {
            // 1. Temel Sorgu (WHERE 1=1 taktiği: Her zaman doğrudur, eklemeyi kolaylaştırır)
            String sql = "SELECT * FROM KITAP WHERE 1=1";

            // 2. Dolu alanları kontrol et ve sorguya ekle
            if (!txtKitapAd.getText().isEmpty()) {
                sql += " AND KitapAdi LIKE '%" + txtKitapAd.getText() + "%'";
            }
            if (!txtYazar.getText().isEmpty()) {
                sql += " AND Yazar LIKE '%" + txtYazar.getText() + "%'";
            }
            if (!txtKategori.getText().isEmpty()) {
                sql += " AND Kategori LIKE '%" + txtKategori.getText() + "%'";
            }
            if (!txtYilMin.getText().isEmpty()) {
                sql += " AND BasimYili >= " + txtYilMin.getText();
            }
            if (!txtYilMax.getText().isEmpty()) {
                sql += " AND BasimYili <= " + txtYilMax.getText();
            }

            // Oluşan sorguyu kullanıcıya göster (Bonus puan için güzel detay)
            System.out.println("Oluşturulan Sorgu: " + sql);

            // 3. Çalıştır
            raporGetir(sql);

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Hata: " + ex.getMessage());
        }
    }

    private Connection baglantiAl() throws Exception {
        String url = "jdbc:mysql://localhost:3306/kütüphanedb?useUnicode=true&characterEncoding=utf8";
        return DriverManager.getConnection(url, "root", "");
    }
}