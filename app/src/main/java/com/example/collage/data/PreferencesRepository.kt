package com.example.collage.data

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

class PreferencesRepository(
    private val dataStore: DataStore<Preferences>
) {
    private companion object {
        val SHOW_CAM = booleanPreferencesKey("show_cam")
        val PHOTO_START_TIME = longPreferencesKey("photo_start_time")
        val PHOTO_DEADLINE = longPreferencesKey("photo_deadline")
        val CAM_ACTIVATION_TIME = longPreferencesKey("cam_activation_time")
    }

    val showCam = dataStore.data.map { preferences ->
        preferences[SHOW_CAM] ?: false
    }

    val photoStartTime = dataStore.data.map { preferences ->
        preferences[PHOTO_START_TIME] ?: 0L
    }

    val photoDeadline = dataStore.data.map { preferences ->
        preferences[PHOTO_DEADLINE] ?: 0L
    }

    val camActivationTime = dataStore.data.map { preferences ->
        preferences[CAM_ACTIVATION_TIME] ?: 0L
    }

    suspend fun setShowCam(showCam: Boolean) {
        dataStore.edit { preferences ->
            preferences[SHOW_CAM] = showCam
        }
    }

    suspend fun setPhotoStartTime(photoStartTime: Long) {
        dataStore.edit { preferences ->
            preferences[PHOTO_START_TIME] = photoStartTime
        }
    }

    suspend fun setPhotoDeadline(photoDeadline: Long) {
        dataStore.edit { preferences ->
            preferences[PHOTO_DEADLINE] = photoDeadline
        }
    }

    suspend fun setCamActivationTime(camActivationTime: Long) {
        dataStore.edit { preferences ->
            preferences[CAM_ACTIVATION_TIME] = camActivationTime
        }
    }

}