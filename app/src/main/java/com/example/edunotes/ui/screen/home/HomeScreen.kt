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
import androidx.compose.material.icons.filled.CheckBox
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.Home
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

    var showDialog by remember { mutableStateOf(false) }
    var categoryToEdit by remember { mutableStateOf<Category?>(null) }

    val context = LocalContext.current

    Scaffold(
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
                    label = { Text("Materi") },
                    selected = true,
                    onClick = {}
                )

                NavigationBarItem(
                    icon = { Icon(Icons.Default.CheckBox, contentDescription = "Tugas") },
                    label = { Text("Tugas") },
                    selected = false,
                    onClick = { navController.navigate(NavRoutes.Tasks.route) }
                )

                NavigationBarItem(
                    icon = { Icon(Icons.Default.EditNote, contentDescription = "Catatan") },
                    label = { Text("Catatan") },
                    selected = false,
                    onClick = { navController.navigate(NavRoutes.Notes.route) }
                )
            }
        },


        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    categoryToEdit = null
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
                                        if (category.id != null) {
                                            val route = NavRoutes.MaterialList.createRoute(
                                                categoryId = category.id,
                                                categoryName = category.name
                                            )
                                            navController.navigate(route)
                                        } else {
                                            Toast.makeText(context, "Error: ID Kategori Null", Toast.LENGTH_SHORT).show()
                                        }
                                    },
                                    onEdit = {
                                        categoryToEdit = category
                                        showDialog = true
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

    if (showDialog) {
        CategoryFormDialog(
            initialCategory = categoryToEdit,
            isUploading = isUploading,
            onDismiss = { showDialog = false },
            onConfirm = { name, uri ->
                val bytes = uri?.let {
                    context.contentResolver.openInputStream(it)?.use { stream -> stream.readBytes() }
                }

                if (categoryToEdit == null) {
                    viewModel.addCategory(name, bytes)
                } else {
                    categoryToEdit?.id?.let { id ->
                        viewModel.updateCategory(id, name, bytes)
                    }
                }
                showDialog = false
            }
        )
    }
}

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
            .height(160.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
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

            IconButton(onClick = onEdit, modifier = Modifier.align(Alignment.TopStart)) {
                Icon(Icons.Default.Edit, contentDescription = "Edit", tint = MaterialTheme.colorScheme.primary)
            }

            IconButton(onClick = onDelete, modifier = Modifier.align(Alignment.TopEnd)) {
                Icon(Icons.Default.Delete, contentDescription = "Hapus", tint = MaterialTheme.colorScheme.error)
            }
        }
    }
}

@Composable
fun CategoryFormDialog(
    initialCategory: Category? = null,
    isUploading: Boolean,
    onDismiss: () -> Unit,
    onConfirm: (String, Uri?) -> Unit
) {
    var name by remember { mutableStateOf(initialCategory?.name ?: "") }
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
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