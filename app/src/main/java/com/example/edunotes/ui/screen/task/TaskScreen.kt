package com.example.edunotes.ui.screen.task

import android.app.DatePickerDialog
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckBox
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Image
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.edunotes.data.model.Task
import com.example.edunotes.ui.components.EduTextField
import com.example.edunotes.ui.nav.NavRoutes
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskScreen(
    navController: NavController,
    viewModel: TaskViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val isUploading by viewModel.uploadState.collectAsState()
    var showDialog by remember { mutableStateOf(false) }

    Scaffold(
        // --- 1. TAMBAHAN NAVIGASI BAWAH (BOTTOM BAR) ---
        bottomBar = {
            NavigationBar {
                // Tombol Home (Aktif)
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
                    label = { Text("Materi") },
                    selected = false, // Karena kita sedang di HomeScreen
                    onClick = {navController.navigate(NavRoutes.Home.route)}
                )
                // Tombol Tugas
                NavigationBarItem(
                    icon = { Icon(Icons.Default.CheckBox, contentDescription = "Tugas") },
                    label = { Text("Tugas") },
                    selected = true,
                    onClick = { navController.navigate(NavRoutes.Tasks.route) }
                )
                // Tombol Catatan
                NavigationBarItem(
                    icon = { Icon(Icons.Default.EditNote, contentDescription = "Catatan") },
                    label = { Text("Catatan") },
                    selected = false,
                    onClick = { navController.navigate(NavRoutes.Notes.route) }
                )
            }
        },

        floatingActionButton = {
            FloatingActionButton(onClick = { showDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = "Tambah Tugas")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            Text("Daftar Tugas", style = MaterialTheme.typography.headlineMedium)
            Spacer(modifier = Modifier.height(16.dp))

            when (val state = uiState) {
                is TaskUiState.Loading -> CircularProgressIndicator()
                is TaskUiState.Error -> Text("Error: ${state.message}", color = Color.Red)
                is TaskUiState.Success -> {
                    if (state.tasks.isEmpty()) {
                        Text("Tidak ada tugas. Hore!", color = Color.Gray)
                    } else {
                        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(state.tasks) { task ->
                                TaskItem(
                                    task = task,
                                    onToggle = { viewModel.toggleStatus(task) },
                                    onDelete = { task.id?.let { viewModel.deleteTask(it) } }
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (showDialog) {
        AddTaskDialog(
            isUploading = isUploading,
            onDismiss = { showDialog = false },
            onConfirm = { title, date, uri ->
                val context = navController.context // Hacky context
                val bytes = uri?.let {
                    context.contentResolver.openInputStream(it)?.use { stream -> stream.readBytes() }
                }
                viewModel.addTask(title, date, bytes)
                showDialog = false
            }
        )
    }
}

@Composable
fun TaskItem(task: Task, onToggle: () -> Unit, onDelete: () -> Unit) {
    Card(
        elevation = CardDefaults.cardElevation(2.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (task.isCompleted) Color(0xFFE0E0E0) else MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 1. Checkbox
            Checkbox(checked = task.isCompleted, onCheckedChange = { onToggle() })

            // 2. Teks & Gambar
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = task.title,
                    style = MaterialTheme.typography.titleMedium,
                    textDecoration = if (task.isCompleted) TextDecoration.LineThrough else null
                )
                Text(
                    text = "Deadline: ${task.deadline ?: "-"}",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
                // Tampilkan gambar kecil jika ada lampiran
                if (task.attachmentUrl != null) {
                    Spacer(modifier = Modifier.height(4.dp))
                    AsyncImage(
                        model = task.attachmentUrl,
                        contentDescription = "Lampiran",
                        modifier = Modifier
                            .height(60.dp)
                            .clip(RoundedCornerShape(4.dp)),
                        contentScale = ContentScale.Crop
                    )
                }
            }

            // 3. Tombol Hapus
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, contentDescription = "Hapus", tint = Color.Red)
            }
        }
    }
}

@Composable
fun AddTaskDialog(
    isUploading: Boolean,
    onDismiss: () -> Unit,
    onConfirm: (String, String, Uri?) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var date by remember { mutableStateOf("") }
    var selectedUri by remember { mutableStateOf<Uri?>(null) }

    val context = LocalContext.current

    val calendar = Calendar.getInstance()
    val datePickerDialog = DatePickerDialog(
        context,
        { _, year, month, day ->
            val formattedMonth = (month + 1).toString().padStart(2, '0')
            val formattedDay = day.toString().padStart(2, '0')
            date = "$year-$formattedMonth-$formattedDay"
        },
        calendar.get(Calendar.YEAR),
        calendar.get(Calendar.MONTH),
        calendar.get(Calendar.DAY_OF_MONTH)
    )

    val photoLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { selectedUri = it }
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Tugas Baru") },
        text = {
            Column {
                EduTextField(value = title, onValueChange = { title = it }, label = "Judul Tugas")
                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = date,
                    onValueChange = {},
                    label = { Text("Deadline") },
                    readOnly = true,
                    trailingIcon = {
                        IconButton(onClick = { datePickerDialog.show() }) {
                            Icon(Icons.Default.DateRange, contentDescription = null)
                        }
                    },
                    modifier = Modifier.fillMaxWidth().clickable { datePickerDialog.show() }
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Button(onClick = {
                        photoLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                    }) {
                        Icon(Icons.Default.Image, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(if (selectedUri == null) "Foto Soal" else "Ganti Foto")
                    }
                    if (selectedUri != null) {
                        Spacer(modifier = Modifier.width(8.dp))
                        AsyncImage(model = selectedUri, contentDescription = null, modifier = Modifier.size(40.dp))
                    }
                }
            }
        },
        confirmButton = {
            Button(
                enabled = !isUploading && title.isNotEmpty() && date.isNotEmpty(),
                onClick = { onConfirm(title, date, selectedUri) }
            ) {
                Text(if (isUploading) "Menyimpan..." else "Simpan")
            }
        },
        dismissButton = {
            if (!isUploading) TextButton(onClick = onDismiss) { Text("Batal") }
        }
    )
}