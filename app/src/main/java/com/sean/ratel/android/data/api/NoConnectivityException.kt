package com.sean.ratel.android.data.api

import java.io.IOException

class NoConnectivityException : IOException() {
    override val message: String = "No internet connection"
}
