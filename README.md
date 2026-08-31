# AirDrive — Android app (WebView shell)

A minimal native Android app: one screen, one WebView, pointed at your
AirDrive server (`http://localhost:8000`, same one running in Termux). It
gives you a real launcher icon and app entry with no browser UI — pull to
refresh, back button navigates within the app, and a small settings icon
(top-right) lets you change the server address if you ever run it on a
different port or want to point it at another device on your network.

This is intentionally simple rather than a Trusted-Web-Activity/PWABuilder
wrapper: those need a public HTTPS URL to build against, which doesn't fit
a server that only ever runs on `localhost`. A plain WebView pointed at
localhost is the standard pattern for this exact situation.

## Build it

1. Install **Android Studio** (any recent version) on a PC.
2. **File → Open** → select this `AirDriveApp` folder.
3. If Android Studio asks to generate a Gradle wrapper, click **OK** — it
   downloads Gradle automatically on first sync. First sync takes a few
   minutes.
4. Once synced: **Build → Build App Bundle(s) / APK(s) → Build APK(s)**.
5. Studio shows a notification when done — click **locate** to find
   `app-debug.apk` (it'll be under `app/build/outputs/apk/debug/`).

## Install it on your phone

1. Copy `app-debug.apk` to your phone (or build directly with a phone
   connected via USB and hit ▶ Run instead of Build APK — installs straight
   to the device).
2. On the phone, enable **Install unknown apps** for whatever app you use
   to open the file (Settings → Apps → Special access), then tap the APK.
3. Make sure the Termux server is running (`uvicorn main:app --host 0.0.0.0
   --port 8000`), then open the AirDrive app — it loads your existing UI,
   just as a real app now.

## Notes

- This is a debug build — fine for installing on your own phone, not meant
  for the Play Store (that needs a signed release build, a separate step
  with its own keystore setup, only worth it if you want to distribute
  this beyond your own device).
- The app talks to whatever server address is saved in its settings
  (gear icon, top right) — defaults to `http://localhost:8000`. If you
  ever want to run the AirDrive server on a different phone or PC on your
  Wi-Fi and control it from here, just set that device's IP there instead.
- Cleartext (plain `http://`) is only allowed to `localhost`/`127.0.0.1`
  by the network security config — if you point it at another device's
  IP over plain HTTP, add that IP the same way in
  `app/src/main/res/xml/network_security_config.xml`.
