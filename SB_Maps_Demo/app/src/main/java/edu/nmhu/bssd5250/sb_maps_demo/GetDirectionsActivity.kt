/* Copyright 2017 Esri
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

import android.app.ProgressDialog
import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.graphics.Color
import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.*
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.esri.arcgisruntime.geometry.Point
import com.esri.arcgisruntime.geometry.SpatialReferences
import com.esri.arcgisruntime.layers.ArcGISVectorTiledLayer
import com.esri.arcgisruntime.mapping.ArcGISMap
import com.esri.arcgisruntime.mapping.Basemap
import com.esri.arcgisruntime.mapping.Viewpoint
import com.esri.arcgisruntime.mapping.view.Graphic
import com.esri.arcgisruntime.mapping.view.GraphicsOverlay
import com.esri.arcgisruntime.mapping.view.MapView
import com.esri.arcgisruntime.symbology.PictureMarkerSymbol
import com.esri.arcgisruntime.symbology.SimpleLineSymbol
import com.esri.arcgisruntime.tasks.networkanalysis.Route
import com.esri.arcgisruntime.tasks.networkanalysis.RouteParameters
import com.esri.arcgisruntime.tasks.networkanalysis.RouteTask
import com.esri.arcgisruntime.tasks.networkanalysis.Stop
import com.google.android.material.floatingactionbutton.FloatingActionButton
import java.util.*
import java.util.concurrent.ExecutionException


class GetDirectionsActivity : AppCompatActivity() {
    private var mProgressDialog: ProgressDialog? = null
    private var mMapView: MapView? = null
    private var mRouteTask: RouteTask? = null
    private var mRouteParams: RouteParameters? = null
    private var mRoute: Route? = null
    private var mRouteSymbol: SimpleLineSymbol? = null
    private var mGraphicsOverlay: GraphicsOverlay? = null
    private var mDrawerLayout: DrawerLayout? = null
    private var mDrawerList: ListView? = null
    private var mDrawerToggle: ActionBarDrawerToggle? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.directions_drawer)

        // inflate MapView from layout
        mMapView = findViewById(R.id.mapView)
        // create new Vector Tiled Layer from service url
        val mVectorTiledLayer = ArcGISVectorTiledLayer(getString(R.string.navigation_vector))

        // set tiled layer as basemap
        val basemap = Basemap(mVectorTiledLayer)
        // create a map with the basemap
        val map = ArcGISMap(basemap)
        // set the map to be displayed in this view
        mMapView?.setMap(map)
        // create a viewpoint from lat, long, scale
        // val sanDiegoPoint = Viewpoint(32.7157, -117.1611, 200000)
        // set initial map extent
        mMapView?.setViewpoint(Viewpoint(32.7157, -117.1611, 200000.0))

        // inflate navigation drawer
        mDrawerLayout = findViewById(R.id.drawer_layout)
        mDrawerList = findViewById(R.id.left_drawer)
        val mDirectionFab = findViewById<FloatingActionButton>(R.id.directionFAB)

        // update UI when attribution view changes
        val params = mDirectionFab.layoutParams as FrameLayout.LayoutParams
        mMapView?.addAttributionViewLayoutChangeListener { _: View?, _: Int, _: Int, _: Int, bottom: Int, _: Int, _: Int, _: Int, oldBottom: Int ->
            val heightDelta = bottom - oldBottom
            params.bottomMargin += heightDelta
        }
        setupDrawer()
        setupSymbols()
        mProgressDialog = ProgressDialog(this)
        mProgressDialog!!.setTitle(getString(R.string.progress_title))
        mProgressDialog!!.setMessage(getString(R.string.progress_message))
        mDirectionFab.setOnClickListener {
            mProgressDialog!!.show()
            if (supportActionBar != null) {
                supportActionBar!!.setDisplayHomeAsUpEnabled(true)
                supportActionBar!!.setHomeButtonEnabled(true)
                title = getString(R.string.app_name)
            }
            mDrawerLayout?.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED)

            // create RouteTask instance
            mRouteTask = RouteTask(applicationContext, getString(R.string.routing_service))
            val listenableFuture =
                mRouteTask!!.createDefaultParametersAsync()
            listenableFuture.addDoneListener {
                try {
                    if (listenableFuture.isDone) {
                        var i = 0
                        mRouteParams = listenableFuture.get()

                        // create stops
                        val stop1 =
                            Stop(
                                Point(
                                    -117.15083257944445,
                                    32.741123367963446,
                                    SpatialReferences.getWgs84()
                                )
                            )
                        val stop2 =
                            Stop(
                                Point(
                                    -117.15557279683529,
                                    32.703360305883045,
                                    SpatialReferences.getWgs84()
                                )
                            )
                        val routeStops: MutableList<Stop> =
                            ArrayList()
                        // add stops
                        routeStops.add(stop1)
                        routeStops.add(stop2)
                        mRouteParams?.setStops(routeStops)

                        // set return directions as true to return turn-by-turn directions in the result of
                        // getDirectionManeuvers().
                        mRouteParams?.setReturnDirections(true)

                        // solve
                        val result = mRouteTask!!.solveRouteAsync(mRouteParams).get()
                        val routes: List<*> = result.routes
                        mRoute = routes[0] as Route?
                        // create a mRouteSymbol graphic
                        val routeGraphic =
                            Graphic(mRoute!!.routeGeometry, mRouteSymbol)
                        // add mRouteSymbol graphic to the map
                        mGraphicsOverlay!!.graphics.add(routeGraphic)
                        // get directions
                        // NOTE: to get turn-by-turn directions Route Parameters should set returnDirection flag as true
                        val directions =
                            mRoute!!.directionManeuvers
                        val directionsArray =
                            arrayOfNulls<String>(directions.size)
                        for (dm in directions) {
                            directionsArray[i++] = dm.directionText
                        }
                        Log.d(
                            TAG,
                            directions[0].geometry.extent.xMin.toString() + ""
                        )
                        Log.d(
                            TAG,
                            directions[0].geometry.extent.yMin.toString() + ""
                        )

                        // Set the adapter for the list view
                        mDrawerList?.setAdapter(
                            ArrayAdapter(
                                applicationContext,
                                R.layout.directions_layout, directionsArray
                            )
                        )
                        if (mProgressDialog!!.isShowing) {
                            mProgressDialog!!.dismiss()
                        }
                        mDrawerList?.setOnItemClickListener { _: AdapterView<*>?, _: View?, position: Int, _: Long ->
                            if (mGraphicsOverlay!!.graphics.size > 3) {
                                mGraphicsOverlay!!.graphics
                                    .removeAt(mGraphicsOverlay!!.graphics.size - 1)
                            }
                            mDrawerLayout?.closeDrawers()
                            val dm = directions[position]
                            val gm = dm.geometry
                            //val vp = Viewpoint(gm.extent, 20)
                            mMapView?.setViewpointAsync(
                                Viewpoint(gm.extent, 20.0),
                                3f
                            )
                            //mMapView?.setViewpointAsync(vp, 3f)
                            val selectedRouteSymbol = SimpleLineSymbol(
                                SimpleLineSymbol.Style.SOLID,
                                Color.GREEN, 5F
                            )
                            val selectedRouteGraphic = Graphic(
                                directions[position].geometry,
                                selectedRouteSymbol
                            )
                            mGraphicsOverlay!!.graphics.add(selectedRouteGraphic)
                        }
                    }
                } catch (e: Exception) {
                    Log.e(TAG, e.message!!)
                }
            }
        }
    }

    /**
     * Set up the Source, Destination and mRouteSymbol graphics symbol
     */
    private fun setupSymbols() {
        mGraphicsOverlay = GraphicsOverlay()

        //add the overlay to the map view
        mMapView!!.graphicsOverlays.add(mGraphicsOverlay)

        //[DocRef: Name=Picture Marker Symbol Drawable-android, Category=Fundamentals, Topic=Symbols and Renderers]
        // create a picture marker symbol from an app resource
        val startDrawable =
            ContextCompat.getDrawable(this, R.drawable.ic_source_tiny) as BitmapDrawable?
        try {
            val pinSourceSymbol = PictureMarkerSymbol.createAsync(startDrawable).get()
            pinSourceSymbol.offsetY = 20f
            // add a new graphic as start point
            val sourcePoint =
                Point(-117.15083257944445, 32.741123367963446, SpatialReferences.getWgs84())
            val pinSourceGraphic = Graphic(sourcePoint, pinSourceSymbol)
            mGraphicsOverlay!!.graphics.add(pinSourceGraphic)
        } catch (e: InterruptedException) {
            val error = "Error creating picture marker symbol: " + e.message
            Log.e(TAG, error)
            Toast.makeText(this, error, Toast.LENGTH_SHORT).show()
        } catch (e: ExecutionException) {
            val error = "Error creating picture marker symbol: " + e.message
            Log.e(TAG, error)
            Toast.makeText(this, error, Toast.LENGTH_SHORT).show()
        }
        //[DocRef: END]
        val endDrawable =
            ContextCompat.getDrawable(this, R.drawable.ic_destination_tiny) as BitmapDrawable?
        try {
            val pinDestinationSymbol = PictureMarkerSymbol.createAsync(endDrawable).get()
            pinDestinationSymbol.offsetY = 20f
            // add a new graphic as destination point
            val destinationPoint =
                Point(-117.15557279683529, 32.703360305883045, SpatialReferences.getWgs84())
            val destinationGraphic = Graphic(destinationPoint, pinDestinationSymbol)
            mGraphicsOverlay!!.graphics.add(destinationGraphic)
        } catch (e: InterruptedException) {
            val error = "Error creating picture marker symbol: " + e.message
            Log.e(TAG, error)
            Toast.makeText(this, error, Toast.LENGTH_SHORT).show()
        } catch (e: ExecutionException) {
            val error = "Error creating picture marker symbol: " + e.message
            Log.e(TAG, error)
            Toast.makeText(this, error, Toast.LENGTH_SHORT).show()
        }
        mRouteSymbol = SimpleLineSymbol(SimpleLineSymbol.Style.SOLID, Color.BLUE, 5F)
    }

    override fun onPause() {
        super.onPause()
        mMapView!!.pause()
    }

    override fun onResume() {
        super.onResume()
        mMapView!!.resume()
    }

    override fun onDestroy() {
        super.onDestroy()
        mMapView!!.dispose()
    }

    /**
     * set up the drawer
     */
    private fun setupDrawer() {
        mDrawerToggle = object : ActionBarDrawerToggle(
            this,
            mDrawerLayout,
            R.string.drawer_open,
            R.string.drawer_close
        ) {
            /** Called when a drawer has settled in a completely open state.  */
            override fun onDrawerOpened(drawerView: View) {
                super.onDrawerOpened(drawerView)
                invalidateOptionsMenu() // creates call to onPrepareOptionsMenu()
            }

            /** Called when a drawer has settled in a completely closed state.  */
            override fun onDrawerClosed(view: View) {
                super.onDrawerClosed(view)
                invalidateOptionsMenu() // creates call to onPrepareOptionsMenu()
            }
        }
        mDrawerToggle?.setDrawerIndicatorEnabled(true)
        mDrawerLayout!!.addDrawerListener(mDrawerToggle as ActionBarDrawerToggle)
        mDrawerLayout!!.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED)
    }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)
        // Sync the toggle state after onRestoreInstanceState has occurred.
        mDrawerToggle!!.syncState()
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
        super.onConfigurationChanged(newConfig)
        mDrawerToggle!!.onConfigurationChanged(newConfig)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.

        // Activate the navigation drawer toggle
        return mDrawerToggle!!.onOptionsItemSelected(item) || super.onOptionsItemSelected(item)
    }

    companion object {
        private val TAG = GetDirectionsActivity::class.java.simpleName
    }
}