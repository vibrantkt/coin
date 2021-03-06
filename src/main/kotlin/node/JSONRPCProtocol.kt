package node

import models.Block
import models.BlockChain
import models.Transaction
import mu.KotlinLogging
import org.vibrant.core.serialization.ConcreteModelSerializer
import org.vibrant.core.database.blockchain.InstantiateBlockChain
import org.vibrant.core.models.Model
import org.vibrant.core.node.RemoteNode
import org.vibrant.core.rpc.JSONRPCMethod
import org.vibrant.core.rpc.json.JSONRPC
import org.vibrant.core.rpc.json.JSONRPCBlockChainSynchronization
import org.vibrant.core.rpc.json.JSONRPCRequest
import org.vibrant.core.rpc.json.JSONRPCResponse

@Suppress("UNUSED_PARAMETER", "unused")
class JSONRPCProtocol(override val node: Node) : JSONRPC(), JSONRPCBlockChainSynchronization<Peer, Block, Transaction, BlockChain> {


    override val chainSerializer: ConcreteModelSerializer<BlockChain> = object: ConcreteModelSerializer<BlockChain>() {
        override fun deserialize(serialized: ByteArray): BlockChain {
            return JSONSerializer.deserialize(serialized) as BlockChain
        }

        override fun serialize(model: Model): ByteArray {
            return JSONSerializer.serialize(model)
        }

    }

    override val blockSerializer: ConcreteModelSerializer<Block> = object: ConcreteModelSerializer<Block>() {
        override fun deserialize(serialized: ByteArray): Block {
            return JSONSerializer.deserialize(serialized) as Block
        }

        override fun serialize(model: Model): ByteArray {
            return JSONSerializer.serialize(model)
        }

    }

    override val transactionSerializer = object: ConcreteModelSerializer<Transaction>() {
        override fun deserialize(serialized: ByteArray): Transaction {
            return JSONSerializer.deserialize(serialized) as Transaction
        }

        override fun serialize(model: Model): ByteArray {
            return JSONSerializer.serialize(model)
        }

    }

    override val modelToProducer: InstantiateBlockChain<Block, BlockChain> =
            object: InstantiateBlockChain<Block, BlockChain> {
                override fun asBlockChainProducer(model: BlockChain): org.vibrant.core.database.blockchain.BlockChain<Block, BlockChain> {
                    val producer = Chain()
                    producer.setBlocks(model.blocks.toList())
                    return producer
                }

            }

    override val chain: Chain
        get() = node.chain

    override val broadcastedTransactions = arrayListOf<String>()
    override val broadcastedBlocks = arrayListOf<String>()


    override val logger = KotlinLogging.logger {  }

    override fun handleDistinctTransaction(transaction: Transaction) {
        val isMiner = node.isMiner
        if(isMiner){

            (this.node as Miner).addTransaction(
                    transaction
            )
        }
    }

    @JSONRPCMethod
    fun peer(request: JSONRPCRequest, remoteNode: RemoteNode): JSONRPCResponse<*> {
        this.node.peer.addUniqueRemoteNode(remoteNode)
        return JSONRPCResponse(
                result = true,
                error = null,
                id = request.id
        )
    }

}