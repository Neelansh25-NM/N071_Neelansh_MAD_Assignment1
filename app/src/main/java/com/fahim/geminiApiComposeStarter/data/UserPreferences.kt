
package com.fahim.geminiApiComposeStarter.data

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.userPreferencesDataStore
        by preferencesDataStore(
            name = "user_preferences"
        )

interface UserPreferences {

    val userName: Flow<String>

    suspend fun saveUserName(
        name: String
    )
}

class UserPreferencesImpl(
    private val context: Context
) : UserPreferences {

    private val userNameKey =
        stringPreferencesKey("user_name")

    override val userName: Flow<String> =
        context.userPreferencesDataStore
            .data
            .map { preferences ->
                preferences[userNameKey]
                    ?: ""
            }

    override suspend fun saveUserName(
        name: String
    ) {

        context.userPreferencesDataStore
            .edit { preferences ->
                preferences[userNameKey] = name
            }
    }
}