# Privacy Policy for Shot Mobile & Wear OS

**Last Updated**: August 16, 2026

**Shot** ("we", "our", or "us") respects your privacy. This Privacy Policy explains how your information is handled when you use the Shot Mobile Android application and the Shot Wear OS companion app (collectively, the "App").

---

## 1. Information We Collect and Process

Shot is designed with a **privacy-first approach**. All your archery data remains stored locally on your device.

### A. Archery Session Data
- **Data Collected**: Target rounds, arrow score values, plotted 2D target coordinates, sight marks, bow profiles, arrow sets, target locations, and session timestamps.
- **Storage Location**: Stored locally on your device in an encrypted SQLite database via Android Room.
- **Third-Party Access**: We do **NOT** transmit, upload, sell, or share your archery session data to external servers or third parties.

### B. Wear OS & Sensor Data (Smartwatch Companion)
- **Data Collected**: Heart rate (beats per minute) via watch sensors (`BODY_SENSORS`), touch inputs, and session scores.
- **Data Transmission**: Synchronized strictly between your paired Android phone and Wear OS smartwatch via Google Play Services Wearable Data Layer (`com.google.android.gms.wearable`). Data remains on your paired devices.

### C. System Permissions
- **WAKE_LOCK / Keep Screen On**: Used strictly to keep the screen active while scoring ends.
- **BODY_SENSORS (Wear OS)**: Used strictly to read real-time heart rate metrics during active sessions.

---

## 2. Third-Party Services & Tracking

- **No Ad Networks or Trackers**: Shot contains **zero advertising**, analytics trackers, or third-party behavioral profiling scripts.
- **Google Play Services**: Used for Wear OS device pairing and Play Store license compliance. Google's privacy policy governs Play Services: [https://policies.google.com/privacy](https://policies.google.com/privacy).

---

## 3. Data Storage, Security, and Backup

- **Local Storage**: All data resides in local application sandboxes on your phone and watch.
- **Database Backup & Restore**: You can export your full database to a JSON backup file stored in your device storage or Google Drive. You maintain full ownership and control over backup files.

---

## 4. User Rights & Data Deletion

You have complete control over your data:
- **Delete Individual Data**: Delete sessions, bow profiles, archers, or sight marks at any time within the App.
- **Clear All App Data**: Go to your device **Settings > Apps > Shot > Storage > Clear Data** to permanently erase all locally stored data.

---

## 5. Contact Us

If you have any questions or feedback regarding this Privacy Policy, please reach out via GitHub Issues:
[https://github.com/seyone22/shot-mobile/issues](https://github.com/seyone22/shot-mobile/issues)
