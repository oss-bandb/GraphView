package dev.bandb.graphview.sample

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setupToolbar()
        setupRecyclerView()
    }

    private fun setupRecyclerView() {
        findViewById<RecyclerView>(R.id.graphs).apply {
            layoutManager = LinearLayoutManager(this@MainActivity)
            adapter = GraphListAdapter()
            val decoration = DividerItemDecoration(applicationContext, DividerItemDecoration.VERTICAL)
            addItemDecoration(decoration)
        }
    }

    private fun setupToolbar() {
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        val ab = supportActionBar
        ab?.setDisplayHomeAsUpEnabled(false)
    }

    private inner class GraphListAdapter : RecyclerView.Adapter<GraphViewHolder>() {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GraphViewHolder {
            val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.main_item, parent, false)
            return GraphViewHolder(view)
        }

        override fun onBindViewHolder(holder: GraphViewHolder, position: Int) {
            val graphItem = MainContent.ITEMS[position]
            holder.title.text = graphItem.title
            holder.description.text = graphItem.description
            holder.itemView.setOnClickListener { startActivity(Intent(this@MainActivity, graphItem.clazz)) }
        }

        override fun getItemCount(): Int {
            return MainContent.ITEMS.size
        }
    }

    private inner class GraphViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val title: TextView = itemView.findViewById(R.id.title)
        val description: TextView = itemView.findViewById(R.id.description)
    }
}