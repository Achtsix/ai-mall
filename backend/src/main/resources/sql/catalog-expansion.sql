-- 商品目录扩充：真实商品摄影资源 + 分类、品牌、评价
-- 可重复执行；已有 ID 会被忽略。
SET NAMES utf8mb4;

-- Restore UTF-8 names after import regardless of shell code page.
UPDATE product SET name=CONVERT(0x4479736F6E205631322044657465637420536C696D20E697A0E7BABFE590B8E5B098E599A8 USING utf8mb4), subtitle=CONVERT(0xE8BDBBE9878FE69CBAE8BAAEFBC8CE6BF80E58589E698BEE5B098EFBC8CE98082E59088E585A8E5B18BE697A5E5B8B8E6B885E6B49C USING utf8mb4) WHERE id=19;
UPDATE product SET name=CONVERT(0x5068696C697073204143333033332F303020E7A9BAE6B094E58780E58C96E599A8 USING utf8mb4), subtitle=CONVERT(0xE98082E59088E5AEA2E58E85E58DA7E5AEA4EFBC8CE5AE9EE697B6E698BEE7A4BAE7A9BAE6B094E8B4A8E9878F USING utf8mb4) WHERE id=20;

-- Additional realistic home appliances (idempotent inserts).
INSERT IGNORE INTO product (id, category_id, brand_id, name, subtitle, main_image, price, original_price, stock, sales, status, detail_html, params_json) VALUES
(19, 20, 6, 'Dyson V12 Detect Slim 无线吸尘器', '轻量机身，激光显尘，适合全屋日常清洁', '/uploads/products/homeease-vacuum.jpg', 3499.00, 3999.00, 32, 86, 1, '<p>轻量无线设计，配备激光显尘吸头和多种清洁刷头，适合地板、地毯与宠物毛发清洁。</p>', '{"吸力":"150AW","续航":"60分钟","功能":"激光显尘/多刷头"}'),
(20, 19, 6, 'Philips AC3033/00 空气净化器', '适合客厅卧室，实时显示空气质量', '/uploads/products/homeease-air.jpg', 1899.00, 2199.00, 28, 74, 1, '<p>搭载 HEPA 滤网，可过滤空气中的细颗粒物，支持空气质量显示和定时运行。</p>', '{"适用面积":"约48㎡","滤网":"HEPA","噪音":"15-56dB"}');

INSERT IGNORE INTO product_image (product_id, url, sort) VALUES
(19, '/uploads/products/homeease-vacuum-2.jpg', 1), (20, '/uploads/products/homeease-air.jpg', 1);

INSERT IGNORE INTO knowledge_doc (product_id, title, type, content) VALUES
(19, 'Dyson V12 Detect Slim 使用说明', 'PRODUCT', '配备激光显尘吸头和多种刷头，最长续航约 60 分钟，适合地板、地毯和宠物毛发清洁。'),
(20, 'Philips AC3033/00 使用说明', 'PRODUCT', '适用约 48 平方米空间，搭载 HEPA 滤网，支持空气质量实时显示和定时运行。');

-- Realistic retail names for the core catalog.
UPDATE product SET name='Apple iPhone 17 Pro', subtitle='A19 Pro 芯片，专业影像系统，全天候性能' WHERE id=1;
UPDATE product SET name='Sony WH-1000XM6 降噪耳机', subtitle='旗舰主动降噪，Hi-Res 高解析音质' WHERE id=2;
UPDATE product SET name='Apple MacBook Air 15 英寸 M4', subtitle='M4 芯片，轻薄长续航，适合移动办公' WHERE id=3;
UPDATE product SET name='Google Pixel 10 Pro', subtitle='Pro 级影像与 Gemini AI 助手' WHERE id=17;
UPDATE product SET name='Apple AirPods Pro 3', subtitle='自适应主动降噪，通勤佩戴更舒适' WHERE id=18;

INSERT IGNORE INTO category (id, parent_id, name, sort) VALUES
(7, 0, '摄影摄像', 4), (8, 0, '智能穿戴', 5), (9, 0, '生活电器', 6),
(10, 0, '运动户外', 7), (11, 0, '美妆个护', 8),
(12, 7, '相机', 1), (13, 2, '平板电脑', 2), (14, 8, '智能手表', 1),
(15, 2, '显示器', 3), (16, 2, '键盘鼠标', 4), (17, 1, '音箱', 3),
(18, 9, '咖啡机', 1), (19, 9, '空气净化器', 2), (20, 9, '吸尘器', 3),
(21, 10, '双肩包', 1), (22, 10, '运动鞋', 2), (23, 11, '护肤套装', 1);

INSERT IGNORE INTO brand (id, name, logo, description) VALUES
(4, 'PixelCraft', NULL, '影像设备与创作工具品牌'),
(5, 'NovaGear', NULL, '智能穿戴与数码配件品牌'),
(6, 'HomeEase', NULL, '品质生活电器品牌'),
(7, 'TrailMate', NULL, '城市运动户外品牌'),
(8, 'PureSkin', NULL, '温和护肤品牌');

UPDATE product SET main_image = '/uploads/products/deeptech-x1.jpg' WHERE id = 1;
UPDATE product SET main_image = '/uploads/products/soundpro-pro.jpg' WHERE id = 2;
UPDATE product SET main_image = '/uploads/products/cloudbook-air.jpg' WHERE id = 3;
UPDATE product_image SET url = '/uploads/products/deeptech-x1-2.jpg' WHERE product_id = 1 AND sort = 1;
UPDATE product_image SET url = '/uploads/products/deeptech-x1-3.jpg' WHERE product_id = 1 AND sort = 2;
UPDATE product_image SET url = '/uploads/products/soundpro-pro-2.jpg' WHERE product_id = 2 AND sort = 1;
UPDATE product_image SET url = '/uploads/products/soundpro-pro-3.jpg' WHERE product_id = 2 AND sort = 2;
UPDATE product_image SET url = '/uploads/products/cloudbook-air-2.jpg' WHERE product_id = 3 AND sort = 1;
UPDATE product_image SET url = '/uploads/products/cloudbook-air-3.jpg' WHERE product_id = 3 AND sort = 2;

INSERT IGNORE INTO product (id, category_id, brand_id, name, subtitle, main_image, price, original_price, stock, sales, status, detail_html, params_json) VALUES
(4, 12, 4, 'PixelCraft X100 微单相机', 'APS-C 画幅，适合旅行和日常创作', '/uploads/products/pixelcraft-camera.jpg', 4299.00, 4799.00, 36, 188, 1, '<p>轻量化机身搭配高解析传感器，支持 4K 视频和快速自动对焦，适合旅行、人像与短视频创作。</p>', '{"传感器":"APS-C","视频":"4K 30fps","重量":"398g"}'),
(5, 13, 5, 'NovaTab 11 Pro 平板电脑', '2.8K 高刷屏，学习办公娱乐三合一', '/uploads/products/novatab.jpg', 2399.00, 2699.00, 68, 426, 1, '<p>11 英寸 120Hz 屏幕，支持手写笔和多任务分屏，适合阅读、网课、轻办公和影音娱乐。</p>', '{"屏幕":"11英寸 120Hz","内存":"8GB+256GB","电池":"8600mAh"}'),
(6, 14, 5, 'NovaWatch Active 智能手表', '全天候健康监测，14 天长续航', '/uploads/products/novawatch.jpg', 899.00, 1099.00, 92, 612, 1, '<p>支持心率、血氧、睡眠和运动监测，具备 5ATM 防水能力，日常通勤和运动都适用。</p>', '{"续航":"14天","防水":"5ATM","定位":"双频GPS"}'),
(7, 15, 5, 'NovaView 27 英寸 4K 显示器', '色彩准确，适合设计和居家办公', '/uploads/products/novaview.jpg', 1899.00, 2199.00, 41, 237, 1, '<p>27 英寸 4K IPS 面板，支持 100% sRGB 色域和 USB-C 一线连接。</p>', '{"分辨率":"3840×2160","刷新率":"60Hz","接口":"USB-C/HDMI"}'),
(8, 16, 5, 'NovaType 机械键盘套装', '静音轴体，办公与游戏兼顾', '/uploads/products/novatype.jpg', 399.00, 499.00, 145, 932, 1, '<p>全尺寸无线机械键盘，搭配人体工学鼠标，支持三模连接和长效续航。</p>', '{"轴体":"静音红轴","连接":"蓝牙/2.4G/有线","布局":"108键"}'),
(9, 17, 3, 'SoundPro Home 360 智能音箱', '360 度环绕音效，支持多房间播放', '/uploads/products/soundpro-speaker.jpg', 599.00, 699.00, 75, 344, 1, '<p>紧凑机身提供饱满低音，支持语音控制、蓝牙和 Wi-Fi 多房间播放。</p>', '{"功率":"30W","连接":"Wi-Fi 6/蓝牙","控制":"语音控制"}'),
(10, 18, 6, 'HomeEase Barista 胶囊咖啡机', '15 bar 萃取压力，三分钟享用咖啡', '/uploads/products/homeease-coffee.jpg', 799.00, 999.00, 58, 288, 1, '<p>一键式胶囊咖啡机，支持浓缩、美式和热水模式，适合家庭和办公室。</p>', '{"压力":"15 bar","水箱":"0.8L","加热":"25秒"}'),
(11, 19, 6, 'HomeEase AirPure 空气净化器', '适合卧室的静音净化方案', '/uploads/products/homeease-air.jpg', 1299.00, 1599.00, 27, 156, 1, '<p>高效滤网可过滤过敏原和细颗粒物，支持空气质量显示和智能定时。</p>', '{"适用面积":"35㎡","噪音":"24dB","滤网":"HEPA H13"}'),
(12, 20, 6, 'HomeEase Clean 无线吸尘器', '轻量机身，地板地毯一机清洁', '/uploads/products/homeease-vacuum.jpg', 999.00, 1299.00, 49, 402, 1, '<p>无线设计搭配多刷头，支持强劲吸力和墙面收纳，适合日常全屋清洁。</p>', '{"续航":"45分钟","吸力":"22kPa","重量":"1.6kg"}'),
(13, 21, 7, 'TrailMate Urban 轻量双肩包', '通勤防泼水，15.6 英寸电脑隔层', '/uploads/products/trailmate-backpack.jpg', 269.00, 329.00, 120, 756, 1, '<p>轻量防泼水面料，拥有独立电脑仓和多功能收纳空间，适合通勤、出差和短途旅行。</p>', '{"容量":"20L","电脑仓":"15.6英寸","面料":"防泼水尼龙"}'),
(14, 22, 7, 'TrailMate Run Pro 跑鞋', '缓震回弹，适合日常慢跑', '/uploads/products/trailmate-shoes.jpg', 459.00, 599.00, 84, 518, 1, '<p>轻量网面鞋身搭配缓震中底，提供稳定支撑和舒适脚感，适合 5-10 公里慢跑。</p>', '{"鞋面":"透气网面","中底":"高回弹泡棉","适用":"日常慢跑"}'),
(15, 23, 8, 'PureSkin Hydra 水润护肤套装', '洁面、精华、面霜三步基础护理', '/uploads/products/pureskin-set.jpg', 329.00, 399.00, 73, 665, 1, '<p>温和洁面配合保湿精华和面霜，适合干燥及混合肤质的日常基础护理。</p>', '{"套装":"洁面/精华/面霜","容量":"120ml+30ml+50g","肤质":"干燥/混合"}'),
(16, 6, 2, 'CloudBook Studio 16 创作本', '大屏高性能，适合设计剪辑', '/uploads/products/cloudbook-studio.jpg', 7499.00, 7999.00, 22, 97, 1, '<p>16 英寸高分辨率屏幕，搭载高性能处理器和独立显卡，适合设计、剪辑与开发。</p>', '{"屏幕":"16英寸 2.5K","内存":"32GB+1TB","显卡":"独立显卡"}'),
(17, 4, 1, 'DeepTech X1 Pro 智能手机', '旗舰影像系统，全天候性能表现', '/uploads/products/deeptech-pro.jpg', 4999.00, 5599.00, 44, 263, 1, '<p>旗舰芯片和多摄像头影像系统，支持高刷屏、快充和 IP68 防尘防水。</p>', '{"屏幕":"6.7英寸 120Hz","内存":"16GB+512GB","快充":"100W"}'),
(18, 5, 3, 'SoundPro Mini 真无线耳机', '小巧舒适，通勤降噪好选择', '/uploads/products/soundpro-mini.jpg', 299.00, 399.00, 180, 1140, 1, '<p>轻量耳机支持主动降噪和通透模式，单次续航 8 小时，适合通勤与运动。</p>', '{"降噪":"主动降噪","续航":"8小时","防水":"IPX4"}');

INSERT IGNORE INTO product_image (id, product_id, url, sort) VALUES
(7, 4, '/uploads/products/pixelcraft-camera-2.jpg', 1), (8, 5, '/uploads/products/novatab-2.jpg', 1), (9, 6, '/uploads/products/novawatch-2.jpg', 1),
(10, 7, '/uploads/products/novaview-2.jpg', 1), (11, 8, '/uploads/products/novatype-2.jpg', 1), (12, 9, '/uploads/products/soundpro-speaker-2.jpg', 1),
(13, 10, '/uploads/products/homeease-coffee-2.jpg', 1), (14, 11, '/uploads/products/homeease-air.jpg', 1), (15, 12, '/uploads/products/homeease-vacuum-2.jpg', 1),
(16, 13, '/uploads/products/trailmate-backpack-2.jpg', 1), (17, 14, '/uploads/products/trailmate-shoes-2.jpg', 1), (18, 15, '/uploads/products/pureskin-set-2.jpg', 1),
(19, 16, '/uploads/products/cloudbook-studio-2.jpg', 1), (20, 17, '/uploads/products/deeptech-pro-2.jpg', 1), (21, 18, '/uploads/products/soundpro-mini-2.jpg', 1);

INSERT IGNORE INTO review (id, user_id, product_id, rating, content, reply) VALUES
(1, 2, 1, 5, '拍照效果很惊艳，电池也够用，系统运行很流畅。', '感谢支持，祝您使用愉快！'),
(2, 2, 1, 4, '屏幕清晰，手感不错，配送速度也很快。', NULL),
(3, 2, 2, 5, '降噪效果比预期好，通勤地铁上听歌很舒服。', '感谢您的真实体验分享！'),
(4, 2, 2, 4, '佩戴舒适，续航够用，连接速度很快。', NULL),
(5, 2, 3, 5, '重量轻，办公携带方便，续航能撑一天。', '感谢认可！'),
(6, 2, 4, 5, '旅行拍照很方便，自动对焦速度快，直出颜色好看。', '感谢您的好评，期待再次光临！'),
(7, 2, 4, 4, '机身很轻，套机镜头适合日常使用。', NULL),
(8, 2, 5, 5, '上网课和记笔记都很顺滑，屏幕看起来很舒服。', '感谢支持！'),
(9, 2, 6, 4, '运动数据记录准确，续航确实不错。', NULL),
(10, 2, 7, 5, '4K 画面细腻，接电脑办公非常方便。', '感谢您的使用反馈！'),
(11, 2, 8, 5, '键盘声音很小，办公室使用不会打扰同事。', NULL),
(12, 2, 9, 4, '房间里放着很有氛围感，音质比想象中好。', NULL),
(13, 2, 10, 5, '咖啡机操作简单，早上几分钟就能喝到咖啡。', '感谢支持，愿每个早晨都有好咖啡！'),
(14, 2, 11, 4, '卧室使用声音很小，空气质量提示很直观。', NULL),
(15, 2, 12, 5, '吸力够强，清理猫毛很方便，续航也满意。', '感谢您的认可！'),
(16, 2, 13, 5, '电脑仓有缓冲，容量适合通勤，防泼水很实用。', NULL),
(17, 2, 14, 4, '鞋底回弹不错，跑步一小时脚也不会累。', NULL),
(18, 2, 15, 5, '成分温和不刺激，换季使用皮肤状态稳定很多。', '感谢分享使用感受！'),
(19, 2, 16, 5, '剪视频非常流畅，屏幕大看时间线很舒服。', NULL),
(20, 2, 17, 5, '影像和续航都很强，快充速度特别快。', '感谢您的支持！'),
(21, 2, 18, 4, '小巧好戴，通勤听播客很方便。', NULL);

INSERT INTO knowledge_doc (product_id, title, type, content)
SELECT 4, 'PixelCraft X100 使用与选购建议', 'PRODUCT', '适合旅行、人像和短视频创作，APS-C 画幅，支持 4K 视频，机身重量约 398g。' WHERE NOT EXISTS (SELECT 1 FROM knowledge_doc WHERE product_id = 4 AND title = 'PixelCraft X100 使用与选购建议');
INSERT INTO knowledge_doc (product_id, title, type, content)
SELECT 5, 'NovaTab 11 Pro 使用说明', 'PRODUCT', '11 英寸 120Hz 屏幕，支持手写笔和分屏，适合网课、阅读、记笔记和轻办公。' WHERE NOT EXISTS (SELECT 1 FROM knowledge_doc WHERE product_id = 5 AND title = 'NovaTab 11 Pro 使用说明');
INSERT INTO knowledge_doc (product_id, title, type, content)
SELECT 6, 'NovaWatch Active 健康功能', 'PRODUCT', '支持心率、血氧、睡眠和运动监测，5ATM 防水，续航约 14 天。' WHERE NOT EXISTS (SELECT 1 FROM knowledge_doc WHERE product_id = 6 AND title = 'NovaWatch Active 健康功能');
INSERT INTO knowledge_doc (product_id, title, type, content)
SELECT 7, 'NovaView 4K 显示器参数', 'PARAMETER', '27 英寸 4K IPS 面板，100% sRGB 色域，支持 USB-C 一线连接，适合设计和办公。' WHERE NOT EXISTS (SELECT 1 FROM knowledge_doc WHERE product_id = 7 AND title = 'NovaView 4K 显示器参数');
INSERT INTO knowledge_doc (product_id, title, type, content)
SELECT 8, 'NovaType 机械键盘说明', 'PRODUCT', '静音红轴、三模连接、108 键布局，适合办公室和家庭桌面使用。' WHERE NOT EXISTS (SELECT 1 FROM knowledge_doc WHERE product_id = 8 AND title = 'NovaType 机械键盘说明');
INSERT INTO knowledge_doc (product_id, title, type, content)
SELECT 9, 'SoundPro Home 360 音箱说明', 'PRODUCT', '支持 Wi-Fi 6、蓝牙和语音控制，30W 功率，适合客厅和卧室多房间播放。' WHERE NOT EXISTS (SELECT 1 FROM knowledge_doc WHERE product_id = 9 AND title = 'SoundPro Home 360 音箱说明');
INSERT INTO knowledge_doc (product_id, title, type, content)
SELECT 10, 'HomeEase Barista 咖啡机使用说明', 'FAQ', '支持浓缩、美式和热水模式，水箱容量 0.8L，约 25 秒完成加热。' WHERE NOT EXISTS (SELECT 1 FROM knowledge_doc WHERE product_id = 10 AND title = 'HomeEase Barista 咖啡机使用说明');
INSERT INTO knowledge_doc (product_id, title, type, content)
SELECT 11, 'HomeEase AirPure 净化器说明', 'PRODUCT', '适用面积约 35 平方米，最低噪音 24dB，配备 HEPA H13 滤网。' WHERE NOT EXISTS (SELECT 1 FROM knowledge_doc WHERE product_id = 11 AND title = 'HomeEase AirPure 净化器说明');
INSERT INTO knowledge_doc (product_id, title, type, content)
SELECT 12, 'HomeEase Clean 吸尘器说明', 'PRODUCT', '22kPa 吸力、45 分钟续航，配备多刷头，适合地板、地毯和宠物毛发清理。' WHERE NOT EXISTS (SELECT 1 FROM knowledge_doc WHERE product_id = 12 AND title = 'HomeEase Clean 吸尘器说明');
INSERT INTO knowledge_doc (product_id, title, type, content)
SELECT 13, 'TrailMate Urban 双肩包说明', 'PRODUCT', '20L 容量，配备 15.6 英寸电脑隔层，防泼水尼龙面料，适合通勤和短途旅行。' WHERE NOT EXISTS (SELECT 1 FROM knowledge_doc WHERE product_id = 13 AND title = 'TrailMate Urban 双肩包说明');
INSERT INTO knowledge_doc (product_id, title, type, content)
SELECT 14, 'TrailMate Run Pro 跑鞋说明', 'PRODUCT', '透气网面和高回弹泡棉中底，适合日常 5 到 10 公里慢跑。' WHERE NOT EXISTS (SELECT 1 FROM knowledge_doc WHERE product_id = 14 AND title = 'TrailMate Run Pro 跑鞋说明');
INSERT INTO knowledge_doc (product_id, title, type, content)
SELECT 15, 'PureSkin Hydra 护肤套装说明', 'PRODUCT', '包含洁面、保湿精华和面霜，适合干燥及混合肤质日常基础护理。' WHERE NOT EXISTS (SELECT 1 FROM knowledge_doc WHERE product_id = 15 AND title = 'PureSkin Hydra 护肤套装说明');
INSERT INTO knowledge_doc (product_id, title, type, content)
SELECT 16, 'CloudBook Studio 16 创作本参数', 'PARAMETER', '16 英寸 2.5K 屏幕，32GB 内存和 1TB 存储，配备独立显卡，适合设计剪辑。' WHERE NOT EXISTS (SELECT 1 FROM knowledge_doc WHERE product_id = 16 AND title = 'CloudBook Studio 16 创作本参数');
INSERT INTO knowledge_doc (product_id, title, type, content)
SELECT 17, 'DeepTech X1 Pro 使用说明', 'PRODUCT', '6.7 英寸 120Hz 屏幕，16GB+512GB，100W 快充，支持 IP68 防尘防水。' WHERE NOT EXISTS (SELECT 1 FROM knowledge_doc WHERE product_id = 17 AND title = 'DeepTech X1 Pro 使用说明');
INSERT INTO knowledge_doc (product_id, title, type, content)
SELECT 18, 'SoundPro Mini 耳机说明', 'FAQ', '支持主动降噪和通透模式，单次续航约 8 小时，具备 IPX4 防水能力。' WHERE NOT EXISTS (SELECT 1 FROM knowledge_doc WHERE product_id = 18 AND title = 'SoundPro Mini 耳机说明');
