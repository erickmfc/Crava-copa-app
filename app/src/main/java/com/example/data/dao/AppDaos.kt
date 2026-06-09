package com.example.data.dao

import androidx.room.*
import com.example.data.model.*
import kotlinx.coroutines.flow.Flow

data class UserWithScore(
    val userId: String,
    val bolaoId: String,
    val nomeDeVidente: String,
    val avatarUrl: String,
    val pontosNoBolao: Int
)

@Dao
interface UserDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: User)

    @Update
    suspend fun updateUser(user: User)

    @Query("SELECT * FROM users WHERE id = :id LIMIT 1")
    fun getUserById(id: String): Flow<User?>

    @Query("SELECT * FROM users WHERE id = :id LIMIT 1")
    suspend fun getUserDirect(id: String): User?

    @Query("SELECT * FROM users ORDER BY pontosTotais DESC")
    fun getAllUsersSorted(): Flow<List<User>>
}

@Dao
interface BolaoDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBolao(bolao: Bolao)

    @Query("SELECT * FROM boloes WHERE id = :id LIMIT 1")
    fun getBolaoById(id: String): Flow<Bolao?>

    @Query("SELECT * FROM boloes WHERE codigoConvite = :codigo LIMIT 1")
    suspend fun getBolaoByCodigo(codigo: String): Bolao?

    @Query("SELECT * FROM boloes")
    fun getAllBoloes(): Flow<List<Bolao>>

    @Query("""
        SELECT b.* FROM boloes b
        INNER JOIN bolao_users bu ON b.id = bu.bolaoId
        WHERE bu.userId = :userId
    """)
    fun getBoloesForUser(userId: String): Flow<List<Bolao>>
}

@Dao
interface BolaoUserDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBolaoUser(bolaoUser: BolaoUser)

    @Query("DELETE FROM bolao_users WHERE bolaoId = :bolaoId AND userId = :userId")
    suspend fun removeUserFromBolao(bolaoId: String, userId: String)

    @Query("""
        SELECT u.id as userId, bu.bolaoId, u.nomeDeVidente, u.avatarUrl, bu.pontosNoBolao 
        FROM users u 
        INNER JOIN bolao_users bu ON u.id = bu.userId 
        WHERE bu.bolaoId = :bolaoId 
        ORDER BY bu.pontosNoBolao DESC
    """)
    fun getParticipantsWithScores(bolaoId: String): Flow<List<UserWithScore>>

    @Query("SELECT COUNT(*) FROM bolao_users WHERE bolaoId = :bolaoId")
    fun getParticipantCount(bolaoId: String): Flow<Int>
}

@Dao
interface JogoDao {
    @Query("SELECT * FROM jogos ORDER BY timestamp ASC")
    fun getAllJogos(): Flow<List<Jogo>>

    @Query("SELECT * FROM jogos ORDER BY timestamp ASC")
    suspend fun getJogosDirect(): List<Jogo>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertJogo(jogo: Jogo)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertJogos(jogos: List<Jogo>)

    @Update
    suspend fun updateJogo(jogo: Jogo)
}

@Dao
interface PalpiteDao {
    @Query("SELECT * FROM palpites WHERE userId = :userId")
    fun getPalpitesForUser(userId: String): Flow<List<Palpite>>

    @Query("SELECT * FROM palpites WHERE bolaoId = :bolaoId AND userId = :userId")
    fun getPalpitesForBolaoAndUser(bolaoId: String, userId: String): Flow<List<Palpite>>

    @Query("SELECT * FROM palpites WHERE bolaoId = :bolaoId AND userId = :userId AND jogoId = :jogoId LIMIT 1")
    suspend fun getSpecificPalpite(bolaoId: String, userId: String, jogoId: String): Palpite?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPalpite(palpite: Palpite)

    @Query("DELETE FROM palpites WHERE id = :palpiteId")
    suspend fun deletePalpiteById(palpiteId: String)
}
