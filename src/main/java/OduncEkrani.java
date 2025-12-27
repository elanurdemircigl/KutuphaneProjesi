import javax.swing.*;
import java.awt.*;
import java.sql.*;

public class OduncEkrani extends JFrame {

    private JTextField txtUyeID, txtKitapID, txtPersonelID;

    public OduncEkrani() {
        setTitle("Kitap Ödünç Verme Ekranı"); // Madde 4.5
        setSize(450, 400); // Boyut optimize edildi
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        // --- FORM PANELİ ---
        JPanel pnlForm = new JPanel(new GridLayout(4, 2, 5, 10));
        pnlForm.setBorder(BorderFactory.createTitledBorder("Ödünç İşlemi"));

        Dimension txtBoyut = new Dimension(130, 25);

        // 1. Üye ID ve ? Butonu
        JPanel pnlUyeSatir = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        txtUyeID = new JTextField();
        txtUyeID.setPreferredSize(txtBoyut);

        JButton btnUyeSorgula = new JButton("?");
        btnUyeSorgula.setPreferredSize(new Dimension(40, 25));
        btnUyeSorgula.setToolTipText("Üyenin elindeki kitapları gör");
        btnUyeSorgula.addActionListener(e -> aktifUyeOduncGoster()); // Senin Bonus Metodun

        pnlUyeSatir.add(txtUyeID);
        pnlUyeSatir.add(new JLabel(" ")); // Boşluk
        pnlUyeSatir.add(btnUyeSorgula);

        pnlForm.add(new JLabel("Üye ID:"));
        pnlForm.add(pnlUyeSatir);

        // 2. Kitap ID
        pnlForm.add(new JLabel("Kitap ID:"));
        txtKitapID = new JTextField(); txtKitapID.setPreferredSize(txtBoyut);
        pnlForm.add(txtKitapID);

        // 3. Personel ID
        pnlForm.add(new JLabel("Personel ID:"));
        txtPersonelID = new JTextField("1"); // Varsayılan Admin
        txtPersonelID.setEditable(false);
        txtPersonelID.setPreferredSize(txtBoyut);
        pnlForm.add(txtPersonelID);

        // Boşluk
        pnlForm.add(new JLabel("")); pnlForm.add(new JLabel(""));

        // --- BUTON ---
        JButton btnVer = new JButton("ÖDÜNÇ VER");
        btnVer.setBackground(new Color(100, 200, 100)); // Yeşil
        btnVer.setForeground(Color.WHITE);
        btnVer.setFont(new Font("Arial", Font.BOLD, 14));
        btnVer.setPreferredSize(new Dimension(0, 50));
        btnVer.addActionListener(e -> islemOduncVer());

        // Düzenleme
        JPanel pnlWrapper = new JPanel(new BorderLayout());
        pnlWrapper.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        pnlWrapper.add(pnlForm, BorderLayout.NORTH);

        add(pnlWrapper, BorderLayout.CENTER);
        add(btnVer, BorderLayout.SOUTH);
    }

    // --- BONUS METOT: ÜYENİN ÜZERİNDEKİ KİTAPLAR ---
    private void aktifUyeOduncGoster() {
        String uyeID = txtUyeID.getText();
        if (uyeID.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Lütfen önce bir Üye ID giriniz.");
            return;
        }

        try (Connection conn = baglantiAl()) {
            String sql = "SELECT K.KitapAdi, O.SonTeslimTarihi " +
                    "FROM ODUNC O " +
                    "JOIN KITAP K ON O.KitapID = K.KitapID " +
                    "WHERE O.UyeID = ? AND O.TeslimTarihi IS NULL";

            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, Integer.parseInt(uyeID));
            ResultSet rs = ps.executeQuery();

            StringBuilder mesaj = new StringBuilder("--- Üyenin Elindeki Kitaplar ---\n");
            int sayac = 0;
            while (rs.next()) {
                sayac++;
                mesaj.append(sayac).append(". ").append(rs.getString("KitapAdi"))
                        .append(" (Son: ").append(rs.getDate("SonTeslimTarihi")).append(")\n");
            }

            if (sayac == 0) {
                JOptionPane.showMessageDialog(this, "Bu üyenin üzerinde kitap yok.");
            } else {
                mesaj.append("\nToplam: ").append(sayac).append(" / 3 (Limit)");
                JOptionPane.showMessageDialog(this, mesaj.toString());
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Hata: " + ex.getMessage());
        }
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
            txtUyeID.setText(""); txtKitapID.setText("");

        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "HATA: " + ex.getMessage());
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Giriş Hatası: ID alanları sayı olmalı.");
        }
    }

    private Connection baglantiAl() throws Exception {
        return DriverManager.getConnection("jdbc:mysql://localhost:3306/kütüphanedb?useUnicode=true&characterEncoding=utf8", "root", "");
    }
}