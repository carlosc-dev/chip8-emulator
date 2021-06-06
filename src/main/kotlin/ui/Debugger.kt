package ui

import androidx.compose.foundation.VerticalScrollbar
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollbarAdapter
import androidx.compose.material.Card
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun Debugger(commands: List<String>) {
    val state = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()

    coroutineScope.launch {
        state.scrollToItem(commands.lastIndex)
    }

    Card(modifier = Modifier.fillMaxWidth().fillMaxHeight()) {
        Box(modifier = Modifier.padding(1.dp).fillMaxSize().background(Color.Black)) {
            LazyColumn(state = state) {
                items(commands) { com ->
                    Text("> $com", modifier = Modifier.fillMaxWidth(), color = Color.White)

                }
            }
            VerticalScrollbar(
                modifier = Modifier.align(Alignment.CenterEnd).fillMaxHeight().background(Color.White),
                adapter = rememberScrollbarAdapter(
                    scrollState = state
                )
            )
        }
    }
}