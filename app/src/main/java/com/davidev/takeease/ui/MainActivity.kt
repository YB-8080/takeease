package com.davidev.takeease.ui

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.KeyEvent
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import com.davidev.takeease.Signin
import com.davidev.takeease.databinding.ActivityMainBinding
import com.davidev.takeease.datasource.TaskDataSource


class MainActivity : AppCompatActivity() {


    private lateinit var binding: ActivityMainBinding
    private val adapter by lazy { TaskListAdapter() }
    private val addEditTaskActivityLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            // No need to manually update list; LiveData observer will handle updates
        }

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize the data source
        TaskDataSource.initialize(this)

        // Setup RecyclerView adapter
        binding.rvTasks.adapter = adapter

        // Observe LiveData from TaskDataSource
        observeTasks()

        // Setup listeners for UI elements
        insertListeners()

        binding.logout.setOnClickListener {
            val backScreen = Intent(applicationContext, Signin::class.java)
            startActivity(backScreen)
            }
    }

    private fun observeTasks() {
        TaskDataSource.getTasksLiveData().observe(this, Observer { tasks ->
            adapter.submitList(tasks)  // Automatically updates the UI when data changes
            binding.includeEmpty.emptyState.visibility = if (tasks.isEmpty()) View.VISIBLE else View.GONE
        })
    }

    private fun insertListeners() {
        binding.fab.setOnClickListener {
            // Launch AddTaskActivity expecting a result using the new API
            val intent = Intent(this, AddTaskActivity::class.java)
            addEditTaskActivityLauncher.launch(intent)
        }

        adapter.listenerEdit = { task ->
            // Edit task and expect a result using the new API
            val intent = Intent(this, AddTaskActivity::class.java).apply {
                putExtra(AddTaskActivity.TASK_ID, task.id)
            }
            addEditTaskActivityLauncher.launch(intent)
        }

        adapter.listenerDelete = { task ->
            TaskDataSource.deleteTask(task)
            // No need to update list manually, LiveData will notify observer
        }
    }

    private var backPressedTime = 0L

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (event?.repeatCount == 0) {
                backPressedTime = System.currentTimeMillis()
            }
            return true  // Indicate that we are handling this event
        }
        return super.onKeyDown(keyCode, event)
    }

    override fun onKeyUp(keyCode: Int, event: KeyEvent?): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (System.currentTimeMillis() - backPressedTime > 500) { // 500ms for long press
                // Long press detected, exit the app
                finishAffinity()  // Close all activities and exit app
                return true
            } else {
                // Short press, go to the home screen
                moveTaskToBack(true)
                return true
            }
        }
        return super.onKeyUp(keyCode, event)
    }
}
