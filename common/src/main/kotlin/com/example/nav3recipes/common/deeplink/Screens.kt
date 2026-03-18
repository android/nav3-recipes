package com.example.nav3recipes.common.deeplink

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
public fun EntryScreen(text: String, content: @Composable () -> Unit = { }) {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(text, fontWeight = FontWeight.Bold, fontSize = FONT_SIZE_TITLE)
            content()
        }
    }
}

@Composable
public fun FriendsList(
    users: List<User>,
    onClick: ((user: User) -> Unit)? = null
) {
    // display list of matching targets
    if (users.isEmpty()) {
        Text("List is Empty", fontWeight = FontWeight.Bold)
    } else {
        LazyColumn {
            items(users.size) { idx ->
                val user = users[idx]
                val userString = "${user.firstName}(${user.age}), ${user.location}"
                if (onClick != null) {
                    TextClickable(userString) { onClick(user) }
                } else {
                    TextContent(userString)
                }
            }
        }
    }
}

/**
 * Displays a text input menu, may include several text fields
 */
@Composable
public fun MenuTextInput(
    menuLabels: List<String>,
    onValueChange: (String, String) -> Unit = { _, _ ->},
) {
    Column {
        menuLabels.forEach { label ->
            var inputText by remember { mutableStateOf("") }

            OutlinedTextField(
                value = inputText,
                onValueChange = {
                    inputText = it
                    onValueChange(label, it)
                },
                placeholder = { Text("enter integer") },
                label = { Text(label) },
            )
        }
    }

}

@Composable
public fun PaddedButton(
    text: String,
    onClick: () -> Unit,
) {
    Button(
        modifier = Modifier.padding(BUTTON_PADDING),
        onClick = onClick
    ) {
        Text(text)
    }
}

/**
 * Displays a drop down menu, may include multiple drop downs
 */
@Composable
public fun MenuDropDown(
    menuOptions: Map<String, List<String>>,
    onSelect: (label: String, selection: String) -> Unit = { _, _ ->},
) {
    Column(
        modifier = Modifier.animateContentSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        menuOptions.forEach { entry ->
            val key = entry.key
            ArgumentDropDownMenu(label = key, menuItemOptions = entry.value) { label, selection ->
                onSelect(key, selection)
            }
        }
    }
}

// Display list of selections for one drop down
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ArgumentDropDownMenu(
    label: String,
    menuItemOptions: List<String>,
    onSelect: (label: String, selection: String) -> Unit,
) {
    val initValue = menuItemOptions.firstOrNull() ?: ""
    var expanded by remember { mutableStateOf(false) }
    var currSelected by remember { mutableStateOf(initValue) }
    Box(
        modifier = Modifier
            .padding(16.dp)
    ) {
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = !expanded }
        ) {
            TextField(
                readOnly = true,
                value = currSelected,
                onValueChange = { },
                label = { Text(label) },
                trailingIcon = {
                    ExposedDropdownMenuDefaults.TrailingIcon(
                        expanded = expanded
                    )
                },
                modifier = Modifier.menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable, true)
            )
            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = {
                    expanded = false
                }
            ) {
                menuItemOptions.forEach { text ->
                    DropdownMenuItem(
                        text = { Text(text) },
                        onClick = {
                            expanded = false
                            currSelected = text
                            onSelect(label, text)
                        }
                    )
                }
            }
        }
    }
}


@Composable
fun TextContent(text: String) {
    Text(
        text = text,
        modifier = Modifier.width(300.dp),
        textAlign = TextAlign.Center,
        fontSize = FONT_SIZE_TEXT,
    )
}

@Composable
public fun TextClickable(
    text: String,
    onClick: () -> Unit,
) {
    Text(
        text = text,
        color = Color.Blue,
        style = TextStyle(textDecoration = TextDecoration.Underline),
        modifier = Modifier.width(300.dp).clickable(
            true,
            onClick = onClick
        ),
        textAlign = TextAlign.Center,
        fontSize = FONT_SIZE_TEXT,
    )
}

public val FONT_SIZE_TITLE: TextUnit = 20.sp
public val FONT_SIZE_TEXT: TextUnit = 15.sp
private val BUTTON_PADDING = PaddingValues(12.dp, 12.dp, 12.dp, 12.dp)