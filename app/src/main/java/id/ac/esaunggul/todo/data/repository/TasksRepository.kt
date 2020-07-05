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
import id.ac.esaunggul.todo.util.state.Result

interface TasksRepository {

    fun observeTasks(): LiveData<Result<List<TaskModel>>>

    suspend fun getTasks(forceUpdate: Boolean = false): Result<List<TaskModel>>

    fun observeTask(taskId: String): LiveData<Result<TaskModel>>

    suspend fun getTask(taskId: String, forceUpdate: Boolean = false): Result<TaskModel>

    suspend fun saveTask(taskModel: TaskModel)

    suspend fun completeTask(taskModel: TaskModel)

    suspend fun completeTask(taskId: String)

    suspend fun activateTask(taskModel: TaskModel)

    suspend fun activateTask(taskId: String)

    suspend fun clearCompletedTasks()

    suspend fun deleteAllTasks()

    suspend fun deleteTask(taskId: String)
}