package me.venj.remotehelper

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.View
import android.widget.LinearLayout

class TorrentsListActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_torrents_list)
        actionBar?.setHomeButtonEnabled(true)

        setTitle(R.string.torrents_title)

        val recyclerView = findViewById<RecyclerView>(R.id.torrentsListTableView)
        recyclerView.layoutManager = LinearLayoutManager(this, LinearLayout.VERTICAL, false)
        recyclerView.addOnItemTouchListener(RecyclerItemOnClickListener(applicationContext, recyclerView, object: RecyclerItemOnClickListener.OnItemClickListener {
            override fun onItemClick(view: View, position: Int) {
                Log.info("Item clicked at $position!")
            }

            override fun onItemLongClick(view: View, position: Int) {
                Log.info("Item long clicked at $position!")
            }
        }))

        val torrentsList = intent.getSerializableExtra("list") as List<TorrentsListItem>

        val adaptor = TorrentsListAdaptor(torrentsList)
        recyclerView.adapter = adaptor
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}
