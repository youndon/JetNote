package com.example.jetnote.ui.top_action_bar

import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.res.painterResource
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.jetnote.icons.REVOKE_ACCESS_ICON
import com.example.jetnote.icons.SIGN_IN_ICON
import com.example.jetnote.icons.SIGN_OUT_ICON

@Composable
fun AccountListMenu(
    profileVM: ProfileVM = hiltViewModel(),
    authViewModel: AuthVM = hiltViewModel(),
    expandedState: MutableState<Boolean>?,
    signInDialogState: MutableState<Boolean>?,
    revokeAccessDialogState: MutableState<Boolean>?
) {
    DropdownMenu(
        expanded = expandedState?.value == true,
        onDismissRequest = {
            expandedState?.value = false
        }
    ) {
        DropdownMenuItem(
            text = {
                if (authViewModel.isUserAuthenticated) {
                    Text(text = "Sign-Out")
                } else {
                    Text(text = "Sign-In")
                }
            },
            leadingIcon = {
                if (authViewModel.isUserAuthenticated) Icon(painterResource(SIGN_OUT_ICON),null)
                else Icon(painterResource(SIGN_IN_ICON),null)
                },
            onClick = {
                if (authViewModel.isUserAuthenticated) {
                    profileVM.signOut()
                } else {
                    signInDialogState?.value = true
                }
                expandedState?.value = false
            }
        )
        if (authViewModel.isUserAuthenticated) {
            DropdownMenuItem(
                text = {
                    Text(text = "Revoke-Access")
                },
                leadingIcon = {
                              Icon(painterResource(REVOKE_ACCESS_ICON),null)
                },
                onClick = {
                    revokeAccessDialogState?.value = true
                    expandedState?.value = false
                }
            )
        }
    }
}