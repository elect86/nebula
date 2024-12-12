package nebula.test.functional.internal.toolingapi

import nebula.test.functional.internal.DefaultExecutionResult

/**
 * Hold additional response data, that is only available
 */
class ToolingExecutionResult(success: Boolean,
                             standardOutput: String, standardError: String,
                             executedTasks: List<MinimalExecutedTask>,
                             failure: Throwable?) : DefaultExecutionResult(success, standardOutput, standardError, executedTasks, failure)
