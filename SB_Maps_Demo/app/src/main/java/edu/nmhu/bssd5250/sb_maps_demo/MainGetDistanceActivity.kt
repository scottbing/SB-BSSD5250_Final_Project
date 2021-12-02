/* Copyright 2020 Esri
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package edu.nmhu.bssd5250.sb_maps_demo

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.content.Intent
import android.graphics.Color
import android.util.Log
import android.widget.Button
import android.widget.EditText
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.constraintlayout.widget.ConstraintLayout
import com.esri.arcgisruntime.ArcGISRuntimeEnvironment

class MainGetDistanceActivity : AppCompatActivity() {

    private var orig: EditText? = null

    companion object{
        const val ORIG_RESULT:String = "edu.nmhu.bssd5250.sb_maps_demo.ORIG_RESULT"
    }

    private val startForResult =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
                result: ActivityResult ->
            if (result.resultCode == RESULT_OK) {
                val intent = result.data
                orig?.setText(intent?.getStringExtra(ORIG_RESULT).toString())
                // Handle the Intent
                Log.i("MACTResult", intent?.getStringExtra(ORIG_RESULT).toString())
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main_get_distance)

        /*// authentication with an API key or named user is required to access basemaps and other
        // location services
        //ArcGISRuntimeEnvironment.setApiKey(BuildConfig.API_KEY)
        ArcGISRuntimeEnvironment.setApiKey("AAPKb69f88fcdbf341cc8964d433816035740MNbue4c-nw7PVHJ2snWYDRjr2sVt5s7viIvViEqMfPE4jtepjiqpiUxDPDaazC2")*/

        orig = EditText(this).apply {
            hint = "Enter a Starting Location"
            //setText("11111 Eagle Rock Ave NE, Albququerque, NM 87122")
        }
        val submitButton = Button(this).apply {
            "Submit".also { text = it }
            setOnClickListener {
                val passableData = Intent(applicationContext, GetDistanceActivity::class.java).apply {
                    putExtra(GetDistanceActivity.ORIG_REQUESTED, "#"+ orig!!.text.toString())
                }
                startForResult.launch(passableData)
            }
        }

        val linearLayout = LinearLayoutCompat(this).apply {
            layoutParams = LinearLayoutCompat.LayoutParams(
                LinearLayoutCompat.LayoutParams.MATCH_PARENT,
                LinearLayoutCompat.LayoutParams.MATCH_PARENT)
            orientation = LinearLayoutCompat.VERTICAL
            addView(orig)
            addView(submitButton)
        }

        //look up the main layout by the id we just gave it
        findViewById<ConstraintLayout>(R.id.main_layout).apply {
            addView(linearLayout)
        }
    }
}