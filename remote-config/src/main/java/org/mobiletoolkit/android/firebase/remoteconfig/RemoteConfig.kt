package org.mobiletoolkit.android.firebase.remoteconfig

import android.util.Log
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.gson.Gson
import com.google.gson.GsonBuilder

/**
 * Created by Sebastian Owodzin on 03/02/2019.
 */
class RemoteConfig(
    private val firebaseRemoteConfig: FirebaseRemoteConfig,
    cacheExpiration: Long = 3600L
) {

    companion object {
        private const val TAG = "RemoteConfig"

        private val gson: Gson by lazy {
            GsonBuilder().serializeNulls().create()
        }
    }

    init {
        fetchData(cacheExpiration)
    }

    fun refreshData() = fetchData(0)

    private fun fetchData(cacheExpiration: Long) {
        Log.v(TAG, "fetchData -> cacheExpiration: $cacheExpiration")

        firebaseRemoteConfig.fetch(cacheExpiration).addOnCompleteListener {
            Log.v(TAG, "fetchData -> fetch result: ${it.isSuccessful}")

            if (it.isSuccessful) {
                Log.v(TAG, "fetchData -> activating fetched data")

                firebaseRemoteConfig.activateFetched()
            }
        }
    }
}