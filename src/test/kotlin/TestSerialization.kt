import models.Block
import models.BlockChain
import models.Transaction
import models.TransactionPayload
import org.junit.Assert
import org.junit.Test
import org.vibrant.core.models.Model

class TestSerialization {


    @Test
    fun `Test type equality`(){

        fun <T: Model> assertFor(t: T){
            val serialized = JSONSerializer.serialize(t)
            val deserialized = JSONSerializer.deserialize(serialized)
            Assert.assertEquals(
                    t::class,
                    deserialized::class
            )
        }
        assertFor(TransactionPayload(100L))
        assertFor(Transaction.create("from", "to", TransactionPayload(100L)))
        assertFor(Block(0, "", "", listOf(Transaction.create("from", "to", TransactionPayload(100L))), 1))
        assertFor(BlockChain(0, arrayOf(Block(0, "", "", listOf(Transaction.create("from", "to", TransactionPayload(100L))), 1))))

    }
}