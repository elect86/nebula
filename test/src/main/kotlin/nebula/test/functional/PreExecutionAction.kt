package nebula.test.functional

import java.io.File


/**
 * Executes actions before gradle is called.
 */
typealias PreExecutionAction = (projectDir: File, arguments: List<String>, jvmArguments: List<String>) -> Unit
