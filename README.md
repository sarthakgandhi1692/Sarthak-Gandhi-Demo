# 📱 Holdings App

A modern Android application built with Jetpack Compose for managing and tracking investment holdings. The app provides real-time information about your investment portfolio, including profit and loss calculations, current market values, and detailed holdings information.

## ✨ Features

- 📊 **Real-time Holdings Display**: View your current investment holdings with live market prices
- 📈 **Portfolio Analytics**: Track key metrics including:
  - Total investment value
  - Current portfolio value
  - Profit & Loss (P&L) calculations
  - P&L percentage
  - Today's P&L
- 🎨 **Modern UI**: Built with Jetpack Compose for a fluid and responsive user experience
- 🏗️ **Clean Architecture**: Implements MVVM pattern with clean architecture principles
- 💉 **Dependency Injection**: Uses Dagger Hilt for dependency management
- 🧭 **Navigation**: Implements type-safe navigation using Jetpack Navigation Compose
- 📱 **Offline Support**: Local caching with Room database for offline access
- 🔄 **Data Sync**: Implements repository pattern with remote and local data sources

## 🛠️ Tech Stack

- 🎨 **UI Framework**: Jetpack Compose
- 📐 **Architecture**: MVVM + Clean Architecture
- 💉 **Dependency Injection**: Dagger Hilt
- 🧭 **Navigation**: Navigation Compose
- 💾 **Local Storage**: Room Database
- 📊 **State Management**: ViewModel + StateFlow
- ⚡ **Concurrency**: Kotlin Coroutines
- 📦 **Serialization**: Kotlin Serialization
- 🌐 **Network**: Retrofit with Demo API

## 📁 Project Structure

The project follows a clean architecture approach with the following main components:

- `app/src/main/java/com/example/test/`
  - 🎨 `ui/`: Contains all UI-related code including screens and view models
  - 📦 `model/`: Data models and database implementations
    - 💾 `local/`: Room database, DAOs, and entities
    - 🌐 `remote/`: API interfaces and network models
    - 📚 `repository/`: Repository implementations
  - ⚙️ `domain/`: Use cases and business logic
  - 💉 `di/`: Dependency injection modules
  - 🔧 `base/`: Base classes and common utilities
  - 🧭 `navGraph/`: Navigation-related components

## 🚀 Getting Started

### 📋 Prerequisites

- Android Studio Arctic Fox or later
- Android SDK 21 or higher
- Kotlin 1.5.0 or higher

### 🔧 Setup

1. Clone the repository
2. Open the project in Android Studio
3. Sync project with Gradle files
4. Run the app on an emulator or physical device

## 🤝 Contributing

Contributions are welcome! Please feel free to submit a Pull Request.

## 📄 License

This project is licensed under the MIT License - see the LICENSE file for details. 