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

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import id.ac.esaunggul.todo.R
import id.ac.esaunggul.todo.databinding.AddTaskFragmentBinding
import id.ac.esaunggul.todo.ui.main.ADD_EDIT_RESULT_OK
import id.ac.esaunggul.todo.util.event.EventObserver
import id.ac.esaunggul.todo.util.extensions.setupSnackbar

@AndroidEntryPoint
class AddFragment : Fragment(R.layout.add_task_fragment) {

    private var _viewDataBinding: AddTaskFragmentBinding? = null
    private val viewDataBinding get() = _viewDataBinding!!

    private val args: AddFragmentArgs by navArgs()

    private val viewModel: AddViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        _viewDataBinding = AddTaskFragmentBinding.bind(view).apply {
            this.viewmodel = viewModel
        }

        viewDataBinding.lifecycleOwner = viewLifecycleOwner

        setupSnackbar(view)
        setupNavigation()

        viewModel.start(args.taskId)
    }

    private fun setupSnackbar(view: View) {
        view.setupSnackbar(viewLifecycleOwner, viewModel.snackbarText, Snackbar.LENGTH_SHORT)
    }

    private fun setupNavigation() {
        viewModel.taskUpdatedEvent.observe(viewLifecycleOwner,
            EventObserver {
                val action = AddFragmentDirections
                    .actionAddEditTaskFragmentToTasksFragment(ADD_EDIT_RESULT_OK)
                findNavController().navigate(action)
            })
    }

    override fun onDestroyView() {
        super.onDestroyView()

        _viewDataBinding = null
    }
}