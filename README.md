# Desktop Streaming Stopwatch

This application was developed to solve the problem of not being able to fully focus on a video while completing all workout activities. This is the android application that receives the RTSP video capture from the desktop server. See https://github.com/bcartfall/desktop-streaming-server for the desktop application.

The problem: When doing workout stretches and activities such as planks the head is facing the floor and it is not possible to watch the computer screen.

The solution: The mobile device can be placed on the floor and will duplicate the computer screen so that the video content can be enjoyed.

# Instructions

Compile this code in Android Studio. There is no application settings. The RTSP URL for the desktop server is hard coded in the MainActivity `const val rtspUrl = "rtsp://192.168.1.75:8554/live"`.

# Usage Requirements

- Android device with SDK 30 or higher.

# Licence

MIT. 

This software uses code of LibVLC for android which is licensed under LGPLv2 and its source can be downloaded at https://code.videolan.org/videolan/vlc-android.