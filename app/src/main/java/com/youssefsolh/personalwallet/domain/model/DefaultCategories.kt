package com.youssefsolh.personalwallet.domain.model

object DefaultCategories {

    fun getDefaultCategories(): List<Category> = listOf(
        // Income Categories - Using emojis for vibrant visual appeal
        Category(
            id = "income_salary",
            name = "Salary",
            icon = "💼",
            color = "#4CAF50",
            type = TransactionType.INCOME,
            isDefault = true
        ),
        Category(
            id = "income_freelance",
            name = "Freelance",
            icon = "💻",
            color = "#2196F3",
            type = TransactionType.INCOME,
            isDefault = true
        ),
        Category(
            id = "income_investment",
            name = "Investment",
            icon = "📈",
            color = "#FF9800",
            type = TransactionType.INCOME,
            isDefault = true
        ),
        Category(
            id = "income_gift",
            name = "Gift",
            icon = "🎁",
            color = "#E91E63",
            type = TransactionType.INCOME,
            isDefault = true
        ),
        Category(
            id = "income_bonus",
            name = "Bonus",
            icon = "🎉",
            color = "#9C27B0",
            type = TransactionType.INCOME,
            isDefault = true
        ),
        Category(
            id = "income_other",
            name = "Other Income",
            icon = "💰",
            color = "#00BCD4",
            type = TransactionType.INCOME,
            isDefault = true
        ),

        // Expense Categories - Using emojis
        Category(
            id = "expense_food",
            name = "Food & Dining",
            icon = "🍽️",
            color = "#FF5722",
            type = TransactionType.EXPENSE,
            isDefault = true
        ),
        Category(
            id = "expense_groceries",
            name = "Groceries",
            icon = "🛒",
            color = "#4CAF50",
            type = TransactionType.EXPENSE,
            isDefault = true
        ),
        Category(
            id = "expense_transport",
            name = "Transportation",
            icon = "🚗",
            color = "#2196F3",
            type = TransactionType.EXPENSE,
            isDefault = true
        ),
        Category(
            id = "expense_utilities",
            name = "Utilities",
            icon = "⚡",
            color = "#FFC107",
            type = TransactionType.EXPENSE,
            isDefault = true
        ),
        Category(
            id = "expense_entertainment",
            name = "Entertainment",
            icon = "🎬",
            color = "#9C27B0",
            type = TransactionType.EXPENSE,
            isDefault = true
        ),
        Category(
            id = "expense_shopping",
            name = "Shopping",
            icon = "🛍️",
            color = "#E91E63",
            type = TransactionType.EXPENSE,
            isDefault = true
        ),
        Category(
            id = "expense_health",
            name = "Health & Medical",
            icon = "🏥",
            color = "#F44336",
            type = TransactionType.EXPENSE,
            isDefault = true
        ),
        Category(
            id = "expense_education",
            name = "Education",
            icon = "📚",
            color = "#3F51B5",
            type = TransactionType.EXPENSE,
            isDefault = true
        ),
        Category(
            id = "expense_travel",
            name = "Travel",
            icon = "✈️",
            color = "#00BCD4",
            type = TransactionType.EXPENSE,
            isDefault = true
        ),
        Category(
            id = "expense_bills",
            name = "Bills",
            icon = "🧾",
            color = "#607D8B",
            type = TransactionType.EXPENSE,
            isDefault = true
        ),
        Category(
            id = "expense_fitness",
            name = "Fitness & Sports",
            icon = "🏋️",
            color = "#FF6F00",
            type = TransactionType.EXPENSE,
            isDefault = true
        ),
        Category(
            id = "expense_pets",
            name = "Pets",
            icon = "🐾",
            color = "#8D6E63",
            type = TransactionType.EXPENSE,
            isDefault = true
        ),
        Category(
            id = "expense_home",
            name = "Home & Garden",
            icon = "🏡",
            color = "#689F38",
            type = TransactionType.EXPENSE,
            isDefault = true
        ),
        Category(
            id = "expense_subscriptions",
            name = "Subscriptions",
            icon = "📱",
            color = "#5E35B1",
            type = TransactionType.EXPENSE,
            isDefault = true
        ),
        Category(
            id = "expense_other",
            name = "Other Expense",
            icon = "💸",
            color = "#9E9E9E",
            type = TransactionType.EXPENSE,
            isDefault = true
        )
    )
}

object EmojiIconLibrary {
    val availableIcons = listOf(
        // Money & Finance
        "💰", "💵", "💴", "💶", "💷", "💳", "🏦", "💼", "💻", "📈", "📊", "💹", "🎁", "🎉",

        // Shopping & Food
        "🍽️", "🍕", "🍔", "🍟", "🍜", "☕", "🍺", "🍷", "🛒", "🛍️", "👕", "👗", "👠",

        // Transportation
        "🚗", "🚕", "🚙", "🚌", "🚎", "🏎️", "🚓", "🚑", "🚒", "🚐", "🚚", "🚛", "🚜",
        "🏍️", "🛵", "🚲", "🛴", "⛽", "✈️", "🚁", "⛵", "🚤", "🚆", "🚇", "🚊", "🚝",

        // Entertainment
        "🎬", "🎮", "🎯", "🎲", "🎪", "🎨", "🎭", "🎤", "🎧", "🎵", "🎸", "🎹", "🎺", "🎻",

        // Health & Education
        "🏥", "⚕️", "💊", "💉", "🩺", "📚", "📖", "📝", "✏️", "🎓", "🏫", "🔬", "🔭",

        // Home & Utilities
        "🏡", "🏠", "🏘️", "🏗️", "🔨", "🔧", "🛠️", "⚡", "💡", "🔥", "💧", "📱", "💻", "⌨️", "🖱️",

        // Sports & Fitness
        "⚽", "🏀", "🏈", "⚾", "🥎", "🎾", "🏐", "🏉", "🥏", "🎱", "🏓", "🏸", "🏒", "🏑",
        "🥍", "🏏", "⛳", "🏹", "🎣", "🥊", "🥋", "🥅", "⛸️", "🎿", "🛷", "⛷️", "🏂",
        "🏋️", "🤸", "🧘", "🚴", "🚵", "🤾", "🏊", "🏄", "🧗", "🤺",

        // Pets & Animals
        "🐾", "🐶", "🐱", "🐭", "🐹", "🐰", "🦊", "🐻", "🐼", "🐨", "🐯", "🦁", "🐮",
        "🐷", "🐸", "🐵", "🐔", "🐧", "🐦", "🐤", "🦆", "🦅", "🦉", "🦇", "🐺", "🐗",

        // Nature & Weather
        "🌸", "🌺", "🌻", "🌹", "🌷", "🌱", "🌲", "🌳", "🌴", "🌵", "🌾", "🌿", "☀️",
        "🌙", "⭐", "🌟", "⛅", "⛈️", "🌈", "❄️", "⚡", "🔥", "💧", "🌊",

        // Common
        "❤️", "⭐", "🔔", "⚙️", "ℹ️", "✅", "❌", "➕", "✏️", "🗑️", "📧", "👤", "📍",
        "💬", "📞", "📅", "⏰", "🔒", "🔓", "🔑", "🎯", "🏆", "🎖️", "💎", "👑"
    )
}
