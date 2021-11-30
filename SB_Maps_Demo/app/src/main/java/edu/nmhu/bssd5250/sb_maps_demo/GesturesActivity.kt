package edu.nmhu.bssd5250.sb_maps_demo

import android.graphics.Matrix
import android.os.Bundle
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import android.view.ScaleGestureDetector.SimpleOnScaleGestureListener
import android.view.View
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity

class GesturesActivity : AppCompatActivity() {
    private var img: ImageView? = null
    private val matrix = Matrix()
    private var scale = 1f
    private var detector: ScaleGestureDetector? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_gestures)
        img = findViewById<View>(R.id.imageview) as ImageView
        detector = ScaleGestureDetector(this@GesturesActivity, ScaleListener())
    }

    private inner class ScaleListener : SimpleOnScaleGestureListener() {
        override fun onScale(detector: ScaleGestureDetector): Boolean {
            scale *= detector.scaleFactor
            scale = 0.1f.coerceAtLeast(scale.coerceAtMost(5.0f))
            matrix.setScale(scale, scale)
            img!!.imageMatrix = matrix
            return true
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        detector!!.onTouchEvent(event)
        return true
    }
}