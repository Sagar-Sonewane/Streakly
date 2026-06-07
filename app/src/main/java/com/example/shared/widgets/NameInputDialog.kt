package com.example.shared.widgets

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.theme.AppColors
import com.example.core.theme.AppTextStyles

@Composable
fun NameInputDialog(
    initialName: String,
    accentColor: Color,
    getLabel: (String, String, String) -> String,
    onDismiss: (() -> Unit)?, // null if not dismissible (e.g. first startup)
    onConfirm: (String) -> Unit
) {
    var nameInput by remember { mutableStateOf(initialName) }

    AlertDialog(
        onDismissRequest = { if (onDismiss != null) onDismiss() },
        confirmButton = {
            Button(
                onClick = {
                    if (nameInput.isNotBlank()) {
                        com.example.core.utils.SoundService.playTap()
                        com.example.core.utils.HapticService.confirm()
                        onConfirm(nameInput.trim())
                    }
                },
                enabled = nameInput.isNotBlank(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = accentColor,
                    contentColor = Color.Black,
                    disabledContainerColor = AppColors.border,
                    disabledContentColor = AppColors.textHint
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = getLabel("Save", "सहेजें", "जतन करा"),
                    fontWeight = FontWeight.Bold
                )
            }
        },
        dismissButton = if (onDismiss != null) {
            {
                TextButton(onClick = {
                    com.example.core.utils.SoundService.playTap()
                    com.example.core.utils.HapticService.selectionClick()
                    onDismiss()
                }) {
                    Text(
                        text = getLabel("Cancel", "रद्द करें", "रद्द करा"),
                        color = AppColors.textSecondary
                    )
                }
            }
        } else null,
        title = {
            Text(
                text = getLabel("Enter Your Name", "अपना नाम दर्ज करें", "तुमचे नाव प्रविष्ट करा"),
                style = AppTextStyles.titleLarge.copy(
                    fontWeight = FontWeight.Black,
                    color = AppColors.textPrimary
                )
            )
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = getLabel(
                        "Please enter your name to personalize your greetings.",
                        "शुभकामनाओं को निजीकृत करने के लिए कृपया अपना नाम दर्ज करें।",
                        "आपल्या शुभेच्छा वैयक्तिकृत करण्यासाठी कृपया आपले नाव प्रविष्ट करा."
                    ),
                    style = AppTextStyles.bodyMedium.copy(color = AppColors.textSecondary),
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                OutlinedTextField(
                    value = nameInput,
                    onValueChange = {
                        if (it.length <= 20) {
                            nameInput = it
                        }
                    },
                    placeholder = {
                        Text(
                            text = getLabel("Your Name", "आपका नाम", "तुमचे नाव"),
                            color = AppColors.textHint
                        )
                    },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = accentColor,
                        unfocusedBorderColor = AppColors.border,
                        focusedLabelColor = accentColor,
                        unfocusedLabelColor = AppColors.textSecondary,
                        cursorColor = accentColor,
                        focusedTextColor = AppColors.textPrimary,
                        unfocusedTextColor = AppColors.textPrimary
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth(),
                    supportingText = {
                        Box(
                            modifier = Modifier.fillMaxWidth(),
                            contentAlignment = Alignment.CenterEnd
                        ) {
                            val count = nameInput.length
                            val counterColor = if (count >= 18) accentColor else AppColors.textHint
                            Text(
                                text = "$count / 20",
                                style = AppTextStyles.caption.copy(fontSize = 11.sp),
                                color = counterColor
                            )
                        }
                    }
                )
            }
        },
        containerColor = AppColors.bgSecondary,
        shape = RoundedCornerShape(24.dp),
        modifier = Modifier.border(1.dp, AppColors.border, RoundedCornerShape(24.dp))
    )
}
