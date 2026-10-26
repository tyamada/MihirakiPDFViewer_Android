# Testing & Diagnostics (Branch: `testing`)

- **Diagnostic Logger (`AppLogger`)**: Records rendering times, memory usage, and application events in an in-memory rolling buffer.
- **Testing & Diagnostics Screen**: Accessible via Settings → Testing & Diagnostic Logs, displaying real-time memory usage and recent application logs.
- **Enhanced Performance Tests**: Instrumented tests in `LoadPerformanceTest.kt` verifying memory consumption, rapid page navigation stress, and multi-document switching stability.
