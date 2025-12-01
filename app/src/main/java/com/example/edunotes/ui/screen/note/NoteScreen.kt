package com.example.edunotes.ui.screen.note

import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import com.example.edunotes.data.model.StudyNote
import com.example.edunotes.ui.components.EduTextField
import com.example.edunotes.ui.nav.NavRoutes

@Composable
fun NoteScreen(
    navController: NavHostController,
    viewModel: NoteViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val isUploading by viewModel.uploadState.collectAsState()

    // State UI
    var showAddDialog by remember { mutableStateOf(false) }
    var selectedNote by remember { mutableStateOf<StudyNote?>(null) } // Jika tidak null, tampilkan detail

    // 1. TAMPILAN DETAIL FULL SCREEN (Jika ada note dipilih)
    if (selectedNote != null) {
        BackHandler { selectedNote = null } // Handle Back Button HP

        NoteDetailFullScreen(
            note = selectedNote!!,
            isUploading = isUploading,
            onDismiss = { selectedNote = null },
            onSaveEdit = { id, title, body, uri ->
                val context = navController.context
                val bytes = uri?.let { context.contentResolver.openInputStream(it)?.use { s -> s.readBytes() } }
                viewModel.updateNote(id, title, body, bytes)
                selectedNote = null
            }
        )
    }
    // 2. TAMPILAN UTAMA (LIST CATATAN)
    else {
        Scaffold(
            floatingActionButton = {
                FloatingActionButton(onClick = { showAddDialog = true }) {
                    Icon(Icons.Default.Add, contentDescription = "Tambah Catatan")
                }
            },
            bottomBar = {
                NavigationBar {
                    NavigationBarItem(
                        icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
                        label = { Text("Materi") },
                        selected = false,
                        onClick = {
                            navController.navigate(NavRoutes.Home.route) {
                                popUpTo(NavRoutes.Home.route) { inclusive = true }
                            }
                        }
                    )
                    NavigationBarItem(
                        icon = { Icon(Icons.Default.CheckBox, contentDescription = "Tugas") },
                        label = { Text("Tugas") },
                        selected = false,
                        onClick = {
                            navController.navigate(NavRoutes.Tasks.route) {
                                popUpTo(NavRoutes.Home.route)
                            }
                        }
                    )
                    NavigationBarItem(
                        icon = { Icon(Icons.Default.EditNote, contentDescription = "Catatan") },
                        label = { Text("Catatan") },
                        selected = true,
                        onClick = { /* Sudah di sini */ }
                    )
                }
            }
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp)
            ) {
                Text("Catatan Harian", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(16.dp))

                when (val state = uiState) {
                    is NoteUiState.Loading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
                    is NoteUiState.Error -> Text("Error: ${state.message}", color = Color.Red)
                    is NoteUiState.Success -> {
                        if (state.notes.isEmpty()) {
                            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                Text("Belum ada catatan.", color = Color.Gray)
                            }
                        } else {
                            // Gunakan Grid agar mirip Sticky Notes
                            LazyVerticalGrid(
                                columns = GridCells.Fixed(2),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                items(state.notes) { note ->
                                    NoteItem(
                                        note = note,
                                        onClick = { selectedNote = note },
                                        onDelete = { note.id?.let { viewModel.deleteNote(it) } }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Dialog Tambah
    if (showAddDialog) {
        NoteFormDialog(
            isUploading = isUploading,
            onDismiss = { showAddDialog = false },
            onConfirm = { title, body, uri ->
                val context = navController.context
                val bytes = uri?.let { context.contentResolver.openInputStream(it)?.use { s -> s.readBytes() } }
                viewModel.addNote(title, body, bytes)
                showAddDialog = false
            }
        )
    }
}

// --- ITEM KARTU CATATAN ---
@Composable
fun NoteItem(
    note: StudyNote,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(180.dp)
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        shape = RoundedCornerShape(12.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    text = note.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = note.noteBody,
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 4,
                    overflow = TextOverflow.Ellipsis,
                    color = Color.DarkGray
                )

                // Indikator ada gambar
                if (note.mindmapUrl != null) {
                    Spacer(modifier = Modifier.weight(1f))
                    Icon(Icons.Default.Image, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                }
            }

            // Tombol Hapus Kecil di pojok
            IconButton(
                onClick = onDelete,
                modifier = Modifier.align(Alignment.BottomEnd)
            ) {
                Icon(Icons.Default.Delete, contentDescription = "Hapus", tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(20.dp))
            }
        }
    }
}

// --- DETAIL FULL SCREEN & EDIT ---
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoteDetailFullScreen(
    note: StudyNote,
    isUploading: Boolean,
    onDismiss: () -> Unit,
    onSaveEdit: (Long, String, String, Uri?) -> Unit
) {
    var isEditing by remember { mutableStateOf(false) }
    var title by remember { mutableStateOf(note.title) }
    var body by remember { mutableStateOf(note.noteBody) }
    var selectedUri by remember { mutableStateOf<Uri?>(null) }

    val photoLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { selectedUri = it }
    )

    Surface(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text(if (isEditing) "Edit Catatan" else "Detail") },
                    navigationIcon = {
                        IconButton(onClick = onDismiss) { Icon(Icons.Default.Close, contentDescription = null) }
                    },
                    actions = {
                        if (!isEditing) {
                            IconButton(onClick = { isEditing = true }) { Icon(Icons.Default.Edit, contentDescription = null) }
                        }
                    }
                )
            }
        ) { padding ->
            Column(
                modifier = Modifier
                    .padding(padding)
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // GAMBAR MINDMAP
                if (isEditing) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                            .background(Color.LightGray, RoundedCornerShape(8.dp))
                            .clickable { photoLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) },
                        contentAlignment = Alignment.Center
                    ) {
                        if (selectedUri != null) {
                            AsyncImage(model = selectedUri, contentDescription = null, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                        } else if (note.mindmapUrl != null) {
                            AsyncImage(model = note.mindmapUrl, contentDescription = null, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                        } else {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(Icons.Default.Image, contentDescription = null)
                                Text("Upload Foto")
                            }
                        }
                    }
                } else {
                    if (note.mindmapUrl != null) {
                        AsyncImage(
                            model = note.mindmapUrl,
                            contentDescription = null,
                            modifier = Modifier
                                .fillMaxWidth()
                                .wrapContentHeight()
                                .clip(RoundedCornerShape(8.dp)),
                            contentScale = ContentScale.FillWidth
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // FORM / TEKS
                if (isEditing) {
                    EduTextField(value = title, onValueChange = { title = it }, label = "Judul")
                    Spacer(modifier = Modifier.height(8.dp))
                    EduTextField(value = body, onValueChange = { body = it }, label = "Isi Catatan", maxLines = 15)
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = { note.id?.let { onSaveEdit(it, title, body, selectedUri) } },
                        enabled = !isUploading,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(if (isUploading) "Menyimpan..." else "Simpan")
                    }
                } else {
                    Text(text = note.title, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = note.noteBody, style = MaterialTheme.typography.bodyLarge, lineHeight = 24.sp)
                }
            }
        }
    }
}

// --- DIALOG TAMBAH ---
@Composable
fun NoteFormDialog(
    isUploading: Boolean,
    onDismiss: () -> Unit,
    onConfirm: (String, String, Uri?) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var body by remember { mutableStateOf("") }
    var selectedUri by remember { mutableStateOf<Uri?>(null) }

    val photoLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { selectedUri = it }
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Buat Catatan Baru") },
        text = {
            Column {
                EduTextField(value = title, onValueChange = { title = it }, label = "Judul")
                Spacer(modifier = Modifier.height(8.dp))
                EduTextField(value = body, onValueChange = { body = it }, label = "Isi Catatan", maxLines = 5)
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedButton(onClick = { photoLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) }) {
                    Icon(Icons.Default.Image, contentDescription = null)
                    Text(if (selectedUri == null) "Foto" else "Ganti Foto")
                }
            }
        },
        confirmButton = {
            Button(
                enabled = !isUploading && title.isNotEmpty(),
                onClick = { onConfirm(title, body, selectedUri) }
            ) { Text(if(isUploading) "Loading..." else "Simpan") }
        },
        dismissButton = { if(!isUploading) TextButton(onClick = onDismiss) { Text("Batal") } }
    )
}