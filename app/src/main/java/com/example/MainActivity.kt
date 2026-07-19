package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.Backspace
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.ui.theme.MyApplicationTheme
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    setContent {
      MyApplicationTheme {
        GuardianApp()
      }
    }
  }
}

enum class GuardianScreen {
  SPLASH,
  ONBOARDING,
  LOGIN,
  PERMISSIONS,
  HOME,
  PROFILE,
  ALARM_OVERLAY
}

@Composable
fun GuardianApp() {
  var currentScreen by remember { mutableStateOf(GuardianScreen.SPLASH) }
  var previousScreen by remember { mutableStateOf<GuardianScreen?>(null) }
  
  // App Shared States
  var emailInput by remember { mutableStateOf("operative@guardian.net") }
  var passwordInput by remember { mutableStateOf("••••••••") }
  var biometricEnabled by remember { mutableStateOf(true) }
  var twoFactorEnabled by remember { mutableStateOf(false) }
  var alertConfigEnabled by remember { mutableStateOf(true) }
  
  Box(
    modifier = Modifier
      .fillMaxSize()
      .background(Color(0xFF050507)) // Void Black
  ) {
    AnimatedContent(
      targetState = currentScreen,
      transitionSpec = {
        fadeIn(animationSpec = tween(400)) togetherWith fadeOut(animationSpec = tween(400))
      },
      label = "ScreenTransition"
    ) { screen ->
      when (screen) {
        GuardianScreen.SPLASH -> {
          SplashScreen(onTimeout = {
            currentScreen = GuardianScreen.ONBOARDING
          })
        }
        GuardianScreen.ONBOARDING -> {
          OnboardingScreen(
            onContinue = { currentScreen = GuardianScreen.LOGIN },
            onSkip = { currentScreen = GuardianScreen.LOGIN }
          )
        }
        GuardianScreen.LOGIN -> {
          LoginScreen(
            email = emailInput,
            password = passwordInput,
            onEmailChange = { emailInput = it },
            onPasswordChange = { passwordInput = it },
            onLoginSuccess = { currentScreen = GuardianScreen.PERMISSIONS }
          )
        }
        GuardianScreen.PERMISSIONS -> {
          PermissionsScreen(
            onPermissionGranted = { currentScreen = GuardianScreen.HOME },
            onSkip = { currentScreen = GuardianScreen.HOME }
          )
        }
        GuardianScreen.HOME -> {
          HomeScreen(
            onLockTriggered = {
              previousScreen = GuardianScreen.HOME
              currentScreen = GuardianScreen.ALARM_OVERLAY
            },
            onNavigateToProfile = { currentScreen = GuardianScreen.PROFILE }
          )
        }
        GuardianScreen.PROFILE -> {
          ProfileScreen(
            biometricEnabled = biometricEnabled,
            twoFactorEnabled = twoFactorEnabled,
            alertConfigEnabled = alertConfigEnabled,
            onBiometricToggle = { biometricEnabled = it },
            onTwoFactorToggle = { twoFactorEnabled = it },
            onAlertConfigToggle = { alertConfigEnabled = it },
            onLockTriggered = {
              previousScreen = GuardianScreen.PROFILE
              currentScreen = GuardianScreen.ALARM_OVERLAY
            },
            onLogout = { currentScreen = GuardianScreen.LOGIN },
            onNavigateToHome = { currentScreen = GuardianScreen.HOME }
          )
        }
        GuardianScreen.ALARM_OVERLAY -> {
          AlarmOverlayScreen(
            onUnlock = {
              currentScreen = previousScreen ?: GuardianScreen.HOME
            },
            onCancel = {
              currentScreen = previousScreen ?: GuardianScreen.HOME
            }
          )
        }
      }
    }

    // Overlay threat simulator to let the user trigger the Device Lockdown demo from Home/Profile screens
    if (currentScreen == GuardianScreen.HOME || currentScreen == GuardianScreen.PROFILE) {
      Box(
        modifier = Modifier
          .align(Alignment.TopCenter)
          .padding(top = 80.dp)
          .clip(CircleShape)
          .background(Color(0xFFFF5167).copy(alpha = 0.15f))
          .border(1.dp, Color(0xFFFF5167).copy(alpha = 0.4f), CircleShape)
          .clickable {
            previousScreen = currentScreen
            currentScreen = GuardianScreen.ALARM_OVERLAY
          }
          .padding(horizontal = 16.dp, vertical = 6.dp)
      ) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.Center,
          modifier = Modifier.wrapContentSize()
        ) {
          Box(
            modifier = Modifier
              .size(6.dp)
              .clip(CircleShape)
              .background(Color(0xFFFF5167))
          )
          Spacer(modifier = Modifier.width(6.dp))
          Text(
            text = "SIMULATE BREACH",
            color = Color(0xFFFF5167),
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.5.sp
          )
        }
      }
    }
  }
}

@Composable
fun SplashScreen(onTimeout: () -> Unit) {
  LaunchedEffect(Unit) {
    delay(2500)
    onTimeout()
  }

  Box(
    modifier = Modifier
      .fillMaxSize()
      .background(Color(0xFF050507)),
    contentAlignment = Alignment.Center
  ) {
    // Ambient back-glow
    Box(
      modifier = Modifier
        .size(300.dp)
        .scale(1.5f)
        .background(
          brush = Brush.radialGradient(
            colors = listOf(Color(0xFF49FCD9).copy(alpha = 0.08f), Color.Transparent),
            radius = 300f
          )
        )
    )

    Column(
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.Center
    ) {
      // Hexagon base vector shield
      Box(
        modifier = Modifier
          .size(160.dp)
          .padding(bottom = 20.dp),
        contentAlignment = Alignment.Center
      ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
          val width = size.width
          val height = size.height
          val center = Offset(width / 2, height / 2)
          val radius = width * 0.4f

          // Outer glowing radar circle
          drawCircle(
            color = Color(0xFF49FCD9).copy(alpha = 0.2f),
            radius = radius * 1.15f,
            style = Stroke(width = 1.dp.toPx())
          )

          // Glowing point light
          drawCircle(
            color = Color(0xFFB6C4FF).copy(alpha = 0.15f),
            radius = radius * 0.9f
          )
        }

        Icon(
          imageVector = Icons.Default.Shield,
          contentDescription = "Guardian Shield",
          modifier = Modifier
            .size(70.dp)
            .scale(1.1f),
          tint = Color(0xFF49FCD9)
        )
      }

      Text(
        text = "GUARDIAN",
        fontSize = 32.sp,
        fontWeight = FontWeight.ExtraBold,
        letterSpacing = 8.sp,
        textAlign = TextAlign.Center,
        color = Color(0xFFB6C4FF)
      )

      Spacer(modifier = Modifier.height(8.dp))

      Text(
        text = "ANTI-THEFT INTELLIGENCE",
        fontSize = 10.sp,
        fontWeight = FontWeight.Bold,
        letterSpacing = 4.sp,
        color = Color(0xFFC3C5D8).copy(alpha = 0.6f)
      )

      Spacer(modifier = Modifier.height(48.dp))

      // Custom Shimmer Loading lines
      Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp),
        modifier = Modifier.width(120.dp)
      ) {
        val infiniteTransition = rememberInfiniteTransition(label = "Loading")
        val progress by infiniteTransition.animateFloat(
          initialValue = 0f,
          targetValue = 1f,
          animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
          ),
          label = "progress"
        )

        Box(
          modifier = Modifier
            .fillMaxWidth()
            .height(2.dp)
            .background(Color(0xFFE5E1E5).copy(alpha = 0.1f))
        ) {
          Box(
            modifier = Modifier
              .fillMaxHeight()
              .fillMaxWidth(0.4f)
              .align(Alignment.CenterStart)
              .offset(x = (120 * progress).dp - 24.dp)
              .background(Color(0xFF49FCD9))
          )
        }
        Box(
          modifier = Modifier
            .fillMaxWidth(0.8f)
            .height(2.dp)
            .background(Color(0xFFE5E1E5).copy(alpha = 0.1f))
        ) {
          Box(
            modifier = Modifier
              .fillMaxHeight()
              .fillMaxWidth(0.3f)
              .align(Alignment.CenterStart)
              .offset(x = (96 * progress).dp - 20.dp)
              .background(Color(0xFFB6C4FF))
          )
        }
      }
    }

    Text(
      text = "v2.4.0",
      color = Color(0xFFC3C5D8).copy(alpha = 0.4f),
      fontSize = 12.sp,
      fontWeight = FontWeight.Medium,
      modifier = Modifier
        .align(Alignment.BottomCenter)
        .padding(bottom = 32.dp)
    )
  }
}

@Composable
fun OnboardingScreen(onContinue: () -> Unit, onSkip: () -> Unit) {
  Box(
    modifier = Modifier
      .fillMaxSize()
      .background(Color(0xFF050507))
      .safeDrawingPadding()
  ) {
    // Ambient radial blur background
    Box(
      modifier = Modifier
        .size(400.dp)
        .align(Alignment.Center)
        .scale(1.2f)
        .background(
          brush = Brush.radialGradient(
            colors = listOf(Color(0xFFB6C4FF).copy(alpha = 0.05f), Color.Transparent),
            radius = 400f
          )
        )
    )

    // Top action bar
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 24.dp, vertical = 16.dp),
      horizontalArrangement = Arrangement.End
    ) {
      Button(
        onClick = onSkip,
        colors = ButtonDefaults.buttonColors(
          containerColor = Color(0xFF131316).copy(alpha = 0.5f),
          contentColor = Color(0xFFC3C5D8)
        ),
        border = BorderStroke(0.5.dp, Color(0xFF434655).copy(alpha = 0.6f)),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp),
        shape = CircleShape
      ) {
        Text("Skip", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
      }
    }

    Column(
      modifier = Modifier
        .fillMaxSize()
        .padding(horizontal = 24.dp, vertical = 80.dp),
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.SpaceBetween
    ) {
      // Rotating orbiting HUD illustration
      Box(
        modifier = Modifier
          .size(240.dp)
          .weight(1f),
        contentAlignment = Alignment.Center
      ) {
        val infiniteTransition = rememberInfiniteTransition(label = "RadarOrbit")
        val angle by infiniteTransition.animateFloat(
          initialValue = 0f,
          targetValue = 360f,
          animationSpec = infiniteRepeatable(
            animation = tween(12000, easing = LinearEasing)
          ),
          label = "angle"
        )

        // Center split shield hexagon
        Box(
          modifier = Modifier
            .size(140.dp),
          contentAlignment = Alignment.Center
        ) {
          Canvas(modifier = Modifier.fillMaxSize()) {
            val sizePx = size.width
            val cx = sizePx / 2
            val cy = sizePx / 2
            // Hexagon polygon outline
            val path = androidx.compose.ui.graphics.Path().apply {
              moveTo(cx, 10f)
              lineTo(sizePx - 10f, sizePx * 0.25f)
              lineTo(sizePx - 10f, sizePx * 0.75f)
              lineTo(cx, sizePx - 10f)
              lineTo(10f, sizePx * 0.75f)
              lineTo(10f, sizePx * 0.25f)
              close()
            }
            drawPath(
              path = path,
              color = Color(0xFF434655),
              style = Stroke(width = 1.dp.toPx())
            )
          }

          Icon(
            imageVector = Icons.Default.AdminPanelSettings,
            contentDescription = "Military Grade Admin icon",
            tint = Color(0xFFB6C4FF),
            modifier = Modifier.size(48.dp)
          )
        }

        // Orbital elements rotating slowly
        Box(
          modifier = Modifier
            .size(220.dp)
            .rotate(angle)
        ) {
          Box(
            modifier = Modifier
              .size(28.dp)
              .clip(RoundedCornerShape(8.dp))
              .background(Color(0xFF131316))
              .border(0.5.dp, Color(0xFF49FCD9).copy(alpha = 0.5f), RoundedCornerShape(8.dp))
              .align(Alignment.TopCenter)
              .padding(4.dp),
            contentAlignment = Alignment.Center
          ) {
            Icon(
              imageVector = Icons.Default.Security,
              contentDescription = "Sec icon",
              tint = Color(0xFF49FCD9),
              modifier = Modifier.size(14.dp)
            )
          }

          Box(
            modifier = Modifier
              .size(28.dp)
              .clip(RoundedCornerShape(8.dp))
              .background(Color(0xFF131316))
              .border(0.5.dp, Color(0xFFB6C4FF).copy(alpha = 0.5f), RoundedCornerShape(8.dp))
              .align(Alignment.BottomStart)
              .padding(4.dp),
            contentAlignment = Alignment.Center
          ) {
            Icon(
              imageVector = Icons.Default.Lock,
              contentDescription = "Encrypted icon",
              tint = Color(0xFFB6C4FF),
              modifier = Modifier.size(14.dp)
            )
          }

          Box(
            modifier = Modifier
              .size(28.dp)
              .clip(RoundedCornerShape(8.dp))
              .background(Color(0xFF131316))
              .border(0.5.dp, Color(0xFFFFB3B5).copy(alpha = 0.5f), RoundedCornerShape(8.dp))
              .align(Alignment.BottomEnd)
              .padding(4.dp),
            contentAlignment = Alignment.Center
          ) {
            Icon(
              imageVector = Icons.Default.Policy,
              contentDescription = "Policy icon",
              tint = Color(0xFFFFB3B5),
              modifier = Modifier.size(14.dp)
            )
          }
        }
      }

      // Title & Description
      Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(bottom = 24.dp)
      ) {
        Text(
          text = "01 / 03",
          color = Color(0xFF49FCD9),
          fontSize = 12.sp,
          fontWeight = FontWeight.Bold,
          letterSpacing = 2.sp
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
          text = "Defense That Never Sleeps",
          fontSize = 26.sp,
          fontWeight = FontWeight.ExtraBold,
          textAlign = TextAlign.Center,
          color = Color(0xFFE5E1E5)
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
          text = "Premium military-grade protection for your mobile lifestyle.",
          fontSize = 14.sp,
          fontWeight = FontWeight.Medium,
          textAlign = TextAlign.Center,
          color = Color(0xFFC3C5D8).copy(alpha = 0.7f),
          modifier = Modifier.widthIn(max = 280.dp)
        )
      }

      // Pagination & Continue footer
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        // Page Dots
        Row(
          horizontalArrangement = Arrangement.spacedBy(6.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          Box(
            modifier = Modifier
              .size(width = 24.dp, height = 5.dp)
              .clip(CircleShape)
              .background(Color(0xFFB6C4FF))
          )
          Box(
            modifier = Modifier
              .size(5.dp)
              .clip(CircleShape)
              .background(Color(0xFF434655))
          )
          Box(
            modifier = Modifier
              .size(5.dp)
              .clip(CircleShape)
              .background(Color(0xFF434655))
          )
        }

        // CTA Continue button
        Button(
          onClick = onContinue,
          colors = ButtonDefaults.buttonColors(
            containerColor = Color(0xFFB6C4FF).copy(alpha = 0.08f),
            contentColor = Color(0xFFB6C4FF)
          ),
          border = BorderStroke(1.dp, Color(0xFFB6C4FF).copy(alpha = 0.3f)),
          shape = CircleShape,
          contentPadding = PaddingValues(horizontal = 24.dp, vertical = 14.dp)
        ) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            Text("Continue", fontSize = 14.sp, fontWeight = FontWeight.Bold)
            Icon(
              imageVector = Icons.AutoMirrored.Filled.ArrowForward,
              contentDescription = "Arrow right icon",
              modifier = Modifier.size(16.dp)
            )
          }
        }
      }
    }
  }
}

@Composable
fun LoginScreen(
  email: String,
  password: String,
  onEmailChange: (String) -> Unit,
  onPasswordChange: (String) -> Unit,
  onLoginSuccess: () -> Unit
) {
  var pwdVisible by remember { mutableStateOf(false) }

  Box(
    modifier = Modifier
      .fillMaxSize()
      .background(Color(0xFF050507))
      .safeDrawingPadding()
  ) {
    // Top pulsing background radial glow
    Box(
      modifier = Modifier
        .size(450.dp)
        .align(Alignment.TopCenter)
        .offset(y = (-150).dp)
        .background(
          brush = Brush.radialGradient(
            colors = listOf(Color(0xFF668AFF).copy(alpha = 0.06f), Color.Transparent),
            radius = 450f
          )
        )
    )

    Column(
      modifier = Modifier
        .fillMaxSize()
        .padding(horizontal = 24.dp)
        .verticalScroll(rememberScrollState()),
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.Center
    ) {
      Spacer(modifier = Modifier.height(40.dp))

      // Logo Header
      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.padding(bottom = 12.dp)
      ) {
        Icon(
          imageVector = Icons.Default.Shield,
          contentDescription = "Shield logo",
          tint = Color(0xFF49FCD9),
          modifier = Modifier.size(24.dp)
        )
        Text(
          text = "GUARDIAN",
          fontSize = 18.sp,
          fontWeight = FontWeight.Black,
          letterSpacing = 2.sp,
          color = Color(0xFF49FCD9)
        )
      }

      Text(
        text = "Welcome Back.",
        fontSize = 32.sp,
        fontWeight = FontWeight.ExtraBold,
        color = Color(0xFFE5E1E5)
      )

      Spacer(modifier = Modifier.height(6.dp))

      Text(
        text = "Authenticate to access the tactical grid.",
        fontSize = 14.sp,
        color = Color(0xFFC3C5D8).copy(alpha = 0.7f),
        textAlign = TextAlign.Center
      )

      Spacer(modifier = Modifier.height(40.dp))

      // Input Form Fields (M3 Filling Glass style)
      Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
      ) {
        // Email Field
        OutlinedTextField(
          value = email,
          onValueChange = onEmailChange,
          leadingIcon = {
            Icon(
              imageVector = Icons.Default.Mail,
              contentDescription = "Mail icon",
              tint = Color(0xFF434655)
            )
          },
          placeholder = {
            Text(
              "Email Address",
              color = Color(0xFF8D90A1)
            )
          },
          textStyle = LocalTextStyle.current.copy(color = Color(0xFFE5E1E5)),
          singleLine = true,
          shape = RoundedCornerShape(14.dp),
          colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = Color(0xFF0C0E1A).copy(alpha = 0.4f),
            unfocusedContainerColor = Color(0xFF0C0E1A).copy(alpha = 0.4f),
            focusedBorderColor = Color(0xFF668AFF),
            unfocusedBorderColor = Color(0xFF434655).copy(alpha = 0.6f)
          ),
          modifier = Modifier.fillMaxWidth()
        )

        // Password Field
        OutlinedTextField(
          value = password,
          onValueChange = onPasswordChange,
          leadingIcon = {
            Icon(
              imageVector = Icons.Default.Lock,
              contentDescription = "Lock icon",
              tint = Color(0xFF434655)
            )
          },
          trailingIcon = {
            IconButton(onClick = { pwdVisible = !pwdVisible }) {
              Icon(
                imageVector = if (pwdVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                contentDescription = "Toggle password",
                tint = Color(0xFF8D90A1)
              )
            }
          },
          placeholder = {
            Text(
              "Password",
              color = Color(0xFF8D90A1)
            )
          },
          textStyle = LocalTextStyle.current.copy(color = Color(0xFFE5E1E5)),
          visualTransformation = if (pwdVisible) androidx.compose.ui.text.input.VisualTransformation.None else androidx.compose.ui.text.input.PasswordVisualTransformation(),
          singleLine = true,
          shape = RoundedCornerShape(14.dp),
          colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = Color(0xFF0C0E1A).copy(alpha = 0.4f),
            unfocusedContainerColor = Color(0xFF0C0E1A).copy(alpha = 0.4f),
            focusedBorderColor = Color(0xFF668AFF),
            unfocusedBorderColor = Color(0xFF434655).copy(alpha = 0.6f)
          ),
          modifier = Modifier.fillMaxWidth()
        )
      }

      Spacer(modifier = Modifier.height(10.dp))

      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.End
      ) {
        Text(
          text = "Forgot password?",
          color = Color(0xFFB6C4FF),
          fontSize = 12.sp,
          fontWeight = FontWeight.SemiBold,
          modifier = Modifier.clickable { }
        )
      }

      Spacer(modifier = Modifier.height(24.dp))

      // Gradient primary button
      Button(
        onClick = onLoginSuccess,
        colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
        contentPadding = PaddingValues(),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier
          .fillMaxWidth()
          .height(52.dp)
      ) {
        Box(
          modifier = Modifier
            .fillMaxSize()
            .background(
              brush = Brush.linearGradient(
                colors = listOf(Color(0xFF3D6FFF), Color(0xFF4DFFDB))
              )
            ),
          contentAlignment = Alignment.Center
        ) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            Text(
              "SIGN IN",
              color = Color(0xFF00164F),
              fontSize = 12.sp,
              fontWeight = FontWeight.Bold,
              letterSpacing = 1.sp
            )
            Icon(
              imageVector = Icons.AutoMirrored.Filled.ArrowForward,
              contentDescription = "Login sign icon",
              tint = Color(0xFF00164F),
              modifier = Modifier.size(16.dp)
            )
          }
        }
      }

      Spacer(modifier = Modifier.height(24.dp))

      // OR Divider
      Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
      ) {
        Box(
          modifier = Modifier
            .weight(1f)
            .height(1.dp)
            .background(Color(0xFF434655).copy(alpha = 0.4f))
        )
        Text(
          "OR",
          color = Color(0xFFC3C5D8).copy(alpha = 0.6f),
          fontSize = 10.sp,
          fontWeight = FontWeight.Bold,
          modifier = Modifier.padding(horizontal = 16.dp)
        )
        Box(
          modifier = Modifier
            .weight(1f)
            .height(1.dp)
            .background(Color(0xFF434655).copy(alpha = 0.4f))
        )
      }

      Spacer(modifier = Modifier.height(24.dp))

      // Google sign-in button
      Button(
        onClick = onLoginSuccess,
        colors = ButtonDefaults.buttonColors(
          containerColor = Color(0xFF131316),
          contentColor = Color(0xFFE5E1E5)
        ),
        border = BorderStroke(0.5.dp, Color(0xFF434655)),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier
          .fillMaxWidth()
          .height(48.dp)
      ) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
          // Custom Google G logo icon
          Icon(
            imageVector = Icons.Default.Android,
            contentDescription = "G icon",
            tint = Color(0xFFB6C4FF),
            modifier = Modifier.size(18.dp)
          )
          Text(
            "Sign in with Google",
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium
          )
        }
      }

      Spacer(modifier = Modifier.height(32.dp))

      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
      ) {
        Text(
          "New here? ",
          color = Color(0xFFC3C5D8).copy(alpha = 0.7f),
          fontSize = 14.sp
        )
        Text(
          "Create account",
          color = Color(0xFF49FCD9),
          fontSize = 14.sp,
          fontWeight = FontWeight.Bold,
          modifier = Modifier.clickable { }
        )
      }

      Spacer(modifier = Modifier.height(40.dp))
    }
  }
}

@Composable
fun PermissionsScreen(onPermissionGranted: () -> Unit, onSkip: () -> Unit) {
  var isExpanded by remember { mutableStateOf(false) }

  Box(
    modifier = Modifier
      .fillMaxSize()
      .background(Color(0xFF050507))
      .safeDrawingPadding()
  ) {
    // Top Background glow
    Box(
      modifier = Modifier
        .size(400.dp)
        .align(Alignment.TopCenter)
        .background(
          brush = Brush.radialGradient(
            colors = listOf(Color(0xFF3D6FFF).copy(alpha = 0.08f), Color.Transparent),
            radius = 400f
          )
        )
    )

    Column(
      modifier = Modifier
        .fillMaxSize()
        .padding(horizontal = 24.dp, vertical = 24.dp),
      horizontalAlignment = Alignment.CenterHorizontally
    ) {
      // Progress header
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        IconButton(
          onClick = onSkip,
          modifier = Modifier
            .size(40.dp)
            .clip(CircleShape)
            .background(Color(0xFF131316).copy(alpha = 0.6f))
            .border(0.5.dp, Color(0xFF434655), CircleShape)
        ) {
          Icon(
            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
            contentDescription = "Back",
            tint = Color(0xFFC3C5D8),
            modifier = Modifier.size(18.dp)
          )
        }

        Text(
          text = "Step 2 of 6",
          color = Color(0xFFE5E1E5),
          fontSize = 16.sp,
          fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.width(40.dp)) // Equalizer spacer
      }

      Spacer(modifier = Modifier.height(32.dp))

      // Timeline Rail progress visualizer
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        // Dot 1 (Complete)
        Box(
          modifier = Modifier
            .size(24.dp)
            .clip(CircleShape)
            .background(Color(0xFF49FCD9))
            .border(2.dp, Color(0xFF49FCD9), CircleShape),
          contentAlignment = Alignment.Center
        ) {
          Icon(
            imageVector = Icons.Default.Check,
            contentDescription = "complete",
            tint = Color(0xFF00201A),
            modifier = Modifier.size(12.dp)
          )
        }

        // Connector line 1
        Box(
          modifier = Modifier
            .weight(1f)
            .height(1.dp)
            .background(Color(0xFF49FCD9))
        )

        // Dot 2 (Active)
        Box(
          modifier = Modifier
            .size(24.dp)
            .clip(CircleShape)
            .background(Color(0xFF050507))
            .border(1.5.dp, Color(0xFF3D6FFF), CircleShape),
          contentAlignment = Alignment.Center
        ) {
          Box(
            modifier = Modifier
              .size(8.dp)
              .clip(CircleShape)
              .background(Color(0xFF49FCD9))
          )
        }

        // Connector line 2
        Box(
          modifier = Modifier
            .weight(1.5f)
            .height(1.dp)
            .background(Color(0xFF434655))
        )

        // Dot 3-6 (Pending)
        repeat(3) {
          Box(
            modifier = Modifier
              .size(24.dp)
              .clip(CircleShape)
              .background(Color(0xFF131316))
              .border(0.5.dp, Color(0xFF434655), CircleShape),
            contentAlignment = Alignment.Center
          ) {
            Box(
              modifier = Modifier
                .size(6.dp)
                .clip(CircleShape)
                .background(Color(0xFF434655))
            )
          }
          if (it < 2) {
            Box(
              modifier = Modifier
                .weight(1f)
                .height(1.dp)
                .background(Color(0xFF434655))
            )
          }
        }
      }

      Spacer(modifier = Modifier.height(48.dp))

      // Main Icon visualizer
      Box(
        modifier = Modifier
          .size(96.dp)
          .clip(RoundedCornerShape(24.dp))
          .background(
            brush = Brush.linearGradient(
              colors = listOf(Color(0xFF3D6FFF), Color(0xFF6B4FFF))
            )
          )
          .padding(1.dp),
        contentAlignment = Alignment.Center
      ) {
        Box(
          modifier = Modifier
            .fillMaxSize()
            .clip(RoundedCornerShape(23.dp))
            .background(Color(0xFF050507).copy(alpha = 0.8f)),
          contentAlignment = Alignment.Center
        ) {
          Icon(
            imageVector = Icons.Default.NotificationsActive,
            contentDescription = "Alert notifications logo",
            tint = Color(0xFFE5E1E5),
            modifier = Modifier.size(40.dp)
          )
        }
      }

      Spacer(modifier = Modifier.height(24.dp))

      Text(
        text = "PERMISSIONS REQUIRED",
        fontSize = 24.sp,
        fontWeight = FontWeight.ExtraBold,
        letterSpacing = 1.sp,
        textAlign = TextAlign.Center,
        color = Color(0xFFE5E1E5)
      )

      Spacer(modifier = Modifier.height(12.dp))

      Text(
        text = "Guardian relies on critical alerts to keep you informed of tactical updates and security breaches.",
        fontSize = 14.sp,
        fontWeight = FontWeight.Medium,
        textAlign = TextAlign.Center,
        color = Color(0xFFC3C5D8).copy(alpha = 0.7f),
        modifier = Modifier.widthIn(max = 280.dp)
      )

      Spacer(modifier = Modifier.height(40.dp))

      // System state status card
      Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF131316).copy(alpha = 0.6f)),
        border = BorderStroke(0.5.dp, Color(0xFF434655).copy(alpha = 0.4f)),
        modifier = Modifier.fillMaxWidth()
      ) {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text(
            "SYSTEM STATE",
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.5.sp,
            color = Color(0xFFC3C5D8)
          )

          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            Box(
              modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(Color(0xFFFF4560))
            )
            Text(
              "PENDING",
              fontSize = 12.sp,
              fontWeight = FontWeight.Bold,
              letterSpacing = 1.sp,
              color = Color(0xFFE5E1E5)
            )
          }
        }
      }

      Spacer(modifier = Modifier.height(12.dp))

      // Expandable FAQ list item
      Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF131316).copy(alpha = 0.6f)),
        border = BorderStroke(0.5.dp, Color(0xFF434655).copy(alpha = 0.4f)),
        modifier = Modifier
          .fillMaxWidth()
          .clickable { isExpanded = !isExpanded }
      ) {
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
        ) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Text(
              "WHY DO WE NEED THIS?",
              fontSize = 12.sp,
              fontWeight = FontWeight.Bold,
              letterSpacing = 1.sp,
              color = Color(0xFFC3C5D8)
            )

            Icon(
              imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
              contentDescription = "Expand icon",
              tint = Color(0xFF8D90A1),
              modifier = Modifier.size(20.dp)
            )
          }

          AnimatedVisibility(
            visible = isExpanded,
            enter = expandVertically() + fadeIn(),
            exit = shrinkVertically() + fadeOut()
          ) {
            Column {
              Spacer(modifier = Modifier.height(12.dp))
              Text(
                text = "Push notification authorization allows Guardian's hardware alarm systems, remote lockers, and geofencing systems to send background tactical alerts instantly, bypassing standard system dormancy states during critical lockouts.",
                fontSize = 13.sp,
                color = Color(0xFFC3C5D8).copy(alpha = 0.8f),
                lineHeight = 18.sp
              )
            }
          }
        }
      }

      Spacer(modifier = Modifier.weight(1f))

      // Footer Actions
      Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
      ) {
        Button(
          onClick = onPermissionGranted,
          colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
          contentPadding = PaddingValues(),
          shape = RoundedCornerShape(12.dp),
          modifier = Modifier
            .fillMaxWidth()
            .height(52.dp)
        ) {
          Box(
            modifier = Modifier
              .fillMaxSize()
              .background(
                brush = Brush.linearGradient(
                  colors = listOf(Color(0xFF3D6FFF), Color(0xFF4DFFDB))
                )
              ),
            contentAlignment = Alignment.Center
          ) {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
              Icon(
                imageVector = Icons.Default.CheckCircle,
                contentDescription = "verify complete icon",
                tint = Color(0xFF00164F),
                modifier = Modifier.size(18.dp)
              )
              Text(
                "GRANT ACCESS",
                color = Color(0xFF00164F),
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
              )
            }
          }
        }

        TextButton(
          onClick = onSkip,
          modifier = Modifier.height(48.dp)
        ) {
          Text(
            "SKIP FOR NOW",
            color = Color(0xFFC3C5D8),
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp
          )
        }
      }
    }
  }
}

@Composable
fun HomeScreen(
  onLockTriggered: () -> Unit,
  onNavigateToProfile: () -> Unit
) {
  var statsTabSelected by remember { mutableStateOf(0) }

  Scaffold(
    modifier = Modifier
      .fillMaxSize()
      .background(Color(0xFF050507)),
    containerColor = Color(0xFF050507),
    bottomBar = {
      GuardianBottomNavBar(
        activeTab = 0,
        onHomeClick = { /* Already here */ },
        onProfileClick = onNavigateToProfile
      )
    }
  ) { innerPadding ->
    Box(
      modifier = Modifier
        .fillMaxSize()
        .padding(innerPadding)
    ) {
      Column(
        modifier = Modifier
          .fillMaxSize()
          .padding(horizontal = 20.dp)
          .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
      ) {
        Spacer(modifier = Modifier.height(20.dp))

        // Profile and Header active bar
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
          ) {
            // Operator profile headshot avatar with green ring
            Box(
              modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(Color(0xFF131316))
                .border(2.dp, Color(0xFF49FCD9), CircleShape)
            ) {
              AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                  .data("https://lh3.googleusercontent.com/aida-public/AB6AXuDiohrRrcgGK62Dnpjiv30jbTUfSbEhAvxzZVtVOuJ7MViR2-9vki2cxjzjvOdLf2-uU2HR-tV-38JYk6SCct9bEjXTL8k_RCxxMVgSP7T919_jwBYSYv7Z2SG1rgKDyEFA7ib4u--buIoDs8SH6lFNJWk2_ooowX-4nPUQvBHbwq1lhs1N2eu0Sdw1qEbPywJiQoy503ElMdUny03t39l-V-gQxoF7h76iVcE5GPafQZLolwtiJb2UOA")
                  .crossfade(true)
                  .build(),
                contentDescription = "Operator Avatar",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
              )
            }

            Column {
              Text(
                "Good evening, John",
                fontSize = 18.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color(0xFFE5E1E5)
              )
              Text(
                "OPERATOR ACTIVE",
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp,
                color = Color(0xFF49FCD9)
              )
            }
          }

          // Bell button alert
          Box(
            modifier = Modifier
              .size(40.dp)
              .clip(CircleShape)
              .background(Color(0xFF201F22).copy(alpha = 0.6f))
              .border(0.5.dp, Color(0xFF434655).copy(alpha = 0.5f), CircleShape),
            contentAlignment = Alignment.Center
          ) {
            Icon(
              imageVector = Icons.Outlined.Notifications,
              contentDescription = "Alert notification bell",
              tint = Color(0xFFE5E1E5),
              modifier = Modifier.size(20.dp)
            )
            // Glowing red pulse point
            Box(
              modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(Color(0xFFFF5167))
                .align(Alignment.TopEnd)
                .offset(x = (-4).dp, y = 4.dp)
            )
          }
        }

        Spacer(modifier = Modifier.height(30.dp))

        // Center HUD Circular animated system armed visualizer
        Box(
          modifier = Modifier
            .size(240.dp)
            .padding(10.dp),
          contentAlignment = Alignment.Center
        ) {
          val infiniteTransition = rememberInfiniteTransition(label = "RadarHUD")
          
          // Outer circle rotating slowly clockwise
          val outerAngle by infiniteTransition.animateFloat(
            initialValue = 0f,
            targetValue = 360f,
            animationSpec = infiniteRepeatable(animation = tween(24000, easing = LinearEasing)),
            label = "outer"
          )
          
          // Inner dashed circle rotating counter-clockwise
          val innerAngle by infiniteTransition.animateFloat(
            initialValue = 360f,
            targetValue = 0f,
            animationSpec = infiniteRepeatable(animation = tween(16000, easing = LinearEasing)),
            label = "inner"
          )

          // Background glowing radar wave effect
          val glowScale by infiniteTransition.animateFloat(
            initialValue = 0.95f,
            targetValue = 1.05f,
            animationSpec = infiniteRepeatable(
              animation = tween(2000, easing = EaseInOutSine),
              repeatMode = RepeatMode.Reverse
            ),
            label = "glow"
          )

          // Outer rotating ring
          Canvas(
            modifier = Modifier
              .fillMaxSize()
              .scale(glowScale)
              .rotate(outerAngle)
          ) {
            drawCircle(
              color = Color(0xFF49FCD9).copy(alpha = 0.12f),
              radius = size.width / 2,
              style = Stroke(width = 1.dp.toPx())
            )
            drawCircle(
              color = Color(0xFF49FCD9).copy(alpha = 0.25f),
              radius = size.width / 2 * 0.95f,
              style = Stroke(width = 1.5.dp.toPx(), pathEffect = androidx.compose.ui.graphics.PathEffect.dashPathEffect(floatArrayOf(15f, 25f), 0f))
            )
          }

          // Inner backward rotating ring
          Canvas(
            modifier = Modifier
              .fillMaxSize(0.85f)
              .rotate(innerAngle)
          ) {
            drawCircle(
              color = Color(0xFFB6C4FF).copy(alpha = 0.15f),
              radius = size.width / 2,
              style = Stroke(width = 1.dp.toPx(), pathEffect = androidx.compose.ui.graphics.PathEffect.dashPathEffect(floatArrayOf(6f, 12f), 0f))
            )
          }

          // Core System button
          Button(
            onClick = onLockTriggered,
            colors = ButtonDefaults.buttonColors(
              containerColor = Color(0xFF0E0E11),
              contentColor = Color(0xFF49FCD9)
            ),
            border = BorderStroke(1.dp, Color(0xFF49FCD9).copy(alpha = 0.5f)),
            shape = CircleShape,
            modifier = Modifier
              .size(130.dp)
              .scale(glowScale)
              .shadow(
                elevation = 20.dp,
                shape = CircleShape,
                ambientColor = Color(0xFF49FCD9),
                spotColor = Color(0xFF49FCD9)
              ),
            contentPadding = PaddingValues()
          ) {
            Column(
              horizontalAlignment = Alignment.CenterHorizontally,
              verticalArrangement = Arrangement.Center
            ) {
              Icon(
                imageVector = Icons.Default.Shield,
                contentDescription = "Lock overlay active system icon",
                tint = Color(0xFF49FCD9),
                modifier = Modifier.size(36.dp)
              )
              Spacer(modifier = Modifier.height(8.dp))
              Text(
                "SYSTEM ARMED",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp,
                color = Color(0xFF49FCD9)
              )
            }
          }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Bento Stats Grid Row (3 cards)
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
          // Card 1: Sats
          Box(
            modifier = Modifier
              .weight(1f)
              .clip(RoundedCornerShape(12.dp))
              .background(Color(0xFF131316).copy(alpha = 0.6f))
              .border(0.5.dp, Color(0xFF434655).copy(alpha = 0.3f), RoundedCornerShape(12.dp))
              .padding(vertical = 12.dp, horizontal = 8.dp),
            contentAlignment = Alignment.Center
          ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
              Icon(
                imageVector = Icons.Default.Satellite,
                contentDescription = "Satellite icon",
                tint = Color(0xFF49FCD9),
                modifier = Modifier.size(20.dp)
              )
              Spacer(modifier = Modifier.height(4.dp))
              Text(
                "12",
                fontSize = 18.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color(0xFFE5E1E5)
              )
              Text(
                "SATS",
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.5.sp,
                color = Color(0xFFC3C5D8).copy(alpha = 0.6f)
              )
            }
          }

          // Card 2: Alerts
          Box(
            modifier = Modifier
              .weight(1f)
              .clip(RoundedCornerShape(12.dp))
              .background(Color(0xFF131316).copy(alpha = 0.6f))
              .border(0.5.dp, Color(0xFF434655).copy(alpha = 0.3f), RoundedCornerShape(12.dp))
              .padding(vertical = 12.dp, horizontal = 8.dp),
            contentAlignment = Alignment.Center
          ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
              Icon(
                imageVector = Icons.Default.Warning,
                contentDescription = "Alerts warning",
                tint = Color(0xFFFF5167),
                modifier = Modifier.size(20.dp)
              )
              Spacer(modifier = Modifier.height(4.dp))
              Text(
                "0",
                fontSize = 18.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color(0xFFE5E1E5)
              )
              Text(
                "ALERTS",
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.5.sp,
                color = Color(0xFFC3C5D8).copy(alpha = 0.6f)
              )
            }
          }

          // Card 3: Coverage
          Box(
            modifier = Modifier
              .weight(1f)
              .clip(RoundedCornerShape(12.dp))
              .background(Color(0xFF131316).copy(alpha = 0.6f))
              .border(0.5.dp, Color(0xFF434655).copy(alpha = 0.3f), RoundedCornerShape(12.dp))
              .padding(vertical = 12.dp, horizontal = 8.dp),
            contentAlignment = Alignment.Center
          ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
              Icon(
                imageVector = Icons.Default.WifiTethering,
                contentDescription = "Coverage active",
                tint = Color(0xFFB6C4FF),
                modifier = Modifier.size(20.dp)
              )
              Spacer(modifier = Modifier.height(4.dp))
              Text(
                "98%",
                fontSize = 18.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color(0xFFE5E1E5)
              )
              Text(
                "COVERAGE",
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.5.sp,
                color = Color(0xFFC3C5D8).copy(alpha = 0.6f)
              )
            }
          }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Safe Zone interactive card with custom Canvas drawing representing high-end satellite HUD
        Card(
          shape = RoundedCornerShape(14.dp),
          colors = CardDefaults.cardColors(containerColor = Color(0xFF131316).copy(alpha = 0.6f)),
          border = BorderStroke(0.5.dp, Color(0xFF434655).copy(alpha = 0.3f)),
          modifier = Modifier.fillMaxWidth()
        ) {
          Column(modifier = Modifier.padding(16.dp)) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Text(
                "Safe Zone",
                fontSize = 18.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color(0xFFE5E1E5)
              )

              Box(
                modifier = Modifier
                  .clip(CircleShape)
                  .background(Color(0xFF49FCD9).copy(alpha = 0.1f))
                  .border(0.5.dp, Color(0xFF49FCD9).copy(alpha = 0.4f), CircleShape)
                  .padding(horizontal = 8.dp, vertical = 4.dp)
              ) {
                Row(
                  verticalAlignment = Alignment.CenterVertically,
                  horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                  Box(
                    modifier = Modifier
                      .size(6.dp)
                      .clip(CircleShape)
                      .background(Color(0xFF49FCD9))
                  )
                  Text(
                    "ACTIVE",
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF49FCD9)
                  )
                }
              }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Satellite HUD Vector Drawing Map Canvas or AsyncImage Loader
            Box(
              modifier = Modifier
                .fillMaxWidth()
                .height(160.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(Color(0xFF0E0E11))
                .border(0.5.dp, Color(0xFF434655).copy(alpha = 0.5f), RoundedCornerShape(10.dp))
            ) {
              // Try to load high-fidelity map backdrop
              AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                  .data("https://lh3.googleusercontent.com/aida-public/AB6AXuAkFEMdcJbK684jlVq3KaPNOgIPIYzvbzw_9N4snUaj-E23gnLz-sbHOGei8UqyQJLxKFZJpF-0bzPzEDL_xFi_Am2iiuzaGBRN204Dix8bSD11zX55a-Cr1IdCyJNcSxb2qP_mBZAZtunUTe8t1yoUY_444nW7chWKw9EKcToGv9zD-Xlc__7ql2aGKpsA0GBU1ASUSdJTZuiISyO7ME2mVuSVBDNS1JdCrBTqS51ajcAEJQNH8777LA")
                  .crossfade(true)
                  .build(),
                contentDescription = "Tactical satellite map",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
              )

              // Custom Overlay coordinate grid crosshairs
              Canvas(modifier = Modifier.fillMaxSize()) {
                val width = size.width
                val height = size.height

                // Draw coordinate tracking crosshair lines
                drawLine(
                  color = Color(0xFF49FCD9).copy(alpha = 0.15f),
                  start = Offset(width / 2, 0f),
                  end = Offset(width / 2, height),
                  strokeWidth = 0.5.dp.toPx()
                )
                drawLine(
                  color = Color(0xFF49FCD9).copy(alpha = 0.15f),
                  start = Offset(0f, height / 2),
                  end = Offset(width, height / 2),
                  strokeWidth = 0.5.dp.toPx()
                )

                // Safe Zone concentric boundary circles
                drawCircle(
                  color = Color(0xFF49FCD9).copy(alpha = 0.3f),
                  radius = 45.dp.toPx(),
                  center = Offset(width / 2, height / 2),
                  style = Stroke(
                    width = 1.dp.toPx(),
                    pathEffect = androidx.compose.ui.graphics.PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
                  )
                )

                // Coordinate points
                drawCircle(
                  color = Color(0xFF49FCD9),
                  radius = 4.dp.toPx(),
                  center = Offset(width / 2, height / 2)
                )
              }

              // Pulse waves concentric from core coordinate
              val infiniteTransition = rememberInfiniteTransition(label = "MapPulse")
              val pulseSize by infiniteTransition.animateFloat(
                initialValue = 0f,
                targetValue = 60f,
                animationSpec = infiniteRepeatable(
                  animation = tween(2000, easing = EaseOutQuad),
                  repeatMode = RepeatMode.Restart
                ),
                label = "pulse"
              )

              Canvas(modifier = Modifier.fillMaxSize()) {
                val cx = size.width / 2
                val cy = size.height / 2
                drawCircle(
                  color = Color(0xFF49FCD9).copy(alpha = 0.35f * (1f - pulseSize / 60f)),
                  radius = pulseSize.dp.toPx(),
                  center = Offset(cx, cy),
                  style = Stroke(width = 1.5.dp.toPx())
                )
              }

              // Glowing center Location Pin
              Icon(
                imageVector = Icons.Default.LocationOn,
                contentDescription = "location pointer",
                tint = Color(0xFF49FCD9),
                modifier = Modifier
                  .size(24.dp)
                  .align(Alignment.Center)
                  .offset(y = (-6).dp)
                  .shadow(elevation = 10.dp, shape = CircleShape)
              )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // CTA Ghost outline button
            OutlinedButton(
              onClick = { },
              border = BorderStroke(0.5.dp, Color(0xFF434655)),
              colors = ButtonDefaults.outlinedButtonColors(
                containerColor = Color(0xFF0E0E11).copy(alpha = 0.5f),
                contentColor = Color(0xFFE5E1E5)
              ),
              shape = RoundedCornerShape(8.dp),
              modifier = Modifier.fillMaxWidth()
            ) {
              Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(vertical = 4.dp)
              ) {
                Icon(
                  imageVector = Icons.Default.EditLocation,
                  contentDescription = "Edit location pointer",
                  tint = Color(0xFFE5E1E5),
                  modifier = Modifier.size(16.dp)
                )
                Text(
                  "UPDATE SAFE LOCATION",
                  fontSize = 11.sp,
                  fontWeight = FontWeight.Bold,
                  letterSpacing = 1.sp
                )
              }
            }
          }
        }

        Spacer(modifier = Modifier.height(40.dp))
      }
    }
  }
}

@Composable
fun ProfileScreen(
  biometricEnabled: Boolean,
  twoFactorEnabled: Boolean,
  alertConfigEnabled: Boolean,
  onBiometricToggle: (Boolean) -> Unit,
  onTwoFactorToggle: (Boolean) -> Unit,
  onAlertConfigToggle: (Boolean) -> Unit,
  onLockTriggered: () -> Unit,
  onLogout: () -> Unit,
  onNavigateToHome: () -> Unit
) {
  Scaffold(
    modifier = Modifier
      .fillMaxSize()
      .background(Color(0xFF050507)),
    containerColor = Color(0xFF050507),
    bottomBar = {
      GuardianBottomNavBar(
        activeTab = 2,
        onHomeClick = onNavigateToHome,
        onProfileClick = { /* Already here */ }
      )
    }
  ) { innerPadding ->
    Box(
      modifier = Modifier
        .fillMaxSize()
        .padding(innerPadding)
    ) {
      Column(
        modifier = Modifier
          .fillMaxSize()
          .padding(horizontal = 20.dp)
          .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
      ) {
        Spacer(modifier = Modifier.height(20.dp))

        // Top brand title bar
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
          ) {
            Icon(
              imageVector = Icons.Default.Shield,
              contentDescription = "Guardian logo icon",
              tint = Color(0xFFB6C4FF),
              modifier = Modifier.size(20.dp)
            )
            Text(
              "GUARDIAN",
              fontSize = 18.sp,
              fontWeight = FontWeight.Black,
              letterSpacing = 2.sp,
              color = Color(0xFFB6C4FF)
            )
          }

          IconButton(
            onClick = { },
            modifier = Modifier
              .size(40.dp)
              .clip(CircleShape)
              .background(Color(0xFF131316).copy(alpha = 0.6f))
              .border(0.5.dp, Color(0xFF434655), CircleShape)
          ) {
            Icon(
              imageVector = Icons.Default.Notifications,
              contentDescription = "Alert notifications config",
              tint = Color(0xFFE5E1E5),
              modifier = Modifier.size(18.dp)
            )
          }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Profile Avatar with rotating border
        Box(
          modifier = Modifier.size(90.dp),
          contentAlignment = Alignment.Center
        ) {
          val infiniteTransition = rememberInfiniteTransition(label = "AvatarBorder")
          val angle by infiniteTransition.animateFloat(
            initialValue = 0f,
            targetValue = 360f,
            animationSpec = infiniteRepeatable(animation = tween(4000, easing = LinearEasing)),
            label = "angle"
          )

          // Glowing spinning gradient mask
          Box(
            modifier = Modifier
              .size(86.dp)
              .rotate(angle)
              .clip(CircleShape)
              .background(
                brush = Brush.sweepGradient(
                  colors = listOf(Color(0xFF49FCD9), Color(0xFF3D6FFF), Color(0xFF49FCD9))
                )
              )
          )

          // Image Mask
          Box(
            modifier = Modifier
              .size(80.dp)
              .clip(CircleShape)
              .background(Color(0xFF050507))
              .border(2.dp, Color(0xFF050507), CircleShape)
          ) {
            AsyncImage(
              model = ImageRequest.Builder(LocalContext.current)
                .data("https://lh3.googleusercontent.com/aida-public/AB6AXuCNiw349BN_OaEpO2ZDpHHydAbRSJYnQwDjZPhulXt_Gem9CO7miqqVPZ84Jy1ob6fWsZG_YpnK2Yu6kfoqVPrtsdB64-JFPxCxQFJQMWxbYbwVHb4gEVLVMJGuyfJyPIc8nZ486KBpflTTyzClFsoqzIZc1x6lZxvVa03Hu6O1RyXwKnVLeAaqkmidLiObNVW8z6-p7qm1MygBwweH_pNCifmBXCQXP1H7A4IShsY9tL_mIBoVmkKADQ")
                .crossfade(true)
                .build(),
              contentDescription = "Tactical Profile Photo",
              contentScale = ContentScale.Crop,
              modifier = Modifier.fillMaxSize()
            )
          }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Text(
          "ALEX VANCE",
          fontSize = 28.sp,
          fontWeight = FontWeight.ExtraBold,
          letterSpacing = 1.sp,
          color = Color(0xFFE5E1E5)
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
          "OPERATIVE ID: 893-X-11",
          fontSize = 11.sp,
          fontWeight = FontWeight.Bold,
          letterSpacing = 1.5.sp,
          color = Color(0xFFC3C5D8).copy(alpha = 0.5f)
        )

        Spacer(modifier = Modifier.height(14.dp))

        // Interactive Badges (Pro / Verified)
        Row(
          horizontalArrangement = Arrangement.spacedBy(8.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          Box(
            modifier = Modifier
              .clip(CircleShape)
              .background(
                brush = Brush.linearGradient(
                  colors = listOf(Color(0xFFFF5167), Color(0xFFFFB3B5))
                )
              )
              .padding(horizontal = 14.dp, vertical = 5.dp)
          ) {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
              Icon(
                imageVector = Icons.Default.Star,
                contentDescription = "pro active badge star",
                tint = Color(0xFF5B0015),
                modifier = Modifier.size(12.dp)
              )
              Text(
                "PRO",
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF5B0015)
              )
            }
          }

          Box(
            modifier = Modifier
              .clip(CircleShape)
              .border(0.5.dp, Color(0xFF49FCD9).copy(alpha = 0.5f), CircleShape)
              .background(Color(0xFF49FCD9).copy(alpha = 0.05f))
              .padding(horizontal = 14.dp, vertical = 5.dp)
          ) {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
              Icon(
                imageVector = Icons.Default.VerifiedUser,
                contentDescription = "verified badge",
                tint = Color(0xFF49FCD9),
                modifier = Modifier.size(12.dp)
              )
              Text(
                "VERIFIED",
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF49FCD9)
              )
            }
          }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Security settings protocols Card List
        SettingsSectionHeader("SECURITY PROTOCOLS")
        Card(
          shape = RoundedCornerShape(14.dp),
          colors = CardDefaults.cardColors(containerColor = Color(0xFF131316).copy(alpha = 0.6f)),
          border = BorderStroke(0.5.dp, Color(0xFF434655).copy(alpha = 0.3f)),
          modifier = Modifier.fillMaxWidth()
        ) {
          Column {
            // Row 1: Biometrics
            SettingsToggleRow(
              icon = Icons.Default.Fingerprint,
              title = "Biometric Authentication",
              isChecked = biometricEnabled,
              onCheckedChange = onBiometricToggle
            )
            Divider(color = Color(0xFF434655).copy(alpha = 0.2f), thickness = 0.5.dp)
            // Row 2: 2FA
            SettingsValueRow(
              icon = Icons.Default.PhonelinkLock,
              title = "Two-Factor Auth",
              value = "OFF",
              onClick = { onTwoFactorToggle(!twoFactorEnabled) }
            )
            Divider(color = Color(0xFF434655).copy(alpha = 0.2f), thickness = 0.5.dp)
            // Row 3: Active Sessions
            SettingsValueRow(
              icon = Icons.Default.Devices,
              title = "Active Sessions",
              value = "3 DEVICES",
              onClick = { }
            )
          }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Preferences header
        SettingsSectionHeader("SYSTEM PREFERENCES")
        Card(
          shape = RoundedCornerShape(14.dp),
          colors = CardDefaults.cardColors(containerColor = Color(0xFF131316).copy(alpha = 0.6f)),
          border = BorderStroke(0.5.dp, Color(0xFF434655).copy(alpha = 0.3f)),
          modifier = Modifier.fillMaxWidth()
        ) {
          Column {
            SettingsToggleRow(
              icon = Icons.Default.NotificationsActive,
              title = "Alert Configuration",
              isChecked = alertConfigEnabled,
              onCheckedChange = onAlertConfigToggle
            )
            Divider(color = Color(0xFF434655).copy(alpha = 0.2f), thickness = 0.5.dp)
            SettingsValueRow(
              icon = Icons.Default.Language,
              title = "Region & Language",
              value = "EN-US",
              onClick = { }
            )
          }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Recent Logs Section
        SettingsSectionHeader("RECENT LOGS")
        Column(
          verticalArrangement = Arrangement.spacedBy(8.dp),
          modifier = Modifier.fillMaxWidth()
        ) {
          LogItemRow(
            isAuthorized = true,
            title = "System Login Authorized",
            node = "NODE: OMEGA-4",
            time = "08:42 AM"
          )

          LogItemRow(
            isAuthorized = false,
            title = "Authentication Failure",
            node = "NODE: EXTERNAL-IP",
            time = "YESTERDAY"
          )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Disconnect logout button
        OutlinedButton(
          onClick = onLogout,
          border = BorderStroke(1.dp, Color(0xFFFF5167).copy(alpha = 0.5f)),
          colors = ButtonDefaults.outlinedButtonColors(
            containerColor = Color(0xFFFF5167).copy(alpha = 0.05f),
            contentColor = Color(0xFFFF5167)
          ),
          shape = RoundedCornerShape(12.dp),
          modifier = Modifier
            .fillMaxWidth()
            .height(52.dp)
        ) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            Icon(
              imageVector = Icons.AutoMirrored.Filled.Logout,
              contentDescription = "Logout icon",
              modifier = Modifier.size(18.dp)
            )
            Text(
              "DISCONNECT",
              fontSize = 11.sp,
              fontWeight = FontWeight.Bold,
              letterSpacing = 1.5.sp
            )
          }
        }

        Spacer(modifier = Modifier.height(30.dp))
      }
    }
  }
}

@Composable
fun SettingsSectionHeader(text: String) {
  Text(
    text = text,
    fontSize = 10.sp,
    fontWeight = FontWeight.Bold,
    letterSpacing = 2.sp,
    color = Color(0xFFB6C4FF),
    modifier = Modifier
      .fillMaxWidth()
      .padding(start = 4.dp, bottom = 8.dp)
  )
}

@Composable
fun SettingsToggleRow(
  icon: androidx.compose.ui.graphics.vector.ImageVector,
  title: String,
  isChecked: Boolean,
  onCheckedChange: (Boolean) -> Unit
) {
  Row(
    modifier = Modifier
      .fillMaxWidth()
      .padding(horizontal = 16.dp, vertical = 12.dp),
    horizontalArrangement = Arrangement.SpaceBetween,
    verticalAlignment = Alignment.CenterVertically
  ) {
    Row(
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
      Box(
        modifier = Modifier
          .size(36.dp)
          .clip(RoundedCornerShape(8.dp))
          .background(Color(0xFF201F22)),
        contentAlignment = Alignment.Center
      ) {
        Icon(
          imageVector = icon,
          contentDescription = title,
          tint = Color(0xFFC3C5D8).copy(alpha = 0.8f),
          modifier = Modifier.size(18.dp)
        )
      }
      Text(
        text = title,
        fontSize = 14.sp,
        fontWeight = FontWeight.SemiBold,
        color = Color(0xFFE5E1E5)
      )
    }

    Switch(
      checked = isChecked,
      onCheckedChange = onCheckedChange,
      colors = SwitchDefaults.colors(
        checkedThumbColor = Color(0xFF0E0E11),
        checkedTrackColor = Color(0xFF49FCD9),
        uncheckedThumbColor = Color(0xFF8D90A1),
        uncheckedTrackColor = Color(0xFF201F22)
      )
    )
  }
}

@Composable
fun SettingsValueRow(
  icon: androidx.compose.ui.graphics.vector.ImageVector,
  title: String,
  value: String = "",
  onClick: () -> Unit
) {
  Row(
    modifier = Modifier
      .fillMaxWidth()
      .clickable { onClick() }
      .padding(horizontal = 16.dp, vertical = 16.dp),
    horizontalArrangement = Arrangement.SpaceBetween,
    verticalAlignment = Alignment.CenterVertically
  ) {
    Row(
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(16.dp),
      modifier = Modifier.weight(1f)
    ) {
      Box(
        modifier = Modifier
          .size(36.dp)
          .clip(RoundedCornerShape(8.dp))
          .background(Color(0xFF201F22)),
        contentAlignment = Alignment.Center
      ) {
        Icon(
          imageVector = icon,
          contentDescription = title,
          tint = Color(0xFFC3C5D8).copy(alpha = 0.8f),
          modifier = Modifier.size(18.dp)
        )
      }
      Text(
        text = title,
        fontSize = 14.sp,
        fontWeight = FontWeight.SemiBold,
        color = Color(0xFFE5E1E5)
      )
    }

    Row(
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
      if (value.isNotEmpty()) {
        Text(
          text = value,
          fontSize = 11.sp,
          fontWeight = FontWeight.Bold,
          letterSpacing = 1.sp,
          color = if (value == "OFF") Color(0xFFC3C5D8).copy(alpha = 0.5f) else Color(0xFF49FCD9)
        )
      }
      Icon(
        imageVector = Icons.Default.ChevronRight,
        contentDescription = "Forward menu navigation",
        tint = Color(0xFF8D90A1),
        modifier = Modifier.size(18.dp)
      )
    }
  }
}

@Composable
fun LogItemRow(
  isAuthorized: Boolean,
  title: String,
  node: String,
  time: String
) {
  Card(
    shape = RoundedCornerShape(10.dp),
    colors = CardDefaults.cardColors(containerColor = Color(0xFF131316).copy(alpha = 0.6f)),
    border = BorderStroke(0.5.dp, Color(0xFF434655).copy(alpha = 0.2f)),
    modifier = Modifier.fillMaxWidth()
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(16.dp),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
      ) {
        // Glowing status node dot
        Box(
          modifier = Modifier
            .size(8.dp)
            .clip(CircleShape)
            .background(if (isAuthorized) Color(0xFF49FCD9) else Color(0xFFFF5167))
            .shadow(
              elevation = 4.dp,
              shape = CircleShape,
              ambientColor = if (isAuthorized) Color(0xFF49FCD9) else Color(0xFFFF5167),
              spotColor = if (isAuthorized) Color(0xFF49FCD9) else Color(0xFFFF5167)
            )
        )

        Column {
          Text(
            text = title,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFFE5E1E5)
          )
          Text(
            text = node,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.5.sp,
            color = Color(0xFFC3C5D8).copy(alpha = 0.4f)
          )
        }
      }

      Text(
        text = time,
        fontSize = 11.sp,
        fontWeight = FontWeight.Bold,
        letterSpacing = 0.5.sp,
        color = Color(0xFF8D90A1)
      )
    }
  }
}

@Composable
fun AlarmOverlayScreen(onUnlock: () -> Unit, onCancel: () -> Unit) {
  var pinCode by remember { mutableStateOf("") }
  var isLockedError by remember { mutableStateOf(false) }
  var isUnlockedSuccess by remember { mutableStateOf(false) }

  // Custom live date/time formatting ticker
  var currentTimeString by remember { mutableStateOf("00:00:00") }
  LaunchedEffect(Unit) {
    while (true) {
      currentTimeString = SimpleDateFormat("HH:mm:ss", Locale.US).format(Date())
      delay(1000)
    }
  }

  // Animation shaking offset on incorrect passcode entry
  val shakeOffset = remember { Animatable(0f) }
  LaunchedEffect(pinCode) {
    if (pinCode.length == 4) {
      delay(300)
      if (pinCode == "1234") {
        // Unlock sequence success
        isUnlockedSuccess = true
        delay(600)
        onUnlock()
      } else {
        // Alarm override fail sequence
        isLockedError = true
        // Trigger a native Compose shake animation
        shakeOffset.animateTo(
          targetValue = 20f,
          animationSpec = spring(dampingRatio = Spring.DampingRatioHighBouncy, stiffness = Spring.StiffnessMedium)
        )
        shakeOffset.animateTo(
          targetValue = -20f,
          animationSpec = spring(dampingRatio = Spring.DampingRatioHighBouncy, stiffness = Spring.StiffnessMedium)
        )
        shakeOffset.animateTo(
          targetValue = 0f,
          animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium)
        )
        pinCode = ""
        isLockedError = false
      }
    }
  }

  Box(
    modifier = Modifier
      .fillMaxSize()
      .background(Color(0xFF050507)) // True deep black void
  ) {
    // Pulsing bright warning plasma red background
    val infiniteTransition = rememberInfiniteTransition(label = "WarningHeartbeat")
    val heartbeatGlow by infiniteTransition.animateFloat(
      initialValue = 0.05f,
      targetValue = 0.2f,
      animationSpec = infiniteRepeatable(
        animation = tween(1500, easing = EaseInOutSine),
        repeatMode = RepeatMode.Reverse
      ),
      label = "heartbeat"
    )

    Box(
      modifier = Modifier
        .fillMaxSize()
        .background(
          brush = Brush.radialGradient(
            colors = listOf(Color(0xFFFF5167).copy(alpha = heartbeatGlow), Color.Transparent),
            radius = 1200f
          )
        )
    )

    // Red lockdown warnings blinking marquee scrolling bar at the top
    Column(modifier = Modifier.fillMaxWidth()) {
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .height(32.dp)
          .background(Color(0xFFFF5167).copy(alpha = 0.2f))
          .border(0.5.dp, Color(0xFFFF5167).copy(alpha = 0.5f))
          .padding(horizontal = 16.dp),
        contentAlignment = Alignment.Center
      ) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text(
            text = "!!! SECURITY BREACH LOCKDOWN ACTIVE !!!",
            color = Color(0xFFFF5167),
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.5.sp,
            modifier = Modifier.weight(1f)
          )

          Text(
            text = currentTimeString,
            color = Color(0xFFFF5167),
            fontSize = 11.sp,
            fontWeight = FontWeight.Black,
            letterSpacing = 1.sp
          )
        }
      }
    }

    Column(
      modifier = Modifier
        .fillMaxSize()
        .padding(horizontal = 24.dp)
        .offset(x = shakeOffset.value.dp),
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.Center
    ) {
      Spacer(modifier = Modifier.height(48.dp))

      // Radial vector locked shield with high voltage cracks visualizer
      Box(
        modifier = Modifier
          .size(160.dp),
        contentAlignment = Alignment.Center
      ) {
        // Orbit HUD circles
        Canvas(modifier = Modifier.fillMaxSize()) {
          drawCircle(
            color = Color(0xFFFF5167).copy(alpha = 0.15f),
            radius = size.width / 2,
            style = Stroke(width = 1.dp.toPx())
          )
          drawCircle(
            color = Color(0xFF49FCD9).copy(alpha = 0.1f),
            radius = size.width / 2 * 0.85f,
            style = Stroke(width = 0.5.dp.toPx(), pathEffect = androidx.compose.ui.graphics.PathEffect.dashPathEffect(floatArrayOf(5f, 10f), 0f))
          )
        }

        Icon(
          imageVector = Icons.Default.Shield,
          contentDescription = "Lock cracked warning shield",
          tint = if (isUnlockedSuccess) Color(0xFF49FCD9) else Color(0xFFFF5167),
          modifier = Modifier
            .size(80.dp)
            .scale(1.1f)
            .shadow(
              elevation = 15.dp,
              shape = CircleShape,
              ambientColor = if (isUnlockedSuccess) Color(0xFF49FCD9) else Color(0xFFFF5167),
              spotColor = if (isUnlockedSuccess) Color(0xFF49FCD9) else Color(0xFFFF5167)
            )
        )
      }

      Spacer(modifier = Modifier.height(24.dp))

      Text(
        text = if (isUnlockedSuccess) "GRID ACCESSIBLE" else "DEVICE LOCKED",
        fontSize = 32.sp,
        fontWeight = FontWeight.ExtraBold,
        letterSpacing = 2.sp,
        color = if (isUnlockedSuccess) Color(0xFF49FCD9) else Color(0xFFFF5167),
        textAlign = TextAlign.Center
      )

      Spacer(modifier = Modifier.height(16.dp))

      // 4 PIN visualizer dots
      Row(
        horizontalArrangement = Arrangement.spacedBy(20.dp),
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(vertical = 12.dp)
      ) {
        repeat(4) { idx ->
          val isFilled = idx < pinCode.length
          val dotColor = when {
            isUnlockedSuccess -> Color(0xFF49FCD9)
            isLockedError -> Color(0xFFFF5167)
            isFilled -> Color(0xFFB6C4FF)
            else -> Color(0xFF434655).copy(alpha = 0.6f)
          }

          Box(
            modifier = Modifier
              .size(14.dp)
              .clip(CircleShape)
              .background(dotColor)
              .border(
                1.dp,
                if (isFilled) Color(0xFFB6C4FF) else Color(0xFF434655).copy(alpha = 0.4f),
                CircleShape
              )
              .shadow(
                elevation = if (isFilled || isUnlockedSuccess) 8.dp else 0.dp,
                shape = CircleShape,
                ambientColor = dotColor,
                spotColor = dotColor
              )
          )
        }
      }

      Spacer(modifier = Modifier.height(30.dp))

      // Standard Numeric Glass-morphism Keypad Matrix
      Column(
        modifier = Modifier.width(280.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
      ) {
        val rows = listOf(
          listOf("1", "2", "3"),
          listOf("4", "5", "6"),
          listOf("7", "8", "9"),
          listOf("CANCEL", "0", "DEL")
        )

        rows.forEach { rowKeys ->
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
          ) {
            rowKeys.forEach { key ->
              Box(
                modifier = Modifier
                  .weight(1f)
                  .aspectRatio(1f)
                  .clip(CircleShape)
                  .background(Color(0xFF131316).copy(alpha = 0.4f))
                  .border(0.5.dp, Color(0xFFB6C4FF).copy(alpha = 0.1f), CircleShape)
                  .clickable {
                    if (key == "DEL") {
                      if (pinCode.isNotEmpty()) pinCode = pinCode.dropLast(1)
                    } else if (key == "CANCEL") {
                      onCancel()
                    } else {
                      if (pinCode.length < 4) pinCode += key
                    }
                  },
                contentAlignment = Alignment.Center
              ) {
                Column(
                  horizontalAlignment = Alignment.CenterHorizontally,
                  verticalArrangement = Arrangement.Center
                ) {
                  if (key == "DEL") {
                    Icon(
                      imageVector = Icons.AutoMirrored.Filled.Backspace,
                      contentDescription = "Backspace back deletion key",
                      tint = Color(0xFFFF5167),
                      modifier = Modifier.size(20.dp)
                    )
                  } else if (key == "CANCEL") {
                    Text(
                      text = "CANCEL",
                      fontSize = 10.sp,
                      fontWeight = FontWeight.Bold,
                      letterSpacing = 0.5.sp,
                      color = Color(0xFF8D90A1)
                    )
                  } else {
                    Text(
                      text = key,
                      fontSize = 24.sp,
                      fontWeight = FontWeight.SemiBold,
                      color = Color(0xFFE5E1E5)
                    )
                    // Sub alphabet labels as in mock screenshots
                    val subText = when (key) {
                      "2" -> "ABC"
                      "3" -> "DEF"
                      "4" -> "GHI"
                      "5" -> "JKL"
                      "6" -> "MNO"
                      "7" -> "PQRS"
                      "8" -> "TUV"
                      "9" -> "WXYZ"
                      else -> ""
                    }
                    if (subText.isNotEmpty()) {
                      Text(
                        text = subText,
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF8D90A1).copy(alpha = 0.6f)
                      )
                    }
                  }
                }
              }
            }
          }
        }
      }

      Spacer(modifier = Modifier.height(40.dp))
    }
  }
}

@Composable
fun GuardianBottomNavBar(
  activeTab: Int,
  onHomeClick: () -> Unit,
  onProfileClick: () -> Unit
) {
  Box(
    modifier = Modifier
      .fillMaxWidth()
      .height(80.dp)
      .background(Color(0xFF201F22).copy(alpha = 0.6f))
      .border(0.5.dp, Color.White.copy(alpha = 0.1f))
      .padding(horizontal = 16.dp, vertical = 8.dp)
  ) {
    Row(
      modifier = Modifier.fillMaxSize(),
      horizontalArrangement = Arrangement.SpaceAround,
      verticalAlignment = Alignment.CenterVertically
    ) {
      // Home Tab button
      NavBarItem(
        icon = Icons.Default.Shield,
        label = "HOME",
        isActive = activeTab == 0,
        onClick = onHomeClick
      )

      // Features Tab button (simulated as custom clickable)
      NavBarItem(
        icon = Icons.Default.GridView,
        label = "FEATURES",
        isActive = activeTab == 1,
        onClick = { }
      )

      // Profile Tab button
      NavBarItem(
        icon = Icons.Default.Person,
        label = "PROFILE",
        isActive = activeTab == 2,
        onClick = onProfileClick
      )
    }
  }
}

@Composable
fun NavBarItem(
  icon: androidx.compose.ui.graphics.vector.ImageVector,
  label: String,
  isActive: Boolean,
  onClick: () -> Unit
) {
  val contentColor = if (isActive) Color(0xFF49FCD9) else Color(0xFFC3C5D8).copy(alpha = 0.6f)
  
  Column(
    horizontalAlignment = Alignment.CenterHorizontally,
    verticalArrangement = Arrangement.Center,
    modifier = Modifier
      .clip(RoundedCornerShape(12.dp))
      .clickable(
        interactionSource = remember { MutableInteractionSource() },
        indication = null,
        onClick = onClick
      )
      .padding(horizontal = 16.dp, vertical = 6.dp)
  ) {
    if (isActive) {
      Box(
        modifier = Modifier
          .clip(RoundedCornerShape(12.dp))
          .background(Color(0xFF49FCD9).copy(alpha = 0.15f))
          .border(0.5.dp, Color(0xFF49FCD9).copy(alpha = 0.3f), RoundedCornerShape(12.dp))
          .padding(horizontal = 12.dp, vertical = 4.dp),
        contentAlignment = Alignment.Center
      ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
          Icon(
            imageVector = icon,
            contentDescription = label,
            tint = Color(0xFF49FCD9),
            modifier = Modifier.size(20.dp)
          )
          Spacer(modifier = Modifier.height(2.dp))
          Text(
            text = label,
            fontSize = 9.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF49FCD9),
            letterSpacing = 0.5.sp
          )
        }
      }
    } else {
      Icon(
        imageVector = icon,
        contentDescription = label,
        tint = contentColor,
        modifier = Modifier.size(20.dp)
      )
      Spacer(modifier = Modifier.height(4.dp))
      Text(
        text = label,
        fontSize = 9.sp,
        fontWeight = FontWeight.Bold,
        color = contentColor,
        letterSpacing = 0.5.sp
      )
    }
  }
}
