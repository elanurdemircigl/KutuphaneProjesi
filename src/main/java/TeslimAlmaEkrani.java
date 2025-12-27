import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.sql.*;

public class TeslimAlmaEkrani extends JFrame {

    private JTable table;
    private DefaultTableModel model;
    private JTextField txtAra;

    public TeslimAlmaEkrani() {
        setTitle("Kitap Teslim Alma (İade) Ekranı"); // Madde 4.6
        setSize(900, 600);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));

        JPanel pnlUst = new JPanel(new FlowLayout(FlowLayout.LEFT));
        pnlUst.add(new JLabel("Filtrele (Üye/Kitap Adı):"));
        txtAra = new JTextField(20);
        txtAra.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                listele(txtAra.getText());
            }
        });
        pnlUst.add(txtAra);

        JButton btnYenile = new JButton("Listeyi Yenile");
        btnYenile.addActionListener(e -> { txtAra.setText(""); listele(""); });
        pnlUst.add(btnYenile);

        add(pnlUst, BorderLayout.NORTH);
        model = new DefaultTableModel();
        model.setColumnIdentifiers(new String[]{"Ödünç ID", "Üye Adı", "Kitap Adı", "Veriliş Tarihi", "Son Teslim"});
        table = new JTable(model);
        add(new JScrollPane(table), BorderLayout.CENTER);

        JButton btnTeslim = new JButton("SEÇİLİ KİTABI TESLİM AL");
        btnTeslim.setBackground(new Color(200, 100, 100));
        btnTeslim.setForeground(Color.WHITE);
        btnTeslim.setFont(new Font("Arial", Font.BOLD, 14));
        btnTeslim.setPreferredSize(new Dimension(0, 50));
        btnTeslim.addActionListener(e -> teslimAl());

        add(btnTeslim, BorderLayout.SOUTH);

        listele("");
    }

    private void listele(String arama) {
        try (Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/kütüphanedb?useUnicode=true&characterEncoding=utf8", "root", "")) {
            model.setRowCount(0);
            String sql = "SELECT O.OduncID, CONCAT(U.Ad,' ',U.Soyad) as UyeAd, K.KitapAdi, O.OduncTarihi, O.SonTeslimTarihi " +
                    "FROM ODUNC O " +
                    "JOIN UYE U ON O.UyeID = U.UyeID " +
                    "JOIN KITAP K ON O.KitapID = K.KitapID " +
                    "WHERE O.TeslimTarihi IS NULL " +
                    "AND (U.Ad LIKE ? OR U.Soyad LIKE ? OR K.KitapAdi LIKE ?)";

            PreparedStatement ps = conn.prepareStatement(sql);
            String p = "%" + arama + "%";
            ps.setString(1, p); ps.setString(2, p); ps.setString(3, p);

            ResultSet rs = ps.executeQuery();
            while(rs.next()) {
                model.addRow(new Object[]{ rs.getInt("OduncID"), rs.getString("UyeAd"), rs.getString("KitapAdi"), rs.getDate("OduncTarihi"), rs.getDate("SonTeslimTarihi") });
            }
        } catch (Exception ex) { ex.printStackTrace(); }
    }

    private void teslimAl() {
        int satir = table.getSelectedRow();
        if (satir == -1) {
            JOptionPane.showMessageDialog(this, "Lütfen teslim alınacak kitabı seçin.");
            return;
        }
        int oduncID = (int) model.getValueAt(satir, 0);

        try (Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/kütüphanedb?useUnicode=true&characterEncoding=utf8", "root", "")) {
            CallableStatement cs = conn.prepareCall("{ call sp_KitapTeslimAl(?) }");
            cs.setInt(1, oduncID);
            cs.execute();

            PreparedStatement ps = conn.prepareStatement("SELECT Tutar, Aciklama FROM CEZA WHERE OduncID = ?");
            ps.setInt(1, oduncID);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                JOptionPane.showMessageDialog(this, "⚠️ GECİKME CEZASI KESİLDİ!\nTutar: " + rs.getDouble("Tutar") + " TL\n(" + rs.getString("Aciklama") + ")", "Uyarı", JOptionPane.WARNING_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this, "Kitap zamanında teslim alındı.");
            }
            listele("");

        } catch (Exception ex) { JOptionPane.showMessageDialog(this, "Hata: " + ex.getMessage()); }
    }
}