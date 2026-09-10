# Safe Space

A lightweight native Android implementation of all 21 screens in the supplied Safe Space design:

1. Animated splash screen
2. Find Your Safe Space
3. Talk Freely
4. Small Steps, Big Progress
5. Create Your Account
6. Login
7. Permissions in a blue-sky theme
8. Home dashboard
9. Mood check-in
10. AI companion
11. Journal
12. Relax and breathing
13. Mindful activities
14. Wellbeing insights
15. Support
16. Profile
17. Reminders
18. Community
19. Settings
20. Night mode
21. Loading and success

The onboarding pages support both horizontal swipes and the **Next / Get Started** buttons, with smooth background-stable slide transitions. The supplied Safe Space artwork is used for both the launcher icon and splash logo. The home, bottom navigation, profile, community, and settings controls connect the complete prototype flow.

Signup, login, display-name saving, and password reset use Firebase Authentication through the same prototype Firebase project as SAHAAY. The Google and Apple buttons are visual placeholders; email/password is the connected prototype flow.

After authentication, the flow continues through the prototype permission choices into the Safe Space home dashboard.

## Build

Open the project in Android Studio and run the `app` configuration, or run:

```powershell
./gradlew.bat :app:assembleDebug
```

The APK is written to `app/build/outputs/apk/debug/app-debug.apk`. A signed release build can be created with `:app:assembleRelease`.
