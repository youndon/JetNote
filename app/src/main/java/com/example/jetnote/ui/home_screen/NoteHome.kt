package com.example.jetnote.ui.home_screen

import android.annotation.SuppressLint
import android.app.Activity
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.Scaffold
import androidx.compose.material.SnackbarResult
import androidx.compose.material.rememberScaffoldState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.core.graphics.drawable.toBitmap
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.ImageLoader
import coil.request.ImageRequest
import com.example.jetnote.cons.*
import com.example.jetnote.db.entities.Entity
import com.example.jetnote.db.entities.label.Label
import com.example.jetnote.db.entities.note.Note
import com.example.jetnote.ds.DataStore
import com.example.jetnote.cons.Screens.HOME_SCREEN
import com.example.jetnote.fp.getMaterialColor
import com.example.jetnote.icons.PLUS_ICON
import com.example.jetnote.ui.layouts.VerticalGrid
import com.example.jetnote.ui.navigation_drawer.NavigationDrawer
import com.example.jetnote.ui.note_card.NoteCard
import com.example.jetnote.ui.snackebars.UndoSnackbar
import com.example.jetnote.ui.top_action_bar.*
import com.example.jetnote.ui.top_action_bar.dialogs.RevokeAccessDialog
import com.example.jetnote.ui.top_action_bar.dialogs.SignInDialog
import com.example.jetnote.ui.top_action_bar.utils.SignOut
import com.example.jetnote.vm.*
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import com.jakewharton.processphoenix.ProcessPhoenix
import kotlinx.coroutines.launch
import java.util.*

@SuppressLint(
    "UnusedMaterial3ScaffoldPaddingParameter",
    "UnusedMaterialScaffoldPaddingParameter",
    "UnrememberedMutableState"
)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoteHome(
    noteVM: NoteVM = hiltViewModel(),
    authViewModel: AuthVM = hiltViewModel(),
    profileVM: ProfileVM = hiltViewModel(),
    firestoreViewModel: FirestoreVM = hiltViewModel(),
    storageViewModel: StorageVM = hiltViewModel(),
    entityVM: EntityVM = hiltViewModel(),
    navController: NavController,
) {
    val ctx = LocalContext.current
    val scope = rememberCoroutineScope()
    //
    val searchTitleState = remember { mutableStateOf("") }
    val searchLabelState = remember { mutableStateOf(Label()) }
    //
    val noteDataStore = DataStore(ctx)
    //
    val orderBy = noteDataStore.getOrder.collectAsState("").value
    // the true value is 'list' layout by default and false is 'grid'.
    val currentLayout = noteDataStore.getLayout.collectAsState(true).value
    //
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    //
    val scaffoldState = rememberScaffoldState()
//     to observer notes while changing immediately.
    val observerLocalNotes: State<List<Entity>> = when (orderBy) {
        ORDER_BY_NAME -> remember(entityVM, entityVM::allNotesByName).collectAsState()
        ORDER_BY_OLDEST -> remember(entityVM, entityVM::allNotesByOldest).collectAsState()
        ORDER_BY_NEWEST -> remember(entityVM, entityVM::allNotesByNewest).collectAsState()
        ORDER_BY_PRIORITY -> remember(entityVM, entityVM::allNotesByPriority).collectAsState()
        ORDER_BY_REMINDER -> remember(entityVM, entityVM::allRemindingNotes).collectAsState()
        else -> remember(entityVM, entityVM::allNotesById).collectAsState()
    }
    val observerRemoteNotes = remember(firestoreViewModel, firestoreViewModel::notes)

    val uid = UUID.randomUUID().toString()
    val topAppBarState = rememberTopAppBarState()
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior(topAppBarState)
    val lazyListState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()

    val trashedNotesState = remember(entityVM) { entityVM.allTrashedNotes }.collectAsState()

    val signInDialogState = remember { mutableStateOf(false) }
    val revokeAccessState = remember { mutableStateOf(false) }
    val expandedState = remember { mutableStateOf(false) }
    val expandedSortMenuState = remember { mutableStateOf(false) }

    val refreshState = rememberSwipeRefreshState(
        authViewModel.isLoading ||
                profileVM.isLoading ||
                firestoreViewModel.isLoading ||
                noteVM.isProcessing
    )

    val selectionState = remember { mutableStateOf(false) }
    val selectedNotes = remember { mutableStateListOf<Note>() }

    //undo snackbar.
    val undo = UndoSnackbar(
        viewModule = noteVM,
        scaffoldState = scaffoldState,
        scope = coroutineScope,
        trashedNotesState = trashedNotesState
    )

    if (signInDialogState.value) {
        SignInDialog(signInDialogState = signInDialogState)
    }

    if (revokeAccessState.value) {
        RevokeAccessDialog(dialogState = revokeAccessState) {
            runCatching {
                firestoreViewModel.apply {
                    notes.value.data?.let {
                        deleteAllDataFromCloud(it)
                        storageViewModel.deleteAllNotesDataFromStorage(it)
                    }
                }
            }.onSuccess {
                profileVM.revokeAccess()
            }
        }
    }

    SignInWithGoogle(ctx = ctx) {
        if (it) {
            LaunchedEffect(it) {
                ProcessPhoenix.triggerRebirth(ctx)
            }
        }
    }

    SignOut(
        ctx = ctx,
        action = {
            if (it) {
                LaunchedEffect(it) {
                    navController.navigate(HOME_ROUTE)
                }
            }
        }
    )

    val launcher =
        rememberLauncherForActivityResult(ActivityResultContracts.StartIntentSenderForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                runCatching {
                    val credentials =
                        authViewModel.oneTapClient.getSignInCredentialFromIntent(result.data)
                    val googleIdToken = credentials.googleIdToken
                    val googleCredentials = GoogleAuthProvider.getCredential(googleIdToken, null)
                    authViewModel.signInWithGoogle(googleCredentials)
                }.onFailure {
                    Toast.makeText(ctx, it.cause.toString(), Toast.LENGTH_SHORT).show()
                }
            }
        }

    @RequiresApi(Build.VERSION_CODES.DONUT)
    fun launch(signInResult: BeginSignInResult) {
        val intent = IntentSenderRequest.Builder(signInResult.pendingIntent.intentSender).build()
        launcher.launch(intent)
    }

    OneTapSignIn(
        ctx = ctx,
        launch = {
            LaunchedEffect(it) {
                launch(it)
            }
        }
    )

    fun showSnackBar() = coroutineScope.launch {
        val result = scaffoldState.snackbarHostState.showSnackbar(
            message = "You need to re-authenticate before revoking the access.",
            actionLabel = "Sign-out"
        )
        if (result == SnackbarResult.ActionPerformed) {
            profileVM.signOut()
        }
    }

    RevokeAccess(
        action = { accessRevoked ->
            if (accessRevoked) {
                LaunchedEffect(accessRevoked) {
                    navController.navigate(HOME_ROUTE)
                }
            }
        },
        showSnackBar = {
            showSnackBar()
        }
    )

    ModalNavigationDrawer(
        drawerContent = {
            NavigationDrawer(
                drawerState = drawerState,
                navController = navController,
                searchLabel = searchLabelState,
                searchTitle = searchTitleState
            )
        },
        drawerState = drawerState
    ) {
        Scaffold(
            modifier = Modifier
                .navigationBarsPadding()
                .nestedScroll(scrollBehavior.nestedScrollConnection),
            scaffoldState = scaffoldState,
            backgroundColor = getMaterialColor(SURFACE),
            topBar = {
                if (selectionState.value) {
                    SelectionTopAppBar(selectionState = selectionState, selectedNotes = selectedNotes)
                } else {
                    NoteTopAppBar(
                        searchNoteTitle = searchTitleState,
                        dataStore = noteDataStore,
                        scrollBehavior = scrollBehavior,
                        drawerState = drawerState,
                        thisHomeScreen = true,
                        confirmationDialogState = null,
                        expandedSortMenuState = expandedSortMenuState,
                        expandedAccountMenuState = expandedState,
                        signInDialogState = signInDialogState,
                        revokeAccessDialogState = revokeAccessState,
                        searchScreen = SEARCH_IN_LOCAL,
                        label = searchLabelState
                    )
                }
            },
            floatingActionButton = {
                ExtendedFloatingActionButton(
                    text = {
                        Text("Compose")
                    },
                    icon = {
                        Icon(
                            painter = painterResource(id = PLUS_ICON),
                            null
                        )
                    },
                    onClick = {
                        navController.navigate("$ADD_ROUTE/$uid")
                    },
                    expanded = scrollBehavior.state.collapsedFraction != 1f,
                    containerColor = getMaterialColor(SURFACE_VARIANT),
                    contentColor = contentColorFor(
                        backgroundColor = getMaterialColor(SURFACE_VARIANT)
                    ),
                    modifier = Modifier.navigationBarsPadding()
                )
            }
        ) {
            SwipeRefresh(modifier = Modifier.fillMaxSize(),
                state = refreshState, onRefresh = {
                    observerRemoteNotes.value.data?.forEach { note ->
                        scope.launch {
                            // save remote notes in local database.
                            noteVM.addNote(note)

                            // save remote images in local images file.
                            val ss = ImageLoader(ctx)
                            val cc = ImageRequest.Builder(ctx)
                                .data(note.imageUrl)
                                .target {
                                    noteVM.saveImageLocally(
                                        img = it.toBitmap(),
                                        path = ctx.filesDir.path + "/" + IMAGE_FILE,
                                        name = note.uid + "." + JPEG
                                    )
                                }
                                .build()
                            ss.enqueue(cc)

                            // save the audio file locally.
                            note.audioUrl?.let { it1 ->
                                noteVM.saveAudioLocally(
                                    it1,
                                    ctx.filesDir.path + "/" + AUDIO_FILE,
                                    note.uid + "." + MP3)
                            }
                        }
                    }

                }) {
                if (currentLayout) {
                    LazyColumn(
                        state = lazyListState,
                        modifier = Modifier
                            .fillMaxSize(),
                    ) {
                        items(
                            observerLocalNotes.value.filter {
                                it.note.title?.contains(searchTitleState.value, true) ?: true ||
                                        it.labels.contains(searchLabelState.value)
                            }
                        ) { entity ->
                            NoteCard(
                                entity = entity,
                                navController = navController,
                                forScreen = HOME_SCREEN,
                                selectionState = selectionState,
                                selectedNotes = selectedNotes
                            ) {
                                noteVM.updateNote(
                                    Note(
                                        title = it.note.title,
                                        description = it.note.description,
                                        priority = it.note.priority,
                                        uid = it.note.uid,
                                        color = it.note.color,
                                        textColor = it.note.textColor,
                                        trashed = 1
                                    )
                                )
                                undo.invoke(entity.note)
                            }
                        }
                    }
                } else {
                    LazyColumn(
                        state = lazyListState,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        item {
                            VerticalGrid(
                                maxColumnWidth = 220.dp
                            ) {
                                observerLocalNotes.value.filter {
                                    it.note.title?.contains(searchTitleState.value, true) ?: true ||
                                            it.labels.contains(searchLabelState.value)
                                }.forEach { entity ->
                                    NoteCard(
                                        entity = entity,
                                        navController = navController,
                                        forScreen = HOME_SCREEN,
                                        selectionState = selectionState,
                                        selectedNotes = selectedNotes
                                    ) {
                                        noteVM.updateNote(
                                            Note(
                                                title = it.note.title,
                                                description = it.note.description,
                                                priority = it.note.priority,
                                                uid = it.note.uid,
                                                color = it.note.color,
                                                textColor = it.note.textColor,
                                                trashed = 1
                                            )
                                        )
                                        undo.invoke(entity.note)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}