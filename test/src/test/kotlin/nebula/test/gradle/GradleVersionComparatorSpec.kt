package nebula.test.gradle

import nebula.test.GradleVersion
import kotlin.test.Test

class GradleVersionComparatorSpec {

    @Test
    fun `checks if version is greater than`() {
        val `v5․0` = GradleVersion("5.0")
        val `v4․10․3` = GradleVersion("4.10.3")

        assert(`v5․0` > `v4․10․3`)
    }

    @Test
    fun `checks if version is less than`() {

        val `v5․0` = GradleVersion("5.0")
        val `v5․1` = GradleVersion("5.1")

        assert(`v5․0` < `v5․1`)
    }
}
