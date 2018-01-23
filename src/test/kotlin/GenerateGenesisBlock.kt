import models.Block
import models.Transaction
import models.TransactionPayload
import org.junit.Assert.assertEquals
import org.junit.Test
import org.vibrant.example.chat.base.util.HashUtils

class GenerateGenesisBlock {


    @Test
    fun `Generate`(){
        val difficulty = 2

        var nonce = 0L
        val newIndex = 0
        val prevHash = ""
        var blockHash: String?
        val timestamp = 0
        val genesisTransaction = Transaction.create(
                from = "0x0",
                to = "0x0",
                payload = TransactionPayload(100_000_000L)
        )
        do {
            nonce++
            val serializedTransactions = genesisTransaction.serialize()
            val content = newIndex.toString() + prevHash + serializedTransactions + nonce + timestamp
            blockHash = HashUtils.bytesToHex(HashUtils.sha256(content.toByteArray()))
        } while (blockHash!!.substring(0, difficulty) != "0".repeat(difficulty))

        assertEquals(
                Block(newIndex, prevHash, blockHash, listOf(genesisTransaction), nonce, timestamp = 0).serialize(),
                Chain::class.java.getResource("genesis.json").readText()
        )
    }

}