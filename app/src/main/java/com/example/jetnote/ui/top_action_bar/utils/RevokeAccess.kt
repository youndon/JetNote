package com.example.jetnote.ui.top_action_bar

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.jetnote.fb.Response.Failure
import com.example.jetnote.fb.Response.Success

@Composable
fun RevokeAccess(
    viewModel: ProfileVM = hiltViewModel(),
    action: @Composable (accessRevoked: Boolean) -> Unit,
    showSnackBar: () -> Unit
) {
    when(val revokeAccessResponse = viewModel.revokeAccessResponse) {
        is Success -> revokeAccessResponse.data?.let {
            action(it)
        }
        is Failure -> LaunchedEffect(Unit) {
            showSnackBar()
        }
    }
}