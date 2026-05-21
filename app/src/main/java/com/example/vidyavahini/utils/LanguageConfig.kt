package com.example.vidyavahini.utils

import androidx.compose.runtime.compositionLocalOf

enum class AppLanguage {
    ENGLISH, KANNADA
}

val LocalAppLanguage = compositionLocalOf { AppLanguage.ENGLISH }

object AppStrings {
    fun appTitle(lang: AppLanguage) = when(lang) {
        AppLanguage.ENGLISH -> "Vidya-Vahini"
        AppLanguage.KANNADA -> "ವಿದ್ಯಾ-ವಾಹಿನಿ"
    }

    fun busRoutes(lang: AppLanguage) = when(lang) {
        AppLanguage.ENGLISH -> "Bus Routes"
        AppLanguage.KANNADA -> "ಬಸ್ ಮಾರ್ಗಗಳು"
    }
    
    fun searchPlaceholder(lang: AppLanguage) = when(lang) {
        AppLanguage.ENGLISH -> "Search routes..."
        AppLanguage.KANNADA -> "ಮಾರ್ಗಗಳನ್ನು ಹುಡುಕಿ..."
    }

    fun statusStale(lang: AppLanguage) = when(lang) {
        AppLanguage.ENGLISH -> "Status Stale"
        AppLanguage.KANNADA -> "ಸ್ಥಿತಿ ಹಳೆಯದಾಗಿದೆ"
    }

    fun lastSeen(lang: AppLanguage) = when(lang) {
        AppLanguage.ENGLISH -> "Last seen"
        AppLanguage.KANNADA -> "ಕೊನೆಯ ಬಾರಿ ಕಂಡಿದ್ದು"
    }

    fun logOut(lang: AppLanguage) = when(lang) {
        AppLanguage.ENGLISH -> "Log Out"
        AppLanguage.KANNADA -> "ಲಾಗ್ ಔಟ್"
    }

    fun requestNewBus(lang: AppLanguage) = when(lang) {
        AppLanguage.ENGLISH -> "Request New Bus"
        AppLanguage.KANNADA -> "ಹೊಸ ಬಸ್ ವಿನಂತಿಸಿ"
    }

    fun viewRequests(lang: AppLanguage) = when(lang) {
        AppLanguage.ENGLISH -> "View Requests"
        AppLanguage.KANNADA -> "ವಿನಂತಿಗಳನ್ನು ನೋಡಿ"
    }

    fun deleteRoute(lang: AppLanguage) = when(lang) {
        AppLanguage.ENGLISH -> "Delete Route"
        AppLanguage.KANNADA -> "ಮಾರ್ಗವನ್ನು ಅಳಿಸಿ"
    }

    fun deleteConfirm(lang: AppLanguage, busNumber: String) = when(lang) {
        AppLanguage.ENGLISH -> "Are you sure you want to delete Bus $busNumber?"
        AppLanguage.KANNADA -> "ನೀವು ನಿಜವಾಗಿಯೂ ಬಸ್ $busNumber ಅನ್ನು ಅಳಿಸಲು ಬಯಸುವಿರಾ?"
    }

    fun delete(lang: AppLanguage) = when(lang) {
        AppLanguage.ENGLISH -> "Delete"
        AppLanguage.KANNADA -> "ಅಳಿಸಿ"
    }

    fun cancel(lang: AppLanguage) = when(lang) {
        AppLanguage.ENGLISH -> "Cancel"
        AppLanguage.KANNADA -> "ರದ್ದುಮಾಡಿ"
    }

    fun live(lang: AppLanguage) = when(lang) {
        AppLanguage.ENGLISH -> "Live"
        AppLanguage.KANNADA -> "ಲೈವ್"
    }

    fun minutesAgo(lang: AppLanguage, minutes: Long) = when(lang) {
        AppLanguage.ENGLISH -> "Live ${minutes}m ago"
        AppLanguage.KANNADA -> "ಲೈವ್ ${minutes}ನಿಮಿಷದ ಹಿಂದೆ"
    }

    fun bus(lang: AppLanguage) = when(lang) {
        AppLanguage.ENGLISH -> "Bus"
        AppLanguage.KANNADA -> "ಬಸ್"
    }

    fun breakdown(lang: AppLanguage) = when(lang) {
        AppLanguage.ENGLISH -> "BREAKDOWN"
        AppLanguage.KANNADA -> "ಸ್ಥಗಿತಗೊಂಡಿದೆ"
    }

    fun noRoutesFound(lang: AppLanguage) = when(lang) {
        AppLanguage.ENGLISH -> "No routes found"
        AppLanguage.KANNADA -> "ಯಾವುದೇ ಮಾರ್ಗಗಳು ಕಂಡುಬಂದಿಲ್ಲ"
    }

    fun loggedInAs(lang: AppLanguage, role: String) = when(lang) {
        AppLanguage.ENGLISH -> "Logged in as $role"
        AppLanguage.KANNADA -> "$role ಆಗಿ ಲಾಗ್ ಇನ್ ಆಗಿದ್ದೀರಿ"
    }

    fun iSeeTheBus(lang: AppLanguage) = when(lang) {
        AppLanguage.ENGLISH -> "I SEE THE BUS!"
        AppLanguage.KANNADA -> "ನಾನು ಬಸ್ಸನ್ನು ನೋಡಿದೆ!"
    }

    fun reportBreakdown(lang: AppLanguage) = when(lang) {
        AppLanguage.ENGLISH -> "REPORT BREAKDOWN"
        AppLanguage.KANNADA -> "ಸ್ಥಗಿತ ವರದಿ ಮಾಡಿ"
    }

    fun notifySafeReach(lang: AppLanguage) = when(lang) {
        AppLanguage.ENGLISH -> "NOTIFY SAFE REACH 🏠"
        AppLanguage.KANNADA -> "ಸುರಕ್ಷಿತ ತಲುಪುವಿಕೆಯನ್ನು ತಿಳಿಸಿ 🏠"
    }

    fun recentPings(lang: AppLanguage) = when(lang) {
        AppLanguage.ENGLISH -> "Recent Pings"
        AppLanguage.KANNADA -> "ಇತ್ತೀಚಿನ ಪಿಂಗ್‌ಗಳು"
    }

    fun noPingsYet(lang: AppLanguage) = when(lang) {
        AppLanguage.ENGLISH -> "No pings yet today"
        AppLanguage.KANNADA -> "ಇಂದು ಇನ್ನೂ ಯಾವುದೇ ಪಿಂಗ್‌ಗಳಿಲ್ಲ"
    }

    fun confirmed(lang: AppLanguage) = when(lang) {
        AppLanguage.ENGLISH -> "Confirmed"
        AppLanguage.KANNADA -> "ಖಚಿತಪಡಿಸಲಾಗಿದೆ"
    }

    fun unconfirmed(lang: AppLanguage) = when(lang) {
        AppLanguage.ENGLISH -> "Unconfirmed"
        AppLanguage.KANNADA -> "ದೃಢೀಕರಿಸಲಾಗಿಲ್ಲ"
    }

    fun resetRouteAdmin(lang: AppLanguage) = when(lang) {
        AppLanguage.ENGLISH -> "RESET ROUTE (ADMIN)"
        AppLanguage.KANNADA -> "ಮಾರ್ಗ ಮರುಹೊಂದಿಸಿ (ಅಡ್ಮಿನ್)"
    }

    fun whereIsTheBus(lang: AppLanguage) = when(lang) {
        AppLanguage.ENGLISH -> "Where is the bus?"
        AppLanguage.KANNADA -> "ಬಸ್ ಎಲ್ಲಿದೆ?"
    }

    fun current(lang: AppLanguage) = when(lang) {
        AppLanguage.ENGLISH -> "Current"
        AppLanguage.KANNADA -> "ಪ್ರಸ್ತುತ"
    }

    fun takeSnapTitle(lang: AppLanguage) = when(lang) {
        AppLanguage.ENGLISH -> "Take a snap of it?"
        AppLanguage.KANNADA -> "ಇದರ ಫೋಟೋ ತೆಗೆಯುವಿರಾ?"
    }

    fun takeSnapText(lang: AppLanguage) = when(lang) {
        AppLanguage.ENGLISH -> "Adding a photo confirms the bus location for everyone."
        AppLanguage.KANNADA -> "ಫೋಟೋ ಸೇರಿಸುವುದರಿಂದ ಎಲ್ಲರಿಗೂ ಬಸ್ ಇರುವ ಸ್ಥಳ ಖಚಿತವಾಗುತ್ತದೆ."
    }

    fun yesTakePhoto(lang: AppLanguage) = when(lang) {
        AppLanguage.ENGLISH -> "Yes, Take Photo"
        AppLanguage.KANNADA -> "ಹೌದು, ಫೋಟೋ ತೆಗೆಯಿರಿ"
    }

    fun noJustReport(lang: AppLanguage) = when(lang) {
        AppLanguage.ENGLISH -> "No, just report"
        AppLanguage.KANNADA -> "ಬೇಡ, ಹಾಗೆಯೇ ವರದಿ ಮಾಡಿ"
    }

    fun addBusPhoto(lang: AppLanguage) = when(lang) {
        AppLanguage.ENGLISH -> "Add Bus Photo"
        AppLanguage.KANNADA -> "ಬಸ್ ಫೋಟೋ ಸೇರಿಸಿ"
    }

    fun uploadingPhoto(lang: AppLanguage) = when(lang) {
        AppLanguage.ENGLISH -> "Uploading Photo..."
        AppLanguage.KANNADA -> "ಫೋಟೋ ಅಪ್‌ಲೋಡ್ ಮಾಡಲಾಗುತ್ತಿದೆ..."
    }

    fun openCamera(lang: AppLanguage) = when(lang) {
        AppLanguage.ENGLISH -> "Open Camera"
        AppLanguage.KANNADA -> "ಕ್ಯಾಮೆರಾ ತೆರೆಯಿರಿ"
    }

    fun chooseFromGallery(lang: AppLanguage) = when(lang) {
        AppLanguage.ENGLISH -> "Choose from Gallery"
        AppLanguage.KANNADA -> "ಗ್ಯಾಲರಿಯಿಂದ ಆರಿಸಿ"
    }
}
