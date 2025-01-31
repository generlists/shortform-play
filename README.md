
# 📺 ShortForm Play
 - A fast and optimized short-form video player
 - The beginning of fun, ShortForm Play!

 
`"“Endless enjoyment in short moments! Easily enjoy short videos with infinite looping playback.
Don’t miss out on the latest trends and diverse content. Anytime, anywhere, dive into the world of short-form videos that will make your day more exciting!””`


# 🏗️ Architecture Overview

## **MVVM-based Clean Architecture**

- `Uses Single Activity + Navigation` for screen structure
- `ViewModel` handles state management and separates business logic
- DI use (Dependency Injection)로 **Hilt** 

## **📌 Key Features**

**🎬 YouTube-based Short-Form Video Player**

- Provides trending videos using the YouTube Data API
- **WebView-based YouTube Player**for video playback
- Supports infinite scrolling and loop playback

### **🚀 Fast Performance & Optimization**

- **Fast loading speed** with Firebase Storage JSON data
- **Minimizes API calls** using cached data

### **🔄 Personalized Recommendations & History**

- Stores up to **500 recently watched videos**, deleting older ones once the limit is exceeded
- Like and dislike functionality for content preference management

### **📱  Intuitive UI & Navigation**

- **MVVM-based Clean Architecture** implementation
- Flexible navigation with Single Activity + Navigation

### **📢  AdMob Integration**

- **Monetization via Google AdMob**
- Supports banner and interstitial ads

**[스크린샷]**

![스크린샷 2025-01-30 오후 6 52 34](https://github.com/user-attachments/assets/c2ae6f33-8b01-4251-802a-9fc3c7465189)


# Development Environment

- Language: Kotlin (1.9.0)
- Tool: Android Studio Koala Feature Drop | 2024.1.2+
- Gradle: 8.7+
- Target SDK: 34
- Minimum SDK: Android 9.0

# Technologies Used

| Architecture    |  UI     | Data   | Environment    |
| ------ | ------  | ------ | ------ |
| MVVM   | jetpack Compose  | Co-Routines | tomi
| clean Architecture | Material Design 3 |Retrofit | gradle
| Single Activity | Navigation | Hilt | ktlint
| Modular Retrofit architecture |Coil | OkHttp
| SharedFlow for communication | DataStore | Firebase
| Reusable UI components | Lottie | Google Analytics
| Sharing feature| ViewModel | Crashlytics|
|                | AdMob|YouTube Data Api | |
|                |  | Remote Config||

# Download & Setup

```bash
$ git clone https://github.com/generlists/shortform-play.git
$ cd  shortform-play.git
```

# Data Management

- [YouTubeData Api](https://developers.google.com/youtube/v3?hl=ko)
    - The YouTube Data API has a daily  [limit of 10,000 requests](https://developers.google.com/youtube/v3/determine_quota_cost?hl=ko)
    - Instead of making API calls directly, we preprocess data once daily and store JSON data in Firebase Storage to optimize requests within the quota.
    - Like/Dislike actions are fetched from the API but are stored locally.
    - The recently watched videos list has a 500-video limit, deleting the oldest video when exceeded.

> ⚠️ YouTube Data API integration is not covered in this guide.



# 🎬 YouTube Video Playback

- **WebView-based YouTube Player** (per YouTube policy)
- [Android YouTube Player](https://github.com/PierfrancescoSoffritti/android-youtube-player) 	Modified Android YouTube Player (MIT-licensed)
- **Legacy Fragment-based Player** → Future plan to refactor into an independent library
- Uses WebView + Compose integration with AndroidView for flexible UI

# Application Architecture

![스크린샷 2025-01-30 오후 10 52 40](https://github.com/user-attachments/assets/69d380cd-3ef8-4e8c-a429-40509b54d49f)


# Required Setup

- To run the app after cloning the repository, you need to configure the following

ShortForm Play manages JSON data using Firebase Storage.

Follow these steps to set up Firebase and download the google-services.json file:


 - **AdMob & API Configuration**
1. Create a project in [Firebase Console](https://firebase.google.com/)

2.	Switch to the **Blaze plan** (5GB free storage)

3.	Download the **google-services.json** file and update the package name (com.sean.ratel.android)

- admob([https://admob.google.com/intl/ko/home/](https://admob.google.com/intl/ko/home/))
  
- **Update local.properties** with the required environment variables:

```bash
  FIREBASE_BASE_URL= [https://firebasestorage.googleapis.com](https://firebasestorage.googleapis.com/)
  ADMOB_APP_ID=your_admob_app_id
  MY_EMAIL_ACCOUNT=your_email@example.com](mailto:MY_EMAIL_ACCOUNT=your_email@example.com)
  NOTICE_URL=https://example.com/notice
  NOTICE_URL_EN=https://example.com/notice_en
  REGAL_URL=https://example.com/privacy
  REGAL_URL_EN=https://example.com/privacy_en
```

- Upload the provided file to Firebase Storage Root Directory:
    
 [shorts_main_default_KR.json](https://github.com/user-attachments/files/18616010/shorts_main_default_KR.json)


# Google Play Store


[https://play.google.com/store/apps/details?id=com.sean.ratel.android](https://play.google.com/store/apps/details?id=com.sean.ratel.android)

# Support the Project

![스크린샷 2025-01-31 오후 7 55 47](https://github.com/user-attachments/assets/c5cd4a92-7ff5-4026-aded-4c6fd879930a)

For inquiries, please contact us at[email](mailto:shortform.play@gmail.com)


