package com.example.collage.ui

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.provider.MediaStore
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.collage.data.Date
import com.example.collage.data.NotificationScheduler
import com.example.collage.data.PreferencesRepository
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.PermissionState
import com.google.accompanist.permissions.isGranted
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream
import java.util.Calendar

class MainViewModel(
    private val preferencesRepository: PreferencesRepository
) : ViewModel() {

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application = (this[APPLICATION_KEY] as MyApplication)
                MainViewModel(application.preferencesRepository)
            }
        }
    }

    private val _uiState = MutableStateFlow(AppState())
    val uiState = _uiState.asStateFlow()

    private val showCam = MutableSharedFlow<Boolean>(replay = 1)
    private val photoStartTime = MutableSharedFlow<Long>(replay = 1)
    private val photoDeadline = MutableSharedFlow<Long>(replay = 1)
    private val camActivationTime = MutableSharedFlow<Long>(replay = 1)

    init {
        viewModelScope.launch {
            preferencesRepository.showCam.collect { showCam.emit(it) }
        }
        viewModelScope.launch {
            preferencesRepository.photoStartTime.collect { photoStartTime.emit(it) }
        }
        viewModelScope.launch {
            preferencesRepository.photoDeadline.collect { photoDeadline.emit(it) }
        }
        viewModelScope.launch {
            preferencesRepository.camActivationTime.collect {
                camActivationTime.emit(it)
                startTimerManager()
            }
        }
    }

    private fun startTimerManager() {
        viewModelScope.launch {
            //delay(1000L)
            camActivationTime.collectLatest { activationTime ->
                if (activationTime == 0L) {
                    val currentTime = System.currentTimeMillis()
                    val deadline = currentTime + 1000L * 60 * 60 * 3
                    val calendar = Calendar.getInstance()
                    calendar.add(Calendar.DAY_OF_YEAR, 1)
                    calendar.set(Calendar.HOUR_OF_DAY, 0)
                    calendar.set(Calendar.MINUTE, 0)
                    calendar.set(Calendar.SECOND, 0)
                    calendar.set(Calendar.MILLISECOND, 0)
                    val newActivationTime = calendar.timeInMillis

                    preferencesRepository.setPhotoStartTime(currentTime)
                    preferencesRepository.setPhotoDeadline(deadline)
                    preferencesRepository.setCamActivationTime(newActivationTime)

                }
                startTimer()
            }
        }
    }

    private fun startTimer() {
        viewModelScope.launch {
            while (true) {
                val currentTime = System.currentTimeMillis()
                val startTime = photoStartTime.first()
                val deadline = photoDeadline.first()
                val activation = camActivationTime.first()
                val showCam = showCam.first()

                completeTimerLoading()

                Log.i("Timer", "Current Time: $currentTime")
                Log.i("Timer", "Start Time: $startTime")
                Log.i("Timer", "Deadline: $deadline")
                Log.i("Timer", "Activation: $activation")
                Log.i("Timer", "Show Cam: $showCam")


                when (currentTime) {
                    in startTime..<deadline -> {
                        //checkTime(startTime, deadline)
                        _uiState.update { state ->
                            state.copy(
                                timerState = TimerState(
                                    photoTimeRemaining = deadline - currentTime,
                                    camActivationTimeRemaining = null
                                ),
                                showCam = true
                            )
                        }
                    }
                    in deadline..<activation -> {
                        preferencesRepository.setShowCam(true)
                        _uiState.update { state ->
                            state.copy(
                                timerState = TimerState(
                                    photoTimeRemaining = null,
                                    camActivationTimeRemaining = activation - currentTime
                                ),
                                showCam = false
                            )
                        }
                    }
                    else -> {
                        if(showCam){
                            _uiState.update { state ->
                                state.copy(
                                    timerState = TimerState(
                                        photoTimeRemaining = null,
                                        camActivationTimeRemaining = null,
                                    ),
                                    showCam = true
                                )
                            }
                        }else{
                            _uiState.update { state ->
                                state.copy(
                                    timerState = TimerState(
                                        photoTimeRemaining = null,
                                        camActivationTimeRemaining = null,
                                    ),
                                    showCam = false
                                )
                            }
                        }

                    }
                }
                delay(1000L)
            }
        }
    }

    private suspend fun checkTime(startTime: Long, deadline: Long){
        if(deadline - startTime > 1000L * 60 * 60 * 3){
            preferencesRepository.setPhotoStartTime(deadline - 1000L * 60 * 60 * 3)
        }
    }


    private fun generateRandomTime(): Long {
        val currentTime = Calendar.getInstance()
        val startHour = 9
        val endHour = 20

        val start = currentTime.apply {
            add(Calendar.DAY_OF_YEAR, 1)
            set(Calendar.HOUR_OF_DAY, startHour)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
        }.timeInMillis

        val end = currentTime.apply {
            set(Calendar.HOUR_OF_DAY, endHour)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.SECOND, 0)
        }.timeInMillis

        return (start..end).random() - System.currentTimeMillis()
    }

    fun scheduleNotification(context: Context){
        val randomDelay = generateRandomTime()
        NotificationScheduler.scheduleRandomNotification(context, randomDelay)
        viewModelScope.launch {
            val calendar = Calendar.getInstance()
            calendar.add(Calendar.DAY_OF_YEAR, 2)
            calendar.set(Calendar.HOUR_OF_DAY, 0)
            calendar.set(Calendar.MINUTE, 0)
            calendar.set(Calendar.SECOND, 0)
            calendar.set(Calendar.MILLISECOND, 0)
            val newActivationTime = calendar.timeInMillis

            val currentTime = System.currentTimeMillis()
            val photoStartTime = currentTime + randomDelay
            val deadline = photoStartTime + 1000L * 60 * 60 * 3

            preferencesRepository.setShowCam(false)
            preferencesRepository.setPhotoStartTime(photoStartTime)
            preferencesRepository.setPhotoDeadline(deadline)
            preferencesRepository.setCamActivationTime(newActivationTime)
        }
    }

    fun completeCalendarLoading(){
        _uiState.value = _uiState.value.copy(
            calendarLoadingComplete = true
        )
    }

    private fun completeTimerLoading(){
        _uiState.value = _uiState.value.copy(
            timerLoadingComplete = true
        )
    }

    fun changeLayout(layout: Layout){
        _uiState.value = _uiState.value.copy(
            layout = layout
        )
    }

    fun savePhoto(bitmap: Bitmap, fileName: String, context: Context): String?{
        val directory = context.filesDir
        val photoFile = File(directory, fileName)

        return try {
            FileOutputStream(photoFile).use { outputStream ->
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
            }
            photoFile.absolutePath
        } catch (e: Exception){
            e.printStackTrace()
            null
        }
    }

    fun exportPhoto(date: Date, context: Context): String?{
        val directory = context.filesDir
        val fileName = "CA_${date.day}_${date.month}_${date.year}.jpg"
        val albumName = "LifeFlow"
        val photoFile = File(directory, fileName)

        if (!photoFile.exists()) {
            return null
        }

        val contentResolver = context.contentResolver
        val values = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, fileName)
            put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
            put(MediaStore.Images.Media.DATE_ADDED, System.currentTimeMillis() / 1000)
            put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/$albumName")
        }

        val uri = contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)

        return if (uri != null) {
            try {
                context.contentResolver.openOutputStream(uri).use { outputStream ->
                    photoFile.inputStream().use { inputStream ->
                        inputStream.copyTo(outputStream!!)
                    }
                }
                uri.toString()
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        } else {
            null
        }
    }

    fun deletePhoto(date: Date, context: Context){
        val fileName = "CA_${date.day}_${date.month}_${date.year}.jpg"
        val file = File(context.filesDir, fileName)
        file.delete()
    }

    fun clickOnDate(date: Date){
        _uiState.value = _uiState.value.copy(
            showImageDialog = true,
            selectedDate = date
        )
    }

    fun closeImageDialog(){
        _uiState.value = _uiState.value.copy(
            showImageDialog = false,
            selectedDate = null
        )
    }

    @OptIn(ExperimentalPermissionsApi::class)
    fun openCameraScreen(cameraPermissionState: PermissionState){
        when {
            cameraPermissionState.status.isGranted -> {
                _uiState.value = _uiState.value.copy(screenToShow = Screen.CAMERA)
            }
            else -> {
                cameraPermissionState.launchPermissionRequest()
            }
        }
    }

    fun closeCameraScreen(){
        _uiState.value = _uiState.value.copy(
            screenToShow = Screen.CALENDAR
        )
    }

    fun getWeekDays(
        index: Int,
        context: Context,
        layout: Layout
    ): List<Date> {
        val today = Calendar.getInstance()
        val todayWeekIndex = Int.MAX_VALUE / 2
        val weeks = index - todayWeekIndex
        if(layout == Layout.TWO_WEEKS){
            today.add(Calendar.WEEK_OF_YEAR, weeks * 2)
        }else{
            today.add(Calendar.WEEK_OF_YEAR, weeks)
        }
        today.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
        val days = mutableListOf<Date>()
        val range =
            when (layout) {
                Layout.WEEK -> 6
                Layout.TWO_WEEKS -> 13
            }
        for (i in 0..range) {

            val day = today.get(Calendar.DAY_OF_MONTH)
            val month = today.get(Calendar.MONTH) + 1
            val year = today.get(Calendar.YEAR)
            val fileName = "CA_${day}_${month}_${year}.jpg"
            val file = File(context.filesDir, fileName)
            val path = if(file.exists()) file.absolutePath else null

            days.add(
                Date(
                    day = day,
                    month = month,
                    year = year,
                    path = path
                )
            )
            today.add(Calendar.DAY_OF_MONTH, 1)
        }

        return days
    }

}