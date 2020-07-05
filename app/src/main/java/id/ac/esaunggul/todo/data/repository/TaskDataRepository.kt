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
package id.ac.esaunggul.todo.data.repository

import androidx.lifecycle.LiveData
import id.ac.esaunggul.todo.data.model.TaskModel
import id.ac.esaunggul.todo.data.source.TaskDataSource
import id.ac.esaunggul.todo.util.state.Result
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Default repository for local data saving with room sqlite library
 * This app is ready for remote data source repositories
 */
class TaskDataRepository(
    private val tasksLocalDataSource: TaskDataSource
) : TasksRepository {

    override suspend fun getTasks(forceUpdate: Boolean): Result<List<TaskModel>> {
        return tasksLocalDataSource.getTasks()
    }

    override fun observeTasks(): LiveData<Result<List<TaskModel>>> {
        return tasksLocalDataSource.observeTasks()
    }

    override fun observeTask(taskId: String): LiveData<Result<TaskModel>> {
        return tasksLocalDataSource.observeTask(taskId)
    }

    override suspend fun getTask(taskId: String, forceUpdate: Boolean): Result<TaskModel> {
        return tasksLocalDataSource.getTask(taskId)
    }

    override suspend fun saveTask(taskModel: TaskModel) {
        coroutineScope {
            launch { tasksLocalDataSource.saveTask(taskModel) }
        }
    }

    override suspend fun completeTask(taskModel: TaskModel) {
        coroutineScope {
            launch { tasksLocalDataSource.completeTask(taskModel) }
        }
    }

    override suspend fun completeTask(taskId: String) {
        withContext(Dispatchers.IO) {
            (getTaskWithId(taskId) as? Result.Success)?.let { it ->
                completeTask(it.data)
            }
        }
    }

    override suspend fun activateTask(taskModel: TaskModel) = withContext<Unit>(Dispatchers.IO) {
        coroutineScope {
            launch { tasksLocalDataSource.activateTask(taskModel) }
        }
    }

    override suspend fun activateTask(taskId: String) {
        withContext(Dispatchers.IO) {
            (getTaskWithId(taskId) as? Result.Success)?.let { it ->
                activateTask(it.data)
            }
        }
    }

    override suspend fun clearCompletedTasks() {
        coroutineScope {
            launch { tasksLocalDataSource.clearCompletedTasks() }
        }
    }

    override suspend fun deleteAllTasks() {
        withContext(Dispatchers.IO) {
            coroutineScope {
                launch { tasksLocalDataSource.deleteAllTasks() }
            }
        }
    }

    override suspend fun deleteTask(taskId: String) {
        coroutineScope {
            launch { tasksLocalDataSource.deleteTask(taskId) }
        }
    }

    private suspend fun getTaskWithId(id: String): Result<TaskModel> {
        return tasksLocalDataSource.getTask(id)
    }
}