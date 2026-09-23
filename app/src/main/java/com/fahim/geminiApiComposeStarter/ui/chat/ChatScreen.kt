package com.fahim.geminiApiComposeStarter.chat

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.speech.RecognizerIntent

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width

import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.remember

import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp

import androidx.core.content.ContextCompat

import androidx.lifecycle.compose.collectAsStateWithLifecycle


@Composable
fun ChatScreen(
    viewModel: ChatViewModel
) {

    val uiState by
    viewModel.uiState.collectAsStateWithLifecycle()

    /*
     * Local light/dark mode state.
     *
     * false = Light Mode
     * true  = Dark Mode
     */
    var isDarkMode by rememberSaveable {
        mutableStateOf(false)
    }

    val colorScheme =
        if (isDarkMode) {
            darkColorScheme()
        } else {
            lightColorScheme()
        }

    /*
     * This MaterialTheme controls the appearance of
     * the ChatScreen itself.
     */
    MaterialTheme(
        colorScheme = colorScheme
    ) {

        ChatScreenContent(
            state = uiState,
            isDarkMode = isDarkMode,
            onToggleTheme = {
                isDarkMode = !isDarkMode
            },
            onInputChange = viewModel::updateInput,
            onSend = viewModel::sendMessage,
            onClearError = viewModel::clearError,
            onOpenPreferences = viewModel::openPreferences,
            onClosePreferences = viewModel::closePreferences,
            onNameChange = viewModel::updateNameDraft,
            onSavePreferences = viewModel::savePreferences,
            onClearChat = viewModel::clearChat
        )
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ChatScreenContent(
    state: ChatUiState,
    isDarkMode: Boolean,
    onToggleTheme: () -> Unit,
    onInputChange: (String) -> Unit,
    onSend: () -> Unit,
    onClearError: () -> Unit,
    onOpenPreferences: () -> Unit,
    onClosePreferences: () -> Unit,
    onNameChange: (String) -> Unit,
    onSavePreferences: () -> Unit,
    onClearChat: () -> Unit
) {

    val context = LocalContext.current

    val listState =
        rememberLazyListState()


    /*
     * Speech recognizer configuration.
     */
    val speechIntent = remember {

        Intent(
            RecognizerIntent.ACTION_RECOGNIZE_SPEECH
        ).apply {

            putExtra(
                RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
            )

            putExtra(
                RecognizerIntent.EXTRA_LANGUAGE,
                "en-US"
            )

            putExtra(
                RecognizerIntent.EXTRA_PROMPT,
                "Speak your message"
            )
        }
    }


    /*
     * Receives the result returned by the speech recognizer.
     */
    val voiceLauncher =
        rememberLauncherForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->

            val recognizedText =
                result.data
                    ?.getStringArrayListExtra(
                        RecognizerIntent.EXTRA_RESULTS
                    )
                    ?.firstOrNull()

            if (!recognizedText.isNullOrBlank()) {

                onInputChange(
                    recognizedText
                )
            }
        }


    /*
     * Requests microphone permission when required.
     */
    val permissionLauncher =
        rememberLauncherForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { granted ->

            if (granted) {

                voiceLauncher.launch(
                    speechIntent
                )
            }
        }


    /*
     * Starts voice dictation.
     */
    fun startVoiceInput() {

        val permissionStatus =
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.RECORD_AUDIO
            )

        if (
            permissionStatus ==
            PackageManager.PERMISSION_GRANTED
        ) {

            voiceLauncher.launch(
                speechIntent
            )

        } else {

            permissionLauncher.launch(
                Manifest.permission.RECORD_AUDIO
            )
        }
    }


    /*
     * Automatically scroll to the newest message.
     */
    LaunchedEffect(
        state.messages.size,
        state.isLoading,
        state.errorMessage
    ) {

        val additionalItems =
            (if (state.isLoading) 1 else 0) +
                    (if (state.errorMessage != null) 1 else 0)

        val totalItems =
            state.messages.size +
                    additionalItems

        if (totalItems > 0) {

            listState.animateScrollToItem(
                totalItems - 1
            )
        }
    }


    Scaffold(

        topBar = {

            TopAppBar(

                title = {
                    Text(
                        text = "Gemini Chat"
                    )
                },

                actions = {

                    /*
                     * Light/Dark mode toggle.
                     */
                    IconButton(
                        onClick = onToggleTheme
                    ) {

                        Text(
                            text =
                                if (isDarkMode) {
                                    "☀"
                                } else {
                                    "☾"
                                }
                        )
                    }

                    /*
                     * Preferences button.
                     */
                    TextButton(
                        onClick = onOpenPreferences
                    ) {

                        Text(
                            text = "Preferences"
                        )
                    }
                }
            )
        }

    ) { innerPadding ->

        Column(

            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .navigationBarsPadding()
                .padding(horizontal = 12.dp)
        ) {


            /*
             * Personalized greeting.
             */
            if (state.userName.isNotBlank()) {

                Text(
                    text = "Hi, ${state.userName}",

                    modifier =
                        Modifier.padding(
                            start = 4.dp,
                            top = 8.dp,
                            bottom = 8.dp
                        ),

                    style =
                        MaterialTheme
                            .typography
                            .titleMedium
                )
            }


            /*
             * Chat history.
             */
            LazyColumn(

                state = listState,

                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),

                verticalArrangement =
                    Arrangement.spacedBy(8.dp)
            ) {

                items(

                    items = state.messages,

                    key = { message ->
                        message.id
                    }

                ) { message ->

                    ChatBubble(
                        message = message
                    )
                }


                /*
                 * Loading indicator.
                 */
                if (state.isLoading) {

                    item(
                        key = "loading"
                    ) {

                        Box(

                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),

                            contentAlignment =
                                Alignment.Center
                        ) {

                            CircularProgressIndicator()
                        }
                    }
                }


                /*
                 * Error card.
                 */
                state.errorMessage?.let { error ->

                    item(
                        key = "error"
                    ) {

                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(8.dp)
                        ) {

                            Column(
                                modifier =
                                    Modifier.padding(16.dp)
                            ) {

                                Text(
                                    text = "Error",

                                    style =
                                        MaterialTheme
                                            .typography
                                            .titleMedium
                                )

                                Spacer(
                                    modifier =
                                        Modifier.height(8.dp)
                                )

                                Text(
                                    text = error,

                                    style =
                                        MaterialTheme
                                            .typography
                                            .bodyMedium
                                )

                                Spacer(
                                    modifier =
                                        Modifier.height(8.dp)
                                )

                                TextButton(
                                    onClick =
                                        onClearError
                                ) {

                                    Text(
                                        text = "Dismiss"
                                    )
                                }
                            }
                        }
                    }
                }
            }


            HorizontalDivider()


            /*
             * Input row.
             */
            Row(

                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        top = 10.dp,
                        bottom = 8.dp
                    ),

                verticalAlignment =
                    Alignment.Bottom
            ) {


                /*
                 * Gemini text input.
                 *
                 * Light mode  -> black text
                 * Dark mode   -> white text
                 */
                OutlinedTextField(

                    value =
                        state.inputText,

                    onValueChange =
                        onInputChange,

                    modifier =
                        Modifier.weight(1f),

                    label = {

                        Text(
                            text = "Ask Gemini"
                        )
                    },

                    placeholder = {

                        Text(
                            text =
                                "Type or dictate a message..."
                        )
                    },

                    maxLines = 4,

                    colors =
                        OutlinedTextFieldDefaults.colors(

                            focusedTextColor =
                                if (isDarkMode) {
                                    Color.White
                                } else {
                                    Color.Black
                                },

                            unfocusedTextColor =
                                if (isDarkMode) {
                                    Color.White
                                } else {
                                    Color.Black
                                },

                            disabledTextColor =
                                if (isDarkMode) {
                                    Color.White
                                } else {
                                    Color.Black
                                },

                            cursorColor =
                                if (isDarkMode) {
                                    Color.White
                                } else {
                                    Color.Black
                                },

                            focusedLabelColor =
                                MaterialTheme
                                    .colorScheme
                                    .primary,

                            unfocusedLabelColor =
                                MaterialTheme
                                    .colorScheme
                                    .onSurfaceVariant,

                            focusedPlaceholderColor =
                                if (isDarkMode) {
                                    Color.LightGray
                                } else {
                                    Color.DarkGray
                                },

                            unfocusedPlaceholderColor =
                                if (isDarkMode) {
                                    Color.LightGray
                                } else {
                                    Color.DarkGray
                                }
                        )
                )


                Spacer(
                    modifier =
                        Modifier.width(4.dp)
                )


                /*
                 * Voice dictation.
                 */
                IconButton(
                    onClick = {
                        startVoiceInput()
                    }
                ) {

                    Text(
                        text = "🎤"
                    )
                }


                Spacer(
                    modifier =
                        Modifier.width(4.dp)
                )


                /*
                 * Send button.
                 */
                Button(

                    onClick = onSend,

                    enabled =
                        state.inputText
                            .isNotBlank() &&
                                !state.isLoading
                ) {

                    Text(
                        text = "Send"
                    )
                }
            }


            /*
             * Assignment information.
             */
            Text(

                text =
                    "Name: Neelansh Pandey, Roll No: N071, Assignment: 1",

                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        bottom = 8.dp
                    )
            )
        }
    }


    /*
     * Preferences dialog.
     */
    if (state.showPreferences) {

        AlertDialog(

            onDismissRequest =
                onClosePreferences,

            title = {

                Text(
                    text = "Preferences"
                )
            },

            text = {

                Column {

                    OutlinedTextField(

                        value =
                            state.nameDraft,

                        onValueChange =
                            onNameChange,

                        label = {

                            Text(
                                text = "Your name"
                            )
                        },

                        singleLine = true,

                        modifier =
                            Modifier.fillMaxWidth(),

                        colors =
                            OutlinedTextFieldDefaults.colors(

                                focusedTextColor =
                                    if (isDarkMode) {
                                        Color.White
                                    } else {
                                        Color.Black
                                    },

                                unfocusedTextColor =
                                    if (isDarkMode) {
                                        Color.White
                                    } else {
                                        Color.Black
                                    },

                                cursorColor =
                                    if (isDarkMode) {
                                        Color.White
                                    } else {
                                        Color.Black
                                    }
                            )
                    )

                    Spacer(
                        modifier =
                            Modifier.height(12.dp)
                    )

                    Text(
                        text =
                            "Your name is saved locally using Preferences DataStore."
                    )
                }
            },

            confirmButton = {

                TextButton(
                    onClick =
                        onSavePreferences
                ) {

                    Text(
                        text = "Save"
                    )
                }
            },

            dismissButton = {

                Row {

                    TextButton(
                        onClick =
                            onClearChat
                    ) {

                        Text(
                            text = "Clear Chat"
                        )
                    }

                    TextButton(
                        onClick =
                            onClosePreferences
                    ) {

                        Text(
                            text = "Cancel"
                        )
                    }
                }
            }
        )
    }
}


/*
 * Individual chat message bubble.
 */
@Composable
private fun ChatBubble(
    message: ChatMessage
) {

    Row(

        modifier =
            Modifier.fillMaxWidth(),

        horizontalArrangement =
            if (message.isUser) {

                Arrangement.End

            } else {

                Arrangement.Start
            }
    ) {

        Surface(

            modifier =
                Modifier.fillMaxWidth(
                    0.88f
                ),

            shape =
                MaterialTheme
                    .shapes
                    .large,

            color =
                if (message.isUser) {

                    MaterialTheme
                        .colorScheme
                        .primaryContainer

                } else {

                    MaterialTheme
                        .colorScheme
                        .surfaceVariant
                }
        ) {

            Column(
                modifier =
                    Modifier.padding(14.dp)
            ) {

                Text(

                    text =
                        if (message.isUser) {
                            "You"
                        } else {
                            "Gemini"
                        },

                    style =
                        MaterialTheme
                            .typography
                            .labelLarge
                )

                Spacer(
                    modifier =
                        Modifier.height(4.dp)
                )

                Text(

                    text =
                        message.text,

                    style =
                        MaterialTheme
                            .typography
                            .bodyLarge
                )
            }
        }
    }
}