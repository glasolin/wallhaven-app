package otus.gpb.homework.wallhaven.ui.assets

import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.ImeAction

@OptIn(ExperimentalMaterial3Api::class, ExperimentalComposeUiApi::class)
@Composable
fun SuggestionTextBox(
    value: String,
    items: MutableState<List<String>>,
    onInput: (selectedItem: String) -> Unit,
    onChange: (inputText: String) -> Unit,
    modifier:Modifier = Modifier,
) {
    var expanded by remember { mutableStateOf(false) }
    var input by remember { mutableStateOf(value) }

    val focusRequester = FocusRequester()
    val keyboardController = LocalSoftwareKeyboardController.current

    Box(modifier) {
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = {
                expanded = !expanded
            },
            modifier= Modifier
        ) {
            TextField(
                singleLine = true,
                value = input,
                onValueChange = {
                    input=it
                    expanded = true
                    onChange(it)
                },
                readOnly = false,
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(
                    onDone = {
                        onInput(input)
                        expanded = false
                        focusRequester.restoreFocusedChild()
                        input=""
                        keyboardController?.hide()
                    }
                ),
                modifier = Modifier
                    .menuAnchor()
                    .focusRequester(focusRequester)
            )
            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                items.value.forEach { item ->
                    DropdownMenuItem(
                        text = { Text(text = item) },
                        onClick = {
                            input=item
                            expanded = false
                            focusRequester.freeFocus()
                            onInput(item)
                        }
                    )
                }
            }
        }
    }
}
