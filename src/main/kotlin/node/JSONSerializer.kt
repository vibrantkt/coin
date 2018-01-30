package node

import com.fasterxml.jackson.annotation.JsonTypeName
import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import models.Block
import models.BlockChain
import models.Transaction
import models.SimpleTransactionPayload
import org.vibrant.core.ModelSerializer
import org.vibrant.core.models.Model
import java.util.HashMap

object JSONSerializer : ModelSerializer(){
    override fun deserialize(serialized: ByteArray): Model {
        val map: HashMap<String, Any> = jacksonObjectMapper().readValue(serialized, object : TypeReference<Map<String, Any>>(){})

        val targetType = when(map["@type"]){
            Block::class.java.getAnnotation(JsonTypeName::class.java).value -> {
                Block::class.java
            }
            BlockChain::class.java.getAnnotation(JsonTypeName::class.java).value -> {
                BlockChain::class.java
            }
            Transaction::class.java.getAnnotation(JsonTypeName::class.java).value -> {
                Transaction::class.java
            }
            SimpleTransactionPayload::class.java.getAnnotation(JsonTypeName::class.java).value -> {
                SimpleTransactionPayload::class.java
            }
            else -> {
                throw kotlin.Exception("Can't deserialize type ${map["@type"]}")
            }
        }
        return jacksonObjectMapper().readValue(serialized, targetType)
    }

    override fun serialize(model: Model): ByteArray {
        return jacksonObjectMapper().writeValueAsBytes(model)
    }

}