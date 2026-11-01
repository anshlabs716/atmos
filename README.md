# Atmos — Weather, beautifully understood

Atmos is a polished, privacy-first, open-source Android weather application built with native Kotlin, Jetpack Compose, and expressive Material 3 design.

## Features

- **Immersive Weather Hero**: Centrepiece atmospheric illustration with integrated current temperature, weather conditions, high/low ranges, and dynamic sky backdrops for clear, cloudy, rainy, snowy, and thunderstorm conditions.
- **Weather-Reactive Animations**: Subtle, battery-conscious animations including drifting cloud formations, sun pulse, gentle rainfall, fluttering snowflakes, and thunderstorm lightning effects.
- **Gyroscope Parallax**: Device tilt tracking using Android's rotation vector sensor with smooth low-pass filtering, lifecycle awareness (auto-pauses when screen is hidden), and reduced-motion settings.
- **Hourly Forecast Cards**: Horizontally scrolling expressive Material 3 cards with hour labels, weather condition icons, temperatures, and precipitation probabilities with current-hour highlighting.
- **7-Day Daily Forecast**: Clean rows featuring day names, weather icons, conditions, and proportional temperature range bars.
- **Precipitation Outlook**: Dedicated precipitation cards with probability percentages, expected amounts in mm/in, and 6-hour bar breakdowns.
- **Mixed-Size Detail Cards**: Dynamic cards including Wind Speed with interactive compass dial, UV Index gauge, Feels-like temperature, Humidity with Dew point, Barometric Pressure, and Optical Visibility.
- **Sunrise & Sunset Arc**: Beautiful quadratic curve reflecting actual local sun path, sunrise and sunset timestamps, and remaining daylight tracking.
- **Air Quality Summary**: Compact AQI card displaying US/European air quality indexes, category indicators, and PM2.5/PM10 particle concentrations.
- **Interactive Weather Radar**: Radar screen featuring an interactive precipitation reflectivity canvas, timeline scrubber (-90m to +60m), playback animation controls, intensity legend, and zoom/pan controls.
- **Locations Management**: Search any city worldwide via Open-Meteo Geocoding, save favorites, swipe between cities on the home screen, or switch directly from the top bar dropdown. Works completely without GPS permissions.
- **Offline Resilience & Caching**: Cached forecast storage with timestamps and prominent offline mode indicator when disconnected.
- **Privacy First**: Zero advertisements, zero third-party tracking, no account required, and privacy-preserving direct requests to Open-Meteo.

## Architecture

- **Language**: Kotlin 2.2 / JDK 21
- **UI Framework**: Jetpack Compose with Material Design 3
- **Architecture**: MVVM with Repository Pattern
- **Networking**: Ktor Client with Kotlinx Serialization
- **Local Storage**: Android DataStore Preferences & JSON File Cache
- **Sensors**: Android SensorManager (`Sensor.TYPE_ROTATION_VECTOR`)
- **Typography**: Plus Jakarta Sans & Outfit (bundled locally)
- **Minimum SDK**: Android 14 (API 34)
- **Target SDK**: Android 16 (API 36)

## License

GPL-3.0-or-later. Open-Meteo weather data under CC BY 4.0.
