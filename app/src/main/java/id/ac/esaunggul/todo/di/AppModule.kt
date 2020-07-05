/*
 * Copyright (C) 2019 The Android Open Source Project
 * Copyright (C) 2020 Angga Satya Putra
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

package id.ac.esaunggul.todo.di

import android.content.Context
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ApplicationComponent
import dagger.hilt.android.qualifiers.ApplicationContext
import id.ac.esaunggul.todo.data.database.TodoDatabase
import id.ac.esaunggul.todo.data.repository.TaskDataRepository
import id.ac.esaunggul.todo.data.repository.TasksRepository
import id.ac.esaunggul.todo.data.source.TaskDataSource
import id.ac.esaunggul.todo.data.source.TaskLocalDataSource
import javax.inject.Singleton

@Module
@InstallIn(ApplicationComponent::class)
object AppModule {

    @Singleton
    @Provides
    fun provideDatabase(@ApplicationContext context: Context): TodoDatabase {
        return Room.databaseBuilder(
            context.applicationContext,
            TodoDatabase::class.java,
            "Task.db"
        ).build()
    }

    @Singleton
    @Provides
    fun provideTaskLocalDataSource(database: TodoDatabase): TaskDataSource {
        return TaskLocalDataSource(database.taskDao())
    }

    @Singleton
    @Provides
    fun provideTaskDataRepository(taskLocalDataSource: TaskDataSource): TasksRepository {
        return TaskDataRepository(
            taskLocalDataSource
        )
    }
}