package de.siba.reportbookgen.service

import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.io.TempDir
import java.nio.file.Path
import kotlin.test.Test

class ReportBookValidationServiceTest {
    private val dataService = ReportBookDataService()
    private val service = ReportBookValidationService()

    @Test
    fun `max hour validation test`() {
        val data = dataService.mapData(createDefaultJson(), createDefaultYearMap())
        assertDoesNotThrow {
            service.validateMaxHours(data, 32)
        }
        assertFailsWithMessage<IllegalArgumentException>("Hours of week 1 add up to 32, which is more than the allowed 31 hours.") {
            service.validateMaxHours(data, 31)
        }
    }

    @Test
    fun `week validation success test`(@TempDir tmpDir: Path) {
        copyResource("example-result.docx", tmpDir.resolve("Nr1_2025.09.01.docx"))
        copyResource("example-result.docx", tmpDir.resolve("Nr2_2025.09.04.docx"))
        copyResource("example-result.docx", tmpDir.resolve("Nr3_2025.09.11.docx"))

        assertDoesNotThrow {
            service.validateWeeks(tmpDir)
        }
    }

    @Test
    fun `week validation fail test`(@TempDir tmpDir: Path) {
        // Missing Nr1
        copyResource("example-result.docx", tmpDir.resolve("Nr2_2025.09.04.docx"))
        copyResource("example-result.docx", tmpDir.resolve("Nr3_2025.09.11.docx"))
        copyResource("example-result.docx", tmpDir.resolve("Nr4_2025.09.01.docx"))

        assertFailsWithMessage<IllegalStateException>("Missing week 1.") {
            service.validateWeeks(tmpDir)
        }
    }
}