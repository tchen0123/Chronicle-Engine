package net.openhft.chronicle.engine.map;

import net.openhft.chronicle.core.Jvm;
import net.openhft.chronicle.engine.ThreadMonitoringTest;
import net.openhft.chronicle.engine.api.map.MapView;
import net.openhft.chronicle.engine.api.tree.AssetTree;
import net.openhft.chronicle.engine.server.ServerEndpoint;
import net.openhft.chronicle.engine.tree.VanillaAssetTree;
import net.openhft.chronicle.network.TCPRegistry;
import net.openhft.chronicle.wire.WireType;
import org.jetbrains.annotations.NotNull;
import org.junit.*;
import org.junit.rules.TestName;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.io.Closeable;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;

import static net.openhft.chronicle.engine.Utils.methodName;

/**
 * @author Rob Austin.
 */


/**
 * test using the listener both remotely or locally via the engine
 *
 * @author Rob Austin.
 */
@RunWith(value = Parameterized.class)
public class ServerOverloadTest extends ThreadMonitoringTest {
    private static final String NAME = "test";
    public String connection = "QueryableTest.host.port";
    private static MapView<String, String> map;
    private final Boolean isRemote;
    private final WireType wireType;
    @NotNull
    @Rule
    public TestName name = new TestName();
    private AssetTree assetTree = new VanillaAssetTree().forTesting();
    private VanillaAssetTree serverAssetTree;
    private ServerEndpoint serverEndpoint;

    public ServerOverloadTest(Object isRemote, WireType wireType) {
        this.isRemote = (Boolean) isRemote;
        this.wireType = wireType;
    }

    @Parameterized.Parameters
    public static Collection<Object[]> data() throws IOException {
        return Arrays.asList(
                new Object[]{Boolean.TRUE, WireType.BINARY},
                new Object[]{Boolean.TRUE, WireType.TEXT},
                new Object[]{Boolean.FALSE, WireType.BINARY},
                new Object[]{Boolean.TRUE, WireType.TEXT}
        );
    }


    @Before
    public void before() throws IOException {
        serverAssetTree = new VanillaAssetTree().forTesting();

        if (isRemote) {

            methodName(name.getMethodName());
            connection = "ServerOverloadTest." + name.getMethodName() + ".host.port";
            TCPRegistry.createServerSocketChannelFor(connection);
            serverEndpoint = new ServerEndpoint(connection, serverAssetTree, wireType);
            assetTree = new VanillaAssetTree().forRemoteAccess(connection, wireType);
        } else
            assetTree = serverAssetTree;

        map = assetTree.acquireMap(NAME, String.class, String.class);
    }

    @After
    public void after() throws IOException {
        assetTree.close();
        Jvm.pause(1000);
        if (serverEndpoint != null)
            serverEndpoint.close();
        serverAssetTree.close();
        if (map instanceof Closeable)
            ((Closeable) map).close();
        TCPRegistry.reset();
    }


    @Test
    public void testThatSendingAlotOfDataToTheServerIsNotAnIussue() throws Exception {

        final MapView<String, String> map = assetTree.acquireMap("name", String.class, String
                .class);


        final int megabyte = 1048576;
        char[] large2MBChar = new char[2 * megabyte];

        Arrays.fill(large2MBChar, 'X');

        final String large20MBString = new String(large2MBChar);

        for (int i = 0; i < 20; i++) {
            map.put("" + i, large20MBString);
        }

        Assert.assertEquals(20, map.size());


    }


}

