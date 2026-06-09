package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.ui.theme.*
import com.example.ui.viewmodel.CravaCopaViewModel
import com.example.ui.viewmodel.HomeTab
import com.example.ui.viewmodel.Screen
import kotlinx.coroutines.launch

data class MockChatMessage(
    val senderName: String,
    val senderAvatarUrl: String,
    val text: String,
    val isCurrentUser: Boolean,
    val timeStr: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BolaoDetailsScreen(viewModel: CravaCopaViewModel) {
    val bolao by viewModel.selectedBolao.collectAsState()
    val participants by viewModel.selectedBolaoParticipants.collectAsState(initial = emptyList())
    val participantCount by viewModel.selectedBolaoParticipantCount.collectAsState(initial = 0)
    val currentUser by viewModel.currentUser.collectAsState()

    var activeSubTab by remember { mutableStateOf("Resenha") } // "Classificação" vs "Resenha"
    var messageText by remember { mutableStateOf("") }
    val coroutineScope = rememberCoroutineScope()
    val listState = rememberLazyListState()

    var chatMessages by remember {
        mutableStateOf(
            listOf(
                MockChatMessage(
                    senderName = "Lucas C.",
                    senderAvatarUrl = "https://images.unsplash.com/photo-1519085360753-af0119f7cbe7?auto=format&fit=crop&w=150&h=150&q=80",
                    text = "BORA GALERA! QUEM VAO CRAVAR O BRASIL?",
                    isCurrentUser = false,
                    timeStr = "14:30"
                ),
                MockChatMessage(
                    senderName = "Rafael S.",
                    senderAvatarUrl = "https://images.unsplash.com/photo-1535713875002-d1d0cf377fde?auto=format&fit=crop&w=150&h=150&q=80",
                    text = "Eu apostei 2x1 Brasil contra Marrocos. Certeza de green!",
                    isCurrentUser = false,
                    timeStr = "14:32"
                ),
                MockChatMessage(
                    senderName = "Juliana M.",
                    senderAvatarUrl = "https://images.unsplash.com/photo-1494790108377-be9c29b29330?auto=format&fit=crop&w=150&h=150&q=80",
                    text = "Marrocos tá forte, acho que dá empate 1x1 hein.",
                    isCurrentUser = false,
                    timeStr = "14:34"
                ),
                MockChatMessage(
                    senderName = "Você",
                    senderAvatarUrl = "https://images.unsplash.com/photo-1599566150163-29194dcaad36?auto=format&fit=crop&w=150&h=150&q=80",
                    text = "Vocês tão malucos, vai ser 3x0 pro Brasil com show do Vini! 🇧🇷🔥",
                    isCurrentUser = true,
                    timeStr = "14:35"
                ),
                MockChatMessage(
                    senderName = "Rafael S.",
                    senderAvatarUrl = "https://images.unsplash.com/photo-1535713875002-d1d0cf377fde?auto=format&fit=crop&w=150&h=150&q=80",
                    text = "Quem errar paga a coca na sexta! 🥤😂",
                    isCurrentUser = false,
                    timeStr = "14:37"
                )
            )
        )
    }

    if (bolao == null) {
        LaunchedEffect(key1 = Unit) {
            viewModel.navigateTo(Screen.MAIN_HUB)
        }
        return
    }

    val activeBolao = bolao!!

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(activeBolao.nome, fontWeight = FontWeight.Black, color = TextWhite) },
                navigationIcon = {
                    IconButton(onClick = {
                        viewModel.selectBolao(null)
                        viewModel.navigateTo(Screen.MAIN_HUB)
                    }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Voltar", tint = TextWhite)
                    }
                },
                actions = {
                    Box(
                        modifier = Modifier
                            .padding(end = 12.dp)
                            .background(Color.Black.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                            .clickable {
                                viewModel.showToast("Código Copiado: ${activeBolao.codigoConvite}")
                            }
                    ) {
                        Text(
                            text = activeBolao.codigoConvite,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = NeonYellow
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF0C1412)),
                modifier = Modifier.testTag("details_top_bar")
            )
        },
        containerColor = BackgroundMidnight
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Elegant Tab Select Panel (Classificação vs Resenha)
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
                        .background(if (activeSubTab == "Classificação") Color(0xFF1E2824) else Color.Transparent)
                        .clickable { activeSubTab = "Classificação" },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Classificação",
                        color = if (activeSubTab == "Classificação") NeonGreen else TextMuted,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                }
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(6.dp))
                        .background(if (activeSubTab == "Resenha") Color(0xFF1E2824) else Color.Transparent)
                        .clickable { activeSubTab = "Resenha" },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Resenha",
                        color = if (activeSubTab == "Resenha") NeonGreen else TextMuted,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                }
            }

            if (activeSubTab == "Classificação") {
                // Header Hero Card themed with Bolão custom banner color
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFF0D1C16)
                    ),
                    border = BorderStroke(1.dp, BorderNavy),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Admin do Grupo",
                                color = TextMuted,
                                fontSize = 12.sp
                            )
                            Box(
                                modifier = Modifier
                                    .background(Color.Black.copy(alpha = 0.3f), CircleShape)
                                    .padding(horizontal = 8.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    "👑 Lucas C.",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = NeonYellow
                                )
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("Videntes participando", fontSize = 11.sp, color = TextMuted)
                                Text(
                                    text = "$participantCount participantes",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextWhite
                                )
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text("Código Convite", fontSize = 11.sp, color = TextMuted)
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = activeBolao.codigoConvite,
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Black,
                                        color = NeonYellow
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Icon(
                                        Icons.Default.ContentCopy,
                                        contentDescription = "Copiar",
                                        tint = NeonYellow,
                                        modifier = Modifier
                                            .size(14.dp)
                                            .clickable { viewModel.showToast("Código copiado!") }
                                    )
                                }
                            }
                        }
                    }
                }

                // Title Classificação
                Text(
                    "Classificação do Grupo",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = TextWhite,
                    modifier = Modifier.padding(top = 4.dp)
                )

                if (participants.isEmpty()) {
                    Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = NeonGreen)
                    }
                } else {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        items(participants) { member ->
                            val isCurrentUser = member.userId == currentUser?.id
                            val pos = participants.indexOf(member) + 1
                            Card(
                                colors = CardDefaults.cardColors(
                                    containerColor = if (isCurrentUser) Color(0xFF11221B) else Color(0xFF0C1412)
                                ),
                                border = BorderStroke(
                                    1.dp,
                                    if (isCurrentUser) NeonGreen else BorderNavy
                                ),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    // Position indicator
                                    Text(
                                        text = "${pos}º",
                                        fontWeight = FontWeight.Black,
                                        color = if (pos == 1) NeonYellow else if (pos == 3) NeonGreen else TextMuted,
                                        fontSize = 14.sp,
                                        modifier = Modifier.width(36.dp)
                                    )

                                    // Member Avatar
                                    AsyncImage(
                                        model = member.avatarUrl,
                                        contentDescription = null,
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier
                                            .size(32.dp)
                                            .clip(CircleShape)
                                            .background(SurfaceSecondary)
                                    )

                                    Spacer(modifier = Modifier.width(10.dp))

                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = if (isCurrentUser) "${member.nomeDeVidente} (Você)" else member.nomeDeVidente,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isCurrentUser) NeonGreen else TextWhite,
                                            fontSize = 14.sp
                                        )
                                    }

                                    Text(
                                        text = "${member.pontosNoBolao} pts",
                                        fontWeight = FontWeight.ExtraBold,
                                        color = if (pos == 1) NeonYellow else TextWhite,
                                        fontSize = 14.sp
                                    )
                                }
                            }
                        }
                    }
                }

                // Control Actions at bottom for Classificação tab
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = {
                            viewModel.selectBolao(null)
                            viewModel.navigateTo(Screen.MAIN_HUB)
                            viewModel.selectTab(HomeTab.APOSTAS)
                        },
                        modifier = Modifier.weight(1f).height(44.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF162238)),
                        border = BorderStroke(1.dp, BorderNavy),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Icon(Icons.Default.SportsBasketball, contentDescription = null, tint = NeonGreen, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("PALPITAR JOGOS", color = TextWhite, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = {
                            viewModel.leaveBolao(activeBolao.id)
                        },
                        modifier = Modifier.weight(1f).height(44.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1C1315)),
                        border = BorderStroke(1.dp, Color(0xFFEF4444).copy(alpha = 0.5f)),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Icon(Icons.Default.ExitToApp, contentDescription = null, tint = Color(0xFFEF4444), modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("SAIR DO BOLÃO", color = Color(0xFFEF4444), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            } else {
                // "Resenha" (Chat) subtab selected!
                // Chat list layout with live scroll to bottom on entry and messaging
                LazyColumn(
                    state = listState,
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .background(Color(0xFF070B0B), RoundedCornerShape(12.dp))
                        .padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(chatMessages) { msg ->
                        val isCurrentUser = msg.isCurrentUser || msg.senderName == "Você"
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = if (isCurrentUser) Arrangement.End else Arrangement.Start,
                            verticalAlignment = Alignment.Top
                        ) {
                            if (!isCurrentUser) {
                                AsyncImage(
                                    model = msg.senderAvatarUrl,
                                    contentDescription = null,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier
                                        .size(32.dp)
                                        .clip(CircleShape)
                                        .background(SurfaceSecondary)
                                        .border(BorderStroke(1.dp, BorderNavy), CircleShape)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                            }

                            Column(
                                horizontalAlignment = if (isCurrentUser) Alignment.End else Alignment.Start,
                                modifier = Modifier.widthIn(max = 240.dp)
                            ) {
                                if (!isCurrentUser) {
                                    Text(
                                        text = msg.senderName,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = TextMuted,
                                        modifier = Modifier.padding(bottom = 2.dp)
                                    )
                                }

                                // Speech Bubble
                                Card(
                                    colors = CardDefaults.cardColors(
                                        containerColor = if (isCurrentUser) Color(0xFF14241E) else Color(0xFF0F1E19)
                                    ),
                                    border = BorderStroke(1.dp, if (isCurrentUser) NeonGreen.copy(alpha = 0.4f) else BorderNavy),
                                    shape = RoundedCornerShape(
                                        topStart = 12.dp,
                                        topEnd = 12.dp,
                                        bottomStart = if (isCurrentUser) 12.dp else 0.dp,
                                        bottomEnd = if (isCurrentUser) 0.dp else 12.dp
                                    )
                                ) {
                                    Text(
                                        text = msg.text,
                                        color = TextWhite,
                                        fontSize = 13.sp,
                                        modifier = Modifier.padding(12.dp)
                                    )
                                }

                                Text(
                                    text = msg.timeStr,
                                    fontSize = 10.sp,
                                    color = TextMuted,
                                    modifier = Modifier.padding(top = 2.dp, start = 4.dp, end = 4.dp)
                                )
                            }

                            if (isCurrentUser) {
                                Spacer(modifier = Modifier.width(8.dp))
                                AsyncImage(
                                    model = "https://images.unsplash.com/photo-1599566150163-29194dcaad36?auto=format&fit=crop&w=150&h=150&q=80",
                                    contentDescription = null,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier
                                        .size(32.dp)
                                        .clip(CircleShape)
                                        .background(SurfaceSecondary)
                                        .border(BorderStroke(1.dp, NeonGreen), CircleShape)
                                )
                            }
                        }
                    }
                }

                // Chat Input Bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = messageText,
                        onValueChange = { messageText = it },
                        placeholder = { Text("Escreva sua resenha...", color = TextMuted, fontSize = 13.sp) },
                        modifier = Modifier
                            .weight(1f)
                            .height(52.dp),
                        shape = RoundedCornerShape(24.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = Color(0xFF0C1412),
                            unfocusedContainerColor = Color(0xFF0C1412),
                            focusedBorderColor = NeonGreen,
                            unfocusedBorderColor = BorderNavy,
                            focusedTextColor = TextWhite,
                            unfocusedTextColor = TextWhite
                        ),
                        maxLines = 1,
                        singleLine = true
                    )

                    IconButton(
                        onClick = {
                            if (messageText.isNotBlank()) {
                                chatMessages = chatMessages + MockChatMessage(
                                    senderName = "Você",
                                    senderAvatarUrl = "https://images.unsplash.com/photo-1599566150163-29194dcaad36?auto=format&fit=crop&w=150&h=150&q=80",
                                    text = messageText,
                                    isCurrentUser = true,
                                    timeStr = "14:38"
                                )
                                messageText = ""
                                // Auto scroll to bottom
                                coroutineScope.launch {
                                    listState.animateScrollToItem(chatMessages.size - 1)
                                }
                            }
                        },
                        modifier = Modifier
                            .size(44.dp)
                            .background(NeonGreen, CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Send,
                            contentDescription = "Enviar",
                            tint = Color.Black,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }
}
