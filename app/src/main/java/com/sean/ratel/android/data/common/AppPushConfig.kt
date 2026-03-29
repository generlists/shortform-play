package com.sean.ratel.android.data.common

import so.smartlab.common.push.fcm.data.repository.PushConfigProvider

class AppPushConfig(
    url: String,
    id: String,
    version: String,
) : PushConfigProvider {
    override val apiUrl = url
    override val appId = id
    override val appVersion = version
}
