import models.Block
import models.Transaction
import models.TransactionPayload
import node.Chain
import org.junit.Assert.assertEquals
import org.junit.Ignore
import org.junit.Test
import org.vibrant.example.chat.base.util.AccountUtils
import org.vibrant.example.chat.base.util.HashUtils

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
                TransactionPayload(100_000_000L),
                HashUtils.bytesToHex(HashUtils.sha1(("0x0" + "0x0" + TransactionPayload(100_000_000L).serialize()).toByteArray())),
                "0x0"
        )
        do {
            nonce++
            val serializedTransactions = genesisTransaction.serialize()
            val content = newIndex.toString() + prevHash + serializedTransactions + nonce + timestamp
            blockHash = HashUtils.bytesToHex(HashUtils.sha256(content.toByteArray()))
        } while (blockHash!!.substring(0, difficulty) != "0".repeat(difficulty))

        assertEquals(
                Block(newIndex, prevHash, blockHash, listOf(genesisTransaction), nonce, timestamp = 0).serialize(),
                CLI::class.java.getResource("genesis.json").readText()
        )
    }


    @Ignore
    @Test
    fun `123`(){
        val key = AccountUtils.generateKeyPair()
        println(key.public.algorithm)
        println(key.public.format)
        println(key.public.encoded)
//        println(HashUtils.bytesToHex())
    }
}