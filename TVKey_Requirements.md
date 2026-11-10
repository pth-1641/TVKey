# TVKey — Android TV Remote Keyboard

## 1. Mục tiêu

Xây dựng ứng dụng bàn phím ảo Android có thể **cài bằng APK** và chọn làm bàn phím mặc định (Input Method / IME) trên thiết bị Android x86 / Bliss OS 14 đang được sử dụng như TV Box. Mục tiêu là trải nghiệm gõ tìm kiếm như bàn phím trên Android TV: chữ và nút lớn, nhìn rõ từ sofa, **điều khiển hoàn toàn bằng remote D-pad** (trái/phải/lên/xuống/OK/Back) và cũng hỗ trợ chuột bay/chuột USB; không phụ thuộc màn hình cảm ứng.

Tên sản phẩm: **TVKey**. Package gợi ý: `com.tvkey.ime` (kiểm tra xung đột trước khi dùng). Giao diện và hành vi cần phù hợp với người lớn tuổi, thao tác ít, dễ hiểu.

## 2. Phạm vi và nền tảng

- Kotlin, Android Studio, Gradle, Android SDK.
- Android `InputMethodService` là thành phần bắt buộc: tạo IME thật, **không** chỉ làm một Activity/overlay giả bàn phím. Khi người dùng chọn làm IME mặc định, bàn phím xuất hiện trong các trường nhập liệu tiêu chuẩn.
- Mục tiêu kiểm thử chính: Bliss OS 14 (Android 11-based nếu đúng bản đang cài; **kiểm tra API thực tế**, đừng giả định). Ưu tiên Android API 30+ và khả năng chạy trên Android x86_64; viết code độc lập ABI nếu không cần native.
- Thiết kế cho TV độ phân giải 1280×720 và 1920×1080, xoay ngang, không che mất toàn bộ nội dung cần xem.
- Hoạt động offline. Không cần tài khoản, mạng, quảng cáo, tracking, quyền nhạy cảm hay server.
- Không cần quyền hệ thống/root. Các trường hợp ứng dụng hoặc trang web không hỗ trợ IME bên thứ ba phải được giải thích là giới hạn, không tìm cách can thiệp trái phép.

## 3. Trải nghiệm chính

### 3.1 Bố cục

- Bàn phím ngang chiếm vùng dưới màn hình, nền tối, chữ trắng, focus nổi bật (viền sáng/màu tương phản); giao diện giống tinh thần Android TV nhưng có thiết kế riêng, **không sao chép tài sản thương hiệu của Google**.
- Phím kích thước lớn, spacing thoáng, vùng chọn rõ từ khoảng cách 2–3 mét.
- Hàng đầu là vùng gợi ý nhập liệu (tùy chọn cho MVP), trạng thái ngôn ngữ `EN`/`VI`, và nút đóng bàn phím.
- Lưới chữ ưu tiên QWERTY quen thuộc (cho phép chọn bố cục ABC nếu thực sự thuận tiện, nhưng không bắt buộc MVP).
- Các phím chức năng phải dễ tìm: `123`, `ABC`, `Shift`, `Space` (rộng), `Xóa`, `Enter/Tìm`, `Ngôn ngữ`; có thể có `@`, `.`, `/` ở các ngữ cảnh URL/email.
- Màu focus có thể nhìn rõ ngay cả khi remote không có con trỏ.

### 3.2 Remote / phím điều hướng

- D-pad LEFT/RIGHT/UP/DOWN di chuyển focus theo tọa độ phím gần nhất và **không bị kẹt** ở hàng có phím rộng (Space, Enter).
- CENTER / ENTER xác nhận phím đang focus và gửi ký tự qua `InputConnection`.
- BACK ưu tiên đóng IME và trả focus về trường nhập; không tự ý thoát cả ứng dụng đang dùng.
- Nhấn giữ phím xóa: xóa lặp, có độ trễ và nhịp hợp lý; tránh xóa hết do một lần click.
- Tương thích thiết bị gửi `KEYCODE_DPAD_CENTER`, `KEYCODE_ENTER`, các D-pad, `KEYCODE_BACK`; tránh xử lý hai lần nếu sự kiện được hệ thống gửi theo nhiều đường.
- Mỗi lần IME xuất hiện, chọn focus mặc định hợp lý và nhớ focus trong phiên nếu hữu ích.
- Hỗ trợ chuột bay: click vào phím hoạt động; hover có thể highlight, nhưng không làm hỏng focus remote.
- Hỗ trợ bàn phím USB/Bluetooth vật lý: không chặn ký tự hay phím tắt hệ thống không cần thiết.

### 3.3 Nhập văn bản

- `commitText()` với chữ/ký tự thông thường; `deleteSurroundingText()` hoặc API phù hợp cho Backspace; `performEditorAction()` với Search/Go/Done/Next theo `EditorInfo.imeOptions`, fallback Enter khi cần.
- Shift/caps lock có trạng thái trực quan.
- `123` chuyển qua bố cục số/ký hiệu; `ABC` quay lại chữ.
- Nhận biết `inputType` để đưa ra bố cục tối ưu cho text, number, email, URL, password. Với mật khẩu: không hiển thị/ghi log nội dung, không gợi ý lưu lịch sử.
- Tránh tự thay thế dữ liệu người dùng nếu chưa kiểm thử tốt. Không thu thập nội dung nhập.

### 3.4 Tiếng Việt

- MVP phải nhập được **Unicode tiếng Việt có dấu** theo cách dễ thao tác bằng remote.
- Ưu tiên cung cấp lựa chọn `VI` có màn hình chọn dấu/chữ (`ă`, `â`, `ê`, `ô`, `ơ`, `ư`, `đ` và dấu thanh) hoặc bộ gõ Telex khi gõ bằng bàn phím vật lý. Đối với remote, **không bắt buộc bố mẹ nhớ quy tắc Telex**.
- Thiết kế tương tác nhập dấu với remote đơn giản, số thao tác ít; làm prototype và mô tả quy tắc rõ ràng trước khi nâng cấp bộ gõ tự động.
- Không được tuyên bố hỗ trợ đầy đủ Telex nếu chưa xử lý chuẩn thứ tự dấu, chữ hoa, xóa và vị trí con trỏ.
- Cho phép EN/VI nhanh, lưu lựa chọn bằng DataStore/SharedPreferences.

## 4. Thiết lập lần đầu

Tạo một Activity cấu hình đơn giản, mở từ biểu tượng ứng dụng:

1. Nút **Bật TVKey** dẫn đến trang cài đặt bàn phím hệ thống (`Settings.ACTION_INPUT_METHOD_SETTINGS` hoặc intent phù hợp trên ROM).
2. Nút **Chọn TVKey** mở bộ chọn IME (`InputMethodManager.showInputMethodPicker()`).
3. Màn hình hướng dẫn rõ rằng Android yêu cầu người dùng **tự bật và tự chọn IME mặc định**; ứng dụng không thể tự ý thay đổi.
4. Trường nhập thử để test remote, chuột bay, tiếng Việt, Enter/Search.
5. Trang hướng dẫn khắc phục nếu ROM / app không hiển thị IME, hay con trỏ D-pad bị ứng dụng khác giữ.

Đảm bảo có `AndroidManifest.xml` khai báo service với permission `android.permission.BIND_INPUT_METHOD`, intent-filter `android.view.InputMethod`, và `android.view.im` metadata trỏ tới file XML cấu hình IME. Không yêu cầu người dùng cấp `BIND_INPUT_METHOD` trực tiếp (đây là permission hệ thống ràng buộc service).

## 5. Kiến trúc kỹ thuật mong muốn

- `TvKeyImeService : InputMethodService`: khởi tạo/dỡ keyboard view, đọc `EditorInfo`, quản lý InputConnection.
- `KeyboardView` tự xây bằng Android Views hoặc Compose nếu bảo đảm focus/key handling ổn định trong IME; ưu tiên Views cho MVP nhằm tối ưu độ tương thích Bliss OS.
- `KeyboardLayout` mô tả các hàng/phím bằng data models (`label`, `output`, `weight`, `type`, `neighbors` nếu cần).
- `RemoteFocusController`: thuật toán focus D-pad quyết định được, có unit tests; ưu tiên chọn phím có vùng hình học gần nhất khi điều hướng giữa các hàng không đều.
- `KeyboardController`: xử lý Shift, symbols, language, delete, editor actions.
- `SettingsActivity`: setup, thử nhập, cài đặt đơn giản.
- Tách resources `strings.xml`, dimensions/colors, dễ mở rộng theme.
- Không dùng quyền accessibility để gõ hộ, không dùng overlay che ứng dụng, không hook bàn phím của app khác.

## 6. Tiêu chí nghiệm thu (Definition of Done)

- [ ] Build được APK debug bằng `./gradlew assembleDebug`; cung cấp vị trí file APK.
- [ ] Cài APK trên Bliss OS, bật/chọn TVKey trong Android Settings thành công.
- [ ] Gõ được vào ô tìm kiếm launcher, trình duyệt và một app Android thông dụng **nếu các ứng dụng đó dùng cơ chế nhập chuẩn của Android**.
- [ ] Chỉ dùng D-pad + OK + Back vẫn nhập được `Hello 123`, xóa, Space, Search/Done.
- [ ] Nhập được cụm tiếng Việt có dấu ví dụ `bố mẹ xem tivi` bằng cơ chế đã thiết kế, không cần bàn phím vật lý.
- [ ] Chuột bay click được tất cả phím; focus không biến mất khi đổi qua remote.
- [ ] Không crash khi IME đóng/mở nhiều lần, đổi ô nhập, đổi hướng focus, chuyển app.
- [ ] Hiển thị rõ trên 720p/1080p; không quá nhỏ hay che hết ô tìm kiếm.
- [ ] Không có quyền Internet, quảng cáo, analytics, thu thập văn bản, log nội dung password.
- [ ] Có README hướng dẫn build, cài APK, bật/chọn bàn phím, kiểm thử trên Bliss OS.

## 7. Ngoài phạm vi bản đầu

- Nhận diện giọng nói (yêu cầu mic/dịch vụ riêng, không phải chức năng cốt lõi của IME).
- AI/autocomplete qua Internet, đồng bộ lịch sử, gõ vuốt, theme marketplace.
- Tự động bypass giới hạn ứng dụng, sửa framework hệ thống hoặc root.

## 8. Yêu cầu cụ thể cho Claude Code

**Hãy triển khai repository chạy được, không chỉ mô tả giải pháp.**

Thứ tự triển khai:
1. Khởi tạo project Android Kotlin/Gradle và service IME khai báo đầy đủ.
2. Làm bàn phím TV dark theme, responsive, D-pad focus + OK + Back + mouse click.
3. Ghép InputConnection và editor actions; bố cục letters/symbols, Shift, Space, Delete.
4. Tạo setup Activity có hướng dẫn enable/select IME và ô nhập thử.
5. Thiết kế/triển khai nhập tiếng Việt bằng remote theo mục 3.4; kiểm thử ít nhất chuỗi trong DoD.
6. Thêm unit tests cho focus navigation, test thủ công các tình huống IME.
7. Build APK, sửa compile errors và ghi README bằng tiếng Việt.

Khi viết code, ưu tiên trải nghiệm **dùng remote TV cho bố mẹ** hơn hiệu ứng phức tạp. Đừng dừng ở mockup. Nếu môi trường hiện tại không có Android SDK/thiết bị để build hoặc kiểm thử, ghi rõ phần chưa xác minh; không nói rằng đã chạy thành công.

## 9. Gợi ý nâng cấp sau MVP

- Layout `ABC` kiểu TV box, tùy chọn thay QWERTY.
- Hàng gợi ý các cụm tìm kiếm **cục bộ** không lưu dữ liệu nhạy cảm.
- Tùy chỉnh kích thước chữ, tương phản, màu focus.
- Tùy chọn giao diện light/dark.
- Quick key `.com`, `youtube`, `www.` chỉ ở URL/search khi phù hợp.
