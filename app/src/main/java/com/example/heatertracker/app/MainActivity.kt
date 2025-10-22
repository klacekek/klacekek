package com.example.heatertracker.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import com.example.heatertracker.ui.HeaterTrackerApp
import com.example.heatertracker.ui.HeaterTrackerViewModel
import com.example.heatertracker.ui.theme.HeaterTrackerTheme

class MainActivity : ComponentActivity() {

    private val viewModel: HeaterTrackerViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            HeaterTrackerTheme {
                HeaterTrackerApp(viewModel)
            }
        }
    }
}
