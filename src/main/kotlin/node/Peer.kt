package node

import org.vibrant.base.http.HTTPJsonRPCPeer

class Peer(port: Int, rpc: JSONRPCProtocol): HTTPJsonRPCPeer(port, rpc)