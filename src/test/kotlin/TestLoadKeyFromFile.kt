import node.Node
import org.junit.Assert.assertArrayEquals
import org.junit.Test
import org.vibrant.base.util.SignTools
import java.io.File

class TestLoadKeyFromFile {


    @Test
    fun `Test deserialization`(){
        val kp = SignTools.generateKeyPair("RSA")
        val tmp = File.createTempFile("keypair", "lol")
        Node.saveKeyPair(kp, tmp.absolutePath)
        val kp2 = Node.loadFromFile(tmp.absolutePath)
        assertArrayEquals(
                kp.public.encoded,
                kp2.public.encoded
        )

        assertArrayEquals(
                kp.private.encoded,
                kp2.private.encoded
        )
    }
}