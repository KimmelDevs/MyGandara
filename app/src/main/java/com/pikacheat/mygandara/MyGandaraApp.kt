package com.pikacheat.mygandara

import android.app.Application
import com.pikacheat.mygandara.data.AppContainer

class MyGandaraApp : Application() {
    val container: AppContainer by lazy { AppContainer() }
}
