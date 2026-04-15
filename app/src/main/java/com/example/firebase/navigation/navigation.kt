package com.example.firebase.navigation

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.firebase.screen.Home
import com.example.firebase.screen.Login
import com.example.firebase.screen.LoginViewModel
import com.example.firebase.screen.Register
import com.example.firebase.screen.RegisterViewModel

enum class AppScreens{
    login,
    register,
    home
}

@Composable
fun Navigation(innerPadding: PaddingValues) {
    val navController = rememberNavController()
    val loginViewModel: LoginViewModel = viewModel()
    val registerViewModel: RegisterViewModel = viewModel()
    
    NavHost(
        navController = navController,
        startDestination = AppScreens.login.name,
        modifier = Modifier.padding(innerPadding)
    ){
        composable(route = AppScreens.login.name){
            Login(navController = navController, model = loginViewModel)
        }
        composable(route = AppScreens.register.name){
            Register(navController = navController, model = registerViewModel)
        }
        composable(route = AppScreens.home.name){
            Home(navController = navController)
        }
    }
}
