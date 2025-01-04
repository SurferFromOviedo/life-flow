package com.example.collage.ui

import com.example.collage.data.Date

data class AppState(
    val calendarLoadingComplete: Boolean = false,
    val screenToShow: Screen = Screen.CALENDAR,
    val showImageDialog: Boolean = false,
    val selectedDate: Date? = null,
    val layout: Layout = Layout.WEEK,
    val timerState: TimerState = TimerState(),
    val showCam: Boolean = false
)

data class TimerState(
    val photoTimeRemaining: Long? = null,
    val camActivationTimeRemaining: Long? = null
)

enum class Layout {
    WEEK,
    TWO_WEEKS
}

enum class Screen {
    CALENDAR,
    CAMERA
}