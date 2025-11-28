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
            id = "income_rental",
            name = "Rental Income",
            icon = "🏠",
            color = "#795548",
            type = TransactionType.INCOME,
            isDefault = true
        ),
        Category(
            id = "income_refund",
            name = "Refund",
            icon = "↩️",
            color = "#607D8B",
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
            id = "expense_rent",
            name = "Rent/Mortgage",
            icon = "🏠",
            color = "#795548",
            type = TransactionType.EXPENSE,
            isDefault = true
        ),
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
            id = "expense_insurance",
            name = "Insurance",
            icon = "🛡️",
            color = "#1565C0",
            type = TransactionType.EXPENSE,
            isDefault = true
        ),
        Category(
            id = "expense_personal_care",
            name = "Personal Care",
            icon = "💇",
            color = "#EC407A",
            type = TransactionType.EXPENSE,
            isDefault = true
        ),
        Category(
            id = "expense_clothing",
            name = "Clothing",
            icon = "👔",
            color = "#AB47BC",
            type = TransactionType.EXPENSE,
            isDefault = true
        ),
        Category(
            id = "expense_fuel",
            name = "Gas & Fuel",
            icon = "⛽",
            color = "#EF6C00",
            type = TransactionType.EXPENSE,
            isDefault = true
        ),
        Category(
            id = "expense_internet",
            name = "Internet & Phone",
            icon = "📡",
            color = "#0097A7",
            type = TransactionType.EXPENSE,
            isDefault = true
        ),
        Category(
            id = "expense_coffee",
            name = "Coffee & Snacks",
            icon = "☕",
            color = "#6D4C41",
            type = TransactionType.EXPENSE,
            isDefault = true
        ),
        Category(
            id = "expense_gifts",
            name = "Gifts Given",
            icon = "🎁",
            color = "#D81B60",
            type = TransactionType.EXPENSE,
            isDefault = true
        ),
        Category(
            id = "expense_charity",
            name = "Charity & Donations",
            icon = "❤️",
            color = "#C62828",
            type = TransactionType.EXPENSE,
            isDefault = true
        ),
        Category(
            id = "expense_car_maintenance",
            name = "Car Maintenance",
            icon = "🔧",
            color = "#455A64",
            type = TransactionType.EXPENSE,
            isDefault = true
        ),
        Category(
            id = "expense_restaurants",
            name = "Restaurants & Bars",
            icon = "🍷",
            color = "#6A1B9A",
            type = TransactionType.EXPENSE,
            isDefault = true
        ),
        Category(
            id = "expense_loans",
            name = "Loans & Debt",
            icon = "🏦",
            color = "#424242",
            type = TransactionType.EXPENSE,
            isDefault = true
        ),
        Category(
            id = "expense_savings",
            name = "Savings",
            icon = "🐷",
            color = "#F48FB1",
            type = TransactionType.EXPENSE,
            isDefault = true
        ),
        Category(
            id = "expense_children",
            name = "Children & Childcare",
            icon = "👶",
            color = "#FFB74D",
            type = TransactionType.EXPENSE,
            isDefault = true
        ),
        Category(
            id = "expense_business",
            name = "Business Expenses",
            icon = "💼",
            color = "#5D4037",
            type = TransactionType.EXPENSE,
            isDefault = true
        ),
        Category(
            id = "expense_parking",
            name = "Parking & Tolls",
            icon = "🅿️",
            color = "#78909C",
            type = TransactionType.EXPENSE,
            isDefault = true
        ),
        Category(
            id = "expense_laundry",
            name = "Laundry & Cleaning",
            icon = "🧺",
            color = "#64B5F6",
            type = TransactionType.EXPENSE,
            isDefault = true
        ),
        Category(
            id = "expense_books",
            name = "Books & Media",
            icon = "📖",
            color = "#9575CD",
            type = TransactionType.EXPENSE,
            isDefault = true
        ),
        Category(
            id = "expense_furniture",
            name = "Furniture & Appliances",
            icon = "🛋️",
            color = "#A1887F",
            type = TransactionType.EXPENSE,
            isDefault = true
        ),
        Category(
            id = "expense_taxes",
            name = "Taxes",
            icon = "📋",
            color = "#757575",
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
