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
package id.ac.esaunggul.todo.ui.main

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.hilt.Assisted
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import androidx.lifecycle.distinctUntilChanged
import androidx.lifecycle.switchMap
import androidx.lifecycle.viewModelScope
import id.ac.esaunggul.todo.R
import id.ac.esaunggul.todo.data.model.TaskModel
import id.ac.esaunggul.todo.data.repository.TasksRepository
import id.ac.esaunggul.todo.util.event.Event
import id.ac.esaunggul.todo.util.state.Result
import id.ac.esaunggul.todo.util.state.TaskState
import kotlinx.coroutines.launch

class MainViewModel
@ViewModelInject
constructor(
    private val tasksRepository: TasksRepository,
    @Assisted private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _forceUpdate = MutableLiveData<Boolean>(false)

    private val _items: LiveData<List<TaskModel>> = _forceUpdate.switchMap { _ ->
        tasksRepository.observeTasks().distinctUntilChanged().switchMap { filterTasks(it) }
    }

    val items: LiveData<List<TaskModel>> = _items

    private val _currentFilteringLabel = MutableLiveData<Int>()
    val currentFilteringLabel: LiveData<Int> = _currentFilteringLabel

    private val _noTasksLabel = MutableLiveData<Int>()
    val noTasksLabel: LiveData<Int> = _noTasksLabel

    private val _noTaskIconRes = MutableLiveData<Int>()
    val noTaskIconRes: LiveData<Int> = _noTaskIconRes

    private val _tasksAddViewVisible = MutableLiveData<Boolean>()

    private val _snackbarText = MutableLiveData<Event<Int>>()
    val snackbarText: LiveData<Event<Int>> = _snackbarText

    private val isDataLoadingError = MutableLiveData<Boolean>()

    private val _openTaskEvent = MutableLiveData<Event<String>>()
    val openTaskEvent: LiveData<Event<String>> = _openTaskEvent

    private val _newTaskEvent = MutableLiveData<Event<Unit>>()
    val newTaskEvent: LiveData<Event<Unit>> = _newTaskEvent

    private var resultMessageShown: Boolean = false

    val empty: LiveData<Boolean> = Transformations.map(_items) {
        it.isEmpty()
    }

    init {
        setFiltering(getSavedFilterType())
        loadTasks(true)
    }

    fun setFiltering(requestType: TaskState) {
        savedStateHandle.set(FILTER_SAVED_STATE_KEY, requestType)

        when (requestType) {
            TaskState.ALL_TASKS -> {
                setFilter(
                    R.string.label_all, R.string.no_tasks_all,
                    R.drawable.ic_check_circle_96dp, true
                )
            }
            TaskState.ACTIVE_TASKS -> {
                setFilter(
                    R.string.label_active, R.string.no_tasks_active,
                    R.drawable.ic_check_circle_96dp, false
                )
            }
            TaskState.COMPLETED_TASKS -> {
                setFilter(
                    R.string.label_completed, R.string.no_tasks_completed,
                    R.drawable.ic_verified_user_96dp, false
                )
            }
        }
        loadTasks(false)
    }

    private fun setFilter(
        @StringRes filteringLabelString: Int,
        @StringRes noTasksLabelString: Int,
        @DrawableRes noTaskIconDrawable: Int,
        tasksAddVisible: Boolean
    ) {
        _currentFilteringLabel.value = filteringLabelString
        _noTasksLabel.value = noTasksLabelString
        _noTaskIconRes.value = noTaskIconDrawable
        _tasksAddViewVisible.value = tasksAddVisible
    }

    fun clearCompletedTasks() {
        viewModelScope.launch {
            tasksRepository.clearCompletedTasks()
            showSnackbarMessage(R.string.completed_tasks_cleared)
        }
    }

    fun completeTask(taskModel: TaskModel, completed: Boolean) = viewModelScope.launch {
        if (completed) {
            tasksRepository.completeTask(taskModel)
            showSnackbarMessage(R.string.task_marked_complete)
        } else {
            tasksRepository.activateTask(taskModel)
            showSnackbarMessage(R.string.task_marked_active)
        }
    }

    fun openTask(taskId: String) {
        _openTaskEvent.value = Event(taskId)
    }

    fun showEditResultMessage(result: Int) {
        if (resultMessageShown) return
        when (result) {
            EDIT_RESULT_OK -> showSnackbarMessage(R.string.successfully_saved_task_message)
            ADD_EDIT_RESULT_OK -> showSnackbarMessage(R.string.successfully_added_task_message)
            DELETE_RESULT_OK -> showSnackbarMessage(R.string.successfully_deleted_task_message)
        }
        resultMessageShown = true
    }

    private fun showSnackbarMessage(message: Int) {
        _snackbarText.value = Event(message)
    }

    private fun filterTasks(tasksResult: Result<List<TaskModel>>): LiveData<List<TaskModel>> {
        val result = MutableLiveData<List<TaskModel>>()

        if (tasksResult is Result.Success) {
            isDataLoadingError.value = false
            viewModelScope.launch {
                result.value = filterItems(tasksResult.data, getSavedFilterType())
            }
        } else {
            result.value = emptyList()
            showSnackbarMessage(R.string.loading_tasks_error)
            isDataLoadingError.value = true
        }

        return result
    }

    fun loadTasks(forceUpdate: Boolean) {
        _forceUpdate.value = forceUpdate
    }

    private fun filterItems(
        taskModels: List<TaskModel>,
        filteringType: TaskState
    ): List<TaskModel> {
        val tasksToShow = ArrayList<TaskModel>()
        for (task in taskModels) {
            when (filteringType) {
                TaskState.ALL_TASKS -> tasksToShow.add(task)
                TaskState.ACTIVE_TASKS -> if (task.isActive) {
                    tasksToShow.add(task)
                }
                TaskState.COMPLETED_TASKS -> if (task.isCompleted) {
                    tasksToShow.add(task)
                }
            }
        }
        return tasksToShow
    }

    private fun getSavedFilterType(): TaskState {
        return savedStateHandle.get(FILTER_SAVED_STATE_KEY) ?: TaskState.ALL_TASKS
    }
}

const val FILTER_SAVED_STATE_KEY = "TASKS_FILTER_SAVED_STATE_KEY"