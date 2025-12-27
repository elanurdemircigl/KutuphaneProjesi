import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;

public class OduncEkrani extends JFrame {

    private JTabbedPane tabbedPane;
    private JTextField txtUyeID, txtKitapID, txtPersonelID;
    private JTable tableTeslim;
    private DefaultTableModel modelTeslim;

    public OduncEkrani() {
        setTitle("Ödünç ve İade İşlemleri");
        setSize(850, 650);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        tabbedPane = new JTabbedPane();

        // 1. Sekme: Ödünç Verme
        JPanel panelOdunc = new JPanel(null);
        panelOduncHazirla(panelOdunc);
        tabbedPane.addTab("Ödünç Ver", panelOdunc);

        // 2. Sekme: Teslim Alma
        JPanel panelTeslim = new JPanel(null);
        panelTeslimHazirla(panelTeslim);
        tabbedPane.addTab("Teslim Al (İade)", panelTeslim);

        add(tabbedPane, BorderLayout.CENTER);
    }

    private void panelOduncHazirla(JPanel p) {
        JLabel lblUye = new JLabel("Üye ID:");
        lblUye.setBounds(50, 50, 100, 25);
        p.add(lblUye);

        txtUyeID = new JTextField();
        txtUyeID.setBounds(150, 50, 150, 25);
        p.add(txtUyeID);

        // --- BONUS ÖZELLİK: "?" BUTONU VE AKTİF ÖDÜNÇ LİSTESİ ---
        JButton btnUyeSorgula = new JButton("?");
        btnUyeSorgula.setBounds(310, 50, 45, 25);
        btnUyeSorgula.setToolTipText("Üyenin elindeki kitapları gör");
        btnUyeSorgula.addActionListener(e -> aktifUyeOduncGoster());
        p.add(btnUyeSorgula);

        JLabel lblKitap = new JLabel("Kitap ID:");
        lblKitap.setBounds(50, 90, 100, 25);
        p.add(lblKitap);

        txtKitapID = new JTextField();
        txtKitapID.setBounds(150, 90, 150, 25);
        p.add(txtKitapID);

        JLabel lblPer = new JLabel("Personel ID:");
        lblPer.setBounds(50, 130, 100, 25);
        p.add(lblPer);

        txtPersonelID = new JTextField("1");
        txtPersonelID.setBounds(150, 130, 150, 25);
        txtPersonelID.setEditable(false);
        p.add(txtPersonelID);

        JButton btnVer = new JButton("Ödünç Ver (Prosedürü Çalıştır)");
        btnVer.setBounds(150, 180, 250, 40);
        btnVer.setBackground(new Color(100, 200, 100));
        p.add(btnVer);

        btnVer.addActionListener(e -> islemOduncVer());
    }

    // --- BONUS METOT: ÜYENİN ÜZERİNDEKİ KİTAPLARI SORGULA ---
    private void aktifUyeOduncGoster() {
        String uyeID = txtUyeID.getText();
        if (uyeID.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Lütfen önce bir Üye ID giriniz.");
            return;
        }

        try (Connection conn = baglantiAl()) {
            // image_57fda1.jpg'deki SQL hatası burada düzeltilmiştir
            String sql = "SELECT K.KitapAdi, O.OduncTarihi, O.SonTeslimTarihi " +
                    "FROM ODUNC O " +
                    "JOIN KITAP K ON O.KitapID = K.KitapID " +
                    "WHERE O.UyeID = ? AND O.TeslimTarihi IS NULL";

            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, Integer.parseInt(uyeID));
            ResultSet rs = ps.executeQuery();

            StringBuilder mesaj = new StringBuilder("Üyenin Teslim Etmediği Kitaplar:\n");
            int sayac = 0;
            while (rs.next()) {
                sayac++;
                mesaj.append(sayac).append(". ").append(rs.getString("KitapAdi"))
                        .append(" (Son Teslim: ").append(rs.getDate("SonTeslimTarihi")).append(")\n");
            }

            if (sayac == 0) {
                JOptionPane.showMessageDialog(this, "Bu üyenin üzerinde bekleyen ödünç kitap bulunmuyor.");
            } else {
                // Dokümandaki 5 kitap sınırı uyarısı
                mesaj.append("\nToplam Aktif Ödünç: ").append(sayac).append(" / 5");
                JOptionPane.showMessageDialog(this, mesaj.toString());
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Hata: " + ex.getMessage());
        }
    }

    private void panelTeslimHazirla(JPanel p) {
        modelTeslim = new DefaultTableModel();
        modelTeslim.setColumnIdentifiers(new String[]{"Ödünç ID", "Üye", "Kitap", "Veriliş Tarihi", "Son Teslim"});
        tableTeslim = new JTable(modelTeslim);
        JScrollPane scroll = new JScrollPane(tableTeslim);
        scroll.setBounds(20, 20, 760, 400);
        p.add(scroll);

        JButton btnTeslimAl = new JButton("Seçili Kitabı Teslim Al");
        btnTeslimAl.setBounds(20, 440, 220, 40);
        btnTeslimAl.setBackground(new Color(200, 100, 100));
        p.add(btnTeslimAl);

        JButton btnYenile = new JButton("Listeyi Yenile");
        btnYenile.setBounds(260, 440, 150, 40);
        p.add(btnYenile);

        btnTeslimAl.addActionListener(e -> islemTeslimAl());
        btnYenile.addActionListener(e -> aktifOduncleriListele());
        aktifOduncleriListele();
    }

    private void islemOduncVer() {
        try (Connection conn = baglantiAl()) {
            String sql = "{ call sp_YeniOduncVer(?, ?, ?) }";
            CallableStatement cstmt = conn.prepareCall(sql);
            cstmt.setInt(1, Integer.parseInt(txtUyeID.getText()));
            cstmt.setInt(2, Integer.parseInt(txtKitapID.getText()));
            cstmt.setInt(3, Integer.parseInt(txtPersonelID.getText()));

            cstmt.execute();
            JOptionPane.showMessageDialog(this, "İşlem Başarılı! Kitap ödünç verildi.");
            aktifOduncleriListele();

        } catch (SQLException ex) {
            // Veritabanındaki "5 Kitap Limiti" hatası burada yakalanır
            JOptionPane.showMessageDialog(this, "Sistem Hatası: " + ex.getMessage());
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Hata: Lütfen geçerli ID'ler giriniz.");
        }
    }

    private void islemTeslimAl() {
        int seciliSatir = tableTeslim.getSelectedRow();
        if (seciliSatir == -1) {
            JOptionPane.showMessageDialog(this, "Lütfen iade edilecek kitabı tablodan seçin.");
            return;
        }
        int oduncID = (int) modelTeslim.getValueAt(seciliSatir, 0);
        try (Connection conn = baglantiAl()) {
            CallableStatement cstmt = conn.prepareCall("{ call sp_KitapTeslimAl(?) }");
            cstmt.setInt(1, oduncID);
            cstmt.execute();
            JOptionPane.showMessageDialog(this, "Kitap başarıyla teslim alındı.");
            aktifOduncleriListele();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Hata: " + ex.getMessage());
        }
    }

    private void aktifOduncleriListele() {
        try (Connection conn = baglantiAl()) {
            modelTeslim.setRowCount(0);
            String sql = "SELECT O.OduncID, CONCAT(U.Ad,' ',U.Soyad) as UyeAd, K.KitapAdi, O.OduncTarihi, O.SonTeslimTarihi " +
                    "FROM ODUNC O " +
                    "JOIN UYE U ON O.UyeID = U.UyeID " +
                    "JOIN KITAP K ON O.KitapID = K.KitapID " +
                    "WHERE O.TeslimTarihi IS NULL";
            PreparedStatement ps = conn.prepareStatement(sql);
            ResultSet rs = ps.executeQuery();
            while(rs.next()) {
                modelTeslim.addRow(new Object[] {
                        rs.getInt("OduncID"), rs.getString("UyeAd"), rs.getString("KitapAdi"),
                        rs.getDate("OduncTarihi"), rs.getDate("SonTeslimTarihi")
                });
            }
        } catch (Exception ex) { ex.printStackTrace(); }
    }

    private Connection baglantiAl() throws Exception {
        return DriverManager.getConnection("jdbc:mysql://localhost:3306/kütüphanedb?useUnicode=true&characterEncoding=utf8", "root", "");
    }
}