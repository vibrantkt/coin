import org.vibrant.base.http.HTTPJsonRPCPeer
import org.vibrant.core.node.RemoteNode

class Peer(port: Int, rpc: JSONRPCProtocol): HTTPJsonRPCPeer(port, rpc)