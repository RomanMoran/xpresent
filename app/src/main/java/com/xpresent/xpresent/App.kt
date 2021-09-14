package com.xpresent.xpresent

import android.app.Application
import com.jivosite.sdk.Jivo
import com.jivosite.sdk.support.builders.Config
import timber.log.Timber

/**
 * Created on 4/19/21.
 *
 * @author Alexander Tavtorkin (av.tavtorkin@gmail.com)
 */
class App : Application() {

    override fun onCreate() {
        super.onCreate()
        Timber.plant(Timber.DebugTree())
        Jivo.init(
            appContext = this,
            widgetId = "bx5IAdiHb6" // "Q7BcPYNqCG"
        )
        Jivo.enableLogging()

        Jivo.setConfig(
            Config.Builder()
                //.setLogo(R.drawable.vic_jivosdk_logo)
                //.setBackground(R.drawable.bg_jivosdk_appbar)
                .setTitle(R.string.jivo_title)
                .setTitleTextColor(R.color.white)
                .setSubtitle(R.string.jivo_subtitle)
                .setSubtitleTextColor(R.color.white)
                .setSubtitleTextColorAlpha(0.6f)
                .setWelcomeMessage(R.string.jivo_welcome)
                .setOutgoingMessageColor(Config.Color.GREY)
                .build()
        )
    }
}