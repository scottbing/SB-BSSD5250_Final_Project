package edu.nmhu.bssd5250.sb_maps_demo

import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.view.View
import android.view.animation.Animation
import android.view.animation.RotateAnimation
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity

class CompassActivity : AppCompatActivity(), SensorEventListener {
    //
    // taken from: https://www.techrepublic.com/article/pro-tip-create-your-own-magnetic-compass-using-androids-internal-sensors/
    // Tech Republic - Pro Tip: Create Your Own Magnetic Compass Using Android's Internal Sensors
    //
    private var mPointer: ImageView? = null
    private var mSensorManager: SensorManager? = null
    private var mAccelerometer: Sensor? = null
    private var mMagnetometer: Sensor? = null
    private val mLastAccelerometer = FloatArray(3)
    private val mLastMagnetometer = FloatArray(3)
    private var mLastAccelerometerSet = false
    private var mLastMagnetometerSet = false
    private val mR = FloatArray(9)
    private val mOrientation = FloatArray(3)
    private var mCurrentDegree = 0f
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_compass)
        mSensorManager = getSystemService(SENSOR_SERVICE) as SensorManager
        mAccelerometer = mSensorManager!!.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        mMagnetometer = mSensorManager!!.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)
        mPointer = findViewById<View>(R.id.pointer) as ImageView
    }

    override fun onResume() {
        super.onResume()
        mSensorManager!!.registerListener(this, mAccelerometer, SensorManager.SENSOR_DELAY_GAME)
        mSensorManager!!.registerListener(this, mMagnetometer, SensorManager.SENSOR_DELAY_GAME)
    }

    override fun onPause() {
        super.onPause()
        mSensorManager!!.unregisterListener(this, mAccelerometer)
        mSensorManager!!.unregisterListener(this, mMagnetometer)
    }

    override fun onSensorChanged(event: SensorEvent) {
        if (event.sensor == mAccelerometer) {
            System.arraycopy(event.values, 0, mLastAccelerometer, 0, event.values.size)
            mLastAccelerometerSet = true
        } else if (event.sensor == mMagnetometer) {
            System.arraycopy(event.values, 0, mLastMagnetometer, 0, event.values.size)
            mLastMagnetometerSet = true
        }
        if (mLastAccelerometerSet && mLastMagnetometerSet) {
            SensorManager.getRotationMatrix(mR, null, mLastAccelerometer, mLastMagnetometer)
            SensorManager.getOrientation(mR, mOrientation)
            val azimuthInRadians = mOrientation[0]
            val azimuthInDegress =
                (Math.toDegrees(azimuthInRadians.toDouble()) + 360).toFloat() % 360
            val ra = RotateAnimation(
                mCurrentDegree,
                -azimuthInDegress,
                Animation.RELATIVE_TO_SELF, 0.5f,
                Animation.RELATIVE_TO_SELF,
                0.5f
            )
            ra.duration = 250
            ra.fillAfter = true
            mPointer!!.startAnimation(ra)
            mCurrentDegree = -azimuthInDegress
        }
    }

    override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {}
}