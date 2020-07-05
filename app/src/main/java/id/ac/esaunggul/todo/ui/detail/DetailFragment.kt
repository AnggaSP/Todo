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

import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.google.android.material.snackbar.Snackbar
import id.ac.esaunggul.todo.R
import id.ac.esaunggul.todo.databinding.TaskDetailFragmentBinding
import id.ac.esaunggul.todo.ui.main.DELETE_RESULT_OK
import id.ac.esaunggul.todo.util.event.EventObserver
import id.ac.esaunggul.todo.util.extensions.getViewModelFactory
import id.ac.esaunggul.todo.util.extensions.setupSnackbar

class DetailFragment : Fragment(R.layout.task_detail_fragment) {

    private var _viewDataBinding: TaskDetailFragmentBinding? = null
    private val viewDataBinding get() = _viewDataBinding!!

    private val args: DetailFragmentArgs by navArgs()

    private val viewModel by viewModels<DetailViewModel> { getViewModelFactory() }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        _viewDataBinding = TaskDetailFragmentBinding.bind(view).apply {
            viewmodel = viewModel
        }

        viewDataBinding.lifecycleOwner = viewLifecycleOwner

        setHasOptionsMenu(true)

        setupFab()
        setupSnackbar(view)
        setupNavigation()

        viewModel.start(args.taskId)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.menu_delete -> {
                viewModel.deleteTask()
                true
            }
            else -> false
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.task_detail_fragment_menu, menu)
    }

    private fun setupSnackbar(view: View) {
        view.setupSnackbar(viewLifecycleOwner, viewModel.snackbarText, Snackbar.LENGTH_SHORT)
    }

    private fun setupNavigation() {
        viewModel.deleteTaskEvent.observe(viewLifecycleOwner,
            EventObserver {
                val action = DetailFragmentDirections
                    .actionTaskDetailFragmentToTasksFragment(DELETE_RESULT_OK)
                findNavController().navigate(action)
            })
        viewModel.editTaskEvent.observe(viewLifecycleOwner,
            EventObserver {
                val action = DetailFragmentDirections
                    .actionTaskDetailFragmentToAddEditTaskFragment(
                        args.taskId,
                        resources.getString(R.string.edit_task)
                    )
                findNavController().navigate(action)
            })
    }

    private fun setupFab() {
        requireActivity().findViewById<View>(R.id.edit_task_fab)?.setOnClickListener {
            viewModel.editTask()
        }
    }
}