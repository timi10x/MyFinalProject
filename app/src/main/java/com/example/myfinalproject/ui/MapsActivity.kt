package com.example.myfinalproject.ui

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.core.app.ActivityCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.example.myfinalproject.R
import com.example.myfinalproject.adapter.BookmarkInfoAdapter
import com.example.myfinalproject.viewmodel.MapsViewModel
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import timber.log.Timber
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.PhotoMetadata
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.net.FetchPhotoRequest
import com.google.android.libraries.places.api.net.FetchPlaceRequest
import com.google.android.libraries.places.api.net.PlacesClient
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class MapsActivity : AppCompatActivity(), OnMapReadyCallback {
    private lateinit var map: GoogleMap
    private lateinit var placesClient: PlacesClient
    private lateinit var mapsViewModel: MapsViewModel
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)

        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        setupLocationClient()
        setupPlacesClient()
        setupViewModel()
    }

    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap
        setupMapListeners()
        setupViewModel()
        createBookmarkMarkerObserver()
        getCurrentLocation()
    }

    private fun setupMapListeners() {
        map.setInfoWindowAdapter(BookmarkInfoAdapter(this))
        map.setOnPoiClickListener {
            displayPoi(it)
        }
        map.setOnInfoWindowClickListener {
            handleInfoWindowClick(it)
        }
    }


    private fun setupViewModel() {
        mapsViewModel = ViewModelProviders.of(this).get(MapsViewModel::class.java)
    }


    private fun setupPlacesClient() {
        Places.initialize(applicationContext, "AIzaSyDNmKdyC-UHu-XChS21GQsjxqgyIytXhyY");
        placesClient = Places.createClient(this);
    }


    private fun displayPoi(pointOfInterest: PointOfInterest) {
        displayPoiGetPlaceStep(pointOfInterest)
    }

    private fun displayPoiGetPlaceStep(
        pointOfInterest:
        PointOfInterest
    ) {
        val placeId = pointOfInterest.placeId

        val placeFields = listOf(
            Place.Field.ID,
            Place.Field.NAME,
            Place.Field.PHONE_NUMBER,
            Place.Field.PHOTO_METADATAS,
            Place.Field.ADDRESS,
            Place.Field.LAT_LNG
        )

        val request = FetchPlaceRequest
            .builder(placeId, placeFields)
            .build()

        placesClient.fetchPlace(request)
            .addOnSuccessListener { response ->
                val place = response.place
                displayPoiGetPhotoStep(place)
            }.addOnFailureListener { exception ->
                if (exception is ApiException) {
                    val statusCode = exception.statusCode
                    Timber.tag(TAG).e(
                        "Place not found: " +
                                exception.message + ", " +
                                "statusCode: " + statusCode
                    )
                }
            }
    }

    private fun displayPoiGetPhotoStep(place: Place) {
        val photoMetadata = place.getPhotoMetadatas()?.get(0)
        if (photoMetadata == null) {
            displayPoiDisplayStep(place, null)
            return
        }
        val photoRequest = FetchPhotoRequest.builder(photoMetadata as PhotoMetadata)
            .setMaxWidth(resources.getDimensionPixelSize(R.dimen.default_image_width))
            .setMaxHeight(resources.getDimensionPixelSize(R.dimen.default_image_height))
            .build()
        placesClient.fetchPhoto(photoRequest).addOnSuccessListener { fetchPhotoResponse ->
            val bitmap = fetchPhotoResponse.bitmap
            displayPoiDisplayStep(place, bitmap)
        }.addOnFailureListener { exception ->
            if (exception is ApiException) {
                val statusCode = exception.statusCode
                Timber.tag(TAG).e(
                    "Place not found: " +
                            exception.message + ", " +
                            "statusCode: " + statusCode
                )
            }
        }
    }

    private fun displayPoiDisplayStep(place: Place, photo: Bitmap?) {
        val marker = map.addMarker(
            MarkerOptions()
                .position(place.latLng as LatLng)
                .title(place.name)
                .snippet(place.phoneNumber)
        )
        marker?.tag = PlaceInfo(place, photo)
        marker?.showInfoWindow()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        if (requestCode == REQUEST_LOCATION) {
            if (grantResults.size == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getCurrentLocation()
            } else {
                Timber.tag(TAG).e("Location permission denied")
            }
        }
    }

    //listening to changes form the viewModel
    //this is also the helper method that adds a single blue marker to the map based on a BookmarkMarkerView.
    private fun addPlaceMarker(
        bookmark: MapsViewModel.BookmarkMarkerView
    ): Marker?{
        val marker = map.addMarker(MarkerOptions()
            .position
        (bookmark.location)
            .title(bookmark.name)
            .icon(BitmapDescriptorFactory.defaultMarker(
                BitmapDescriptorFactory.HUE_AZURE
            ))
            .snippet(bookmark.phone)
            .alpha(0.8f))
        marker.tag = bookmark
        return marker
    }

    //displaying all og the bookmark markers
    private fun displayAllBookmarks(
        bookmarks: List<MapsViewModel.BookmarkMarkerView>
    ){
        for (bookmark in bookmarks){
            addPlaceMarker(bookmark)
        }
    }

    //I want to observe all the changes to the BookmarkMarkerView objects for the VM
    private fun createBookmarkMarkerObserver(){
        mapsViewModel.getBookmarkMarkerViews()?.observe(
            this, Observer {
                map.clear()
                it?.let {
                    displayAllBookmarks(it)
                }
            }
        )
    }

    private fun setupLocationClient() {
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
    }

    private fun getCurrentLocation() {

        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) !=
            PackageManager.PERMISSION_GRANTED
        ) {
            requestLocationPermissions()
        } else {
            map.isMyLocationEnabled = true

            fusedLocationClient.lastLocation.addOnCompleteListener {
                val location = it.result
                if (location != null) {
                    val latLng = LatLng(location.latitude, location.longitude)
                    val update = CameraUpdateFactory.newLatLngZoom(latLng, 16.0f)
                    map.moveCamera(update)
                } else {
                    Timber.tag(TAG).e("No location found")
                }
            }
        }
    }

    private fun requestLocationPermissions() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
            REQUEST_LOCATION
        )
    }

    private fun handleInfoWindowClick(marker: Marker) {
        when(marker.tag){
            is PlaceInfo ->{
                val placeInfo = (marker.tag as PlaceInfo)
                if (placeInfo.place != null) {
                    GlobalScope.launch {
                        mapsViewModel.addBookmarkFomPlace(placeInfo.place, placeInfo.image)
                    }
                }
                marker.remove()
            }
            is MapsViewModel.BookmarkMarkerView ->{
                val bookMarkerView = (marker.tag as MapsViewModel.BookmarkMarkerView)
                marker.hideInfoWindow()
                bookMarkerView.id?.let {
                    startBookmarkDetails(it)
                }
            }
        }

    }

    companion object {
        private const val REQUEST_LOCATION = 1
        private const val TAG = "MapsActivity"
        const val EXTRA_BOOKMARK_ID = "com.example.myfinalproject.ui.EXTRA_BOOKMARK_ID"
    }

    private fun startBookmarkDetails(bookmarkId: Long){
        val intent = Intent(this, BookmarkDetailsActivity::class.java)
        startActivity(intent)
    }

    class PlaceInfo(val place: Place? = null, val image: Bitmap? = null)
}
