package com.example.recharge.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

val RechargeShapes = Shapes(
    // Small controls: checkboxes, micro-inputs
    small = RoundedCornerShape(8.dp),
    // Cards, sheets, modals
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(16.dp),
    // Pills: buttons, chips, badges
    extraLarge = RoundedCornerShape(percent = 50)
)

/** Full-pill shape for primary/secondary buttons */
val PillShape = RoundedCornerShape(percent = 50)

/** Card shape — 16dp corners */
val CardShape = RoundedCornerShape(16.dp)

/** Bottom sheet handle */
val BottomSheetShape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
