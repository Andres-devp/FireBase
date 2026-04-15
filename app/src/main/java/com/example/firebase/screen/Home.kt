package com.example.firebase.screen

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.example.firebase.auth
import com.example.firebase.database
import com.example.firebase.navigation.AppScreens
import androidx.compose.ui.graphics.Color
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update


data class MyUser(
    val name : String = "",
    val lastName : String = "",
    val age : String = "",

)
class HomeViewModel : ViewModel(){
    private val _form = MutableStateFlow<MyUser>(MyUser())
    val form = _form.asStateFlow()

    private val _users = MutableStateFlow<List<MyUser>>(emptyList())
    val users: StateFlow<List<MyUser>> = _users.asStateFlow()

    private var valueEventListener: ValueEventListener? = null

    fun loadUsers() {
        val dbReference = database.getReference("users")
        valueEventListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val updatedList = mutableListOf<MyUser>()
                for (child in snapshot.children) {
                    val user = child.getValue(MyUser::class.java)
                    user?.let { updatedList.add(it) }
                }
                _users.value = updatedList
            }

            override fun onCancelled(error: DatabaseError) {
                // Manejar errores
            }
        }
        dbReference.addValueEventListener(valueEventListener!!)
    }

    fun updateName(newValue: String){
        _form.update { it.copy(name = newValue) }
    }
    fun updateLastName(newValue: String){
        _form.update { it.copy(lastName = newValue) }
    }
    fun updateAge(newValue: String){
        _form.update { it.copy(age = newValue) }
    }

    override fun onCleared() {
        super.onCleared()
        valueEventListener?.let {
            database.getReference("users").removeEventListener(it)
        }
    }
}
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Home(navController: NavHostController, model : HomeViewModel = viewModel()) {
    val form by model.form.collectAsState()
    val users by model.users.collectAsState()

    LaunchedEffect(Unit) {
        model.loadUsers()
    }
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Home") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    actionIconContentColor = MaterialTheme.colorScheme.onPrimary
                ),
                actions = {
                    IconButton(onClick = {
                        auth.signOut()
                        navController.navigate(AppScreens.login.name) {
                            popUpTo(AppScreens.home.name) { inclusive = true }
                        }
                    }) {
                        Icon(Icons.Default.ExitToApp, contentDescription = "Exit Icon")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
        ) {
            Text("Welcome to Home!")
            Text(auth.currentUser?.email ?: "No email", color = Color.Blue)

            OutlinedTextField(
                value = form.name,
                onValueChange = {model.updateName(it)},
                label = {Text("name")},
                modifier = Modifier.fillMaxWidth()

            )

            OutlinedTextField(
                value =form.lastName,
                onValueChange = {model.updateLastName(it)},
                label = {Text("last name")},
                modifier = Modifier.fillMaxWidth()

            )
            OutlinedTextField(
                value = form.age,
                onValueChange = {model.updateAge(it)},
                label = {Text("age")},
                modifier = Modifier.fillMaxWidth()

            )

            Button(modifier = Modifier.fillMaxWidth(), onClick = {
                val myRef = database.getReference("users").push()
                val user = MyUser(name = form.name, lastName = form.lastName, age = form.age)
                myRef.setValue(user)
            }) {
                Text("Save User")
            }

            LazyColumn(modifier = Modifier.fillMaxWidth().padding(top = 16.dp)) {
                items(users) { user ->
                    Text(text = "${user.name} ${user.lastName} -> ${user.age}")
                }
            }
        }
    }
}

@Preview
@Composable
fun HomePreview(){
    val c = rememberNavController()
    Home(c)
}