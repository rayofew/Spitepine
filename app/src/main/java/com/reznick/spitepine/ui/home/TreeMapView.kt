package com.reznick.spitepine.ui.home

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState
import com.reznick.spitepine.data.model.Tree
import com.reznick.spitepine.data.model.TreeStatus
import com.reznick.spitepine.ui.components.label
import com.reznick.spitepine.util.LocationHelper

@Composable
fun TreeMapView(
    trees: List<Tree>,
    padding: PaddingValues,
    onTreeClick: (Tree) -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val cameraPositionState = rememberCameraPositionState {
        // Initial position: a wide US-centered view until we get something better.
        position = CameraPosition.fromLatLngZoom(LatLng(39.0, -98.0), 3f)
    }

    // Re-center on the first real tree as soon as one is available.
    LaunchedEffect(trees.isNotEmpty()) {
        trees.firstOrNull()?.let { tree ->
            cameraPositionState.position = CameraPosition.fromLatLngZoom(
                LatLng(tree.location.latitude, tree.location.longitude),
                15f,
            )
        }
    }

    val showMyLocation = LocationHelper.hasFineLocationPermission(context)

    GoogleMap(
        modifier = modifier
            .fillMaxSize()
            .padding(padding),
        cameraPositionState = cameraPositionState,
        properties = MapProperties(isMyLocationEnabled = showMyLocation),
        uiSettings = MapUiSettings(
            myLocationButtonEnabled = showMyLocation,
            zoomControlsEnabled = false,
            mapToolbarEnabled = true,
        ),
    ) {
        trees.forEach { tree ->
            Marker(
                state = MarkerState(
                    position = LatLng(tree.location.latitude, tree.location.longitude),
                ),
                title = tree.address.ifBlank { "Tree ${tree.id.takeLast(6)}" },
                snippet = tree.status.label(),
                icon = BitmapDescriptorFactory.defaultMarker(tree.status.markerHue()),
                onInfoWindowClick = { onTreeClick(tree) },
            )
        }
    }
}

// Pin colors per spec §4.1, mapped to BitmapDescriptorFactory's predefined hues.
// SPOTTED's "gray" doesn't have a default-marker equivalent; use violet as the
// closest neutral until we ship custom marker bitmaps.
private fun TreeStatus.markerHue(): Float = when (this) {
    TreeStatus.SPOTTED -> BitmapDescriptorFactory.HUE_VIOLET
    TreeStatus.ASKED -> BitmapDescriptorFactory.HUE_YELLOW
    TreeStatus.GRANTED -> BitmapDescriptorFactory.HUE_GREEN
    TreeStatus.DENIED -> BitmapDescriptorFactory.HUE_RED
    TreeStatus.EXPIRED -> BitmapDescriptorFactory.HUE_ORANGE
    TreeStatus.HARVESTED_RECENTLY -> BitmapDescriptorFactory.HUE_BLUE
}
