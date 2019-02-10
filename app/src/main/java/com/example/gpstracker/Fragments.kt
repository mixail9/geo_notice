package com.example.gpstracker

import android.app.Activity
import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ListAdapter
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.ListFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.snackbar.Snackbar


class SettingsFragment: Fragment() {

    private var listenerEnabled: Boolean = false

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val fragment = inflater.inflate(R.layout.settings, container, false)

        fragment.findViewById<Button>(R.id.btnStart).setOnClickListener {
            if(!listenerEnabled) {
                Log.d("current", "btnStart " + listenerEnabled)
                try {
                    (activity as MainActivity).apply {
                        tryStartTrackerService()
                    }

                    listenerEnabled = true
                } catch (e: SecurityException) {
                    Log.d("current", e.toString())
                }
            }

        }

        fragment.findViewById<Button>(R.id.btnStop).setOnClickListener {
            if(listenerEnabled) {
                Log.d("current", "btnStop " + listenerEnabled)
                (activity as MainActivity).tryStopTrackerService()
                listenerEnabled = false
                //locationManager.removeUpdates(listener)
            }
        }

        fragment.findViewById<Button>(R.id.btnClearData).setOnClickListener {
            DataManager(this.activity!!.baseContext).deleteAll()
        }
        return fragment
    }
}


class PlaceInputFragment: DialogFragment() {

    private var point: LatLng? = null

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return activity?.let {
            val dialogBuilder = AlertDialog.Builder(it)
            dialogBuilder.setTitle(resources.getString(R.string.add_place_dialog_title))
            val view = (activity as FragmentActivity).layoutInflater.inflate(R.layout.place_input, null)
            dialogBuilder.setView(view)
            dialogBuilder.setPositiveButton(resources.getString(R.string.btnAdd), object: DialogInterface.OnClickListener{
                override fun onClick(dialog: DialogInterface?, which: Int) {
                    if(point == null) {
                        Snackbar.make(view, resources.getString(R.string.err_try_add_empty_point), Snackbar.LENGTH_LONG).show()
                        return
                    }

                    val name = view.findViewById<EditText>(R.id.placeName).text.toString()
                    (activity as MainActivity).myMap?.addMarker(MarkerOptions().position(point!!).title(name))
                    DataManager(activity!!).addPlace(Place(name, point!!.latitude, point!!.longitude))
                }
            })
            dialogBuilder.setNegativeButton(resources.getString(R.string.btnClose), object: DialogInterface.OnClickListener{
                override fun onClick(dialog: DialogInterface?, which: Int) {
                }
            })
            dialogBuilder.create()
        } ?: throw IllegalStateException("Activity cannot be null")
    }

    fun setPoint(point: LatLng): PlaceInputFragment {
        this.point = point
        return this
    }
}



class NoticeListFragment: ListFragment() {

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        listAdapter = ArrayAdapter<Notice>(activity!!, R.layout.notification_list_item, DataManager(activity as Activity).notices!!)
    }

}


class NoticeAddFragment: Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        if(activity == null)
            return null
        val fragment = inflater.inflate(R.layout.notification_input, container, false)
        val inputField = fragment.findViewById<EditText>(R.id.noticeName)
        fragment.findViewById<Button>(R.id.btnAddNotice).setOnClickListener {
            if(inputField.text.toString().isEmpty() || inputField.text.toString().isEmpty())
                Snackbar.make(fragment, resources.getString(R.string.err_notice_name_empty), Snackbar.LENGTH_SHORT).show()
            else {
                DataManager(activity!!).addNotice(Notice(inputField.text.toString()))
                Snackbar.make(fragment, resources.getString(R.string.add_notice_success), Snackbar.LENGTH_SHORT).show()
            }
        }
        return fragment
    }
}