package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.ChatViewModel
import com.example.ui.MainApp
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
  private val viewModel: ChatViewModel by viewModels()

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    setContent {
      val selectedTheme by viewModel.selectedTheme.collectAsStateWithLifecycle()
      MyApplicationTheme(appTheme = selectedTheme) {
        Surface(modifier = Modifier.fillMaxSize()) {
          MainApp(viewModel = viewModel)
        }
      }
    }
  }
}
