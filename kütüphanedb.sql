-- phpMyAdmin SQL Dump
-- version 5.2.1
-- https://www.phpmyadmin.net/
--
-- Anamakine: 127.0.0.1
-- Üretim Zamanı: 27 Ara 2025, 17:25:00
-- Sunucu sürümü: 10.4.32-MariaDB
-- PHP Sürümü: 8.2.12

SET SQL_MODE = "NO_AUTO_VALUE_ON_ZERO";
START TRANSACTION;
SET time_zone = "+00:00";


/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8mb4 */;

--
-- Veritabanı: `kütüphanedb`
--

DELIMITER $$
--
-- Yordamlar
--
CREATE DEFINER=`root`@`localhost` PROCEDURE `sp_KitapAra` (IN `p_AramaMetni` VARCHAR(100), IN `p_Kategori` VARCHAR(50))   BEGIN
    SELECT * FROM KITAP
    WHERE 
        -- 1. Kural: Arama metni boşsa geç, doluysa Kitap Adı VEYA Yazar'da ara
        (
            p_AramaMetni IS NULL OR p_AramaMetni = '' 
            OR KitapAdi LIKE CONCAT('%', p_AramaMetni, '%') 
            OR Yazar LIKE CONCAT('%', p_AramaMetni, '%')
        )
        AND 
        -- 2. Kural: Kategori 'Hepsi' seçildiyse geç, değilse o kategoriyi getir
        (
            p_Kategori IS NULL OR p_Kategori = 'Hepsi' OR p_Kategori = '' 
            OR Kategori = p_Kategori
        );
END$$

CREATE DEFINER=`root`@`localhost` PROCEDURE `sp_KitapEkleVeyaGuncelle` (IN `p_KitapID` INT, IN `p_KitapAdi` VARCHAR(255), IN `p_Yazar` VARCHAR(255), IN `p_Kategori` VARCHAR(100), IN `p_Yayinevi` VARCHAR(255), IN `p_BasimYili` INT, IN `p_ToplamAdet` INT)   BEGIN
    -- Kitap mevcut mu kontrol et
    IF EXISTS (SELECT 1 FROM KITAP WHERE KitapID = p_KitapID) THEN
        -- Güncelleme işlemi
        UPDATE KITAP SET 
            KitapAdi = p_KitapAdi, 
            Yazar = p_Yazar, 
            Kategori = p_Kategori, 
            Yayinevi = p_Yayinevi, 
            BasimYili = p_BasimYili, 
            ToplamAdet = p_ToplamAdet,
            -- MevcutAdet'i de toplam sayıya göre orantılı güncelle (Opsiyonel mantık)
            MevcutAdet = MevcutAdet + (p_ToplamAdet - ToplamAdet)
        WHERE KitapID = p_KitapID;
    ELSE
        -- Yeni ekleme işlemi
        INSERT INTO KITAP (KitapAdi, Yazar, Kategori, Yayinevi, BasimYili, ToplamAdet, MevcutAdet) 
        VALUES (p_KitapAdi, p_Yazar, p_Kategori, p_Yayinevi, p_BasimYili, p_ToplamAdet, p_ToplamAdet);
    END IF;
END$$

CREATE DEFINER=`root`@`localhost` PROCEDURE `sp_KitapTeslimAl` (IN `p_OduncID` INT)   BEGIN
    DECLARE v_Gecikme INT;
    DECLARE v_UyeID INT;
    DECLARE v_SonTarih DATETIME;

    -- Gerekli bilgileri al
    SELECT UyeID, SonTeslimTarihi INTO v_UyeID, v_SonTarih
    FROM ODUNC WHERE OduncID = p_OduncID;

    -- Teslim tarihini güncelle (Bugün olarak)
    UPDATE ODUNC SET TeslimTarihi = NOW() WHERE OduncID = p_OduncID;

    -- Gecikme hesapla (Bugün - SonTeslimTarihi)
    SET v_Gecikme = DATEDIFF(NOW(), v_SonTarih);

    -- Eğer gecikme varsa ceza ekle (Günlük 5 TL diyelim)
    IF v_Gecikme > 0 THEN
        INSERT INTO CEZA (UyeID, OduncID, Tutar, Aciklama)
        VALUES (v_UyeID, p_OduncID, v_Gecikme * 5.0, CONCAT(v_Gecikme, ' gun gecikme cezasi'));
    END IF;
END$$

CREATE DEFINER=`root`@`localhost` PROCEDURE `sp_UyeOzetRapor` (IN `p_UyeID` INT)   BEGIN
    SELECT
        (SELECT COUNT(*) FROM ODUNC WHERE UyeID = p_UyeID) as ToplamAldigiKitap,
        (SELECT COUNT(*) FROM ODUNC WHERE UyeID = p_UyeID AND TeslimTarihi IS NULL) as ElindekiKitapSayisi,
        (SELECT ToplamBorc FROM UYE WHERE UyeID = p_UyeID) as ToplamBorc;
END$$

CREATE DEFINER=`root`@`localhost` PROCEDURE `sp_YeniOduncVer` (IN `p_UyeID` INT, IN `p_KitapID` INT, IN `p_KullaniciID` INT)   BEGIN
    DECLARE v_OduncTarihi DATE;
    DECLARE v_SonTeslimTarihi DATE;
    DECLARE v_MevcutStok INT;

    -- 1. Stok kontrolü yapalım
    SELECT MevcutAdet INTO v_MevcutStok FROM KITAP WHERE KitapID = p_KitapID;

    IF v_MevcutStok > 0 THEN
        -- 2. Tarihleri Hesapla (Otomatik 15 gün sonrası)
        SET v_OduncTarihi = CURDATE();
        SET v_SonTeslimTarihi = DATE_ADD(v_OduncTarihi, INTERVAL 15 DAY);

        -- 3. Ödünç kaydını ekle
        INSERT INTO ODUNC (UyeID, KitapID, KullaniciID, OduncTarihi, SonTeslimTarihi, TeslimTarihi)
        VALUES (p_UyeID, p_KitapID, p_KullaniciID, v_OduncTarihi, v_SonTeslimTarihi, NULL);

        -- 4. Kitap stoğunu 1 azalt
        UPDATE KITAP SET MevcutAdet = MevcutAdet - 1 WHERE KitapID = p_KitapID;
    ELSE
        -- Stok yoksa hata fırlat
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Stokta kitap yok!';
    END IF;
END$$

DELIMITER ;

-- --------------------------------------------------------

--
-- Tablo için tablo yapısı `ceza`
--

CREATE TABLE `ceza` (
  `CezaID` int(11) NOT NULL,
  `UyeID` int(11) DEFAULT NULL,
  `OduncID` int(11) DEFAULT NULL,
  `Tutar` decimal(10,2) NOT NULL,
  `CezaTarihi` datetime DEFAULT current_timestamp(),
  `Aciklama` varchar(255) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Tablo döküm verisi `ceza`
--

INSERT INTO `ceza` (`CezaID`, `UyeID`, `OduncID`, `Tutar`, `CezaTarihi`, `Aciklama`) VALUES
(8, 1, 1, 15.00, '2025-12-27 18:29:28', 'Gecikme Cezası'),
(9, 1, 2, 10.00, '2025-12-27 18:29:28', 'Kitap Yıpranma'),
(11, 1, 3, 5090.00, '2025-12-27 18:35:35', '1018 gun gecikme cezasi');

--
-- Tetikleyiciler `ceza`
--
DELIMITER $$
CREATE TRIGGER `TR_CEZA_INSERT` AFTER INSERT ON `ceza` FOR EACH ROW BEGIN
    UPDATE UYE SET ToplamBorc = ToplamBorc + NEW.Tutar WHERE UyeID = NEW.UyeID;

    INSERT INTO LOG_ISLEM (IslemTipi, Aciklama)
    VALUES ('CEZA EKLEME', CONCAT('UyeID: ', NEW.UyeID, ' icin ', NEW.Tutar, ' TL ceza eklendi.'));
END
$$
DELIMITER ;

-- --------------------------------------------------------

--
-- Tablo için tablo yapısı `kitap`
--

CREATE TABLE `kitap` (
  `KitapID` int(11) NOT NULL,
  `KitapAdi` varchar(150) NOT NULL,
  `Yazar` varchar(100) NOT NULL,
  `Kategori` varchar(50) DEFAULT NULL,
  `Yayinevi` varchar(100) DEFAULT NULL,
  `BasimYili` int(11) DEFAULT NULL,
  `ToplamAdet` int(11) NOT NULL,
  `MevcutAdet` int(11) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Tablo döküm verisi `kitap`
--

INSERT INTO `kitap` (`KitapID`, `KitapAdi`, `Yazar`, `Kategori`, `Yayinevi`, `BasimYili`, `ToplamAdet`, `MevcutAdet`) VALUES
(1, 'Sefiller', 'Victor Hugo', 'Roman', 'Can Yayınları', 1862, 8, 7),
(2, 'Suç ve Ceza', 'Dostoyevski', 'Roman', 'İş Bankası', 1866, 3, 1),
(3, 'Temiz Kod', 'Robert C. Martin', 'Yazılım', 'Pearson', 2008, 10, 9),
(4, 'Java Programlama', 'Deitel', 'Yazılım', 'Pearson', 2018, 5, 5),
(5, 'Nutuk', 'Mustafa Kemal Atatürk', 'Tarih', 'TTK', 1927, 10, 10);

-- --------------------------------------------------------

--
-- Tablo için tablo yapısı `kullanici`
--

CREATE TABLE `kullanici` (
  `KullaniciID` int(11) NOT NULL,
  `AdSoyad` varchar(100) NOT NULL,
  `KullaniciAdi` varchar(50) NOT NULL,
  `Sifre` varchar(50) NOT NULL,
  `Rol` varchar(20) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Tablo döküm verisi `kullanici`
--

INSERT INTO `kullanici` (`KullaniciID`, `AdSoyad`, `KullaniciAdi`, `Sifre`, `Rol`) VALUES
(1, 'Elanur Demircioğlu', 'elanur', '1234', 'Admin'),
(2, 'Elif Duru Eken', 'duru', '1234', 'Admin');

-- --------------------------------------------------------

--
-- Tablo için tablo yapısı `log_islem`
--

CREATE TABLE `log_islem` (
  `LogID` int(11) NOT NULL,
  `IslemTipi` varchar(50) DEFAULT NULL,
  `Aciklama` text DEFAULT NULL,
  `IslemTarihi` datetime DEFAULT current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Tablo döküm verisi `log_islem`
--

INSERT INTO `log_islem` (`LogID`, `IslemTipi`, `Aciklama`, `IslemTarihi`) VALUES
(1, 'ODUNC VERME', 'KitapID: 1 odunc verildi.', '2025-12-27 13:54:54'),
(2, 'ODUNC VERME', 'KitapID: 3 odunc verildi.', '2025-12-27 13:54:54'),
(3, 'ODUNC VERME', 'KitapID: 1 odunc verildi.', '2025-12-27 13:54:54'),
(4, 'TESLIM ALMA', 'OduncID: 2 teslim alindi.', '2025-12-27 14:29:49'),
(5, 'CEZA EKLEME', 'UyeID: 1 icin 2030.00 TL ceza eklendi.', '2025-12-27 14:29:49'),
(6, 'TESLIM ALMA', 'OduncID: 3 teslim alindi.', '2025-12-27 14:30:20'),
(7, 'ODUNC VERME', 'KitapID: 5 odunc verildi.', '2025-12-27 14:35:19'),
(8, 'ODUNC VERME', 'KitapID: 3 odunc verildi.', '2025-12-27 14:35:19'),
(9, 'ODUNC VERME', 'KitapID: 1 odunc verildi.', '2025-12-27 14:35:19'),
(10, 'ODUNC VERME', 'KitapID: 1 odunc verildi.', '2025-12-27 14:35:19'),
(11, 'ODUNC VERME', 'KitapID: 3 odunc verildi.', '2025-12-27 14:35:19'),
(12, 'ODUNC VERME', 'KitapID: 5 odunc verildi.', '2025-12-27 14:35:19'),
(13, 'CEZA EKLEME', 'UyeID: 1 icin 35.00 TL ceza eklendi.', '2025-12-27 14:35:19'),
(14, 'TESLIM ALMA', 'OduncID: 4 teslim alindi.', '2025-12-27 14:35:58'),
(15, 'CEZA EKLEME', 'UyeID: 6 icin 2010.00 TL ceza eklendi.', '2025-12-27 14:35:58'),
(16, 'TESLIM ALMA', 'OduncID: 9 teslim alindi.', '2025-12-27 14:36:01'),
(17, 'CEZA EKLEME', 'UyeID: 7 icin 1835.00 TL ceza eklendi.', '2025-12-27 14:36:01'),
(18, 'TESLIM ALMA', 'OduncID: 8 teslim alindi.', '2025-12-27 14:36:04'),
(19, 'TESLIM ALMA', 'OduncID: 5 teslim alindi.', '2025-12-27 14:36:07'),
(20, 'CEZA EKLEME', 'UyeID: 1 icin 1960.00 TL ceza eklendi.', '2025-12-27 14:36:07'),
(21, 'TESLIM ALMA', 'OduncID: 7 teslim alindi.', '2025-12-27 14:36:10'),
(22, 'ODUNC VERME', 'KitapID: 3 odunc verildi.', '2025-12-27 15:34:52'),
(23, 'TESLIM ALMA', 'OduncID: 13 teslim alindi.', '2025-12-27 15:37:35'),
(24, 'ODUNC VERME', 'KitapID: 4 odunc verildi.', '2025-12-27 15:45:29'),
(25, 'ODUNC VERME', 'KitapID: 3 odunc verildi.', '2025-12-27 15:45:34'),
(26, 'ODUNC VERME', 'KitapID: 5 odunc verildi.', '2025-12-27 15:45:39'),
(27, 'ODUNC VERME', 'KitapID: 5 odunc verildi.', '2025-12-27 15:45:42'),
(28, 'ODUNC VERME', 'KitapID: 1 odunc verildi.', '2025-12-27 15:46:35'),
(29, 'TESLIM ALMA', 'OduncID: 15 teslim alindi.', '2025-12-27 15:46:52'),
(30, 'TESLIM ALMA', 'OduncID: 19 teslim alindi.', '2025-12-27 15:46:54'),
(31, 'TESLIM ALMA', 'OduncID: 17 teslim alindi.', '2025-12-27 15:46:58'),
(32, 'TESLIM ALMA', 'OduncID: 16 teslim alindi.', '2025-12-27 15:47:01'),
(33, 'TESLIM ALMA', 'OduncID: 14 teslim alindi.', '2025-12-27 15:47:05'),
(34, 'ODUNC VERME', 'KitapID: 1 odunc verildi.', '2025-12-27 15:49:26'),
(35, 'TESLIM ALMA', 'OduncID: 20 teslim alindi.', '2025-12-27 16:01:55'),
(36, 'ODUNC VERME', 'KitapID: 1 odunc verildi.', '2025-12-27 18:29:28'),
(37, 'ODUNC VERME', 'KitapID: 2 odunc verildi.', '2025-12-27 18:29:28'),
(38, 'ODUNC VERME', 'KitapID: 1 odunc verildi.', '2025-12-27 18:29:28'),
(39, 'CEZA EKLEME', 'UyeID: 1 icin 15.00 TL ceza eklendi.', '2025-12-27 18:29:28'),
(40, 'CEZA EKLEME', 'UyeID: 1 icin 10.00 TL ceza eklendi.', '2025-12-27 18:29:28'),
(41, 'TESLIM ALMA', 'OduncID: 3 teslim alindi.', '2025-12-27 18:35:35'),
(42, 'CEZA EKLEME', 'UyeID: 1 icin 5090.00 TL ceza eklendi.', '2025-12-27 18:35:35'),
(43, 'ODUNC VERME', 'KitapID: 3 odunc verildi.', '2025-12-27 19:08:38'),
(44, 'ODUNC VERME', 'KitapID: 2 odunc verildi.', '2025-12-27 19:14:24'),
(45, 'TESLIM ALMA', 'OduncID: 24 teslim alindi.', '2025-12-27 19:16:26'),
(46, 'TESLIM ALMA', 'OduncID: 23 teslim alindi.', '2025-12-27 19:16:30');

-- --------------------------------------------------------

--
-- Tablo için tablo yapısı `odunc`
--

CREATE TABLE `odunc` (
  `OduncID` int(11) NOT NULL,
  `UyeID` int(11) DEFAULT NULL,
  `KitapID` int(11) DEFAULT NULL,
  `VerenPersonelID` int(11) DEFAULT NULL,
  `OduncTarihi` datetime DEFAULT current_timestamp(),
  `SonTeslimTarihi` datetime DEFAULT NULL,
  `TeslimTarihi` datetime DEFAULT NULL,
  `KullaniciID` int(11) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Tablo döküm verisi `odunc`
--

INSERT INTO `odunc` (`OduncID`, `UyeID`, `KitapID`, `VerenPersonelID`, `OduncTarihi`, `SonTeslimTarihi`, `TeslimTarihi`, `KullaniciID`) VALUES
(1, 1, 1, NULL, '2023-01-01 00:00:00', '2023-01-15 00:00:00', '2023-01-20 00:00:00', 1),
(2, 1, 2, NULL, '2023-02-01 00:00:00', '2023-02-15 00:00:00', '2023-02-20 00:00:00', 1),
(3, 1, 1, NULL, '2023-03-01 00:00:00', '2023-03-15 00:00:00', '2025-12-27 18:35:35', 1),
(23, 2, 3, NULL, '2025-12-27 00:00:00', '2026-01-11 00:00:00', '2025-12-27 19:16:30', 1),
(24, 9, 2, NULL, '2025-12-27 00:00:00', '2026-01-11 00:00:00', '2025-12-27 19:16:26', 1);

--
-- Tetikleyiciler `odunc`
--
DELIMITER $$
CREATE TRIGGER `TR_ODUNC_INSERT` AFTER INSERT ON `odunc` FOR EACH ROW BEGIN
    UPDATE KITAP SET MevcutAdet = MevcutAdet - 1 WHERE KitapID = NEW.KitapID;
    INSERT INTO LOG_ISLEM (IslemTipi, Aciklama)
    VALUES ('ODUNC VERME', CONCAT('KitapID: ', NEW.KitapID, ' odunc verildi.'));
END
$$
DELIMITER ;
DELIMITER $$
CREATE TRIGGER `TR_ODUNC_UPDATE_TESLIM` AFTER UPDATE ON `odunc` FOR EACH ROW BEGIN
    IF OLD.TeslimTarihi IS NULL AND NEW.TeslimTarihi IS NOT NULL THEN
        UPDATE KITAP SET MevcutAdet = MevcutAdet + 1 WHERE KitapID = NEW.KitapID;

        INSERT INTO LOG_ISLEM (IslemTipi, Aciklama)
        VALUES ('TESLIM ALMA', CONCAT('OduncID: ', NEW.OduncID, ' teslim alindi.'));
    END IF;
END
$$
DELIMITER ;

-- --------------------------------------------------------

--
-- Tablo için tablo yapısı `uye`
--

CREATE TABLE `uye` (
  `UyeID` int(11) NOT NULL,
  `Ad` varchar(50) NOT NULL,
  `Soyad` varchar(50) NOT NULL,
  `Telefon` varchar(15) DEFAULT NULL,
  `Email` varchar(100) DEFAULT NULL,
  `ToplamBorc` decimal(10,2) DEFAULT 0.00
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Tablo döküm verisi `uye`
--

INSERT INTO `uye` (`UyeID`, `Ad`, `Soyad`, `Telefon`, `Email`, `ToplamBorc`) VALUES
(1, 'mustafa', 'bozkurt', '5314025550', 'mstafabzkurt57@gmail.com', 7110.00),
(2, 'ela', 'nur', '5454231449', 'elanurdemircgl@gmail.com', 0.00),
(4, 'Betul', 'Yagli', '5552023030', 'betul.yagli@gmail.com', 0.00),
(5, 'Merve', 'Akyol', '5553034040', 'merve.akyol@gmail.com', 0.00),
(6, 'Melike', 'Boztepe', '5554045050', 'melike.boztepe@gmail.com', 2010.00),
(7, 'Bengisu', 'Ozkay', '5555056060', 'bengisu.ozkay@gmail.com', 1835.00),
(8, 'Duru', 'eken', '5435490414', 'duru.eken@omu.edu.tr', 0.00),
(9, 'Beyza', 'Yagli', '5546545165', 'bayza.yagli@gmail.com', 0.00);

--
-- Dökümü yapılmış tablolar için indeksler
--

--
-- Tablo için indeksler `ceza`
--
ALTER TABLE `ceza`
  ADD PRIMARY KEY (`CezaID`),
  ADD KEY `UyeID` (`UyeID`),
  ADD KEY `OduncID` (`OduncID`);

--
-- Tablo için indeksler `kitap`
--
ALTER TABLE `kitap`
  ADD PRIMARY KEY (`KitapID`);

--
-- Tablo için indeksler `kullanici`
--
ALTER TABLE `kullanici`
  ADD PRIMARY KEY (`KullaniciID`),
  ADD UNIQUE KEY `KullaniciAdi` (`KullaniciAdi`);

--
-- Tablo için indeksler `log_islem`
--
ALTER TABLE `log_islem`
  ADD PRIMARY KEY (`LogID`);

--
-- Tablo için indeksler `odunc`
--
ALTER TABLE `odunc`
  ADD PRIMARY KEY (`OduncID`),
  ADD KEY `UyeID` (`UyeID`),
  ADD KEY `KitapID` (`KitapID`),
  ADD KEY `VerenPersonelID` (`VerenPersonelID`),
  ADD KEY `fk_odunc_kullanici` (`KullaniciID`);

--
-- Tablo için indeksler `uye`
--
ALTER TABLE `uye`
  ADD PRIMARY KEY (`UyeID`);

--
-- Dökümü yapılmış tablolar için AUTO_INCREMENT değeri
--

--
-- Tablo için AUTO_INCREMENT değeri `ceza`
--
ALTER TABLE `ceza`
  MODIFY `CezaID` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=12;

--
-- Tablo için AUTO_INCREMENT değeri `kitap`
--
ALTER TABLE `kitap`
  MODIFY `KitapID` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=6;

--
-- Tablo için AUTO_INCREMENT değeri `kullanici`
--
ALTER TABLE `kullanici`
  MODIFY `KullaniciID` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=3;

--
-- Tablo için AUTO_INCREMENT değeri `log_islem`
--
ALTER TABLE `log_islem`
  MODIFY `LogID` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=47;

--
-- Tablo için AUTO_INCREMENT değeri `odunc`
--
ALTER TABLE `odunc`
  MODIFY `OduncID` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=25;

--
-- Tablo için AUTO_INCREMENT değeri `uye`
--
ALTER TABLE `uye`
  MODIFY `UyeID` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=10;

--
-- Dökümü yapılmış tablolar için kısıtlamalar
--

--
-- Tablo kısıtlamaları `ceza`
--
ALTER TABLE `ceza`
  ADD CONSTRAINT `ceza_ibfk_1` FOREIGN KEY (`UyeID`) REFERENCES `uye` (`UyeID`),
  ADD CONSTRAINT `ceza_ibfk_2` FOREIGN KEY (`OduncID`) REFERENCES `odunc` (`OduncID`);

--
-- Tablo kısıtlamaları `odunc`
--
ALTER TABLE `odunc`
  ADD CONSTRAINT `fk_odunc_kullanici` FOREIGN KEY (`KullaniciID`) REFERENCES `kullanici` (`KullaniciID`),
  ADD CONSTRAINT `odunc_ibfk_1` FOREIGN KEY (`UyeID`) REFERENCES `uye` (`UyeID`),
  ADD CONSTRAINT `odunc_ibfk_2` FOREIGN KEY (`KitapID`) REFERENCES `kitap` (`KitapID`),
  ADD CONSTRAINT `odunc_ibfk_3` FOREIGN KEY (`VerenPersonelID`) REFERENCES `kullanici` (`KullaniciID`);
COMMIT;

/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
