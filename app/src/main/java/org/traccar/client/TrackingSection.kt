package org.traccar.client

import android.Manifest
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.SharedPreferences.OnSharedPreferenceChangeListener
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.FrameLayout
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.preference.PreferenceManager
import com.google.android.material.button.MaterialButton
import kotlin.random.Random


class TrackingSection : Fragment(), OnSharedPreferenceChangeListener {

    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var alarmManager: AlarmManager
    private lateinit var alarmIntent: PendingIntent
    private var requestingPermissions: Boolean = false
    private lateinit var toggleButton: MaterialButton
    private lateinit var settingsButton: MaterialButton
    private lateinit var animatedCircle: View
    private lateinit var circleAnimation: Animation
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        return inflater.inflate(R.layout.tracking_section, container, false)
    }
    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupSharedPreferences()
        initPreferences()
        setupAlarmManager()
        setupToggleButton(view)
        setupTrackingState()
        setupCopyButton(view)
        setupAnimation(view)
        toggleAnimation()
    }

    private fun setupSharedPreferences() {
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(requireContext())
        sharedPreferences.registerOnSharedPreferenceChangeListener(this)
    }

    private fun setupAlarmManager() {
        alarmManager = requireContext().getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val originalIntent = Intent(requireContext(), AutostartReceiver::class.java).apply {
            addFlags(Intent.FLAG_RECEIVER_FOREGROUND)
        }

        val flags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
        } else {
            PendingIntent.FLAG_UPDATE_CURRENT
        }
        alarmIntent = PendingIntent.getBroadcast(requireContext(), 0, originalIntent, flags)
    }

    private fun setupToggleButton(view: View) {
        settingsButton = view.findViewById(R.id.settings_button)
        toggleButton = view.findViewById(R.id.button_toggle_service)

        settingsButton.setOnClickListener{
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment, MainFragment())
                .addToBackStack(null) // This allows the back button to reverse the transaction
                .commit()
        }
        toggleButton.setOnClickListener {
            toggleTracking(sharedPreferences.getBoolean(KEY_STATUS, false))
        }
    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    private fun setupTrackingState() {

        if (sharedPreferences.getBoolean(KEY_STATUS, false)) {
            toggleButton.apply {

                text = getString(R.string.stop_tracking)
                backgroundTintList = null
                background = ContextCompat.getDrawable(requireContext(), R.drawable.tracking_stopped_button)
                icon = ContextCompat.getDrawable(requireContext(), R.drawable.baseline_not_started_24)
            }
            startTrackingService(checkPermission = true, initialPermission = false)
        } else {
            toggleButton.apply {
                text = getString(R.string.start_tracking)
                backgroundTintList = null
                background = ContextCompat.getDrawable(requireContext(), R.drawable.tracking_started_button)
                icon = ContextCompat.getDrawable(requireContext(), R.drawable.baseline_pause_circle_24)
            }

        }
    }

    private fun setupAnimation(view: View){
        animatedCircle = view.findViewById(R.id.animatedCircle1)

        val screenHeight = resources.displayMetrics.heightPixels
        val margin = (screenHeight * 0.08).toInt()
        val height = (screenHeight * 0.3).toInt()

        val params = animatedCircle.layoutParams as FrameLayout.LayoutParams
        params.topMargin = margin
        params.height = height
        params.width = height
        animatedCircle.layoutParams = params

        circleAnimation = AnimationUtils.loadAnimation(requireContext(), R.anim.scale_anim_1)
    }
    private fun setupCopyButton(view: View) {
        view.findViewById<ImageButton>(R.id.button_copy_id).setOnClickListener {
            sharedPreferences.getString(KEY_DEVICE, "0000")?.let { text -> copyTextToClipboard(text) }
        }
        view.findViewById<TextView>(R.id.trackingIdTextView).text = sharedPreferences.getString(KEY_DEVICE, "0000")
    }


    private fun copyTextToClipboard(text: String) {
        val clipboard = requireContext().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("label", text)
        clipboard.setPrimaryClip(clip)
        Toast.makeText(requireContext(), getString(R.string.text_copied), Toast.LENGTH_SHORT).show()
    }
    private fun initPreferences() {
        if (!sharedPreferences.contains(KEY_DEVICE)) {
            val chars = ('A'..'Z')
            val randomChars = (1..3)
                .map { chars.random() }.joinToString("")
            val id = "$randomChars${(Random.nextInt(900000) + 100000)}"

            sharedPreferences.edit().putString(KEY_DEVICE, id).apply()

        }
    }

    private fun toggleTracking(isTracking: Boolean){
        if(isTracking){
            PreferenceManager.getDefaultSharedPreferences(requireContext()).edit().putBoolean(MainActivity.KEY_STATUS, false).apply()
            Toast.makeText(requireContext(), R.string.status_service_destroy, Toast.LENGTH_SHORT).show()

        }else{
            PreferenceManager.getDefaultSharedPreferences(requireContext()).edit().putBoolean(MainActivity.KEY_STATUS, true).apply()
            Toast.makeText(requireContext(), R.string.status_service_create, Toast.LENGTH_SHORT).show()
        }
        toggleAnimation()
        updateToggleButton(!isTracking)
    }
    private fun updateToggleButton(isTracking: Boolean) {
        if (isTracking) {
            toggleButton.text = getString(R.string.stop_tracking)
            toggleButton.setBackgroundResource(R.drawable.tracking_stopped_button)
            toggleButton.icon = ContextCompat.getDrawable(requireContext(), R.drawable.baseline_not_started_24)


        } else {
            toggleButton.text = getString(R.string.start_tracking)
            toggleButton.setBackgroundResource(R.drawable.tracking_started_button)
            toggleButton.icon = ContextCompat.getDrawable(requireContext(), R.drawable.baseline_pause_circle_24)

        }
    }
    private fun toggleAnimation(){
        if(sharedPreferences.getBoolean(KEY_STATUS, false)){
            animatedCircle.startAnimation(circleAnimation)
        }else{
            animatedCircle.clearAnimation()
        }
    }
    override fun onStart() {
        super.onStart()
        if (requestingPermissions) {
            requestingPermissions = BatteryOptimizationHelper().requestException(requireContext())
        }
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        if (key == KEY_STATUS) {
            if (sharedPreferences?.getBoolean(KEY_STATUS, false) == true) {
                startTrackingService(checkPermission = true, initialPermission = false)
            } else {
                stopTrackingService()
            }
        }
        Log.d(TAG, "updated shared prefs: ${sharedPreferences?.getBoolean(KEY_STATUS, false)}")
    }

    private fun startTrackingService(checkPermission: Boolean, initialPermission: Boolean) {
        if (!isAdded) return

        var permission = initialPermission
        if (checkPermission) {
            val requiredPermissions: MutableSet<String> = HashSet()
            if (ContextCompat.checkSelfPermission(
                    requireContext(),
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                requiredPermissions.add(Manifest.permission.ACCESS_FINE_LOCATION)
            }
            permission = requiredPermissions.isEmpty()
            if (!permission) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    requestPermissions(
                        requiredPermissions.toTypedArray(),
                        PERMISSIONS_REQUEST_LOCATION
                    )
                }
                return
            }
        }
        if (permission) {
            ContextCompat.startForegroundService(
                requireContext(),
                Intent(requireContext(), TrackingService::class.java)
            )
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) {
                alarmManager.setInexactRepeating(
                    AlarmManager.ELAPSED_REALTIME_WAKEUP,
                    ALARM_MANAGER_INTERVAL.toLong(), ALARM_MANAGER_INTERVAL.toLong(), alarmIntent
                )
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q
                && ContextCompat.checkSelfPermission(
                    requireContext(),
                    Manifest.permission.ACCESS_BACKGROUND_LOCATION
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                requestingPermissions = true
                showBackgroundLocationDialog(requireContext()) {
                    requestPermissions(
                        arrayOf(Manifest.permission.ACCESS_BACKGROUND_LOCATION),
                        PERMISSIONS_REQUEST_BACKGROUND_LOCATION
                    )
                }
            } else {
                requestingPermissions =
                    BatteryOptimizationHelper().requestException(requireContext())
            }

//            sharedPreferences.edit().putBoolean(KEY_STATUS, true).apply()


        } else {
            sharedPreferences.edit().putBoolean(KEY_STATUS, false).apply()

        }
    }

    private fun stopTrackingService() {
        Log.d(TAG, "service has stopped")
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) {
            alarmManager.cancel(alarmIntent)
        }
        if (!isAdded) return
        requireActivity().stopService(Intent(requireContext(), TrackingService::class.java))
//        sharedPreferences.edit().putBoolean(KEY_STATUS, false).apply()

    }
    override fun onResume() {
        super.onResume()
        sharedPreferences.registerOnSharedPreferenceChangeListener(this)
    }

    override fun onPause() {
        super.onPause()
        sharedPreferences.unregisterOnSharedPreferenceChangeListener(this)
    }
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray,
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSIONS_REQUEST_LOCATION) {
            var granted = true
            for (result in grantResults) {
                if (result != PackageManager.PERMISSION_GRANTED) {
                    granted = false
                    break
                }
            }
            startTrackingService(false, granted)
        }
    }

    private fun showBackgroundLocationDialog(context: Context, onSuccess: () -> Unit) {
        val builder = AlertDialog.Builder(context)
        val option = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            context.packageManager.backgroundPermissionOptionLabel
        } else {
            context.getString(R.string.request_background_option)
        }
        builder.setMessage(context.getString(R.string.request_background, option))
        builder.setPositiveButton(android.R.string.ok) { _, _ -> onSuccess() }
        builder.setNegativeButton(android.R.string.cancel, null)
        builder.show()
    }



    companion object {
        private val TAG = TrackingSection::class.java.simpleName
        private const val ALARM_MANAGER_INTERVAL = 15000
        const val KEY_DEVICE = "id"
        const val KEY_STATUS = "status"
        private const val PERMISSIONS_REQUEST_LOCATION = 2
        private const val PERMISSIONS_REQUEST_BACKGROUND_LOCATION = 3
    }
}