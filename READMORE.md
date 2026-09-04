# 💰 Finance Manager - Android App

A modern, offline-first personal finance and expense tracking Android application built with **Jetpack Compose**, **Material 3**, **Kotlin Coroutines**, **Room Database**, and **Firebase**.

Designed with a clean, sleek interface inspired by Revolut and Apple Wallet, Finance Manager helps you monitor accounts, track expenses, set monthly budgets, and analyze your spending habits effortlessly.

---

## ✨ Features

- **📊 Comprehensive Dashboard**: Real-time balance overview, monthly cashflow (Income vs Expense), and quick-access account cards.
- **💳 Multi-Account Support**: Manage multiple accounts seamlessly (Bank Accounts, Mobile Wallets like bKash, Nagad, Rocket, DBBL, Cash, and Custom Accounts) with institution badges and balance tracking.
- **🔄 Account-to-Account Transfers**: Easily move funds between accounts with automatic balance reconciliation.
- **🏷️ Smart Categorization & Budgets**: Set spending limits per category, track progress bars, and customize categories with tailored colors and icons.
- **🔁 Recurring Transactions**: Automate periodic bills, salaries, and subscriptions (daily, weekly, monthly, yearly).
- **📈 Visual Analytics & Insights**: Interactive spending breakdowns, category distribution charts, and historical comparisons.
- **📄 Data Export**: Export complete transaction statements to **CSV** or **PDF** format.
- **🔒 Flexible Authentication**:
  - Local Guest / Offline Mode (Full functionality without cloud dependencies).
  - Firebase Authentication (Email/Password & Google Sign-In with Android Credential Manager).
- **🌙 Adaptive UI**: Full Material 3 theming supporting dynamic colors, light mode, and dark mode.

---

## 🛠️ Tech Stack & Architecture

- **UI Framework**: [Jetpack Compose](https://developer.android.com/jetpack/compose) with Material Design 3
- **Language**: [Kotlin](https://kotlinlang.org/) (100%)
- **Architecture**: MVVM (Model-View-ViewModel) + Single Source of Truth Repository Pattern
- **Local Storage**: [Room Database](https://developer.android.com/training/data-storage/room) with SQLite & KSP code generation
- **Asynchronous Flow**: Kotlin Coroutines & `StateFlow`
- **Backend & Cloud Sync**: Firebase Auth & Cloud Firestore
- **Dependency Management**: Gradle Version Catalogs (`libs.versions.toml`)

---

## 🚀 Getting Started

### Prerequisites

- **Android Studio**: Ladybug (2024.2+) or newer recommended.
- **JDK**: Version 17+
- **Android SDK**: `minSdk 24` (Android 7.0+), `targetSdk 35`

### 1. Clone the Repository

```bash
git clone https://github.com/<your-username>/<your-repo-name>.git
cd <your-repo-name>
```

### 2. Configure Firebase (Optional for Cloud Sync)

1. Go to the [Firebase Console](https://console.firebase.google.com/) and create a project.
2. Add an Android app with the package name: `com.financemanager.app`.
3. Download your `google-services.json` file.
4. Place `google-services.json` inside the `app/` directory:
   ```bash
   # You can reference app/google-services.json.example for structure
   cp path/to/your/google-services.json app/google-services.json
   ```

*(Note: The app runs completely offline using local Room DB even without Firebase credentials).*

### 3. Build & Run

Run directly on a connected device or Android Emulator:

```bash
# Debug APK build
./gradlew assembleDebug

# Install to connected device
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

Or open the project folder directly in **Android Studio** and click **Run (Shift + F10)**.

---

## 📂 Project Structure

```text
com.financemanager.app/
├── data/
│   ├── auth/           # Firebase Auth & Google Sign-in Managers
│   ├── cloud/          # Firestore Cloud Sync Provider
│   ├── export/         # CSV and PDF Exporters
│   ├── local/          # Room DB, DAOs, Type Converters, Session Manager
│   ├── model/          # Data classes & Room entities (Transaction, Account, Budget)
│   └── repository/     # Unified Finance Repository
├── ui/
│   ├── components/     # Cards, Dialogs, Charts, Transaction Rows, Squircles
│   ├── navigation/     # App Navigation Drawer & Scaffold
│   ├── screens/        # Home, Transactions, Accounts, Budgets, Analytics, Settings
│   └── theme/          # Material 3 Color Schemes, Typography, and Shapes
└── MainActivity.kt     # App entry point
```

---

## 🛡️ Security & Privacy

- All sensitive keys (`google-services.json`, keystores, credentials, local machine SDK paths) are configured in `.gitignore` by default.
- Never commit private Firebase API keys or release signing keys to public repositories.

---

## 📄 License

This project is licensed under the [MIT License](LICENSE) - feel free to use and modify for personal or commercial projects.
