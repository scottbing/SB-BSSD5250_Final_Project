package edu.nmhu.bssd5250.sb_maps_demo

import android.content.Context
import android.database.Cursor
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.cursoradapter.widget.SimpleCursorAdapter

class RemindersSimpleCursorAdapter(
    context: Context?,
    layout: Int,
    c: Cursor?,
    from: Array<String?>?,
    to: IntArray?,
    flags: Int
) :
    SimpleCursorAdapter(context, layout, c, from, to, flags) {
    //to use a viewholder, you must override the following two methods and define a ViewHolder class
    override fun newView(context: Context, cursor: Cursor, parent: ViewGroup): View {
        return super.newView(context, cursor, parent)
    }

    override fun bindView(view: View, context: Context, cursor: Cursor) {
        super.bindView(view, context, cursor)
        val holder = view.tag as? ViewHolder
        if (holder != null) {
            if (cursor.getInt(holder.colImp) > 0) {
                holder.listTab!!.setBackgroundColor(ContextCompat.getColor(context, R.color.orange))
            } else {
                holder.listTab!!.setBackgroundColor(ContextCompat.getColor(context, R.color.green))
            }
        }
    }

    internal class ViewHolder {
        //store the column index
        var colImp = 0

        //store the view
        var listTab: View? = null
    }
}