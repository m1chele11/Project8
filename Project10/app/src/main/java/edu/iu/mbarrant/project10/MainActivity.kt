package edu.iu.mbarrant.project10

import android.content.Context
import android.content.res.Configuration
import android.hardware.SensorEvent
import android.hardware.SensorManager
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChange
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.LifecycleOwner
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import java.io.IOException
import java.util.Locale
import kotlin.math.roundToInt
import android.Manifest
import android.hardware.Sensor
import android.hardware.SensorEventListener
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.remember
import androidx.activity.result.ActivityResultLauncher
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.MutableState
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberPermissionState


class MainActivity : ComponentActivity() {

    private lateinit var sensorManager: SensorManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        // Obtain a reference to the SensorManager
        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager

        setContent {



            val navController = rememberNavController()

            NavHost(navController = navController, startDestination = "home") {
                composable("home") {
                    SensorActivity(navController)

                }
                composable("gestureScreen") {
                    GestureActivity()
                }
                composable("gestureScreen2") {
                    GestureActivity2(sensorManager)
                }
            }


        }
    }
    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            // Handle changes specific to landscape orientation if needed
            Log.d("Orientation changed", "in landscape mode")
        } else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {
            // Handle changes specific to portrait orientation if needed
            Log.d("Orientation changed", "in portatit mode")
        }
    }
}



@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun SensorActivity(navController: NavHostController) {

    val sensorManager = LocalContext.current.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    val temperatureSensor = sensorManager.getDefaultSensor(Sensor.TYPE_AMBIENT_TEMPERATURE)
    val pressureSensor = sensorManager.getDefaultSensor(Sensor.TYPE_PRESSURE)

    val temperatureData = remember { mutableStateOf<Float?>(null) }
    val pressureData = remember { mutableStateOf<Float?>(null) }

    val temperatureListener = remember {
        object : SensorEventListener {
            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
                // Handle accuracy change if needed
            }

            override fun onSensorChanged(event: SensorEvent) {
                if (event.sensor.type == Sensor.TYPE_AMBIENT_TEMPERATURE) {
                    temperatureData.value = event.values[0]
                    // Log the temperature data
                    Log.d("TemperatureListener", "Temperature changed: ${event.values[0]}")
                }
            }
        }
    }
    if (temperatureSensor != null) {
        // Register temperature sensor listener and update temperatureData
        sensorManager.registerListener(
            temperatureListener,
            temperatureSensor,
            SensorManager.SENSOR_DELAY_NORMAL
        )
    } else {
        Log.d("TemperatureSensor", "Temperature sensor not available on this device")
    }

    val pressureListener = remember {
        object : SensorEventListener {
            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
                // Handle accuracy change if needed
            }

            override fun onSensorChanged(event: SensorEvent) {
                if (event.sensor.type == Sensor.TYPE_PRESSURE) {
                    pressureData.value = event.values[0]
                    // Log the pressure data
                    //Log.d("PressureListener", "Pressure changed: ${event.values[0]}")
                }
            }
        }
    }

    LaunchedEffect(Unit) {
        sensorManager.registerListener(
            temperatureListener,
            temperatureSensor,
            SensorManager.SENSOR_DELAY_NORMAL
        )
        sensorManager.registerListener(
            pressureListener,
            pressureSensor,
            SensorManager.SENSOR_DELAY_NORMAL
        )
    }


    var offset by remember { mutableStateOf(0f) }

    // Log when LocationManager is accessed
    Log.d("SensorActivity", "LocationManager accessed")



    val locationManager =
        LocalContext.current.getSystemService(Context.LOCATION_SERVICE) as LocationManager


    val locationData = remember { mutableStateOf<Location?>(null) }
    val addressData = remember { mutableStateOf<Address?>(null) }

    val geocoder = Geocoder(LocalContext.current, Locale.US)

    val locationListener = remember {
        object : LocationListener {
            override fun onLocationChanged(location: Location) {
                locationData.value = location

                // Log when location changes
                Log.d("LocationListener", "Location changed: $location")


                try {
                    val addresses =
                        geocoder.getFromLocation(location.latitude, location.longitude, 1)
                    if (addresses != null) {
                        if (addresses.isNotEmpty()) {
                            addressData.value = addresses[0]
                            // Log the retrieved address
                            Log.d("LocationListener", "Address retrieved: ${addresses[0]}")
                        }
                    }
                } catch (e: IOException) {
                    // Handle IOException
                    e.printStackTrace()
                    Log.e("LocationListener", "Geocoder error: ${e.message}")
                }
            }

            override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {}
            override fun onProviderEnabled(provider: String) {}
            override fun onProviderDisabled(provider: String) {}
        }
    }

     val MIN_TIME_BETWEEN_UPDATES = 1000L // 1 second (in milliseconds)
     val MIN_DISTANCE_CHANGE_FOR_UPDATES = 10f // 10 meters




    val coarseLocationPermissionState = rememberPermissionState(permission = Manifest.permission.ACCESS_COARSE_LOCATION)
    val fineLocationPermissionState = rememberPermissionState(permission = Manifest.permission.ACCESS_FINE_LOCATION)

    val coarseLocationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            // Permission granted for coarse location
            // Implement logic or call requestLocationUpdates()
            // Inside your SensorActivity composable function

                // Request location updates here
                try {
                    locationManager.requestLocationUpdates(
                        LocationManager.GPS_PROVIDER,
                        MIN_TIME_BETWEEN_UPDATES,  // Minimum time interval between updates
                        MIN_DISTANCE_CHANGE_FOR_UPDATES,  // Minimum distance between updates
                        locationListener
                    )
                } catch (ex: SecurityException) {
                    // Handle exception if permission is not granted
                    ex.printStackTrace()
                }
        }
    }

    val fineLocationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            // Permission granted for fine location
            // Implement logic or call requestLocationUpdates()

            // Request location updates here
            try {
                locationManager.requestLocationUpdates(
                    LocationManager.GPS_PROVIDER,
                    MIN_TIME_BETWEEN_UPDATES,  // Minimum time interval between updates
                    MIN_DISTANCE_CHANGE_FOR_UPDATES,  // Minimum distance between updates
                    locationListener
                )
            } catch (ex: SecurityException) {
                // Handle exception if permission is not granted
                ex.printStackTrace()
            }
        }
    }

    LaunchedEffect(Unit) {
        when {
            coarseLocationPermissionState.shouldShowRationale || fineLocationPermissionState.shouldShowRationale -> {
                // Handle rationale if needed
            }
            coarseLocationPermissionState.hasPermission -> {
                // Permission already granted for coarse location
                // Implement logic or call requestLocationUpdates()
                // Request location updates here
                try {
                    locationManager.requestLocationUpdates(
                        LocationManager.GPS_PROVIDER,
                        MIN_TIME_BETWEEN_UPDATES,  // Minimum time interval between updates
                        MIN_DISTANCE_CHANGE_FOR_UPDATES,  // Minimum distance between updates
                        locationListener
                    )
                } catch (ex: SecurityException) {
                    // Handle exception if permission is not granted
                    ex.printStackTrace()
                }
            }
            fineLocationPermissionState.hasPermission -> {
                // Permission already granted for fine location
                // Implement logic or call requestLocationUpdates()
                // Request location updates here
                try {
                    locationManager.requestLocationUpdates(
                        LocationManager.GPS_PROVIDER,
                        MIN_TIME_BETWEEN_UPDATES,  // Minimum time interval between updates
                        MIN_DISTANCE_CHANGE_FOR_UPDATES,  // Minimum distance between updates
                        locationListener
                    )
                } catch (ex: SecurityException) {
                    // Handle exception if permission is not granted
                    ex.printStackTrace()
                }
            }
            else -> {
                // Request permissions
                coarseLocationPermissionLauncher.launch(Manifest.permission.ACCESS_COARSE_LOCATION)
                fineLocationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
            }
        }
    }




    //Displaying everything in the UI _______________

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {

           Text(
               text = "Sensors PlayGround",
               fontSize = 28.sp, // Adjust the font size as needed
               textAlign = TextAlign.Center, // Center the text horizontally
               modifier = Modifier.fillMaxWidth() // Fill the available width
           )

        // Location details
        Column {

            Text(
                text = "Location",
                fontSize = 20.sp
            )

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "City: ",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(addressData.value?.locality ?: "N/A")
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "State: ",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(addressData.value?.adminArea ?: "N/A")
            }


        }

        // Temperature
        Text(
            text = "Temperature: ${temperatureData.value ?: "33C"}",
            fontSize = 20.sp
        )

        // Air pressure
        Text(
            text = "Pressure: ${pressureData.value ?: "N/A"}",
            fontSize = 20.sp
        )

        //draggable Button

        DraggableText(navController)

    }
}
//End of UI ______________________


// Gesture Playground Button
@Composable
private fun DraggableText(navController: NavHostController) {
    var offsetY by remember { mutableStateOf(0f) }

    var offsetY2 by remember { mutableStateOf(0f) }

    val ogSpot = offsetY
    val ogSpot2 = offsetY2


    Row {


        Box(
            modifier = Modifier
                .offset { IntOffset(0, offsetY.roundToInt()) }
                .draggable(
                    orientation = Orientation.Vertical,
                    state = rememberDraggableState { delta ->
                        offsetY += delta
                    },
                    onDragStopped = {
                        if (offsetY != ogSpot) { // Adjust threshold for triggering navigation
                            Log.d("If case", "Entered If case for drag")
                            navController.navigate("gestureScreen")
                        }
                    }
                )
                .background(color = Color.Cyan)
                .padding(8.dp)
            //.align(Alignment.CenterHorizontally)
        ) {
            Text(
                text = "Gesture Playground",
                modifier = Modifier
                    .padding(8.dp), // Adjust padding for text within the button
                color = Color.Black // Change text color as needed
            )
        }
        

        Box(
            modifier = Modifier
                .offset { IntOffset(0, offsetY2.roundToInt()) }
                .draggable(
                    orientation = Orientation.Vertical,
                    state = rememberDraggableState { delta2 ->
                        offsetY2 += delta2
                    },
                    onDragStopped = {
                        if (offsetY != ogSpot2) { // Adjust threshold for triggering navigation
                            Log.d("If case", "Entered If case for drag to bonus activity")
                            navController.navigate("gestureScreen2")
                        }
                    }
                )
                .background(color = Color.Yellow)
                .padding(8.dp)
            //.align(Alignment.CenterHorizontally)
        ){
            Text(
                text = "Gesture Playground Two",
                modifier = Modifier
                    .padding(8.dp), // Adjust padding for text within the button
                color = Color.Black // Change text color as needed
            )
        }

    }
}



@Composable
fun GestureActivity() {
    var circlePosition by remember { mutableStateOf(Offset(0f, 0f)) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .pointerInput(Unit) {
                detectDragGestures { change, _ ->
                    circlePosition += change.positionChange()
                }
            }
    ) {
        Canvas(
            modifier = Modifier
                .size(150.dp)
                .offset {
                    IntOffset(
                        circlePosition.x.roundToInt(),
                        circlePosition.y.roundToInt()
                    )
                }
        ) {
            drawCircle(Color.Cyan, radius = 60f)
        }

        Text(
            text = "Gesture Playground",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            modifier = Modifier.align(Alignment.Center)
        )
    }
}


//this is for the bonus activity
@Composable
fun GestureActivity2(sensorManager: SensorManager) {

    val accelerometerListener = remember { AccelerometerListener(sensorManager) }

    var circlePosition by accelerometerListener.circlePosition

    DisposableEffect(key1 = accelerometerListener) {
        onDispose {
            accelerometerListener.unregisterListener()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .pointerInput(Unit) {
                detectDragGestures { change, _ ->
                    circlePosition += change.positionChange()
                }
            }
    ) {
        Canvas(
            modifier = Modifier
                .size(150.dp)
                .offset {
                    IntOffset(
                        circlePosition.x.roundToInt(),
                        circlePosition.y.roundToInt()
                    )
                }
        ) {
            drawCircle(Color.Yellow, radius = 60f)
        }

        Text(
            text = "Gesture Playground",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            modifier = Modifier.align(Alignment.Center)
        )
    }
}


//class for using Accelerometer
class AccelerometerListener(private val sensorManager: SensorManager) : SensorEventListener {
    private val gravity = 2f // Adjust gravity as per your requirement
    private val maxMovement = 100f // Maximum movement offset for the circle

    private var offsetX = 0f
    private var offsetY = 0f

    val circlePosition: MutableState<Offset> = mutableStateOf(Offset(0f, 0f))

    init {
        val accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL)
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

    override fun onSensorChanged(event: SensorEvent?) {
        if (event?.sensor?.type == Sensor.TYPE_ACCELEROMETER) {
            val x = event.values[0]
            val y = event.values[1]

            // Mapping accelerometer values to circle movement
            offsetX -= x * maxMovement / gravity
            offsetY += y * maxMovement / gravity

            // Update circle position within bounds
            offsetX = offsetX.coerceIn(-maxMovement, maxMovement)
            offsetY = offsetY.coerceIn(-maxMovement, maxMovement)

            circlePosition.value = Offset(offsetX, offsetY)
        }
    }

    fun unregisterListener() {
        sensorManager.unregisterListener(this)
    }
}
