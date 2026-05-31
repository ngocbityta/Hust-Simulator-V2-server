-- V10: Seed OSM Ways
DELETE FROM campus_ways;

INSERT INTO campus_ways (name, way_type, coordinates, distance_meters, is_oneway)
VALUES ('Phố Kim Hoa', 'ROAD', '[[105.8378157, 21.0102091], [105.8378901, 21.0102701], [105.83791, 21.0102878], [105.8379935, 21.0103435], [105.8380761, 21.0103947], [105.8381589, 21.0104092], [105.8382689, 21.0104292], [105.8384003, 21.0103966], [105.838631, 21.0101754], [105.8388212, 21.009993], [105.8388527, 21.0099627], [105.8389282, 21.0098871], [105.8393065, 21.0095079], [105.8395317, 21.0092818], [105.8399306, 21.0088826], [105.8400288, 21.0087842], [105.8402641, 21.0085485], [105.8403262, 21.0084862], [105.8406851, 21.0081682], [105.8407241, 21.0081325], [105.8407399, 21.0080867], [105.8407335, 21.0080524], [105.8407057, 21.0080206], [105.8406465, 21.0079677]]', 439.6094490981895, false);
INSERT INTO campus_ways (name, way_type, coordinates, distance_meters, is_oneway)
VALUES ('Phố Lê Thanh Nghị', 'ROAD', '[[105.8507357, 21.0032328], [105.8506216, 21.0032517], [105.8504092, 21.0032858], [105.8502549, 21.0033135], [105.850174, 21.0033279], [105.8501297, 21.0033375], [105.8500606, 21.0033484], [105.850037, 21.0033521], [105.8499587, 21.0033606], [105.8497361, 21.0033703], [105.8489109, 21.0033905], [105.8482614, 21.0034039], [105.8482477, 21.0034029], [105.8481986, 21.0033993], [105.8481551, 21.0033902], [105.8480272, 21.0032947], [105.8479807, 21.0032553]]', 292.87403026723854, false);
INSERT INTO campus_ways (name, way_type, coordinates, distance_meters, is_oneway)
VALUES ('Phố Đào Duy Anh', 'ROAD', '[[105.837589, 21.009484], [105.8376464, 21.0094117], [105.838343, 21.0086548], [105.8386602, 21.0083173], [105.8388719, 21.0080943], [105.8389618, 21.0080247], [105.8391407, 21.0079834], [105.8394448, 21.0079281], [105.8396712, 21.0078958], [105.8400025, 21.0078489], [105.8402267, 21.0078235]]', 349.43911616571137, false);
INSERT INTO campus_ways (name, way_type, coordinates, distance_meters, is_oneway)
VALUES ('Đường nội bộ', 'ROAD', '[[105.844576, 21.0040572], [105.8445872, 21.0048393], [105.8445875, 21.0048672], [105.8445896, 21.0050377]]', 109.03580627993823, false);
INSERT INTO campus_ways (name, way_type, coordinates, distance_meters, is_oneway)
VALUES ('Ngõ 40 Phố Tạ Quang Bửu', 'ROAD', '[[105.8454342, 21.0026543], [105.8458635, 21.0030043], [105.8460271, 21.0031376], [105.8464015, 21.0034428], [105.8467001, 21.0036898], [105.8468985, 21.0038539], [105.8470441, 21.0039745], [105.8471585, 21.0040692], [105.8472042, 21.0041071]]', 244.65717675126348, false);
INSERT INTO campus_ways (name, way_type, coordinates, distance_meters, is_oneway)
VALUES ('Đường nội bộ', 'ROAD', '[[105.8477737, 21.0034643], [105.8477512, 21.0034431], [105.8477294, 21.0034229], [105.8472952, 21.0030473], [105.8469302, 21.0027316], [105.8468995, 21.0027052]]', 123.94007720475442, false);
INSERT INTO campus_ways (name, way_type, coordinates, distance_meters, is_oneway)
VALUES ('Ngõ 15 Phố Tạ Quang Bửu', 'ROAD', '[[105.8472066, 21.0045017], [105.8472739, 21.0045021], [105.8478142, 21.0045043], [105.8478491, 21.0051368], [105.8479254, 21.0054993]]', 174.57635599750117, false);
INSERT INTO campus_ways (name, way_type, coordinates, distance_meters, is_oneway)
VALUES ('Đường Giải Phóng', 'ROAD', '[[105.8412431, 21.0041897], [105.841233, 21.0034052], [105.8412322, 21.0033436], [105.8412283, 21.0030394], [105.8412244, 21.0019229], [105.8412279, 21.0017416]]', 272.2294603289903, false);
INSERT INTO campus_ways (name, way_type, coordinates, distance_meters, is_oneway)
VALUES ('Đường Giải Phóng', 'ROAD', '[[105.8414307, 21.0017139], [105.8414337, 21.0018029], [105.8414363, 21.0018743], [105.8414391, 21.002074]]', 40.05268055758236, false);
INSERT INTO campus_ways (name, way_type, coordinates, distance_meters, is_oneway)
VALUES ('Đường nội bộ', 'ROAD', '[[105.846961, 21.0049184], [105.846887, 21.0049572], [105.8467585, 21.0049949], [105.8466472, 21.0050115], [105.8465062, 21.0050233], [105.8463219, 21.0050257], [105.8458507, 21.005031], [105.8458009, 21.0050315], [105.8457944, 21.0050316], [105.8456502, 21.0050332]]', 138.05273355473523, false);
INSERT INTO campus_ways (name, way_type, coordinates, distance_meters, is_oneway)
VALUES ('Phố Trần Đại Nghĩa', 'ROAD', '[[105.8456502, 21.0050332], [105.8456409, 21.0048967], [105.8456383, 21.0048584], [105.8456327, 21.0047355], [105.8456261, 21.0046923], [105.8456056, 21.0046043], [105.8455641, 21.004436], [105.8454383, 21.004262], [105.8453124, 21.0041101], [105.8451651, 21.0039695], [105.8450193, 21.003853], [105.8449428, 21.0037839], [105.8448777, 21.0036982], [105.8448111, 21.0035933], [105.8447504, 21.0034945], [105.8447262, 21.0034402], [105.8446991, 21.0033784], [105.8446805, 21.0032602], [105.8446744, 21.0031261], [105.8446758, 21.0030107], [105.8446945, 21.0028504], [105.8447285, 21.0025977], [105.8447374, 21.0025379], [105.8447624, 21.002369], [105.8447894, 21.0022024], [105.8448228, 21.0021152], [105.8448651, 21.0020399], [105.844914, 21.0019552], [105.8449504, 21.0018846], [105.8449863, 21.0018106]]', 396.53094818303543, false);
INSERT INTO campus_ways (name, way_type, coordinates, distance_meters, is_oneway)
VALUES ('Hầm chui Kim Liên', 'ROAD', '[[105.8406249, 21.0077864], [105.8410732, 21.0076874], [105.8414639, 21.0076328], [105.8419739, 21.0076199]]', 141.7873580757518, false);
INSERT INTO campus_ways (name, way_type, coordinates, distance_meters, is_oneway)
VALUES ('Hầm chui Kim Liên', 'ROAD', '[[105.8449471, 21.0079814], [105.8445654, 21.0079373], [105.8444931, 21.0079254], [105.8437868, 21.0078369], [105.8431154, 21.007764], [105.842773, 21.0077337], [105.8424672, 21.0077169], [105.8419853, 21.0077279]]', 309.2211549138046, false);
INSERT INTO campus_ways (name, way_type, coordinates, distance_meters, is_oneway)
VALUES ('Hầm chui Kim Liên', 'ROAD', '[[105.8380906, 21.0096135], [105.8382126, 21.0095064], [105.838422, 21.0092903], [105.8387198, 21.008998], [105.8390501, 21.0086932], [105.8393536, 21.0084449], [105.8396293, 21.0082508], [105.8399745, 21.0080422], [105.8403439, 21.0078743], [105.8406249, 21.0077864]]', 336.6831011465812, false);
INSERT INTO campus_ways (name, way_type, coordinates, distance_meters, is_oneway)
VALUES ('Ngõ 106 Phố Lê Thanh Nghị', 'ROAD', '[[105.8462568, 21.0018287], [105.8461655, 21.0019061], [105.8460475, 21.0020266], [105.8458337, 21.0022451], [105.8457444, 21.0023366], [105.8456218, 21.0024621], [105.8455782, 21.0025068], [105.8454342, 21.0026543]]', 125.42829840471788, false);
INSERT INTO campus_ways (name, way_type, coordinates, distance_meters, is_oneway)
VALUES ('Đường nội bộ', 'ROAD', '[[105.8420709, 21.0035065], [105.842032, 21.0034818], [105.8416781, 21.0034954], [105.8415836, 21.0035488], [105.8415477, 21.0035512], [105.8414534, 21.0035472]]', 66.65416746240425, false);
INSERT INTO campus_ways (name, way_type, coordinates, distance_meters, is_oneway)
VALUES ('Đường nội bộ', 'FOOTPATH', '[[105.8411606, 21.0016749], [105.84161, 21.0016525], [105.8416215, 21.0020001]]', 85.3876604260861, false);
INSERT INTO campus_ways (name, way_type, coordinates, distance_meters, is_oneway)
VALUES ('Đường nội bộ', 'FOOTPATH', '[[105.8407052, 21.0076693], [105.8408993, 21.0075057], [105.8409983, 21.0074015], [105.841021, 21.0073795], [105.8411266, 21.0072746], [105.8411998, 21.0072019], [105.8413254, 21.007045]]', 94.9106400286725, false);
INSERT INTO campus_ways (name, way_type, coordinates, distance_meters, is_oneway)
VALUES ('Đường nội bộ', 'FOOTPATH', '[[105.8418746, 21.0078013], [105.841772, 21.0078648], [105.8416428, 21.0079835], [105.8415254, 21.008107], [105.8414641, 21.0082076], [105.8414181, 21.0082831]]', 72.48293217274188, false);
INSERT INTO campus_ways (name, way_type, coordinates, distance_meters, is_oneway)
VALUES ('Phố Xã Đàn', 'ROAD', '[[105.8380906, 21.0096135], [105.8381595, 21.0094658], [105.8386416, 21.0089445], [105.8390015, 21.0086192], [105.8392555, 21.0083914], [105.839558, 21.0081797], [105.8399115, 21.0079687], [105.8402267, 21.0078235]]', 302.339752674623, false);
INSERT INTO campus_ways (name, way_type, coordinates, distance_meters, is_oneway)
VALUES ('Đường nội bộ', 'FOOTPATH', '[[105.8412842, 21.0083362], [105.8412082, 21.0082543], [105.8411858, 21.0082384], [105.8410982, 21.0081681], [105.8410472, 21.0081272], [105.8410281, 21.0081146], [105.8409682, 21.0080638], [105.8408887, 21.0080118], [105.8407498, 21.0079405]]', 71.29942660726448, false);
INSERT INTO campus_ways (name, way_type, coordinates, distance_meters, is_oneway)
VALUES ('Đường nội bộ', 'FOOTPATH', '[[105.8415156, 21.0071533], [105.8415666, 21.007287], [105.8416248, 21.0073966], [105.8416549, 21.0074332], [105.8416988, 21.0074656], [105.8417609, 21.0074888], [105.8419506, 21.0075241]]', 67.34572105221991, false);
INSERT INTO campus_ways (name, way_type, coordinates, distance_meters, is_oneway)
VALUES ('Ngõ 1 Phố Tạ Quang Bửu', 'ROAD', '[[105.8482178, 21.0070991], [105.8481326, 21.0070995], [105.8474619, 21.0071102], [105.8474183, 21.0071109]]', 83.0026165054493, false);
INSERT INTO campus_ways (name, way_type, coordinates, distance_meters, is_oneway)
VALUES ('Hầm chui Kim Liên', 'ROAD', '[[105.8419853, 21.0077279], [105.8416697, 21.007735], [105.841418, 21.0077572], [105.8410324, 21.0078205], [105.8406625, 21.0078991]]', 139.03406553733976, false);
INSERT INTO campus_ways (name, way_type, coordinates, distance_meters, is_oneway)
VALUES ('Đường Lê Duẩn', 'ROAD', '[[105.8413355, 21.0087937], [105.8413068, 21.0087133], [105.8412983, 21.0086895], [105.8412802, 21.0086387], [105.8412842, 21.0083362], [105.8412844, 21.008256], [105.8412891, 21.0079486], [105.8412908, 21.0078874], [105.8412932, 21.0078348]]', 107.5692716761763, false);
INSERT INTO campus_ways (name, way_type, coordinates, distance_meters, is_oneway)
VALUES ('Đường Lê Duẩn', 'ROAD', '[[105.8414691, 21.0078117], [105.8414527, 21.0079601], [105.8414181, 21.0082831], [105.8414235, 21.0086347], [105.8413355, 21.0087937]]', 111.68453134057683, false);
INSERT INTO campus_ways (name, way_type, coordinates, distance_meters, is_oneway)
VALUES ('Phố Trần Đại Nghĩa', 'ROAD', '[[105.8437927, 21.0076335], [105.8438946, 21.0075286], [105.8440142, 21.0074152]]', 33.441741720329446, false);
INSERT INTO campus_ways (name, way_type, coordinates, distance_meters, is_oneway)
VALUES ('Phố Trần Đại Nghĩa', 'ROAD', '[[105.8440142, 21.0074152], [105.8440158, 21.0074576], [105.8440264, 21.0074919], [105.8440343, 21.0075158], [105.8440408, 21.0075355], [105.8440765, 21.0076077], [105.8441307, 21.0076739]]', 31.86770085565693, false);
INSERT INTO campus_ways (name, way_type, coordinates, distance_meters, is_oneway)
VALUES ('Hầm chui Kim Liên', 'ROAD', '[[105.8419739, 21.0076199], [105.8424585, 21.0076243], [105.8431055, 21.0076613], [105.8437087, 21.0077244], [105.8445408, 21.0078125], [105.8448073, 21.0077811]]', 295.41177849917614, false);
INSERT INTO campus_ways (name, way_type, coordinates, distance_meters, is_oneway)
VALUES ('Đường nội bộ', 'ROAD', '[[105.8426544, 21.0072266], [105.8426518, 21.0073086], [105.8426485, 21.0074319], [105.842647, 21.007473], [105.8426464, 21.0075341]]', 34.20364936005914, false);
INSERT INTO campus_ways (name, way_type, coordinates, distance_meters, is_oneway)
VALUES ('Đường nội bộ', 'ROAD', '[[105.8423267, 21.0038042], [105.8423308, 21.0041431], [105.8423359, 21.0045606], [105.8423412, 21.0050122], [105.8423417, 21.0050536]]', 138.93566967775664, false);
INSERT INTO campus_ways (name, way_type, coordinates, distance_meters, is_oneway)
VALUES ('Ngõ 33 Phố Lê Thanh Nghị', 'ROAD', '[[105.8476528, 21.0011863], [105.8474471, 21.0014027], [105.8473628, 21.0014913], [105.8472178, 21.0016439], [105.8470669, 21.0018035], [105.8468889, 21.0019918], [105.846778, 21.0021091], [105.8467549, 21.0021344]]', 140.72103703679647, false);
INSERT INTO campus_ways (name, way_type, coordinates, distance_meters, is_oneway)
VALUES ('Ngõ 75 Giải Phóng', 'ROAD', '[[105.8412283, 21.0030394], [105.8414459, 21.0030349]]', 22.594008771056945, false);
INSERT INTO campus_ways (name, way_type, coordinates, distance_meters, is_oneway)
VALUES ('Phố Xã Đàn', 'ROAD', '[[105.8412932, 21.0078348], [105.8412073, 21.0078482], [105.8411086, 21.0078637], [105.8410951, 21.0078682], [105.8407498, 21.0079405], [105.8406822, 21.0079547], [105.8406465, 21.0079677], [105.8403539, 21.0080749], [105.8400604, 21.008206], [105.8397249, 21.0084037], [105.8394551, 21.0085968], [105.8391931, 21.008832], [105.8388546, 21.0091303], [105.8385642, 21.0094227], [105.8383468, 21.0096418], [105.8381811, 21.0097637]]', 397.1951422221644, false);
INSERT INTO campus_ways (name, way_type, coordinates, distance_meters, is_oneway)
VALUES ('Ngõ 2 Phố Phương Mai', 'ROAD', '[[105.8401652, 21.0053382], [105.8403679, 21.005342], [105.8405532, 21.0053417], [105.8407724, 21.0053094], [105.8407673, 21.0051497], [105.8407661, 21.0049533], [105.8407627, 21.0048665], [105.8407493, 21.004667], [105.8407068, 21.0046587], [105.8403834, 21.0046496], [105.8401301, 21.0046352]]', 199.24292339919512, false);
INSERT INTO campus_ways (name, way_type, coordinates, distance_meters, is_oneway)
VALUES ('Ngõ 30 Phố Tạ Quang Bửu', 'ROAD', '[[105.8472555, 21.006807], [105.8469289, 21.0067437], [105.8467718, 21.0066914], [105.8466293, 21.0066835], [105.8462013, 21.0066603], [105.8458521, 21.0066648], [105.8457685, 21.0066653], [105.8457668, 21.0065862], [105.8457659, 21.0065576], [105.8457577, 21.006309], [105.845651, 21.0061208]]', 219.50329211962477, false);
INSERT INTO campus_ways (name, way_type, coordinates, distance_meters, is_oneway)
VALUES ('Phố Tạ Quang Bửu', 'ROAD', '[[105.8474218, 21.0081248], [105.8474281, 21.008046], [105.8474326, 21.0078687], [105.8474403, 21.0075617], [105.8474403, 21.0075401], [105.8474401, 21.0073187], [105.8474419, 21.0071676], [105.8474183, 21.0071109], [105.8472555, 21.006807], [105.8472624, 21.0064838], [105.8472656, 21.0063322], [105.8472658, 21.0059902], [105.847253, 21.0058908], [105.8471794, 21.0054996], [105.8471433, 21.0053768], [105.8470921, 21.0052541], [105.8469836, 21.0050086], [105.8469697, 21.0049623], [105.846961, 21.0049184], [105.8472066, 21.0045017], [105.8472063, 21.0044498], [105.8472042, 21.0041071], [105.847204, 21.0040754], [105.8472308, 21.004006], [105.8477737, 21.0034643], [105.8478268, 21.0034126], [105.8479353, 21.003302], [105.8479807, 21.0032553]]', 588.4311437378537, false);
INSERT INTO campus_ways (name, way_type, coordinates, distance_meters, is_oneway)
VALUES ('Ngách 40/2 Tạ Quang Bửu', 'ROAD', '[[105.8468985, 21.0038539], [105.8468679, 21.0038863], [105.8467853, 21.0039738], [105.8466858, 21.0040793], [105.8465471, 21.0042263], [105.8464373, 21.0043427], [105.8462218, 21.0045711], [105.8457779, 21.004206]]', 167.68733683968168, false);
INSERT INTO campus_ways (name, way_type, coordinates, distance_meters, is_oneway)
VALUES ('Ngõ 104 Phố Lê Thanh Nghị', 'ROAD', '[[105.8463278, 21.0019013], [105.8462885, 21.0019585], [105.846194, 21.0020959], [105.8463034, 21.0021867], [105.8458537, 21.0026564], [105.845968, 21.0027562], [105.8459339, 21.0028016], [105.8459923, 21.0028538], [105.8458849, 21.0029793], [105.8458635, 21.0030043]]', 163.18350077442733, false);
INSERT INTO campus_ways (name, way_type, coordinates, distance_meters, is_oneway)
VALUES ('Ngõ 40 Phố Trần Đại Nghĩa', 'ROAD', '[[105.8454342, 21.0026543], [105.8450227, 21.0023198]]', 56.64076258681295, false);
INSERT INTO campus_ways (name, way_type, coordinates, distance_meters, is_oneway)
VALUES ('Hẻm 40/2/1 Phố Tạ Quang Bửu', 'ROAD', '[[105.8467853, 21.0039738], [105.8462826, 21.0035707]]', 68.79104299456537, false);
INSERT INTO campus_ways (name, way_type, coordinates, distance_meters, is_oneway)
VALUES ('Ngách 40/30 phố Tạ Quang Bửu', 'ROAD', '[[105.8464015, 21.0034428], [105.8463714, 21.0034751], [105.8462826, 21.0035707], [105.846195, 21.0036649], [105.8461613, 21.0037012], [105.8460578, 21.0038125], [105.8460391, 21.0038326], [105.8459263, 21.0039541]]', 75.27101644802195, false);
INSERT INTO campus_ways (name, way_type, coordinates, distance_meters, is_oneway)
VALUES ('Ngõ 75 Đường Giải Phóng', 'ROAD', '[[105.8414459, 21.0030349], [105.8415309, 21.0030279], [105.8417197, 21.0029826], [105.8422055, 21.0029092], [105.8426162, 21.0027603]]', 125.91528329928911, false);
INSERT INTO campus_ways (name, way_type, coordinates, distance_meters, is_oneway)
VALUES ('Ngách 15/1 Phố Tạ Quang Bửu', 'ROAD', '[[105.8484532, 21.0062537], [105.8482992, 21.0061525], [105.8481565, 21.0061215], [105.847816, 21.0061364]]', 70.14266288052337, false);
INSERT INTO campus_ways (name, way_type, coordinates, distance_meters, is_oneway)
VALUES ('Đường Giải Phóng', 'ROAD', '[[105.8410428, 21.0063309], [105.8411375, 21.0063246], [105.8411776, 21.0063248], [105.8412184, 21.0063239], [105.8413094, 21.0063215]]', 27.704354339750996, false);
INSERT INTO campus_ways (name, way_type, coordinates, distance_meters, is_oneway)
VALUES ('Phố Phương Mai', 'ROAD', '[[105.8392963, 21.0042016], [105.8395667, 21.0042157], [105.839771, 21.0042147], [105.840113, 21.0042129], [105.8401901, 21.0042094], [105.8408877, 21.004196]]', 165.2670254198107, false);
INSERT INTO campus_ways (name, way_type, coordinates, distance_meters, is_oneway)
VALUES ('Đường nội bộ', 'ROAD', '[[105.8479254, 21.0054993], [105.8473584, 21.0054979], [105.8472472, 21.005498], [105.8472194, 21.0054987], [105.8471794, 21.0054996]]', 77.4414310253911, false);
INSERT INTO campus_ways (name, way_type, coordinates, distance_meters, is_oneway)
VALUES ('Ngõ 24 Giải Phóng', 'ROAD', '[[105.8410033, 21.0057736], [105.8407874, 21.0057736], [105.8406989, 21.0057009], [105.8401999, 21.0057175], [105.8398425, 21.0056778], [105.8397752, 21.0057075], [105.8397586, 21.0058087], [105.8397447, 21.0060986]]', 175.22136274598307, false);
INSERT INTO campus_ways (name, way_type, coordinates, distance_meters, is_oneway)
VALUES ('Đường nội bộ', 'ROAD', '[[105.8398332, 21.0040232], [105.8399105, 21.0040234], [105.8400169, 21.0040217], [105.8408845, 21.0040075], [105.8409468, 21.0040066], [105.8409482, 21.0037079], [105.8404616, 21.0036964], [105.8403943, 21.0034833]]', 224.0621939195637, false);
INSERT INTO campus_ways (name, way_type, coordinates, distance_meters, is_oneway)
VALUES ('Đường nội bộ', 'ROAD', '[[105.8430558, 21.0037597], [105.8430395, 21.0037735], [105.8430295, 21.0037884], [105.8430218, 21.003805], [105.8430194, 21.0038216], [105.8430236, 21.0041295], [105.8430274, 21.0044167]]', 74.29072626990505, false);
INSERT INTO campus_ways (name, way_type, coordinates, distance_meters, is_oneway)
VALUES ('Đường nội bộ', 'ROAD', '[[105.8463219, 21.0050257], [105.8463209, 21.004962], [105.8463192, 21.0048473]]', 19.839156328667055, false);
INSERT INTO campus_ways (name, way_type, coordinates, distance_meters, is_oneway)
VALUES ('Đường nội bộ', 'ROAD', '[[105.843884, 21.0030501], [105.8438838, 21.0033743]]', 36.04940119675007, false);
INSERT INTO campus_ways (name, way_type, coordinates, distance_meters, is_oneway)
VALUES ('Đường nội bộ', 'ALLEY', '[[105.8428, 21.0033842], [105.8428025, 21.003297]]', 9.699669963693658, false);
INSERT INTO campus_ways (name, way_type, coordinates, distance_meters, is_oneway)
VALUES ('Đường nội bộ', 'ALLEY', '[[105.8431906, 21.0032867], [105.8428025, 21.003297]]', 40.303818577558424, false);
INSERT INTO campus_ways (name, way_type, coordinates, distance_meters, is_oneway)
VALUES ('Đường nội bộ', 'ROAD', '[[105.8438838, 21.0033743], [105.8437866, 21.0034109], [105.843243, 21.0034317], [105.8428, 21.0033842], [105.8423218, 21.0034042], [105.8422087, 21.0035103], [105.8420709, 21.0035065]]', 194.29093457962745, false);
INSERT INTO campus_ways (name, way_type, coordinates, distance_meters, is_oneway)
VALUES ('Đường nội bộ', 'ROAD', '[[105.8465062, 21.0050233], [105.8465076, 21.0050731], [105.846509, 21.0051195], [105.8465218, 21.0055655], [105.8465222, 21.0055811], [105.846544, 21.00634], [105.84655, 21.0065489]]', 169.69991209910083, false);
INSERT INTO campus_ways (name, way_type, coordinates, distance_meters, is_oneway)
VALUES ('Đường nội bộ', 'ROAD', '[[105.8434555, 21.0057173], [105.8434586, 21.0059758], [105.8434592, 21.0060293], [105.8434628, 21.0063694], [105.8434635, 21.0064368], [105.8434678, 21.0067603]]', 115.98340362538818, false);
INSERT INTO campus_ways (name, way_type, coordinates, distance_meters, is_oneway)
VALUES ('Đường nội bộ', 'ROAD', '[[105.8452553, 21.0060937], [105.8448723, 21.0060941], [105.8446564, 21.0060951]]', 62.16921585560529, false);
INSERT INTO campus_ways (name, way_type, coordinates, distance_meters, is_oneway)
VALUES ('Đường nội bộ', 'ROAD', '[[105.8430274, 21.0044167], [105.8430388, 21.004444], [105.8430578, 21.0044678], [105.8430917, 21.004488], [105.8431261, 21.0045002], [105.8431681, 21.0045067], [105.8432159, 21.0045034], [105.8436283, 21.004451], [105.8436502, 21.0044482], [105.8437085, 21.0044408]]', 75.55483216211421, false);
INSERT INTO campus_ways (name, way_type, coordinates, distance_meters, is_oneway)
VALUES ('Đường nội bộ', 'ROAD', '[[105.8437085, 21.0044408], [105.84371, 21.0044972], [105.8437194, 21.004867], [105.8437227, 21.0050512]]', 67.88975637844715, false);
INSERT INTO campus_ways (name, way_type, coordinates, distance_meters, is_oneway)
VALUES ('Đường nội bộ', 'ROAD', '[[105.843798, 21.0055438], [105.8438012, 21.0056843], [105.8445974, 21.00568], [105.8446514, 21.0056796]]', 103.88337532824968, false);
INSERT INTO campus_ways (name, way_type, coordinates, distance_meters, is_oneway)
VALUES ('Đường nội bộ', 'ROAD', '[[105.8438451, 21.005887], [105.8438457, 21.0059736], [105.8438466, 21.0060999], [105.8446035, 21.0060953], [105.8446564, 21.0060951]]', 107.73704521134869, false);
INSERT INTO campus_ways (name, way_type, coordinates, distance_meters, is_oneway)
VALUES ('Đường nội bộ', 'ALLEY', '[[105.841553, 21.0020875], [105.8415518, 21.0020559], [105.8416215, 21.0020001]]', 13.0474559620538, false);
INSERT INTO campus_ways (name, way_type, coordinates, distance_meters, is_oneway)
VALUES ('Đường nội bộ', 'ROAD', '[[105.8409163, 21.0018577], [105.8406007, 21.0018485], [105.8406003, 21.0020369], [105.8409174, 21.0020422]]', 86.64976333242183, false);
INSERT INTO campus_ways (name, way_type, coordinates, distance_meters, is_oneway)
VALUES ('Ngõ 204 Phố Lê Thanh Nghị', 'ROAD', '[[105.8443069, 21.0027468], [105.8437408, 21.0022756], [105.843513, 21.0020861]]', 110.40480089257066, false);
INSERT INTO campus_ways (name, way_type, coordinates, distance_meters, is_oneway)
VALUES ('Đường nội bộ', 'ROAD', '[[105.8470939, 21.0025104], [105.8470663, 21.0025368], [105.8470316, 21.0025698], [105.8468995, 21.0027052], [105.8463566, 21.0022253], [105.8464224, 21.0021579], [105.846492, 21.0020867], [105.8465072, 21.0020713], [105.8465341, 21.002044]]', 134.53278257235527, false);
INSERT INTO campus_ways (name, way_type, coordinates, distance_meters, is_oneway)
VALUES ('Đường nội bộ', 'ROAD', '[[105.8477672, 21.0029991], [105.8477882, 21.0029774], [105.8477948, 21.0029704], [105.8478161, 21.002948], [105.8479651, 21.0027866]]', 31.311721391200606, false);
INSERT INTO campus_ways (name, way_type, coordinates, distance_meters, is_oneway)
VALUES ('Đường nội bộ', 'ROAD', '[[105.8451696, 21.0069289], [105.8451702, 21.0069758], [105.8451719, 21.0070998]]', 19.004714081712077, false);
INSERT INTO campus_ways (name, way_type, coordinates, distance_meters, is_oneway)
VALUES ('Đường Giải Phóng', 'ROAD', '[[105.8414823, 21.0075347], [105.8414691, 21.0078117]]', 30.83145729517281, false);
INSERT INTO campus_ways (name, way_type, coordinates, distance_meters, is_oneway)
VALUES ('Đường Giải Phóng', 'ROAD', '[[105.8412932, 21.0078348], [105.8413007, 21.0075592]]', 30.65520929290512, false);
INSERT INTO campus_ways (name, way_type, coordinates, distance_meters, is_oneway)
VALUES ('Ngõ 27 Đường Đại Cồ Việt', 'ROAD', '[[105.847816, 21.0061364], [105.8478336, 21.0061642], [105.8480022, 21.0063355], [105.8480784, 21.006422], [105.848229, 21.0065647], [105.8488981, 21.0071435], [105.8489585, 21.0071936], [105.8492075, 21.0073799], [105.8497301, 21.0077955], [105.8501303, 21.0081017], [105.8500796, 21.0084915], [105.85007, 21.0085508]]', 375.69434568544045, false);
INSERT INTO campus_ways (name, way_type, coordinates, distance_meters, is_oneway)
VALUES ('Đường Giải Phóng', 'ROAD', '[[105.8411402, 21.0067491], [105.8411375, 21.0063246], [105.8411396, 21.0057352], [105.8411399, 21.0056436], [105.8411038, 21.0051611]]', 176.70949771280988, false);
INSERT INTO campus_ways (name, way_type, coordinates, distance_meters, is_oneway)
VALUES ('Ngõ 15 Phố Tạ Quang Bửu', 'ROAD', '[[105.8479475, 21.0057751], [105.847899, 21.0058098], [105.8478594, 21.0058528], [105.8478286, 21.0058969], [105.8478131, 21.0059386], [105.8478042, 21.0060302], [105.847808, 21.0061034], [105.847816, 21.0061364]]', 45.549067813526975, false);
INSERT INTO campus_ways (name, way_type, coordinates, distance_meters, is_oneway)
VALUES ('Ngõ 15 Phố Tạ Quang Bửu', 'ROAD', '[[105.8479254, 21.0054993], [105.8479475, 21.0057751]]', 30.75324672285854, false);
INSERT INTO campus_ways (name, way_type, coordinates, distance_meters, is_oneway)
VALUES ('Ngõ 134 Phố Lê Thanh Nghị', 'ROAD', '[[105.845514, 21.0018168], [105.8454482, 21.0018842], [105.8454172, 21.0019159], [105.8453244, 21.0020109], [105.845225, 21.0021127], [105.8451755, 21.0021633], [105.8450227, 21.0023198], [105.8448599, 21.0024802], [105.8448511, 21.0024873], [105.8448235, 21.0025096], [105.8447776, 21.0025303], [105.8447374, 21.0025379]]', 114.80914469224368, false);
INSERT INTO campus_ways (name, way_type, coordinates, distance_meters, is_oneway)
VALUES ('Đường nội bộ', 'FOOTPATH', '[[105.8481551, 21.0033902], [105.8479936, 21.0033961], [105.8478268, 21.0034126]]', 34.189616610442, false);
INSERT INTO campus_ways (name, way_type, coordinates, distance_meters, is_oneway)
VALUES ('Đường nội bộ', 'FOOTPATH', '[[105.847839, 21.0031972], [105.8478716, 21.0031634], [105.8479114, 21.003121], [105.8479323, 21.0030977]]', 14.705495360620603, false);
INSERT INTO campus_ways (name, way_type, coordinates, distance_meters, is_oneway)
VALUES ('Đường nội bộ', 'FOOTPATH', '[[105.8415965, 21.0077549], [105.8415869, 21.0076042]]', 16.786680066129502, false);
INSERT INTO campus_ways (name, way_type, coordinates, distance_meters, is_oneway)
VALUES ('Đường Giải Phóng', 'ROAD', '[[105.8415156, 21.0071533], [105.8414962, 21.0073698], [105.8414876, 21.0074796], [105.8414823, 21.0075347]]', 42.55107209540215, false);
INSERT INTO campus_ways (name, way_type, coordinates, distance_meters, is_oneway)
VALUES ('Đường nội bộ', 'FOOTPATH', '[[105.8411919, 21.0053487], [105.8412936, 21.0053544], [105.8415181, 21.0053534], [105.8415875, 21.005356]]', 41.090601198281256, false);
INSERT INTO campus_ways (name, way_type, coordinates, distance_meters, is_oneway)
VALUES ('Đường nội bộ', 'FOOTPATH', '[[105.8410518, 21.0080866], [105.8410662, 21.0080673], [105.8410857, 21.0080434], [105.8411008, 21.008018], [105.8411056, 21.0079779], [105.8411105, 21.0079369], [105.8411082, 21.0078947]]', 22.958555582855933, false);
INSERT INTO campus_ways (name, way_type, coordinates, distance_meters, is_oneway)
VALUES ('Đường nội bộ', 'FOOTPATH', '[[105.8410936, 21.0075664], [105.8410922, 21.0075397], [105.8410808, 21.0074915], [105.8410621, 21.0074621], [105.8410312, 21.0074346]]', 16.694723586552353, false);
INSERT INTO campus_ways (name, way_type, coordinates, distance_meters, is_oneway)
VALUES ('Đường nội bộ', 'FOOTPATH', '[[105.8411812, 21.0044353], [105.8412682, 21.004432], [105.8414763, 21.0044271], [105.8415713, 21.0044289]]', 40.51120025262416, false);
INSERT INTO campus_ways (name, way_type, coordinates, distance_meters, is_oneway)
VALUES ('Đường Đại Cồ Việt', 'ROAD', '[[105.8448073, 21.0077811], [105.845194, 21.0078125], [105.8455639, 21.0078569], [105.8459336, 21.0078928], [105.8460893, 21.0079147]]', 133.93381098599897, false);
INSERT INTO campus_ways (name, way_type, coordinates, distance_meters, is_oneway)
VALUES ('Đường nội bộ', 'ROAD', '[[105.8477526, 21.0081954], [105.8477531, 21.008106], [105.8477507, 21.0078299]]', 40.64289196986846, false);
INSERT INTO campus_ways (name, way_type, coordinates, distance_meters, is_oneway)
VALUES ('Đường Đại Cồ Việt', 'ROAD', '[[105.8414823, 21.0075347], [105.8415913, 21.0075258], [105.8419506, 21.0075241], [105.8422273, 21.0075213], [105.8426464, 21.0075341], [105.8431837, 21.0075693], [105.8435545, 21.0076038], [105.8437927, 21.0076335]]', 240.4451968952805, false);
INSERT INTO campus_ways (name, way_type, coordinates, distance_meters, is_oneway)
VALUES ('Ngõ 81 phố Trần Đại Nghĩa', 'ROAD', '[[105.8455641, 21.004436], [105.8456229, 21.0043727], [105.8457779, 21.004206], [105.8458046, 21.0041772], [105.8459743, 21.0039946], [105.8459263, 21.0039541]]', 71.69444388513969, false);
INSERT INTO campus_ways (name, way_type, coordinates, distance_meters, is_oneway)
VALUES ('Đường nội bộ', 'ROAD', '[[105.8448235, 21.0025096], [105.8448434, 21.0025285], [105.8449968, 21.0026738]]', 25.63195715125754, false);
INSERT INTO campus_ways (name, way_type, coordinates, distance_meters, is_oneway)
VALUES ('Đường nội bộ', 'ROAD', '[[105.843884, 21.0030501], [105.8439591, 21.0030461]]', 7.808604913881972, false);
INSERT INTO campus_ways (name, way_type, coordinates, distance_meters, is_oneway)
VALUES ('Đường nội bộ', 'ROAD', '[[105.8472624, 21.0064838], [105.8473109, 21.0064838], [105.8473465, 21.0064838], [105.8478993, 21.0064839]]', 66.1133399257344, false);
INSERT INTO campus_ways (name, way_type, coordinates, distance_meters, is_oneway)
VALUES ('Đường nội bộ', 'ROAD', '[[105.8453974, 21.007122], [105.8454441, 21.0071243], [105.8454907, 21.0071285], [105.845511, 21.0071423], [105.8456656, 21.0071493], [105.8456872, 21.0071352], [105.845763, 21.0071378], [105.8460856, 21.0071488]]', 72.50724989946255, false);
INSERT INTO campus_ways (name, way_type, coordinates, distance_meters, is_oneway)
VALUES ('Ngõ 30 Phố Tạ Quang Bửu', 'ROAD', '[[105.8457685, 21.0066653], [105.8456284, 21.0066704], [105.8455666, 21.0066833], [105.845508, 21.0067053], [105.8454647, 21.0067224], [105.845425, 21.0067328], [105.8454144, 21.006734], [105.8453346, 21.0067429], [105.8452087, 21.0067481], [105.8450948, 21.0067459]]', 71.20328615763374, false);
INSERT INTO campus_ways (name, way_type, coordinates, distance_meters, is_oneway)
VALUES ('Đường nội bộ', 'ROAD', '[[105.8469864, 21.0023326], [105.8470136, 21.0023032], [105.8470205, 21.0022958], [105.8470474, 21.0022667], [105.8471903, 21.0021126]]', 32.34889260446454, false);
INSERT INTO campus_ways (name, way_type, coordinates, distance_meters, is_oneway)
VALUES ('Đường nội bộ', 'ROAD', '[[105.847253, 21.0058908], [105.8472907, 21.0058901], [105.8472977, 21.00589], [105.847596, 21.0058849]]', 35.61127335786343, false);
INSERT INTO campus_ways (name, way_type, coordinates, distance_meters, is_oneway)
VALUES ('Đường nội bộ', 'ROAD', '[[105.841001, 21.0046118], [105.8410031, 21.0045389], [105.8410062, 21.0044452], [105.8410674, 21.0043695], [105.8410694, 21.0042481], [105.8410713, 21.0041947]]', 48.5204999732033, false);
INSERT INTO campus_ways (name, way_type, coordinates, distance_meters, is_oneway)
VALUES ('Phố Phương Mai', 'ROAD', '[[105.8408877, 21.004196], [105.8410713, 21.0041947]]', 19.05941954845178, false);
INSERT INTO campus_ways (name, way_type, coordinates, distance_meters, is_oneway)
VALUES ('Phố Phương Mai', 'ROAD', '[[105.8410713, 21.0041947], [105.8411358, 21.0041939], [105.8411726, 21.0041914], [105.8412431, 21.0041897]]', 17.84708818429459, false);
INSERT INTO campus_ways (name, way_type, coordinates, distance_meters, is_oneway)
VALUES ('Ngõ 174 Phố Lê Thanh Nghị', 'ROAD', '[[105.8441437, 21.001825], [105.8442143, 21.0018869], [105.8444017, 21.0020975], [105.8443442, 21.002185], [105.8443317, 21.0022357], [105.8443156, 21.0023058], [105.8443236, 21.0023484], [105.8443156, 21.0023984], [105.8443263, 21.002431], [105.844388, 21.0024535], [105.8444614, 21.002484], [105.8444534, 21.0025334], [105.8444277, 21.0026126], [105.8443069, 21.0027468]]', 129.3569315187336, false);
INSERT INTO campus_ways (name, way_type, coordinates, distance_meters, is_oneway)
VALUES ('Ngõ 224 Lê Thanh Nghị', 'ROAD', '[[105.8428835, 21.0018305], [105.8429154, 21.0018901], [105.843039, 21.0021796]]', 42.06224794153304, false);
INSERT INTO campus_ways (name, way_type, coordinates, distance_meters, is_oneway)
VALUES ('Ngõ 256 Phố Lê Thanh Nghị', 'ROAD', '[[105.8420859, 21.0018555], [105.8420859, 21.0019072], [105.8421008, 21.002281], [105.8422101, 21.0022494], [105.8423086, 21.0021969]]', 70.99421167111164, false);
INSERT INTO campus_ways (name, way_type, coordinates, distance_meters, is_oneway)
VALUES ('Ngõ 266 Phố Lê Thanh Nghị', 'ROAD', '[[105.841725, 21.0018687], [105.8416908, 21.0019605], [105.8417028, 21.0026836], [105.8417197, 21.0029826]]', 124.51569713011779, false);
INSERT INTO campus_ways (name, way_type, coordinates, distance_meters, is_oneway)
VALUES ('Đường nội bộ', 'FOOTPATH', '[[105.8416917, 21.00187], [105.8416472, 21.0018954], [105.841605, 21.0019279], [105.8415639, 21.0019611], [105.8415217, 21.0019957], [105.8414391, 21.002074]]', 34.785601795931484, false);
INSERT INTO campus_ways (name, way_type, coordinates, distance_meters, is_oneway)
VALUES ('Hầm chui Kim Liên', 'ROAD', '[[105.8406625, 21.0078991], [105.8403255, 21.0080133], [105.8400269, 21.008148], [105.8396744, 21.0083417], [105.8393997, 21.0085343], [105.8391262, 21.008773], [105.8387781, 21.009076], [105.8385009, 21.0093594], [105.8382737, 21.0095894], [105.8381811, 21.0097637]]', 337.1270377516173, false);
INSERT INTO campus_ways (name, way_type, coordinates, distance_meters, is_oneway)
VALUES ('Đường Giải Phóng', 'ROAD', '[[105.8412795, 21.004697], [105.8412682, 21.004432], [105.8412431, 21.0041897]]', 56.55821896138877, false);
INSERT INTO campus_ways (name, way_type, coordinates, distance_meters, is_oneway)
VALUES ('Đường Giải Phóng', 'ROAD', '[[105.8413254, 21.007045], [105.8413234, 21.0069589], [105.8413094, 21.0063215], [105.8412936, 21.0053544], [105.8412837, 21.005163], [105.8412795, 21.004697]]', 261.1419742262242, false);
INSERT INTO campus_ways (name, way_type, coordinates, distance_meters, is_oneway)
VALUES ('Đường nội bộ', 'ROAD', '[[105.8460661, 21.0081019], [105.8460893, 21.0079147]]', 20.954537088268477, false);
INSERT INTO campus_ways (name, way_type, coordinates, distance_meters, is_oneway)
VALUES ('Đường nội bộ', 'ROAD', '[[105.8430274, 21.0044167], [105.8430209, 21.0044747], [105.8430194, 21.0045304], [105.843028, 21.004998], [105.8430282, 21.0050471]]', 70.14214203392561, false);
INSERT INTO campus_ways (name, way_type, coordinates, distance_meters, is_oneway)
VALUES ('Đường Đại Cồ Việt', 'ROAD', '[[105.8449471, 21.0079814], [105.8446346, 21.0080114], [105.843758, 21.0079211], [105.8430657, 21.0078477], [105.8424483, 21.0077966], [105.8422235, 21.0078007], [105.8418746, 21.0078013], [105.8415988, 21.0077949], [105.8414691, 21.0078117]]', 362.60915391064833, false);
INSERT INTO campus_ways (name, way_type, coordinates, distance_meters, is_oneway)
VALUES ('Đường nội bộ', 'ROAD', '[[105.8429498, 21.005551], [105.8428094, 21.0054356], [105.8426947, 21.0053387], [105.8425682, 21.005232], [105.8425436, 21.0052111], [105.8425337, 21.0052028]]', 58.007831521300105, false);
INSERT INTO campus_ways (name, way_type, coordinates, distance_meters, is_oneway)
VALUES ('Đường nội bộ', 'ROAD', '[[105.8439591, 21.0030461], [105.8441314, 21.0030378]]', 17.909791745837765, false);
INSERT INTO campus_ways (name, way_type, coordinates, distance_meters, is_oneway)
VALUES ('Đường nội bộ', 'ROAD', '[[105.8441314, 21.0030378], [105.8445169, 21.0030191], [105.8445786, 21.0030155], [105.8446758, 21.0030107]]', 56.59328534952026, false);
INSERT INTO campus_ways (name, way_type, coordinates, distance_meters, is_oneway)
VALUES ('Đường nội bộ', 'ROAD', '[[105.8415836, 21.0035488], [105.8416548, 21.0035652], [105.8420247, 21.0035487], [105.8420709, 21.0035065]]', 52.76433579303094, false);
INSERT INTO campus_ways (name, way_type, coordinates, distance_meters, is_oneway)
VALUES ('Đường nội bộ', 'ROAD', '[[105.8420709, 21.0035065], [105.8420679, 21.0037733], [105.8421331, 21.0037196], [105.8422382, 21.003688], [105.8422414, 21.003568], [105.8422087, 21.0035103]]', 70.76222033442639, false);
INSERT INTO campus_ways (name, way_type, coordinates, distance_meters, is_oneway)
VALUES ('Đường nội bộ', 'ALLEY', '[[105.8428025, 21.003297], [105.8426778, 21.0033014], [105.8423816, 21.0033118]]', 43.723393771336845, false);
INSERT INTO campus_ways (name, way_type, coordinates, distance_meters, is_oneway)
VALUES ('Đường nội bộ', 'ALLEY', '[[105.8426778, 21.0033014], [105.8426738, 21.0032162], [105.8426494, 21.0031896], [105.8426372, 21.0028974], [105.8426799, 21.0028936], [105.8426941, 21.0031859], [105.8426738, 21.0032162]]', 86.85513428986395, false);
INSERT INTO campus_ways (name, way_type, coordinates, distance_meters, is_oneway)
VALUES ('Đường Đại Cồ Việt', 'ROAD', '[[105.8460661, 21.0081019], [105.8458024, 21.0080728], [105.8451951, 21.0080127], [105.8450055, 21.0079875], [105.8449471, 21.0079814]]', 116.93658584516025, false);
INSERT INTO campus_ways (name, way_type, coordinates, distance_meters, is_oneway)
VALUES ('Đường Đại Cồ Việt', 'ROAD', '[[105.8460893, 21.0079147], [105.8473013, 21.0081021], [105.8474218, 21.0081248]]', 140.2847553789895, false);
INSERT INTO campus_ways (name, way_type, coordinates, distance_meters, is_oneway)
VALUES ('Phố Lê Thanh Nghị', 'ROAD', '[[105.8479807, 21.0032553], [105.8478716, 21.0031634], [105.8477219, 21.0030369], [105.8470939, 21.0025104], [105.8465341, 21.002044], [105.8463563, 21.0019195], [105.8463278, 21.0019013], [105.8462568, 21.0018287]]', 239.3106005137413, false);
INSERT INTO campus_ways (name, way_type, coordinates, distance_meters, is_oneway)
VALUES ('Đường Giải Phóng', 'ROAD', '[[105.8414391, 21.002074], [105.8414459, 21.0030349], [105.8414534, 21.0035472]]', 163.82001773198704, false);
INSERT INTO campus_ways (name, way_type, coordinates, distance_meters, is_oneway)
VALUES ('Đường Giải Phóng', 'ROAD', '[[105.8414534, 21.0035472], [105.8414763, 21.0044271], [105.8414856, 21.0045406], [105.841507, 21.0051027], [105.8415181, 21.0053534], [105.8415156, 21.0071533]]', 401.1092046523705, false);
INSERT INTO campus_ways (name, way_type, coordinates, distance_meters, is_oneway)
VALUES ('Đường nội bộ', 'ROAD', '[[105.8478161, 21.002948], [105.8470474, 21.0022667]]', 110.0303754892135, false);
INSERT INTO campus_ways (name, way_type, coordinates, distance_meters, is_oneway)
VALUES ('Đường nội bộ', 'FOOTPATH', '[[105.8454206, 21.0081707], [105.8454716, 21.0077861]]', 43.09199780612945, false);
INSERT INTO campus_ways (name, way_type, coordinates, distance_meters, is_oneway)
VALUES ('Đường nội bộ', 'ALLEY', '[[105.8454716, 21.0077861], [105.8455794, 21.007776]]', 11.246286653444162, false);
INSERT INTO campus_ways (name, way_type, coordinates, distance_meters, is_oneway)
VALUES ('Đường nội bộ', 'ALLEY', '[[105.8454716, 21.0077861], [105.8453865, 21.0077563]]', 9.434756032822632, false);
INSERT INTO campus_ways (name, way_type, coordinates, distance_meters, is_oneway)
VALUES ('Đường nội bộ', 'ROAD', '[[105.8456502, 21.0050332], [105.8455476, 21.0050687], [105.8455108, 21.0050692], [105.8453887, 21.0050705], [105.8452961, 21.0050714], [105.8448646, 21.0050734], [105.8447315, 21.0050735], [105.8446398, 21.005037]]', 106.4248853990817, false);
INSERT INTO campus_ways (name, way_type, coordinates, distance_meters, is_oneway)
VALUES ('Đường nội bộ', 'ROAD', '[[105.8446398, 21.005037], [105.8445896, 21.0050377], [105.8444541, 21.0050395], [105.8441102, 21.0050442], [105.8438376, 21.0050479], [105.8437227, 21.0050512], [105.8435723, 21.0050527], [105.8435453, 21.0050531]]', 113.63126359735278, false);
INSERT INTO campus_ways (name, way_type, coordinates, distance_meters, is_oneway)
VALUES ('Đường nội bộ', 'ROAD', '[[105.8446398, 21.005037], [105.8447307, 21.0049904], [105.8454443, 21.0049845], [105.8455097, 21.0049842], [105.8455242, 21.0049841], [105.8455468, 21.004984], [105.8456502, 21.0050332]]', 107.53150445148488, false);
INSERT INTO campus_ways (name, way_type, coordinates, distance_meters, is_oneway)
VALUES ('Đường nội bộ', 'ROAD', '[[105.8417746, 21.0050964], [105.8417155, 21.0050975], [105.8416351, 21.0050989], [105.841615, 21.0050992], [105.8415819, 21.0050998], [105.841507, 21.0051027]]', 27.788717582418, false);
INSERT INTO campus_ways (name, way_type, coordinates, distance_meters, is_oneway)
VALUES ('Đường nội bộ', 'ROAD', '[[105.8417746, 21.0050964], [105.841901, 21.0050586], [105.8423417, 21.0050536], [105.8424411, 21.0050533], [105.8430282, 21.0050471], [105.8430544, 21.0050467], [105.8433507, 21.0050422], [105.8434432, 21.0050417], [105.8435453, 21.0050531]]', 184.55333555020619, false);
INSERT INTO campus_ways (name, way_type, coordinates, distance_meters, is_oneway)
VALUES ('Đường nội bộ', 'ROAD', '[[105.8435453, 21.0050531], [105.8434253, 21.005114], [105.8430553, 21.005118], [105.8424412, 21.0051247]]', 116.34063860443462, false);
INSERT INTO campus_ways (name, way_type, coordinates, distance_meters, is_oneway)
VALUES ('Đường nội bộ', 'FOOTPATH', '[[105.8417204, 21.0056875], [105.8417477, 21.0056872], [105.8417608, 21.0056871], [105.8424337, 21.0056797], [105.8427847, 21.0056753]]', 110.48851867845667, false);
INSERT INTO campus_ways (name, way_type, coordinates, distance_meters, is_oneway)
VALUES ('Đường nội bộ', 'ROAD', '[[105.8435545, 21.0076038], [105.843562, 21.00753], [105.8435702, 21.0074713], [105.8435659, 21.0073094], [105.8435635, 21.0072331]]', 41.321263846907954, false);
INSERT INTO campus_ways (name, way_type, coordinates, distance_meters, is_oneway)
VALUES ('Đường nội bộ', 'ROAD', '[[105.8474403, 21.0075617], [105.8473293, 21.007541], [105.84685, 21.0074594]]', 62.323861779525764, false);
INSERT INTO campus_ways (name, way_type, coordinates, distance_meters, is_oneway)
VALUES ('Đường nội bộ', 'FOOTPATH', '[[105.8410312, 21.0074346], [105.8409983, 21.0074015], [105.8409587, 21.0073632]]', 10.939930811989298, false);
INSERT INTO campus_ways (name, way_type, coordinates, distance_meters, is_oneway)
VALUES ('Đường nội bộ', 'ALLEY', '[[105.8409587, 21.0073632], [105.8410354, 21.0073311]]', 8.725273800604217, false);
INSERT INTO campus_ways (name, way_type, coordinates, distance_meters, is_oneway)
VALUES ('Đường nội bộ', 'FOOTPATH', '[[105.8410354, 21.0073311], [105.8416253, 21.0073209]]', 61.244665975668106, false);
INSERT INTO campus_ways (name, way_type, coordinates, distance_meters, is_oneway)
VALUES ('Đường nội bộ', 'ALLEY', '[[105.8416253, 21.0073209], [105.8417113, 21.0073831]]', 11.292913419710674, false);
INSERT INTO campus_ways (name, way_type, coordinates, distance_meters, is_oneway)
VALUES ('Đường nội bộ', 'FOOTPATH', '[[105.8417113, 21.0073831], [105.8416549, 21.0074332], [105.8416085, 21.0074825]]', 15.378756852987404, false);
INSERT INTO campus_ways (name, way_type, coordinates, distance_meters, is_oneway)
VALUES ('Đường nội bộ', 'FOOTPATH', '[[105.8441298, 21.0075647], [105.8441753, 21.0076042], [105.8442274, 21.0076307], [105.8448199, 21.0076948], [105.8451331, 21.0077287], [105.8453865, 21.0077563], [105.8455794, 21.007776], [105.846141, 21.0078453], [105.8473238, 21.0080242], [105.8474281, 21.008046]]', 348.1574631866032, false);
INSERT INTO campus_ways (name, way_type, coordinates, distance_meters, is_oneway)
VALUES ('Đường nội bộ', 'FOOTPATH', '[[105.8417061, 21.0080534], [105.8418507, 21.0079408], [105.8419524, 21.0078886], [105.8420935, 21.0078638], [105.8424709, 21.0078608], [105.8428315, 21.007881], [105.8432244, 21.0079151], [105.8443772, 21.0080438], [105.8448682, 21.0080965], [105.8452926, 21.0081436], [105.8455741, 21.008177], [105.8456701, 21.0081927], [105.8457846, 21.0082112], [105.8458215, 21.0082219], [105.8458899, 21.0082323], [105.8459444, 21.0082565]]', 449.42436071761864, false);
INSERT INTO campus_ways (name, way_type, coordinates, distance_meters, is_oneway)
VALUES ('Đường nội bộ', 'ROAD', '[[105.8426162, 21.0027603], [105.8425796, 21.0025585], [105.842646, 21.0025221], [105.842701, 21.0023916], [105.8428798, 21.0023156]]', 66.73971744613397, false);
INSERT INTO campus_ways (name, way_type, coordinates, distance_meters, is_oneway)
VALUES ('Đường nội bộ', 'ROAD', '[[105.8459743, 21.0039946], [105.846103, 21.0041033]]', 18.016104524776484, false);
INSERT INTO campus_ways (name, way_type, coordinates, distance_meters, is_oneway)
VALUES ('Hẻm 40/60/11 Phố Tạ Quang Bửu', 'ROAD', '[[105.845606, 21.0034988], [105.8453011, 21.0032411], [105.8451492, 21.0031126], [105.8451147, 21.0031479]]', 69.28779499110698, false);
INSERT INTO campus_ways (name, way_type, coordinates, distance_meters, is_oneway)
VALUES ('Ngách 40/60 Phố Tạ Quang Bửu', 'ROAD', '[[105.8460271, 21.0031376], [105.8459995, 21.0031673], [105.8458947, 21.0032801], [105.8458719, 21.0032609], [105.84577, 21.0033705], [105.8457573, 21.0033842], [105.8457414, 21.0033707], [105.845632, 21.0034889], [105.845606, 21.0034988], [105.8455098, 21.0036024]]', 80.07646345374465, false);
INSERT INTO campus_ways (name, way_type, coordinates, distance_meters, is_oneway)
VALUES ('Hẻm 40/30/5 Phố Tạ Quang Bửu', 'ROAD', '[[105.8461613, 21.0037012], [105.84577, 21.0033705]]', 54.791866032152086, false);
INSERT INTO campus_ways (name, way_type, coordinates, distance_meters, is_oneway)
VALUES ('Đường nội bộ', 'ROAD', '[[105.8460391, 21.0038326], [105.845632, 21.0034889]]', 56.977896825077664, false);
INSERT INTO campus_ways (name, way_type, coordinates, distance_meters, is_oneway)
VALUES ('Đường nội bộ', 'ROAD', '[[105.8458537, 21.0026564], [105.8456218, 21.0024621]]', 32.34644245197319, false);
INSERT INTO campus_ways (name, way_type, coordinates, distance_meters, is_oneway)
VALUES ('Ngách 106/11 Phố Lê Thanh Nghị', 'ROAD', '[[105.8453244, 21.0020109], [105.8457444, 21.0023366]]', 56.67903137051727, false);
INSERT INTO campus_ways (name, way_type, coordinates, distance_meters, is_oneway)
VALUES ('Đường nội bộ', 'ROAD', '[[105.8454172, 21.0019159], [105.8458337, 21.0022451]]', 56.65073807603603, false);
INSERT INTO campus_ways (name, way_type, coordinates, distance_meters, is_oneway)
VALUES ('Đường nội bộ', 'ROAD', '[[105.8392514, 21.0027551], [105.8404545, 21.0027365], [105.8404853, 21.0027171], [105.8405095, 21.002687], [105.8405068, 21.0024541], [105.8405175, 21.0023965], [105.8405497, 21.002354], [105.8405685, 21.0023064], [105.8405739, 21.002076], [105.8406003, 21.0020369]]', 207.54343869021648, false);
INSERT INTO campus_ways (name, way_type, coordinates, distance_meters, is_oneway)
VALUES ('Đường nội bộ', 'ROAD', '[[105.8411038, 21.0051611], [105.8411577, 21.0051676], [105.8411848, 21.0051708], [105.8412837, 21.005163]]', 18.780140689775774, false);
INSERT INTO campus_ways (name, way_type, coordinates, distance_meters, is_oneway)
VALUES ('Ngõ 13 đường Giải Phóng', 'ROAD', '[[105.8414856, 21.0045406], [105.8415726, 21.0045396], [105.8416286, 21.0045359], [105.841835, 21.0045306], [105.8418357, 21.0045503], [105.8421192, 21.0045503], [105.8421147, 21.0043636]]', 88.67939559533403, false);
INSERT INTO campus_ways (name, way_type, coordinates, distance_meters, is_oneway)
VALUES ('Đường nội bộ', 'ROAD', '[[105.846544, 21.00634], [105.8465919, 21.0063395], [105.8472063, 21.0063325], [105.8472255, 21.0063324], [105.8472656, 21.0063322]]', 74.91083863887015, false);
INSERT INTO campus_ways (name, way_type, coordinates, distance_meters, is_oneway)
VALUES ('Đường nội bộ', 'ROAD', '[[105.8443672, 21.0069667], [105.8443425, 21.0069963], [105.8442864, 21.0070404], [105.8442282, 21.0070754], [105.8440001, 21.0071657], [105.8439894, 21.0071699], [105.8439391, 21.0071846], [105.8438251, 21.0072109], [105.8437155, 21.007225], [105.8435635, 21.0072331], [105.8432604, 21.0072358], [105.8430387, 21.0072378], [105.8426881, 21.0072311], [105.8426544, 21.0072266], [105.842608, 21.0072171], [105.8425445, 21.0071981], [105.8424769, 21.007179], [105.8423888, 21.0071557], [105.8421752, 21.0071079], [105.8420513, 21.0070751], [105.8419381, 21.0070246], [105.841853, 21.0069589], [105.8418088, 21.006899], [105.8417954, 21.0068228], [105.8417945, 21.0067568], [105.8417935, 21.0067242], [105.8417851, 21.0064506], [105.8417768, 21.0061817], [105.8417738, 21.0060131], [105.8417404, 21.0059438], [105.841723, 21.0059135], [105.8417218, 21.0058606], [105.8417204, 21.0056875], [105.8417194, 21.0055583], [105.8417184, 21.0054459], [105.8417176, 21.005349], [105.8417168, 21.0052374], [105.8417164, 21.00519], [105.8417155, 21.0050975]]', 484.3546559993294, false);
INSERT INTO campus_ways (name, way_type, coordinates, distance_meters, is_oneway)
VALUES ('Phố Xã Đàn', 'ROAD', '[[105.8407052, 21.0076693], [105.8410389, 21.0076004], [105.8410954, 21.0075921], [105.8412, 21.0075754], [105.8413007, 21.0075592]]', 63.03637180276399, false);
INSERT INTO campus_ways (name, way_type, coordinates, distance_meters, is_oneway)
VALUES ('Đường Đại Cồ Việt', 'ROAD', '[[105.8437927, 21.0076335], [105.8441307, 21.0076739], [105.8444926, 21.0077309], [105.8448073, 21.0077811]]', 106.6104057174394, false);
INSERT INTO campus_ways (name, way_type, coordinates, distance_meters, is_oneway)
VALUES ('Phố Trần Đại Nghĩa', 'ROAD', '[[105.8440142, 21.0074152], [105.8440518, 21.0073617], [105.84409, 21.0073073], [105.8441688, 21.0072442], [105.8442587, 21.0071919], [105.8443689, 21.0071377], [105.8445278, 21.0070782], [105.8446418, 21.0070355], [105.8447435, 21.0069889], [105.8447899, 21.0069676], [105.8449124, 21.0068938], [105.8449942, 21.0068315], [105.8450617, 21.0067735]]', 132.31589170644295, false);
INSERT INTO campus_ways (name, way_type, coordinates, distance_meters, is_oneway)
VALUES ('Phố Lê Thanh Nghị', 'ROAD', '[[105.8462568, 21.0018287], [105.8463295, 21.0018405], [105.8463527, 21.0018443], [105.8464014, 21.0018613], [105.846561, 21.0019759], [105.8467549, 21.0021344], [105.8469864, 21.0023326], [105.8471272, 21.0024531], [105.8475638, 21.0028269], [105.8477672, 21.0029991], [105.8479114, 21.003121], [105.8480271, 21.0032149]]', 241.70139665301522, false);
INSERT INTO campus_ways (name, way_type, coordinates, distance_meters, is_oneway)
VALUES ('Phố Tạ Quang Bửu', 'ROAD', '[[105.8479807, 21.0032553], [105.8480271, 21.0032149]]', 6.586400554395885, false);
INSERT INTO campus_ways (name, way_type, coordinates, distance_meters, is_oneway)
VALUES ('Phố Xã Đàn', 'ROAD', '[[105.8402267, 21.0078235], [105.8402977, 21.0077973], [105.8405904, 21.0077022], [105.8407052, 21.0076693]]', 52.5616331069982, false);
INSERT INTO campus_ways (name, way_type, coordinates, distance_meters, is_oneway)
VALUES ('Đường Giải Phóng', 'ROAD', '[[105.8413007, 21.0075592], [105.8413113, 21.0074212], [105.8413254, 21.007045]]', 57.241428561197054, false);
INSERT INTO campus_ways (name, way_type, coordinates, distance_meters, is_oneway)
VALUES ('Đường nội bộ', 'ROAD', '[[105.8410382, 21.0061558], [105.8408499, 21.0061633], [105.8408592, 21.0064208], [105.8409925, 21.0064253], [105.8410383, 21.0064105], [105.8410428, 21.0063309]]', 75.95396383459891, false);
INSERT INTO campus_ways (name, way_type, coordinates, distance_meters, is_oneway)
VALUES ('Ngõ 2 Phố Phương Mai', 'ROAD', '[[105.840113, 21.0042129], [105.8401135, 21.0042634], [105.8401174, 21.0044973], [105.8401301, 21.0046352], [105.8401394, 21.0048743], [105.8401542, 21.0051404], [105.8401652, 21.0053382]]', 125.2746102805994, false);
INSERT INTO campus_ways (name, way_type, coordinates, distance_meters, is_oneway)
VALUES ('Đường nội bộ', 'ROAD', '[[105.8407627, 21.0048665], [105.8401394, 21.0048743]]', 64.7081047205328, false);
INSERT INTO campus_ways (name, way_type, coordinates, distance_meters, is_oneway)
VALUES ('Đường nội bộ', 'ROAD', '[[105.8401542, 21.0051404], [105.8407673, 21.0051497]]', 63.651754189495826, false);
INSERT INTO campus_ways (name, way_type, coordinates, distance_meters, is_oneway)
VALUES ('Đường nội bộ', 'ROAD', '[[105.8394973, 21.0044924], [105.8397523, 21.0044893], [105.8401174, 21.0044973]]', 64.38295817668961, false);
INSERT INTO campus_ways (name, way_type, coordinates, distance_meters, is_oneway)
VALUES ('Đường nội bộ', 'FOOTPATH', '[[105.8452884, 21.0100443], [105.8452933, 21.0099307], [105.8452971, 21.0098338], [105.8452967, 21.009723], [105.8453162, 21.0096202], [105.8453445, 21.009532], [105.845385, 21.0094347], [105.8454216, 21.0093371], [105.8454138, 21.0092117], [105.8454035, 21.0090874], [105.8454172, 21.0089482], [105.845409, 21.0088201], [105.8453737, 21.0087385], [105.8453005, 21.0086101], [105.8452275, 21.0084969], [105.8451549, 21.0084277], [105.8450766, 21.0083676], [105.8449908, 21.0083206], [105.8448888, 21.0082795], [105.8447579, 21.0082505], [105.8446453, 21.0082254], [105.8445391, 21.0082194], [105.8444511, 21.0082244], [105.8443341, 21.0082374], [105.8442354, 21.0082334], [105.8441614, 21.0082254], [105.8440488, 21.0082044], [105.8439822, 21.0081753], [105.8438555, 21.0081101], [105.8437713, 21.0080839], [105.8436948, 21.0080791], [105.8436246, 21.0080993], [105.8435021, 21.0081279], [105.8433758, 21.0081541], [105.8432342, 21.0081637], [105.843076, 21.0081672], [105.8430449, 21.0081655], [105.842901, 21.0081574], [105.8428025, 21.0081364], [105.8426791, 21.0081102], [105.842403, 21.0080378], [105.842302, 21.0080041], [105.8422098, 21.0079933], [105.8421648, 21.0080171], [105.8421552, 21.0080469], [105.8421619, 21.0080924], [105.8421879, 21.0081374], [105.8422149, 21.0081816], [105.8422311, 21.0082257], [105.8422292, 21.0082636], [105.8422285, 21.008278], [105.8422208, 21.0083216], [105.8421903, 21.0083602], [105.8421334, 21.008391], [105.842113, 21.0083928], [105.84207, 21.0083967], [105.8419411, 21.0084232], [105.8418279, 21.0084792]]', 594.9499184405081, false);
INSERT INTO campus_ways (name, way_type, coordinates, distance_meters, is_oneway)
VALUES ('Đường nội bộ', 'ROAD', '[[105.8444597, 21.0040587], [105.8444952, 21.004058], [105.844576, 21.0040572]]', 12.073999424016211, false);
INSERT INTO campus_ways (name, way_type, coordinates, distance_meters, is_oneway)
VALUES ('Đường nội bộ', 'ROAD', '[[105.8444597, 21.0040587], [105.8443704, 21.004098], [105.8437614, 21.0041163], [105.8436958, 21.0041181]]', 80.31190150692075, false);
INSERT INTO campus_ways (name, way_type, coordinates, distance_meters, is_oneway)
VALUES ('Đường nội bộ', 'ROAD', '[[105.8444597, 21.0040587], [105.8443755, 21.0040134], [105.8437601, 21.0040294]]', 73.99543390557545, false);
INSERT INTO campus_ways (name, way_type, coordinates, distance_meters, is_oneway)
VALUES ('Đường nội bộ', 'ROAD', '[[105.8437614, 21.0041163], [105.8437601, 21.0040294]]', 9.663781406509827, false);
INSERT INTO campus_ways (name, way_type, coordinates, distance_meters, is_oneway)
VALUES ('Đường nội bộ', 'FOOTPATH', '[[105.8417113, 21.0073831], [105.8416606, 21.0073278], [105.8416416, 21.0072891], [105.8416353, 21.0072326], [105.8416308, 21.0071139], [105.841629, 21.0067307], [105.8416128, 21.0060493], [105.8415875, 21.005356], [105.8415842, 21.0051909], [105.8415819, 21.0050998], [105.8415799, 21.0050212], [105.8415767, 21.0047632], [105.8415726, 21.0045396], [105.8415713, 21.0044289], [105.8415699, 21.0043905], [105.8415497, 21.0037912], [105.8415481, 21.0037425], [105.8415477, 21.0035512], [105.8415328, 21.0031708], [105.8415309, 21.0030279], [105.8415277, 21.0027765], [105.8415175, 21.0021607]]', 583.2779913710903, false);
INSERT INTO campus_ways (name, way_type, coordinates, distance_meters, is_oneway)
VALUES ('Đường nội bộ', 'FOOTPATH', '[[105.8451272, 21.0018918], [105.8454482, 21.0018842], [105.8458179, 21.0018842], [105.8458932, 21.0018862], [105.8460432, 21.0018902], [105.8461359, 21.0018956], [105.8461655, 21.0019061], [105.8461904, 21.0019149], [105.8462885, 21.0019585], [105.8463206, 21.0019728], [105.8463343, 21.0019789], [105.8463992, 21.0020084], [105.8464582, 21.0020389], [105.8465072, 21.0020713], [105.8465204, 21.00208], [105.8465778, 21.0021256], [105.8466233, 21.0021636], [105.8467312, 21.0022538], [105.846927, 21.0024191], [105.8470663, 21.0025368], [105.847125, 21.0025863], [105.8475367, 21.0029305], [105.8475748, 21.0029686], [105.847839, 21.0031972], [105.8478449, 21.0032023], [105.8478833, 21.0032356], [105.8478935, 21.0032499], [105.8479021, 21.0032743], [105.8478967, 21.0032931]]', 347.8340234206086, false);
INSERT INTO campus_ways (name, way_type, coordinates, distance_meters, is_oneway)
VALUES ('Đường nội bộ', 'FOOTPATH', '[[105.8478967, 21.0032931], [105.847839, 21.0033526], [105.8477512, 21.0034431], [105.847203, 21.0039765], [105.8471772, 21.00399], [105.8471423, 21.0039928], [105.8471105, 21.0039801], [105.8470707, 21.0039476], [105.8467237, 21.0036642], [105.8458849, 21.0029793]]', 284.02300263626836, false);
INSERT INTO campus_ways (name, way_type, coordinates, distance_meters, is_oneway)
VALUES ('Đường nội bộ', 'FOOTPATH', '[[105.8451272, 21.0018918], [105.8450715, 21.0019037], [105.8450372, 21.0019197], [105.8450133, 21.001939], [105.8449975, 21.0019518], [105.8449847, 21.0019687], [105.8449588, 21.0020028], [105.8449138, 21.002095], [105.8448891, 21.0021871], [105.8448846, 21.0022307], [105.8448799, 21.0022767], [105.8448623, 21.0024275], [105.8448511, 21.0024873], [105.8448434, 21.0025285], [105.844814, 21.0026859], [105.8447732, 21.0029043], [105.8447582, 21.0030395], [105.8447539, 21.0031697], [105.844755, 21.0032709], [105.8447818, 21.003367], [105.8448026, 21.0034174], [105.8448223, 21.0034653], [105.844828, 21.0034792], [105.8449031, 21.0036004], [105.8449943, 21.0037216], [105.84505, 21.0037827], [105.8451391, 21.0038548], [105.8452067, 21.0038839], [105.8453419, 21.00399], [105.8453537, 21.0040461], [105.845418, 21.0041212], [105.8454921, 21.0041923], [105.8455532, 21.0042534], [105.8455908, 21.0043065], [105.8456229, 21.0043727], [105.8456519, 21.0044267], [105.8456841, 21.0045081], [105.8456964, 21.0045558], [105.8457068, 21.0046203], [105.8457121, 21.0046758], [105.845716, 21.0047484], [105.8457215, 21.0048549], [105.8457291, 21.0048896], [105.845748, 21.0049257], [105.8457746, 21.0049505], [105.8457917, 21.0049572], [105.8458035, 21.0049618], [105.8458451, 21.0049682]]', 397.03166713887055, false);
INSERT INTO campus_ways (name, way_type, coordinates, distance_meters, is_oneway)
VALUES ('Đường nội bộ', 'FOOTPATH', '[[105.8458451, 21.0049682], [105.8462854, 21.0049628], [105.8463209, 21.004962], [105.8465411, 21.0049571], [105.8466784, 21.0049501], [105.8467194, 21.0049437], [105.8467998, 21.0049224], [105.846862, 21.0049047]]', 106.2749775260977, false);
INSERT INTO campus_ways (name, way_type, coordinates, distance_meters, is_oneway)
VALUES ('Đường nội bộ', 'FOOTPATH', '[[105.846862, 21.0049047], [105.8469007, 21.0048906], [105.8469363, 21.0048622], [105.8469659, 21.0048219], [105.8470881, 21.0045974], [105.847145, 21.0044982], [105.8471601, 21.0044685], [105.8471623, 21.004449], [105.8471655, 21.004421], [105.8471655, 21.0042029], [105.847151, 21.0041568], [105.8471302, 21.0041196], [105.8471032, 21.0040839], [105.8469492, 21.0039536], [105.8468679, 21.0038863], [105.8463714, 21.0034751], [105.8459995, 21.0031673], [105.845803, 21.0030048]]', 283.8033148878327, false);
INSERT INTO campus_ways (name, way_type, coordinates, distance_meters, is_oneway)
VALUES ('Đường nội bộ', 'FOOTPATH', '[[105.845564, 21.0049304], [105.84556, 21.0048806], [105.845559, 21.0048618], [105.8455483, 21.0046709], [105.8455384, 21.0046027], [105.8455104, 21.0045311], [105.8454806, 21.0044595], [105.8454301, 21.0043745], [105.845376, 21.0042902], [105.8453011, 21.0042052], [105.8452064, 21.0041142], [105.8450774, 21.0039921], [105.8449114, 21.003859], [105.8448572, 21.0038], [105.8448139, 21.0037394], [105.8447625, 21.0036586], [105.8447102, 21.0035592], [105.8446895, 21.0035193], [105.8446642, 21.0034707], [105.8446263, 21.0033772], [105.8446037, 21.0033014], [105.8445911, 21.0032248], [105.8445803, 21.0031263], [105.8445776, 21.0030639], [105.8445786, 21.0030155], [105.8445818, 21.0028665], [105.8445953, 21.0027974], [105.8446232, 21.0026903], [105.8446436, 21.0025931], [105.8446586, 21.0024269], [105.8447187, 21.0021825], [105.8447702, 21.0020573], [105.8448185, 21.0019781], [105.8448357, 21.0019361], [105.8448389, 21.001902], [105.8448196, 21.001879]]', 376.8846501742103, false);
INSERT INTO campus_ways (name, way_type, coordinates, distance_meters, is_oneway)
VALUES ('Đường nội bộ', 'FOOTPATH', '[[105.845559, 21.0048618], [105.8456383, 21.0048584], [105.8457215, 21.0048549]]', 16.885920730695332, false);
INSERT INTO campus_ways (name, way_type, coordinates, distance_meters, is_oneway)
VALUES ('Đường nội bộ', 'FOOTPATH', '[[105.8448807, 21.0051284], [105.8453258, 21.0051241], [105.8453887, 21.0051234], [105.8455058, 21.0051205], [105.8455481, 21.0051208], [105.8455601, 21.0051299], [105.845565, 21.0051426], [105.8455703, 21.0051751], [105.8455766, 21.005437], [105.8455766, 21.0058421], [105.8455739, 21.0060965], [105.8455685, 21.0061512], [105.8455604, 21.00619], [105.8455315, 21.0062338], [105.8452131, 21.0065462], [105.8450958, 21.0066498], [105.8449857, 21.0067383], [105.8449072, 21.0067964], [105.844836, 21.0068452], [105.8447647, 21.006884], [105.844577, 21.0069775], [105.8443713, 21.0070819], [105.8442333, 21.0071493], [105.8441792, 21.007183], [105.8441097, 21.0072284], [105.8440303, 21.0072876], [105.8439518, 21.0073457], [105.843901, 21.0073947], [105.8438737, 21.0074457], [105.8438426, 21.0074959], [105.8438314, 21.0075114], [105.8438149, 21.0075341], [105.8437873, 21.0075492], [105.8437618, 21.0075523]]', 433.212898441955, false);
INSERT INTO campus_ways (name, way_type, coordinates, distance_meters, is_oneway)
VALUES ('Đường nội bộ', 'FOOTPATH', '[[105.8417113, 21.0073831], [105.8417678, 21.0074205], [105.8418418, 21.0074463], [105.8419385, 21.0074637], [105.842647, 21.007473], [105.8429886, 21.0074775], [105.843198, 21.0074889], [105.8434406, 21.0075136], [105.843562, 21.00753], [105.8437618, 21.0075523]]', 215.3543314957987, false);
INSERT INTO campus_ways (name, way_type, coordinates, distance_meters, is_oneway)
VALUES ('Đường nội bộ', 'FOOTPATH', '[[105.8438314, 21.0075114], [105.8438946, 21.0075286], [105.8439595, 21.0075462], [105.8439918, 21.0075471], [105.8440408, 21.0075355], [105.8441017, 21.0075179]]', 29.068348515291696, false);
INSERT INTO campus_ways (name, way_type, coordinates, distance_meters, is_oneway)
VALUES ('Đường nội bộ', 'FOOTPATH', '[[105.8441298, 21.0075647], [105.8441039, 21.0075269], [105.8441017, 21.0075179], [105.8440922, 21.0074788], [105.8441021, 21.0074258], [105.8441273, 21.0073828], [105.8441456, 21.0073629], [105.8441806, 21.0073247], [105.8442239, 21.0072843], [105.8443024, 21.0072287], [105.8445306, 21.0071328], [105.8445829, 21.0071108], [105.8446416, 21.0070973], [105.8447011, 21.0070847], [105.8447372, 21.0070712], [105.8447441, 21.0070683], [105.8447977, 21.0070459], [105.8448725, 21.0070064], [105.844914, 21.0069794], [105.8449235, 21.0069653], [105.8449321, 21.0069525], [105.8449543, 21.0069237], [105.8449718, 21.0069011], [105.8450674, 21.0068379], [105.8451414, 21.0067966], [105.8451919, 21.0067613], [105.8452087, 21.0067481], [105.8452497, 21.0067158], [105.8453525, 21.0066383], [105.8454698, 21.0065254], [105.8455411, 21.0064362], [105.8455844, 21.0063806], [105.8456232, 21.0063326], [105.8456361, 21.0063184], [105.8456493, 21.0063039], [105.8456737, 21.0062997], [105.8457026, 21.0063149], [105.8457318, 21.0063377]]', 234.77978398153596, false);
INSERT INTO campus_ways (name, way_type, coordinates, distance_meters, is_oneway)
VALUES ('Đường nội bộ', 'FOOTPATH', '[[105.8455315, 21.0062338], [105.8455644, 21.0062728], [105.8456361, 21.0063184]]', 14.525784437300604, false);
INSERT INTO campus_ways (name, way_type, coordinates, distance_meters, is_oneway)
VALUES ('Đường nội bộ', 'FOOTPATH', '[[105.8457869, 21.0063065], [105.8457785, 21.0062474], [105.8457664, 21.006183], [105.8457656, 21.0061129], [105.845755, 21.0056178], [105.8457536, 21.0055804], [105.8457467, 21.005399], [105.8457429, 21.0051808], [105.8457461, 21.0051422], [105.8457474, 21.0051256], [105.8457565, 21.0051036], [105.8457801, 21.0050852], [105.8457962, 21.0050822], [105.8458172, 21.0050781], [105.8458521, 21.005076]]', 144.90048384766445, false);
INSERT INTO campus_ways (name, way_type, coordinates, distance_meters, is_oneway)
VALUES ('Đường nội bộ', 'FOOTPATH', '[[105.845565, 21.0051426], [105.8456507, 21.0051424], [105.8457461, 21.0051422]]', 18.79928860542318, false);
INSERT INTO campus_ways (name, way_type, coordinates, distance_meters, is_oneway)
VALUES ('Đường nội bộ', 'ROAD', '[[105.8456508, 21.0055823], [105.8457536, 21.0055804], [105.8458061, 21.0055793], [105.8460233, 21.0055751]]', 38.67585855098761, false);
INSERT INTO campus_ways (name, way_type, coordinates, distance_meters, is_oneway)
VALUES ('Đường nội bộ', 'FOOTPATH', '[[105.8458521, 21.005076], [105.8463587, 21.0050749], [105.8464597, 21.0050737], [105.8465076, 21.0050731], [105.8465555, 21.0050725], [105.8467095, 21.0050606], [105.8468818, 21.0050308], [105.8469162, 21.0050236]]', 110.90940203386714, false);
INSERT INTO campus_ways (name, way_type, coordinates, distance_meters, is_oneway)
VALUES ('Đường nội bộ', 'FOOTPATH', '[[105.8469162, 21.0050236], [105.8469354, 21.0050249], [105.8469481, 21.0050332], [105.8469711, 21.0050689], [105.8470642, 21.0052797], [105.8471216, 21.0054381], [105.8471382, 21.0055072], [105.8471612, 21.0056621], [105.8471944, 21.0058586], [105.8472211, 21.0060254], [105.8472263, 21.006098], [105.8472263, 21.0061778], [105.8472263, 21.0062802], [105.8472255, 21.0063324], [105.8472207, 21.0066352], [105.8472182, 21.0066852], [105.847211, 21.0067121], [105.8471899, 21.0067344], [105.8471637, 21.0067469], [105.8471344, 21.0067513], [105.8471099, 21.0067495], [105.8470757, 21.0067429]]', 210.42165254984127, false);
INSERT INTO campus_ways (name, way_type, coordinates, distance_meters, is_oneway)
VALUES ('Đường nội bộ', 'FOOTPATH', '[[105.8446268, 21.0040021], [105.8446288, 21.0040931], [105.8446321, 21.0042527], [105.8446322, 21.0042712], [105.8446323, 21.0042869], [105.8446393, 21.0048648], [105.8446434, 21.0048951], [105.8446559, 21.0049166], [105.8446779, 21.0049369], [105.8447144, 21.0049465], [105.8453602, 21.0049424]]', 176.24045645749993, false);
INSERT INTO campus_ways (name, way_type, coordinates, distance_meters, is_oneway)
VALUES ('Đường nội bộ', 'ROAD', '[[105.8454443, 21.0049845], [105.8454436, 21.0049418], [105.8454422, 21.0048835], [105.8454352, 21.004627], [105.8454322, 21.0045199]]', 51.67667362944451, false);
INSERT INTO campus_ways (name, way_type, coordinates, distance_meters, is_oneway)
VALUES ('Đường nội bộ', 'FOOTPATH', '[[105.8455481, 21.0051208], [105.8455476, 21.0050687], [105.8455468, 21.004984], [105.8455464, 21.0049425]]', 19.826840842225273, false);
INSERT INTO campus_ways (name, way_type, coordinates, distance_meters, is_oneway)
VALUES ('Đường nội bộ', 'FOOTPATH', '[[105.8457917, 21.0049572], [105.8457944, 21.0050316], [105.8457962, 21.0050822]]', 13.907213905607337, false);
INSERT INTO campus_ways (name, way_type, coordinates, distance_meters, is_oneway)
VALUES ('Đường nội bộ', 'FOOTPATH', '[[105.8448196, 21.001879], [105.8447904, 21.0018788], [105.8446911, 21.0018782], [105.8442143, 21.0018869], [105.8440378, 21.0018901], [105.8436795, 21.0018901], [105.8433733, 21.0018901], [105.8433344, 21.0018901], [105.8431702, 21.0018901], [105.8429154, 21.0018901], [105.8428359, 21.0018901], [105.8422267, 21.0018914], [105.8420859, 21.0019072], [105.8417819, 21.0019066], [105.8417477, 21.0019133], [105.8416908, 21.0019605], [105.841553, 21.0020875], [105.8415474, 21.0020927], [105.8415284, 21.0021171], [105.8415175, 21.0021607]]', 356.23921494144366, false);
INSERT INTO campus_ways (name, way_type, coordinates, distance_meters, is_oneway)
VALUES ('Đường nội bộ', 'FOOTPATH', '[[105.8474619, 21.0071102], [105.8474168, 21.0070286], [105.8473823, 21.0069441], [105.8473236, 21.006819], [105.847316, 21.0067785], [105.8473109, 21.0067023], [105.8473109, 21.0064986], [105.8473109, 21.0064838], [105.8473109, 21.0063581], [105.8473106, 21.0063321], [105.8473071, 21.0060198], [105.8472907, 21.0058901], [105.8472803, 21.0058078], [105.8472484, 21.0056077], [105.8472194, 21.0054987], [105.8472024, 21.005435], [105.8471616, 21.005317], [105.8470812, 21.0051396], [105.8470404, 21.0050395], [105.84702, 21.0049764], [105.8470162, 21.0049335], [105.8470353, 21.0049049], [105.8472177, 21.004606], [105.8472541, 21.0045563], [105.8472739, 21.0045021], [105.847275, 21.0044509], [105.8472768, 21.0043892], [105.8472719, 21.0042242], [105.8472776, 21.0041403], [105.8472892, 21.0040759], [105.8473096, 21.0040354], [105.8473594, 21.0039711], [105.8477587, 21.0035828], [105.8478256, 21.0035149], [105.8478687, 21.0034711], [105.8478929, 21.0034546], [105.8479223, 21.0034428], [105.8479471, 21.0034377], [105.8479981, 21.0034361]]', 457.1298793109409, false);
INSERT INTO campus_ways (name, way_type, coordinates, distance_meters, is_oneway)
VALUES ('Đường nội bộ', 'FOOTPATH', '[[105.847151, 21.0041568], [105.8471516, 21.0041215], [105.8471538, 21.0040976], [105.8471585, 21.0040692], [105.8471612, 21.0040525], [105.8471707, 21.0040098], [105.8471772, 21.00399]]', 18.818507305135814, false);
INSERT INTO campus_ways (name, way_type, coordinates, distance_meters, is_oneway)
VALUES ('Đường nội bộ', 'FOOTPATH', '[[105.8479981, 21.0034361], [105.8479936, 21.0033961], [105.8479874, 21.0033395]]', 10.798715719781335, false);
INSERT INTO campus_ways (name, way_type, coordinates, distance_meters, is_oneway)
VALUES ('Đường nội bộ', 'FOOTPATH', '[[105.8479021, 21.0032743], [105.8479353, 21.003302], [105.8479874, 21.0033395]]', 11.45136203398564, false);
INSERT INTO campus_ways (name, way_type, coordinates, distance_meters, is_oneway)
VALUES ('Đường nội bộ', 'FOOTPATH', '[[105.8479874, 21.0033395], [105.8480073, 21.0033171], [105.8480272, 21.0032947], [105.8480678, 21.0032488], [105.848111, 21.0032002]]', 20.113352391642096, false);
INSERT INTO campus_ways (name, way_type, coordinates, distance_meters, is_oneway)
VALUES ('Đường nội bộ', 'FOOTPATH', '[[105.8480595, 21.0031234], [105.8480406, 21.0031455], [105.848027, 21.0031542], [105.8480159, 21.0031559], [105.8480013, 21.0031515], [105.8479862, 21.0031409], [105.8479323, 21.0030977], [105.8477882, 21.0029774], [105.8474206, 21.0026475], [105.8470136, 21.0023032], [105.8469174, 21.0022218], [105.846778, 21.0021091], [105.8466481, 21.002004], [105.8465757, 21.0019414], [105.8464679, 21.0018488], [105.8464334, 21.0018194], [105.8464121, 21.0018012], [105.8463987, 21.0017841], [105.8463944, 21.0017661], [105.8464, 21.0017554], [105.8464056, 21.0017446], [105.8464917, 21.0016563]]', 251.07098649898938, false);
INSERT INTO campus_ways (name, way_type, coordinates, distance_meters, is_oneway)
VALUES ('Đường nội bộ', 'FOOTPATH', '[[105.8446895, 21.0035193], [105.8447504, 21.0034945], [105.8448223, 21.0034653]]', 15.036496839477785, false);
INSERT INTO campus_ways (name, way_type, coordinates, distance_meters, is_oneway)
VALUES ('Đường nội bộ', 'FOOTPATH', '[[105.8411713, 21.0041133], [105.8411702, 21.0040433], [105.84116, 21.0035656], [105.841157, 21.0034058], [105.8411558, 21.0033442], [105.8411472, 21.0028879], [105.8411306, 21.0020684], [105.841133, 21.001924], [105.8411356, 21.001763], [105.8411383, 21.0015931], [105.8411329, 21.0014396], [105.8411237, 21.0013632], [105.8411224, 21.0013191], [105.8411364, 21.0010737], [105.8411441, 21.0009927], [105.8411556, 21.0008355], [105.8411569, 21.0007021], [105.8411594, 21.0004794], [105.8411561, 21.0003815], [105.8411505, 21.0002149], [105.8411446, 21.0000611], [105.841139, 20.9999171], [105.8411275, 20.9997111], [105.8411122, 20.999449], [105.8411033, 20.999237], [105.8411003, 20.9991451], [105.8410956, 20.9989976], [105.8410905, 20.9986336]]', 609.6855383768566, false);
INSERT INTO campus_ways (name, way_type, coordinates, distance_meters, is_oneway)
VALUES ('Đường nội bộ', 'FOOTPATH', '[[105.8360685, 21.0035091], [105.8360842, 21.0034913], [105.8360927, 21.0034894], [105.8361103, 21.0034853], [105.836141, 21.0034835], [105.8364768, 21.0035767], [105.8368564, 21.0036772], [105.8368644, 21.0036759], [105.8370328, 21.0037197], [105.8371374, 21.0037414], [105.8374939, 21.0038337], [105.8377168, 21.0038914], [105.8377985, 21.0039119], [105.8381438, 21.0039985], [105.8385198, 21.0040958], [105.8385814, 21.0041118], [105.8386083, 21.0041187], [105.839028, 21.0042038], [105.8392306, 21.0042449], [105.8392854, 21.0042505], [105.8393583, 21.004258], [105.8400074, 21.004266], [105.8401135, 21.0042634], [105.840191, 21.0042615], [105.8403389, 21.004258], [105.8408689, 21.0042509], [105.8410694, 21.0042481], [105.8411275, 21.0042479], [105.8411378, 21.0042493], [105.8411557, 21.0042554], [105.8411729, 21.0042701]]', 542.9350065484628, false);
INSERT INTO campus_ways (name, way_type, coordinates, distance_meters, is_oneway)
VALUES ('Đường nội bộ', 'FOOTPATH', '[[105.8401134, 21.0041593], [105.8401891, 21.0041583], [105.8404115, 21.0041551], [105.8407848, 21.0041508], [105.8408862, 21.0041491], [105.841111, 21.0041452], [105.8411359, 21.0041402], [105.8411565, 21.0041282], [105.8411713, 21.0041133]]', 110.99355686155035, false);
INSERT INTO campus_ways (name, way_type, coordinates, distance_meters, is_oneway)
VALUES ('Đường nội bộ', 'FOOTPATH', '[[105.8401891, 21.0041583], [105.8401901, 21.0042094], [105.840191, 21.0042615]]', 11.477017853134683, false);
INSERT INTO campus_ways (name, way_type, coordinates, distance_meters, is_oneway)
VALUES ('Đường nội bộ', 'FOOTPATH', '[[105.8409587, 21.0073632], [105.8410766, 21.0072478], [105.8411515, 21.0071763], [105.8411958, 21.0071277], [105.8412254, 21.0070819], [105.8412345, 21.0070322], [105.8412331, 21.006961], [105.84123, 21.0068562], [105.8412155, 21.0063778], [105.8412184, 21.0063239], [105.8412119, 21.0059601], [105.8411919, 21.0053487], [105.8411848, 21.0051708], [105.8411766, 21.0047472], [105.8411812, 21.0044353], [105.8411729, 21.0042701]]', 354.7511884418072, false);
INSERT INTO campus_ways (name, way_type, coordinates, distance_meters, is_oneway)
VALUES ('Đường nội bộ', 'FOOTPATH', '[[105.8411729, 21.0042701], [105.8411726, 21.0041914], [105.8411713, 21.0041133]]', 17.436468349697698, false);
INSERT INTO campus_ways (name, way_type, coordinates, distance_meters, is_oneway)
VALUES ('Phố Phương Mai', 'ROAD', '[[105.8392955, 21.0041049], [105.8397711, 21.0041176], [105.8399124, 21.0041189], [105.8399456, 21.0041192], [105.8400186, 21.0041402], [105.8400424, 21.0041585], [105.840113, 21.0042129]]', 88.13857969643892, false);
INSERT INTO campus_ways (name, way_type, coordinates, distance_meters, is_oneway)
VALUES ('Đường nội bộ', 'FOOTPATH', '[[105.8392514, 21.0041274], [105.839297, 21.0041382], [105.83936, 21.0041519], [105.8395036, 21.0041566], [105.8395543, 21.0041583], [105.8396309, 21.004159], [105.839771, 21.0041588], [105.8400424, 21.0041585], [105.8401134, 21.0041593]]', 89.81976482026526, false);
INSERT INTO campus_ways (name, way_type, coordinates, distance_meters, is_oneway)
VALUES ('Đường nội bộ', 'FOOTPATH', '[[105.8404297, 21.0077032], [105.8404788, 21.0076907], [105.8405241, 21.0076764], [105.8405787, 21.0076559], [105.8406806, 21.0076072], [105.8408471, 21.0074581], [105.8409587, 21.0073632]]', 67.8587499945801, false);
INSERT INTO campus_ways (name, way_type, coordinates, distance_meters, is_oneway)
VALUES ('Đường nội bộ', 'FOOTPATH', '[[105.8390048, 21.0079504], [105.8394989, 21.0078831], [105.8395554, 21.0078526], [105.8396795, 21.0078302], [105.8400075, 21.0077878], [105.8400969, 21.0077763]]', 115.46846489745795, false);
INSERT INTO campus_ways (name, way_type, coordinates, distance_meters, is_oneway)
VALUES ('Đường nội bộ', 'FOOTPATH', '[[105.8398974, 21.0079252], [105.8400025, 21.0078489], [105.8400969, 21.0077763]]', 26.516571610827505, false);
INSERT INTO campus_ways (name, way_type, coordinates, distance_meters, is_oneway)
VALUES ('Đường nội bộ', 'FOOTPATH', '[[105.8508757, 21.0033912], [105.8507947, 21.0033314], [105.8507406, 21.0032914], [105.8507194, 21.0032836], [105.8506822, 21.0032843], [105.8506257, 21.0032921], [105.8503902, 21.0033247], [105.8501844, 21.0033663], [105.8501361, 21.0033799], [105.8500988, 21.0033902], [105.8500784, 21.0033937], [105.8500646, 21.0033957], [105.8500351, 21.0034001], [105.8499754, 21.0034054], [105.8498741, 21.0034098], [105.8497574, 21.0034118], [105.849102, 21.0034181], [105.8489113, 21.0034241], [105.8483942, 21.0034401], [105.8482496, 21.0034402], [105.8481605, 21.0034402], [105.8479981, 21.0034361]]', 304.1385461356719, false);
INSERT INTO campus_ways (name, way_type, coordinates, distance_meters, is_oneway)
VALUES ('Ngõ 15 Phố Tạ Quang Bửu', 'ROAD', '[[105.8479475, 21.0057751], [105.8480363, 21.0058799]]', 14.858253406768, false);
INSERT INTO campus_ways (name, way_type, coordinates, distance_meters, is_oneway)
VALUES ('Đường nội bộ', 'ROAD', '[[105.8463298, 21.0070863], [105.8463137, 21.0071662], [105.8463019, 21.0071643], [105.8462882, 21.0072404], [105.8461935, 21.0072264], [105.8460816, 21.0072099]]', 40.57636848508648, false);
INSERT INTO campus_ways (name, way_type, coordinates, distance_meters, is_oneway)
VALUES ('Đường nội bộ', 'ROAD', '[[105.8453888, 21.0058448], [105.8453887, 21.0051234], [105.8453887, 21.0050705]]', 86.09823237251908, false);
INSERT INTO campus_ways (name, way_type, coordinates, distance_meters, is_oneway)
VALUES ('Đường nội bộ', 'ROAD', '[[105.8409157, 21.0016568], [105.8409163, 21.0018577], [105.8409165, 21.0019227], [105.8409174, 21.0020422], [105.8409279, 21.0030387]]', 153.66607508124116, false);
INSERT INTO campus_ways (name, way_type, coordinates, distance_meters, is_oneway)
VALUES ('Ngõ 17 Phố Tạ Quang Bửu', 'ROAD', '[[105.8482186, 21.0042947], [105.8481728, 21.004042], [105.8480902, 21.0038292], [105.8480726, 21.0037872], [105.8479291, 21.0036156], [105.8478256, 21.0035149], [105.8477737, 21.0034643]]', 106.19635807840902, false);
INSERT INTO campus_ways (name, way_type, coordinates, distance_meters, is_oneway)
VALUES ('Đường nội bộ', 'ROAD', '[[105.8428015, 21.0067671], [105.8427965, 21.0064536], [105.8427964, 21.006444], [105.8427899, 21.0060343], [105.8427897, 21.0060218], [105.8427846, 21.0057241]]', 115.98960717512153, false);
INSERT INTO campus_ways (name, way_type, coordinates, distance_meters, is_oneway)
VALUES ('Đường nội bộ', 'ROAD', '[[105.8427846, 21.0057241], [105.8431182, 21.0057207], [105.8432299, 21.0057196], [105.8434555, 21.0057173]]', 69.64716675778241, false);
INSERT INTO campus_ways (name, way_type, coordinates, distance_meters, is_oneway)
VALUES ('Đường nội bộ', 'ROAD', '[[105.8434678, 21.0067603], [105.8433928, 21.0067611], [105.8431337, 21.0067637], [105.8431076, 21.006764], [105.8428015, 21.0067671]]', 69.16921010877876, false);
INSERT INTO campus_ways (name, way_type, coordinates, distance_meters, is_oneway)
VALUES ('Đường nội bộ', 'FOOTPATH', '[[105.8427964, 21.006444], [105.8428263, 21.0064437], [105.8428361, 21.0064436], [105.8428481, 21.0064435], [105.8431292, 21.0064404], [105.8434068, 21.0064374], [105.843426, 21.0064372], [105.8434413, 21.006437], [105.8434635, 21.0064368]]', 69.25291035053417, false);
INSERT INTO campus_ways (name, way_type, coordinates, distance_meters, is_oneway)
VALUES ('Đường nội bộ', 'FOOTPATH', '[[105.8431337, 21.0067637], [105.843133, 21.0067141], [105.8431327, 21.0066932], [105.8431322, 21.0066549], [105.8431292, 21.0064404], [105.8431277, 21.0063338]]', 47.8067580673708, false);
INSERT INTO campus_ways (name, way_type, coordinates, distance_meters, is_oneway)
VALUES ('Đường nội bộ', 'FOOTPATH', '[[105.8432203, 21.0062863], [105.843196, 21.0063119], [105.8431639, 21.0063284], [105.8431277, 21.0063338], [105.8430944, 21.0063283], [105.8430645, 21.0063134], [105.8430412, 21.0062904], [105.8430267, 21.0062619], [105.8430225, 21.0062305], [105.8430291, 21.0061995], [105.8430457, 21.0061719], [105.8430707, 21.0061506], [105.8431016, 21.0061377], [105.843124, 21.0061355], [105.8431353, 21.0061344], [105.843171, 21.0061422], [105.8432018, 21.0061607], [105.843224, 21.0061879], [105.8432352, 21.0062204], [105.8432339, 21.0062546], [105.8432203, 21.0062863]]', 69.37582908321117, false);
INSERT INTO campus_ways (name, way_type, coordinates, distance_meters, is_oneway)
VALUES ('Đường nội bộ', 'FOOTPATH', '[[105.843124, 21.0061355], [105.8431228, 21.0060318], [105.843122, 21.0059574]]', 19.804906040205033, false);
INSERT INTO campus_ways (name, way_type, coordinates, distance_meters, is_oneway)
VALUES ('Đường nội bộ', 'FOOTPATH', '[[105.8430919, 21.0058538], [105.8431194, 21.0057992], [105.8431481, 21.0058529]]', 13.381996816406456, false);
INSERT INTO campus_ways (name, way_type, coordinates, distance_meters, is_oneway)
VALUES ('Đường nội bộ', 'FOOTPATH', '[[105.8431489, 21.0058962], [105.843122, 21.0059574], [105.8430927, 21.0058971]]', 14.718389264771593, false);
INSERT INTO campus_ways (name, way_type, coordinates, distance_meters, is_oneway)
VALUES ('Đường nội bộ', 'FOOTPATH', '[[105.8431481, 21.0058529], [105.8431489, 21.0058962]]', 4.815456440598493, false);
INSERT INTO campus_ways (name, way_type, coordinates, distance_meters, is_oneway)
VALUES ('Đường nội bộ', 'FOOTPATH', '[[105.8430927, 21.0058971], [105.8430919, 21.0058538]]', 4.8154564405644145, false);
INSERT INTO campus_ways (name, way_type, coordinates, distance_meters, is_oneway)
VALUES ('Đường nội bộ', 'FOOTPATH', '[[105.8431194, 21.0057992], [105.8431192, 21.0057854], [105.8431189, 21.0057678], [105.8431182, 21.0057207]]', 8.729693993956325, false);
INSERT INTO campus_ways (name, way_type, coordinates, distance_meters, is_oneway)
VALUES ('Đường nội bộ', 'FOOTPATH', '[[105.8427899, 21.0060343], [105.8428197, 21.0060341], [105.8428296, 21.006034], [105.8428415, 21.0060339], [105.8431228, 21.0060318], [105.8434004, 21.0060297], [105.8434205, 21.0060295], [105.8434345, 21.0060294], [105.8434592, 21.0060293]]', 69.47908304459459, false);
INSERT INTO campus_ways (name, way_type, coordinates, distance_meters, is_oneway)
VALUES ('Đường nội bộ', 'ROAD', '[[105.8434592, 21.0052089], [105.8434509, 21.005221], [105.8433835, 21.0053169], [105.8432239, 21.0055493], [105.8431182, 21.0057207]]', 67.03962970636695, false);
INSERT INTO campus_ways (name, way_type, coordinates, distance_meters, is_oneway)
VALUES ('Đường nội bộ', 'FOOTPATH', '[[105.8444137, 21.004928], [105.844412, 21.0048783], [105.8444103, 21.0048308], [105.8445085, 21.0048277], [105.8445299, 21.0048271]]', 23.23594238537959, false);
INSERT INTO campus_ways (name, way_type, coordinates, distance_meters, is_oneway)
VALUES ('Đường nội bộ', 'FOOTPATH', '[[105.8437666, 21.0048377], [105.8438013, 21.0048365], [105.8438826, 21.0048345], [105.8438832, 21.004884], [105.8438842, 21.0049346]]', 23.178823283991562, false);
INSERT INTO campus_ways (name, way_type, coordinates, distance_meters, is_oneway)
VALUES ('Đường nội bộ', 'FOOTPATH', '[[105.8445313, 21.0048695], [105.8445456, 21.0048689], [105.8445875, 21.0048672], [105.8446224, 21.0048656], [105.8446393, 21.0048648]]', 11.223274391629198, false);
INSERT INTO campus_ways (name, way_type, coordinates, distance_meters, is_oneway)
VALUES ('Đường nội bộ', 'FOOTPATH', '[[105.8437674, 21.0048665], [105.8437493, 21.0048667], [105.8437194, 21.004867], [105.8436817, 21.0048675], [105.8436622, 21.0048677]]', 10.921221410305371, false);
INSERT INTO campus_ways (name, way_type, coordinates, distance_meters, is_oneway)
VALUES ('Đường nội bộ', 'ALLEY', '[[105.8429537, 21.0069673], [105.8429523, 21.0068098], [105.8431076, 21.0068086], [105.8432513, 21.0068078], [105.8432521, 21.0069617], [105.8431943, 21.0069616], [105.8431941, 21.0069302], [105.8431091, 21.0069306], [105.8430138, 21.0069311], [105.843014, 21.0069671], [105.8429537, 21.0069673]]', 104.13557613191882, false);
INSERT INTO campus_ways (name, way_type, coordinates, distance_meters, is_oneway)
VALUES ('Đường nội bộ', 'FOOTPATH', '[[105.8431091, 21.0069306], [105.8431076, 21.0068433]]', 9.70856579787252, false);
INSERT INTO campus_ways (name, way_type, coordinates, distance_meters, is_oneway)
VALUES ('Đường nội bộ', 'ALLEY', '[[105.8431076, 21.0068433], [105.8431076, 21.0068086]]', 3.8584639546215875, false);
INSERT INTO campus_ways (name, way_type, coordinates, distance_meters, is_oneway)
VALUES ('Đường nội bộ', 'FOOTPATH', '[[105.8431076, 21.0068086], [105.8431076, 21.0067971], [105.8431076, 21.006764]]', 4.95929372823068, false);
INSERT INTO campus_ways (name, way_type, coordinates, distance_meters, is_oneway)
VALUES ('Đường nội bộ', 'ALLEY', '[[105.8424851, 21.0065544], [105.8425372, 21.0065536], [105.8425364, 21.0065007], [105.8427392, 21.0064979], [105.8427385, 21.0064544], [105.8427378, 21.0064083], [105.84253, 21.0064111], [105.8425254, 21.006114], [105.8424779, 21.0061146], [105.8424801, 21.0062348], [105.8424843, 21.0064578], [105.8424851, 21.0065544]]', 150.76302907918853, false);
INSERT INTO campus_ways (name, way_type, coordinates, distance_meters, is_oneway)
VALUES ('Đường nội bộ', 'ALLEY', '[[105.8424843, 21.0064578], [105.8425313, 21.0064572]]', 4.879287166281572, false);
INSERT INTO campus_ways (name, way_type, coordinates, distance_meters, is_oneway)
VALUES ('Đường nội bộ', 'FOOTPATH', '[[105.8425313, 21.0064572], [105.8427385, 21.0064544], [105.8427493, 21.0064543], [105.8427965, 21.0064536]]', 27.531990614198246, false);
INSERT INTO campus_ways (name, way_type, coordinates, distance_meters, is_oneway)
VALUES ('Đường nội bộ', 'FOOTPATH', '[[105.8424774, 21.0060257], [105.8427291, 21.0060226], [105.842745, 21.0060224], [105.8427897, 21.0060218]]', 32.42127244440553, false);
INSERT INTO campus_ways (name, way_type, coordinates, distance_meters, is_oneway)
VALUES ('Đường nội bộ', 'FOOTPATH', '[[105.8421247, 21.00603], [105.8424134, 21.0060265]]', 29.971092166676023, false);
INSERT INTO campus_ways (name, way_type, coordinates, distance_meters, is_oneway)
VALUES ('Đường nội bộ', 'FOOTPATH', '[[105.8424134, 21.0060265], [105.8424774, 21.0060257]]', 6.64412931133617, false);
INSERT INTO campus_ways (name, way_type, coordinates, distance_meters, is_oneway)
VALUES ('Đường nội bộ', 'ROAD', '[[105.8417194, 21.0055583], [105.8424321, 21.0055552], [105.8429089, 21.0055513], [105.8429498, 21.005551]]', 127.72517391842446, false);
INSERT INTO campus_ways (name, way_type, coordinates, distance_meters, is_oneway)
VALUES ('Đường nội bộ', 'ROAD', '[[105.8417184, 21.0054459], [105.842431, 21.0054392], [105.8427713, 21.005436], [105.8428094, 21.0054356]]', 113.2577279038474, false);
INSERT INTO campus_ways (name, way_type, coordinates, distance_meters, is_oneway)
VALUES ('Đường nội bộ', 'ROAD', '[[105.8417176, 21.005349], [105.8424302, 21.0053415], [105.8426558, 21.0053391], [105.8426947, 21.0053387]]', 101.43500604882578, false);
INSERT INTO campus_ways (name, way_type, coordinates, distance_meters, is_oneway)
VALUES ('Đường nội bộ', 'ROAD', '[[105.8417168, 21.0052374], [105.8424292, 21.0052328], [105.8425287, 21.0052322], [105.8425682, 21.005232]]', 88.38227381982026, false);
INSERT INTO campus_ways (name, way_type, coordinates, distance_meters, is_oneway)
VALUES ('Đường nội bộ', 'ROAD', '[[105.8424337, 21.0056797], [105.8424334, 21.0056589], [105.8424321, 21.0055552], [105.842431, 21.0054392], [105.8424302, 21.0053415], [105.8424292, 21.0052328]]', 49.6952805439703, false);
INSERT INTO campus_ways (name, way_type, coordinates, distance_meters, is_oneway)
VALUES ('Đường nội bộ', 'ROAD', '[[105.8424411, 21.0050533], [105.8424412, 21.0051247]]', 7.939324548624146, false);
INSERT INTO campus_ways (name, way_type, coordinates, distance_meters, is_oneway)
VALUES ('Đường nội bộ', 'ROAD', '[[105.8424412, 21.0051247], [105.8418865, 21.0051257], [105.8417746, 21.0050964]]', 69.64533604413737, false);
INSERT INTO campus_ways (name, way_type, coordinates, distance_meters, is_oneway)
VALUES ('Đường nội bộ', 'ROAD', '[[105.8454322, 21.0045199], [105.8454087, 21.004462], [105.8453609, 21.0043763], [105.8452851, 21.0042661], [105.8452752, 21.0042517], [105.8452403, 21.0042177], [105.8448116, 21.0038692]]', 98.33931682796032, false);
INSERT INTO campus_ways (name, way_type, coordinates, distance_meters, is_oneway)
VALUES ('Đường nội bộ', 'FOOTPATH', '[[105.843397, 21.006961], [105.8433935, 21.0067942], [105.8433928, 21.0067611]]', 22.23214113289398, false);
INSERT INTO campus_ways (name, way_type, coordinates, distance_meters, is_oneway)
VALUES ('Đường nội bộ', 'FOOTPATH', '[[105.8428015, 21.0067671], [105.8428017, 21.0068001], [105.8428025, 21.006968]]', 22.33930418394625, false);
INSERT INTO campus_ways (name, way_type, coordinates, distance_meters, is_oneway)
VALUES ('Đường nội bộ', 'FOOTPATH', '[[105.8421485, 21.0061792], [105.8421263, 21.0061794], [105.8419627, 21.0061805], [105.841926, 21.0061807], [105.8418924, 21.0061809], [105.8418143, 21.0061814]]', 34.69254133514497, false);
INSERT INTO campus_ways (name, way_type, coordinates, distance_meters, is_oneway)
VALUES ('Đường nội bộ', 'FOOTPATH', '[[105.8421263, 21.0061794], [105.8421247, 21.00603], [105.8421237, 21.0059428], [105.8421231, 21.00589]]', 32.181528002807255, false);
INSERT INTO campus_ways (name, way_type, coordinates, distance_meters, is_oneway)
VALUES ('Đường nội bộ', 'FOOTPATH', '[[105.84178, 21.0059438], [105.8419589, 21.0059436], [105.8421237, 21.0059428]]', 35.67811687915994, false);
INSERT INTO campus_ways (name, way_type, coordinates, distance_meters, is_oneway)
VALUES ('Đường nội bộ', 'FOOTPATH', '[[105.8419589, 21.0059436], [105.8419627, 21.0061805], [105.8419663, 21.00633]]', 42.97287273362596, false);
INSERT INTO campus_ways (name, way_type, coordinates, distance_meters, is_oneway)
VALUES ('Đường nội bộ', 'FOOTPATH', '[[105.8418948, 21.0063311], [105.8418924, 21.0061809]]', 16.703336002252097, false);
INSERT INTO campus_ways (name, way_type, coordinates, distance_meters, is_oneway)
VALUES ('Đường nội bộ', 'FOOTPATH', '[[105.8421528, 21.0064433], [105.8419684, 21.006447], [105.8418966, 21.0064484], [105.8418362, 21.0064496], [105.8418212, 21.0064499]]', 34.42953591574239, false);
INSERT INTO campus_ways (name, way_type, coordinates, distance_meters, is_oneway)
VALUES ('Đường nội bộ', 'FOOTPATH', '[[105.8421574, 21.0067199], [105.8419733, 21.0067221], [105.8419019, 21.0067229], [105.8418282, 21.0067238], [105.841816, 21.0067239], [105.8417935, 21.0067242]]', 37.77758269758349, false);
INSERT INTO campus_ways (name, way_type, coordinates, distance_meters, is_oneway)
VALUES ('Đường nội bộ', 'FOOTPATH', '[[105.8418948, 21.0063311], [105.8418966, 21.0064484], [105.8418985, 21.0065705], [105.8419019, 21.0067229]]', 43.57261791836573, false);
INSERT INTO campus_ways (name, way_type, coordinates, distance_meters, is_oneway)
VALUES ('Đường nội bộ', 'FOOTPATH', '[[105.8419663, 21.00633], [105.8419684, 21.006447], [105.8419707, 21.0065694], [105.8419733, 21.0067221]]', 43.60559570900894, false);
INSERT INTO campus_ways (name, way_type, coordinates, distance_meters, is_oneway)
VALUES ('Đường nội bộ', 'FOOTPATH', '[[105.8436782, 21.0059748], [105.843498, 21.0059756], [105.8434586, 21.0059758]]', 22.795905034622077, false);
INSERT INTO campus_ways (name, way_type, coordinates, distance_meters, is_oneway)
VALUES ('Đường nội bộ', 'FOOTPATH', '[[105.8436726, 21.0063664], [105.8435016, 21.0063688], [105.8434628, 21.0063694]]', 21.780842805930135, false);
INSERT INTO campus_ways (name, way_type, coordinates, distance_meters, is_oneway)
VALUES ('Đường nội bộ', 'ROAD', '[[105.8437452, 21.0067592], [105.8439968, 21.0067582], [105.8441928, 21.0067574], [105.8444769, 21.0067563], [105.8445248, 21.0067561]]', 80.9268836949488, false);
INSERT INTO campus_ways (name, way_type, coordinates, distance_meters, is_oneway)
VALUES ('Đường nội bộ', 'ROAD', '[[105.8460475, 21.0020266], [105.8458932, 21.0018862], [105.8458159, 21.0018159]]', 33.569668608251256, false);
INSERT INTO campus_ways (name, way_type, coordinates, distance_meters, is_oneway)
VALUES ('Ngách 106/17 Phố Lê Thanh Nghị', 'ROAD', '[[105.8451755, 21.0021633], [105.8455782, 21.0025068]]', 56.625230460476914, false);
INSERT INTO campus_ways (name, way_type, coordinates, distance_meters, is_oneway)
VALUES ('Đường nội bộ', 'FOOTPATH', '[[105.8415799, 21.0050212], [105.8416152, 21.0050208], [105.8423412, 21.0050122], [105.8423729, 21.0050117]]', 82.32489911978385, false);
INSERT INTO campus_ways (name, way_type, coordinates, distance_meters, is_oneway)
VALUES ('Đường nội bộ', 'FOOTPATH', '[[105.8437598, 21.0045851], [105.843764, 21.0047408], [105.8437666, 21.0048377], [105.8437674, 21.0048665], [105.8437681, 21.0048922], [105.8437749, 21.0049106], [105.8437901, 21.0049283], [105.8438167, 21.0049354], [105.8438842, 21.0049346], [105.8441609, 21.0049311], [105.8444137, 21.004928], [105.8445025, 21.0049269], [105.8445245, 21.0049177], [105.844529, 21.0049], [105.8445313, 21.0048695], [105.8445299, 21.0048271], [105.8445268, 21.0047301], [105.8445206, 21.004541]]', 157.38347865185298, false);
INSERT INTO campus_ways (name, way_type, coordinates, distance_meters, is_oneway)
VALUES ('Đường nội bộ', 'FOOTPATH', '[[105.843764, 21.0047408], [105.8441575, 21.0047353]]', 40.85228281611405, false);
INSERT INTO campus_ways (name, way_type, coordinates, distance_meters, is_oneway)
VALUES ('Đường nội bộ', 'FOOTPATH', '[[105.8441609, 21.0049311], [105.8441575, 21.0047353]]', 21.774827169901553, false);
INSERT INTO campus_ways (name, way_type, coordinates, distance_meters, is_oneway)
VALUES ('Đường nội bộ', 'FOOTPATH', '[[105.8430567, 21.0049974], [105.8435533, 21.0049871], [105.8435887, 21.0049771], [105.8436177, 21.0049611], [105.8436359, 21.004943], [105.843652, 21.004919], [105.8436628, 21.0048869], [105.8436622, 21.0048677], [105.8436509, 21.0044734], [105.8436502, 21.0044482]]', 117.34810964478986, false);
INSERT INTO campus_ways (name, way_type, coordinates, distance_meters, is_oneway)
VALUES ('Đường nội bộ', 'FOOTPATH', '[[105.8423308, 21.0041431], [105.8423568, 21.0041427], [105.8423707, 21.0041423], [105.8429835, 21.00413], [105.8430236, 21.0041295]]', 71.93328848061978, false);
INSERT INTO campus_ways (name, way_type, coordinates, distance_meters, is_oneway)
VALUES ('Đường nội bộ', 'ROAD', '[[105.8435723, 21.0050527], [105.8434592, 21.0052089]]', 20.964442656456892, false);
INSERT INTO campus_ways (name, way_type, coordinates, distance_meters, is_oneway)
VALUES ('Đường nội bộ', 'ROAD', '[[105.8425337, 21.0052028], [105.8424412, 21.0051247]]', 12.946683415480308, false);
INSERT INTO campus_ways (name, way_type, coordinates, distance_meters, is_oneway)
VALUES ('Đường nội bộ', 'FOOTPATH', '[[105.8426518, 21.0073086], [105.8427748, 21.007389], [105.8430419, 21.0073931], [105.8432613, 21.0073952], [105.8434444, 21.0073969], [105.8435659, 21.0073094]]', 101.02888912431818, false);
INSERT INTO campus_ways (name, way_type, coordinates, distance_meters, is_oneway)
VALUES ('Đường nội bộ', 'FOOTPATH', '[[105.8430419, 21.0073931], [105.8430387, 21.0072378]]', 17.271766617912302, false);
INSERT INTO campus_ways (name, way_type, coordinates, distance_meters, is_oneway)
VALUES ('Đường nội bộ', 'FOOTPATH', '[[105.8432613, 21.0073952], [105.8432604, 21.0072358]]', 17.724717519458068, false);
INSERT INTO campus_ways (name, way_type, coordinates, distance_meters, is_oneway)
VALUES ('Đường nội bộ', 'FOOTPATH', '[[105.8437205, 21.0053725], [105.8437193, 21.0053154], [105.8437165, 21.0051763]]', 21.820397169562455, false);
INSERT INTO campus_ways (name, way_type, coordinates, distance_meters, is_oneway)
VALUES ('Đường nội bộ', 'FOOTPATH', '[[105.843517, 21.0051861], [105.8435348, 21.0051789], [105.8435545, 21.0051775], [105.8437165, 21.0051763], [105.8438901, 21.0051736]]', 38.90488307720679, false);
INSERT INTO campus_ways (name, way_type, coordinates, distance_meters, is_oneway)
VALUES ('Đường nội bộ', 'FOOTPATH', '[[105.8436097, 21.0053163], [105.8437193, 21.0053154], [105.8439141, 21.0053132], [105.8441825, 21.0053102], [105.844355, 21.0053082]]', 77.37169469866106, false);
INSERT INTO campus_ways (name, way_type, coordinates, distance_meters, is_oneway)
VALUES ('Đường nội bộ', 'FOOTPATH', '[[105.8443545, 21.0052656], [105.8445919, 21.0052627]]', 24.645593639913127, false);
INSERT INTO campus_ways (name, way_type, coordinates, distance_meters, is_oneway)
VALUES ('Đường nội bộ', 'FOOTPATH', '[[105.844355, 21.0053082], [105.8443545, 21.0052656], [105.8443533, 21.0051678]]', 15.612765453059035, false);
INSERT INTO campus_ways (name, way_type, coordinates, distance_meters, is_oneway)
VALUES ('Đường nội bộ', 'FOOTPATH', '[[105.8441825, 21.0053102], [105.8441836, 21.0053898]]', 8.85185267574857, false);
INSERT INTO campus_ways (name, way_type, coordinates, distance_meters, is_oneway)
VALUES ('Đường nội bộ', 'FOOTPATH', '[[105.8439141, 21.0053132], [105.8439153, 21.0053928]]', 8.851992667496834, false);
INSERT INTO campus_ways (name, way_type, coordinates, distance_meters, is_oneway)
VALUES ('Đường nội bộ', 'FOOTPATH', '[[105.8443533, 21.0051678], [105.8445751, 21.0051653], [105.8445828, 21.0051669], [105.8445887, 21.0051708]]', 24.595113861396694, false);
INSERT INTO campus_ways (name, way_type, coordinates, distance_meters, is_oneway)
VALUES ('Đường nội bộ', 'FOOTPATH', '[[105.8435663, 21.0052738], [105.8435801, 21.0052758], [105.8435926, 21.0052819], [105.8436023, 21.0052913], [105.8436082, 21.0053032], [105.8436097, 21.0053163], [105.8436065, 21.0053293], [105.8435989, 21.0053407], [105.8435878, 21.0053491], [105.8435743, 21.0053536], [105.8435599, 21.0053538], [105.8435463, 21.0053495], [105.843535, 21.0053413], [105.8435271, 21.00533], [105.8435237, 21.005317], [105.8435249, 21.005304], [105.8435306, 21.005292], [105.8435401, 21.0052823], [105.8435524, 21.0052761], [105.8435663, 21.0052738]]', 27.983858181126433, false);
INSERT INTO campus_ways (name, way_type, coordinates, distance_meters, is_oneway)
VALUES ('Đường nội bộ', 'FOOTPATH', '[[105.8435237, 21.005317], [105.8434513, 21.0053175], [105.8434295, 21.0053176]]', 9.778730698143255, false);
INSERT INTO campus_ways (name, way_type, coordinates, distance_meters, is_oneway)
VALUES ('Đường nội bộ', 'ALLEY', '[[105.8446113, 21.0042871], [105.8446224, 21.0048656], [105.8446232, 21.0049039], [105.8446316, 21.0049226], [105.8446416, 21.0049383], [105.8446572, 21.0049515], [105.8446773, 21.0049595], [105.8453602, 21.0049528], [105.8453602, 21.0049424], [105.8453593, 21.0048153], [105.8453256, 21.0048157], [105.8453261, 21.00485], [105.845295, 21.0048504], [105.8451965, 21.0048516], [105.845196, 21.0048177], [105.8449826, 21.0048203], [105.8447732, 21.0048229], [105.8447735, 21.0048435], [105.8447739, 21.0048664], [105.8446652, 21.0048677], [105.8446625, 21.0046786], [105.8446576, 21.0044365], [105.844656, 21.0042866], [105.8446323, 21.0042869], [105.8446113, 21.0042871]]', 317.33413509794786, false);
INSERT INTO campus_ways (name, way_type, coordinates, distance_meters, is_oneway)
VALUES ('Đường nội bộ', 'FOOTPATH', '[[105.8453602, 21.0049424], [105.8454436, 21.0049418], [105.8455124, 21.0049419]]', 15.799534289381292, false);
INSERT INTO campus_ways (name, way_type, coordinates, distance_meters, is_oneway)
VALUES ('Đường nội bộ', 'FOOTPATH', '[[105.8455124, 21.0049419], [105.8455374, 21.0049425], [105.8455464, 21.0049425], [105.8455541, 21.0049394], [105.845564, 21.0049304]]', 5.835171839552348, false);
INSERT INTO campus_ways (name, way_type, coordinates, distance_meters, is_oneway)
VALUES ('Đường nội bộ', 'FOOTPATH', '[[105.8427847, 21.0056753], [105.8429113, 21.0056738], [105.8429238, 21.0056714], [105.8429349, 21.0056633], [105.8429366, 21.0056523], [105.8429361, 21.0055897], [105.8429352, 21.0055766], [105.8429275, 21.005567], [105.8429089, 21.0055513], [105.8427713, 21.005436], [105.8426558, 21.0053391], [105.8425287, 21.0052322], [105.842504, 21.0052115], [105.8424936, 21.0052027], [105.8424823, 21.0051943], [105.8424687, 21.0051884], [105.8424536, 21.0051869], [105.8417164, 21.00519], [105.8415842, 21.0051909]]', 182.38007214418624, false);
INSERT INTO campus_ways (name, way_type, coordinates, distance_meters, is_oneway)
VALUES ('Đường nội bộ', 'FOOTPATH', '[[105.8426284, 21.0051979], [105.8430561, 21.0051943], [105.8433914, 21.0051915], [105.8434079, 21.0051991], [105.8434107, 21.0052169], [105.8434093, 21.0052215], [105.8434059, 21.0052321], [105.8433863, 21.0052523], [105.8430592, 21.0054636], [105.8430265, 21.0054839], [105.8430272, 21.0055469], [105.8430238, 21.0055532], [105.8430147, 21.0055584], [105.8430022, 21.0055597], [105.8429903, 21.0055573], [105.8429834, 21.0055531], [105.8429135, 21.0054902], [105.8426239, 21.0052434], [105.8426113, 21.0052282], [105.8426086, 21.0052148], [105.8426116, 21.0052106], [105.8426158, 21.0052047], [105.8426284, 21.0051979]]', 202.89552218899038, false);
INSERT INTO campus_ways (name, way_type, coordinates, distance_meters, is_oneway)
VALUES ('Đường nội bộ', 'FOOTPATH', '[[105.842504, 21.0052115], [105.8425436, 21.0052111], [105.8425802, 21.0052108], [105.8426116, 21.0052106]]', 11.169965114599622, false);
INSERT INTO campus_ways (name, way_type, coordinates, distance_meters, is_oneway)
VALUES ('Đường nội bộ', 'FOOTPATH', '[[105.8436722, 21.0056725], [105.8434953, 21.005675], [105.8432702, 21.005678], [105.8432551, 21.0056735], [105.8432444, 21.0056645], [105.8432417, 21.0056515], [105.843246, 21.0056345], [105.843261, 21.0056059], [105.8434295, 21.0053176], [105.8434898, 21.0052205], [105.8435033, 21.0051988], [105.843517, 21.0051861]]', 105.62404336343462, false);
INSERT INTO campus_ways (name, way_type, coordinates, distance_meters, is_oneway)
VALUES ('Đường nội bộ', 'FOOTPATH', '[[105.8434093, 21.0052215], [105.8434295, 21.0052213], [105.8434509, 21.005221], [105.8434759, 21.0052207], [105.8434898, 21.0052205]]', 8.35711906087242, false);
INSERT INTO campus_ways (name, way_type, coordinates, distance_meters, is_oneway)
VALUES ('Đường nội bộ', 'FOOTPATH', '[[105.843513, 21.0066845], [105.8435075, 21.0066747], [105.8435043, 21.006663], [105.8435016, 21.0063688], [105.843498, 21.0059756], [105.8434953, 21.005675]]', 112.43749143155037, false);
INSERT INTO campus_ways (name, way_type, coordinates, distance_meters, is_oneway)
VALUES ('Đường nội bộ', 'FOOTPATH', '[[105.8448528, 21.0060287], [105.8447285, 21.0060293], [105.8447262, 21.0057529], [105.8447235, 21.0054309], [105.8447215, 21.0051806], [105.8447229, 21.0051622], [105.8447268, 21.0051488], [105.8447368, 21.0051353]]', 112.69735846602808, false);
INSERT INTO campus_ways (name, way_type, coordinates, distance_meters, is_oneway)
VALUES ('Đường nội bộ', 'FOOTPATH', '[[105.8445973, 21.0056484], [105.8445943, 21.0054322], [105.8445925, 21.0053052], [105.8445919, 21.0052627]]', 42.891546657706996, false);
INSERT INTO campus_ways (name, way_type, coordinates, distance_meters, is_oneway)
VALUES ('Đường nội bộ', 'FOOTPATH', '[[105.8445919, 21.0052627], [105.8445913, 21.0051829], [105.844591, 21.005175], [105.8445887, 21.0051708]]', 10.277074422713362, false);
INSERT INTO campus_ways (name, way_type, coordinates, distance_meters, is_oneway)
VALUES ('Đường nội bộ', 'FOOTPATH', '[[105.8445913, 21.0051829], [105.8446079, 21.0051827], [105.8446424, 21.0051821], [105.8446835, 21.0051815], [105.8447215, 21.0051806]]', 13.518071273843223, false);
INSERT INTO campus_ways (name, way_type, coordinates, distance_meters, is_oneway)
VALUES ('Đường nội bộ', 'ALLEY', '[[105.8445497, 21.0053057], [105.8445925, 21.0053052], [105.8446089, 21.005305], [105.8446108, 21.005432], [105.844614, 21.0056482], [105.8445973, 21.0056484], [105.8445548, 21.005649], [105.8445527, 21.0055045], [105.8445497, 21.0053057]]', 88.63438428031601, false);
INSERT INTO campus_ways (name, way_type, coordinates, distance_meters, is_oneway)
VALUES ('Đường nội bộ', 'FOOTPATH', '[[105.8446032, 21.006075], [105.8445984, 21.0057544], [105.8445977, 21.005705]]', 41.1460853930204, false);
INSERT INTO campus_ways (name, way_type, coordinates, distance_meters, is_oneway)
VALUES ('Đường nội bộ', 'FOOTPATH', '[[105.8445977, 21.005705], [105.8445974, 21.00568], [105.8445973, 21.0056484]]', 6.293822608705316, false);
INSERT INTO campus_ways (name, way_type, coordinates, distance_meters, is_oneway)
VALUES ('Đường nội bộ', 'ROAD', '[[105.8446842, 21.0046332], [105.8452083, 21.0046282]]', 54.40766286834467, false);
INSERT INTO campus_ways (name, way_type, coordinates, distance_meters, is_oneway)
VALUES ('Đường nội bộ', 'ROAD', '[[105.8446828, 21.004525], [105.845207, 21.004521]]', 54.41705976402767, false);
INSERT INTO campus_ways (name, way_type, coordinates, distance_meters, is_oneway)
VALUES ('Đường nội bộ', 'ROAD', '[[105.845207, 21.004521], [105.8453554, 21.0045203]]', 15.40504601860893, false);
INSERT INTO campus_ways (name, way_type, coordinates, distance_meters, is_oneway)
VALUES ('Đường nội bộ', 'ROAD', '[[105.8452083, 21.0046282], [105.8453568, 21.0046274]]', 15.415475600128078, false);
INSERT INTO campus_ways (name, way_type, coordinates, distance_meters, is_oneway)
VALUES ('Đường nội bộ', 'ROAD', '[[105.8453568, 21.0046274], [105.8454352, 21.004627]]', 8.138526719200643, false);
INSERT INTO campus_ways (name, way_type, coordinates, distance_meters, is_oneway)
VALUES ('Đường nội bộ', 'ROAD', '[[105.8453554, 21.0045203], [105.8454322, 21.0045199]]', 7.972445072309374, false);
INSERT INTO campus_ways (name, way_type, coordinates, distance_meters, is_oneway)
VALUES ('Đường nội bộ', 'FOOTPATH', '[[105.8437452, 21.0066924], [105.8445044, 21.0066906], [105.8445208, 21.0066905]]', 80.51125649094202, false);
INSERT INTO campus_ways (name, way_type, coordinates, distance_meters, is_oneway)
VALUES ('Đường nội bộ', 'FOOTPATH', '[[105.843513, 21.0066845], [105.8435249, 21.0066909], [105.843538, 21.0066929], [105.8436767, 21.0066925]]', 17.201300225733974, false);
INSERT INTO campus_ways (name, way_type, coordinates, distance_meters, is_oneway)
VALUES ('Đường nội bộ', 'FOOTPATH', '[[105.8436767, 21.0066925], [105.8437452, 21.0066924]]', 7.1106341919102665, false);
INSERT INTO campus_ways (name, way_type, coordinates, distance_meters, is_oneway)
VALUES ('Đường nội bộ', 'ROAD', '[[105.8434678, 21.0067603], [105.8436767, 21.0067595]]', 21.684985305468636, false);
INSERT INTO campus_ways (name, way_type, coordinates, distance_meters, is_oneway)
VALUES ('Đường nội bộ', 'ROAD', '[[105.8436767, 21.0067595], [105.8437452, 21.0067592]]', 7.11070055735669, false);
INSERT INTO campus_ways (name, way_type, coordinates, distance_meters, is_oneway)
VALUES ('Đường nội bộ', 'FOOTPATH', '[[105.8428259, 21.005805], [105.8428279, 21.0057979], [105.8428343, 21.0057913], [105.8428496, 21.0057865], [105.8431192, 21.0057854], [105.8434002, 21.0057842], [105.8434123, 21.0057883], [105.8434174, 21.0058002], [105.8434205, 21.0060295], [105.843426, 21.0064372], [105.8434288, 21.0066484], [105.8434285, 21.0066558], [105.8434266, 21.0066668], [105.8434202, 21.006677], [105.8434103, 21.0066856], [105.8433992, 21.0066896], [105.8431327, 21.0066932], [105.8428577, 21.0066968], [105.8428491, 21.0066945], [105.8428434, 21.0066882], [105.8428398, 21.0066799], [105.8428361, 21.0064436], [105.8428296, 21.006034], [105.8428259, 21.005805]]', 320.0900587310488, false);
INSERT INTO campus_ways (name, way_type, coordinates, distance_meters, is_oneway)
VALUES ('Đường nội bộ', 'ALLEY', '[[105.8445429, 21.0057054], [105.8445977, 21.005705], [105.8446198, 21.0057049], [105.8446202, 21.0057541], [105.8446228, 21.0060748], [105.8446032, 21.006075], [105.8445764, 21.0060752], [105.8445745, 21.0058343], [105.8445172, 21.0058347], [105.8445166, 21.0057632], [105.8445433, 21.0057631], [105.8445429, 21.0057054]]', 103.80597813281486, false);
INSERT INTO campus_ways (name, way_type, coordinates, distance_meters, is_oneway)
VALUES ('Đường nội bộ', 'FOOTPATH', '[[105.8440001, 21.0071657], [105.8439968, 21.0067582]]', 45.313227435555255, false);
INSERT INTO campus_ways (name, way_type, coordinates, distance_meters, is_oneway)
VALUES ('Đường nội bộ', 'FOOTPATH', '[[105.8441575, 21.0047353], [105.8445268, 21.0047301]]', 38.33995865366526, false);
INSERT INTO campus_ways (name, way_type, coordinates, distance_meters, is_oneway)
VALUES ('Đường nội bộ', 'ALLEY', '[[105.8441575, 21.0047353], [105.8441563, 21.0046677]]', 7.517809130128945, false);
INSERT INTO campus_ways (name, way_type, coordinates, distance_meters, is_oneway)
VALUES ('Đường nội bộ', 'FOOTPATH', '[[105.8436782, 21.0059748], [105.8437734, 21.0059741]]', 9.882566454276692, false);
INSERT INTO campus_ways (name, way_type, coordinates, distance_meters, is_oneway)
VALUES ('Đường nội bộ', 'FOOTPATH', '[[105.8437734, 21.0059741], [105.8438457, 21.0059736]]', 7.505325627614874, false);
INSERT INTO campus_ways (name, way_type, coordinates, distance_meters, is_oneway)
VALUES ('Đường nội bộ', 'ROAD', '[[105.8438897, 21.0063059], [105.8438912, 21.0064631], [105.8446088, 21.0064616], [105.8446504, 21.0064615]]', 96.28942613074707, false);
INSERT INTO campus_ways (name, way_type, coordinates, distance_meters, is_oneway)
VALUES ('Đường nội bộ', 'FOOTPATH', '[[105.8446039, 21.0061131], [105.8446035, 21.0060953], [105.8446032, 21.006075]]', 4.237176999419315, false);
INSERT INTO campus_ways (name, way_type, coordinates, distance_meters, is_oneway)
VALUES ('Đường nội bộ', 'FOOTPATH', '[[105.8446039, 21.0061131], [105.8446123, 21.0064235], [105.8446088, 21.0064616], [105.8446072, 21.0064789], [105.8445938, 21.0065236], [105.8445766, 21.0065819], [105.844551, 21.0066373], [105.8445208, 21.0066905]]', 65.99807678751256, false);
INSERT INTO campus_ways (name, way_type, coordinates, distance_meters, is_oneway)
VALUES ('Đường nội bộ', 'FOOTPATH', '[[105.8445208, 21.0066905], [105.8445166, 21.006698], [105.8444769, 21.0067563], [105.8444485, 21.006798], [105.8443847, 21.0068861], [105.8443367, 21.0069521], [105.8443064, 21.0069846], [105.8442702, 21.0070092]]', 44.251492856237725, false);
INSERT INTO campus_ways (name, way_type, coordinates, distance_meters, is_oneway)
VALUES ('Đường nội bộ', 'ALLEY', '[[105.8423721, 21.0049344], [105.8423639, 21.0045585], [105.8423578, 21.0042787], [105.8423731, 21.0042784], [105.8423908, 21.0042781], [105.8423932, 21.0043868], [105.8424002, 21.0047093], [105.8427159, 21.0047033], [105.842718, 21.0047998], [105.842691, 21.0048003], [105.8426904, 21.0047753], [105.8423905, 21.0047811], [105.8423938, 21.004934], [105.8423834, 21.0049342], [105.8423721, 21.0049344]]', 223.80017517909363, false);
INSERT INTO campus_ways (name, way_type, coordinates, distance_meters, is_oneway)
VALUES ('Đường nội bộ', 'FOOTPATH', '[[105.8423847, 21.0050115], [105.8423834, 21.0049342], [105.8423731, 21.0042784], [105.8423707, 21.0041423]]', 96.66157816988873, false);
INSERT INTO campus_ways (name, way_type, coordinates, distance_meters, is_oneway)
VALUES ('Đường nội bộ', 'FOOTPATH', '[[105.8423729, 21.0050117], [105.8423847, 21.0050115], [105.8429981, 21.0049987]]', 64.91557174539032, false);
INSERT INTO campus_ways (name, way_type, coordinates, distance_meters, is_oneway)
VALUES ('Đường nội bộ', 'FOOTPATH', '[[105.8429981, 21.0049987], [105.8430009, 21.0049986], [105.843028, 21.004998], [105.8430537, 21.0049975], [105.8430567, 21.0049974]]', 6.084808892594321, false);
INSERT INTO campus_ways (name, way_type, coordinates, distance_meters, is_oneway)
VALUES ('Đường nội bộ', 'ROAD', '[[105.8431182, 21.0057207], [105.8429498, 21.005551]]', 25.722528413158997, false);
INSERT INTO campus_ways (name, way_type, coordinates, distance_meters, is_oneway)
VALUES ('Đường nội bộ', 'FOOTPATH', '[[105.8427846, 21.0057241], [105.8427846, 21.0057019], [105.8427847, 21.0056753]]', 5.426330635616081, false);
INSERT INTO campus_ways (name, way_type, coordinates, distance_meters, is_oneway)
VALUES ('Đường nội bộ', 'ROAD', '[[105.8402977, 21.0077973], [105.840333, 21.0077193], [105.8404698, 21.0075212], [105.8406381, 21.0072776], [105.8406958, 21.0072028], [105.8408261, 21.0070338]]', 101.2589967180692, false);
INSERT INTO campus_ways (name, way_type, coordinates, distance_meters, is_oneway)
VALUES ('Đường nội bộ', 'ALLEY', '[[105.8427268, 21.0057701], [105.842726, 21.0057025], [105.8427846, 21.0057019], [105.8429006, 21.0057006], [105.8429118, 21.0056983], [105.8429191, 21.0056937], [105.8429237, 21.005687], [105.8429238, 21.0056714], [105.8429237, 21.0056531], [105.8424334, 21.0056589], [105.8417474, 21.0056671], [105.8417477, 21.0056872], [105.8417513, 21.0059103], [105.8417647, 21.0059102], [105.8417778, 21.0059102], [105.841776, 21.0057277], [105.8419416, 21.0057263], [105.8419421, 21.0057759], [105.8424448, 21.0057717], [105.8426479, 21.00577], [105.8427268, 21.0057701]]', 308.7742196941096, false);
INSERT INTO campus_ways (name, way_type, coordinates, distance_meters, is_oneway)
VALUES ('Đường nội bộ', 'ALLEY', '[[105.8424778, 21.0060456], [105.8424774, 21.0060257], [105.8424771, 21.0060059], [105.8427286, 21.0060016], [105.8427291, 21.0060226], [105.8427294, 21.0060413], [105.8424778, 21.0060456]]', 61.06345602278714, false);
INSERT INTO campus_ways (name, way_type, coordinates, distance_meters, is_oneway)
VALUES ('Đường nội bộ', 'FOOTPATH', '[[105.8417608, 21.0056871], [105.8417647, 21.0059102], [105.84178, 21.0059438], [105.8418107, 21.0060024], [105.8418143, 21.0061814], [105.8418212, 21.0064499], [105.8418282, 21.0067238]]', 116.36096524321486, false);
INSERT INTO campus_ways (name, way_type, coordinates, distance_meters, is_oneway)
VALUES ('Đường nội bộ', 'FOOTPATH', '[[105.8430558, 21.0037597], [105.8430738, 21.0037516], [105.8430944, 21.003748], [105.8435905, 21.0037191], [105.8436103, 21.003722], [105.843627, 21.0037282], [105.843644, 21.0037365], [105.8436564, 21.0037485], [105.843665, 21.0037612], [105.8436701, 21.003775]]', 66.93413973988248, false);
INSERT INTO campus_ways (name, way_type, coordinates, distance_meters, is_oneway)
VALUES ('Đường nội bộ', 'ROAD', '[[105.8448646, 21.0050734], [105.8448652, 21.0051286], [105.8448657, 21.005173]]', 11.075603507543287, false);
INSERT INTO campus_ways (name, way_type, coordinates, distance_meters, is_oneway)
VALUES ('Đường nội bộ', 'ALLEY', '[[105.8448811, 21.0051729], [105.8448807, 21.0051284], [105.8448806, 21.0051194], [105.8453259, 21.005116], [105.8453258, 21.0051241], [105.8453253, 21.0051721], [105.845114, 21.0051725], [105.8448811, 21.0051729]]', 104.524420467334, false);
INSERT INTO campus_ways (name, way_type, coordinates, distance_meters, is_oneway)
VALUES ('Đường nội bộ', 'FOOTPATH', '[[105.8447368, 21.0051353], [105.8447508, 21.0051297], [105.8448493, 21.0051287]]', 11.806546681715593, false);
INSERT INTO campus_ways (name, way_type, coordinates, distance_meters, is_oneway)
VALUES ('Đường nội bộ', 'FOOTPATH', '[[105.8448493, 21.0051287], [105.8448652, 21.0051286], [105.8448807, 21.0051284]]', 3.2596945062944007, false);
INSERT INTO campus_ways (name, way_type, coordinates, distance_meters, is_oneway)
VALUES ('Đường nội bộ', 'FOOTPATH', '[[105.8447262, 21.0057529], [105.8448458, 21.0057524], [105.8449673, 21.0057519], [105.8451058, 21.0057512]]', 39.404989843009595, false);
INSERT INTO campus_ways (name, way_type, coordinates, distance_meters, is_oneway)
VALUES ('Đường nội bộ', 'FOOTPATH', '[[105.8448461, 21.0058216], [105.8448458, 21.0057524], [105.8448455, 21.0056791]]', 15.845399556868497, false);
INSERT INTO campus_ways (name, way_type, coordinates, distance_meters, is_oneway)
VALUES ('Đường nội bộ', 'FOOTPATH', '[[105.8449673, 21.0058214], [105.8449673, 21.0057519], [105.8449673, 21.0056788]]', 15.856396539537958, false);
INSERT INTO campus_ways (name, way_type, coordinates, distance_meters, is_oneway)
VALUES ('Đường nội bộ', 'FOOTPATH', '[[105.8447235, 21.0054309], [105.8448481, 21.0054303], [105.8449706, 21.0054297], [105.8451025, 21.0054291]]', 39.342843426196424, false);
INSERT INTO campus_ways (name, way_type, coordinates, distance_meters, is_oneway)
VALUES ('Đường nội bộ', 'FOOTPATH', '[[105.8448481, 21.0055073], [105.8448481, 21.0054303], [105.8448481, 21.005356]]', 16.82379240158948, false);
INSERT INTO campus_ways (name, way_type, coordinates, distance_meters, is_oneway)
VALUES ('Đường nội bộ', 'FOOTPATH', '[[105.8449706, 21.0055071], [105.8449706, 21.0054297], [105.8449706, 21.0053558]]', 16.82379240119443, false);
INSERT INTO campus_ways (name, way_type, coordinates, distance_meters, is_oneway)
VALUES ('Đường nội bộ', 'FOOTPATH', '[[105.8447262, 21.0057529], [105.8446906, 21.0057533], [105.8446523, 21.0057538], [105.8446202, 21.0057541], [105.8445984, 21.0057544]]', 13.26739995473632, false);
INSERT INTO campus_ways (name, way_type, coordinates, distance_meters, is_oneway)
VALUES ('Đường nội bộ', 'FOOTPATH', '[[105.8447235, 21.0054309], [105.8446866, 21.0054312], [105.8446469, 21.0054316], [105.8446108, 21.005432], [105.8445943, 21.0054322]]', 13.412480853106477, false);
INSERT INTO campus_ways (name, way_type, coordinates, distance_meters, is_oneway)
VALUES ('Đường nội bộ', 'ROAD', '[[105.8448711, 21.0060063], [105.8448714, 21.0060286], [105.8448723, 21.0060941]]', 9.763709271648208, false);
INSERT INTO campus_ways (name, way_type, coordinates, distance_meters, is_oneway)
VALUES ('Đường nội bộ', 'ALLEY', '[[105.8448877, 21.0060507], [105.8448875, 21.0060285], [105.8448874, 21.0060068], [105.8453278, 21.0060049], [105.8453279, 21.0060265], [105.8453281, 21.0060486], [105.8448877, 21.0060507]]', 101.17361255810515, false);
INSERT INTO campus_ways (name, way_type, coordinates, distance_meters, is_oneway)
VALUES ('Đường nội bộ', 'FOOTPATH', '[[105.8453279, 21.0060265], [105.8448875, 21.0060285]]', 45.71635733703192, false);
INSERT INTO campus_ways (name, way_type, coordinates, distance_meters, is_oneway)
VALUES ('Đường nội bộ', 'FOOTPATH', '[[105.8448875, 21.0060285], [105.8448714, 21.0060286], [105.8448528, 21.0060287]]', 3.6021099221448587, false);
INSERT INTO campus_ways (name, way_type, coordinates, distance_meters, is_oneway)
VALUES ('Đường nội bộ', 'FOOTPATH', '[[105.8411082, 21.0078947], [105.8411086, 21.0078637], [105.8411045, 21.0078176]]', 8.591016251610354, false);
INSERT INTO campus_ways (name, way_type, coordinates, distance_meters, is_oneway)
VALUES ('Đường nội bộ', 'FOOTPATH', '[[105.8411045, 21.0078176], [105.8410974, 21.0076478]]', 18.895277492201917, false);
INSERT INTO campus_ways (name, way_type, coordinates, distance_meters, is_oneway)
VALUES ('Đường nội bộ', 'FOOTPATH', '[[105.8410974, 21.0076478], [105.8410954, 21.0075921], [105.8410936, 21.0075664]]', 9.060847436492915, false);
INSERT INTO campus_ways (name, way_type, coordinates, distance_meters, is_oneway)
VALUES ('Đường nội bộ', 'FOOTPATH', '[[105.8416267, 21.007961], [105.8416106, 21.0079385], [105.8416012, 21.0078734], [105.8416003, 21.0078359]]', 14.483848324441455, false);
INSERT INTO campus_ways (name, way_type, coordinates, distance_meters, is_oneway)
VALUES ('Đường nội bộ', 'FOOTPATH', '[[105.8416003, 21.0078359], [105.8415988, 21.0077949], [105.8415965, 21.0077549]]', 9.015850432225799, false);
INSERT INTO campus_ways (name, way_type, coordinates, distance_meters, is_oneway)
VALUES ('Đường nội bộ', 'FOOTPATH', '[[105.8415869, 21.0076042], [105.8415913, 21.0075258]]', 8.72963880183145, false);
INSERT INTO campus_ways (name, way_type, coordinates, distance_meters, is_oneway)
VALUES ('Đường nội bộ', 'FOOTPATH', '[[105.8415913, 21.0075258], [105.8416085, 21.0074825]]', 5.135123355614335, false);
INSERT INTO campus_ways (name, way_type, coordinates, distance_meters, is_oneway)
VALUES ('Đường nội bộ', 'FOOTPATH', '[[105.8417061, 21.0080534], [105.8416428, 21.0079835], [105.8416267, 21.007961]]', 13.186520681628133, false);
INSERT INTO campus_ways (name, way_type, coordinates, distance_meters, is_oneway)
VALUES ('Đường nội bộ', 'ROAD', '[[105.8451147, 21.0031479], [105.8450251, 21.0032464], [105.8449698, 21.0033116], [105.8448806, 21.0033962], [105.8448026, 21.0034174], [105.8447262, 21.0034402]]', 53.57567495780247, false);
INSERT INTO campus_ways (name, way_type, coordinates, distance_meters, is_oneway)
VALUES ('Đường nội bộ', 'ROAD', '[[105.8446398, 21.005037], [105.8446412, 21.0051143], [105.8446421, 21.0051645], [105.8446424, 21.0051821], [105.8446439, 21.0052621], [105.8446469, 21.0054316], [105.8446514, 21.0056796], [105.8446523, 21.0057538], [105.8446564, 21.0060951], [105.8446578, 21.0062756], [105.8446564, 21.0063626], [105.8446504, 21.0064615], [105.8446465, 21.0064979], [105.8446332, 21.0065546], [105.8446084, 21.0066172], [105.8445646, 21.0066904], [105.8445248, 21.0067561]]', 194.08896119116483, false);
INSERT INTO campus_ways (name, way_type, coordinates, distance_meters, is_oneway)
VALUES ('Đường nội bộ', 'ROAD', '[[105.8443672, 21.0069667], [105.8443779, 21.0069246], [105.8444024, 21.0068952], [105.8444857, 21.0067781], [105.8445248, 21.0067561]]', 29.32266410811459, false);
INSERT INTO campus_ways (name, way_type, coordinates, distance_meters, is_oneway)
VALUES ('Đường nội bộ', 'ROAD', '[[105.8445248, 21.0067561], [105.8445167, 21.0068004], [105.8444365, 21.0069131], [105.8444075, 21.0069479], [105.8443672, 21.0069667]]', 29.621323942906557, false);
INSERT INTO campus_ways (name, way_type, coordinates, distance_meters, is_oneway)
VALUES ('Đường nội bộ', 'FOOTPATH', '[[105.8430537, 21.0049975], [105.8430544, 21.0050467], [105.8430553, 21.005118], [105.8430561, 21.0051943], [105.8430592, 21.0054636]]', 51.83112374253834, false);
INSERT INTO campus_ways (name, way_type, coordinates, distance_meters, is_oneway)
VALUES ('Đường nội bộ', 'FOOTPATH', '[[105.8429835, 21.00413], [105.8430009, 21.0049986]]', 96.60080106470296, false);
INSERT INTO campus_ways (name, way_type, coordinates, distance_meters, is_oneway)
VALUES ('Đường nội bộ', 'FOOTPATH', '[[105.8430444, 21.0045334], [105.8430537, 21.0049975]]', 51.61459460985715, false);
INSERT INTO campus_ways (name, way_type, coordinates, distance_meters, is_oneway)
VALUES ('Đường nội bộ', 'FOOTPATH', '[[105.847275, 21.0044509], [105.8472063, 21.0044498], [105.8471623, 21.004449]]', 11.70088635813521, false);
INSERT INTO campus_ways (name, way_type, coordinates, distance_meters, is_oneway)
VALUES ('Đường nội bộ', 'FOOTPATH', '[[105.8400969, 21.0077763], [105.840333, 21.0077193], [105.8404297, 21.0077032]]', 35.51066769183025, false);
INSERT INTO campus_ways (name, way_type, coordinates, distance_meters, is_oneway)
VALUES ('Đường nội bộ', 'ROAD', '[[105.8458792, 21.0063474], [105.8464721, 21.0063408], [105.8464961, 21.0063405], [105.846544, 21.00634]]', 69.01446978447606, false);
INSERT INTO campus_ways (name, way_type, coordinates, distance_meters, is_oneway)
VALUES ('Đường nội bộ', 'ROAD', '[[105.84655, 21.0065489], [105.8465021, 21.0065494], [105.8458287, 21.0065569], [105.8457659, 21.0065576]]', 81.39913189293416, false);
INSERT INTO campus_ways (name, way_type, coordinates, distance_meters, is_oneway)
VALUES ('Phố Trần Đại Nghĩa', 'ROAD', '[[105.8450617, 21.0067735], [105.8450948, 21.0067459], [105.845161, 21.0066907], [105.8453283, 21.0065325], [105.8455644, 21.0062728], [105.845651, 21.0061208]]', 95.55935250113299, false);
INSERT INTO campus_ways (name, way_type, coordinates, distance_meters, is_oneway)
VALUES ('Phố Trần Đại Nghĩa', 'ROAD', '[[105.845651, 21.0061208], [105.8456508, 21.0055823], [105.8456507, 21.0051601], [105.8456507, 21.0051424], [105.8456502, 21.0050332]]', 120.93571789438131, false);
INSERT INTO campus_ways (name, way_type, coordinates, distance_meters, is_oneway)
VALUES ('Đường nội bộ', 'ROAD', '[[105.8461271, 21.0069854], [105.8458034, 21.0069771]]', 33.61420541050751, false);
INSERT INTO campus_ways (name, way_type, coordinates, distance_meters, is_oneway)
VALUES ('Đường nội bộ', 'ROAD', '[[105.847097, 21.0055749], [105.8465822, 21.0055805], [105.8465701, 21.0055806], [105.8465222, 21.0055811]]', 59.67140479947247, false);
INSERT INTO campus_ways (name, way_type, coordinates, distance_meters, is_oneway)
VALUES ('Đường nội bộ', 'FOOTPATH', '[[105.8465987, 21.0065766], [105.8465919, 21.0063395], [105.8465701, 21.0055806], [105.8465615, 21.005283], [105.8465563, 21.0050993], [105.8465555, 21.0050725]]', 167.30840151106844, false);
INSERT INTO campus_ways (name, way_type, coordinates, distance_meters, is_oneway)
VALUES ('Đường nội bộ', 'FOOTPATH', '[[105.8464597, 21.0050737], [105.8464605, 21.0051019], [105.846466, 21.0052923]]', 24.316007190778944, false);
INSERT INTO campus_ways (name, way_type, coordinates, distance_meters, is_oneway)
VALUES ('Hẻm 40/2/3 Phố Tạ Quang Bửu', 'ROAD', '[[105.8466858, 21.0040793], [105.846195, 21.0036649]]', 68.69514874435536, false);
INSERT INTO campus_ways (name, way_type, coordinates, distance_meters, is_oneway)
VALUES ('Đường nội bộ', 'ROAD', '[[105.8457414, 21.0033707], [105.8455723, 21.0032279]]', 23.66994618624833, false);
INSERT INTO campus_ways (name, way_type, coordinates, distance_meters, is_oneway)
VALUES ('Đường nội bộ', 'ROAD', '[[105.8458719, 21.0032609], [105.8456879, 21.0031055]]', 25.756896944714253, false);
INSERT INTO campus_ways (name, way_type, coordinates, distance_meters, is_oneway)
VALUES ('Đường nội bộ', 'ROAD', '[[105.8465471, 21.0042263], [105.8460578, 21.0038125]]', 68.53489769433357, false);
INSERT INTO campus_ways (name, way_type, coordinates, distance_meters, is_oneway)
VALUES ('Đường nội bộ', 'ROAD', '[[105.8459263, 21.0039541], [105.8455316, 21.0036208], [105.8455098, 21.0036024]]', 58.29826711958222, false);
INSERT INTO campus_ways (name, way_type, coordinates, distance_meters, is_oneway)
VALUES ('Đường nội bộ', 'ROAD', '[[105.8464373, 21.0043427], [105.8461756, 21.0041217]]', 36.63175033676708, false);
INSERT INTO campus_ways (name, way_type, coordinates, distance_meters, is_oneway)
VALUES ('Đường nội bộ', 'ROAD', '[[105.8458046, 21.0041772], [105.8455017, 21.0039215], [105.8454793, 21.0039442], [105.8453543, 21.0038387]]', 63.3163930332937, false);
INSERT INTO campus_ways (name, way_type, coordinates, distance_meters, is_oneway)
VALUES ('Đường nội bộ', 'ROAD', '[[105.8455316, 21.0036208], [105.8454483, 21.0037106], [105.8454625, 21.003722], [105.8453543, 21.0038387], [105.8453195, 21.0038761]]', 37.82406461284734, false);
INSERT INTO campus_ways (name, way_type, coordinates, distance_meters, is_oneway)
VALUES ('Đường nội bộ', 'ROAD', '[[105.8453011, 21.0032411], [105.8452011, 21.0033487], [105.8451659, 21.0033866], [105.8451263, 21.0034292]]', 27.689847870247764, false);
INSERT INTO campus_ways (name, way_type, coordinates, distance_meters, is_oneway)
VALUES ('Đường nội bộ', 'ROAD', '[[105.8420679, 21.0037733], [105.8416595, 21.0037817]]', 42.404976868793646, false);
INSERT INTO campus_ways (name, way_type, coordinates, distance_meters, is_oneway)
VALUES ('Đường nội bộ', 'ROAD', '[[105.8416548, 21.0035652], [105.8416595, 21.0037817]]', 24.07864507037474, false);
INSERT INTO campus_ways (name, way_type, coordinates, distance_meters, is_oneway)
VALUES ('Đường nội bộ', 'ROAD', '[[105.8454144, 21.006734], [105.8454088, 21.0069348], [105.8454039, 21.0070349], [105.8453974, 21.007122], [105.8453921, 21.0072096], [105.8453872, 21.0072428], [105.8453786, 21.0072677], [105.8453677, 21.0072896], [105.8453532, 21.0073108], [105.8453375, 21.0073324], [105.8453274, 21.0073608], [105.8453111, 21.0074339]]', 79.59276721424546, false);
INSERT INTO campus_ways (name, way_type, coordinates, distance_meters, is_oneway)
VALUES ('Đường nội bộ', 'ROAD', '[[105.8454088, 21.0069348], [105.8457311, 21.0069411]]', 33.46354970745216, false);
INSERT INTO campus_ways (name, way_type, coordinates, distance_meters, is_oneway)
VALUES ('Đường nội bộ', 'ROAD', '[[105.8451702, 21.0069758], [105.8452825, 21.0069764], [105.8452829, 21.0069102]]', 19.01866363056551, false);
INSERT INTO campus_ways (name, way_type, coordinates, distance_meters, is_oneway)
VALUES ('Ngõ 43 Phố Trần Đại Nghĩa', 'ROAD', '[[105.8449124, 21.0068938], [105.8449235, 21.0069653], [105.8449455, 21.0070998], [105.8451719, 21.0070998], [105.8453015, 21.0070998]]', 60.11695910450189, false);
INSERT INTO campus_ways (name, way_type, coordinates, distance_meters, is_oneway)
VALUES ('Ngõ 43 Phố Trần Đại Nghĩa', 'ROAD', '[[105.8449455, 21.0070998], [105.8449512, 21.0071519], [105.8449689, 21.0071522], [105.8449681, 21.0071966], [105.8449871, 21.0071969], [105.844986, 21.0072577], [105.8450958, 21.0072593]]', 32.732075507499935, false);
INSERT INTO campus_ways (name, way_type, coordinates, distance_meters, is_oneway)
VALUES ('Ngõ 19 Phố Trần Đại Nghĩa', 'ROAD', '[[105.8445278, 21.0070782], [105.8445306, 21.0071328], [105.8445368, 21.0072745], [105.844892, 21.0072771]]', 58.72009311674465, false);
INSERT INTO campus_ways (name, way_type, coordinates, distance_meters, is_oneway)
VALUES ('Ngõ 1 Phố Trần Đại Nghĩa', 'ROAD', '[[105.8440518, 21.0073617], [105.8441456, 21.0073629], [105.8442812, 21.007364], [105.8443463, 21.0073756], [105.8444068, 21.0073817], [105.8445417, 21.0073855], [105.8448918, 21.0073885], [105.844892, 21.0072771], [105.844892, 21.0071513], [105.8449512, 21.0071519]]', 119.88450196417696, false);
INSERT INTO campus_ways (name, way_type, coordinates, distance_meters, is_oneway)
VALUES ('Ngõ 19 Phố Trần Đại Nghĩa', 'ROAD', '[[105.8445368, 21.0072745], [105.8445417, 21.0073855]]', 12.353112983247112, false);
INSERT INTO campus_ways (name, way_type, coordinates, distance_meters, is_oneway)
VALUES ('Ngõ 33 phố Trần Đại Nghĩa', 'ROAD', '[[105.8447435, 21.0069889], [105.8447441, 21.0070683], [105.8447462, 21.0071385], [105.8447773, 21.0071456]]', 19.961474308594116, false);
INSERT INTO campus_ways (name, way_type, coordinates, distance_meters, is_oneway)
VALUES ('Đường nội bộ', 'ROAD', '[[105.8462925, 21.0069257], [105.8462985, 21.0069268], [105.8465681, 21.0069783], [105.8468115, 21.0070248], [105.8467868, 21.0071643]]', 70.71215156363608, false);
INSERT INTO campus_ways (name, way_type, coordinates, distance_meters, is_oneway)
VALUES ('Ngõ 22 Phố Tạ Quang Bửu', 'ROAD', '[[105.8474401, 21.0073187], [105.8467843, 21.0071786], [105.8467868, 21.0071643], [105.8463298, 21.0070863], [105.8462954, 21.0070805], [105.8463019, 21.0070428], [105.846278, 21.0070245], [105.8462925, 21.0069257]]', 141.8427851956692, false);
INSERT INTO campus_ways (name, way_type, coordinates, distance_meters, is_oneway)
VALUES ('Ngách 40/16 phố Tạ Quang Bửu', 'ROAD', '[[105.8466293, 21.0066835], [105.8465681, 21.0069783]]', 33.39018375204336, false);
INSERT INTO campus_ways (name, way_type, coordinates, distance_meters, is_oneway)
VALUES ('Đường nội bộ', 'ROAD', '[[105.8461935, 21.0072264], [105.8461759, 21.0073919]]', 18.49322463399666, false);
INSERT INTO campus_ways (name, way_type, coordinates, distance_meters, is_oneway)
VALUES ('Ngõ 16 phố Tạ Quang Bửu', 'ROAD', '[[105.8474403, 21.0075401], [105.8469455, 21.0074392], [105.8468608, 21.0074174], [105.8467633, 21.0073848], [105.8466601, 21.0073614], [105.846519, 21.007343]]', 98.25725701299, false);
INSERT INTO campus_ways (name, way_type, coordinates, distance_meters, is_oneway)
VALUES ('Ngách 40/18 phố Tạ Quang Bửu', 'ROAD', '[[105.8462013, 21.0066603], [105.8461869, 21.0067775], [105.8461599, 21.0069025], [105.8461514, 21.0069323], [105.8461391, 21.0069621], [105.8461271, 21.0069854], [105.8461085, 21.0070217], [105.8460966, 21.0070566], [105.8460892, 21.0070936], [105.8460856, 21.0071488], [105.8460816, 21.0072099], [105.8460765, 21.0073238]]', 75.51594774625251, false);
INSERT INTO campus_ways (name, way_type, coordinates, distance_meters, is_oneway)
VALUES ('Đường nội bộ', 'ROAD', '[[105.845763, 21.0071378], [105.8457604, 21.0073306]]', 21.44008064366804, false);
INSERT INTO campus_ways (name, way_type, coordinates, distance_meters, is_oneway)
VALUES ('Ngách 40/30 phố Tạ Quang Bửu', 'ROAD', '[[105.8458521, 21.0066648], [105.8458491, 21.0068098], [105.8460588, 21.0068135]]', 37.89799823106097, false);
INSERT INTO campus_ways (name, way_type, coordinates, distance_meters, is_oneway)
VALUES ('Đường nội bộ', 'ROAD', '[[105.8448073, 21.0077811], [105.8448199, 21.0076948], [105.8448271, 21.007545]]', 26.358605111894384, false);
INSERT INTO campus_ways (name, way_type, coordinates, distance_meters, is_oneway)
VALUES ('Đường nội bộ', 'ROAD', '[[105.8440343, 21.0075158], [105.8441017, 21.0075179], [105.8447111, 21.0075445], [105.8448271, 21.007545], [105.8449069, 21.0075428], [105.8449715, 21.0075429], [105.8449862, 21.0075375], [105.8450105, 21.0075339], [105.8450813, 21.0075446], [105.8452069, 21.0075649], [105.845286, 21.0075696], [105.8453632, 21.0075577], [105.8454742, 21.0075571], [105.8456312, 21.0075774], [105.8459233, 21.0076058], [105.8462124, 21.0076313], [105.8466112, 21.0076917], [105.8468397, 21.0077378], [105.8469376, 21.0077607], [105.8474326, 21.0078687]]', 356.6356198252482, false);
INSERT INTO campus_ways (name, way_type, coordinates, distance_meters, is_oneway)
VALUES ('Đường nội bộ', 'FOOTPATH', '[[105.846466, 21.0052923], [105.8464739, 21.0055664], [105.8464961, 21.0063405], [105.8465021, 21.0065494]]', 139.83336408810558, false);
INSERT INTO campus_ways (name, way_type, coordinates, distance_meters, is_oneway)
VALUES ('Đường nội bộ', 'FOOTPATH', '[[105.8472656, 21.0063322], [105.8473106, 21.0063321], [105.847321, 21.0063321]]', 5.750810370189579, false);
INSERT INTO campus_ways (name, way_type, coordinates, distance_meters, is_oneway)
VALUES ('Đường nội bộ', 'ROAD', '[[105.8461715, 21.0055722], [105.8464404, 21.005567], [105.8464739, 21.0055664], [105.8465218, 21.0055655]]', 36.37071340281719, false);
INSERT INTO campus_ways (name, way_type, coordinates, distance_meters, is_oneway)
VALUES ('Đường nội bộ', 'ROAD', '[[105.8467001, 21.0036898], [105.8467237, 21.0036642], [105.8467412, 21.0036453], [105.8467603, 21.0036247], [105.8472952, 21.0030473]]', 94.44728679584308, false);
INSERT INTO campus_ways (name, way_type, coordinates, distance_meters, is_oneway)
VALUES ('Đường nội bộ', 'ROAD', '[[105.8467603, 21.0036247], [105.8468156, 21.0036717], [105.8471021, 21.003916], [105.8470866, 21.0039312], [105.8470707, 21.0039476], [105.8470441, 21.0039745]]', 56.90664350719013, false);
INSERT INTO campus_ways (name, way_type, coordinates, distance_meters, is_oneway)
VALUES ('Ngõ 75 Đường Giải Phóng', 'ROAD', '[[105.843513, 21.0020861], [105.8436795, 21.0018901], [105.8437259, 21.0018233]]', 36.66877596447971, false);
INSERT INTO campus_ways (name, way_type, coordinates, distance_meters, is_oneway)
VALUES ('Ngõ 204 Phố Lê Thanh Nghị', 'ROAD', '[[105.843513, 21.0020861], [105.8433383, 21.0019007], [105.8433344, 21.0018901], [105.8433109, 21.0018263]]', 36.205235202520015, false);
INSERT INTO campus_ways (name, way_type, coordinates, distance_meters, is_oneway)
VALUES ('Ngõ 75 Đường Giải Phóng', 'ROAD', '[[105.8426162, 21.0027603], [105.8431848, 21.0025905], [105.8433448, 21.0023098], [105.843513, 21.0020861]]', 127.71852058074009, false);
INSERT INTO campus_ways (name, way_type, coordinates, distance_meters, is_oneway)
VALUES ('Đại Cồ Việt - Xã Đàn', 'ROAD', '[[105.8414691, 21.0078117], [105.8412932, 21.0078348]]', 18.43890155809326, false);
INSERT INTO campus_ways (name, way_type, coordinates, distance_meters, is_oneway)
VALUES ('Xã Đàn - Đại Cồ Việt', 'ROAD', '[[105.8413007, 21.0075592], [105.8414823, 21.0075347]]', 19.04667049655423, false);
INSERT INTO campus_ways (name, way_type, coordinates, distance_meters, is_oneway)
VALUES ('Ngõ 10 Giải Phóng', 'ROAD', '[[105.8411402, 21.0067491], [105.8409109, 21.006747], [105.8405731, 21.0067302], [105.8405729, 21.0065767], [105.8400751, 21.0065855]]', 127.67026707580094, false);
INSERT INTO campus_ways (name, way_type, coordinates, distance_meters, is_oneway)
VALUES ('Đường Giải Phóng', 'ROAD', '[[105.8413234, 21.0069589], [105.8412331, 21.006961], [105.8411922, 21.0069615], [105.8411458, 21.0069654], [105.8411402, 21.0067491]]', 42.51693235460209, false);
INSERT INTO campus_ways (name, way_type, coordinates, distance_meters, is_oneway)
VALUES ('Ngõ 24 Giải Phóng', 'ROAD', '[[105.8411396, 21.0057352], [105.8410419, 21.0057351], [105.8410033, 21.0057736]]', 16.005425464344533, false);
INSERT INTO campus_ways (name, way_type, coordinates, distance_meters, is_oneway)
VALUES ('Đường Giải Phóng', 'ROAD', '[[105.8410428, 21.0063309], [105.8410382, 21.0061558], [105.8410141, 21.005828], [105.8410033, 21.0057736]]', 62.163550622917384, false);
INSERT INTO campus_ways (name, way_type, coordinates, distance_meters, is_oneway)
VALUES ('Đường nội bộ', 'ROAD', '[[105.8407673, 21.0051497], [105.840951, 21.0051214], [105.8411025, 21.0051215]]', 35.05362342162481, false);
INSERT INTO campus_ways (name, way_type, coordinates, distance_meters, is_oneway)
VALUES ('Đường Giải Phóng', 'ROAD', '[[105.8411025, 21.0051215], [105.8410991, 21.0048429], [105.8410983, 21.0047824]]', 37.70872262249611, false);
INSERT INTO campus_ways (name, way_type, coordinates, distance_meters, is_oneway)
VALUES ('Đường nội bộ', 'FOOTPATH', '[[105.8414534, 21.0035472], [105.8413984, 21.0036593], [105.8413833, 21.0037011], [105.8413797, 21.0039819], [105.8413647, 21.0040656], [105.8413347, 21.0041186], [105.8412974, 21.0041616], [105.8412431, 21.0041897]]', 78.54046313370313, false);
INSERT INTO campus_ways (name, way_type, coordinates, distance_meters, is_oneway)
VALUES ('Đường nội bộ', 'ROAD', '[[105.841233, 21.0034052], [105.841157, 21.0034058], [105.8411216, 21.0034053], [105.8409595, 21.0034075]]', 28.39370562684528, false);
INSERT INTO campus_ways (name, way_type, coordinates, distance_meters, is_oneway)
VALUES ('Đường nội bộ', 'ROAD', '[[105.8451921, 21.0042668], [105.8450568, 21.0042679]]', 14.045543769938792, false);
INSERT INTO campus_ways (name, way_type, coordinates, distance_meters, is_oneway)
VALUES ('Đường nội bộ', 'ROAD', '[[105.8450568, 21.0042679], [105.8448902, 21.0042692]]', 17.29475668853598, false);
INSERT INTO campus_ways (name, way_type, coordinates, distance_meters, is_oneway)
VALUES ('Đường nội bộ', 'ROAD', '[[105.8452851, 21.0042661], [105.8451921, 21.0042668]]', 9.6543126479339, false);
INSERT INTO campus_ways (name, way_type, coordinates, distance_meters, is_oneway)
VALUES ('Đường nội bộ', 'ROAD', '[[105.8436701, 21.003775], [105.8436931, 21.0040749], [105.8436958, 21.0041181], [105.843702, 21.0042183], [105.8437092, 21.0043355], [105.8437085, 21.0044408]]', 74.16732642693181, false);
INSERT INTO campus_ways (name, way_type, coordinates, distance_meters, is_oneway)
VALUES ('Đường nội bộ', 'ROAD', '[[105.8404698, 21.0075212], [105.8404421, 21.0075083], [105.8404191, 21.0074866], [105.8403963, 21.0074563], [105.8403835, 21.0074263], [105.8403777, 21.0074095], [105.8403724, 21.0073675], [105.8403753, 21.007331], [105.8403905, 21.007283], [105.8404146, 21.007241], [105.8404465, 21.0072089], [105.8404787, 21.0071877], [105.8405131, 21.0071708], [105.840561, 21.0071593], [105.8406088, 21.0071588], [105.8406424, 21.0071697], [105.8406736, 21.0071872], [105.8406958, 21.0072028]]', 69.35997165985343, false);
INSERT INTO campus_ways (name, way_type, coordinates, distance_meters, is_oneway)
VALUES ('Đường Giải Phóng', 'ROAD', '[[105.8411025, 21.0051215], [105.8411038, 21.0051611]]', 4.405386464332895, false);
INSERT INTO campus_ways (name, way_type, coordinates, distance_meters, is_oneway)
VALUES ('Đường nội bộ', 'ROAD', '[[105.8400186, 21.0041402], [105.8400178, 21.0040816], [105.8400169, 21.0040217]]', 13.177783178923226, false);
INSERT INTO campus_ways (name, way_type, coordinates, distance_meters, is_oneway)
VALUES ('Đường nội bộ', 'ROAD', '[[105.8409595, 21.0034075], [105.8409595, 21.0033458], [105.8409594, 21.0032023], [105.8409031, 21.0032023]]', 28.661546541389136, false);
INSERT INTO campus_ways (name, way_type, coordinates, distance_meters, is_oneway)
VALUES ('Đường nội bộ', 'ROAD', '[[105.8409595, 21.0033458], [105.8411216, 21.0033444], [105.8411558, 21.0033442], [105.8412322, 21.0033436]]', 28.30926124542664, false);
INSERT INTO campus_ways (name, way_type, coordinates, distance_meters, is_oneway)
VALUES ('Đường nội bộ', 'ROAD', '[[105.8409595, 21.0034075], [105.8409599, 21.0036617], [105.8405659, 21.0036651]]', 69.16743057957771, false);
INSERT INTO campus_ways (name, way_type, coordinates, distance_meters, is_oneway)
VALUES ('Đường nội bộ', 'ROAD', '[[105.8408845, 21.0040075], [105.8408857, 21.0040773], [105.8408862, 21.0041491], [105.8408877, 21.004196]]', 20.963736014847726, false);
