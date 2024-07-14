/*
 * Copyright 2017 - 2021 Anton Tananaev (anton@traccar.org)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.bazytrack.weenak

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        setContentView(R.layout.tracking_section)
        setContentView(R.layout.main)

        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragment, TrackingSection())
                .commit()
        }

    }


    companion object {
        private val TAG = "Traccar"
        private const val ALARM_MANAGER_INTERVAL = 15000
        const val KEY_DEVICE = "id"
        const val KEY_URL = "url"
        const val KEY_INTERVAL = "interval"
        const val KEY_DISTANCE = "distance"
        const val KEY_ANGLE = "angle"
        const val KEY_ACCURACY = "accuracy"
        const val KEY_STATUS = "status"
        const val KEY_BUFFER = "buffer"
        const val KEY_WAKELOCK = "wakelock"
        const val TRACKING_SECTION = "open_section"
        private const val PERMISSIONS_REQUEST_LOCATION = 2
        private const val PERMISSIONS_REQUEST_BACKGROUND_LOCATION = 3
    }
}
