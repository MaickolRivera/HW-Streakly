package com.streakly.app.data

import com.streakly.app.data.local.TokenStore
import com.streakly.app.data.remote.ActiveUserDto
import com.streakly.app.data.remote.AddTaskRequest
import com.streakly.app.data.remote.AuthRequest
import com.streakly.app.data.remote.ComplianceDto
import com.streakly.app.data.remote.CreateGoalRequest
import com.streakly.app.data.remote.CreateHabitRequest
import com.streakly.app.data.remote.EntryRequest
import com.streakly.app.data.remote.GoalDto
import com.streakly.app.data.remote.HabitDto
import com.streakly.app.data.remote.HabitStatsDto
import com.streakly.app.data.remote.StreaklyApi
import com.streakly.app.data.remote.SummaryDto
import com.streakly.app.data.remote.TodayDto
import com.streakly.app.data.remote.UserDto
import com.streakly.app.data.remote.apiCall
import com.streakly.app.data.remote.apiCallUnit
import java.time.LocalDate

class AuthRepository(private val api: StreaklyApi, private val tokenStore: TokenStore) {

    val token = tokenStore.token
    val email = tokenStore.email

    suspend fun login(email: String, password: String): Result<Unit> =
        apiCall { api.login(AuthRequest(email.trim(), password)) }
            .map { tokenStore.save(it.token, it.user.email) }

    suspend fun register(email: String, password: String): Result<Unit> =
        apiCall { api.register(AuthRequest(email.trim(), password)) }
            .map { tokenStore.save(it.token, it.user.email) }

    suspend fun me(): Result<UserDto> = apiCall { api.me() }

    suspend fun logout() = tokenStore.clear()
}

class AdminRepository(private val api: StreaklyApi) {

    suspend fun activeUsers(days: Int = 7): Result<List<ActiveUserDto>> = apiCall { api.activeUsers(days) }
}

class HabitRepository(private val api: StreaklyApi) {

    // Se envia la fecha local del telefono para que "hoy" sea el del usuario.
    private fun localDate() = LocalDate.now().toString()

    suspend fun today(): Result<TodayDto> = apiCall { api.today(localDate()) }

    suspend fun habits(): Result<List<HabitDto>> = apiCall { api.habits() }

    suspend fun create(request: CreateHabitRequest): Result<HabitDto> = apiCall { api.createHabit(request) }

    suspend fun delete(id: String): Result<Unit> = apiCallUnit { api.deleteHabit(id) }

    /** value <= 0 borra el registro del dia (desmarcar / contador en cero). */
    suspend fun setTodayValue(habitId: String, value: Double): Result<Unit> =
        if (value <= 0) apiCallUnit { api.deleteEntry(habitId, localDate()) }
        else apiCall { api.setEntry(habitId, EntryRequest(localDate(), value)) }.map { }

    suspend fun stats(habitId: String, weeks: Int = 18): Result<HabitStatsDto> =
        apiCall { api.habitStats(habitId, localDate(), weeks) }

    suspend fun compliance(days: Int = 30): Result<List<ComplianceDto>> = apiCall { api.compliance(localDate(), days) }

    suspend fun summary(): Result<SummaryDto> = apiCall { api.summary(localDate()) }
}

class GoalRepository(private val api: StreaklyApi) {

    suspend fun goals(): Result<List<GoalDto>> = apiCall { api.goals() }

    suspend fun create(request: CreateGoalRequest): Result<GoalDto> = apiCall { api.createGoal(request) }

    suspend fun addTask(goalId: String, title: String): Result<GoalDto> =
        apiCall { api.addTask(goalId, AddTaskRequest(title)) }

    suspend fun toggleTask(goalId: String, taskId: String): Result<GoalDto> =
        apiCall { api.toggleTask(goalId, taskId) }

    suspend fun delete(goalId: String): Result<Unit> = apiCallUnit { api.deleteGoal(goalId) }
}
