package com.example.lab5_1_tts

import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.speech.tts.Voice
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.lab5_1_tts.ui.theme.TTSAppTheme
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            TTSAppTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    TTSScreen()
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TTSScreen() {
    val context = LocalContext.current
    var text by remember { mutableStateOf("") }
    var selectedLanguage by remember { mutableStateOf("Español") }
    var selectedAccent by remember { mutableStateOf("") }

    val allVoices = remember { mutableStateListOf<Voice>() }
    var accentOptions by remember { mutableStateOf(listOf<String>()) }
    var ttsReady by remember { mutableStateOf(false) }

    val tts = remember {
        TextToSpeech(context) { status ->
            ttsReady = status == TextToSpeech.SUCCESS
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            tts.stop()
            tts.shutdown()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp, vertical = 32.dp),
        verticalArrangement = Arrangement.Top
    ) {
        Text(
            text = "Text to Speech",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .padding(bottom = 24.dp)
        )

        OutlinedTextField(
            value = text,
            onValueChange = { text = it },
            label = { Text("Texto a leer") },
            modifier = Modifier
                .fillMaxWidth()
                .height(150.dp)
        )

        Spacer(modifier = Modifier.height(24.dp))

        LanguageDropdown(selectedLanguage) {
            selectedLanguage = it

            val locale = when (it) {
                "Español" -> Locale("es", "ES")
                "Inglés" -> Locale("en", "US")
                "Francés" -> Locale("fr", "FR")
                "Mandarín" -> Locale.SIMPLIFIED_CHINESE
                else -> Locale.getDefault()
            }

            val result = tts.setLanguage(locale)
            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                accentOptions = emptyList()
                return@LanguageDropdown
            }

            val voices = tts.voices?.filter { it.locale.language == locale.language } ?: emptyList()
            allVoices.clear()
            allVoices.addAll(voices)

            accentOptions = voices.map { "${it.locale.displayCountry} - ${it.name}" }.distinct()
            if (accentOptions.isNotEmpty()) {
                selectedAccent = accentOptions.first()
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        AccentDropdown(
            options = accentOptions,
            selected = selectedAccent,
            onSelect = { selectedAccent = it },
            enabled = accentOptions.isNotEmpty()
        )

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = {
                if (!ttsReady || text.isBlank()) return@Button

                val selectedVoice = allVoices.firstOrNull {
                    "${it.locale.displayCountry} - ${it.name}" == selectedAccent
                }

                selectedVoice?.let {
                    tts.voice = it
                }

                tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, null)
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Reproducir")
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LanguageDropdown(selected: String, onSelect: (String) -> Unit) {
    val options = listOf("Español", "Inglés", "Francés", "Mandarín")
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = !expanded }) {
        OutlinedTextField(
            value = selected,
            onValueChange = {},
            readOnly = true,
            label = { Text("Idioma") },
            modifier = Modifier.menuAnchor()
        )
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            options.forEach {
                DropdownMenuItem(text = { Text(it) }, onClick = {
                    onSelect(it)
                    expanded = false
                })
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccentDropdown(
    options: List<String>,
    selected: String,
    onSelect: (String) -> Unit,
    enabled: Boolean = true
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { if (enabled) expanded = !expanded }
    ) {
        OutlinedTextField(
            value = selected,
            onValueChange = {},
            readOnly = true,
            label = { Text("Acento") },
            modifier = Modifier.menuAnchor(),
            enabled = enabled
        )
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            options.forEach {
                DropdownMenuItem(text = { Text(it) }, onClick = {
                    onSelect(it)
                    expanded = false
                })
            }
        }
    }
}