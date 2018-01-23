package account

import models.Transaction
import java.util.*

data class Account(val address: String, val money: Long, val transactions: List<Transaction>)