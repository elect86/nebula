/**
 *
 *  Copyright 2020 Netflix, Inc.
 *
 *     Licensed under the Apache License, Version 2.0 (the "License");
 *     you may not use this file except in compliance with the License.
 *     You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
 *
 */

package nebula.test

import kotlin.test.Test

class IntegrationTestKitSpecCustomBaseFolderSpec : IntegrationTestKitSpec() {

    //    def setup() {
    //        // used to test trait & groovy setup method https://stackoverflow.com/questions/56464191/public-groovy-method-must-be-public-says-the-compiler
    //    }
    //
    //    def cleanup() {
    //        // used to test trait & groovy cleanup method https://stackoverflow.com/questions/56464191/public-groovy-method-must-be-public-says-the-compiler
    //    }

    override val projectBaseFolderName = "notnebulatest"

    @Test
    fun `can override project dir base folder name`() {
        assert("/build/nebulatest/" !in projectDir.absolutePath)
        assert("/build/notnebulatest/" in projectDir.absolutePath)
    }
}
