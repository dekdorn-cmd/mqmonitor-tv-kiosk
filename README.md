# 📺 MQMonitor Android TV Kiosk

แอปพลิเคชัน **Android TV (WebView Kiosk)** สำหรับแสดงผลหน้าจอระบบคิว `MQMonitor` แบบเต็มจอ พร้อมระบบเล่นเสียงเรียกคิวอัตโนมัติและเปิดใช้งานทันทีเมื่อเปิดทีวี

---

## ✨ คุณสมบัติหลัก

- 🖥️ **Fullscreen Kiosk Mode** — แสดงผลเต็มจอซ่อนแถบเมนูและแถบสถานะ พร้อมป้องกันหน้าจอพัก (`Keep Screen On`)
- 🔊 **Autoplay Audio / Video** — เล่นเสียงเรียกคิวได้ทันทีโดยไม่ต้องกดหน้าจอก่อน (`mediaPlaybackRequiresUserGesture = false`)
- 🌐 **รองรับ HTTP ภายในวง LAN** — เปิดใช้งาน `http://172.1.1.96/MQMonitor/` ได้ทันที (`usesCleartextTraffic`)
- ⚡ **Auto-start on Boot** — เปิดแอปขึ้นมาแสดงผลอัตโนมัติทันทีเมื่อเปิดเครื่อง Android TV
- 🎮 **ควบคุมง่ายผ่านรีโมท TV**
  - กดปุ่ม **Back** เพื่อแสดงหน้าต่างยืนยันก่อนปิดแอป (พร้อมปุ่ม **ปิดแอป** / **แก้ไข IP/URL** / **ยกเลิก**)
  - กดปุ่ม **Menu** หรือ **Settings** เพื่อเปิดหน้าแก้ไข IP / URL ได้ทันที
- 🔄 **Auto-Retry เมื่อเน็ตยังไม่พร้อม** — หากทีวีบูตเสร็จก่อน Wi-Fi/LAN เชื่อมต่อ หน้าจอจะลองเชื่อมต่อใหม่อัตโนมัติทุก **5 วินาที**

---

## 🛠️ วิธี Build เป็นไฟล์ `.apk`

### ☁️ วิธีที่ 1: Build ผ่าน GitHub Actions (แนะนำ — ไม่ต้องลงโปรแกรมในเครื่อง)
1. นำไฟล์ทั้งหมดในโฟลเดอร์ `android-tv-kiosk` อัปโหลดขึ้น Repository บน GitHub *(ให้โฟลเดอร์ `.github/` อยู่ที่ root ของ repo)*
2. เข้าไปที่แท็บ **Actions** บน GitHub
3. เลือก Workflow **Build Android TV Kiosk APK** แล้วกด **Run workflow** *(หรือ push โค้ดขึ้น branch `main` / `master`)*
4. เมื่อ Build เสร็จสิ้น ดาวน์โหลดไฟล์ **`MQMonitor-TV-Kiosk-APK`** (`app-debug.apk`) ได้จากหัวข้อ **Artifacts**

### 💻 วิธีที่ 2: Build ผ่าน Android Studio
1. เปิด **Android Studio** เลือก **Open** แล้วชี้ไปที่โฟลเดอร์ `android-tv-kiosk`
2. รอให้ **Gradle Sync** ทำงานจนเสร็จสมบูรณ์
3. ไปที่เมนู **Build > Build Bundle(s) / APK(s) > Build APK(s)**
4. นำไฟล์ที่ได้จาก `app/build/outputs/apk/debug/app-debug.apk` ไปติดตั้งลงบน Android TV

---

## 🎮 วิธีใช้งานบน Android TV

| ปุ่มบนรีโมท | การทำงาน |
| :--- | :--- |
| 🔙 **Back** | เปิดหน้าต่างถามยืนยันก่อนออกจากแอป (เลือก **ปิดแอป**, **แก้ไข IP / URL**, หรือ **ยกเลิก**) |
| ⚙️ **Menu / Settings** | เปิดหน้าต่างสำหรับ **แก้ไข IP / URL** ได้ทันที (และบันทึกค่าถาวรในเครื่อง) |

---

## 💡 ข้อแนะนำเพิ่มเติม (สำหรับ Auto-start บน Android TV 10+)

บน Android TV รุ่นใหม่ (Android 10 ขึ้นไป) ระบบอาจจำกัดการเปิดแอปอัตโนมัติหลังบูตเครื่อง หากเปิดทีวีแล้วแอปยังไม่เด้งขึ้นมาเอง ให้ตั้งค่าเพิ่มดังนี้:

1. เข้าไปที่ **Settings (การตั้งค่า)** ของ Android TV
2. เลือก **Apps > Special app access > Display over other apps**
3. เปิดอนุญาตให้แอป **MQMonitor Kiosk**
