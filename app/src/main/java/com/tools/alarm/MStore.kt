package com.tools.alarm

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.lang.Exception

class MStore(context: Context) {

    private val context: Context

    init {
        this.context = context
    }

    companion object {
        private val Context.dataStore: DataStore<Preferences> by preferencesDataStore("dataSt")
    }

    suspend fun getData(key: String): String {
        val storeKEY = stringPreferencesKey(key)
        return context.dataStore.data.map { preferences -> preferences[storeKEY] ?: "null" }.first()
    }

    suspend fun setData(key: String, value: String): Boolean {
        return try {
            val storeKEY = stringPreferencesKey(key)
            context.dataStore.edit { preferences -> preferences[storeKEY] = value }
            true
        } catch (e: Exception) {
            false
        }
    }

    suspend fun clearData(): Boolean {
        return try {
            context.dataStore.edit { it.clear() }
            true
        } catch (e: Exception) {
            false
        }
    }
}