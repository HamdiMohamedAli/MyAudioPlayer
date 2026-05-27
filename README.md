# MyAudioPlayer

A simple, clean local music player app for Android built with Java. It scans your device's internal storage for audio files, lists them in a RecyclerView, and plays them using Android's native MediaPlayer api. 

This project was built to learn how to handle audio files, custom UI layouts, and Android's newer storage permissions.

---

## Features

* **Auto-Scan Music:** Automatically finds `.mp3` and other audio files stored on your device.
* **Modern Storage Compatible:** Uses modern Android Scoped Storage APIs so it works on older devices as well as Android 13/14+.
* **Dynamic Player UI:** Uses the Android Palette library to extract colors from the song's album art and change the player background dynamically.
* **Audio Controls:** Supports Play/Pause, Skip Next/Previous, Shuffle, Repeat, and a working Seekbar.
* **Delete from App:** Includes a pop-up option to delete music files directly from the list.

---

## Built With

* **Language:** Java
* **UI:** XML Layouts (ConstraintLayout, ViewPager2, TabLayout)
* **Libraries:** * [Glide](https://github.com/bumptech/glide) - For loading album cover art seamlessly.
  * Android Jetpack Palette - For making the UI colors adapt to the music art.

---

