package com.ortsinton.easysaving.domain.model

import kotlinx.datetime.LocalDate

data class Transaction(
    val id: Long,
    val amount: Money,
    val description: String,
    val date: LocalDate,
    val categoryId: Long,
)
