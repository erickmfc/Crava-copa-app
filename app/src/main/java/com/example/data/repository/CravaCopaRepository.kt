package com.example.data.repository

import com.example.data.dao.*
import com.example.data.model.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.UUID

class CravaCopaRepository(private val db: com.example.data.database.AppDatabase) {

    private val userDao = db.userDao()
    private val bolaoDao = db.bolaoDao()
    private val bolaoUserDao = db.bolaoUserDao()
    private val jogoDao = db.jogoDao()
    private val palpiteDao = db.palpiteDao()

    // Keep track of the currently logged-in user reactively
    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser.asStateFlow()

    init {
        // By default, let's auto-login as "Lucas C." so they don't have to register on first launch,
        // but have full access to SignUp/Login screens!
        // We will perform a suspend check in our ViewModel or setup.
    }

    suspend fun autoLoginDefault() {
        // Enforce latest match formats with stadiums and corrected schedules
        val currentJogos = jogoDao.getJogosDirect()
        val updatedJogos = currentJogos.map { j ->
            when (j.id) {
                "j_1" -> j.copy(dataHoraStr = "12/06 às 20:00 (MetLife Stadium)")
                "j_2" -> j.copy(dataHoraStr = "13/06 às 16:00 (AT&T Stadium)")
                "j_3" -> j.copy(dataHoraStr = "14/06 às 16:00 (Hard Rock Stadium)")
                else -> j
            }
        }
        if (updatedJogos.isNotEmpty()) {
            jogoDao.insertJogos(updatedJogos)
        }

        val defaultUser = userDao.getUserDirect("u_lucas")
        if (defaultUser != null) {
            _currentUser.emit(defaultUser)
        }
    }

    suspend fun loginUser(email: String): Boolean {
        // Find existing user in mock pool
        val users = listOf("u_rafael", "u_juliana", "u_lucas", "u_pedro", "u_amanda")
        for (uid in users) {
            val u = userDao.getUserDirect(uid)
            if (u != null && u.email.lowercase() == email.lowercase().trim()) {
                _currentUser.emit(u)
                return true
            }
        }
        
        // If not found, let's search in the database generally or create a simple mock user
        _currentUser.emit(
            User(
                id = UUID.randomUUID().toString(),
                email = email,
                nomeDeVidente = email.substringBefore("@"),
                avatarUrl = "https://images.unsplash.com/photo-1535713875002-d1d0cf377fde?auto=format&fit=crop&w=150&h=150&q=80",
                pontosTotais = 0
            ).also { userDao.insertUser(it) }
        )
        return true
    }

    suspend fun registerNewUser(email: String, nomeDeVidente: String): User {
        val newUser = User(
            id = UUID.randomUUID().toString(),
            email = email,
            nomeDeVidente = nomeDeVidente,
            avatarUrl = "https://images.unsplash.com/photo-1534528741775-53994a69daeb?auto=format&fit=crop&w=150&h=150&q=80",
            pontosTotais = 0
        )
        userDao.insertUser(newUser)
        _currentUser.emit(newUser)
        return newUser
    }

    suspend fun logout() {
        _currentUser.emit(null)
    }

    // --- User Operations ---
    fun getAllUsersSorted(): Flow<List<User>> = userDao.getAllUsersSorted()
    fun getUserById(id: String): Flow<User?> = userDao.getUserById(id)
    suspend fun updateUser(user: User) {
        userDao.updateUser(user)
        if (_currentUser.value?.id == user.id) {
            _currentUser.emit(user)
        }
    }

    // --- Bolão Operations ---
    fun getAllBoloes(): Flow<List<Bolao>> = bolaoDao.getAllBoloes()
    fun getBoloesForCurrentUser(userId: String): Flow<List<Bolao>> = bolaoDao.getBoloesForUser(userId)
    fun getBolaoById(id: String): Flow<Bolao?> = bolaoDao.getBolaoById(id)

    suspend fun createBolao(nome: String, userId: String): Bolao {
        val code = (1000..9999).random().toString()
        val initials = nome.take(2).uppercase()
        val inviteCode = "$initials$code"
        
        val newBolao = Bolao(
            id = UUID.randomUUID().toString(),
            nome = nome,
            codigoConvite = inviteCode,
            adminId = userId,
            bannerColor = listOf("#059669", "#B91C1C", "#1D4ED8", "#4D7C0F", "#7C3AED", "#DB2777").random()
        )
        bolaoDao.insertBolao(newBolao)
        
        // Autojoin creator
        joinBolaoByCode(inviteCode, userId)
        return newBolao
    }

    suspend fun joinBolaoByCode(code: String, userId: String): Bolao? {
        val bolao = bolaoDao.getBolaoByCodigo(code.uppercase().trim())
        if (bolao != null) {
            val membership = BolaoUser(
                bolaoId = bolao.id,
                userId = userId,
                pontosNoBolao = 0
            )
            bolaoUserDao.insertBolaoUser(membership)
            return bolao
        }
        return null
    }

    suspend fun leaveBolao(bolaoId: String, userId: String) {
        bolaoUserDao.removeUserFromBolao(bolaoId, userId)
    }

    // --- Bolão Member Scores ---
    fun getBolaoParticipants(bolaoId: String): Flow<List<UserWithScore>> {
        return bolaoUserDao.getParticipantsWithScores(bolaoId)
    }

    fun getBolaoParticipantCount(bolaoId: String): Flow<Int> {
        return bolaoUserDao.getParticipantCount(bolaoId)
    }

    // --- Jogo (Match) Operations ---
    fun getAllJogos(): Flow<List<Jogo>> = jogoDao.getAllJogos()
    suspend fun insertMatch(jogo: Jogo) = jogoDao.insertJogo(jogo)

    // --- Palpites (Guesses) Operations ---
    fun getPalpitesForUser(userId: String): Flow<List<Palpite>> = palpiteDao.getPalpitesForUser(userId)
    
    fun getPalpitesForBolaoAndUser(bolaoId: String, userId: String): Flow<List<Palpite>> {
        return palpiteDao.getPalpitesForBolaoAndUser(bolaoId, userId)
    }

    suspend fun submitPalpite(
        userId: String,
        jogoId: String,
        bolaoId: String,
        palpiteCasa: Int,
        palpiteFora: Int,
        usouZica: Boolean
    ) {
        // Check if exists
        val existing = palpiteDao.getSpecificPalpite(bolaoId, userId, jogoId)
        val id = existing?.id ?: UUID.randomUUID().toString()
        
        val palpite = Palpite(
            id = id,
            userId = userId,
            jogoId = jogoId,
            bolaoId = bolaoId,
            palpiteCasa = palpiteCasa,
            palpiteFora = palpiteFora,
            usouZica = usouZica,
            pontosGanhos = 0 // Initially 0
        )
        palpiteDao.insertPalpite(palpite)
    }
}
