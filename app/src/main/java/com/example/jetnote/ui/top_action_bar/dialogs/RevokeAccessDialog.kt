package com.example.jetnote.ui.top_action_bar.dialogs

import androidx.compose.foundation.text.ClickableText
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.sp
import com.example.jetnote.cons.SURFACE_TINT
import com.example.jetnote.fp.getMaterialColor

@Composable
fun RevokeAccessDialog(
    dialogState: MutableState<Boolean>,
    confirmation : () -> Unit,
) {
    AlertDialog(
        onDismissRequest = { dialogState.value = false },
        confirmButton = {
            ClickableText(
                text = AnnotatedString("Confirm"),
                style = TextStyle(
                    color = getMaterialColor(SURFACE_TINT),
                    fontSize = 17.sp
                ),
                onClick = {
                    confirmation.invoke()
                    dialogState.value = false
                }
            )
        },
        dismissButton = {
            ClickableText(
                text = AnnotatedString("Cansel"),
                style = TextStyle(
                    color = getMaterialColor(SURFACE_TINT),
                    fontSize = 17.sp
                ),
                onClick = {
                    dialogState.value = false
                }
            )
        },
        title = {
            Text(text = "Revoke Access?")
        },
        text = {
            Text(text = "Revoke access to the account result in erase all you cloud data include your images and medias\n" +
                    "Type confirm button if you sure.")
        }
    )
}