package com.davidev.takeease.datasource

import android.content.ContentValues
import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.davidev.takeease.model.Task
import android.util.Log

object TaskDataSource {
    lateinit var dbHelper: DBHelper
    private val tasksLiveData = MutableLiveData<List<Task>>()

    fun initialize(context: Context) {
        dbHelper = DBHelper(context)
        refreshTasks()
    }

    fun getTasksLiveData(): LiveData<List<Task>> = tasksLiveData

    private fun refreshTasks() {
        val db = dbHelper.readableDatabase
        val list = mutableListOf<Task>()
        try {
            val cursor = db.rawQuery("SELECT * FROM ${DBHelper.TABLE_TASKS}", null)
            if (cursor.moveToFirst()) {
                do {
                    val id = cursor.getInt(cursor.getColumnIndex(DBHelper.COLUMN_ID))
                    val title = cursor.getString(cursor.getColumnIndex(DBHelper.COLUMN_TITLE))
                    val hour = cursor.getString(cursor.getColumnIndex(DBHelper.COLUMN_HOUR))
                    val date = cursor.getString(cursor.getColumnIndex(DBHelper.COLUMN_DATE))
                    val isComplete = cursor.getInt(cursor.getColumnIndex(DBHelper.COLUMN_IS_COMPLETE)) > 0
                    list.add(Task(id, title, date, hour, isComplete))
                } while (cursor.moveToNext())
            }
            cursor.close()
        } catch (e: Exception) {
            Log.e("TaskDataSource", "Error refreshing tasks", e)
        }
        tasksLiveData.postValue(list)
    }

    fun insertTask(task: Task) {
        val db = dbHelper.writableDatabase
        val values = ContentValues().apply {
            put(DBHelper.COLUMN_TITLE, task.title)
            put(DBHelper.COLUMN_HOUR, task.hour)
            put(DBHelper.COLUMN_DATE, task.date)
            put(DBHelper.COLUMN_IS_COMPLETE, if (task.isComplete) 1 else 0)
        }
        db.insert(DBHelper.TABLE_TASKS, null, values)
        refreshTasks()
    }

    fun updateTask(task: Task): Int {
        val db = dbHelper.writableDatabase
        val values = ContentValues().apply {
            put(DBHelper.COLUMN_TITLE, task.title)
            put(DBHelper.COLUMN_HOUR, task.hour)
            put(DBHelper.COLUMN_DATE, task.date)
            put(DBHelper.COLUMN_IS_COMPLETE, if (task.isComplete) 1 else 0)
        }
        val selection = "${DBHelper.COLUMN_ID} = ?"
        val selectionArgs = arrayOf(task.id.toString())
        val count = db.update(DBHelper.TABLE_TASKS, values, selection, selectionArgs)
        if (count > 0) refreshTasks()
        return count
    }

    fun updateTaskCompletion(taskId: Int, isComplete: Boolean) {
        val db = dbHelper.writableDatabase
        val contentValues = ContentValues()
        contentValues.put(DBHelper.COLUMN_IS_COMPLETE, if (isComplete) 1 else 0)

        val selection = "${DBHelper.COLUMN_ID} = ?"
        val selectionArgs = arrayOf(taskId.toString())
        db.update(DBHelper.TABLE_TASKS, contentValues, selection, selectionArgs)
    }
    fun deleteTask(task: Task) {
        val db = dbHelper.writableDatabase
        db.delete(DBHelper.TABLE_TASKS, "${DBHelper.COLUMN_ID} = ?", arrayOf(task.id.toString()))
        refreshTasks()
    }

    fun findById(taskId: Int): Task? {
        val db = dbHelper.readableDatabase
        var task: Task? = null
        try {
            val cursor = db.query(DBHelper.TABLE_TASKS, null, "${DBHelper.COLUMN_ID} = ?", arrayOf(taskId.toString()), null, null, null)
            if (cursor.moveToFirst()) {
                val id = cursor.getInt(cursor.getColumnIndex(DBHelper.COLUMN_ID))
                val title = cursor.getString(cursor.getColumnIndex(DBHelper.COLUMN_TITLE))
                val hour = cursor.getString(cursor.getColumnIndex(DBHelper.COLUMN_HOUR))
                val date = cursor.getString(cursor.getColumnIndex(DBHelper.COLUMN_DATE))
                val isComplete = cursor.getInt(cursor.getColumnIndex(DBHelper.COLUMN_IS_COMPLETE)) > 0
                task = Task(id, title, date, hour, isComplete)
            }
            cursor.close()
        } catch (e: Exception) {
            Log.e("TaskDataSource", "Error finding task by ID", e)
        }
        return task
    }
}
