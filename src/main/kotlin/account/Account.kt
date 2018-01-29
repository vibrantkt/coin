package account

import models.Transaction

data class Account(val address: String, val money: Long, val transactions: List<Transaction>)