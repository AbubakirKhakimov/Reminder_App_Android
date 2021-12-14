package com.x.a_technologies.reminder.adapters

import android.content.Context
import android.media.MediaPlayer
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import androidx.recyclerview.widget.RecyclerView
import com.x.a_technologies.reminder.R
import com.x.a_technologies.reminder.datas.ReminderData
import com.x.a_technologies.reminder.databinding.RemindersItemLayoutBinding
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

interface RemindersAdapterCallBack{
    fun delateReminder(position: Int)
    fun editReminder(position: Int)
    fun setReminder(position: Int)
    fun cancelReminder(position: Int)
}

class RemindersAdapter(val remindersDataList:ArrayList<ReminderData>, val remindersAdapterCallBack: RemindersAdapterCallBack):RecyclerView.Adapter<RemindersAdapter.ItemHolder>(),PopupMenu.OnMenuItemClickListener {
    inner class ItemHolder(val binding: RemindersItemLayoutBinding):RecyclerView.ViewHolder(binding.root)

    lateinit var mediaPlayer: MediaPlayer
    var popupPosition = -1
    lateinit var context:Context

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemHolder {
        context = parent.context
        return ItemHolder(RemindersItemLayoutBinding.inflate(LayoutInflater.from(parent.context),parent,false))
    }

    override fun onBindViewHolder(holder: ItemHolder, position: Int) {
        val item = remindersDataList[position]
        holder.binding.reminderTitle.text = item.reminderTitle
        holder.binding.reminderDate.text = getDateFormat(item.reminderTimeMillis)

        holder.binding.mode.text =
            if (item.everyDayMode)
                context.getString(R.string.every_day)
            else context.getString(R.string.disposable)

        holder.binding.vibrateIcon.visibility =
            if (item.vibrateMode) View.VISIBLE
            else View.GONE

        holder.binding.switchActive.isChecked = item.reminderActive

        holder.binding.moreFun.setOnClickListener {
            popupPosition = position
            val pupop = PopupMenu(context,it)
            pupop.setOnMenuItemClickListener(this)
            pupop.inflate(R.menu.reminders_change_menu)
            pupop.show()
        }

        holder.binding.switchActive.setOnCheckedChangeListener { buttonView, isChecked ->
            if (isChecked){
                remindersAdapterCallBack.setReminder(position)
            }else{
                remindersAdapterCallBack.cancelReminder(position)
            }
        }
    }

    override fun getItemCount(): Int {
        return remindersDataList.size
    }

    fun getDateFormat(timeMillis:Long):String{
        return SimpleDateFormat("E, dd MMMM  HH:mm").format(Date(timeMillis))
    }

    override fun onMenuItemClick(item: MenuItem?): Boolean {
        when(item!!.itemId){
            R.id.startAudio -> {
                mediaPlayer = MediaPlayer()
                mediaPlayer.setDataSource(remindersDataList[popupPosition].reminderAudioPath)
                mediaPlayer.prepare()
                mediaPlayer.start()
            }
            R.id.deleteReminder -> {
                remindersAdapterCallBack.delateReminder(popupPosition)
                notifyDataSetChanged()
            }
            R.id.editReminder -> {
                remindersAdapterCallBack.editReminder(popupPosition)
            }
        }
        return true
    }

}