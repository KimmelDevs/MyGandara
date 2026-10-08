package com.pikacheat.mygandara

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.pikacheat.mygandara.i18n.LocalLanguage
import com.pikacheat.mygandara.i18n.LocalSetLanguage
import com.pikacheat.mygandara.navigation.MyGandaraRoot
import com.pikacheat.mygandara.ui.theme.MyGandaraTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val localStore = (application as MyGandaraApp).container.localStore
        setContent {
            var language by remember { mutableStateOf(localStore.language) }
            CompositionLocalProvider(
                LocalLanguage provides language,
                LocalSetLanguage provides { chosen ->
                    language = chosen
                    localStore.language = chosen
                }
            ) {
                MyGandaraTheme {
                    Surface(modifier = Modifier.fillMaxSize()) {
                        MyGandaraRoot()
                    }
                }
            }
        }
    }
}
