-- Add display properties to buildings for frontend rendering

ALTER TABLE buildings ADD COLUMN fill_color VARCHAR(30) DEFAULT '179,167,154,230';
ALTER TABLE buildings ADD COLUMN label_min_zoom DOUBLE PRECISION DEFAULT 15.0;
ALTER TABLE buildings ADD COLUMN is_label_visible BOOLEAN DEFAULT TRUE;
ALTER TABLE buildings ADD COLUMN category VARCHAR(50) DEFAULT 'OTHER';

-- Populate categories and colors based on building names

-- KTX / Dormitory area (Blue)
UPDATE buildings SET category = 'DORMITORY', fill_color = '74,144,226,230'
WHERE name IN ('B5', 'B6', 'B7', 'B7bis', 'B8', 'B9', 'B10', 'B13', 'B3', 'TC',
  'Ký túc xá B6', 'Ký túc xá B8', 'Ký túc xá B10', 'Ký túc xá B3',
  'Ký túc xá B5', 'Ký túc xá B5b', 'Ký túc xá B7', 'Ký túc xá B7bis',
  'Hội trường B6', 'Căng tin B5-9',
  'Nhà Trung tâm Phục vụ Tổng hợp Bách khoa',
  'Trung tâm đào tạo thực hành ĐTVT', 'B13b', 'Nhà B4',
  'Số 75 Trần Đại Nghĩa', 'Nhà tôn', 'Trạm y tế', 'Nhà vệ sinh công cộng');

-- Library area (Purple)
UPDATE buildings SET category = 'LIBRARY', fill_color = '167,139,250,230'
WHERE name IN ('Thư viện Tạ Quang Bửu', 'Thư Viện', 'Nhà T');

-- Academic buildings C/D (Orange)
UPDATE buildings SET category = 'ACADEMIC', fill_color = '249,115,22,230'
WHERE name IN ('C1', 'Nhà Cựu sinh viên Bách khoa', 'HUST Digital Hub',
  'C2', 'C3', 'C4', 'C5', 'C6', 'C7', 'C8', 'C9', 'C10', 'C10b',
  'C3b', 'C4b', 'C15', 'D3', 'D4', 'D5', 'D6', 'D7', 'D8', 'D9',
  'D3-5', 'D2A', 'D2B', 'D6b', 'ITIMS', 'HiTech', 'Trung tâm Việt Đức');

-- Facilities / A area (Green)
UPDATE buildings SET category = 'FACILITY', fill_color = '34,197,94,230'
WHERE name IN ('Nhà B1', 'B1', 'Nhà ăn A1-5', 'A15',
  'Nhà khách Bách Khoa', 'Nhà A17', 'A17', 'Nhà E',
  'Trung tâm liên hợp thực hành công nghệ',
  'Ký túc xá A3', 'Ký túc xá A2', 'Nhà A',
  'Phòng thí nghiệm Kỹ thuật in', 'ADCBook', 'Highlands Coffee',
  'Nhà F', 'Khán đài', 'Nhà TC');

-- Sports (Deep Purple)
UPDATE buildings SET category = 'SPORTS', fill_color = '168,85,247,230'
WHERE name = 'Nhà thi đấu Bách Khoa';

-- Parking lots (Slate Gray)
UPDATE buildings SET category = 'PARKING', fill_color = '100,116,139,230'
WHERE name IN ('Nhà xe cán bộ', 'Điểm trông giữ xe D3 - D5',
  'Điểm trông giữ xe D4-6-8', 'Điểm trông giữ xe D7 - D9',
  'Nhà xe B1', 'Nhà xe B6', 'Nhà xe B13', 'Nhà xe C5');

-- Plazas / Open squares (Invisible)
UPDATE buildings SET category = 'PLAZA', fill_color = '0,0,0,0', is_label_visible = FALSE
WHERE name IN ('Quảng trường C1', 'Quảng trường Tạ Quang Bửu', 'Quảng trường Ngã 5');

-- "Công trình phụ" and "college5_" buildings: keep on map but hide labels
UPDATE buildings SET is_label_visible = FALSE
WHERE name LIKE 'Công trình phụ%' OR name LIKE 'college5_%';

-- Assign "Công trình phụ" buildings to their correct zone color
-- These were in specific color groups in the original hardcoded frontend

-- Dormitory zone (Blue) Công trình phụ
UPDATE buildings SET category = 'DORMITORY', fill_color = '74,144,226,230'
WHERE name IN (
  'Công trình phụ 1272194484', 'Công trình phụ 860063996',
  'Công trình phụ 1274073657', 'Công trình phụ 1272194483',
  'Công trình phụ 860064000', 'Công trình phụ 860063999',
  'Công trình phụ 1445974100', 'Công trình phụ 1236403194',
  'Công trình phụ 1236403193', 'Công trình phụ 1236403196',
  'Công trình phụ 1236403189');

-- Academic zone (Orange) Công trình phụ
UPDATE buildings SET category = 'ACADEMIC', fill_color = '249,115,22,230'
WHERE name IN (
  'Công trình phụ 821287771', 'Công trình phụ 821304200',
  'Công trình phụ 821292439', 'Công trình phụ 821287772',
  'Công trình phụ 860063992', 'Công trình phụ 1207215774',
  'Công trình phụ 1228585405', 'Công trình phụ 1267322211',
  'Công trình phụ 1294882996', 'Công trình phụ 1207215776',
  'Công trình phụ 1207215777', 'Công trình phụ 821287773');

-- Facility zone (Green) Công trình phụ
UPDATE buildings SET category = 'FACILITY', fill_color = '34,197,94,230'
WHERE name IN (
  'Công trình phụ 824889322', 'Công trình phụ 1458203539',
  'Công trình phụ 1458203541', 'Công trình phụ 1458203542',
  'Công trình phụ 1458203537', 'Công trình phụ 1458203536',
  'Công trình phụ 1458203538');

-- college5_ buildings: Facility zone (Green) - keep label hidden
UPDATE buildings SET category = 'FACILITY', fill_color = '34,197,94,230'
WHERE name LIKE 'college5_%';

-- Label zoom: short building codes (<=6 chars or starts with A-Z + digit) show at zoom 15
-- Long names only show at zoom 17.5
UPDATE buildings SET label_min_zoom = 15.0
WHERE LENGTH(name) <= 6 OR name ~ '^[A-Z][0-9]';

UPDATE buildings SET label_min_zoom = 17.5
WHERE LENGTH(name) > 6 AND NOT (name ~ '^[A-Z][0-9]');
