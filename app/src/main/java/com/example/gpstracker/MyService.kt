package com.example.gpstracker

import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.*
import android.util.Log
import android.view.Gravity
import android.view.WindowManager
import android.widget.ImageView
import java.util.*
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.*


class MyService(): Service() {


    private lateinit var winManager: WindowManager
    private lateinit var img: ImageView
    private val timer = Timer()

    private val timerTask = object: TimerTask() {
        override fun run() {
            Log.d("current", "step timer")
            handler.sendEmptyMessage(1)
        }
    }

    private var isVisible = false
    private var counter: Int = 0


    private val handler = object: Handler(Looper.getMainLooper()) {
        override fun handleMessage(msg: Message?) {
            Log.d("current", "handleMessage")
            createImg()
        }
    }


    override fun onCreate() {
        super.onCreate()
        img = ImageView(this)
        winManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager
        Log.d("current", "onCreate service")

        initTracker()
    }



    fun createImg() {

        Log.d("current", "createImg $isVisible")
        if(isVisible)
            return

        img.setOnClickListener {
            winManager.removeViewImmediate(img)
            isVisible = false
        }

        val paint = Paint()
        val icon = Bitmap.createBitmap(150, 150, Bitmap.Config.RGB_565)
        val canvas = Canvas(icon)

        paint.color = Color.GRAY
        canvas.drawRect(0f, 0f, 150f, 150f, paint)
        paint.color = Color.RED
        paint.textSize = 60f
        canvas.drawText((counter++).toString(), 30f, 30f, paint)
        img.setImageBitmap(icon)

        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.TYPE_PHONE,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        )
        params.apply {
            x = 100
            y = 100
            gravity = Gravity.BOTTOM and Gravity.END
        }

        winManager.addView(img, params)
        isVisible = true
    }



    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onDestroy() {
        timer.cancel()
        timerTask.cancel()
        try {
            winManager.removeView(img)
        } catch(e: Exception) {}  //  img not showed
        super.onDestroy()
    }



    private fun initTracker() {
        val locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        val listener = object: LocationListener {
            override fun onLocationChanged(location: Location?) {
                Log.d("current", "onLocationChanged " + location)
                if(location == null)
                    return
                createImg()
            }

            override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {

                Log.d("current", "onStatusChanged")
            }

            override fun onProviderEnabled(provider: String?) {

                Log.d("current", "onProviderEnabled")
            }

            override fun onProviderDisabled(provider: String?) {

                Log.d("current", "onProviderDisabled")
            }
        }
        try {
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 2000, 0f, listener)
        } catch(e: SecurityException) {}

    }
}