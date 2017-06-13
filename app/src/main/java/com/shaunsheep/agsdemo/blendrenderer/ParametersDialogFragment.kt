package com.shaunsheep.agsdemo.blendrenderer

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.support.v7.app.AlertDialog
import android.view.LayoutInflater
import android.view.View
import android.widget.*
import com.esri.arcgisruntime.raster.ColorRamp
import com.esri.arcgisruntime.raster.SlopeType
import com.shaunsheep.agsdemo.R


/**
 * Class which handles the blend renderer parameters dialog.
 * Created by jiang on 2017/6/13.
 */
class ParametersDialogFragment : DialogFragment() {
    private var mAltitude: Int = 0
    private var mAzimuth: Int = 0
    private var mSlopeType: SlopeType? = null
    private var mColorRampType: ColorRamp.PresetType? = null

    private var mCurrAltitudeTextView: TextView? = null
    private var mCurrAzimuthTextView: TextView? = null

    /**
     * Builds parameter dialog with values pulled through from BlendRendererActivity.

     * @param savedInstanceState
     * *
     * @return create parameter dialog box
     */
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {

        val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

        val blendParameters = arguments
        if (blendParameters != null) {
            mAltitude = blendParameters.getInt("altitude")
            mAzimuth = blendParameters.getInt("azimuth")
            mSlopeType = blendParameters.getSerializable("slope_type") as SlopeType
            mColorRampType = blendParameters.getSerializable("color_ramp_type") as ColorRamp.PresetType
        }

        val paramDialog = AlertDialog.Builder(context)
        @SuppressLint("InflateParams") val dialogView = inflater.inflate(R.layout.blend_renderer_dialog_box, null)
        paramDialog.setView(dialogView)
        paramDialog.setTitle(R.string.dialog_title)
        paramDialog.setNegativeButton("Cancel", DialogInterface.OnClickListener { dialog, which -> dismiss() })
        paramDialog.setPositiveButton("Render", DialogInterface.OnClickListener { dialog, which ->
            dialog.dismiss()
            val activity = activity as ParametersListener
            activity.returnParameters(mAltitude, mAzimuth, mSlopeType!!, mColorRampType!!)
        })

        mCurrAltitudeTextView = dialogView.findViewById(R.id.curr_altitude_text) as TextView
        val altitudeSeekBar: SeekBar = dialogView.findViewById(R.id.altitude_seek_bar) as SeekBar
        altitudeSeekBar.max = 90 //altitude is restricted to 0 - 90
        //set initial altitude value
        updateAltitudeSeekBar(altitudeSeekBar)
        altitudeSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                mAltitude = progress
                updateAltitudeSeekBar(seekBar)
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {}

            override fun onStopTrackingTouch(seekBar: SeekBar) {}
        })

        mCurrAzimuthTextView = dialogView.findViewById(R.id.curr_azimuth_text) as TextView
        val azimuthSeekBar: SeekBar = dialogView.findViewById(R.id.azimuth_seek_bar) as SeekBar
        azimuthSeekBar.max = 360 //azimuth measured in degrees 0 - 360
        //set initial azimuth value
        updateAzimuthSeekBar(azimuthSeekBar)
        azimuthSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                mAzimuth = progress
                updateAzimuthSeekBar(seekBar)
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {}

            override fun onStopTrackingTouch(seekBar: SeekBar) {}
        })

        val slopeTypeArray = ArrayList<String>()
        slopeTypeArray.add("None")    //ordinals:0
        slopeTypeArray.add("Degree")           //1
        slopeTypeArray.add("Percent rise")     //2
        slopeTypeArray.add("Scaled")           //3

        val slopeTypeSpinnerAdapter = ArrayAdapter(
                context,
                R.layout.blend_renderer_spinner_text_view,
                slopeTypeArray)

        val slopeTypeSpinner: Spinner = dialogView.findViewById(R.id.slope_type_spinner) as Spinner
        slopeTypeSpinner.adapter = slopeTypeSpinnerAdapter
        slopeTypeSpinner.setSelection(mSlopeType!!.ordinal)
        slopeTypeSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long) {
                when (position) {
                    0 -> mSlopeType = SlopeType.NONE
                    1 -> mSlopeType = SlopeType.DEGREE
                    2 -> mSlopeType = SlopeType.PERCENT_RISE
                    3 -> mSlopeType = SlopeType.SCALED
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }

        val colorRampTypeArray = ArrayList<String>()
        colorRampTypeArray.add("None")    //ordinals:0
        colorRampTypeArray.add("Elevation")        //1
        colorRampTypeArray.add("DEM screen")       //2
        colorRampTypeArray.add("DEM light")        //3

        val colorRampSpinnerAdapter = ArrayAdapter(
                context,
                R.layout.blend_renderer_spinner_text_view,
                colorRampTypeArray)

        val colorRampSpinner: Spinner = dialogView.findViewById(R.id.color_ramp_spinner) as Spinner
        colorRampSpinner.adapter = colorRampSpinnerAdapter
        colorRampSpinner.setSelection(mColorRampType!!.ordinal)
        colorRampSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long) {
                when (position) {
                    0 -> mColorRampType = ColorRamp.PresetType.NONE
                    1 -> mColorRampType = ColorRamp.PresetType.ELEVATION
                    2 -> mColorRampType = ColorRamp.PresetType.DEM_SCREEN
                    3 -> mColorRampType = ColorRamp.PresetType.DEM_LIGHT
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }

        return paramDialog.create()
    }

    @SuppressLint("SetTextI18n")
    private fun updateAltitudeSeekBar(altitudeSeekBar: SeekBar) {
        altitudeSeekBar.progress = mAltitude
        mCurrAltitudeTextView!!.text = mAltitude.toString()
    }

    @SuppressLint("SetTextI18n")
    private fun updateAzimuthSeekBar(azimuthSeekBar: SeekBar) {
        azimuthSeekBar.progress = mAzimuth
        mCurrAzimuthTextView!!.text = mAzimuth.toString()
    }

    /**
     * Interface for passing dialog parameters back to BlendRendererActivity.
     */
    interface ParametersListener {
        fun returnParameters(altitude: Int, azimuth: Int, slopeType: SlopeType, colorRampType: ColorRamp.PresetType)
    }
}