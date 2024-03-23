# FlexChat

FlexChat is a instant messaging application that supports audio and video call

## Table of Contents

- [App Preview](#App-Preview)
- [Getting Started](#Getting-Started)
  - [Prerequisites](#Prerequisites)
  - [Installation](#Installation)
- [Contributing](#Contributing)
- [Bug Reporting](#Bug-Reporting)
- [License](#License)

## App Preview

## Getting Started

Follow these steps to get the app up and running on your Android device or emulator.

### Prerequisites

- Android Studio: [Download](https://developer.android.com/studio) and install the latest version of
  Android Studio.
- Android Device or Emulator: Ensure you have a physical Android device connected or an emulator set
  up in Android Studio.

### Installation

1. **Clone the repository:** Clone this repository to your local machine using `git`.

   ```bash
   git clone https://github.com/onirutlA/FlexChat.git
   cd FlexChat
   ```

2. **Open in Android Studio:** Launch Android Studio and select "Open an existing Android Studio
   project." Navigate to the folder where you cloned the repository and open it.

3. **Build and Run:** Wait for the project to build, and then click the "Run" button in Android
   Studio. Select your connected Android device or choose an emulator to run the app.

## Library Used

- Kotlin based, Coroutines + Flow for asynchronous.
- Hilt for dependency injection.
- Jetpack
  - Lifecycle - dispose of observing data when lifecycle state changes.
  - ViewModel - UI related data holder, lifecycle aware.
  - Room Persistence - construct the database using the abstract layer.
  - Navigation Component - Navigate to another fragment
- Architecture
  - MVI Architecture
  - Repository pattern
- Firebase
  - Firestore
  - FirebaseAuth
  - FirebaseFunction
  - FirebaseMessaging
  - FirebaseCrashlytics
- kotlinx.serialization - A modern JSON library for Kotlin and Java.
- Coil - Image Loading Library.
- UiAutomator
- ArrowKT
- Material-Components - Material design components for building ripple animation, and CardView.

## Contributing

Your contributions to this project are highly appreciated! To contribute, follow these steps:

1. Fork the repository and create a new branch.
2. Make your changes and ensure the code is well-tested.
3. Commit your changes and push them to your fork.
4. Submit a pull request, explaining the changes you've made.

## Bug Reporting

If you encounter any bugs, issues, or have suggestions for improvements, please open an issue on the
repository. Provide as much detail as possible to help us understand and resolve the problem
quickly.

## License

```
Copyright 2024 Ricky Alturino

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

https://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```
