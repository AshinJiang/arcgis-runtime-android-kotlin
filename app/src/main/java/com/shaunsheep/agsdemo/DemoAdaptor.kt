package com.shaunsheep.agsdemo

import android.support.v7.widget.RecyclerView
import android.view.View
import android.view.ViewGroup
import android.widget.Button

/**
 * Created by jiang on 2017/6/8.
 */
class DemoAdaptor(val items: List<String>) : RecyclerView.Adapter<DemoAdaptor.ViewHolder>(), View.OnClickListener{

    private var onItemClickListener:OnItemClickListener?=null

    override fun getItemCount() = items.size

    override fun onCreateViewHolder(p0: ViewGroup?, p1: Int): ViewHolder {
        val btn: Button = (Button(p0!!.context))
        val layoutParams:ViewGroup.MarginLayoutParams = ViewGroup.MarginLayoutParams(ViewGroup.LayoutParams.MATCH_PARENT
                , ViewGroup.LayoutParams.WRAP_CONTENT)
        layoutParams.leftMargin=10
        layoutParams.rightMargin=10
        layoutParams.bottomMargin=15
        layoutParams.topMargin=10
        btn.layoutParams=layoutParams
        btn.setBackgroundColor(p0.resources.getColor(R.color.colorPrimary))
        btn.textSize=p0.context.resources.getDimension(R.dimen.main_btn_text_size)
        btn.setTextColor(p0.context.resources.getColor(R.color.colorWhite))
        val viewHolder: ViewHolder = ViewHolder(btn)
        btn.setOnClickListener(this)
        return viewHolder
    }

    override fun onClick(v: View?) {
        if (onItemClickListener!=null){
            onItemClickListener!!.onClick(v!!.rootView,(v.tag).toString().toInt())
        }
    }

    fun setOnItemClickListener(listener: OnItemClickListener) {
        onItemClickListener = listener
    }

    override fun onBindViewHolder(p0: ViewHolder?, p1: Int) {
        p0!!.button.text = items[p1]
        p0!!.itemView.tag=p1
    }

    class ViewHolder(val button: Button) : RecyclerView.ViewHolder(button)
}