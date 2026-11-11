# TVKey

Bàn phím tiếng Anh cho Android TV / Bliss OS, làm theo kiểu bàn phím Google TV. Phím to, ngồi xa vẫn nhìn rõ. Dùng được với điều khiển bay, remote D-pad hoặc bàn phím thật.

App không xin quyền Internet và không lưu những gì bạn gõ. Package: `com.tvkey.ime`.

## Cài đặt

1. Vào trang [Releases](../../releases/latest), tải file `TVKey.apk`.
2. Chép vào máy (USB hoặc `adb install -r TVKey.apk`) rồi mở file để cài. Nếu máy hỏi thì cho phép cài từ nguồn không rõ.
3. Mở app TVKey, bấm **1. Bật TVKey**, rồi bật công tắc TVKey trong cài đặt bàn phím.
4. Quay lại TVKey, bấm **2. Chọn TVKey** và chọn TVKey trong danh sách.
5. Bấm thử vào ô nhập ngay trong app. Bàn phím sẽ hiện ở cuối màn hình.

Android bắt người dùng tự bật và tự chọn bàn phím, app không làm thay được.

APK ký bằng khóa debug, chỉ đủ để cài trực tiếp.

## Cách dùng

| | Điều khiển bay / chuột | Remote |
|---|---|---|
| Gõ phím | Bấm vào phím | Mũi tên chọn phím, OK để gõ |
| Xóa | Bấm `⌫`, giữ để xóa liên tục | Chọn `⌫`, giữ OK |
| Đóng bàn phím | Nút Back | Back |
| Gõ số | Giữ chuột vào phím hàng trên, hoặc bấm `?123` | Giống bên trái |

Các phím:

- Hàng trên cùng có số 1 đến 0 nhỏ ở góc phím.
- Phím Shift: bấm 1 lần để viết hoa một chữ, bấm 2 lần để khóa Caps Lock, bấm lần ba để tắt.
- `?123` mở trang ký hiệu. Trong đó `=\<` sang trang ký hiệu thứ hai, `123?` quay lại trang một, `ABC` về chữ cái. Khi đổi trang, ô đang chọn đứng yên tại chỗ.
- `◀ ▶` di chuyển con trỏ.
- `Xóa hết` xóa toàn bộ nội dung ô nhập.
- Phím dài ở giữa là dấu cách. Phím cuối hàng là Enter, tên đổi theo ô nhập: Tìm, Đi, Xong hoặc Tiếp.
- Ô địa chỉ web và email có thêm `@` và `.com`.
- Ô nhập số hoặc điện thoại tự dùng bố cục số.

## Bàn phím không hiện

- Vào Cài đặt, Ngôn ngữ và nhập liệu, kiểm tra TVKey đã bật và đang được chọn.
- Bấm lại vào ô nhập, hoặc mở lại app đang dùng.
- Cài lại hoặc cập nhật APK có thể làm Android bỏ chọn TVKey. Mở app và bấm **2. Chọn TVKey** lần nữa.
- Vài app hoặc ROM không dùng cơ chế nhập chuẩn của Android nên không nhận bàn phím bên thứ ba. Cái này TVKey không sửa được.

## Cho người muốn build

Cần JDK 17 và Android SDK (platform 35). Gradle 8.7 do script `gradlew` tự tải.

```sh
./gradlew testDebugUnitTest assembleDebug
```

APK nằm ở `app/build/outputs/apk/debug/app-debug.apk`.

GitHub Actions (`.github/workflows/build-apk.yml`) chạy test và build mỗi lần push. Muốn ra bản phát hành, tăng `versionCode` và `versionName` trong `app/build.gradle.kts`, rồi đẩy một tag dạng `v0.1.2`. Workflow sẽ tạo Release kèm `TVKey.apk`.

### Thử bằng emulator

Không có TV thật thì dùng emulator Android TV:

```sh
sdkmanager "emulator" "system-images;android-34;android-tv;arm64-v8a"   # Mac chip Apple
avdmanager create avd -n tv -k "system-images;android-34;android-tv;arm64-v8a"
emulator -avd tv
adb install -r app/build/outputs/apk/debug/app-debug.apk
adb shell ime enable com.tvkey.ime/.TvKeyImeService
adb shell ime set com.tvkey.ime/.TvKeyImeService
```

Emulator TV không có sẵn trình duyệt, có thể cài thêm Firefox để thử ô địa chỉ. Trên Mac, phím mũi tên, Enter và Esc tương ứng D-pad, OK và Back.

Đừng chạy `adb shell am force-stop com.tvkey.ime`, Android sẽ bỏ chọn TVKey. Cài lại APK cũng vậy, phải chạy lại lệnh `ime set`.

Unit test kiểm tra việc di chuyển giữa các phím và các bố cục. Mới thử trên emulator, chưa thử trên Bliss OS hay TV thật.
