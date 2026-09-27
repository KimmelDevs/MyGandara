package com.pikacheat.mygandara

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.pikacheat.mygandara.navigation.MyGandaraNavGraph
import com.pikacheat.mygandara.ui.theme.MyGandaraTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyGandaraTheme {
                MyGandaraNavGraph()
            }
        }
    }
}
