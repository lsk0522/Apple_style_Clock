package com.lsk0522.nightstand.core.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore

/**
 * The app's single preference store. Declared once here because the delegate
 * must not be created twice for the same file name — doing so throws at
 * runtime the moment both are touched.
 */
internal val Context.nightstandDataStore: DataStore<Preferences> by
    preferencesDataStore(name = "nightstand")
