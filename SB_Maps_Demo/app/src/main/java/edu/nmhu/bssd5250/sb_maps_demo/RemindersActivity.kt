package edu.nmhu.bssd5250.sb_maps_demo

import android.annotation.TargetApi
import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.*
import android.widget.AbsListView.MultiChoiceModeListener
import android.widget.AdapterView.OnItemClickListener
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.navigation.NavigationView
import edu.nmhu.bssd5250.sb_maps_demo.DeleteConfirmationDialogFragment.NoticeDialogListener

class RemindersActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener,
    NoticeDialogListener {
    private var mListView: ListView? = null
    private var mDbAdapter: RemindersDbAdapter? = null
    private var listPosition = 0
    private var mCursorAdapter: RemindersSimpleCursorAdapter? = null
    private var mdialogFragment: DeleteConfirmationDialogFragment? = null
    private var fm: FragmentManager? = null
    private val confirmFlag = 0
    private val url = "https://www.android.com"

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_reminders)
        val toolbar = findViewById<View>(R.id.toolbar) as Toolbar
        setSupportActionBar(toolbar)
        val fab = findViewById<View>(R.id.fab) as FloatingActionButton
        fab.setOnClickListener { /* Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                            .setAction("Action", null).show();*/
            //create new Reminder
            fireCustomDialog(null)
        }
        val drawer = findViewById<View>(R.id.drawer_layout) as DrawerLayout
        val toggle = ActionBarDrawerToggle(
            this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close
        )
        drawer.setDrawerListener(toggle)
        toggle.syncState()
        val navigationView = findViewById<View>(R.id.nav_view) as NavigationView
        navigationView.setNavigationItemSelectedListener(this)
        val actionBar = supportActionBar
        actionBar!!.setHomeButtonEnabled(true)
        actionBar.setDisplayShowHomeEnabled(true)
        actionBar.setIcon(R.mipmap.ic_launcher)
        mListView = findViewById<View>(R.id.reminders_list_view) as ListView
        mListView!!.divider = null
        mDbAdapter = RemindersDbAdapter(this)
        mDbAdapter!!.open()
        if (savedInstanceState == null) {
            initializeDatabase()

            /*//Clear all data
            mDbAdapter.deleteAllReminders();
            //Add some data
            insertSomeReminders();*/
        }
        val cursor = mDbAdapter!!.fetchAllReminders()
        //from columns defined in the db
        val from = arrayOf<String?>(
            RemindersDbAdapter.COL_CONTENT
        )
        //to the ids of views in the layout
        val to = intArrayOf(R.id.row_text)
        mCursorAdapter = RemindersSimpleCursorAdapter( //context
            this@RemindersActivity,  //the layout of the row
            R.layout.reminders_row,  //cursor
            cursor,  //from columns defined in the db
            from,  //to the ids of views in the layout
            to,  //flag - not used
            0
        )
        //the cursorAdapter (controller) is now updating the listView (view)
        //with data from the db(model)
        mListView!!.adapter = mCursorAdapter

        //when we click an individual item in the listview
        mListView!!.onItemClickListener =
            OnItemClickListener { parent, view, masterListPosition, id ->
                listPosition = masterListPosition // transfer the value
                val builder = AlertDialog.Builder(this@RemindersActivity)
                val modeListView = ListView(this@RemindersActivity)
                val modes = arrayOf("Edit Reminder", "Delete Reminder")
                val modeAdapter = ArrayAdapter(
                    this@RemindersActivity,
                    android.R.layout.simple_list_item_1, android.R.id.text1, modes
                )
                modeListView.adapter = modeAdapter
                builder.setView(modeListView)
                val dialog: Dialog = builder.create()
                dialog.show()
                modeListView.onItemClickListener =
                    OnItemClickListener { parent, view, position, id -> //edit reminder
                        if (position == 0) {
                            val nId = getIdFromPosition(masterListPosition)
                            val reminder = mDbAdapter!!.fetchReminderById(nId)
                            fireCustomDialog(reminder)
                            //delete reminder
                        } else {
                            // show confirmation dialog
                            fm = supportFragmentManager
                            mdialogFragment = DeleteConfirmationDialogFragment()
                            mdialogFragment!!.show(fm!!, "Delete Confirmation Dialog")
                        }
                        dialog.dismiss()
                    }
            }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            mListView!!.choiceMode = ListView.CHOICE_MODE_MULTIPLE_MODAL
            mListView!!.setMultiChoiceModeListener(object : MultiChoiceModeListener {
                override fun onCreateActionMode(mode: ActionMode, menu: Menu): Boolean {
                    val inflater = mode.menuInflater
                    inflater.inflate(R.menu.cam_menu, menu)
                    return true
                }

                override fun onPrepareActionMode(mode: ActionMode, menu: Menu): Boolean {
                    return false
                }

                override fun onActionItemClicked(mode: ActionMode, item: MenuItem): Boolean {
                    when (item.itemId) {
                        R.id.menu_item_delete_reminder -> {
                            var nC = mCursorAdapter!!.count - 1
                            while (nC >= 0) {
                                if (mListView!!.isItemChecked(nC)) {
                                    mDbAdapter!!.deleteReminderById(getIdFromPosition(nC))
                                }
                                nC--
                            }
                            mode.finish()
                            mCursorAdapter!!.changeCursor(mDbAdapter!!.fetchAllReminders())
                            return true
                        }
                    }
                    return false
                }

                override fun onDestroyActionMode(mode: ActionMode) {}
                override fun onItemCheckedStateChanged(
                    mode: ActionMode,
                    position: Int,
                    id: Long,
                    checked: Boolean
                ) {
                }

                fun showConfirmationDialog() {
                    // Create an instance of the dialog fragment and show it
                    val fm = supportFragmentManager
                    mdialogFragment = DeleteConfirmationDialogFragment()
                    mdialogFragment!!.show(fm, " ") //.show(fm, "Delete Confirmation Dialog");
                }
            })
        }
    }

    // The dialog fragment receives a reference to this Activity through the
    // Fragment.onAttach() callback, which it uses to call the following methods
    // defined by the NoticeDialogFragment.NoticeDialogListener interface
    override fun onDialogPositiveClick(dialog: DialogFragment?) {
        // User touched the dialog's positive button
        mDbAdapter!!.deleteReminderById(getIdFromPosition(listPosition))
        mCursorAdapter!!.changeCursor(mDbAdapter!!.fetchAllReminders())
    }

    override fun onDialogNegativeClick(dialog: DialogFragment?) {
        // User touched the dialog's negative button
        // do nothing...
    }

    private fun getIdFromPosition(nC: Int): Int {
        return mCursorAdapter!!.getItemId(nC).toInt()
    }

    private fun initializeDatabase() {
        //Clear all data
        mDbAdapter!!.deleteAllReminders()
        //Add some data
        insertSomeReminders()
    }

    private fun insertSomeReminders() {
        mDbAdapter!!.createReminder("Pick up kids at school", true)
        mDbAdapter!!.createReminder("Get anniversary gift", false)
        mDbAdapter!!.createReminder("Take car into shop", false)
        mDbAdapter!!.createReminder("Practice the piano", false)
        mDbAdapter!!.createReminder("Trim the hedges", false)
        mDbAdapter!!.createReminder("Finish school project", true)
        mDbAdapter!!.createReminder("Paint the garage", false)
        mDbAdapter!!.createReminder("Get concert tickets", false)
        mDbAdapter!!.createReminder("Go to Costco", false)
        mDbAdapter!!.createReminder("Dentist appointment tomorrow", true)
        mDbAdapter!!.createReminder("Donate items to Goodwill", false)
        mDbAdapter!!.createReminder("Meet with Lawyer", false)
        mDbAdapter!!.createReminder("Finish preparing tax returns", false)
        mDbAdapter!!.createReminder("Get a haircut", false)
        mDbAdapter!!.createReminder("Sign up for next sememster", true)
    }

    private fun fireCustomDialog(reminder: Reminder?) {
        // custom dialog
        val dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.dialog_custom)
        val titleView = dialog.findViewById<View>(R.id.custom_title) as TextView
        val editCustom = dialog.findViewById<View>(R.id.custom_edit_reminder) as EditText
        val commitButton = dialog.findViewById<View>(R.id.custom_button_commit) as Button
        commitButton.setTextColor(Color.parseColor("#054d21"))
        val checkBox = dialog.findViewById<View>(R.id.custom_check_box) as CheckBox
        val rootLayout = dialog.findViewById<View>(R.id.custom_root_layout) as LinearLayout
        val isEditOperation = reminder != null
        //this is for an edit
        if (isEditOperation) {
            titleView.text = "Edit Reminder"
            checkBox.isChecked = reminder!!.important == 1
            editCustom.setText(reminder.content)
            rootLayout.setBackgroundColor(resources.getColor(R.color.blue))
        }
        commitButton.setOnClickListener {
            val reminderText = editCustom.text.toString()
            if (isEditOperation) {
                val reminderEdited = Reminder(
                    reminder!!.id,
                    reminderText, if (checkBox.isChecked) 1 else 0
                )
                mDbAdapter!!.updateReminder(reminderEdited)
                //this is for new reminder
            } else {
                mDbAdapter!!.createReminder(reminderText, checkBox.isChecked)
            }
            mCursorAdapter!!.changeCursor(mDbAdapter!!.fetchAllReminders())
            dialog.dismiss()
        }
        val buttonCancel = dialog.findViewById<View>(R.id.custom_button_cancel) as Button
        buttonCancel.setOnClickListener { dialog.dismiss() }
        buttonCancel.setTextColor(Color.parseColor("#054d21"))
        dialog.show()
    }

    override fun onBackPressed() {
        val drawer = findViewById<View>(R.id.drawer_layout) as DrawerLayout
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_reminders, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_new -> {
                //create new Reminder
                fireCustomDialog(null)
                true
            }
            R.id.action_exit -> {
                finish()
                true
            }
            else -> false
        }
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        // Handle navigation view item clicks here.
        val id = item.itemId
        if (id == R.id.add_a_reminder) {
            //create new Reminder
            fireCustomDialog(null)
            return true
        } else if (id == R.id.refreshdb_app) {
            initializeDatabase()
            //Update Cursor
            mCursorAdapter!!.changeCursor(mDbAdapter!!.fetchAllReminders())
        } else if (id == R.id.webview_app) {
            Log.d("Android", "Access to android.com")
            val intent = Intent(this@RemindersActivity, WebViewActivity::class.java)
            intent.putExtra("url", url)
            startActivity(intent)
        } else if (id == R.id.gestures_app) {
            Log.d("Gestures", "Demonstrate Pinch Gesture")
            val intent = Intent(this@RemindersActivity, GesturesActivity::class.java)
            startActivity(intent)
        } else if (id == R.id.camera_app) {
            Log.d("Camera", "Take a Picture")
            val intent = Intent(this@RemindersActivity, CameraActivity::class.java)
            startActivity(intent)
        } else if (id == R.id.compass_app) {
            Log.d("Compass", "Compass")
            val intent = Intent(this@RemindersActivity, CompassActivity::class.java)
            startActivity(intent)
        } else if (id == R.id.find_address_app) {
            Log.d("Address", "Address")
            val intent = Intent(this@RemindersActivity, FindAddressActivity::class.java)
            startActivity(intent)
        } else if (id == R.id.get_directions_app) {
            Log.d("Directions", "Directions")
            val intent = Intent(this@RemindersActivity, GetDirectionsActivity::class.java)
            startActivity(intent)
        } else if (id == R.id.exit_app) {
            finish()
        }
        val drawer = findViewById<View>(R.id.drawer_layout) as DrawerLayout
        drawer.closeDrawer(GravityCompat.START)
        return true
    }
}