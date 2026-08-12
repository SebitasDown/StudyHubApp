package org.studyhub.project.net

import kotlin.test.Test
import kotlin.test.assertNotNull

/**
 * Regresión del bug que rompía el login: `json` se inicializaba DESPUÉS de `client`,
 * y `configure()` lo usaba al crear el HttpClient → NPE "Parameter specified as
 * non-null is null: parameter json" → la app mostraba "No se pudo conectar".
 */
class ApiClientInitTest {

    @Test
    fun apiClientSeConstruyeSinNpe() {
        val client = ApiClient()
        assertNotNull(client)
    }
}
