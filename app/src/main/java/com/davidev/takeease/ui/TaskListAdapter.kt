package com.davidev.takeease.ui



import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.PopupMenu
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.davidev.takeease.R
import com.davidev.takeease.databinding.ItemTaskBinding
import com.davidev.takeease.datasource.TaskDataSource
import com.davidev.takeease.model.Task
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale


class TaskListAdapter : ListAdapter<Task, TaskListAdapter.TaskViewHolder>(DiffCallback()) {

    var listenerEdit: (Task) -> Unit = {}
    var listenerDelete: (Task) -> Unit = {}

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaskViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ItemTaskBinding.inflate(inflater, parent, false)

        return TaskViewHolder(binding)
    }

    override fun onBindViewHolder(holder: TaskViewHolder, position: Int) {
        holder.bind(getItem(position))
    }


    inner class TaskViewHolder(
        private val binding: ItemTaskBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        @SuppressLint("SetTextI18n")
        fun bind(item: Task) {
            binding.tvTitle.text = item.title
            binding.tvDate.text = "${item.date} ${item.hour}"
            binding.switch1.isChecked = item.isComplete
            binding.ivMore.setOnClickListener {
                showPopup(item)
            }
            if (!item.isComplete) {
                scheduleNotification(item)
            }
            updateTaskAppearance(item.isComplete)
            binding.switch1.setOnCheckedChangeListener { _, isChecked ->
                if (item.isComplete != isChecked) { // Prevent unnecessary updates
                    item.isComplete = isChecked
                    TaskDataSource.updateTaskCompletion(item.id, isChecked)
                    updateTaskAppearance(isChecked)
                }
            }
        }

        private fun updateTaskAppearance(isComplete: Boolean) {
            Log.d("TaskAdapter", "updateTaskAppearance called with isComplete: $isComplete")
            if (isComplete) {
                binding.taskBar.setBackgroundColor(Color.parseColor("#FFEBEE"))
                binding.tvTitle.setTextColor(Color.RED)
            } else {
                binding.taskBar.setBackgroundColor(Color.WHITE)
                binding.tvTitle.setTextColor(Color.BLACK)
            }
        }
        private fun scheduleNotification(item: Task) {
            val alarmManager = itemView.context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            val intent = Intent(itemView.context, ReminderBroadcastReceiver::class.java).apply {
                putExtra("taskId", item.id)
                putExtra("taskTitle", item.title)
            }
            val pendingIntent = PendingIntent.getBroadcast(itemView.context, item.id, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)

            // Adjust the date format to match your input date string
            val datetime = Calendar.getInstance()
            val dateFormat = SimpleDateFormat("dd/MM/yy HH:mm", Locale.getDefault())
            try {
                datetime.time = dateFormat.parse("${item.date} ${item.hour}") ?: throw IllegalArgumentException("Invalid date or time format")
            } catch (e: ParseException) {
                e.printStackTrace()
                return
            }

            // Set the alarm to trigger
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, datetime.timeInMillis, pendingIntent)
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                alarmManager.setExact(AlarmManager.RTC_WAKEUP, datetime.timeInMillis, pendingIntent)
            } else {
                alarmManager.set(AlarmManager.RTC_WAKEUP, datetime.timeInMillis, pendingIntent)
            }
        }


        private fun showPopup(item: Task) {
            val ivMore = binding.ivMore
            val popupMenu = PopupMenu(ivMore.context, ivMore)
            popupMenu.menuInflater.inflate(R.menu.popup_menu, popupMenu.menu)
            popupMenu.setOnMenuItemClickListener {
                when (it.itemId) {
                    R.id.action_edit -> listenerEdit(item)
                    R.id.action_delete -> listenerDelete(item)
                }
                return@setOnMenuItemClickListener true
            }
            popupMenu.show()
        }
    }

}



class DiffCallback : DiffUtil.ItemCallback<Task>() {
    override fun areItemsTheSame(oldItem: Task, newItem: Task): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: Task, newItem: Task): Boolean {
        return oldItem == newItem
    }
}