package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.database.AppDatabase
import com.example.data.model.*
import com.example.data.dao.UserWithScore
import com.example.data.repository.CravaCopaRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.io.Serializable

enum class Screen {
    SIGN_UP,
    LOGIN,
    MAIN_HUB,
    BOLAO_DETAILS
}

enum class HomeTab {
    INICIO,
    BOLOES,
    APOSTAS,
    RANKING,
    PERFIL
}

@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
class CravaCopaViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getDatabase(application)
    private val repository = CravaCopaRepository(db)

    // Current Reactively Auth'd User
    val currentUser: StateFlow<User?> = repository.currentUser

    // High Level Navigation State
    private val _currentScreen = MutableStateFlow(Screen.MAIN_HUB) // Default, will auto-auth inside MainActivity
    val currentScreen: StateFlow<Screen> = _currentScreen.asStateFlow()

    private val _currentTab = MutableStateFlow(HomeTab.INICIO)
    val currentTab: StateFlow<HomeTab> = _currentTab.asStateFlow()

    // Active inspection elements
    private val _selectedBolao = MutableStateFlow<Bolao?>(null)
    val selectedBolao: StateFlow<Bolao?> = _selectedBolao.asStateFlow()

    val selectedBolaoParticipants: Flow<List<UserWithScore>> = _selectedBolao
        .filterNotNull()
        .flatMapLatest { bolao -> 
            repository.getBolaoParticipants(bolao.id)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val selectedBolaoParticipantCount: Flow<Int> = _selectedBolao
        .filterNotNull()
        .flatMapLatest { bolao -> 
            repository.getBolaoParticipantCount(bolao.id)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    // Match selected for creating / placing bets
    private val _selectedMatchForBet = MutableStateFlow<Jogo?>(null)
    val selectedMatchForBet: StateFlow<Jogo?> = _selectedMatchForBet.asStateFlow()

    // Global Collections
    val allGames: StateFlow<List<Jogo>> = repository.getAllJogos()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allBoloes: StateFlow<List<Bolao>> = repository.getAllBoloes()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val globalLeaderboard: StateFlow<List<User>> = repository.getAllUsersSorted()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Currently joined bolões
    val joinedBoloes: StateFlow<List<Bolao>> = currentUser
        .filterNotNull()
        .flatMapLatest { user ->
            repository.getBoloesForCurrentUser(user.id)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // All user guesses
    val userPalpites: StateFlow<List<Palpite>> = currentUser
        .flatMapLatest { user ->
            if (user != null) repository.getPalpitesForUser(user.id) else flowOf(emptyList())
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Track sabotaged userIds by the active user (Modo Zica feature)
    private val _activeZicas = MutableStateFlow<Set<String>>(setOf("u_pedro"))
    val activeZicas: StateFlow<Set<String>> = _activeZicas.asStateFlow()

    fun toggleZicaOnCompetitor(competitorId: String) {
        val currentSet = _activeZicas.value
        if (currentSet.contains(competitorId)) {
            _activeZicas.value = currentSet - competitorId
            showToast("Zica desfeita! Competidor recuperou a paz.")
        } else {
            if (currentSet.size >= 2) {
                showToast("Erro! Você só tem 2 Zicas disponíveis por rodada.")
            } else {
                _activeZicas.value = currentSet + competitorId
                showToast("💀 Zica Lançada! O palpite dele foi sabotado!")
            }
        }
    }

    // Feedbacks & Transient Status UI Messages
    private val _toastMessage = MutableStateFlow<String?>(null)
    val toastMessage: StateFlow<String?> = _toastMessage.asStateFlow()

    init {
        viewModelScope.launch {
            // Check if we need to auto-activate a session on first run
            repository.autoLoginDefault()
            // If we have a user logged-in, start in MAIN_HUB
            if (repository.currentUser.value == null) {
                _currentScreen.value = Screen.SIGN_UP
            } else {
                _currentScreen.value = Screen.MAIN_HUB
            }
        }
    }

    // --- Action Methods ---

    fun showToast(msg: String) {
        _toastMessage.value = msg
        viewModelScope.launch {
            kotlinx.coroutines.delay(2000)
            if (_toastMessage.value == msg) {
                _toastMessage.value = null
            }
        }
    }

    fun navigateTo(screen: Screen) {
        _currentScreen.value = screen
    }

    fun selectTab(tab: HomeTab) {
        _currentTab.value = tab
    }

    fun selectBolao(bolao: Bolao?) {
        _selectedBolao.value = bolao
        if (bolao != null) {
            navigateTo(Screen.BOLAO_DETAILS)
        }
    }

    fun selectMatchForBet(jogo: Jogo?) {
        _selectedMatchForBet.value = jogo
    }

    // --- Auth Actions ---

    fun login(email: String) {
        if (email.isBlank() || !email.contains("@")) {
            showToast("Insira um e-mail válido!")
            return
        }
        viewModelScope.launch {
            val success = repository.loginUser(email)
            if (success) {
                showToast("Bem-vindo de volta!")
                navigateTo(Screen.MAIN_HUB)
            } else {
                showToast("Erro ao fazer login.")
            }
        }
    }

    fun signUp(email: String, name: String, passwordConfirm: String) {
        if (email.isBlank() || !email.contains("@")) {
            showToast("Insira um e-mail válido!")
            return
        }
        if (name.isBlank() || name.length < 3) {
            showToast("Sua identidade de craque deve ter ao menos 3 letras!")
            return
        }
        if (passwordConfirm.isBlank() || passwordConfirm.length < 4) {
            showToast("Insira uma senha válida (mínimo 4 caracteres)!")
            return
        }
        viewModelScope.launch {
            repository.registerNewUser(email.trim(), name.trim())
            showToast("Cadastro finalizado! Bem-vindo de vidente.")
            navigateTo(Screen.MAIN_HUB)
        }
    }

    fun logout() {
        viewModelScope.launch {
            repository.logout()
            navigateTo(Screen.SIGN_UP)
        }
    }

    fun updateUser(user: User) {
        viewModelScope.launch {
            repository.updateUser(user)
        }
    }

    // --- Sweeptakes (Bolão) Actions ---

    fun createBolao(nome: String) {
        val user = currentUser.value ?: return
        if (nome.isBlank() || nome.length < 3) {
            showToast("Nome do bolão deve ter no mínimo 3 caracteres!")
            return
        }
        viewModelScope.launch {
            val newBolao = repository.createBolao(nome.trim(), user.id)
            showToast("Bolão '${newBolao.nome}' criado com sucesso!")
            selectBolao(newBolao)
        }
    }

    fun joinBolaoByCode(inviteCode: String) {
        val user = currentUser.value ?: return
        if (inviteCode.isBlank()) {
            showToast("Insira o código de convite!")
            return
        }
        viewModelScope.launch {
            val joined = repository.joinBolaoByCode(inviteCode.uppercase().trim(), user.id)
            if (joined != null) {
                showToast("Você entrou no bolão '${joined.nome}'!")
                selectBolao(joined)
            } else {
                showToast("Código de convite inválido ou bolão não encontrado.")
            }
        }
    }

    fun leaveBolao(bolaoId: String) {
        val user = currentUser.value ?: return
        viewModelScope.launch {
            repository.leaveBolao(bolaoId, user.id)
            showToast("Você saiu do bolão!")
            _selectedBolao.value = null
            navigateTo(Screen.MAIN_HUB)
            selectTab(HomeTab.BOLOES)
        }
    }

    // --- Palpite (Bet) Submission ---

    fun createPalpite(jogoId: String, palpiteCasa: Int, palpiteFora: Int, usouZica: Boolean) {
        val user = currentUser.value ?: return
        val bolao = selectedBolao.value ?: joinedBoloes.value.firstOrNull()
        
        if (bolao == null) {
            showToast("Crie ou entre em um Bolão para poder deixar palpite!")
            return
        }
        
        viewModelScope.launch {
            repository.submitPalpite(
                userId = user.id,
                jogoId = jogoId,
                bolaoId = bolao.id,
                palpiteCasa = palpiteCasa,
                palpiteFora = palpiteFora,
                usouZica = usouZica
            )
            showToast("Seu palpite foi cravado com sucesso!")
            _selectedMatchForBet.value = null
        }
    }
}
