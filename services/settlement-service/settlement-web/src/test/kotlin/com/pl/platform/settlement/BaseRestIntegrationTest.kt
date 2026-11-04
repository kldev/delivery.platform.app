package com.pl.platform.settlement

import com.pl.platform.settlement.testing.BaseIntegrationTest
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureRestTestClient
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.client.RestTestClient
import tools.jackson.databind.json.JsonMapper

@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT
)
@AutoConfigureRestTestClient
@ActiveProfiles("test")
abstract class BaseRestIntegrationTest : BaseIntegrationTest() {

    @LocalServerPort
    protected var port: Int = 0

    @Autowired
    protected lateinit var restTestClient: RestTestClient

    @Autowired
    protected lateinit var jsonMapper: JsonMapper

    protected fun url(path: String): String =
        "http://localhost:$port$path"
}