package fr.retourecole.child

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class MainActivity : ComponentActivity() {

    private var tracking by mutableStateOf(false)
    private var latitude by mutableStateOf<Double?>(null)
    private var longitude by mutableStateOf<Double?>(null)

    private val permissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { result ->
            val granted = result[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                result[Manifest.permission.ACCESS_COARSE_LOCATION] == true

            if (granted) startTracking()
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            MaterialTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "RETOUR ÉCOLE",
                            style = MaterialTheme.typography.headlineMedium
                        )

                        Spacer(Modifier.height(16.dp))

                        Text(
                            text = if (tracking) "🟢 SUIVI EN COURS" else "Suivi arrêté",
                            style = MaterialTheme.typography.titleLarge
                        )

                        Spacer(Modifier.height(24.dp))

                        if (latitude != null && longitude != null) {
                            Text("Latitude : %.6f".format(latitude))
                            Text("Longitude : %.6f".format(longitude))
                            Spacer(Modifier.height(16.dp))
                        }

                        Button(
                            onClick = {
                                if (tracking) stopTracking()
                                else requestPermissionsAndStart()
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(if (tracking) "ARRÊTER LE SUIVI" else "🟢 DÉMARRER LE SUIVI")
                        }

                        Spacer(Modifier.height(16.dp))

                        if (tracking) {
                            Text(
                                "Tu peux verrouiller le téléphone.\nLe suivi continue.",
                                style = MaterialTheme.typography.bodyLarge
                            )
                        }
                    }
                }
            }
        }
    }

    private fun requestPermissionsAndStart() {
        val fineGranted = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        val coarseGranted = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        if (fineGranted || coarseGranted) {
            startTracking()
        } else {
            permissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.POST_NOTIFICATIONS
                )
            )
        }
    }

    private fun startTracking() {
        val intent = Intent(this, LocationService::class.java)
            .setAction(LocationService.ACTION_START)

        ContextCompat.startForegroundService(this, intent)
        tracking = true
        observeLastKnownLocation()
    }

    private fun stopTracking() {
        val intent = Intent(this, LocationService::class.java)
            .setAction(LocationService.ACTION_STOP)

        startService(intent)
        tracking = false
        latitude = null
        longitude = null
    }

    private fun observeLastKnownLocation() {
        val client = LocationServices.getFusedLocationProviderClient(this)
        client.lastLocation.addOnSuccessListener { location ->
            if (location != null && tracking) {
                latitude = location.latitude
                longitude = location.longitude
            }
        }
    }
}
