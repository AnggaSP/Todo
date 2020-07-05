/*
 * Copyright (C) 2019 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package id.ac.esaunggul.todo.data.source

import androidx.lifecycle.LiveData
import androidx.lifecycle.map
import id.ac.esaunggul.todo.data.dao.TaskDao
import id.ac.esaunggul.todo.data.model.TaskModel
import id.ac.esaunggul.todo.util.state.Result
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Local impl for task data
 */
class TaskLocalDataSource internal constructor(
    private val taskDao: TaskDao
) : TaskDataSource {

    override fun observeTasks(): LiveData<Result<List<TaskModel>>> {
        return taskDao.observeTasks().map {
            Result.Success(it)
        }
    }

    override fun observeTask(taskId: String): LiveData<Result<TaskModel>> {
        return taskDao.observeTaskById(taskId).map {
            Result.Success(it)
        }
    }

    override suspend fun getTasks(): Result<List<TaskModel>> = withContext(Dispatchers.IO) {
        return@withContext try {
            Result.Success(taskDao.getTasks())
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    override suspend fun getTask(taskId: String): Result<TaskModel> = withContext(Dispatchers.IO) {
        try {
            val task = taskDao.getTaskById(taskId)
            if (task != null) {
                return@withContext Result.Success(task)
            } else {
                return@withContext Result.Error(Exception("Task not found!"))
            }
        } catch (e: Exception) {
            return@withContext Result.Error(e)
        }
    }

    override suspend fun saveTask(taskModel: TaskModel) = withContext(Dispatchers.IO) {
        taskDao.insertTask(taskModel)
    }

    override suspend fun completeTask(taskModel: TaskModel) = withContext(Dispatchers.IO) {
        taskDao.updateCompleted(taskModel.id, true)
    }

    override suspend fun completeTask(taskId: String) {
        taskDao.updateCompleted(taskId, true)
    }

    override suspend fun activateTask(taskModel: TaskModel) = withContext(Dispatchers.IO) {
        taskDao.updateCompleted(taskModel.id, false)
    }

    override suspend fun activateTask(taskId: String) {
        taskDao.updateCompleted(taskId, false)
    }

    override suspend fun clearCompletedTasks() = withContext<Unit>(Dispatchers.IO) {
        taskDao.deleteCompletedTasks()
    }

    override suspend fun deleteAllTasks() = withContext(Dispatchers.IO) {
        taskDao.deleteTasks()
    }

    override suspend fun deleteTask(taskId: String) = withContext<Unit>(Dispatchers.IO) {
        taskDao.deleteTaskById(taskId)
    }
}