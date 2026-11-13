package com.streakly.app.data.remote

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

interface StreaklyApi {
    // Auth
    @POST("api/auth/register")
    suspend fun register(@Body body: AuthRequest): AuthResponse

    @POST("api/auth/login")
    suspend fun login(@Body body: AuthRequest): AuthResponse

    @GET("api/auth/me")
    suspend fun me(): UserDto

    // Admin (solo rol Admin)
    @GET("api/admin/users/active")
    suspend fun activeUsers(@Query("days") days: Int): List<ActiveUserDto>

    // Habits
    @GET("api/habits")
    suspend fun habits(): List<HabitDto>

    @POST("api/habits")
    suspend fun createHabit(@Body body: CreateHabitRequest): HabitDto

    @DELETE("api/habits/{id}")
    suspend fun deleteHabit(@Path("id") id: String): Response<Unit>

    @PUT("api/habits/{id}/entries")
    suspend fun setEntry(@Path("id") id: String, @Body body: EntryRequest): EntryDto

    @DELETE("api/habits/{id}/entries/{date}")
    suspend fun deleteEntry(@Path("id") id: String, @Path("date") date: String): Response<Unit>

    @GET("api/habits/{id}/stats")
    suspend fun habitStats(
        @Path("id") id: String,
        @Query("date") date: String,
        @Query("weeks") weeks: Int,
    ): HabitStatsDto

    // Stats
    @GET("api/today")
    suspend fun today(@Query("date") date: String): TodayDto

    @GET("api/stats/compliance")
    suspend fun compliance(@Query("date") date: String, @Query("days") days: Int): List<ComplianceDto>

    @GET("api/stats/summary")
    suspend fun summary(@Query("date") date: String): SummaryDto

    // Goals
    @GET("api/goals")
    suspend fun goals(): List<GoalDto>

    @POST("api/goals")
    suspend fun createGoal(@Body body: CreateGoalRequest): GoalDto

    @POST("api/goals/{id}/tasks")
    suspend fun addTask(@Path("id") id: String, @Body body: AddTaskRequest): GoalDto

    @PATCH("api/goals/{id}/tasks/{taskId}/toggle")
    suspend fun toggleTask(@Path("id") id: String, @Path("taskId") taskId: String): GoalDto

    @DELETE("api/goals/{id}")
    suspend fun deleteGoal(@Path("id") id: String): Response<Unit>
}
