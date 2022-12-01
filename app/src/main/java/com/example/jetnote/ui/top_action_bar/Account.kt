package com.example.jetnote.ui.top_action_bar

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.jetnote.icons.USER_ICON
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

@Composable
internal fun Account(
    profileVM: ProfileVM = hiltViewModel(),
    expandedState: MutableState<Boolean>?
) {
    val ctx = LocalContext.current
    if (Firebase.auth.currentUser != null) {
        AsyncImage(
            model = ImageRequest.Builder(ctx)
                .data(profileVM.photoUrl)
                .crossfade(true)
                .build(),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .clip(CircleShape)
                .size(30.dp)
                .clickable {
                    expandedState?.value = true
                }
        )
    } else {
        Icon(
            painter = painterResource(USER_ICON),
            contentDescription = null,
            modifier = Modifier.clickable {
                expandedState?.value = true
            }
        )
    }

}

