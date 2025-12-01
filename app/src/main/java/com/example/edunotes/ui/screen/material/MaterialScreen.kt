package com.example.edunotes.ui.screen.material

import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.edunotes.data.model.Material
import com.example.edunotes.ui.components.EduTextField

@Composable
fun MaterialScreen(
    navController: NavController,
    categoryId: Long,
    categoryName: String,
    viewModel: MaterialViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val isUploading by viewModel.uploadState.collectAsState()

    var showAddDialog by remember { mutableStateOf(false) }
    var selectedMaterial by remember { mutableStateOf<Material?>(null) }

    // Load data otomatis
    LaunchedEffect(categoryId) {
        viewModel.loadMaterials(categoryId)
    }

    // --- LOGIKA TAMPILAN ---
    // Jika ada materi yang dipilih, TAMPILKAN LAYAR PENUH DETAIL
    if (selectedMaterial != null) {
        // Tangkap tombol Back HP agar menutup detail, bukan keluar app
        BackHandler { selectedMaterial = null }

        MaterialDetailFullScreen(
            material = selectedMaterial!!,
            isUploading = isUploading,
            onDismiss = { selectedMaterial = null }, // Tutup detail
            onSaveEdit = { id, title, content, uri ->
                val context = navController.context
                val bytes = uri?.let {
                    context.contentResolver.openInputStream(it)?.use { stream -> stream.readBytes() }
                }
                viewModel.updateMaterial(id, categoryId, title, content, bytes)
                selectedMaterial = null
            }
        )
    }
    // Jika tidak ada yang dipilih, TAMPILKAN LIST MATERI BIASA
    else {
        Scaffold(
            topBar = {
                @OptIn(ExperimentalMaterial3Api::class)
                TopAppBar(
                    title = { Text(categoryName) },
                    navigationIcon = {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Kembali")
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        titleContentColor = MaterialTheme.colorScheme.primary
                    )
                )
            },
            floatingActionButton = {
                FloatingActionButton(onClick = { showAddDialog = true }) {
                    Icon(Icons.Default.Add, contentDescription = "Tambah Materi")
                }
            }
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp)
            ) {
                when (val state = uiState) {
                    is MaterialUiState.Loading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
                    is MaterialUiState.Error -> Text("Error: ${state.message}", color = Color.Red)
                    is MaterialUiState.Success -> {
                        if (state.materials.isEmpty()) {
                            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                Text("Belum ada materi di pelajaran ini.", color = Color.Gray)
                            }
                        } else {
                            LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                items(state.materials) { material ->
                                    MaterialItem(
                                        material = material,
                                        onClick = { selectedMaterial = material },
                                        onDelete = {
                                            material.id?.let { id ->
                                                viewModel.deleteMaterial(id, categoryId)
                                            }
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Dialog Tambah (Tetap Popup Kecil)
    if (showAddDialog) {
        MaterialFormDialog(
            titleDialog = "Tambah Materi Baru",
            isUploading = isUploading,
            onDismiss = { showAddDialog = false },
            onConfirm = { title, content, uri ->
                val context = navController.context
                val bytes = uri?.let {
                    context.contentResolver.openInputStream(it)?.use { stream -> stream.readBytes() }
                }
                viewModel.addMaterial(categoryId, title, content, bytes)
                showAddDialog = false
            }
        )
    }
}

// --- KOMPONEN ITEM LIST ---
@Composable
fun MaterialItem(
    material: Material,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        elevation = CardDefaults.cardElevation(2.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier.clickable { onClick() }
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                if (material.imageUrl != null) {
                    AsyncImage(
                        model = material.imageUrl,
                        contentDescription = null,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(150.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color.LightGray),
                        contentScale = ContentScale.Crop
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                }
                Text(text = material.title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = material.content,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.DarkGray,
                    maxLines = 2,
                    lineHeight = 20.sp
                )
            }
            IconButton(
                onClick = onDelete,
                modifier = Modifier.align(Alignment.TopEnd)
            ) {
                Icon(Icons.Default.Delete, contentDescription = "Hapus", tint = MaterialTheme.colorScheme.error)
            }
        }
    }
}

// --- LAYAR PENUH DETAIL & EDIT ---
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MaterialDetailFullScreen(
    material: Material,
    isUploading: Boolean,
    onDismiss: () -> Unit,
    onSaveEdit: (Long, String, String, Uri?) -> Unit
) {
    var isEditing by remember { mutableStateOf(false) }

    // State Form
    var title by remember { mutableStateOf(material.title) }
    var content by remember { mutableStateOf(material.content) }
    var selectedUri by remember { mutableStateOf<Uri?>(null) }

    val photoLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { selectedUri = it }
    )

    // Gunakan Surface agar menutupi layar list di belakangnya
    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text(if (isEditing) "Edit Materi" else "Detail Materi") },
                    navigationIcon = {
                        // Tombol Close/Back
                        IconButton(onClick = onDismiss) {
                            Icon(Icons.Default.Close, contentDescription = "Tutup")
                        }
                    },
                    actions = {
                        // Tombol Edit (Hanya di mode baca)
                        if (!isEditing) {
                            IconButton(onClick = { isEditing = true }) {
                                Icon(Icons.Default.Edit, contentDescription = "Edit")
                            }
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                )
            }
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp)
            ) {
                // --- BAGIAN GAMBAR ---
                if (isEditing) {
                    // Mode Edit: Area Ganti Foto
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color.LightGray)
                            .clickable { photoLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) },
                        contentAlignment = Alignment.Center
                    ) {
                        if (selectedUri != null) {
                            AsyncImage(model = selectedUri, contentDescription = null, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                        } else if (material.imageUrl != null) {
                            AsyncImage(model = material.imageUrl, contentDescription = null, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                        } else {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(Icons.Default.Image, contentDescription = null)
                                Text("Ganti Foto")
                            }
                        }
                    }
                } else {
                    // Mode Baca: Tampilkan Foto Full
                    if (material.imageUrl != null) {
                        AsyncImage(
                            model = material.imageUrl,
                            contentDescription = null,
                            modifier = Modifier
                                .fillMaxWidth()
                                .wrapContentHeight() // Tinggi menyesuaikan gambar
                                .clip(RoundedCornerShape(12.dp)),
                            contentScale = ContentScale.FillWidth
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // --- BAGIAN KONTEN ---
                if (isEditing) {
                    EduTextField(value = title, onValueChange = { title = it }, label = "Judul")
                    Spacer(modifier = Modifier.height(12.dp))
                    EduTextField(value = content, onValueChange = { content = it }, label = "Isi Materi", maxLines = 15) // TextField lebih tinggi

                    Spacer(modifier = Modifier.height(24.dp))

                    Button(
                        onClick = {
                            material.id?.let { id ->
                                onSaveEdit(id, title, content, selectedUri)
                            }
                        },
                        enabled = !isUploading,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        if (isUploading) Text("Menyimpan...") else {
                            Icon(Icons.Default.Save, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Simpan Perubahan")
                        }
                    }
                } else {
                    // Mode Baca: Teks yang nyaman dibaca
                    Text(
                        text = material.title,
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    // Garis pembatas tipis
                    HorizontalDivider(thickness = 1.dp, color = Color.LightGray)
                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = material.content,
                        style = MaterialTheme.typography.bodyLarge,
                        lineHeight = 28.sp, // Jarak antar baris biar enak dibaca
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }
            }
        }
    }
}

// --- DIALOG TAMBAH (POPUP KECIL) ---
@Composable
fun MaterialFormDialog(
    titleDialog: String,
    isUploading: Boolean,
    onDismiss: () -> Unit,
    onConfirm: (String, String, Uri?) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var content by remember { mutableStateOf("") }
    var selectedUri by remember { mutableStateOf<Uri?>(null) }

    val photoLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { selectedUri = it }
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(titleDialog) },
        text = {
            Column {
                EduTextField(value = title, onValueChange = { title = it }, label = "Judul Materi")
                Spacer(modifier = Modifier.height(8.dp))
                EduTextField(value = content, onValueChange = { content = it }, label = "Isi / Deskripsi", maxLines = 5)
                Spacer(modifier = Modifier.height(12.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    OutlinedButton(onClick = {
                        photoLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                    }) {
                        Icon(Icons.Default.Image, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(if (selectedUri == null) "Upload Foto" else "Ganti Foto")
                    }
                }
                if (selectedUri != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    AsyncImage(model = selectedUri, contentDescription = null, modifier = Modifier.height(100.dp))
                }
            }
        },
        confirmButton = {
            Button(
                enabled = !isUploading && title.isNotEmpty(),
                onClick = { onConfirm(title, content, selectedUri) }
            ) {
                Text(if (isUploading) "Uploading..." else "Simpan")
            }
        },
        dismissButton = { if(!isUploading) TextButton(onClick = onDismiss) { Text("Batal") } }
    )
}