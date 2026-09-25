package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MarkEmailRead
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.ui.theme.WineBurgundyPrimary
import com.example.util.SoundFeedbackHelper

/**
 * Diálogo / Página de Recuperação de Senha por E-mail:
 * Passo 1: Informar o e-mail ou usuário cadastrado
 * Passo 2: Receber código de verificação no e-mail e redefinir nova senha
 */
@Composable
fun ForgotPasswordDialog(
    initialCnpj: String = "",
    onDismiss: () -> Unit,
    onRequestResetCode: (companyCnpj: String, identifier: String, callback: (Boolean, String, String?, String?) -> Unit) -> Unit,
    onConfirmReset: (companyCnpj: String, identifier: String, newPass: String, callback: (Boolean, String) -> Unit) -> Unit,
    onSuccessReset: (username: String, newPass: String) -> Unit
) {
    var step by remember { mutableStateOf(1) } // 1 = Digitar e-mail, 2 = Digitar código e nova senha, 3 = Sucesso
    var cnpjInput by remember { mutableStateOf(initialCnpj) }
    var emailOrUserInput by remember { mutableStateOf("") }
    var codeInput by remember { mutableStateOf("") }
    var sentEmailAddress by remember { mutableStateOf("") }
    var generatedCodeDemo by remember { mutableStateOf("") }

    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var isNewPasswordVisible by remember { mutableStateOf(false) }

    var isLoading by remember { mutableStateOf(false) }
    var statusMessage by remember { mutableStateOf<String?>(null) }
    var isError by remember { mutableStateOf(false) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
            modifier = Modifier
                .fillMaxWidth(0.94f)
                .padding(vertical = 24.dp)
                .testTag("forgot_password_dialog")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header with Back / Close
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (step == 2) {
                        IconButton(onClick = { step = 1; statusMessage = null }) {
                            Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Voltar")
                        }
                    } else {
                        Spacer(modifier = Modifier.size(40.dp))
                    }

                    Text(
                        text = "Recuperar Acesso",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )

                    IconButton(onClick = onDismiss) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Fechar")
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Icon Avatar
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (step == 3) Icons.Default.CheckCircle else if (step == 2) Icons.Default.Key else Icons.Default.Email,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(32.dp)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // STEP 1: Informar E-mail / Usuário
                if (step == 1) {
                    Text(
                        text = "Esqueceu sua senha?",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Digite seu e-mail cadastrado ou usuário de acesso. Enviaremos as instruções e o código de verificação para redefinir sua senha com segurança.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedTextField(
                        value = emailOrUserInput,
                        onValueChange = {
                            emailOrUserInput = it
                            statusMessage = null
                        },
                        label = { Text("E-mail ou Usuário Cadastrado *") },
                        placeholder = { Text("ex: carlos@maxbebidas.com.br ou carlos.silva") },
                        leadingIcon = {
                            Icon(imageVector = Icons.Default.Email, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("reset_email_or_user_input")
                    )

                    if (!statusMessage.isNullOrBlank()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = statusMessage!!,
                            color = if (isError) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                            style = MaterialTheme.typography.bodySmall,
                            textAlign = TextAlign.Center
                        )
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    Button(
                        onClick = {
                            if (emailOrUserInput.isBlank()) {
                                isError = true
                                statusMessage = "Por favor, digite seu e-mail cadastrado ou usuário."
                                SoundFeedbackHelper.playError()
                                return@Button
                            }
                            isLoading = true
                            isError = false
                            statusMessage = "Buscando dados e enviando código..."

                            onRequestResetCode(cnpjInput, emailOrUserInput) { success, msg, targetEmail, code ->
                                isLoading = false
                                if (success) {
                                    sentEmailAddress = targetEmail ?: emailOrUserInput
                                    generatedCodeDemo = code ?: ""
                                    statusMessage = msg
                                    step = 2
                                    SoundFeedbackHelper.playSuccess()
                                } else {
                                    isError = true
                                    statusMessage = msg
                                    SoundFeedbackHelper.playError()
                                }
                            }
                        },
                        enabled = !isLoading,
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("send_reset_code_button")
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(modifier = Modifier.size(20.dp), color = MaterialTheme.colorScheme.onPrimary, strokeWidth = 2.dp)
                        } else {
                            Icon(imageVector = Icons.Default.MarkEmailRead, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Enviar Informações para o E-mail", fontWeight = FontWeight.Bold)
                        }
                    }
                }

                // STEP 2: Inserir Código recebido por e-mail e Nova Senha
                if (step == 2) {
                    Text(
                        text = "Código de Redefinição",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Enviamos o código de 6 dígitos para o e-mail:",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = sentEmailAddress,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Simulated Email Inbox Notification Banner
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f),
                        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.MarkEmailRead,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "E-mail de Segurança Recebido",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Código gerado para redefinição: $generatedCodeDemo",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Bold
                            )
                            TextButton(
                                onClick = { codeInput = generatedCodeDemo },
                                modifier = Modifier.align(Alignment.End)
                            ) {
                                Text("Preencher Código ➔", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = codeInput,
                        onValueChange = {
                            codeInput = it.filter { ch -> ch.isDigit() }.take(6)
                            statusMessage = null
                        },
                        label = { Text("Código de 6 dígitos recebido *") },
                        placeholder = { Text("ex: 123456") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("reset_code_input")
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = newPassword,
                        onValueChange = {
                            newPassword = it
                            statusMessage = null
                        },
                        label = { Text("Nova Senha *") },
                        placeholder = { Text("Mínimo 4 caracteres") },
                        leadingIcon = {
                            Icon(imageVector = Icons.Default.Lock, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        },
                        trailingIcon = {
                            IconButton(onClick = { isNewPasswordVisible = !isNewPasswordVisible }) {
                                Icon(
                                    imageVector = if (isNewPasswordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                    contentDescription = null
                                )
                            }
                        },
                        visualTransformation = if (isNewPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("reset_new_password_input")
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = confirmPassword,
                        onValueChange = {
                            confirmPassword = it
                            statusMessage = null
                        },
                        label = { Text("Confirmar Nova Senha *") },
                        placeholder = { Text("Repita a nova senha") },
                        leadingIcon = {
                            Icon(imageVector = Icons.Default.Lock, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        },
                        visualTransformation = if (isNewPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("reset_confirm_password_input")
                    )

                    if (!statusMessage.isNullOrBlank()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = statusMessage!!,
                            color = if (isError) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                            style = MaterialTheme.typography.bodySmall,
                            textAlign = TextAlign.Center
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = {
                            if (codeInput.length < 4) {
                                isError = true
                                statusMessage = "Por favor, digite o código de verificação recebido no e-mail."
                                SoundFeedbackHelper.playError()
                                return@Button
                            }
                            if (generatedCodeDemo.isNotBlank() && codeInput != generatedCodeDemo) {
                                isError = true
                                statusMessage = "Código de verificação incorreto."
                                SoundFeedbackHelper.playError()
                                return@Button
                            }
                            if (newPassword.length < 4) {
                                isError = true
                                statusMessage = "A nova senha deve possuir pelo menos 4 caracteres."
                                SoundFeedbackHelper.playError()
                                return@Button
                            }
                            if (newPassword != confirmPassword) {
                                isError = true
                                statusMessage = "A confirmação de senha não confere."
                                SoundFeedbackHelper.playError()
                                return@Button
                            }

                            isLoading = true
                            isError = false
                            onConfirmReset(cnpjInput, emailOrUserInput, newPassword) { success, msg ->
                                isLoading = false
                                if (success) {
                                    SoundFeedbackHelper.playSuccess()
                                    step = 3
                                    statusMessage = msg
                                } else {
                                    isError = true
                                    statusMessage = msg
                                    SoundFeedbackHelper.playError()
                                }
                            }
                        },
                        enabled = !isLoading,
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("btn_confirm_password_reset")
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(modifier = Modifier.size(20.dp), color = MaterialTheme.colorScheme.onPrimary, strokeWidth = 2.dp)
                        } else {
                            Text("Salvar Nova Senha", fontWeight = FontWeight.Bold)
                        }
                    }
                }

                // STEP 3: Concluído com Sucesso
                if (step == 3) {
                    Text(
                        text = "Senha Alterada com Sucesso!",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF2E7D32),
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Sua nova senha foi registrada no banco de dados. Agora você já pode acessar o sistema com suas novas credenciais.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(20.dp))

                    Button(
                        onClick = {
                            val userToFill = if (emailOrUserInput.contains("@")) emailOrUserInput.substringBefore("@") else emailOrUserInput
                            onSuccessReset(userToFill, newPassword)
                            onDismiss()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("btn_return_to_login_with_new_pass")
                    ) {
                        Text("Ir para Login e Entrar", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
