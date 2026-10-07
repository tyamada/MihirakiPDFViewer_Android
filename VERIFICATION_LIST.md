# Verification List

This is a list of verification results compiled from developer and user information.

| Category   | Manufacturer | Device Name          |   OS Version   | App Version |  Result   | Notes |
|:-----------|:-------------|:---------------------|:--------------:|:-----------:|:---------:|:------|
| Smartphone | Google       | Pixel 8a             |       17       |  1.4.0(22)  | No issues | -     |
| Smartphone | Samsung      | Galaxy A54 5G SC-53D |       16       |  1.4.0(22)  | No issues | -     |
| Smartphone | Sony         | Xperia 10VI XQ-ES44  |       16       |  1.4.0(22)  | No issues | -     |
| Tablet     | Samsung      | Galaxy Tab A SM-T510 |       9        |  1.4.0(22)  | No issues | 1     |
| Tablet     | aiwa         | tab AG10             |       13       |  1.4.0(22)  | No issues | -     |
| Desktop    | ASUS         | Chromebook CM30      | 152.0.7977.132 |  1.4.0(22)  | No issues | 2     |

## Notes
1. The page cache function is automatically disabled on devices with low RAM capacity.
2. Window mode and tablet (full-screen) mode ready.

## Testing & Diagnostics (Branch: `testing`)
- **Diagnostic Logger (`AppLogger`)**: Records rendering times, memory usage, and application events in an in-memory rolling buffer.
- **Testing & Diagnostics Screen**: Accessible via Settings -> Testing & Diagnostic Logs, displaying real-time memory usage and recent application logs.
- **Enhanced Performance Tests**: Instrumented tests in `LoadPerformanceTest.kt` verifying memory consumption, rapid page navigation stress, and multi-document switching stability.
