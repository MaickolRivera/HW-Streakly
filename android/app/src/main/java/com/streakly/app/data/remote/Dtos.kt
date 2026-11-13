package com.streakly.app.data.remote

import kotlinx.serialization.Serializable

// ---------- Auth ----------
@Serializable
data class AuthRequest(val email: String, val password: String)

@Serializable
data class UserDto(val id: String, val email: String, val createdAt: String, val role: String = UserRole.USER) {
    val isAdmin: Boolean get() = role == UserRole.ADMIN
}

object UserRole {
    const val USER = "User"
    const val ADMIN = "Admin"
}

// ---------- Admin ----------
@Serializable
data class ActiveUserDto(val id: String, val email: String, val createdAt: String, val lastActiveAt: String)

@Serializable
data class AuthResponse(val token: String, val expiresAt: String, val user: UserDto)

// ---------- Habits ----------
object HabitType {
    const val BOOLEAN = "Boolean"
    const val COUNTER = "Counter"
}

@Serializable
data class HabitDto(
    val id: String,
    val name: String,
    val icon: String,
    val type: String,
    val targetValue: Double,
    val unit: String,
    val days: List<Int>,
    val reminderTime: String? = null,
    val createdAt: String,
)

@Serializable
data class CreateHabitRequest(
    val name: String,
    val icon: String,
    val type: String,
    val targetValue: Double,
    val unit: String,
    val days: List<Int>,
    val reminderTime: String? = null,
)

@Serializable
data class EntryRequest(val date: String, val value: Double)

@Serializable
data class EntryDto(val id: String, val date: String, val value: Double)

@Serializable
data class TodayHabitDto(
    val id: String,
    val name: String,
    val icon: String,
    val type: String,
    val targetValue: Double,
    val unit: String,
    val value: Double,
    val completed: Boolean,
    val currentStreak: Int,
)

@Serializable
data class TodayDto(
    val date: String,
    val globalStreak: Int,
    val completed: Int,
    val total: Int,
    val habits: List<TodayHabitDto>,
)

@Serializable
data class HeatmapDayDto(val date: String, val level: Int, val scheduled: Boolean)

@Serializable
data class HabitStatsDto(
    val habitId: String,
    val name: String,
    val currentStreak: Int,
    val bestStreak: Int,
    val compliancePercent: Int,
    val completedDays: Int,
    val scheduledDays: Int,
    val weeks: Int,
    val heatmap: List<HeatmapDayDto>,
)

@Serializable
data class ComplianceDto(val habitId: String, val name: String, val icon: String, val percent: Int)

@Serializable
data class SummaryDto(
    val globalStreak: Int,
    val bestGlobalStreak: Int,
    val totalHabits: Int,
    val totalCompletions: Int,
)

// ---------- Goals ----------
@Serializable
data class GoalTaskDto(val id: String, val title: String, val isCompleted: Boolean)

@Serializable
data class GoalDto(
    val id: String,
    val title: String,
    val description: String? = null,
    val dueDate: String? = null,
    val createdAt: String,
    val completedTasks: Int,
    val totalTasks: Int,
    val progress: Int,
    val tasks: List<GoalTaskDto>,
)

@Serializable
data class CreateGoalRequest(
    val title: String,
    val description: String?,
    val dueDate: String?,
    val tasks: List<String>,
)

@Serializable
data class AddTaskRequest(val title: String)

// ---------- Errores (ProblemDetails de ASP.NET) ----------
@Serializable
data class ProblemDetails(val title: String? = null, val detail: String? = null, val status: Int? = null)
