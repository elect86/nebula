package nebula.test.functional.internal

interface ExecutedTask {
    val path: String
    val upToDate: Boolean
    val skipped: Boolean
}
