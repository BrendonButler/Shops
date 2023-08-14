package net.sparkzz.util;

import be.seeseemelk.mockbukkit.MockBukkit;
import be.seeseemelk.mockbukkit.ServerMock;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.TestMethodOrder;

import static net.sparkzz.shops.TestHelper.printMessage;

@DisplayName("Config Test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class ConfigTest {

    private static ServerMock server;

    @BeforeAll
    static void setUp() {
        printMessage("==[ TEST CONFIG ]==");
        server = MockBukkit.getOrCreateMock();
    }
}
