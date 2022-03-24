// Copyright 2000-2021 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import javax.swing.JFileChooser

const val MAX_SAVE = 10
const val BASE_BACKUP_DIR = "backup"
const val CONFIG_FILE = "config.json"
val DATA_FORMAT = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
val objectMapper = ObjectMapper().apply {
    this.registerKotlinModule()
}


val backupPath = mutableStateOf("")
val showConfirm = mutableStateOf(false)
val confirmMessage = mutableStateOf("")
val confirmAction = mutableStateOf({})

@Composable
@Preview
fun App() {
    val backups = remember {
        arrayOfNulls<Backup>(MAX_SAVE).also {
            initData(it)
        }
    }

    if (showConfirm.value) Confirm()

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
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Start,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        modifier = Modifier.weight(1f),
                        value = backupPath.value,
                        onValueChange = {
                            backupPath.value = it
                            saveConfig(backups)
                        },
                        label = { Text("输入需要管理的文件目录") },
                    )
                    Button(
                        onClick = {
                            openFileDialog {
                                backupPath.value = it
                                saveConfig(backups)
                            }
                        },
                        modifier = Modifier.padding(5.dp)
                            .width(100.dp)
                    ) {
                        Text("选择目录")
                    }
                }
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
                            val backup = backups[it]
                            Card(
                                modifier = Modifier
                                    .padding(vertical = 10.dp)
                                    .padding(end = 20.dp)
                                    .fillMaxWidth()
                                    .height(50.dp)
                                    .clickable {
                                        showConfirmDialog("确定要保存当前数据到${it + 1}栏位吗") {
                                            saveData(it, backups)
                                        }
                                    },
                                elevation = 10.dp
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.Start,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    val text = if (backup == null) "可保存" else "保存于${backup.createDate}"
                                    Text(
                                        text, modifier = Modifier
                                            .padding(start = 5.dp)
                                            .weight(1f)
                                    )
                                    if (backup != null) {
                                        Button(
                                            onClick = {
                                                showConfirmDialog("确定要加载${it + 1}栏位的数据吗") {
                                                    loadData(
                                                        it,
                                                        backupPath.value
                                                    )
                                                }
                                            },
                                            modifier = Modifier
                                                .padding(5.dp)
                                                .width(100.dp)
                                        ) {
                                            Text("读取")
                                        }
                                    }

                                }

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

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun Confirm() {
    AlertDialog(
        onDismissRequest = {},
        title = { Text(confirmMessage.value) },
        confirmButton = {
            Button(
                onClick = {
                    showConfirm.value = false
                    confirmAction.value.invoke()
                }
            ) {
                Text("确定")
            }
        },
        dismissButton = {
            Button(
                onClick = { showConfirm.value = false }
            ) {
                Text("取消")
            }
        }
    )
}

fun showConfirmDialog(message: String, action: () -> Unit = {}) {
    showConfirm.value = true
    confirmMessage.value = message
    confirmAction.value = action
}

fun loadData(index: Int, path: String) {
    val loadPath = BASE_BACKUP_DIR + File.separator + index
    File(loadPath).copyRecursively(File(path), true)
}

fun saveData(index: Int, backups: Array<Backup?>) {
    val savePath = BASE_BACKUP_DIR + File.separator + index
    File(backupPath.value).copyRecursively(File(savePath), true)
    backups[index] = Backup(index, DATA_FORMAT.format(Date()), "")
    saveConfig(backups)
}

fun initData(backups: Array<Backup?>) {
    File(BASE_BACKUP_DIR).run {
        val config = objectMapper.readValue<Config>(File(BASE_BACKUP_DIR + File.separator + CONFIG_FILE))
        for (i in 0..config.backups.size) {
            val backup = config.backups[i] ?: break
            // 删除本地已经不存在的备份
            if (!File(BASE_BACKUP_DIR + File.separator + backup.index).exists()) {
                config.backups[config.backups.indexOf(backup)] = null
            }
            // 读取到对象里
            backups[i] = config.backups[i]
        }
        backupPath.value = config.path
        saveConfig(backups)
        //FIXME 前面若使用foreach&foreach index在这里的save不执行,未找到原因
//        saveConfig()
    }
}

fun saveConfig(backups: Array<Backup?>) {
    objectMapper.writeValue(
        File(BASE_BACKUP_DIR + File.separator + CONFIG_FILE),
        Config(backupPath.value, backups)
    )
}

fun openFileDialog(callback: (path: String) -> Unit) {
    JFileChooser().run {
        dialogTitle = "选择需要管理的文件目录"
        fileSelectionMode = JFileChooser.FILES_AND_DIRECTORIES
        showSaveDialog(null).also {
            if (it == JFileChooser.APPROVE_OPTION) {
                callback.invoke(this.selectedFile.path)
            }
        }
    }

}


fun main() {
    try {
        application {

            Window(onCloseRequest = ::exitApplication, title = "存档管理工具") {
                App()
            }
        }
    } catch (e: Exception) {
        File("error.log").writeText(e.stackTraceToString())
    }

}
