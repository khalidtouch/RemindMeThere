package com.gigaxysafe.remindmethere

import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.SeekBar
import com.gigaxysafe.remindmethere.databinding.ActivityNewReminderBinding
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.material.snackbar.Snackbar
import kotlin.math.roundToInt

class NewReminderActivity : BaseActivity(), OnMapReadyCallback {

    private lateinit var map: GoogleMap
    private lateinit var binding: ActivityNewReminderBinding

    private var reminder = Reminder(latLng = null, radius = null, message = null)

    private val radiusBarChangeListener = object : SeekBar.OnSeekBarChangeListener {
        override fun onStartTrackingTouch(seekBar: SeekBar?) {}

        override fun onStopTrackingTouch(seekBar: SeekBar?) {}

        override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
            updateRadiusWithProgress(progress)

            showReminderUpdate()
        }
    }

    private fun updateRadiusWithProgress(progress: Int) {
        val radius = getRadius(progress)
        reminder.radius = radius
        binding.radiusDescription.text =
            getString(R.string.radius_description, radius.roundToInt().toString())
    }

    companion object {
        private const val EXTRA_LAT_LNG = "EXTRA_LAT_LNG"
        private const val EXTRA_ZOOM = "EXTRA_ZOOM"

        fun newIntent(context: Context, latLng: LatLng, zoom: Float): Intent {
            val intent = Intent(context, NewReminderActivity::class.java)
            intent
                .putExtra(EXTRA_LAT_LNG, latLng)
                .putExtra(EXTRA_ZOOM, zoom)
            return intent
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNewReminderBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        binding.instructionTitle.visibility = View.GONE
        binding.instructionSubtitle.visibility = View.GONE
        binding.radiusBar.visibility = View.GONE
        binding.radiusDescription.visibility = View.GONE
        binding.message.visibility = View.GONE

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }


    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }

    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap
        map.uiSettings.isMapToolbarEnabled = false

        centerCamera()

        showConfigureLocationStep()
    }

    private fun centerCamera() {
        val latLng = intent.extras?.get(EXTRA_LAT_LNG) as LatLng
        val zoom = intent.extras?.get(EXTRA_ZOOM) as Float
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, zoom))
    }

    private fun showConfigureLocationStep() {
        binding.marker.visibility = View.VISIBLE
        binding.instructionTitle.visibility = View.VISIBLE
        binding.instructionSubtitle.visibility = View.VISIBLE
        binding.radiusBar.visibility = View.GONE
        binding.radiusDescription.visibility = View.GONE
        binding.message.visibility = View.GONE
        binding.instructionTitle.text = getString(R.string.instruction_where_description)
        binding.next.setOnClickListener {
            reminder.latLng = map.cameraPosition.target
            showConfigureRadiusStep()
        }

        showReminderUpdate()
    }

    private fun showConfigureRadiusStep() {
        binding.marker.visibility = View.GONE
        binding.instructionTitle.visibility = View.VISIBLE
        binding.instructionSubtitle.visibility = View.GONE
        binding.radiusBar.visibility = View.VISIBLE
        binding.radiusDescription.visibility = View.VISIBLE
        binding.message.visibility = View.GONE
        binding.instructionTitle.text = getString(R.string.instruction_radius_description)
        binding.next.setOnClickListener {
            showConfigureMessageStep()
        }
        binding.radiusBar.setOnSeekBarChangeListener(radiusBarChangeListener)
        updateRadiusWithProgress(binding.radiusBar.progress)

        map.animateCamera(CameraUpdateFactory.zoomTo(15f))

        showReminderUpdate()
    }

    private fun getRadius(progress: Int) = 100 + (2 * progress.toDouble() + 1) * 100

    private fun showConfigureMessageStep() {
        binding.marker.visibility = View.GONE
        binding.instructionTitle.visibility = View.VISIBLE
        binding.instructionSubtitle.visibility = View.GONE
        binding.radiusBar.visibility = View.GONE
        binding.radiusDescription.visibility = View.GONE
        binding.message.visibility = View.VISIBLE
        binding.instructionTitle.text = getString(R.string.instruction_message_description)
        binding.next.setOnClickListener {
            hideKeyboard(this, binding.message)

            reminder.message = binding.message.text.toString()

            if (reminder.message.isNullOrEmpty()) {
                binding.message.error = getString(R.string.error_required)
            } else {
                addReminder(reminder)
            }
        }
        binding.message.requestFocusWithKeyboard()

        showReminderUpdate()
    }

    private fun addReminder(reminder: Reminder) {
        getRepository().add(reminder,
            success = {
                setResult(Activity.RESULT_OK)
                finish()
            },
            failure = {
                Snackbar.make(binding.main, it, Snackbar.LENGTH_LONG).show()

            })
    }

    private fun showReminderUpdate() {
        map.clear()
        showReminderInMap(this, map, reminder)
    }
}