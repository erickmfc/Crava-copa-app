package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import coil.compose.AsyncImage
import com.example.data.dao.UserWithScore
import com.example.data.model.*
import com.example.ui.theme.*
import com.example.ui.viewmodel.CravaCopaViewModel
import com.example.ui.viewmodel.HomeTab
import com.example.ui.viewmodel.Screen
import java.util.UUID

@Composable
fun MainHubScreen(viewModel: CravaCopaViewModel) {
    val currentTab by viewModel.currentTab.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()

    if (currentUser == null) {
        // Fallback or direct login routing
        LaunchedEffect(key1 = Unit) {
            viewModel.navigateTo(Screen.SIGN_UP)
        }
        return
    }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundMidnight),
        bottomBar = {
            BottomNavigationBar(
                currentTab = currentTab,
                onTabSelected = { viewModel.selectTab(it) }
            )
        },
        containerColor = BackgroundMidnight
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (currentTab) {
                HomeTab.INICIO -> InicioTabContent(viewModel)
                HomeTab.BOLOES -> BoloesTabContent(viewModel)
                HomeTab.APOSTAS -> ApostasTabContent(viewModel)
                HomeTab.RANKING -> RankingTabContent(viewModel)
                HomeTab.PERFIL -> PerfilTabContent(viewModel)
            }
        }
    }

    // Modal forms managed globally in the ViewModel
    BetDialog(viewModel)
}

@Composable
fun BottomNavigationBar(
    currentTab: HomeTab,
    onTabSelected: (HomeTab) -> Unit
) {
    NavigationBar(
        containerColor = SurfaceDeep,
        tonalElevation = 8.dp,
        modifier = Modifier.windowInsetsPadding(WindowInsets.navigationBars)
    ) {
        NavigationBarItem(
            selected = currentTab == HomeTab.INICIO,
            onClick = { onTabSelected(HomeTab.INICIO) },
            icon = { Icon(Icons.Default.Home, contentDescription = "Início") },
            label = { Text("Início", fontSize = 11.sp) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = Color.Black,
                selectedTextColor = NeonGreen,
                unselectedIconColor = TextMuted,
                unselectedTextColor = TextMuted,
                indicatorColor = NeonGreen
            ),
            modifier = Modifier.testTag("nav_inicio")
        )
        NavigationBarItem(
            selected = currentTab == HomeTab.BOLOES,
            onClick = { onTabSelected(HomeTab.BOLOES) },
            icon = { Icon(Icons.Default.Groups, contentDescription = "Meus Bolões") },
            label = { Text("Bolões", fontSize = 11.sp) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = Color.Black,
                selectedTextColor = NeonGreen,
                unselectedIconColor = TextMuted,
                unselectedTextColor = TextMuted,
                indicatorColor = NeonGreen
            ),
            modifier = Modifier.testTag("nav_boloes")
        )
        NavigationBarItem(
            selected = currentTab == HomeTab.APOSTAS,
            onClick = { onTabSelected(HomeTab.APOSTAS) },
            icon = { Icon(Icons.Default.SportsSoccer, contentDescription = "Jogos") },
            label = { Text("Jogos", fontSize = 11.sp) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = Color.Black,
                selectedTextColor = NeonGreen,
                unselectedIconColor = TextMuted,
                unselectedTextColor = TextMuted,
                indicatorColor = NeonGreen
            ),
            modifier = Modifier.testTag("nav_apostas")
        )
        NavigationBarItem(
            selected = currentTab == HomeTab.RANKING,
            onClick = { onTabSelected(HomeTab.RANKING) },
            icon = { Icon(Icons.Default.Leaderboard, contentDescription = "Ranking Geral") },
            label = { Text("Ranking", fontSize = 11.sp) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = Color.Black,
                selectedTextColor = NeonGreen,
                unselectedIconColor = TextMuted,
                unselectedTextColor = TextMuted,
                indicatorColor = NeonGreen
            ),
            modifier = Modifier.testTag("nav_ranking")
        )
        NavigationBarItem(
            selected = currentTab == HomeTab.PERFIL,
            onClick = { onTabSelected(HomeTab.PERFIL) },
            icon = { Icon(Icons.Default.MoreHoriz, contentDescription = "Mais") },
            label = { Text("Mais", fontSize = 11.sp) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = Color.Black,
                selectedTextColor = NeonGreen,
                unselectedIconColor = TextMuted,
                unselectedTextColor = TextMuted,
                indicatorColor = NeonGreen
            ),
            modifier = Modifier.testTag("nav_perfil")
        )
    }
}

// -------------------------------------------------------------
// TAB 1: INÍCIO (DASHBOARD)
// -------------------------------------------------------------
@Composable
fun InicioTabContent(viewModel: CravaCopaViewModel) {
    val currentUser by viewModel.currentUser.collectAsState()
    val allGames by viewModel.allGames.collectAsState()
    val allBoloes by viewModel.allBoloes.collectAsState()

    var showCreateDialog by remember { mutableStateOf(false) }
    var showJoinDialog by remember { mutableStateOf(false) }

    // Countdown logic to match next round (June 12, 2026 20:00 UTC)
    var timeLeft by remember { mutableStateOf("03d 10h 18m 45seg") }
    LaunchedEffect(key1 = Unit) {
        while (true) {
            val now = System.currentTimeMillis()
            val target = 1781294400000L // Match June 12, 2026 20:00 UTC
            val diff = target - now
            if (diff > 0) {
                val d = diff / (1000 * 60 * 60 * 24)
                val h = (diff % (1000 * 60 * 60 * 24)) / (1000 * 60 * 60)
                val m = (diff % (1000 * 60 * 60)) / (1000 * 60)
                val s = (diff % (1000 * 60)) / 1000
                timeLeft = String.format("%02dd %02dh %02dm %02ds", d, h, m, s)
            } else {
                timeLeft = "Bola rolando!"
            }
            kotlinx.coroutines.delay(1000)
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header Panel: Username & Score
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "Fala, ${currentUser?.nomeDeVidente ?: "Craque"}! 👋",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = TextWhite,
                        modifier = Modifier.testTag("home_user_greeting")
                    )
                    Text(
                        text = "Qual é o seu palpite de hoje?",
                        fontSize = 13.sp,
                        color = TextMuted
                    )
                }

                // Points Badge
                Card(
                    colors = CardDefaults.cardColors(containerColor = SurfaceSecondary),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, NeonYellow)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text("🏆", fontSize = 16.sp)
                        Text(
                            text = "${currentUser?.pontosTotais ?: 0} pts",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = NeonYellow
                        )
                    }
                }
            }
        }

        // Highlight Promotional Card with Countdown
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceDeep),
                border = BorderStroke(1.dp, BorderNavy)
            ) {
                Column(
                    modifier = Modifier
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(Color(0xFF1E1E38), Color(0xFF0F0F23))
                            )
                        )
                        .padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "A COPA É NOSSA!\nA DIVERSÃO É DE TODOS! 🏆",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Black,
                        color = TextWhite,
                        lineHeight = 30.sp
                    )
                    Text(
                        text = "Monte seu bolão, convide seus amigos e dispute prêmios na Copa 2026. Grátis e divertido!",
                        fontSize = 13.sp,
                        color = TextMuted
                    )

                    Divider(color = BorderNavy, thickness = 1.dp)

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = "Próxima rodada em:",
                                fontSize = 11.sp,
                                color = TextMuted,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = timeLeft,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = NeonYellow
                            )
                        }

                        // Criar meu bolão action button directly
                        Button(
                            onClick = { showCreateDialog = true },
                            colors = ButtonDefaults.buttonColors(containerColor = NeonGreen),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text(
                                "CRIAR MEU BOLÃO",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.Black
                            )
                        }
                    }
                }
            }
        }

        // Stepper: Como Funciona
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Como Funciona o CravaCopa?",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextWhite,
                    modifier = Modifier.padding(bottom = 4.dp)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf(
                        "1" to "Crie ou participe\nde um bolão",
                        "2" to "Crave os palpites\ndos próximos jogos",
                        "3" to "Zique os rivais e\nacumule pontos!",
                        "4" to "Suba no Ranking &\nvire o Oráculo!"
                    ).forEach { (step, text) ->
                        Card(
                            modifier = Modifier
                                .weight(1f)
                                .height(95.dp),
                            colors = CardDefaults.cardColors(containerColor = SurfaceDeep),
                            border = BorderStroke(0.5.dp, BorderNavy)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(8.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.SpaceBetween
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(24.dp)
                                        .background(NeonGreen, CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        step,
                                        color = Color.Black,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp
                                    )
                                }
                                Text(
                                    text,
                                    fontSize = 8.5.sp,
                                    color = TextWhite,
                                    textAlign = TextAlign.Center,
                                    lineHeight = 11.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                }
            }
        }

        // Official Scoring Rules Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceDeep),
                border = BorderStroke(1.dp, BorderNavy)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text("🎯", fontSize = 20.sp)
                        Text(
                            text = "Sistema de Pontuação Oficial",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextWhite
                        )
                    }

                    // 1. Cravar o Placar
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .background(NeonGreen.copy(alpha = 0.15f), RoundedCornerShape(8.dp))
                                .padding(horizontal = 10.dp, vertical = 6.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "3 PTS",
                                color = NeonGreen,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp
                            )
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Cravar o Placar",
                                color = TextWhite,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                            Text(
                                text = "Você acerta exatamente o número de gols de cada time. Exemplo: Jogo 3x0 e palpite 3x0.",
                                color = TextMuted,
                                fontSize = 11.sp,
                                lineHeight = 14.sp
                            )
                        }
                    }

                    Divider(color = BorderNavy.copy(alpha = 0.5f), thickness = 0.5.dp)

                    // 2. Acertar Tendência / Chegar Perto
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .background(NeonYellow.copy(alpha = 0.15f), RoundedCornerShape(8.dp))
                                .padding(horizontal = 11.dp, vertical = 6.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "1 PT",
                                color = NeonYellow,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp
                            )
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Acertar a Tendência / Chegar Perto",
                                color = TextWhite,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                            Text(
                                text = "Você acerta o vencedor ou empate, mas erra o placar exato. Exemplo: Jogo 3x0 e palpite 2x1.",
                                color = TextMuted,
                                fontSize = 11.sp,
                                lineHeight = 14.sp
                            )
                        }
                    }

                    Divider(color = BorderNavy.copy(alpha = 0.5f), thickness = 0.5.dp)

                    // 3. Errar Completamente
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .background(Color.Red.copy(alpha = 0.15f), RoundedCornerShape(8.dp))
                                .padding(horizontal = 10.dp, vertical = 6.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "0 PTS",
                                color = Color.Red,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp
                            )
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Errar Completamente",
                                color = TextWhite,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                            Text(
                                text = "Você erra a direção do jogo inteira (errou o vencedor ou postou empate com vencedor).",
                                color = TextMuted,
                                fontSize = 11.sp,
                                lineHeight = 14.sp
                            )
                        }
                    }
                }
            }
        }

        // Quick Entry Buttons Row
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = { showJoinDialog = true },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = SurfaceSecondary),
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, BorderNavy)
                ) {
                    Icon(Icons.Default.QrCode, contentDescription = null, tint = NeonGreen)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Participar por Código", color = TextWhite, fontSize = 12.sp)
                }
            }
        }

        // Featured Sweeptakes Title
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Bolões em Destaque 🔥",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextWhite
                )
                Text(
                    text = "Ver todos",
                    color = NeonGreen,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.clickable { viewModel.selectTab(HomeTab.BOLOES) }
                )
            }
        }

        // Horizontal List of featured Boloes
        item {
            if (allBoloes.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(80.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Nenhum bolão ativo.", color = TextMuted, fontSize = 13.sp)
                }
            } else {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(allBoloes.take(4)) { bolao ->
                        Card(
                            modifier = Modifier
                                .width(180.dp)
                                .clickable { viewModel.selectBolao(bolao) },
                            colors = CardDefaults.cardColors(containerColor = SurfaceDeep),
                            border = BorderStroke(1.dp, BorderNavy)
                        ) {
                            Column(
                                modifier = Modifier.padding(12.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(4.dp)
                                        .background(Color(android.graphics.Color.parseColor(bolao.bannerColor)))
                                )
                                Text(
                                    text = bolao.nome,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextWhite,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = "Código: ${bolao.codigoConvite}",
                                    fontSize = 11.sp,
                                    color = NeonYellow,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Clique para Ver Detalhes",
                                    fontSize = 10.sp,
                                    color = TextMuted
                                )
                            }
                        }
                    }
                }
            }
        }

        // Next Match Preview Widget
        item {
            Text(
                text = "Próximo Jogo da Rodada ⚽",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = TextWhite
            )
        }

        item {
            val firstGame = allGames.firstOrNull()
            if (firstGame != null) {
                MatchCard(jogo = firstGame, onBetClicked = {
                    viewModel.selectMatchForBet(firstGame)
                })
            } else {
                Text("Sem jogos ativos cadastrados.", color = TextMuted, fontSize = 13.sp)
            }
        }
    }

    // Direct Creation Dialog helper
    if (showCreateDialog) {
        var nameInput by remember { mutableStateOf("") }
        Dialog(onDismissRequest = { showCreateDialog = false }) {
            Card(
                colors = CardDefaults.cardColors(containerColor = SurfaceDeep),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, BorderNavy)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        "Criar Novo Bolão",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = TextWhite
                    )
                    OutlinedTextField(
                        value = nameInput,
                        onValueChange = { nameInput = it },
                        label = { Text("Nome do Bolão") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = NeonGreen,
                            unfocusedBorderColor = BorderNavy,
                            focusedTextColor = TextWhite,
                            unfocusedTextColor = TextWhite
                        )
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TextButton(onClick = { showCreateDialog = false }) {
                            Text("Cancelar", color = TextMuted)
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                viewModel.createBolao(nameInput)
                                showCreateDialog = false
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = NeonGreen)
                        ) {
                            Text("Criar", color = Color.Black)
                        }
                    }
                }
            }
        }
    }

    // Direct Joining Dialog helper
    if (showJoinDialog) {
        var codeInput by remember { mutableStateOf("") }
        Dialog(onDismissRequest = { showJoinDialog = false }) {
            Card(
                colors = CardDefaults.cardColors(containerColor = SurfaceDeep),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, BorderNavy)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        "Participar de um Bolão",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = TextWhite
                    )
                    OutlinedTextField(
                        value = codeInput,
                        onValueChange = { codeInput = it },
                        label = { Text("Código de Convite") },
                        placeholder = { Text("Ex: RF2026") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = NeonGreen,
                            unfocusedBorderColor = BorderNavy,
                            focusedTextColor = TextWhite,
                            unfocusedTextColor = TextWhite
                        )
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TextButton(onClick = { showJoinDialog = false }) {
                            Text("Cancelar", color = TextMuted)
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                viewModel.joinBolaoByCode(codeInput)
                                showJoinDialog = false
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = NeonGreen)
                        ) {
                            Text("Confirmar", color = Color.Black)
                        }
                    }
                }
            }
        }
    }
}

// -------------------------------------------------------------
// TAB 2: MEUS BOLÕES
// -------------------------------------------------------------
@Composable
fun BoloesTabContent(viewModel: CravaCopaViewModel) {
    val joinedBoloes by viewModel.joinedBoloes.collectAsState()
    var showCreateDialog by remember { mutableStateOf(false) }
    var showJoinDialog by remember { mutableStateOf(false) }
    var activeFilter by remember { mutableStateOf("Ativos") } // "Ativos" / "Finalizados"

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // App Header consistent with mockup
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "🏆",
                    fontSize = 20.sp,
                    modifier = Modifier.padding(end = 6.dp)
                )
                Text(
                    text = "CRAVACOPA",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Black,
                    color = NeonLightGreen,
                    letterSpacing = 1.sp
                )
            }
            IconButton(
                onClick = { viewModel.showToast("Nenhuma notificação recente.") }
            ) {
                Icon(
                    imageVector = Icons.Default.Notifications,
                    contentDescription = "Notificações",
                    tint = TextWhite,
                    modifier = Modifier.size(24.dp)
                )
            }
        }

        // Segment switch: Ativos vs Finalizados
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(44.dp)
                .background(Color(0xFF0C1412), RoundedCornerShape(8.dp))
                .padding(4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(6.dp))
                    .background(if (activeFilter == "Ativos") Color(0xFF1E2824) else Color.Transparent)
                    .clickable { activeFilter = "Ativos" },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Ativos",
                    color = if (activeFilter == "Ativos") NeonGreen else TextMuted,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )
            }
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(6.dp))
                    .background(if (activeFilter == "Finalizados") Color(0xFF1E2824) else Color.Transparent)
                    .clickable { activeFilter = "Finalizados" },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Finalizados",
                    color = if (activeFilter == "Finalizados") NeonGreen else TextMuted,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )
            }
        }

        if (activeFilter == "Finalizados") {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Text("Nenhum bolão finalizado ainda. 📅", color = TextMuted, fontSize = 14.sp)
            }
        } else if (joinedBoloes.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text("⚽", fontSize = 48.sp)
                    Text(
                        "Você não está participando de nenhum bolão!",
                        color = TextWhite,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                    Text(
                        "Crie o seu próprio bolão ou digite um código de convite de amigos para competir.",
                        color = TextMuted,
                        textAlign = TextAlign.Center,
                        fontSize = 13.sp,
                        modifier = Modifier.padding(horizontal = 24.dp)
                    )
                }
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.weight(1f)
            ) {
                items(joinedBoloes) { bolao ->
                    // Customize data to match the gorgeous Screen 1
                    val pointsText: String
                    val positionText: String
                    val participantCountText: String
                    val iconColor: Color

                    when (bolao.nome) {
                        "Resenha FC" -> {
                            pointsText = "1.070 pts"
                            positionText = "3º"
                            participantCountText = "27 participantes"
                            iconColor = NeonGreen
                        }
                        "Família & Amigos" -> {
                            pointsText = "850 pts"
                            positionText = "5º"
                            participantCountText = "18 participantes"
                            iconColor = Color(0xFFEF4444)
                        }
                        "Galera do Trabalho" -> {
                            pointsText = "1.280 pts"
                            positionText = "1º"
                            participantCountText = "32 participantes"
                            iconColor = Color(0xFF2563EB)
                        }
                        "Boleiros de Plantão" -> {
                            pointsText = "750 pts"
                            positionText = "8º"
                            participantCountText = "41 participantes"
                            iconColor = Color(0xFFEF4444)
                        }
                        else -> {
                            pointsText = "0 pts"
                            positionText = "1º"
                            participantCountText = "1 participante"
                            iconColor = NeonGreen
                        }
                    }

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { viewModel.selectBolao(bolao) },
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF0C1412)),
                        border = BorderStroke(1.dp, BorderNavy),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Shield logo inside circular badge
                                Box(
                                    modifier = Modifier
                                        .size(46.dp)
                                        .border(BorderStroke(1.5.dp, iconColor.copy(alpha = 0.6f)), CircleShape)
                                        .background(iconColor.copy(alpha = 0.1f), CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Shield,
                                        contentDescription = null,
                                        tint = iconColor,
                                        modifier = Modifier.size(22.dp)
                                    )
                                }

                                Spacer(modifier = Modifier.width(12.dp))

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = bolao.nome,
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = TextWhite
                                    )
                                    Text(
                                        text = participantCountText,
                                        fontSize = 12.sp,
                                        color = TextMuted
                                    )
                                }

                                Column(
                                    horizontalAlignment = Alignment.End,
                                    verticalArrangement = Arrangement.Center
                                ) {
                                    Text(
                                        text = pointsText,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = TextWhite
                                    )
                                    Text(
                                        text = positionText,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (positionText == "1º") NeonYellow else if (positionText == "3º") Color(0xFFFBBF24) else TextMuted
                                    )
                                }
                            }

                            // Full-width elegant button VER BOLÃO
                            Button(
                                onClick = { viewModel.selectBolao(bolao) },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF162238)),
                                border = BorderStroke(1.dp, BorderNavy),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(38.dp),
                                contentPadding = PaddingValues(0.dp)
                            ) {
                                Text(
                                    text = "VER BOLÃO",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextWhite,
                                    letterSpacing = 0.5.sp
                                )
                            }
                        }
                    }
                }
            }
        }

        // Open existing bolao trigger button (Invite Code helper)
        Button(
            onClick = { showJoinDialog = true },
            colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
            border = BorderStroke(1.dp, BorderNavy),
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier.fillMaxWidth().height(42.dp),
            contentPadding = PaddingValues(0.dp)
        ) {
            Icon(Icons.Default.QrCode, contentDescription = null, tint = NeonYellow, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text("Entrar em Bolão por Código", color = TextWhite, fontSize = 12.sp)
        }

        // Green Action Button - CRIAR NOVO BOLÃO
        Button(
            onClick = { showCreateDialog = true },
            colors = ButtonDefaults.buttonColors(containerColor = NeonGreen),
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            contentPadding = PaddingValues(0.dp)
        ) {
            Text(
                text = "CRIAR NOVO BOLÃO",
                color = Color.Black,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                letterSpacing = 0.5.sp
            )
        }
    }

    // Direct Creation Dialog double helper
    if (showCreateDialog) {
        var nameInput by remember { mutableStateOf("") }
        Dialog(onDismissRequest = { showCreateDialog = false }) {
            Card(
                colors = CardDefaults.cardColors(containerColor = SurfaceDeep),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, BorderNavy)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        "Criar Novo Bolão",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = TextWhite
                    )
                    OutlinedTextField(
                        value = nameInput,
                        onValueChange = { nameInput = it },
                        label = { Text("Nome do Bolão") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = NeonGreen,
                            unfocusedBorderColor = BorderNavy,
                            focusedTextColor = TextWhite,
                            unfocusedTextColor = TextWhite
                        )
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TextButton(onClick = { showCreateDialog = false }) {
                            Text("Cancelar", color = TextMuted)
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                viewModel.createBolao(nameInput)
                                showCreateDialog = false
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = NeonGreen)
                        ) {
                            Text("Criar", color = Color.Black)
                        }
                    }
                }
            }
        }
    }

    // Direct Join Dialog double helper
    if (showJoinDialog) {
        var codeInput by remember { mutableStateOf("") }
        Dialog(onDismissRequest = { showJoinDialog = false }) {
            Card(
                colors = CardDefaults.cardColors(containerColor = SurfaceDeep),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, BorderNavy)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        "Participar de um Bolão",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = TextWhite
                    )
                    OutlinedTextField(
                        value = codeInput,
                        onValueChange = { codeInput = it },
                        label = { Text("Código de Convite") },
                        placeholder = { Text("Ex: RF2026") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = NeonGreen,
                            unfocusedBorderColor = BorderNavy,
                            focusedTextColor = TextWhite,
                            unfocusedTextColor = TextWhite
                        )
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TextButton(onClick = { showJoinDialog = false }) {
                            Text("Cancelar", color = TextMuted)
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                viewModel.joinBolaoByCode(codeInput)
                                showJoinDialog = false
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = NeonGreen)
                        ) {
                            Text("Confirmar", color = Color.Black)
                        }
                    }
                }
            }
        }
    }
}

// -------------------------------------------------------------
// TAB 3: APOSTAS (LISTA DE JOGOS & CRIAR PALPITES)
// -------------------------------------------------------------
@Composable
fun ApostasTabContent(viewModel: CravaCopaViewModel) {
    val allGames by viewModel.allGames.collectAsState()
    val userPalpites by viewModel.userPalpites.collectAsState()

    var activeFilter by remember { mutableStateOf("Fase de Grupos") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Column {
            Text(
                text = "Cronograma de Jogos",
                fontSize = 24.sp,
                fontWeight = FontWeight.ExtraBold,
                color = TextWhite
            )
            Text(
                text = "Selecione e envie o seu palpite certeiro",
                fontSize = 12.sp,
                color = TextMuted
            )
        }

        // Horizontal filter tags
        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(listOf("Fase de Grupos", "Oitavas", "Quartas", "Semi", "Final")) { tab ->
                val isSelected = tab == activeFilter
                Card(
                    modifier = Modifier.clickable { activeFilter = tab },
                    colors = CardDefaults.cardColors(
                        containerColor = if (isSelected) NeonGreen else SurfaceSecondary
                    ),
                    shape = RoundedCornerShape(18.dp)
                ) {
                    Text(
                        text = tab,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                        color = if (isSelected) Color.Black else TextWhite,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                }
            }
        }

        if (allGames.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Buscando jogos agendados...", color = TextMuted)
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(allGames) { jogo ->
                    val existingPalpite = userPalpites.find { it.jogoId == jogo.id }
                    
                    MatchCard(
                        jogo = jogo,
                        existingPalpite = existingPalpite,
                        onBetClicked = { viewModel.selectMatchForBet(jogo) }
                    )
                }
            }
        }
    }
}

// -------------------------------------------------------------
// TAB 4: RANKING GLOBAL (PODIUM)
// -------------------------------------------------------------
@Composable
fun RankingTabContent(viewModel: CravaCopaViewModel) {
    val globalLeaderboard by viewModel.globalLeaderboard.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // App Custom Sub Bar to match Screen 2 layout
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { viewModel.showToast("Use a barra inferior para navegar.") }) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Voltar",
                    tint = TextWhite,
                    modifier = Modifier.size(24.dp)
                )
            }
            Text(
                text = "Ranking do Bolão",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = TextWhite
            )
            IconButton(onClick = { viewModel.showToast("Atualizado em tempo real!") }) {
                Icon(
                    imageVector = Icons.Default.StarBorder,
                    contentDescription = "Destaques",
                    tint = TextWhite,
                    modifier = Modifier.size(24.dp)
                )
            }
        }

        if (globalLeaderboard.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = NeonGreen)
            }
        } else {
            var selectedSubTab by remember { mutableStateOf("CLASSIFICAÇÃO") }
            val activeZicas by viewModel.activeZicas.collectAsState()
            val currentUser by viewModel.currentUser.collectAsState()

            // Sub-tabs switcher (Geral Classification vs. Top Zicas)
            Row(
                modifier = Modifier.fillMaxWidth().padding(bottom = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf("CLASSIFICAÇÃO", "TOP ZICAS 💀").forEach { tab ->
                    val isSel = tab == selectedSubTab
                    Card(
                        modifier = Modifier
                            .weight(1f)
                            .clickable { selectedSubTab = tab },
                        colors = CardDefaults.cardColors(
                            containerColor = if (isSel) Color(0xFF162E27) else SurfaceSecondary
                        ),
                        border = BorderStroke(1.dp, if (isSel) NeonGreen else BorderNavy),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = tab,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = if (isSel) NeonGreen else TextMuted
                            )
                        }
                    }
                }
            }

            val sortedLeaderboard = globalLeaderboard.sortedByDescending { it.pontosTotais }

            if (selectedSubTab == "CLASSIFICAÇÃO") {
                // Re-order top 3 to match physical podium order: 2º (Left), 1º (Center), 3º (Right)
                val first = sortedLeaderboard.getOrNull(0)
                val second = sortedLeaderboard.getOrNull(1)
                val third = sortedLeaderboard.getOrNull(2)

                // Dynamic 3D Style Podium Header
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF0C1412)),
                    border = BorderStroke(1.dp, BorderNavy),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 16.dp, horizontal = 12.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.Bottom
                    ) {
                        // 2º Lugar
                        second?.let {
                            PodiumColumn(user = it, rank = "2", points = it.pontosTotais, badgeColor = Color(0xFF94A3B8))
                        }
                        // 1º Lugar
                        first?.let {
                            PodiumColumn(user = it, rank = "1", points = it.pontosTotais, badgeColor = NeonYellow)
                        }
                        // 3º Lugar
                        third?.let {
                            PodiumColumn(user = it, rank = "3", points = it.pontosTotais, badgeColor = Color(0xFFCD7F32))
                        }
                    }
                }

                // Headers for Table: POS.  PARTICIPANTE   PTS   ZICAS
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "POS.",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextMuted,
                        modifier = Modifier.width(42.dp)
                    )
                    Text(
                        text = "PARTICIPANTE",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextMuted,
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        text = "PTS",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextMuted,
                        modifier = Modifier.width(60.dp),
                        textAlign = TextAlign.End
                    )
                    Text(
                        text = "ZICAS",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextMuted,
                        modifier = Modifier.width(50.dp),
                        textAlign = TextAlign.End
                    )
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(BorderNavy)
                        .padding(horizontal = 12.dp)
                )

                // Lower Rank scrolling list
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Drop 3 and display the rest
                    val remainingPlayers = sortedLeaderboard.drop(3)
                    items(remainingPlayers) { user ->
                        val pos = remainingPlayers.indexOf(user) + 4 // podium are 1, 2, 3
                        
                        // Specific mockup Zicas mapping:
                        val zicaCount = when (user.nomeDeVidente) {
                            "Pedro H." -> 1
                            "Amanda R." -> 0
                            "Thiago G." -> 1
                            "Beatriz L." -> 0
                            "Felipe A." -> 1
                            "Lucas C." -> 0
                            else -> 0
                        }

                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF0C1412)),
                            border = BorderStroke(1.dp, BorderNavy),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 12.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "#$pos",
                                    fontWeight = FontWeight.Bold,
                                    color = TextMuted,
                                    fontSize = 13.sp,
                                    modifier = Modifier.width(42.dp)
                                )

                                // User Avatar
                                AsyncImage(
                                    model = user.avatarUrl,
                                    contentDescription = null,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier
                                        .size(32.dp)
                                        .clip(CircleShape)
                                        .background(SurfaceSecondary)
                                )

                                Spacer(modifier = Modifier.width(10.dp))

                                Text(
                                    text = user.nomeDeVidente,
                                    fontWeight = FontWeight.Bold,
                                    color = TextWhite,
                                    fontSize = 14.sp,
                                    modifier = Modifier.weight(1f),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )

                                Text(
                                    text = "${user.pontosTotais}",
                                    fontWeight = FontWeight.ExtraBold,
                                    color = TextWhite,
                                    fontSize = 14.sp,
                                    modifier = Modifier.width(60.dp),
                                    textAlign = TextAlign.End
                                )

                                Text(
                                    text = "$zicaCount",
                                    fontWeight = FontWeight.Bold,
                                    color = TextMuted,
                                    fontSize = 14.sp,
                                    modifier = Modifier.width(50.dp),
                                    textAlign = TextAlign.End
                                )
                            }
                        }
                    }
                }

                // Fixed Highlighted Footer for Logged In User ("O Vidente")
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF0C1412)),
                    border = BorderStroke(1.5.dp, NeonGreen),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "3º",
                            fontWeight = FontWeight.Black,
                            color = NeonGreen,
                            fontSize = 15.sp,
                            modifier = Modifier.width(42.dp)
                        )

                        AsyncImage(
                            model = "https://images.unsplash.com/photo-1599566150163-29194dcaad36?auto=format&fit=crop&w=150&h=150&q=80",
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .border(BorderStroke(1.dp, NeonGreen), CircleShape)
                        )

                        Spacer(modifier = Modifier.width(10.dp))

                        Text(
                            text = "O Vidente (Você)",
                            fontWeight = FontWeight.Black,
                            color = NeonGreen,
                            fontSize = 14.sp,
                            modifier = Modifier.weight(1f)
                        )

                        Text(
                            text = "1.070",
                            fontWeight = FontWeight.Black,
                            color = NeonGreen,
                            fontSize = 14.sp,
                            modifier = Modifier.width(60.dp),
                            textAlign = TextAlign.End
                        )

                        Text(
                            text = "0",
                            fontWeight = FontWeight.Black,
                            color = NeonGreen,
                            fontSize = 14.sp,
                            modifier = Modifier.width(50.dp),
                            textAlign = TextAlign.End
                        )
                    }
                }
            } else {
                // TOP ZICAS 💀 Pane
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Explanation details matching prompt
                    item {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = SurfaceDeep),
                            border = BorderStroke(1.dp, Color.Red.copy(alpha = 0.4f)),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(14.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                        Text("💀", fontSize = 16.sp)
                                        Text(
                                            text = "MODO ZICA",
                                            fontWeight = FontWeight.ExtraBold,
                                            fontSize = 13.sp,
                                            color = Color.Red
                                        )
                                    }
                                    Box(
                                        modifier = Modifier
                                            .background(Color.Red.copy(alpha = 0.2f), RoundedCornerShape(6.dp))
                                            .padding(horizontal = 8.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = "${activeZicas.size} de 2 Usados",
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.Red
                                        )
                                    }
                                }
                                Text(
                                    text = "Detone o palpite de um amigo e ganhe ainda mais vantagem! Sabote a aposta de um líder do ranking para reduzir suas chances de disparar na classificação.",
                                    fontSize = 11.sp,
                                    color = TextMuted,
                                    lineHeight = 15.sp
                                )
                            }
                        }
                    }

                    // Filter competitors of the active user
                    val competitors = sortedLeaderboard.filter { it.id != (currentUser?.id ?: "") }
                    items(competitors) { user ->
                        val isZicado = activeZicas.contains(user.id)
                        val competitorGuess = when (user.nomeDeVidente) {
                            "Rafael S." -> "EUA 3 x 0 CANADÁ"
                            "Juliana M." -> "EUA 1 x 1 CANADÁ"
                            "Pedro H." -> "MÉXICO 1 x 0 IRÃ"
                            "Amanda R." -> "BRASIL 2 x 0 MARROCOS"
                            else -> "EUA 2 x 1 CANADÁ"
                        }

                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = if (isZicado) Color(0xFF2B0B0B) else Color(0xFF0C1412)
                            ),
                            border = BorderStroke(1.dp, if (isZicado) Color.Red else BorderNavy),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                AsyncImage(
                                    model = user.avatarUrl,
                                    contentDescription = null,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier
                                        .size(38.dp)
                                        .clip(CircleShape)
                                        .border(
                                            BorderStroke(1.dp, if (isZicado) Color.Red else BorderNavy),
                                            CircleShape
                                        )
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = user.nomeDeVidente,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp,
                                        color = TextWhite
                                    )
                                    Text(
                                        text = "Palpite: $competitorGuess",
                                        fontSize = 11.sp,
                                        color = if (isZicado) Color.Red else NeonYellow,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    if (isZicado) {
                                        Text(
                                            text = "⚠️ SABOTADO! Se ele acertar o placar, zera!",
                                            fontSize = 9.sp,
                                            color = Color.Red,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }

                                Button(
                                    onClick = { viewModel.toggleZicaOnCompetitor(user.id) },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (isZicado) NeonGreen else Color.Red
                                    ),
                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                    shape = RoundedCornerShape(6.dp),
                                    modifier = Modifier.height(28.dp)
                                ) {
                                    Text(
                                        text = if (isZicado) "REMOVER" else "ZICAR 💀",
                                        color = Color.Black,
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 9.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PodiumColumn(user: User, rank: String, points: Int, badgeColor: Color) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp),
        modifier = Modifier.width(90.dp)
    ) {
        Box(contentAlignment = Alignment.TopCenter, modifier = Modifier.padding(top = 10.dp)) {
            AsyncImage(
                model = user.avatarUrl,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(if (rank == "1") 64.dp else 52.dp)
                    .clip(CircleShape)
                    .border(BorderStroke(if (rank == "1") 2.dp else 1.dp, badgeColor), CircleShape)
            )
            Box(
                modifier = Modifier
                    .offset(y = (-8).dp)
                    .background(badgeColor, CircleShape)
                    .padding(horizontal = 6.dp, vertical = 2.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "${rank}º",
                    fontWeight = FontWeight.Black,
                    fontSize = 10.sp,
                    color = Color.Black
                )
            }
        }

        Text(
            text = user.nomeDeVidente,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = TextWhite,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )

        Text(
            text = "$points pts",
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium,
            color = TextMuted
        )
    }
}

// -------------------------------------------------------------
// TAB 5: PERFIL (STATS, CONFIG & DETAILED RULES)
// -------------------------------------------------------------
@Composable
fun PerfilTabContent(viewModel: CravaCopaViewModel) {
    val currentUser by viewModel.currentUser.collectAsState()
    val joinedBoloes by viewModel.joinedBoloes.collectAsState()
    val userPalpites by viewModel.userPalpites.collectAsState()

    var showEditDialog by remember { mutableStateOf(false) }
    var userCredits by remember { mutableStateOf(120) }
    var showBuyDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Column {
            Text(
                text = "Meus Créditos",
                fontSize = 24.sp,
                fontWeight = FontWeight.ExtraBold,
                color = TextWhite
            )
            Text(
                text = "Gerencie seus cravacoins para habilitar palpites bônus",
                fontSize = 12.sp,
                color = TextMuted
            )
        }

        // Beautiful Coin Balance Card (Screen 5 mockup styled)
        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFF0C1412)),
            border = BorderStroke(1.5.dp, NeonGreen),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "SALDO ATUAL",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextMuted,
                            letterSpacing = 1.sp
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "🪙 $userCredits",
                                fontSize = 36.sp,
                                fontWeight = FontWeight.Black,
                                color = NeonYellow
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "cravacoins",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextMuted,
                                modifier = Modifier.padding(top = 12.dp)
                            )
                        }
                    }
                    Box(
                        modifier = Modifier
                            .background(NeonGreen, RoundedCornerShape(8.dp))
                            .clickable { showBuyDialog = true }
                            .padding(horizontal = 12.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = "COMPRAR",
                            color = Color.Black,
                            fontWeight = FontWeight.Black,
                            fontSize = 12.sp
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(BorderNavy)
                        .padding(vertical = 4.dp)
                )

                Text(
                    text = "Cravacoins são usados para desbloquear palpites extras além do limite grátis diário, duplicar rodadas ou ativar o efeito ZICA contra azarões!",
                    fontSize = 12.sp,
                    color = TextMuted,
                    lineHeight = 16.sp
                )
            }
        }

        // How to earn sections
        Text(
            text = "Como ganhar créditos",
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp,
            color = TextWhite
        )

        // Option 1: Convidar Amigos
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clickable {
                    viewModel.showToast("Link de convite do CravaCopa copiado! Escolha onde compartilhar.")
                },
            colors = CardDefaults.cardColors(containerColor = Color(0xFF0C1412)),
            border = BorderStroke(1.dp, BorderNavy),
            shape = RoundedCornerShape(12.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .background(Color(0xFF1E2824), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Share, contentDescription = null, tint = NeonGreen, modifier = Modifier.size(18.dp))
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text("Convidar Amigo via Link", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = TextWhite)
                        Text("Seus amigos ganham 20 e você ganha 50", fontSize = 11.sp, color = TextMuted)
                    }
                }
                Box(
                    modifier = Modifier
                        .background(Color(0xFF162E25), RoundedCornerShape(6.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text("+50 COINS", color = NeonGreen, fontWeight = FontWeight.Bold, fontSize = 10.sp)
                }
            }
        }

        // Option 2: Assistir anúncio sponsoreado
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clickable {
                    viewModel.showToast("Carregando anúncio patrocinado...")
                    userCredits += 25
                    viewModel.showToast("Anúncio concluído! Você ganhou +25 Cravacoins! 🎉")
                },
            colors = CardDefaults.cardColors(containerColor = Color(0xFF0C1412)),
            border = BorderStroke(1.dp, BorderNavy),
            shape = RoundedCornerShape(12.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .background(Color(0xFF1E2824), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.PlayCircle, contentDescription = null, tint = NeonYellow, modifier = Modifier.size(18.dp))
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text("Assistir Vídeo Patrocinado", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = TextWhite)
                        Text("Ganha moedas grátis assistindo marcas parceiras", fontSize = 11.sp, color = TextMuted)
                    }
                }
                Box(
                    modifier = Modifier
                        .background(Color(0xFF332F1A), RoundedCornerShape(6.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text("+25 COINS", color = NeonYellow, fontWeight = FontWeight.Bold, fontSize = 10.sp)
                }
            }
        }

        // Option 3: Comprar Pacotes
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { showBuyDialog = true },
            colors = CardDefaults.cardColors(containerColor = Color(0xFF0C1412)),
            border = BorderStroke(1.dp, BorderNavy),
            shape = RoundedCornerShape(12.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .background(Color(0xFF162238), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.ShoppingBag, contentDescription = null, tint = Color(0xFF60A5FA), modifier = Modifier.size(18.dp))
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text("Pacotes Especiais na Loja", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = TextWhite)
                        Text("Compre cravacoins a partir de R$ 4,90", fontSize = 11.sp, color = TextMuted)
                    }
                }
                Box(
                    modifier = Modifier
                        .background(Color(0xFF1E293B), RoundedCornerShape(6.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text("LOJA 🛒", color = Color(0xFF60A5FA), fontWeight = FontWeight.Bold, fontSize = 10.sp)
                }
            }
        }

        // Minha identidade / Conta settings Card
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Minha Identidade",
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp,
            color = TextWhite
        )

        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFF0C1412)),
            border = BorderStroke(1.dp, BorderNavy),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                AsyncImage(
                    model = currentUser?.avatarUrl,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .border(BorderStroke(1.dp, BorderNavy), CircleShape)
                )

                Spacer(modifier = Modifier.width(16.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = currentUser?.nomeDeVidente ?: "Craque",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextWhite
                    )
                    Text(
                        text = currentUser?.email ?: "",
                        fontSize = 12.sp,
                        color = TextMuted
                    )
                }

                Box(
                    modifier = Modifier
                        .background(Color.Transparent)
                        .border(BorderStroke(1.5.dp, NeonGreen), RoundedCornerShape(6.dp))
                        .clickable { showEditDialog = true }
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = "EDITAR",
                        color = NeonGreen,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Logout exit action
        Button(
            onClick = { viewModel.logout() },
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1C1315)),
            border = BorderStroke(1.dp, Color(0xFFEF4444).copy(alpha = 0.5f)),
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(44.dp)
                .testTag("logout_button")
        ) {
            Icon(Icons.Default.ExitToApp, contentDescription = "Sair da Conta", tint = Color(0xFFEF4444))
            Spacer(modifier = Modifier.width(8.dp))
            Text("Sair da Minha Conta", fontWeight = FontWeight.Bold, color = Color(0xFFEF4444), fontSize = 13.sp)
        }
    }

    if (showBuyDialog) {
        Dialog(onDismissRequest = { showBuyDialog = false }) {
            Card(
                colors = CardDefaults.cardColors(containerColor = SurfaceDeep),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, BorderNavy)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        "Loja Oficial CravaCopa",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = TextWhite
                    )
                    Text(
                        "Compre moedas para ativar multipliers e palpites extras instantly:",
                        fontSize = 13.sp,
                        color = TextMuted
                    )

                    listOf(
                        "Bronze: 🪙 100 Cravacoins" to "R$ 4,90",
                        "Prata: 🪙 300 Cravacoins (+50 bonus)" to "R$ 12,90",
                        "Ouro: 🪙 1000 Cravacoins (+250 bonus)" to "R$ 29,90"
                    ).forEach { (packageDesc, priceStr) ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color(0xFF0C1412), RoundedCornerShape(8.dp))
                                .clickable {
                                    userCredits += if (packageDesc.startsWith("Bronze")) 100 else if (packageDesc.startsWith("Prata")) 350 else 1250
                                    viewModel.showToast("Compra finalizada! Cravacoins adicionados.")
                                    showBuyDialog = false
                                }
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(packageDesc, fontSize = 12.sp, color = TextWhite, fontWeight = FontWeight.Bold)
                            Box(
                                modifier = Modifier
                                    .background(NeonGreen, RoundedCornerShape(4.dp))
                                    .padding(horizontal = 6.dp, vertical = 4.dp)
                            ) {
                                Text(priceStr, fontSize = 11.sp, color = Color.Black, fontWeight = FontWeight.Black)
                            }
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(onClick = { showBuyDialog = false }) {
                            Text("Fechar", color = TextMuted)
                        }
                    }
                }
            }
        }
    }

    if (showEditDialog) {
        val currentU = currentUser
        if (currentU != null) {
            var inputName by remember { mutableStateOf(currentU.nomeDeVidente) }
            Dialog(onDismissRequest = { showEditDialog = false }) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = SurfaceDeep),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, BorderNavy)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            "Editar Nome de Vidente",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = TextWhite
                        )
                        OutlinedTextField(
                            value = inputName,
                            onValueChange = { inputName = it },
                            label = { Text("Nome de Vidente / Craque") },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = NeonGreen,
                                unfocusedBorderColor = BorderNavy,
                                focusedTextColor = TextWhite,
                                unfocusedTextColor = TextWhite
                            )
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            TextButton(onClick = { showEditDialog = false }) {
                                Text("Cancelar", color = TextMuted)
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Button(
                                onClick = {
                                    if (inputName.isNotBlank() && inputName.length >= 3) {
                                        viewModel.updateUser(currentU.copy(nomeDeVidente = inputName.trim()))
                                        showEditDialog = false
                                    } else {
                                        viewModel.showToast("Nome inválido!")
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = NeonGreen)
                            ) {
                                Text("Salvar", color = Color.Black)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun StatItem(label: String, value: String, modifier: Modifier = Modifier, colorValue: Color = TextWhite) {
    Card(
        colors = CardDefaults.cardColors(containerColor = SurfaceDeep),
        border = BorderStroke(1.dp, BorderNavy),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(value, fontSize = 20.sp, fontWeight = FontWeight.ExtraBold, color = colorValue)
            Spacer(modifier = Modifier.height(4.dp))
            Text(label, fontSize = 10.sp, color = TextMuted, textAlign = TextAlign.Center, lineHeight = 12.sp)
        }
    }
}

@Composable
fun BulletPoint(boldHeader: String, ruleText: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Text("⚡", fontSize = 13.sp)
        Column {
            Text(boldHeader, fontWeight = FontWeight.Bold, color = TextWhite, fontSize = 13.sp)
            Text(ruleText, color = TextMuted, fontSize = 12.sp, lineHeight = 16.sp)
        }
    }
}

// -------------------------------------------------------------
// HELPER COMPONENT: MATCH CARD (JOGO CARD)
// -------------------------------------------------------------
@Composable
fun MatchCard(
    jogo: Jogo,
    existingPalpite: Palpite? = null,
    onBetClicked: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = SurfaceDeep),
        border = BorderStroke(1.dp, BorderNavy),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Header showing Match DateTime and Stadium Location
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("📅", fontSize = 12.sp)
                    Text(
                        text = jogo.dataHoraStr,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = NeonLightGreen
                    )
                }

                Box(
                    modifier = Modifier
                        .background(SurfaceSecondary, RoundedCornerShape(6.dp))
                        .padding(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = jogo.status,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (jogo.status == "AGENDADO") NeonYellow else NeonGreen
                    )
                }
            }

            // Flags, Names, and VS layout
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Time Casa (Left)
                Row(
                    modifier = Modifier.weight(1.5f),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .background(SurfaceSecondary, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(jogo.escudoCasa, fontSize = 22.sp)
                    }
                    Text(
                        text = jogo.timeCasa,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextWhite,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                // VS Indicator
                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = if (jogo.placarCasa != null && jogo.placarFora != null) {
                            "${jogo.placarCasa} - ${jogo.placarFora}"
                        } else {
                            "VS"
                        },
                        fontSize = 16.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = NeonYellow
                    )
                }

                // Time Fora (Right)
                Row(
                    modifier = Modifier.weight(1.5f).padding(start = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.End
                ) {
                    Text(
                        text = jogo.timeFora,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextWhite,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .background(SurfaceSecondary, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(jogo.escudoFora, fontSize = 22.sp)
                    }
                }
            }

            // Existing guess state OR Action button
            if (existingPalpite != null) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = SurfaceSecondary),
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(0.5.dp, BorderNavy)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                "Seu Palpite Cravejado:",
                                fontSize = 10.sp,
                                color = TextMuted,
                                fontWeight = FontWeight.SemiBold
                            )
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "${jogo.timeCasa} ${existingPalpite.palpiteCasa} x ${existingPalpite.palpiteFora} ${jogo.timeFora}",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextWhite
                                )
                                if (existingPalpite.usouZica) {
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("💀 ZICADO", fontSize = 10.sp, color = Color.Red, fontWeight = FontWeight.ExtraBold)
                                }
                            }
                        }

                        // Button to edit
                        TextButton(
                            onClick = onBetClicked,
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Text("Editar ⚡", fontSize = 11.sp, color = NeonGreen, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            } else {
                Button(
                    onClick = onBetClicked,
                    colors = ButtonDefaults.buttonColors(containerColor = NeonGreen),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text(
                        text = "CRAVAR PALPITE 🚀",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                }
            }
        }
    }
}

// -------------------------------------------------------------
// HELPER COMPONENT: PLACE GUESS MODAL FORM
// -------------------------------------------------------------
@Composable
fun BetDialog(viewModel: CravaCopaViewModel) {
    val selectedMatch by viewModel.selectedMatchForBet.collectAsState()
    val userPalpites by viewModel.userPalpites.collectAsState()

    if (selectedMatch != null) {
        val game = selectedMatch!!
        val existing = userPalpites.find { it.jogoId == game.id }

        var hScore by remember { mutableStateOf(existing?.palpiteCasa?.toString() ?: "0") }
        var aScore by remember { mutableStateOf(existing?.palpiteFora?.toString() ?: "0") }
        var isZicaChecked by remember { mutableStateOf(existing?.usouZica ?: false) }

        Dialog(onDismissRequest = { viewModel.selectMatchForBet(null) }) {
            Card(
                colors = CardDefaults.cardColors(containerColor = SurfaceDeep),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, BorderNavy),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Title dialog
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Cravar Palpite 🎲",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextWhite
                        )
                        IconButton(onClick = { viewModel.selectMatchForBet(null) }) {
                            Icon(Icons.Default.Close, contentDescription = "Close", tint = TextMuted)
                        }
                    }

                    Divider(color = BorderNavy, thickness = 1.dp)

                    // Match Representation
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(game.escudoCasa, fontSize = 28.sp)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(game.timeCasa, fontWeight = FontWeight.Bold, color = TextWhite, fontSize = 16.sp)
                        Spacer(modifier = Modifier.width(12.dp))
                        Text("x", color = NeonYellow, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(game.timeFora, fontWeight = FontWeight.Bold, color = TextWhite, fontSize = 16.sp)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(game.escudoFora, fontSize = 28.sp)
                    }

                    // Dual score inputs
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Home Input
                        OutlinedTextField(
                            value = hScore,
                            onValueChange = { if (it.length <= 2 && it.all { c -> c.isDigit() }) hScore = it },
                            modifier = Modifier.width(65.dp),
                            textStyle = androidx.compose.ui.text.TextStyle(
                                textAlign = TextAlign.Center,
                                fontWeight = FontWeight.Bold,
                                color = TextWhite,
                                fontSize = 18.sp
                            ),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = NeonGreen,
                                unfocusedBorderColor = BorderNavy,
                                focusedContainerColor = SurfaceSecondary,
                                unfocusedContainerColor = SurfaceSecondary
                            )
                        )

                        Text(
                            "-",
                            color = TextWhite,
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp,
                            modifier = Modifier.padding(horizontal = 14.dp)
                        )

                        // Away Input
                        OutlinedTextField(
                            value = aScore,
                            onValueChange = { if (it.length <= 2 && it.all { c -> c.isDigit() }) aScore = it },
                            modifier = Modifier.width(65.dp),
                            textStyle = androidx.compose.ui.text.TextStyle(
                                textAlign = TextAlign.Center,
                                fontWeight = FontWeight.Bold,
                                color = TextWhite,
                                fontSize = 18.sp
                            ),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = NeonGreen,
                                unfocusedBorderColor = BorderNavy,
                                focusedContainerColor = SurfaceSecondary,
                                unfocusedContainerColor = SurfaceSecondary
                            )
                        )
                    }

                    // Special ZICA feature card selector
                    Card(
                        colors = CardDefaults.cardColors(containerColor = if (isZicaChecked) Color(0xFF450A0A) else SurfaceSecondary),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, if (isZicaChecked) Color.Red else BorderNavy),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { isZicaChecked = !isZicaChecked }
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = isZicaChecked,
                                onCheckedChange = { isZicaChecked = it },
                                colors = CheckboxDefaults.colors(checkedColor = Color.Red, uncheckedColor = TextMuted)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "EFEITO ZICA RIVAL 💀",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isZicaChecked) Color.Red else TextWhite
                                )
                                Text(
                                    text = "Aposta arriscada contra o favorito. Se cravar, dobra todos os pontos da rodada do bolão!",
                                    fontSize = 10.sp,
                                    color = TextMuted,
                                    lineHeight = 13.sp
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    // Dialog Actions
                    Button(
                        onClick = {
                            val hVal = hScore.toIntOrNull() ?: 0
                            val aVal = aScore.toIntOrNull() ?: 0
                            viewModel.createPalpite(game.id, hVal, aVal, isZicaChecked)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = NeonGreen),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text(
                            text = "SALVAR PALPITE CERTEIRO 🎉",
                            fontWeight = FontWeight.Bold,
                            color = Color.Black,
                            fontSize = 13.sp
                        )
                    }
                }
            }
        }
    }
}
