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

package io.github.pshevche.spockk.compilation.transformer.mock

import io.github.pshevche.spockk.compilation.common.SpockkConstants
import io.github.pshevche.spockk.compilation.common.classId
import io.github.pshevche.spockk.compilation.ir.ContextAwareIrFactory
import org.jetbrains.kotlin.ir.declarations.IrClass
import org.jetbrains.kotlin.ir.declarations.IrDeclaration
import org.jetbrains.kotlin.ir.declarations.IrFunction
import org.jetbrains.kotlin.ir.expressions.IrCall
import org.jetbrains.kotlin.ir.expressions.IrConst
import org.jetbrains.kotlin.ir.expressions.IrExpression
import org.jetbrains.kotlin.ir.symbols.UnsafeDuringIrConstructionAPI
import org.jetbrains.kotlin.ir.util.defaultType
import org.jetbrains.kotlin.ir.util.file
import org.jetbrains.kotlin.ir.util.isStatic
import org.jetbrains.kotlin.ir.util.kotlinFqName

internal class InteractionRewriter(
  private val irFactory: ContextAwareIrFactory,
  private val stat: IrExpression,
  private val spec: IrClass,
  private val declaringFunction :IrFunction
) {
  companion object {
    private val INTERACTION_BUILDER_CLASS = classId("org.spockframework.mock.runtime.InteractionBuilder")
    private val MOCK_CONTROLLER_CLASS =  classId("org.spockframework.mock.runtime.MockController")
    private val SPEC_CONTEXT_CLASS =  classId("org.spockframework.lang.ISpecificationContext")
  }

  private var result: Boolean? = null
  private var count: IrExpression? = null
  private var mockCall: IrExpression? = null

  @OptIn(UnsafeDuringIrConstructionAPI::class)
  fun isInteraction(): Boolean {
    if (result != null) {
      return result!!
    }
    if(declaringFunction.isStatic){
      return false
    }

    //val expr: org.codehaus.groovy.ast.expr.Expression = parseCount(parseResults(stat.getExpression()))
    //val interaction = true // (count != null || !responses.isEmpty()) && parseCall(expr)
    /*if (interaction && resources.getCurrentMethod().getAst().isStatic()) {
      throw InvalidSpecCompileException(stat, "Interactions cannot be declared in static scope")
    }
    */
    var interaction = false
    if (stat is IrCall) {
      val call: IrCall = stat
      if (stat.symbol.owner.kotlinFqName == SpockkConstants.SPOCKK_TIMES_METHOD) {
        if (call.arguments.size == 2) {
          val count = call.arguments[0]
          val mockCall = call.arguments[1]
          if (count is IrConst) {
            this.count = count
            this.mockCall = mockCall
            interaction = true
          }
        }
      }
    }

    result = interaction
    return interaction
  }

  @OptIn(UnsafeDuringIrConstructionAPI::class)
  fun rewrite(): IrExpression? {
    if (!isInteraction()) {
      return null
    }

    val lineAndCol = spec.file.fileEntry.getLineAndColumnNumbers(stat.startOffset)
    val ctor = irFactory.constructorCall(INTERACTION_BUILDER_CLASS,
      irFactory.const(lineAndCol.line + 1),
      irFactory.const(lineAndCol.column + 1),
      irFactory.const("mock text"),
      )
    var expr: IrExpression = ctor
    if( count!= null) {
      expr = irFactory.call(INTERACTION_BUILDER_CLASS, "setFixedCount",
        expr,
        count!!
        )
    }

    //val mockInteraction = builder.build()
    val mockInteraction = irFactory.call(INTERACTION_BUILDER_CLASS,"build", expr)
    return addMockInteraction(mockInteraction)
  }

  /**
   * Generates: `this.getSpecificationContext().getMockController().addInteraction(mockInteraction)`
   */
  private fun addMockInteraction(
      mockInteraction: IrCall
  ): IrExpression {
    //val specCtx = this.getSpecificationContext()
    val specCtx = irFactory.call(
      SpockkConstants.SPECIFICATION_CLASS_ID, "getSpecificationContext",
      irFactory.getThisByFuncParameter(spec.defaultType, declaringFunction.parameters[0])
    )
    //val mockController = specCtx.getMockController()
    val mockController = irFactory.call(SPEC_CONTEXT_CLASS, "getMockController", specCtx)
    //val mockController.add = mockController.addInteraction(mockInteraction)
    return irFactory.call(MOCK_CONTROLLER_CLASS, "addInteraction", mockController, mockInteraction)
  }
}
