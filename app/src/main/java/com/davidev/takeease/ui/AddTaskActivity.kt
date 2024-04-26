package com.davidev.takeease.ui

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.davidev.takeease.databinding.ActivityAddTaskBinding
import com.davidev.takeease.datasource.TaskDataSource
import com.davidev.takeease.extensions.format
import com.davidev.takeease.extensions.text
import com.davidev.takeease.model.Task
import com.google.android.material.button.MaterialButton
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.timepicker.MaterialTimePicker
import com.google.android.material.timepicker.TimeFormat
import java.util.*


class AddTaskActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddTaskBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        binding = ActivityAddTaskBinding.inflate(layoutInflater)
        setContentView(binding.root)


        if (intent.hasExtra(TASK_ID)) {
            binding.btnCancel.visibility = MaterialButton.VISIBLE
            val taskId = intent.getIntExtra(TASK_ID, 0)
            TaskDataSource.findById(taskId)?.let {
                binding.tilTitle.text = it.title
                binding.tilDate.text = it.date
                binding.tilHour.text = it.hour
            }

            binding.btnNewTask.visibility = MaterialButton.GONE
            binding.btnNewButton.visibility = MaterialButton.VISIBLE


        }

        insertListeners()
    }

    private fun insertListeners() {

        //Date
        binding.tilDate.editText?.setOnClickListener {
            val datePicker = MaterialDatePicker.Builder.datePicker().build()

            datePicker.addOnPositiveButtonClickListener {
                //binding.tilDate.editText?.setText(Date(it).format())
                val timeZone = TimeZone.getDefault()

                // the use of "* -1" is to fix the local date/time offset selected by user
                val offset = timeZone.getOffset(Date().time) * -1
                binding.tilDate.text = Date(it + offset).format()
            }
            datePicker.show(supportFragmentManager, "DATE_PICKER_TAG")
        }

        //Hour
        binding.tilHour.editText?.setOnClickListener {
            val timePicker = MaterialTimePicker.Builder()
                .setTimeFormat(TimeFormat.CLOCK_24H)
                .build()

            timePicker.addOnPositiveButtonClickListener {
                val minute =
                    if (timePicker.minute in 0..9) "0${timePicker.minute}" else timePicker.minute
                val hour = if (timePicker.hour in 0..9) "0${timePicker.hour}" else timePicker.hour

                binding.tilHour.text = "$hour:$minute"
            }

            timePicker.show(supportFragmentManager, null)
        }

        binding.btnCancel.setOnClickListener {
            finish()
        }

        binding.btnNewTask.setOnClickListener {
            val title = binding.tilTitle.text.toString().trim()
            val date = binding.tilDate.text.toString()
            val hour = binding.tilHour.text.toString()
            val id = intent.getIntExtra(TASK_ID, 0)  // Likely 0 for new task

            if (title.isEmpty()) {
                Toast.makeText(this, "Please enter a task title", Toast.LENGTH_SHORT).show()
            } else {
                val task = Task(id = id, title = title, date = date, hour = hour, isComplete = false)
                TaskDataSource.insertTask(task)
                setResult(Activity.RESULT_OK)
                finish()
            }
        }

        binding.btnNewButton.setOnClickListener {
            val title = binding.tilTitle.text.toString().trim()
            val date = binding.tilDate.text.toString()
            val hour = binding.tilHour.text.toString()
            val id = intent.getIntExtra(TASK_ID, 0)

            if (title.isEmpty()) {
                Toast.makeText(this, "Please enter a task title", Toast.LENGTH_SHORT).show()
            } else {
                TaskDataSource.findById(id)?.let {
                    val updatedTask = Task(id = id, title = title, date = date, hour = hour, isComplete = it.isComplete)
                    TaskDataSource.updateTask(updatedTask)
                    setResult(Activity.RESULT_OK)
                    val backScreen = Intent(applicationContext, MainActivity::class.java)
                    startActivity(backScreen)
                    finish()  // Assuming you want to close this Activity after updating
                }
            }
        }

    }


    companion object {
        const val TASK_ID = "task_id"
    }

}