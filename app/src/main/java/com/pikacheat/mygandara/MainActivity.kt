package com.pikacheat.mygandara

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.pikacheat.mygandara.navigation.MyGandaraRoot
import com.pikacheat.mygandara.ui.theme.MyGandaraTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyGandaraTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    MyGandaraRoot()
                }
            }
        }
    }
}
