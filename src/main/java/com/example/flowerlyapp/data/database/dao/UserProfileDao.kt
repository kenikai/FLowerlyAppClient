package com.example.flowerlyapp.data.database.dao

import androidx.room.*
import com.example.flowerlyapp.data.database.entities.UserProfileEntity
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object для работы с профилями пользователей
 */
@Dao
interface UserProfileDao {
    
    @Query("SELECT * FROM user_profiles WHERE userId = :userId")
    suspend fun getUserProfile(userId: String): UserProfileEntity?
    
    @Query("SELECT * FROM user_profiles WHERE userId = :userId")
    fun getUserProfileFlow(userId: String): Flow<UserProfileEntity?>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUserProfile(userProfile: UserProfileEntity)
    
    @Update
    suspend fun updateUserProfile(userProfile: UserProfileEntity)
    
    @Delete
    suspend fun deleteUserProfile(userProfile: UserProfileEntity)
    
    @Query("DELETE FROM user_profiles WHERE userId = :userId")
    suspend fun deleteUserProfileByUserId(userId: String)
    
    @Query("DELETE FROM user_profiles WHERE id = :profileId")
    suspend fun deleteUserProfileById(profileId: String)
    
    @Query("UPDATE user_profiles SET phoneNumber = :phoneNumber, updatedAt = :updatedAt WHERE userId = :userId")
    suspend fun updatePhoneNumber(userId: String, phoneNumber: String, updatedAt: Long)
    
    @Query("UPDATE user_profiles SET address = :address, city = :city, postalCode = :postalCode, updatedAt = :updatedAt WHERE userId = :userId")
    suspend fun updateAddress(userId: String, address: String, city: String, postalCode: String, updatedAt: Long)
    
    @Query("UPDATE user_profiles SET avatarUrl = :avatarUrl, updatedAt = :updatedAt WHERE userId = :userId")
    suspend fun updateAvatar(userId: String, avatarUrl: String, updatedAt: Long)
    
    @Query("UPDATE user_profiles SET isEmailNotificationsEnabled = :enabled, updatedAt = :updatedAt WHERE userId = :userId")
    suspend fun updateEmailNotifications(userId: String, enabled: Boolean, updatedAt: Long)
    
    @Query("UPDATE user_profiles SET isSmsNotificationsEnabled = :enabled, updatedAt = :updatedAt WHERE userId = :userId")
    suspend fun updateSmsNotifications(userId: String, enabled: Boolean, updatedAt: Long)
    
    @Query("UPDATE user_profiles SET isPushNotificationsEnabled = :enabled, updatedAt = :updatedAt WHERE userId = :userId")
    suspend fun updatePushNotifications(userId: String, enabled: Boolean, updatedAt: Long)
}
