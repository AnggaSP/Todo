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
package id.ac.esaunggul.todo.ui.add

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import id.ac.esaunggul.todo.R
import id.ac.esaunggul.todo.data.model.TaskModel
import id.ac.esaunggul.todo.data.repository.TasksRepository
import id.ac.esaunggul.todo.util.event.Event
import id.ac.esaunggul.todo.util.state.Result
import kotlinx.coroutines.launch

class AddViewModel(
    private val tasksRepository: TasksRepository
) : ViewModel() {

    val title = MutableLiveData<String>()
    val description = MutableLiveData<String>()
    private val _dataLoading = MutableLiveData<Boolean>()
    val dataLoading: LiveData<Boolean> = _dataLoading
    private val _snackbarText = MutableLiveData<Event<Int>>()
    val snackbarText: LiveData<Event<Int>> = _snackbarText
    private val _taskUpdatedEvent = MutableLiveData<Event<Unit>>()
    val taskUpdatedEvent: LiveData<Event<Unit>> = _taskUpdatedEvent
    private var taskId: String? = null
    private var isNewTask: Boolean = false
    private var isDataLoaded = false
    private var taskCompleted = false

    fun start(taskId: String?) {
        if (_dataLoading.value == true) {
            return
        }

        this.taskId = taskId
        if (taskId == null) {
            isNewTask = true
            return
        }
        if (isDataLoaded) {
            return
        }

        isNewTask = false
        _dataLoading.value = true

        viewModelScope.launch {
            tasksRepository.getTask(taskId).let { result ->
                if (result is Result.Success) {
                    onTaskLoaded(result.data)
                } else {
                    onDataNotAvailable()
                }
            }
        }
    }

    private fun onTaskLoaded(taskModel: TaskModel) {
        title.value = taskModel.title
        description.value = taskModel.description
        taskCompleted = taskModel.isCompleted
        _dataLoading.value = false
        isDataLoaded = true
    }

    private fun onDataNotAvailable() {
        _dataLoading.value = false
    }

    fun saveTask() {
        val currentTitle = title.value
        val currentDescription = description.value

        if (currentTitle == null || currentDescription == null) {
            _snackbarText.value =
                Event(R.string.empty_task_message)
            return
        }
        if (TaskModel(
                currentTitle,
                currentDescription
            ).isEmpty
        ) {
            _snackbarText.value =
                Event(R.string.empty_task_message)
            return
        }

        val currentTaskId = taskId
        if (isNewTask || currentTaskId == null) {
            createTask(
                TaskModel(
                    currentTitle,
                    currentDescription
                )
            )
        } else {
            val task = TaskModel(
                currentTitle,
                currentDescription,
                taskCompleted,
                currentTaskId
            )
            updateTask(task)
        }
    }

    private fun createTask(newTaskModel: TaskModel) = viewModelScope.launch {
        tasksRepository.saveTask(newTaskModel)
        _taskUpdatedEvent.value = Event(Unit)
    }

    private fun updateTask(taskModel: TaskModel) {
        if (isNewTask) {
            throw RuntimeException("updateTask() was called but task is new.")
        }
        viewModelScope.launch {
            tasksRepository.saveTask(taskModel)
            _taskUpdatedEvent.value = Event(Unit)
        }
    }
}