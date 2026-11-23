# UPI Payment Alert

Android starter project for a notification-based UPI alert app.

## Features in this starter
- Hindi voice alert for detected incoming-payment notifications
- Received-notification total and local history
- Voice and beep toggles
- Optional night silent window (10 PM–7 AM)
- Duplicate notification guard
- No SMS, contacts, location, microphone, UPI PIN, or OTP permission/request

## Important limitations
- Currently recognizes notification packages for Google Pay, PhonePe, Paytm, and BHIM. Other apps/package variants may not be detected; support is not guaranteed for every UPI app.
- It reads notification text, not bank records. It cannot prove that money actually arrived. Notifications can be incomplete, delayed, or spoofed. Always verify in your bank/UPI app.
- Parsing depends on each app's notification wording and may miss some formats or misclassify a notification.
- Android Notification Access is sensitive: it can expose notification contents from apps. Enable it only if you understand and accept this.

## Build APK using GitHub Actions
1. Create a GitHub repository and upload these project files.
2. Open the repository's **Actions** tab.
3. Run **Build Android APK** (or push to `main`).
4. Download the `UPI-Payment-Alert-debug-apk` artifact from the workflow run.

This ZIP contains source code, not a prebuilt APK. The APK is produced by GitHub Actions after a successful build.
