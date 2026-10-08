# QuoteGap 📊

**QuoteGap** is a modern Android application built with **Jetpack Compose** and **Material 3** that helps individuals, freelancers, businesses, and procurement teams instantly compare vendor price quotes against target budgets.

Whether you're managing a single project budget or evaluating bulk unit pricing, QuoteGap provides real-time budget status analysis, visual comparisons, percentage variance metrics, and complete calculation history.

---

## ✨ Features

- 🎯 **Dual Comparison Modes**:
  - **Total Quote Mode**: Direct comparison between Total Quoted Price and Target Budget.
  - **Per-Unit Quote Mode**: Calculate total quoted price based on Unit Price × Quantity before comparing to Target Budget.
- 📈 **Real-Time Budget Analysis**:
  - Instant status indicators: **Under Budget**, **On Budget**, or **Over Budget**.
  - Precise variance calculation: Difference Amount, Percentage Variance, and Budget Usage Percentage.
  - Visual **Budget Comparison Bar** and interactive formula breakdown cards.
- 💱 **Multi-Currency Support**:
  - Default preset currencies (`PKR`, `USD`, `EUR`, `GBP`, `AED`, `SAR`, `INR`) or define your own **Custom Currency** symbol.
- 📁 **Calculation History & Favorites**:
  - Save calculations with project names and custom notes.
  - Search, filter by favorites, copy calculation summaries to clipboard, or share with clients/team members.
  - Re-open saved calculations back into the interactive calculator at any time.
- 🎨 **Material 3 Customization**:
  - **Theme Modes**: System Default, Light Theme, or Dark Theme.
  - **Dynamic Colors**: Material You dynamic color scheme support.
  - **Haptic Feedback**: Tactile feedback on key interactions.
- 🔒 **100% Offline & Private**:
  - Works completely offline with local data persistence via Jetpack DataStore Preferences. No server required.

---

## 📱 Screen Breakdown

| Screen | Description |
| :--- | :--- |
| **Calculator** | Interactive calculation hub with live variance metrics, comparison bar, formula breakdown, and project notes. |
| **History** | Searchable archive of all saved calculations with filtering by favorites, quick actions (copy, share, restore, delete). |
| **Settings** | Personalize default currency, preferred calculation mode, default labels, theme mode, dynamic color, and haptic feedback. |
| **About** | App specifications, feature highlights, and privacy overview. |

---

## 🛠️ Tech Stack & Architecture

- **Language**: [Kotlin](https://kotlinlang.org/)
- **UI Toolkit**: [Jetpack Compose](https://developer.android.com/jetpack/compose) with [Material 3](https://m3.material.io/)
- **Architecture Pattern**: MVVM (Model-View-ViewModel) + Clean Domain Logic
- **Async & Reactive Data**: [Kotlin Coroutines](https://kotlinlang.org/docs/coroutines-overview.html) & `StateFlow`
- **Navigation**: [Navigation Compose](https://developer.android.com/jetpack/compose/navigation)
- **Local Persistence**: [Jetpack DataStore (Preferences)](https://developer.android.com/topic/libraries/architecture/datastore) + [Gson](https://github.com/google/gson)
- **Design System**: Material Icons Extended, Edge-to-Edge layout support

---

## 📐 Calculation Formulas

- **Quoted Total (Per-Unit)** = $\text{Unit Price} \times \text{Quantity}$
- **Difference Amount** = $\text{Quoted Total} - \text{Target Budget}$
- **Percentage Variance** = $\left(\frac{|\text{Difference Amount}|}{\text{Target Budget}}\right) \times 100\%$
- **Budget Usage** = $\left(\frac{\text{Quoted Total}}{\text{Target Budget}}\right) \times 100\%$

---

## 🚀 Getting Started

### Prerequisites
- **Android Studio** Ladybug (2024.2.1) or newer recommended
- **JDK**: 11 or higher
- **Minimum SDK**: Android 7.0 (API level 24)
- **Target SDK**: Android 15 (API level 35) / Android 16 (API level 37)

### Build & Run

1. **Clone the repository**:
   ```bash
   git clone https://github.com/abdullah-ateeq/QuoteGap.git
   cd QuoteGap
   ```

2. **Open in Android Studio**:
   - Open Android Studio and choose **Open an Existing Project**.
   - Select the cloned `QuoteGap` project directory.

3. **Build & Install**:
   - Connect an Android device or start an emulator.
   - Run the `:app` configuration or run using Gradle:
     ```bash
     ./gradlew assembleDebug
     ```

---

## 📄 License

```text
Copyright (C) 2025 Abdullah Ateeq

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain license at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```
