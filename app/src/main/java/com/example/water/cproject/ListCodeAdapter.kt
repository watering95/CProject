package com.example.water.cproject

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView
import java.util.*

/**
 * Created by watering on 18. 3. 17.
 */

class ListCodeAdapter(context: Context, data: ArrayList<String>?) : BaseAdapter() {
    private val list: List<String>?
    private val inflater: LayoutInflater

    init {
        this.list = data
        inflater = LayoutInflater.from(context)
    }

    override fun getCount(): Int {
        return this.list!!.size
    }

    override fun getItem(position: Int): Any {
        return list!![position]
    }

    override fun getItemId(position: Int): Long {
        return 0
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        var convertView = convertView
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.layout_list, parent, false)
        }

        val code = convertView!!.findViewById<TextView>(R.id.textView_layout_list)
        if (!list!!.isEmpty()) code.text = list[position]
        return convertView
    }
}
