/*
 * Copyright 2026 Bloomberg Finance L.P.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.bloomberg.selekt.android.lint

import com.android.tools.lint.detector.api.AnnotationInfo
import com.android.tools.lint.detector.api.AnnotationUsageInfo
import com.android.tools.lint.detector.api.AnnotationUsageType
import com.android.tools.lint.detector.api.Category
import com.android.tools.lint.detector.api.ConstantEvaluator
import com.android.tools.lint.detector.api.Detector
import com.android.tools.lint.detector.api.Implementation
import com.android.tools.lint.detector.api.Issue
import com.android.tools.lint.detector.api.JavaContext
import com.android.tools.lint.detector.api.Scope
import com.android.tools.lint.detector.api.Severity
import com.android.tools.lint.detector.api.SourceCodeScanner
import com.intellij.psi.PsiModifierListOwner
import org.jetbrains.uast.UCallExpression
import org.jetbrains.uast.UElement
import org.jetbrains.uast.UExpression
import org.jetbrains.uast.ULiteralExpression
import org.jetbrains.uast.UReferenceExpression
import org.jetbrains.uast.skipParenthesizedExprDown

internal class TrustedSqlDetector : Detector(), SourceCodeScanner {
    override fun applicableAnnotations() = listOf(TRUSTED_SQL_ANNOTATION)

    override fun isApplicableAnnotationUsage(type: AnnotationUsageType) =
        type == AnnotationUsageType.METHOD_CALL_PARAMETER

    override fun visitAnnotationUsage(
        context: JavaContext,
        element: UElement,
        annotationInfo: AnnotationInfo,
        usageInfo: AnnotationUsageInfo
    ) {
        val expression = element as? UExpression ?: return
        if (isTrusted(context, expression)) {
            return
        }
        context.report(
            ISSUE,
            expression,
            context.getLocation(expression),
            "Build SQL structure from trusted constants and bind external values instead."
        )
    }

    private fun isTrusted(context: JavaContext, expression: UExpression): Boolean {
        val unwrapped = expression.skipParenthesizedExprDown()
        val constant = ConstantEvaluator().allowFieldInitializers().evaluate(unwrapped)
        return when {
            unwrapped is ULiteralExpression && unwrapped.value == null -> true
            constant is String || constant is Array<*> && constant.all { it is String } -> true
            isExplicitlyTrusted(context, unwrapped) -> true
            else -> unwrapped is UCallExpression &&
                unwrapped.methodName in ARRAY_FACTORIES &&
                unwrapped.valueArguments.all { isTrusted(context, it) }
        }
    }

    private fun isExplicitlyTrusted(context: JavaContext, expression: UExpression): Boolean {
        val referenced = when (expression) {
            is UReferenceExpression -> expression.resolve()
            is UCallExpression -> expression.resolve()
            else -> null
        } as? PsiModifierListOwner ?: return false
        return context.evaluator.getAnnotation(referenced, TRUSTED_SQL_ANNOTATION) != null
    }

    companion object {
        private const val TRUSTED_SQL_ANNOTATION = "com.bloomberg.selekt.annotations.TrustedSql"
        private val ARRAY_FACTORIES = setOf("arrayOf", "emptyArray")

        val ISSUE: Issue = Issue.create(
            id = "UnsafeTrustedSql",
            briefDescription = "Dynamic input used as SQL structure",
            explanation = """
                Parameters annotated with `TrustedSql` are inserted into SQL statements as syntax rather than bound as
                values. Dynamically assembled input can therefore change the meaning of the statement. Keep SQL structure
                application-controlled, use placeholders for external values, and pass those values through bind arguments.
            """.trimIndent(),
            category = Category.SECURITY,
            priority = 6,
            severity = Severity.WARNING,
            implementation = Implementation(TrustedSqlDetector::class.java, Scope.JAVA_FILE_SCOPE)
        )
    }
}
