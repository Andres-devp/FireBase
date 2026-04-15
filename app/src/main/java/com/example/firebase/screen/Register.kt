package com.example.firebase.screen

import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.navigation.NavHostController
import com.example.firebase.model.validEmailAddress
import com.example.firebase.navigation.AppScreens
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.auth.userProfileChangeRequest
import com.google.firebase.ktx.Firebase
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.asImageBitmap
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update


data class RegisterState(
    val nombre: String = "",
    val apellido: String = "",
    val email: String = "",
    val password: String = "",
    val nombreError: String = "",
    val apellidoError: String = "",
    val emailError: String = "",
    val passwordError: String = ""
)

class RegisterViewModel : ViewModel() {
    private val _registerState = MutableStateFlow(RegisterState())
    val registerState = _registerState.asStateFlow()

    fun updateName(newValue: String) {
        _registerState.update { it.copy(nombre = newValue) } }
    fun updateLastName(newValue: String) {
        _registerState.update { it.copy(apellido = newValue) } }
    fun updateEmail(newValue: String) {
        _registerState.update { it.copy(email = newValue) } }
    fun updatePassword(newValue: String) {
        _registerState.update { it.copy(password = newValue) } }
    
    fun updateNameError(newValue: String) {
        _registerState.update { it.copy(nombreError = newValue) } }
    fun updateLastError(newValue: String) {
        _registerState.update { it.copy(apellidoError = newValue) } }
    fun updateEmailError(newValue: String) {
        _registerState.update { it.copy(emailError = newValue) } }
    fun updatePassError(newValue: String) {
        _registerState.update { it.copy(passwordError = newValue) } }
}


@Composable
fun Register(navController: NavHostController, model: RegisterViewModel) {
    val state by model.registerState.collectAsState()
    val context = LocalContext.current
    val auth = Firebase.auth

    val imageUri = remember { mutableStateOf<Uri?>(null) }
    val bitmap = remember { mutableStateOf<Bitmap?>(null) }

    val galleryLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        imageUri.value = uri
        bitmap.value = null
    }

    val cameraLauncher = rememberLauncherForActivityResult(ActivityResultContracts.TakePicturePreview()) { b: Bitmap? ->
        bitmap.value = b
        imageUri.value = null
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(10.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (imageUri.value != null) {
            val bmp = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                val source = ImageDecoder.createSource(context.contentResolver, imageUri.value!!)
                ImageDecoder.decodeBitmap(source)
            } else {
                MediaStore.Images.Media.getBitmap(context.contentResolver, imageUri.value)
            }
            Image(bitmap = bmp.asImageBitmap(), contentDescription = null, modifier = Modifier.size(100.dp))
        } else if (bitmap.value != null) {
            Image(bitmap = bitmap.value!!.asImageBitmap(), contentDescription = null, modifier = Modifier.size(100.dp))
        }

        Row(modifier = Modifier.padding(bottom = 16.dp)) {
            Button(onClick = { galleryLauncher.launch("image/*") }) { Text("Gallery") }
            Spacer(modifier = Modifier.width(16.dp))
            Button(onClick = { cameraLauncher.launch(null) }) { Text("Camera") }
        }

        TextField(
            value = state.nombre,
            onValueChange = { model.updateName(it) },
            label = { Text("First Name") },
            modifier = Modifier.fillMaxWidth(),
            isError = state.nombreError.isNotEmpty(),
            supportingText = { if (state.nombreError.isNotEmpty()) Text(state.nombreError) }
        )
        
        TextField(
            value = state.apellido,
            onValueChange = { model.updateLastName(it) },
            label = { Text("Last Name") },
            modifier = Modifier.fillMaxWidth(),
            isError = state.apellidoError.isNotEmpty(),
            supportingText = { if (state.apellidoError.isNotEmpty()) Text(state.apellidoError) }
        )

        TextField(
            value = state.email,
            onValueChange = { model.updateEmail(it) },
            label = { Text("Email") },
            modifier = Modifier.fillMaxWidth(),
            isError = state.emailError.isNotEmpty(),
            supportingText = { if (state.emailError.isNotEmpty()) Text(state.emailError) }
        )

        TextField(
            value = state.password,
            onValueChange = { model.updatePassword(it) },
            label = { Text("Password") },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            visualTransformation = PasswordVisualTransformation(),
            isError = state.passwordError.isNotEmpty(),
            supportingText = { if (state.passwordError.isNotEmpty()) Text(state.passwordError) }
        )

        Button(
            modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
            onClick = {
                if (validateRegisterForm(model, state)) {
                    auth.createUserWithEmailAndPassword(state.email, state.password)
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                val user = auth.currentUser
                                val profileUpdates = userProfileChangeRequest {
                                    displayName = "${state.nombre} ${state.apellido}"
                                    photoUri = Uri.parse("path/to/pic")
                                }
                                user?.updateProfile(profileUpdates)?.addOnCompleteListener { updateTask ->
                                    if (updateTask.isSuccessful) {
                                        navController.navigate(AppScreens.home.name) {
                                            popUpTo(AppScreens.login.name) { inclusive = true }
                                        }
                                    }
                                }
                            } else {
                                Toast.makeText(context, "Error: ${task.exception?.message}", Toast.LENGTH_LONG).show()
                            }
                        }
                }
            }
        ) {
            Text("Register")
        }
    }
}

fun validateRegisterForm(model: RegisterViewModel, state: RegisterState): Boolean {
    var isValid = true
    if (state.nombre.isEmpty()) {
        model.updateNameError("Name is empty"); isValid = false } else { model.updateNameError("") }
    if (state.apellido.isEmpty()) {
        model.updateLastError("Last name is empty"); isValid = false } else { model.updateLastError("") }
    if (!validEmailAddress(state.email)) {
        model.updateEmailError("Not a valid address"); isValid = false } else { model.updateEmailError("") }
    if (state.password.length < 6) {
        model.updatePassError("Password must be 6+ chars"); isValid = false } else { model.updatePassError("") }
    return isValid
}