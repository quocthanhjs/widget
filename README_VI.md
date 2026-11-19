# DQT Lunar Weather Widget – 3 giao diện Liquid Glass + cài đặt

## Hai widget
1. **Kiểu 1 – Đa năng:** đồng hồ số lớn, ngày dương và âm, lịch tháng âm/dương, thời tiết, vị trí hiện tại.
2. **Kiểu 2 – Đồng hồ tròn:** mặt đồng hồ kim truyền thống, tùy chọn số thường/La Mã, lời hay ý đẹp, tháng dương/âm, thời tiết.
3. **Kiểu 3 – Tối giản nghệ thuật:** nền tối, thời tiết/vị trí phía trên, ngày dương lớn, ngày âm, lịch tháng và câu lời hay ý đẹp.

## Cài đặt đã có trong mã nguồn
- Chọn 1 trong 3 giao diện widget.
- Khung xem trước trong Cài đặt cập nhật theo giao diện, màu nền, độ trong suốt và câu chữ trước khi áp dụng.
- Chọn số thường hoặc số La Mã trên mặt đồng hồ tròn.
- Chọn AccuWeather API hoặc mở ứng dụng thời tiết trên điện thoại.
- Nhập AccuWeather API Key và Location Key.
- Chu kỳ cập nhật: 15 phút, 30 phút, 1 giờ, 3 giờ.
- Tự nhập câu “Lời hay – Ý đẹp”.
- Đổi nhãn vị trí.
- Chọn 5 màu nền.
- Điều chỉnh độ đậm/độ trong suốt 0–100%.

## Build APK bằng Android Studio
1. Giải nén ZIP.
2. Mở thư mục dự án trong Android Studio.
3. Dùng JDK 17, cài Android SDK Platform 35.
4. Chạy `gradle :app:assembleDebug`.
5. APK: `app/build/outputs/apk/debug/app-debug.apk`.

## Build bằng GitHub
1. Tạo repository mới trên GitHub.
2. Upload toàn bộ nội dung thư mục dự án, commit nhánh `main`.
3. Vào Actions → Build DQT Lunar Weather APK.
4. Tải artifact `DQT-Lunar-Weather-APK`.

## Thêm widget
Nhấn giữ màn hình chính → Widgets/Tiện ích:
- DQT Digital Widget
- DQT Analog Widget

Có thể thêm một trong hai mục widget. Lựa chọn Kiểu 1/Kiểu 2/Kiểu 3 trong Cài đặt sẽ áp dụng layout đã chọn lên các widget DQT đang đặt trên màn hình. Launcher có thể yêu cầu chỉnh kích thước widget.

## Thời tiết và vị trí theo điện thoại
- Đã bỏ hoàn toàn AccuWeather và các trường API Key/Location Key.
- Ứng dụng xin quyền vị trí GPS/mạng của điện thoại, lấy tọa độ hiện tại và dùng tọa độ đó để truy vấn thời tiết từ Open-Meteo.
- Open-Meteo không cần API key cho mức sử dụng thông thường; dữ liệu chỉ gồm thời tiết hiện tại.
- Khi mở ứng dụng hoặc nhấn “Cập nhật vị trí từ điện thoại”, ứng dụng lấy lại vị trí hiện tại. Chu kỳ 15 phút/30 phút/1 giờ/3 giờ làm mới dữ liệu thời tiết tại tọa độ đã lưu.
- Nếu người dùng di chuyển sang địa điểm khác, mở ứng dụng và nhấn cập nhật vị trí để lấy tọa độ mới. Android giới hạn việc truy cập GPS liên tục trong nền để tiết kiệm pin.

Lưu ý: Widget không đọc dữ liệu riêng bên trong ứng dụng Thời tiết của hãng điện thoại, vì Android không có API chung cho việc đó. Thay vào đó, widget tự lấy vị trí từ điện thoại rồi lấy thời tiết trực tuyến cho đúng vị trí đó. Nút chạm vào thời tiết vẫn thử mở ứng dụng Thời tiết của máy.

## Các giới hạn cần biết
- Đây là bộ mã nguồn chức năng nền tảng, không cam kết pixel-perfect 100% với ảnh trên mọi launcher; kích thước, font và RemoteViews phụ thuộc Android/launcher.
- Đồng hồ số dùng TextClock hệ thống. Đồng hồ kim được vẽ lại khi widget cập nhật; muốn kim chạy từng phút cần kiểm thử chính sách nền trên thiết bị thật.
- Lịch âm dùng thuật toán chuyển đổi Việt Nam UTC+7; cần đối chiếu lịch chuẩn các năm trước khi phát hành chính thức.
- AccuWeather cần API key/location key hợp lệ và quyền truy cập phù hợp.
- Một số thành phần minh họa như biểu tượng thời tiết và forecast tuần phụ thuộc dữ liệu API; bản này chưa có tích hợp tự động lấy tọa độ GPS hoặc dự báo (đã loại bỏ theo yêu cầu).

## Kiểm thử
Build trên Android Studio/JDK 17 trước; sau đó kiểm thử trên hãng/model điện thoại thật, kiểm tra widget resize, chế độ tiết kiệm pin, quyền mạng, API quota và lịch âm.

## Cập nhật giao diện Liquid Glass
- Cài đặt được thiết kế lại theo tông kính tối, có khung xem trước ngay đầu màn hình.
- Xem trước thay đổi theo 3 kiểu widget, màu nền, độ trong suốt và câu chữ trước khi nhấn Lưu & Áp dụng.
- Đã loại bỏ các vùng dự báo 7 ngày và phần truy vấn dữ liệu dự báo; widget chỉ lấy thời tiết hiện tại.
- Lịch tháng hiển thị ngày dương/ngày âm theo từng ô dạng lưới ký tự cố định để hạn chế xuống dòng lộn xộn.
