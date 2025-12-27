import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;

public class CezaEkrani extends JFrame {
    private JTable table;
    private DefaultTableModel model;
    private JTextField txtUyeID, txtBaslangic, txtBitis;
    private JLabel lblToplamBorc;

    public CezaEkrani() {
        setTitle("4.7 Ceza Görüntüleme Ekranı");
        setSize(800, 500);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        // --- ÜST PANEL: FİLTRELER ---
        JPanel pnlFiltre = new JPanel(new FlowLayout(FlowLayout.LEFT));
        pnlFiltre.setBorder(BorderFactory.createTitledBorder("Filtreleme Seçenekleri"));

        txtUyeID = new JTextField(5);
        txtBaslangic = new JTextField("2024-01-01", 10);
        txtBitis = new JTextField("2025-12-31", 10);
        JButton btnSorgula = new JButton("Cezaları Listele");

        pnlFiltre.add(new JLabel("Üye ID:")); pnlFiltre.add(txtUyeID);
        pnlFiltre.add(new JLabel("Başlangıç:")); pnlFiltre.add(txtBaslangic);
        pnlFiltre.add(new JLabel("Bitiş:")); pnlFiltre.add(txtBitis);
        pnlFiltre.add(btnSorgula);

        add(pnlFiltre, BorderLayout.NORTH);

        // --- ORTA PANEL: TABLO ---
        model = new DefaultTableModel();
        model.setColumnIdentifiers(new String[]{"Ceza ID", "Kitap Adı", "Ceza Tutarı", "İade Tarihi"});
        table = new JTable(model);
        add(new JScrollPane(table), BorderLayout.CENTER);

        // --- ALT PANEL: TOPLAM BORÇ ---
        JPanel pnlAlt = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        lblToplamBorc = new JLabel("Üyenin Toplam Borcu: 0.00 TL");
        lblToplamBorc.setFont(new Font("Arial", Font.BOLD, 14));
        lblToplamBorc.setForeground(Color.RED);
        pnlAlt.add(lblToplamBorc);
        add(pnlAlt, BorderLayout.SOUTH);

        // --- BUTON OLAYI ---
        btnSorgula.addActionListener(e -> cezaSorgula());
    }

    private void cezaSorgula() {
        String uyeID = txtUyeID.getText();
        if (uyeID.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Lütfen bir Üye ID giriniz!");
            return;
        }

        try (Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/kütüphanedb?useUnicode=true&characterEncoding=utf8", "root", "")) {
            // 1. Üyenin Toplam Borcunu Çek (Ödev 4.7 - Toplam borç alanı)
            String sqlBorc = "SELECT ToplamBorc FROM UYE WHERE UyeID = ?";
            PreparedStatement psBorc = conn.prepareStatement(sqlBorc);
            psBorc.setInt(1, Integer.parseInt(uyeID));
            ResultSet rsBorc = psBorc.executeQuery();
            if (rsBorc.next()) {
                lblToplamBorc.setText("Üyenin Toplam Borcu: " + rsBorc.getDouble("ToplamBorc") + " TL");
            }

            // 2. Cezaları Listele (Ödev 4.7 - Üyeye göre listeleme ve Tarih aralığı)
            model.setRowCount(0);
            String sqlListe = "SELECT C.CezaID, K.KitapAdi, C.Tutar, O.TeslimTarihi " +
                    "FROM CEZA C " +
                    "JOIN ODUNC O ON C.OduncID = O.OduncID " +
                    "JOIN KITAP K ON O.KitapID = K.KitapID " +
                    "WHERE C.UyeID = ? AND O.TeslimTarihi BETWEEN ? AND ?";
            PreparedStatement psListe = conn.prepareStatement(sqlListe);
            psListe.setInt(1, Integer.parseInt(uyeID));
            psListe.setString(2, txtBaslangic.getText());
            psListe.setString(3, txtBitis.getText());

            ResultSet rsListe = psListe.executeQuery();
            while (rsListe.next()) {
                model.addRow(new Object[]{
                        rsListe.getInt("CezaID"),
                        rsListe.getString("KitapAdi"),
                        rsListe.getDouble("Tutar") + " TL",
                        rsListe.getTimestamp("TeslimTarihi")
                });
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Hata: " + ex.getMessage());
        }
    }
}