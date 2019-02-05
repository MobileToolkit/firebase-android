package org.mobiletoolkit.android.firebase.remoteconfig.delegates

import com.google.firebase.remoteconfig.FirebaseRemoteConfig

/**
 * Created by Sebastian Owodzin on 04/02/2019.
 */
interface FirebaseRemoteConfigProvider {
    val firebaseRemoteConfig: FirebaseRemoteConfig
}