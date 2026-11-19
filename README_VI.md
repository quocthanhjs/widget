# DQT Lunar Weather Widget

Ứng dụng widget Android hiển thị giờ, ngày dương lịch, lịch âm Việt Nam và thời tiết hiện tại.

## Ba giao diện widget
1. **Đa năng:** đồng hồ số lớn, ngày dương/âm, thời tiết hiện tại và lịch tháng dạng lưới.
2. **Số tối giản:** đồng hồ số lớn, ngày dương/âm, thời tiết hiện tại và câu chữ tùy chỉnh.
3. **Đồng hồ tròn:** mặt đồng hồ kim, số thường hoặc số La Mã, ngày dương/âm và thời tiết hiện tại.

## Cài đặt
- Xem trước giao diện trực tiếp trước khi áp dụng.
- Chọn mẫu widget, màu nền Liquid Glass và độ đậm nền.
- Chọn kiểu số trên mặt đồng hồ tròn.
- Chỉnh câu chữ hiển thị.
- Chọn chu kỳ cập nhật thời tiết.
- Lấy vị trí theo GPS/mạng của điện thoại.

## Thời tiết
Ứng dụng lấy dữ liệu thời tiết hiện tại từ Open-Meteo theo tọa độ điện thoại. Widget chỉ hiển thị nhiệt độ, biểu tượng, tình trạng thời tiết và địa điểm; không hiển thị dự báo 7 ngày. Chạm vào khu vực thời tiết để thử mở ứng dụng thời tiết có sẵn trên máy.

## Build APK bằng GitHub Actions
1. Giải nén ZIP và tải toàn bộ thư mục dự án lên repository GitHub.
2. Mở tab **Actions**.
3. Chọn workflow **Build DQT Lunar Weather APK** và chạy **Run workflow**.
4. Tải artifact **DQT-Lunar-Weather-APK** sau khi workflow thành công.

Yêu cầu build: JDK 17, Gradle 8.9, Android Gradle Plugin 8.7.3.
