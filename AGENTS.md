# Project Guidelines & Architecture Mandate

## Cross-Platform KMP & Compose Multiplatform Architecture
> **User Mandate:**
> "আমি সিস্টেমে Android ফ্রেমওয়ার্ক নির্বাচন করেছি ঠিকই, তবে আমার আসল লক্ষ্য হলো এমন একটি ক্রস-প্ল্যাটফর্ম স্টাডি অ্যাপ তৈরি করা যা একই কোডবেস থেকে Android মোবাইল (APK) এবং ল্যাপটপ/ডেস্কটপ (Desktop JVM)-এ চলবে। তাই সাধারণ সিঙ্গেল-প্ল্যাটফর্ম Android কোড না লিখে Kotlin Multiplatform (KMP) এবং Compose Multiplatform প্যাটার্নে প্রজেক্ট আর্কিটেকচার, build.gradle.kts এবং UI কোড লিখতে হবে —যাতে পরবর্তীতে আমি GitHub Actions ব্যবহার করে মোবাইল এবং ল্যাপটপ উভয়ের জন্য বিল্ড ফাইল বের করতে পারি।"

### Key Architecture Principles:
1. **Multiplatform-First UI & Code Structure**:
   - Write UI code using pure Compose Multiplatform compatible primitives (Material 3, pure Compose Canvas/Layout, standard Coroutines StateFlow).
   - Keep UI components separated from Android-only platform dependencies so they can be readily shared across Android and Desktop targets (`jvmMain`).
   - Clean separation of UI, Domain, and State Layers.

2. **Navigation & Core Destinations (4 Navigation Bars / Tabs)**:
   - **Home (হোম)**: Study overview, daily goals, streak, active sessions, quick actions.
   - **Explore (এক্সপ্লোর)**: Study subjects, trending topics, resource library, flashcards.
   - **Courses (কোর্স)**: Enrolled courses, lecture modules, progress indicators, upcoming milestones.
   - **Profile (প্রোফাইল)**: Learning statistics, achievements/badges, study analytics, settings.
   - **Navigation Design**: Advanced modern navigation layout (Floating pill navigation bar with active morphing pill indicator, icons, badges, elevation and smooth transitions).

3. **Desktop & Mobile Adaptability**:
   - Responsive layouts that gracefully expand from compact vertical phone screens to wide desktop viewports (Navigation Rail / List-Detail panes on wide screens).
