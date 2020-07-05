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
package id.ac.esaunggul.todo.ui.detail

import androidx.annotation.StringRes
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.map
import androidx.lifecycle.switchMap
import androidx.lifecycle.viewModelScope
import id.ac.esaunggul.todo.R
import id.ac.esaunggul.todo.data.model.TaskModel
import id.ac.esaunggul.todo.data.repository.TasksRepository
import id.ac.esaunggul.todo.util.event.Event
import id.ac.esaunggul.todo.util.state.Result
import id.ac.esaunggul.todo.util.state.Result.Success
import kotlinx.coroutines.launch

class DetailViewModel(
    private val tasksRepository: TasksRepository
) : ViewModel() {

    private val _taskId = MutableLiveData<String>()

    private val _task = _taskId.switchMap { taskId ->
        tasksRepository.observeTask(taskId).map { computeResult(it) }
    }
    val taskModel: LiveData<TaskModel?> = _task

    val isDataAvailable: LiveData<Boolean> = _task.map { it != null }

    private val _dataLoading = MutableLiveData<Boolean>()
    val dataLoading: LiveData<Boolean> = _dataLoading

    private val _editTaskEvent = MutableLiveData<Event<Unit>>()
    val editTaskEvent: LiveData<Event<Unit>> = _editTaskEvent

    private val _deleteTaskEvent = MutableLiveData<Event<Unit>>()
    val deleteTaskEvent: LiveData<Event<Unit>> = _deleteTaskEvent

    private val _snackbarText = MutableLiveData<Event<Int>>()
    val snackbarText: LiveData<Event<Int>> = _snackbarText

    val completed: LiveData<Boolean> = _task.map { input: TaskModel? ->
        input?.isCompleted ?: false
    }

    fun deleteTask() = viewModelScope.launch {
        _taskId.value?.let {
            tasksRepository.deleteTask(it)
            _deleteTaskEvent.value = Event(Unit)
        }
    }

    fun editTask() {
        _editTaskEvent.value = Event(Unit)
    }

    fun setCompleted(completed: Boolean) = viewModelScope.launch {
        val task = _task.value ?: return@launch
        if (completed) {
            tasksRepository.completeTask(task)
            showSnackbarMessage(R.string.task_marked_complete)
        } else {
            tasksRepository.activateTask(task)
            showSnackbarMessage(R.string.task_marked_active)
        }
    }

    fun start(taskId: String?) {
        if (_dataLoading.value == true || taskId == _taskId.value) {
            return
        }
        _taskId.value = taskId
    }

    private fun computeResult(taskModelResult: Result<TaskModel>): TaskModel? {
        return if (taskModelResult is Success) {
            taskModelResult.data
        } else {
            showSnackbarMessage(R.string.loading_tasks_error)
            null
        }
    }

    private fun showSnackbarMessage(@StringRes message: Int) {
        _snackbarText.value = Event(message)
    }
}