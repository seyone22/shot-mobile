# Shot: The Next-Gen Archery Ecosystem

**Shot** is a high-performance, multi-platform archery scoring and analysis suite designed for competitive archers, coaches, and clubs. Built with an **offline-first** philosophy and **Material 3 Expressive** design, it transforms the traditional scoresheet into a data-driven coaching and equipment management engine.

## 🎯 Key Features

### 🏹 Core Scoring & Analytics
* **Visual Target Plotting:** Map shots using X,Y coordinates on vector target faces with automatic score calculation.
* **Rule Engine:** Native support for World Archery (WA), IFAA, and custom practice rounds (including inner-10 vs. outer-10 logic).
* **Advanced Metrics:** Real-time tracking of Average Arrow Value (AAV), total score, grouping diameter, and running handicaps.
* **AI Insights:** Mathematical grouping center (Barycenter) calculation to suggest sight click adjustments and detect fatigue-based form breakdown. **TODO**

### ⚙️ Equipment & Tuning
* **Component Lifecycle:** Manage detailed bow profiles down to the component level (Riser, Limbs, Arrow Spine, Rest, Plunger).
* **Arrow Fatigue:** Assign unique IDs to arrows to track individual shot counts and monitor potential damage or spine degradation. **TODO**
* **Smart Sight Marks:** Quadratic interpolation algorithm to estimate unknown distances based on existing sight tape data. **TODO**

### 🤝 Social & Club Dynamics
* **Shared Lane Syncing:** Two or more archers can link devices via QR code for synchronized real-time scoring. **TODO**
* **Live Club Line:** A read-only dashboard for club admins to monitor active tournament lines and leaderboards in real-time. **TODO**
* **Digital Witnessing:** Cryptographic digital signatures for peer-verified official club record keeping. **TODO**

### ⌚ Wear OS Integration
* **Standalone Scoring:** Full logging capability via watch crown and buttons for phone-free sessions.
* **Biometric Correlation:** Sync heart rate data with specific scoring ends to map physiological stress against accuracy.
* **Haptic Timers:** Integrated WA-standard shooting vibrations (e.g., 10-second warnings). **TODO**

## 🛠 Tech Stack

### Mobile & Wearable
* **Framework:** Android Native with **Jetpack Compose**.
* **Design Language:** Material 3 Expressive guidelines.
* **Local Database:** **Room/SQLite** for robust offline-first functionality and conflict-resolution queuing.
* **On-Device ML:** Gemini Nano or ML Kit for OCR.

### Web & Backend
* **Dashboard:** **Next.js** for coach and admin interfaces.
* **Compute:** **Cloudflare Workers** for low-latency, globally distributed execution.
* **Real-time:** **Cloudflare Durable Objects** managing WebSockets for "Shared Lane" features.
* **Database:** **Neon DB** (Serverless Postgres) for scalable relational data storage.

## 🏗 Architecture

The project follows a modern, reactive architecture to ensure sub-second latency for real-time club features while maintaining 100% functionality in remote field archery environments with zero cellular reception.

### UI/UX Standards
* **Accessibility:** High-contrast dark mode is mandatory for outdoor visibility in direct sunlight.
* **Touch Targets:** Minimum 48x48dp targets, optimized for users wearing archery finger tabs.
* **Motion:** Shared element transitions are utilized to maintain user context during state changes.


## 🚀 Getting Started

### Prerequisites
* Android Studio Ladybug or newer.
* Kotlin 2.0+.
* A Cloudflare account (for backend development).

### Installation
1.  Clone the repository:
    ```bash
    git clone https://github.com/seyone22/shot-mobile.git
    ```
2.  Open the project in Android Studio.
3.  Sync Gradle and ensure all Jetpack Compose dependencies are resolved.
4.  Configure your Neon DB and Cloudflare credentials in `local.properties`.


## 📄 License
This project is licensed under the MIT License.
