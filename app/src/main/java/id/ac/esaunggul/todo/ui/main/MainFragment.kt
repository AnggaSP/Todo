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

import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import androidx.appcompat.widget.PopupMenu
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import id.ac.esaunggul.todo.R
import id.ac.esaunggul.todo.databinding.MainFragmentBinding
import id.ac.esaunggul.todo.util.event.EventObserver
import id.ac.esaunggul.todo.util.extensions.setupSnackbar
import id.ac.esaunggul.todo.util.state.TaskState

@AndroidEntryPoint
class MainFragment : Fragment(R.layout.main_fragment) {

    private val viewModel: MainViewModel by viewModels()

    private val args: MainFragmentArgs by navArgs()

    private var _viewDataBinding: MainFragmentBinding? = null
    private val viewDataBinding get() = _viewDataBinding!!

    private lateinit var listAdapter: MainAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        _viewDataBinding = MainFragmentBinding.bind(view).apply {
            this.viewmodel = viewModel
        }

        viewDataBinding.lifecycleOwner = viewLifecycleOwner

        setHasOptionsMenu(true)

        setupSnackbar(view)
        setupListAdapter()
        setupNavigation()
        setupFab()
    }

    override fun onOptionsItemSelected(item: MenuItem) =
        when (item.itemId) {
            R.id.menu_clear -> {
                viewModel.clearCompletedTasks()
                true
            }
            R.id.menu_filter -> {
                showFilteringPopUpMenu()
                true
            }
            else -> false
        }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.tasks_fragment_menu, menu)
    }

    private fun setupNavigation() {
        viewModel.openTaskEvent.observe(viewLifecycleOwner,
            EventObserver {
                openTaskDetails(it)
            })
        viewModel.newTaskEvent.observe(viewLifecycleOwner,
            EventObserver {
                navigateToAddNewTask()
            })
    }

    private fun setupSnackbar(view: View) {
        view.setupSnackbar(viewLifecycleOwner, viewModel.snackbarText, Snackbar.LENGTH_SHORT)
        arguments?.let {
            viewModel.showEditResultMessage(args.userMessage)
        }
    }

    private fun showFilteringPopUpMenu() {
        val view = requireActivity().findViewById<View>(R.id.menu_filter) ?: return
        PopupMenu(requireContext(), view).run {
            menuInflater.inflate(R.menu.filter_tasks, menu)

            setOnMenuItemClickListener {
                viewModel.setFiltering(
                    when (it.itemId) {
                        R.id.active -> TaskState.ACTIVE_TASKS
                        R.id.completed -> TaskState.COMPLETED_TASKS
                        else -> TaskState.ALL_TASKS
                    }
                )
                true
            }
            show()
        }
    }

    private fun setupFab() {
        requireActivity().findViewById<FloatingActionButton>(R.id.add_task_fab)?.let {
            it.setOnClickListener {
                navigateToAddNewTask()
            }
        }
    }

    private fun navigateToAddNewTask() {
        val action = MainFragmentDirections
            .actionTasksFragmentToAddEditTaskFragment(
                null,
                resources.getString(R.string.add_task)
            )
        findNavController().navigate(action)
    }

    private fun openTaskDetails(taskId: String) {
        val action = MainFragmentDirections.actionTasksFragmentToTaskDetailFragment(taskId)
        findNavController().navigate(action)
    }

    private fun setupListAdapter() {
        val viewModel = viewDataBinding.viewmodel
        if (viewModel != null) {
            listAdapter = MainAdapter(viewModel)
            viewDataBinding.tasksList.adapter = listAdapter
        }
    }
}