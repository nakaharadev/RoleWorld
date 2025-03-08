package com.nakaharadev.roleworld.service

import android.app.Service
import android.content.Intent
import android.os.IBinder
import com.nakaharadev.roleworld.network.data.AbstractResponse
import com.nakaharadev.roleworld.network.task.AbstractTask
import java.util.LinkedList

class NetworkService : Service() {
    private var stopPingLoop = true
    private var stopTasksLoop = true

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startTasksLoop()

        return START_STICKY
    }

    override fun onDestroy() {
        stopPingLoop = true
        stopTasksLoop = true
    }

    private fun startTasksLoop() {
        if (stopTasksLoop) {
            stopTasksLoop = false
        } else return

        Thread {
            while (!stopTasksLoop) {
                while (!tasks.isEmpty()) {
                    try {
                        tasks.poll()?.task()
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
        }.start()
    }

    companion object {
        private val tasks = LinkedList<AbstractTask>()

        fun addTask(task: AbstractTask, callback: (AbstractResponse) -> Unit) {
            task.callback = callback
            tasks.add(task)
        }
    }
}