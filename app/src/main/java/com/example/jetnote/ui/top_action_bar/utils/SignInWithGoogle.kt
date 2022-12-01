package com.example.jetnote.ui.top_action_bar

import android.content.Context
import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.jetnote.fb.Response.*

@Composable
fun SignInWithGoogle(
    viewModel: AuthVM = hiltViewModel(),
    ctx:Context,
    action: @Composable (signedIn: Boolean) -> Unit
) {
    when(val signInWithGoogleResponse = viewModel.signInWithGoogleResponse) {
        is Success -> signInWithGoogleResponse.data?.let { signedIn ->
            action(signedIn)
        }
        is Failure -> LaunchedEffect(Unit) {
            Toast.makeText(ctx,signInWithGoogleResponse.e.cause.toString() , Toast.LENGTH_SHORT).show()
        }
    }
}