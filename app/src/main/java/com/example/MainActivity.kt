package com.example

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.example.ui.screens.BolaoDetailsScreen
import com.example.ui.screens.LoginScreen
import com.example.ui.screens.MainHubScreen
import com.example.ui.screens.SignUpScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.CravaCopaViewModel
import com.example.ui.viewmodel.Screen

class MainActivity : ComponentActivity() {

    private val viewModel: CravaCopaViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            MyApplicationTheme {
                val currentScreen by viewModel.currentScreen.collectAsState()
                val toastMessage by viewModel.toastMessage.collectAsState()

                // Trigger standard android toasts for reactive messages in viewmodel
                LaunchedEffect(toastMessage) {
                    toastMessage?.let {
                        Toast.makeText(this@MainActivity, it, Toast.LENGTH_SHORT).show()
                    }
                }

                // Main navigation flow with smooth transitions
                Crossfade(
                    targetState = currentScreen,
                    modifier = Modifier.fillMaxSize(),
                    label = "screen_navigation"
                ) { screen ->
                    when (screen) {
                        Screen.SIGN_UP -> SignUpScreen(viewModel)
                        Screen.LOGIN -> LoginScreen(viewModel)
                        Screen.MAIN_HUB -> MainHubScreen(viewModel)
                        Screen.BOLAO_DETAILS -> BolaoDetailsScreen(viewModel)
                    }
                }
            }
        }
    }
}
