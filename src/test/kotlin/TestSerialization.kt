import models.Block
import models.BlockChain
import models.Transaction
import models.SimpleTransactionPayload
import node.JSONSerializer
import org.junit.Assert
import org.junit.Assert.assertEquals
import org.junit.Test
import org.vibrant.core.ConcreteModelSerializer
import org.vibrant.core.hash.SignTools
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
        assertFor(SimpleTransactionPayload(100L))
        assertFor(Transaction.create("from", "to", SimpleTransactionPayload(100L), SignTools.generateKeyPair("RSA")))
        assertFor(Block(0, "", "", listOf(Transaction.create("from", "to", SimpleTransactionPayload(100L), SignTools.generateKeyPair("RSA"))), 1))
        assertFor(BlockChain(0, arrayOf(Block(0, "", "", listOf(Transaction.create("from", "to", SimpleTransactionPayload(100L), SignTools.generateKeyPair("RSA"))), 1))))
    }


    @Test
    fun strange(){
        val serializer: ConcreteModelSerializer<BlockChain> = object: ConcreteModelSerializer<BlockChain>() {
            override fun deserialize(serialized: ByteArray): BlockChain {
                return JSONSerializer.deserialize(serialized) as BlockChain
            }

            override fun serialize(model: Model): ByteArray {
                return JSONSerializer.serialize(model)
            }

        }
        val block = Block(0, "", "", listOf(Transaction.create("from", "to", SimpleTransactionPayload(100L), SignTools.generateKeyPair("RSA"))), 1)
        assertEquals(
            String(JSONSerializer.serialize(BlockChain(2, arrayOf(block)))),
            String(serializer.serialize(BlockChain(2, arrayOf(block))))
        )
    }
}