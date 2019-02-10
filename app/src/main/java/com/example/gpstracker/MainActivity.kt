package com.example.gpstracker

import android.Manifest
import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.FrameLayout
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import com.flipboard.bottomsheet.BottomSheetLayout
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.settings.*
import java.lang.Exception

class MainActivity : AppCompatActivity() {

    private lateinit var locationManager: LocationManager
    private lateinit var listener: LocationListener
    private var listenerEnabled = false
    private var newMarker: LatLng = LatLng(0.0, 0.0)
    var myMap: GoogleMap? = null
        private set(value) { field = value }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        navBottom.setOnNavigationItemSelectedListener{ navigate(it.itemId) }
        navigate(R.id.navItemNotices)
    }


    fun tryStartTrackerService() {

        if(ContextCompat.checkSelfPermission(applicationContext, Manifest.permission.SYSTEM_ALERT_WINDOW) == PackageManager.PERMISSION_GRANTED
            && ContextCompat.checkSelfPermission(applicationContext, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
        ) {
            Log.d("current", "has permissions")
            startService(Intent(this, MyService::class.java))
        } else {
            Log.d("current", "hasn't permissions")
            //if(!ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.SYSTEM_ALERT_WINDOW))
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.SYSTEM_ALERT_WINDOW), 10)
        }
    }

    fun tryStopTrackerService() {
        stopService(Intent(this, MyService::class.java))
    }


    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if(ContextCompat.checkSelfPermission(baseContext, Manifest.permission.SYSTEM_ALERT_WINDOW) == PackageManager.PERMISSION_GRANTED
            && ContextCompat.checkSelfPermission(applicationContext, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
        )
            startService(Intent(this, MyService::class.java))
    }


    fun navigate(itemId: Int): Boolean {
        //Log.d("current", "navigate to $itemId")
        val fragment: Fragment =
            when(itemId) {
                //R.id.navItemMap -> fragment = SettingsFragment()  // default (init before)
                R.id.navItemNotices -> NoticeListFragment()
                R.id.navItemAddNotice -> NoticeAddFragment()
                R.id.navItemSettings -> SettingsFragment()
                else -> SupportMapFragment()
            }
        val transaction = supportFragmentManager.beginTransaction()
        transaction.replace(R.id.wrapFragment, fragment)
        transaction.commit()
        if(fragment is SupportMapFragment) {
            fragment.getMapAsync{ onMapReady(it) }
        }
        return true
    }


    private fun onMapReady(mMap: GoogleMap) {

        Log.d("current", "onMapReady")
        myMap = mMap
        val places = DataManager(this).places
        if(places != null) {
            for (place in places)
                mMap.addMarker(MarkerOptions().position(LatLng(place.x, place.y)).title(place.name))
        }

        mMap.setOnMapClickListener {
            newMarker = it
            try {
                PlaceInputFragment().setPoint(it).show(supportFragmentManager, "placeName")
            } catch(e: Exception) {
                Log.d("current", e.toString())
            }
        }
    }
}

