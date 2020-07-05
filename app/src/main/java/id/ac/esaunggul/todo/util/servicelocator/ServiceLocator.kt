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
package id.ac.esaunggul.todo.util.servicelocator

import android.content.Context
import androidx.room.Room
import id.ac.esaunggul.todo.data.database.TodoDatabase
import id.ac.esaunggul.todo.data.repository.TaskDataRepository
import id.ac.esaunggul.todo.data.repository.TasksRepository
import id.ac.esaunggul.todo.data.source.TaskDataSource
import id.ac.esaunggul.todo.data.source.TaskLocalDataSource

/**
 * Simple service locator adequate for this demo app
 */
object ServiceLocator {
    private var database: TodoDatabase? = null
    var tasksRepository: TasksRepository? = null

    fun provideTasksRepository(context: Context): TasksRepository {
        synchronized(this) {
            return tasksRepository
                ?: tasksRepository
                ?: createTasksRepository(
                    context
                )
        }
    }

    private fun createTasksRepository(context: Context): TasksRepository {
        val newRepo = TaskDataRepository(
            createTaskLocalDataSource(
                context
            )
        )
        tasksRepository = newRepo
        return newRepo
    }

    private fun createTaskLocalDataSource(context: Context): TaskDataSource {
        val database = database
            ?: createDataBase(
                context
            )
        return TaskLocalDataSource(database.taskDao())
    }

    private fun createDataBase(context: Context): TodoDatabase {
        val result = Room.databaseBuilder(
            context.applicationContext,
            TodoDatabase::class.java, "Tasks.db"
        ).build()
        database = result
        return result
    }
}