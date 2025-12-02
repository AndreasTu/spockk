/*
 * Copyright 2025 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *     https://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.github.pshevche.spockk.mock

import io.github.pshevche.spockk.lang.compareTo
import io.github.pshevche.spockk.lang.expect
import io.github.pshevche.spockk.lang.given
import io.github.pshevche.spockk.lang.then
import io.github.pshevche.spockk.lang.times
import io.github.pshevche.spockk.lang.`when`
import org.spockframework.mock.MockUtil
import org.spockframework.mock.runtime.MockController
import spock.lang.Specification
import java.util.function.Supplier

/** Tests the usage of the underlying Spock MockingApi is working. */
class InteractionTest : Specification() {

  /*
  fun `MockName uses the variable name and var type`() {
    given
    // times()
    val myMock: Runnable = Mock()
    1 * myMock.run() > {

    }

    3 * myMock.run()
    myMock.run() > {

    }

    `when`
    true
    then
    true
  }*/

  fun `Mock zero interaction`(){
    given
    val mockController = specificationContext
    val m : Supplier<String> = Mock()
    0 * m.get()

    `when`
    val res = m.get()
    then
    res == "Val"
  }

  /*
  fun `Mock data return`(){
    given
    val m : Supplier<String> = Mock()
    1 * m.get() > "Val"

    `when`
    val res = m.get()
    then
    res == "Val"
    true
  }*/
}
