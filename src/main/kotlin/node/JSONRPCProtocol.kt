package node

import models.Block
import models.Transaction
import org.vibrant.base.rpc.JSONRPCMethod
import org.vibrant.base.rpc.json.*
import org.vibrant.core.node.RemoteNode
import serialize

@Suppress("UNUSED_PARAMETER", "unused")
class JSONRPCProtocol(private val node: Node) : JSONRPC() {


    private val broadcastedTransactions = arrayListOf<String>()
    private val broadcastedBlocks = arrayListOf<String>()

    @JSONRPCMethod
    fun addTransaction(request: JSONRPCRequest, remoteNode: RemoteNode): JSONRPCResponse<*>{
        val transaction = JSONSerializer.deserialize(request.params[0].toString().toByteArray()) as Transaction
        if(!this.broadcastedTransactions.contains(transaction.hash)) {
            this.broadcastedTransactions.add(transaction.hash)
            this.node.peer.broadcast(request, this.node.peer.peers.filter { it.address != remoteNode.address || it.port != remoteNode.port })
            val isMiner = node.isMiner
            if(isMiner){

                (this.node as Miner).addTransaction(
                        transaction
                )
            }
        }
        return JSONRPCResponse(node.isMiner, null, request.id)
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


    @JSONRPCMethod
    fun onNewBlock(request: JSONRPCRequest, remoteNode: RemoteNode): JSONRPCResponse<*>{
        val block = JSONSerializer.deserialize(request.params[0].toString().toByteArray()) as Block
        logger.info { "Received last block, handling.." }
        val result = this.node.handleLastBlock(remoteNode, block)

        if(!broadcastedBlocks.contains(block.hash)){
            broadcastedBlocks.add(block.hash)
            logger.info { "$result - true => block already attached and i won't share" }
            if(!result) {
                val some = this.node.peer.broadcast(request, this.node.peer.peers.filter { it.address != remoteNode.address || it.port != remoteNode.port })
                logger.info { "Broad casted between connected nodes $some" }
            }
        }
        return JSONRPCResponse(result, null, request.id)
    }

    @JSONRPCMethod
    fun getLastBlock(request: JSONRPCRequest, remoteNode: RemoteNode): JSONRPCResponse<*>{
        return JSONRPCResponse(
                this.node.chain.latestBlock().serialize(),
                null,
                request.id
        )
    }

    @JSONRPCMethod
    fun getFullChain(request: JSONRPCRequest, remoteNode: RemoteNode): JSONRPCResponse<*>{
        logger.info { this.node.chain.produce(JSONSerializer).serialize() }
        return JSONRPCResponse(
                this.node.chain.produce(JSONSerializer).serialize(),
                null,
                request.id
        )
    }



    @JSONRPCMethod
    fun syncWithMe(request: JSONRPCRequest, remoteNode: RemoteNode): JSONRPCResponse<*>{
        this.node.synchronize(remoteNode)
        return JSONRPCResponse(
                true,
                null,
                request.id
        )
    }
}