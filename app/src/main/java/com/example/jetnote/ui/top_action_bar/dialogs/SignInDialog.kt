package com.example.jetnote.ui.top_action_bar.dialogs

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.jetnote.R
import com.example.jetnote.cons.SURFACE_VARIANT
import com.example.jetnote.fp.getMaterialColor
import com.example.jetnote.ui.AdaptingRow

@Composable
fun SignInDialog(
    authViewModel: AuthVM = hiltViewModel(),
    signInDialogState:MutableState<Boolean>
) {
    AlertDialog(
        onDismissRequest = {
                           signInDialogState.value = false
        },
        confirmButton = {},
        title = {
            Row {
                AdaptingRow(modifier = Modifier.fillMaxWidth()) {
                    Text(text = "SignIn With", fontSize = 25.sp)
                }
            }
        },
        text = {
            Column {
                SingInButton(
                    txt = "Google",
                    icon = {
                        Image(painterResource(id = R.mipmap.google), null)
                    }
                ) {
                    authViewModel.oneTapSignIn()
                    signInDialogState.value = false
                }
                SingInButton(
                    txt = "Github",
                    icon = {
                        Image(painterResource(id = R.mipmap.github), null)
                    }
                ) {
                    signInDialogState.value = false
                }
                SingInButton(
                    txt = "Twitter",
                    icon = {
                        Image(painterResource(id = R.mipmap.twitter), null)
                    }
                ) {
                    signInDialogState.value = false
                }
            }
        }
    )
}

@Composable
fun SingInButton(
    txt: String,
    icon: @Composable () -> Unit,
    action: () -> Unit
) {
    Button(
        modifier = Modifier
            .fillMaxWidth()
            .padding(5.dp),
        onClick = action,
        colors = ButtonDefaults.buttonColors(
            containerColor = getMaterialColor(SURFACE_VARIANT)
        )
        ) {
        Row(
            horizontalArrangement = Arrangement.Start,
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 4.dp)
        ) {
            icon.invoke()
            Spacer(modifier = Modifier.width(15.dp))
            Text(text = txt, fontSize = 20.sp)
        }
    }
}