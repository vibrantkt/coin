package node

import org.vibrant.core.node.http.HTTPJsonRPCPeer


class Peer(port: Int, rpc: JSONRPCProtocol): HTTPJsonRPCPeer(port, rpc)