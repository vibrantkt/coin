import models.Block
import models.Transaction
import models.SimpleTransactionPayload
import org.junit.Assert.assertEquals
import org.junit.Test
import org.vibrant.core.hash.HashUtils
import org.vibrant.core.hash.SHA1

class GenerateGenesisBlock {


    @Test
    fun `Generate`(){
        val difficulty = 2

        var nonce = 0L
        val newIndex = 0L
        val prevHash = ""
        var blockHash: String?
        val timestamp = 0
        val genesisTransaction = Transaction(
                "0x0",
                "0x0",
                SimpleTransactionPayload(100_000_000L),
                HashUtils.bytesToHex(SHA1.produceHash(("0x0" + "0x0" + SimpleTransactionPayload(100_000_000L).serialize()).toByteArray())),
                "0x0"
        )
        do {
            nonce++
            val serializedTransactions = genesisTransaction.serialize()
            val content = newIndex.toString() + prevHash + serializedTransactions + nonce + timestamp
            blockHash = HashUtils.bytesToHex(SHA1.produceHash(content.toByteArray()))
        } while (blockHash!!.substring(0, difficulty) != "0".repeat(difficulty))

        assertEquals(
                Block(newIndex, prevHash, blockHash, listOf(genesisTransaction), nonce, timestamp = 0).serialize(),
                CLI::class.java.getResource("genesis.json").readText()
        )
    }
}