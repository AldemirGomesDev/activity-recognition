package br.com.aldemir.activityrecognition

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import br.com.aldemir.activityrecognition.helper.*
import br.com.aldemir.activityrecognition.services.DetectedActivityService
import kotlinx.android.synthetic.main.activity_main.*

const val TAG = "ActivityUpdate"

class MainActivity : AppCompatActivity() {

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        Log.d("ActivityUpdate", "onNewIntent")
        if (intent.hasExtra(SUPPORTED_ACTIVITY_KEY)) {
            val supportedActivity = intent.getSerializableExtra(
                SUPPORTED_ACTIVITY_KEY
            ) as SupportedActivity
            setDetectedActivity(supportedActivity)
        }
    }

    private val broadcastReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent) {
            val bundle = intent.extras
            bundle?.let {
                val resultConfidence = bundle.getInt(getString(R.string.activity_confidence))
                val confidence = "Nível de confiança: $resultConfidence%"
                activityConfidence.text = confidence
            }

            if (intent.hasExtra(SUPPORTED_ACTIVITY_KEY)) {
                val supportedActivity = intent.getSerializableExtra(
                    SUPPORTED_ACTIVITY_KEY
                ) as SupportedActivity
                setDetectedActivity(supportedActivity)
            }

        }
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        registerReceiver(
            broadcastReceiver,
            IntentFilter(getString(R.string.broadcast_detected_activity))
        )
        startRecognition()

        activityTitle.text = getString(R.string.waiting_title)
        activityConfidence.text = getString(R.string.waiting_text)
    }

    override fun onDestroy() {
        stopRecognition()
        super.onDestroy()
    }

    private fun stopRecognition() {
        stopService(Intent(this, DetectedActivityService::class.java))
        Toast.makeText(this, "Você parou de rastrear sua atividade", Toast.LENGTH_SHORT).show()
    }

    private fun startRecognition() {
        if (isPermissionGranted()) {
            startService(Intent(this, DetectedActivityService::class.java))
            Toast.makeText(this@MainActivity, "Você iniciou o rastreamento de atividades",
            Toast.LENGTH_SHORT).show()
        } else {
            requestPermission()
        }
    }

    private fun setDetectedActivity(supportedActivity: SupportedActivity) {
        activityImage.setImageDrawable(
            ContextCompat.getDrawable(
                this,
                supportedActivity.activityImage
            )
        )
        activityTitle.text = getString(supportedActivity.activityText)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        Log.d("ActivityUpdate", "permission granted")
        if (ActivityCompat.shouldShowRequestPermissionRationale(
                this,
                Manifest.permission.ACTIVITY_RECOGNITION
            ).not() &&
            grantResults.size == 1 &&
            grantResults[0] == PackageManager.PERMISSION_DENIED
        ) {
            showSettingsDialog(this)
        } else if (requestCode == PERMISSION_REQUEST_ACTIVITY_RECOGNITION &&
            permissions.contains(Manifest.permission.ACTIVITY_RECOGNITION) &&
            grantResults.size == 1 &&
            grantResults[0] == PackageManager.PERMISSION_GRANTED
        ) {
            startService(Intent(this, DetectedActivityService::class.java))
        }
    }
}