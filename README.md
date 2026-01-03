# 📱 DoAnCuoiKyMobile

**DoAnCuoiKyMobile** là một **ứng dụng Android hoàn chỉnh** được xây dựng bằng Kotlin, cung cấp trải nghiệm nghe nhạc đa nguồn, quản lý playlist, tài khoản người dùng, và tương tác với dữ liệu backend qua Firebase & Deezer API. Đây là đồ án cuối kỳ mobile app với đầy đủ các tính năng hiện đại. 

---

## 🚀 Tính năng nổi bật

### 🎧 Tính năng người dùng

* 🔐 **Đăng nhập & Đăng ký**

  * Xác thực người dùng bằng email/password và Google Sign-In. 

* 🎵 **Nghe nhạc & Player**

  * Phát nhạc từ các nguồn online (Deezer API) và local.
  * Giao diện player tiện lợi với thông báo điều khiển (Notification). 

* 📚 **Playlist & Library**

  * Tạo / quản lý playlist, thêm bài hát yêu thích.
  * Lưu lịch sử bài nghe gần đây. 

* 🔍 **Tìm kiếm nâng cao**

  * Tìm bài hát, nghệ sĩ theo từ khóa với gợi ý thông minh. 

* ❤️ **Favorites, Follow**

  * Thả tim bài hát, theo dõi nghệ sĩ yêu thích. 

* 🧑‍💼 **Profile & Setting**

  * Chỉnh sửa thông tin cá nhân, avatar, cài đặt ứng dụng. 

---

## 🧠 Kiến trúc & Công nghệ

### 📌 Tech Stack

| Layer      | Công nghệ                                      |
| ---------- | ---------------------------------------------- |
| UI         | Kotlin + Jetpack Compose / XML                 |
| Data       | Firebase Auth, Firestore, Realtime DB, Storage |
| Network    | Retrofit + OkHttp                              |
| Media      | Android Media3 (ExoPlayer), Notification       |
| Async      | Kotlin Coroutines                              |
| Navigation | Navigation Component / Compose                 |
| Test       | JUnit, MockK, Robolectric        |

---

## 🛠️ Cài đặt & Chạy project

### 1) Clone repo

```bash
git clone https://github.com/Mirra-mira/DoAnCuoiKyMobile.git
cd DoAnCuoiKyMobile
```

### 2) Cấu hình Firebase

Đặt file `google-services.json` vào:

```
app/google-services.json
```

### 3) Build & Run

Mở bằng **Android Studio (Arctic Fox trở lên)**:

* Sync Gradle
* Chạy app trên thiết bị hoặc emulator Android API 29+ 

---

## 📁 Cấu trúc dự án

```
app/
├─ src/
│  ├─ main/
│  │  ├─ java/com/example/doancuoikymobile/
│  │  │  ├─ adapter/                   # các RecyclerView/Compose Adapter
│  │  │  ├─ data/
│  │  │  │  ├─ local/                  # dữ liệu local
│  │  │  │  └─ remote/                 # Deezer + Firebase
│  │  │  ├─ model/                     # data class
│  │  │  ├─ player/                    # media player service
│  │  │  ├─ repository/                # class repository
│  │  │  ├─ ui/                        # screens và fragments
│  │  │  ├─ utils/                     # utils & extensions
│  │  │  └─ viewmodel/                 # ViewModels
│  │  └─ res/                         # layout / drawable / values
```

*Đầy đủ theo chuẩn Android MVVM* 

---

## 🧪 Với lập trình viên / reviewer

### 📦 API & Data Flow

* **Deezer API** qua Retrofit cho dữ liệu bài hát & playlist. 
* **Firebase** dùng để xác thực người dùng, lưu bài yêu thích, playlist của user. 

### 🧠 State & Architecture

Áp dụng **MVVM + Repository + LiveData / Compose UI** → dễ bảo trì và test. 

### 🔄 Coroutine & Async

Xử lý request non-blocking với Coroutines để UI luôn mượt. 

---

## 🧑‍💻 Testing

* Unit test cho các repository & API service.
* Instrumentation test cơ bản cho UI. 

---

## 🧩 Đóng góp

Chào mừng mọi đóng góp 🌟
Bạn có thể:

* Tạo issue
* Gửi PR
* Đề xuất tính năng mới

---

## 📜 License

MIT License — Xem `LICENSE` để biết chi tiết.

---
## 💬 Tác giả

Dự án được thực hiện bởi các thành viên nhóm:

| Tên thành viên | Mã số sinh viên | GitHub |
| :--- | :--- | :--- |
| Nguyễn Chính Đạt | 33241024002 | [Link GitHub](https://github.com/Mirra-mira) |
| Phạm Thị Kim Hồng | 31221025429 | [Link GitHub](https://github.com/kimhongpham) |
| Trịnh Thảo Minh | 33241024113 | [Link GitHub](https://github.com/trinhthaominh) |
| Nguyễn Thanh Tùng | 33241024064 | [Link GitHub](https://github.com/thanhtung241202) |
