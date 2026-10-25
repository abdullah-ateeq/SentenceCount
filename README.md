# SentenceCount 📝📊

**SentenceCount** is a modern, feature-rich Android application built with **Jetpack Compose** and **Material 3**. It provides deep statistical analysis and breakdown of text corpora, sentences, words, characters, syllables, reading/speaking time, and readability metrics.

---

## ✨ Key Features

- **Comprehensive Text Statistics**: Instantly calculate total sentences, words, characters, paragraphs, syllables, estimated reading time, and speaking time.
- **Advanced Sentence Insights**:
  - Shortest and longest sentence detection.
  - Average, median, and mode sentence lengths.
  - Standard deviation and range metrics.
- **Interactive Distribution Charts**: Visual sentence length distribution charts and graphs to understand text composition at a glance.
- **Sentence Inspector**: Drill down into individual sentences with detailed modals, word counts, character counts, and index positions.
- **History Management**: Save your text analysis sessions, review past inputs, and manage history items easily.
- **Rich Customization & Settings**:
  - Material 3 dynamic color support & custom theme picker (Light, Dark, System).
  - Configurable length units (Words vs Characters).
  - Flexible sorting options and display preferences.
- **Modern UI/UX**: Designed with Jetpack Compose, Material 3 components, Edge-to-Edge layout support, animated splash screens, and smooth bottom sheets.

---

## 🛠️ Tech Stack & Architecture

- **Language**: [Kotlin](https://kotlinlang.org/)
- **UI Toolkit**: [Jetpack Compose](https://developer.android.com/jetpack/compose) & [Material 3](https://m3.material.io/)
- **Architecture**: MVVM (Model-View-ViewModel) with Unidirectional Data Flow (UDF) using Kotlin Coroutines & `StateFlow`.
- **Navigation**: Jetpack Navigation Compose.
- **State Management**: Lifecycle ViewModel & Compose State.
- **Testing**: JUnit for unit testing core text engines (`SentenceDetector`, `StatisticsCalculator`) and AndroidX Test for instrumented testing.

---

## 📂 Project Structure

```tree
com.example.sentencecount/
├── data/
│   ├── HistoryRepository.kt          # Manages saved analysis history
│   └── PreferencesManager.kt         # Handles app settings & theme persistence
├── engine/
│   ├── CharacterCounter.kt           # Character and whitespace analysis
│   ├── SentenceDetector.kt           # Robust sentence boundary detection
│   ├── SentenceDetectorAndStatisticsTest.kt # Unit tests for text parsing & stats
│   ├── StatisticsCalculator.kt       # Statistical calculations (mean, median, mode, SD)
│   └── WordCounter.kt                # Word frequency & counting logic
├── model/
│   ├── Sentence.kt                   # Sentence data model
│   ├── SentenceCountHistory.kt       # History session model
│   ├── SentenceCountUiState.kt       # Main UI state holder
│   ├── SentenceLengthDistribution.kt # Distribution chart models
│   ├── SentenceSortOrder.kt          # Sorting configuration
│   ├── SentenceStatistics.kt         # Statistical results model
│   └── SettingsModels.kt             # Theme & preference data classes
├── ui/
│   ├── SentenceCountViewModel.kt     # Central ViewModel handling business logic & state
│   ├── components/                   # Reusable Compose components (Charts, Cards, BottomSheets)
│   ├── screens/                      # App screens (Home, History, Settings, About, Splash)
│   └── theme/                        # Material 3 Theme, Color & Typography definitions
└── MainActivity.kt                   # Single-activity host with NavHost navigation
```

---

## 🚀 Getting Started

### Prerequisites
- Android Studio (Ladybug / Koala or newer recommended)
- JDK 11 or higher
- Android SDK (Compile SDK 37, Min SDK 26)

### Building and Running
1. Clone the repository:
   ```bash
   git clone https://github.com/abdullah-ateeq/SentenceCount.git
   ```
2. Open the project in **Android Studio**.
3. Sync Gradle and run the app on an emulator or physical Android device.

---

## 🧪 Testing

Run unit tests via Gradle:
```bash
./gradlew test
```

---

## 📄 License

This project is licensed under the MIT License. See [LICENSE](LICENSE) for details.
