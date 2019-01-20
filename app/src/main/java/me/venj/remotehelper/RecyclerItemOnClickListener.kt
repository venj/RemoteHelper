package me.venj.remotehelper

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View

class RecyclerItemOnClickListener(context: Context, recyclerView: RecyclerView, private val listener: OnItemClickListener) : RecyclerView.OnItemTouchListener {

    interface OnItemClickListener {
        fun onItemClick(view: View, position: Int)
        fun onItemLongClick(view: View, position: Int)
    }

    private val gestureDetector: GestureDetector

    init {
        gestureDetector = GestureDetector(context, object: GestureDetector.SimpleOnGestureListener() {
            override fun onSingleTapUp(e: MotionEvent): Boolean {
                return true
            }

            override fun onLongPress(e: MotionEvent) {
                val child = recyclerView.findChildViewUnder(e.x, e.y)
                if (child != null && listener != null) {
                    listener.onItemLongClick(child, recyclerView.getChildPosition(child))
                }
            }
        })
    }

    override fun onTouchEvent(recyclerView: RecyclerView, e: MotionEvent) {}

    override fun onInterceptTouchEvent(recyclerView: RecyclerView, e: MotionEvent): Boolean {
        val child = recyclerView.findChildViewUnder(e.x, e.y)
        if (child != null && gestureDetector.onTouchEvent(e)) {
            Log.info("Item touched: $child!")
            listener.onItemClick(child, recyclerView.getChildPosition(child))
        }
        return false
    }

    override fun onRequestDisallowInterceptTouchEvent(p0: Boolean) {}
}