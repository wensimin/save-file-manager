// Copyright 2000-2021 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material.Card
import androidx.compose.material.MaterialTheme
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application

const val MAX_SAVE = 10

@Composable
@Preview
fun app() {
    MaterialTheme {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(10.dp),
            contentAlignment = Alignment.TopCenter
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .fillMaxHeight()
            ) {
                var text = ""
                OutlinedTextField(
                    value = text,
                    onValueChange = { text = it },
                    label = { Text("输入需要管理的文件目录") }
                )
                Box(
                    modifier = Modifier
                        .padding(vertical = 20.dp)
                        .border(BorderStroke(2.dp, Color.LightGray))
                ) {
                    val stateVertical = rememberScrollState(0)
                    Column(
                        modifier = Modifier
                            .verticalScroll(stateVertical)
                            .padding(start = 10.dp)
                    ) {
                        repeat(MAX_SAVE) {
                            Card(
                                modifier = Modifier
                                    .padding(vertical = 10.dp)
                                    .padding(end = 20.dp)
                                    .fillMaxWidth()
                                    .height(50.dp)
                                    .clickable {
                                        //TODO save file
                                    },
                                elevation = 10.dp
                            ) {
                                Text(
                                    "test text"
                                )
                            }
                        }
                    }
                    VerticalScrollbar(
                        modifier = Modifier.align(Alignment.CenterEnd)
                            .fillMaxHeight(),
                        adapter = rememberScrollbarAdapter(stateVertical)
                    )
                }
            }

        }
    }
}

fun main() = application {
    Window(onCloseRequest = ::exitApplication, title = "存档管理工具") {
        app()
    }
}
