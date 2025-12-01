package com.example.edunotes.ui.screen.profile

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Save
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
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.edunotes.ui.components.EduButton
import com.example.edunotes.ui.components.EduTextField

@Composable
fun ProfileScreen(
    navController: NavController,
    onLogout: () -> Unit,
    viewModel: ProfileViewModel = viewModel()
) {
    // 1. Observe State
    val uiState by viewModel.uiState.collectAsState()
    val isUploading by viewModel.isUploading.collectAsState()

    // 2. State Lokal Data Form
    var fullName by remember { mutableStateOf("") }
    var schoolName by remember { mutableStateOf("") }
    var avatarUrl by remember { mutableStateOf<String?>(null) }

    // 3. State Mode Edit & Gambar Pilihan
    var isEditing by remember { mutableStateOf(false) }
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }

    val context = LocalContext.current

    // Mengisi data saat load berhasil (dari Server)
    LaunchedEffect(uiState) {
        if (uiState is ProfileUiState.Success) {
            val data = (uiState as ProfileUiState.Success).profile
            fullName = data?.fullName ?: ""
            schoolName = data?.schoolName ?: ""
            avatarUrl = data?.avatarUrl
        }
    }

    // --- LAUNCHER GALERI (INI KUNCINYA) ---
    val photoLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri -> selectedImageUri = uri }
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // --- HEADER ---
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = if (isEditing) "Edit Profil" else "Detail Profil",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            // Tombol Edit (Hanya muncul jika TIDAK sedang mengedit)
            if (!isEditing) {
                IconButton(onClick = { isEditing = true }) {
                    Icon(Icons.Default.Edit, contentDescription = "Edit", tint = MaterialTheme.colorScheme.primary)
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // --- AVATAR (FOTO PROFIL) ---
        Box(
            modifier = Modifier
                .size(120.dp)
                .clip(CircleShape)
                .background(Color.LightGray)
                // Hanya bisa diklik saat Mode Edit
                .clickable(enabled = isEditing) {
                    photoLauncher.launch(
                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                    )
                },
            contentAlignment = Alignment.Center
        ) {
            // Prioritas Tampilan:
            // 1. Gambar Baru (Preview) -> 2. Gambar Server (Lama) -> 3. Icon Default
            if (selectedImageUri != null) {
                AsyncImage(model = selectedImageUri, contentDescription = null, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
            } else if (avatarUrl != null) {
                AsyncImage(model = avatarUrl, contentDescription = null, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
            } else {
                Icon(Icons.Default.Person, contentDescription = null, modifier = Modifier.size(60.dp), tint = Color.Gray)
            }

            // Overlay Icon Kamera (Hanya muncul saat Mode Edit)
            if (isEditing) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.3f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.CameraAlt, contentDescription = "Ganti Foto", tint = Color.White)
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // --- KONTEN DATA DIRI ---
        if (isEditing) {
            // MODE EDIT: Tampilkan TextField
            EduTextField(value = fullName, onValueChange = { fullName = it }, label = "Nama Lengkap")
            Spacer(modifier = Modifier.height(16.dp))
            EduTextField(value = schoolName, onValueChange = { schoolName = it }, label = "Asal Sekolah")

            Spacer(modifier = Modifier.height(32.dp))

            // Tombol Aksi (Simpan & Batal)
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                // Tombol Batal
                OutlinedButton(
                    onClick = {
                        isEditing = false
                        selectedImageUri = null // Reset gambar pilihan
                        // Reload data asli (opsional, bisa panggil viewModel.loadProfile() lagi)
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.Close, contentDescription = null)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Batal")
                }

                // Tombol Simpan
                Button(
                    onClick = {
                        // Ubah URI ke ByteArray untuk Upload
                        val bytes = selectedImageUri?.let { uri ->
                            context.contentResolver.openInputStream(uri)?.use { it.readBytes() }
                        }

                        viewModel.updateProfile(fullName, schoolName, bytes)
                        Toast.makeText(context, "Menyimpan...", Toast.LENGTH_SHORT).show()

                        // isEditing akan diset false nanti kalau sukses (atau manual disini)
                        isEditing = false
                    },
                    enabled = !isUploading,
                    modifier = Modifier.weight(1f)
                ) {
                    if (isUploading) {
                        CircularProgressIndicator(modifier = Modifier.size(16.dp), color = Color.White)
                    } else {
                        Icon(Icons.Default.Save, contentDescription = null)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Simpan")
                    }
                }
            }

        } else {
            // MODE DETAIL: Tampilkan Teks Biasa (Read-Only)
            ProfileInfoItem(label = "Nama Lengkap", value = fullName.ifEmpty { "-" })
            Spacer(modifier = Modifier.height(16.dp))
            ProfileInfoItem(label = "Asal Sekolah", value = schoolName.ifEmpty { "-" })

            Spacer(modifier = Modifier.height(48.dp))

            // Tombol Logout
            OutlinedButton(
                onClick = {
                    viewModel.logout()
                    onLogout()
                },
                modifier = Modifier.fillMaxWidth().height(50.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)
            ) {
                Icon(Icons.Default.ExitToApp, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Keluar (Logout)")
            }
        }
    }
}

// Komponen Helper untuk tampilan Detail
@Composable
fun ProfileInfoItem(label: String, value: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
            .padding(16.dp)
    ) {
        Text(text = label, style = MaterialTheme.typography.labelMedium, color = Color.Gray)
        Spacer(modifier = Modifier.height(4.dp))
        Text(text = value, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
    }
}