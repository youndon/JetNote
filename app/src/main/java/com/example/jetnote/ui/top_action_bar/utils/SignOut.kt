package com.example.jetnote.ui.top_action_bar.utils

import android.content.Context
import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.jetnote.fb.Response.*

@Composable
fun SignOut(
    viewModel: ProfileVM = hiltViewModel(),
    ctx: Context,
    action : @Composable (signedOut: Boolean) -> Unit
) {
    when(val signOutResponse = viewModel.signOutResponse) {
        is Success -> signOutResponse.data?.let { signedOut ->
            action(signedOut)
        }
        is Failure -> LaunchedEffect(Unit) {
            Toast.makeText(ctx,signOutResponse.e.cause.toString() , Toast.LENGTH_SHORT).show()
        }
    }
}