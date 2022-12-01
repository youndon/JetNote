package com.example.jetnote.ui.top_action_bar

import android.content.Context
import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.jetnote.fb.Response.*
import com.google.android.gms.auth.api.identity.BeginSignInResult

@Composable
fun OneTapSignIn(
    viewModel: AuthVM = hiltViewModel(),
    ctx :Context,
    launch: @Composable (result: BeginSignInResult) -> Unit
) {
    when(val oneTapSignInResponse = viewModel.oneTapSignInResponse) {
        is Success -> oneTapSignInResponse.data?.let {
            launch(it)
        }
        is Failure -> LaunchedEffect(Unit) {
            Toast.makeText(ctx,oneTapSignInResponse.e.cause.toString() , Toast.LENGTH_SHORT).show()
        }
    }
}