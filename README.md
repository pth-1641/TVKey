# TVKey — bàn phím cho Android TV / Bliss OS

Bàn phím tiếng Anh kiểu Google TV, to và rõ khi ngồi xa, điều khiển được bằng **điều khiển bay (chuột), remote D-pad** hoặc bàn phím vật lý. Chạy offline: không quyền Internet, không quảng cáo, không thu thập hay lưu nội dung đã gõ. Package: `com.tvkey.ime`.

## Tải về và cài đặt

1. Vào mục **[Releases](../../releases/latest)** của repo này, tải file **`TVKey.apk`**.
2. Chép APK vào thiết bị (USB, hoặc `adb install -r TVKey.apk`) rồi mở file để cài. Nếu máy hỏi, cho phép cài từ nguồn không rõ.
3. Mở ứng dụng **TVKey** → bấm **1. Bật TVKey** → bật công tắc TVKey trong trang cài đặt bàn phím của Android.
4. Quay lại TVKey → bấm **2. Chọn TVKey** → chọn **TVKey** trong danh sách.
5. Mở ô tìm kiếm hoặc trình duyệt bất kỳ, bấm vào ô nhập: bàn phím sẽ hiện ở nửa dưới màn hình.

Android bắt buộc người dùng tự bật và tự chọn bàn phím; app không thể tự đổi. Dùng ô thử ngay trong màn hình thiết lập của TVKey để kiểm tra.

> APK được ký bằng khóa debug (đủ để cài trực tiếp, không dùng để đưa lên cửa hàng ứng dụng).

## Cách dùng

| Thao tác | Điều khiển bay / chuột | Remote D-pad |
|---|---|---|
| Gõ phím | Bấm vào phím | Mũi tên để chọn phím, **OK** để gõ |
| Xóa | Bấm `⌫` (giữ để xóa liên tục) | Chọn `⌫`, giữ **OK** |
| Đóng bàn phím | Nút **Back** của điều khiển | **Back** |
| Gõ số | Bấm giữ chuột vào phím hàng trên (có số nhỏ ở góc), hoặc dùng `?123` | Như trên |

Bố cục:
- Hàng trên `qwertyuiop` có số `1–0` nhỏ ở góc phím.
- `⇧` bấm 1 lần = viết hoa một chữ, 2 lần = khóa Caps Lock, 3 lần = tắt.
- `?123` mở trang ký hiệu (`@ # $ _ & - + ( ) /`...); trong đó `=\<` mở trang ký hiệu thứ hai (`~ ` | • √ π £ € ¥`...), `123?` quay lại trang một, `ABC` về chữ cái.
- `◀ ▶` di chuyển con trỏ trong ô nhập.
- `␣` là phím cách. Phím bên phải cùng là Enter: tự đổi thành **Tìm / Đi / Xong / Tiếp** theo từng ô nhập. Ở ô URL/email, phím cạnh phím cách là `@`.
- Ô nhập số/điện thoại tự dùng bố cục số.

## Nếu bàn phím không hiện

- Mở **Cài đặt › Ngôn ngữ & nhập liệu** và chắc chắn TVKey đã được bật và được chọn.
- Thử bấm lại vào ô nhập, hoặc khởi động lại ứng dụng đang dùng.
- Một số app/ROM không dùng cơ chế nhập chuẩn của Android hoặc không chấp nhận bàn phím bên thứ ba; TVKey không thể vượt qua giới hạn đó.
- Sau khi **cài lại/cập nhật APK**, Android có thể bỏ chọn TVKey: vào lại màn hình thiết lập và bấm **2. Chọn TVKey**.

## Dành cho người phát triển

### Build

Cần JDK 17 và Android SDK (platform 35). Gradle 8.7 được script `gradlew` tự tải.

```sh
./gradlew testDebugUnitTest assembleDebug
```

APK: `app/build/outputs/apk/debug/app-debug.apk`.

### CI và phát hành

Mỗi lần push/PR, GitHub Actions (`.github/workflows/build-apk.yml`) chạy unit test và build APK; tải ở **Actions → lần chạy → Artifacts → TVKey-apk**. Để phát hành: đẩy tag dạng `v0.1.0`, workflow sẽ tự tạo **Release** có đính kèm `TVKey.apk`.

```sh
git tag v0.1.1 && git push origin v0.1.1
```

(Nhớ tăng `versionCode`/`versionName` trong `app/build.gradle.kts` trước khi gắn tag.)

### Thử trên emulator Android TV (không cần TV thật)

```sh
sdkmanager "emulator" "system-images;android-34;android-tv;arm64-v8a"   # Mac Apple Silicon
avdmanager create avd -n tv -k "system-images;android-34;android-tv;arm64-v8a"
emulator -avd tv
adb install -r app/build/outputs/apk/debug/app-debug.apk
adb shell ime enable com.tvkey.ime/.TvKeyImeService && adb shell ime set com.tvkey.ime/.TvKeyImeService
```

Emulator Android TV chưa có trình duyệt; có thể cài Firefox APK để thử bàn phím trong ô địa chỉ. Đừng dùng `adb shell am force-stop com.tvkey.ime` vì Android sẽ bỏ chọn TVKey; cài lại APK cũng cần chạy lại `ime set`. Phím mũi tên/Enter/Esc trên Mac tương ứng D-pad/OK/Back.

### Kiểm thử

Unit test gồm điều hướng D-pad theo hình học (phím rộng, phím đang phóng to, toàn bộ bố cục) và bố cục các trang phím. Chưa kiểm thử trên thiết bị Bliss OS thật; các app không dùng cơ chế nhập chuẩn của Android có thể không nhận bàn phím.
