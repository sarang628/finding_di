package com.sarang.torang.di.finding_di

import android.Manifest
import android.annotation.SuppressLint
import android.location.Location
import androidx.annotation.RequiresPermission
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

@SuppressLint("MissingPermission")
@Composable
fun CurrentLocationScreen(onLocation: (Location) -> Unit = {}, contents : @Composable (()->Unit)->Unit = {}) {
    val permissions = listOf(Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION,)
    PermissionBox(
        permissions = permissions, requiredPermissions = listOf(permissions.first()),
        onGranted = {
            CurrentLocationContent(
                usePreciseLocation = it.contains(Manifest.permission.ACCESS_FINE_LOCATION),
                onLocation = onLocation,
                contents = contents
            )
        },
        contents = contents
    )
}

@RequiresPermission(anyOf = [Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION])
@Composable
fun CurrentLocationContent(usePreciseLocation: Boolean, onLocation: (Location) -> Unit, contents : @Composable (() -> Unit)->Unit = {}) {
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val locationClient = remember { LocationServices.getFusedLocationProviderClient(context) }

    contents.invoke {
        scope.launch(Dispatchers.IO) {
            val priority = if (usePreciseLocation) { Priority.PRIORITY_HIGH_ACCURACY } else { Priority.PRIORITY_BALANCED_POWER_ACCURACY }
            val result = locationClient.getCurrentLocation(priority, CancellationTokenSource().token,).await()
            result?.let { fetchedLocation -> onLocation.invoke(fetchedLocation) } }
    }
}