package com.example.edunotes.ui.screen.home

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.edunotes.data.model.Category
import com.example.edunotes.ui.components.EduTextField
import com.example.edunotes.ui.nav.NavRoutes

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    navController: NavController,
    viewModel: HomeViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val isUploading by viewModel.uploadState.collectAsState()

    // State untuk Dialog (Bisa Mode Tambah atau Edit)
    var showDialog by remember { mutableStateOf(false) }
    var categoryToEdit by remember { mutableStateOf<Category?>(null) } // Data kategori yg mau diedit

    val context = LocalContext.current

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    categoryToEdit = null // Reset jadi mode tambah
                    showDialog = true
                },
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(Icons.Default.Add, contentDescription = "Tambah", tint = Color.White)
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            // --- HEADER DINAMIS ---
            val userName = (uiState as? HomeUiState.Success)?.userName ?: "Pelajar"

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Halo, $userName!", style = MaterialTheme.typography.titleMedium, color = Color.Gray)
                    Text("Mau belajar apa?", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                }
                IconButton(
                    onClick = { navController.navigate(NavRoutes.Profile.route) },
                    modifier = Modifier
                        .size(48.dp)
                        .background(MaterialTheme.colorScheme.primaryContainer, CircleShape)
                ) {
                    Icon(Icons.Default.Person, contentDescription = "Profil")
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // --- KONTEN GRID ---
            when (val state = uiState) {
                is HomeUiState.Loading -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
                is HomeUiState.Error -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(state.message, color = Color.Red)
                        Button(onClick = { viewModel.loadData() }) { Text("Reload") }
                    }
                }
                is HomeUiState.Success -> {
                    if (state.categories.isEmpty()) {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text("Belum ada kategori.", color = Color.Gray)
                        }
                    } else {
                        LazyVerticalGrid(
                            columns = GridCells.Fixed(2),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(state.categories) { category ->
                                CategoryItem(
                                    category = category,
                                    onClick = {
                                        Toast.makeText(context, "Membuka ${category.name}", Toast.LENGTH_SHORT).show()
                                        // TODO: Navigasi ke Materi
                                    },
                                    onEdit = {
                                        categoryToEdit = category // Set data yg mau diedit
                                        showDialog = true // Buka dialog
                                    },
                                    onDelete = {
                                        category.id?.let { viewModel.deleteCategory(it) }
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // --- DIALOG (REUSABLE UNTUK ADD & EDIT) ---
    if (showDialog) {
        CategoryFormDialog(
            initialCategory = categoryToEdit, // Kirim data jika mode edit
            isUploading = isUploading,
            onDismiss = { showDialog = false },
            onConfirm = { name, uri ->
                val bytes = uri?.let {
                    context.contentResolver.openInputStream(it)?.use { stream -> stream.readBytes() }
                }

                if (categoryToEdit == null) {
                    // Mode TAMBAH
                    viewModel.addCategory(name, bytes)
                } else {
                    // Mode EDIT
                    categoryToEdit?.id?.let { id ->
                        viewModel.updateCategory(id, name, bytes)
                    }
                }
                showDialog = false
            }
        )
    }
}

// --- ITEM GRID KATEGORI ---
@Composable
fun CategoryItem(
    category: Category,
    onClick: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(160.dp) // Sedikit lebih tinggi buat muat tombol
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // Konten Tengah
            Column(
                modifier = Modifier.align(Alignment.Center),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (category.iconUrl != null) {
                    AsyncImage(
                        model = category.iconUrl,
                        contentDescription = null,
                        modifier = Modifier.size(60.dp).padding(bottom = 8.dp),
                        contentScale = ContentScale.Fit
                    )
                } else {
                    Icon(Icons.Default.Add, contentDescription = null, Modifier.size(40.dp), tint = Color.Gray)
                }
                Text(category.name, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
            }

            // Tombol Edit (Kiri Atas)
            IconButton(
                onClick = onEdit,
                modifier = Modifier.align(Alignment.TopStart)
            ) {
                Icon(Icons.Default.Edit, contentDescription = "Edit", tint = MaterialTheme.colorScheme.primary)
            }

            // Tombol Hapus (Kanan Atas)
            IconButton(
                onClick = onDelete,
                modifier = Modifier.align(Alignment.TopEnd)
            ) {
                Icon(Icons.Default.Delete, contentDescription = "Hapus", tint = MaterialTheme.colorScheme.error)
            }
        }
    }
}

// --- DIALOG FORM (GABUNGAN ADD & EDIT) ---
@Composable
fun CategoryFormDialog(
    initialCategory: Category? = null, // Null = Mode Tambah
    isUploading: Boolean,
    onDismiss: () -> Unit,
    onConfirm: (String, Uri?) -> Unit
) {
    // Jika mode edit, isi default value dari data yang ada
    var name by remember { mutableStateOf(initialCategory?.name ?: "") }
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }

    // Tentukan Judul
    val title = if (initialCategory == null) "Tambah Kategori" else "Edit Kategori"

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri -> selectedImageUri = uri }
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Column {
                EduTextField(value = name, onValueChange = { name = it }, label = "Nama Kategori")
                Spacer(modifier = Modifier.height(16.dp))

                Text("Icon:", fontSize = 12.sp, color = Color.Gray)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color.LightGray)
                        .clickable { photoPickerLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) },
                    contentAlignment = Alignment.Center
                ) {
                    // Prioritas Tampilan Gambar:
                    // 1. Gambar baru dipilih (Uri)
                    // 2. Gambar lama dari internet (initialCategory.iconUrl)
                    // 3. Placeholder kosong
                    if (selectedImageUri != null) {
                        AsyncImage(model = selectedImageUri, contentDescription = null, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                    } else if (initialCategory?.iconUrl != null) {
                        AsyncImage(model = initialCategory.iconUrl, contentDescription = null, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Fit)
                    } else {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.Add, contentDescription = null)
                            Text("Ganti Gambar", fontSize = 10.sp)
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                enabled = !isUploading,
                onClick = { if (name.isNotEmpty()) onConfirm(name, selectedImageUri) }
            ) {
                if (isUploading) {
                    CircularProgressIndicator(modifier = Modifier.size(16.dp), color = Color.White)
                    Text("Menyimpan...")
                } else {
                    Text("Simpan")
                }
            }
        },
        dismissButton = {
            if (!isUploading) TextButton(onClick = onDismiss) { Text("Batal") }
        }
    )
}