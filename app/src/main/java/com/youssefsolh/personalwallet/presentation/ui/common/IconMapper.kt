package com.youssefsolh.personalwallet.presentation.ui.common

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.vector.ImageVector

object IconMapper {
    fun getIcon(iconName: String): ImageVector {
        return when (iconName.lowercase()) {
            // Core icons that definitely exist
            "shopping_cart" -> Icons.Filled.ShoppingCart
            "home" -> Icons.Filled.Home
            "phone" -> Icons.Filled.Phone
            "favorite" -> Icons.Filled.Favorite
            "star" -> Icons.Filled.Star
            "notifications" -> Icons.Filled.Notifications
            "settings" -> Icons.Filled.Settings
            "info" -> Icons.Filled.Info
            "check" -> Icons.Filled.Check
            "close" -> Icons.Filled.Close
            "add" -> Icons.Filled.Add
            "edit" -> Icons.Filled.Edit
            "delete" -> Icons.Filled.Delete
            "send" -> Icons.Filled.Send
            "email" -> Icons.Filled.Email
            "person" -> Icons.Filled.Person
            "location_on" -> Icons.Filled.LocationOn

            // Try to use additional common icons (fallback to basic if not found)
            "restaurant", "work", "card_giftcard", "laptop_mac", "trending_up",
            "attach_money", "directions_car", "bolt", "movie", "shopping_bag",
            "local_hospital", "school", "flight", "receipt", "more_horiz" -> Icons.Filled.AccountCircle

            else -> Icons.Filled.AccountCircle // Default fallback icon
        }
    }
}
