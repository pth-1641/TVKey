# TVKey — bàn phím Android TV

Bàn phím tiếng Anh, MVP Android IME Kotlin, thiết kế cho Android TV/Android x86 và điều khiển D-pad. Ứng dụng offline, không khai báo quyền Internet, không lưu nội dung đã gõ. Package: `com.tvkey.ime`.

## Build APK

Cần JDK 17 và Android SDK (platform 35 và build-tools do Android Gradle Plugin yêu cầu). Gradle wrapper dùng Gradle 8.7. Từ thư mục repo:

```sh
./gradlew testDebugUnitTest assembleDebug
```

APK đầu ra: `app/build/outputs/apk/debug/app-debug.apk`.

## Cài và chọn IME

1. Cài APK trên thiết bị.
2. Mở TVKey, chọn **1. Bật TVKey**, bật TVKey trong trang cài đặt Android.
3. Quay lại, chọn **2. Chọn TVKey**, chọn TVKey trong picker.
4. Mở ô tìm kiếm bất kỳ. Dùng D-pad để focus, OK/Enter để gõ, Back để đóng IME. Chuột bay/USB click trực tiếp được.

Android bắt buộc người dùng tự bật và chọn IME; app không tự thay đổi IME mặc định. Nếu không hiện, kiểm tra lại trang Ngôn ngữ & nhập liệu, khởi động lại ứng dụng đích/thiết bị, và kiểm tra ứng dụng đó có sử dụng InputConnection chuẩn hay không. Một số app/ROM giữ D-pad hoặc không chấp nhận IME bên thứ ba; TVKey không vượt qua giới hạn đó.

## Ngôn ngữ

Chỉ có bố cục tiếng Anh, kiểu bàn phím Google TV: trải kín chiều ngang, số `1–0` hiện ở góc các phím hàng trên (bấm giữ chuột để gõ số), `◀ ▶` di chuyển con trỏ chữ, `?123` → `=\<` mở 2 trang ký hiệu. Shift bấm 1 lần = hoa một chữ, 2 lần = Caps Lock, 3 lần = tắt. Hàng trên cùng có phím gợi nhanh `youtube`, `.com` (ô URL/email: `.com`, `www.`, `.vn`).

## Kiểm thử và phạm vi

Unit tests bao gồm điều hướng D-pad theo hình học (kể cả phím rộng và phím đang phóng to) và bố cục phím. Kiểm thử thực tế trên Bliss OS/thiết bị, kích thước 720p/1080p và tương thích từng app cần thực hiện trực tiếp trên thiết bị. Mật khẩu không được ghi log/lưu; TVKey không cung cấp gợi ý hay lịch sử. Enter chuyển thành Search/Go/Done/Next theo `EditorInfo` khi có, fallback Enter nếu không có action.

## CI: tự build APK trên GitHub

Mỗi lần push/PR, workflow `.github/workflows/build-apk.yml` chạy unit test và build APK. Tải file ở tab **Actions → lần chạy → Artifacts → TVKey-debug-apk**. Push tag dạng `v0.1.0` để APK được đính kèm vào **Releases**.

## Thử trên emulator Android TV (không cần TV thật)

```sh
sdkmanager "emulator" "system-images;android-34;android-tv;arm64-v8a"   # Mac Apple Silicon
avdmanager create avd -n tv -k "system-images;android-34;android-tv;arm64-v8a"
emulator -avd tv
adb install -r app/build/outputs/apk/debug/app-debug.apk
adb shell ime enable com.tvkey.ime/.TvKeyImeService && adb shell ime set com.tvkey.ime/.TvKeyImeService
```

Mẹo: đừng dùng `adb shell am force-stop com.tvkey.ime` — Android sẽ tự bỏ chọn TVKey. Cài lại APK cũng làm mất lựa chọn, cần chạy lại `ime set`. Phím mũi tên/Enter/Esc trên Mac tương ứng D-pad/OK/Back.
