package com.example.collage.ui

import android.app.Application
import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import com.example.collage.data.PreferencesRepository

private const val PREFERENCES_NAME = "preferences"

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(
    name = PREFERENCES_NAME
)


class MyApplication: Application() {
    lateinit var preferencesRepository: PreferencesRepository
    override fun onCreate() {
        super.onCreate()
        preferencesRepository = PreferencesRepository(dataStore)
    }

}