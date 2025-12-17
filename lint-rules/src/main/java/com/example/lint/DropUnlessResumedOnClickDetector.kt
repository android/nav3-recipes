package com.example.lint

import com.android.tools.lint.detector.api.Category
import com.android.tools.lint.detector.api.Detector
import com.android.tools.lint.detector.api.Issue
import com.android.tools.lint.detector.api.JavaContext
import com.android.tools.lint.detector.api.Severity
import com.android.tools.lint.detector.api.Scope
import com.android.tools.lint.detector.api.Implementation
import org.jetbrains.uast.UCallExpression
import org.jetbrains.uast.ULambdaExpression
import org.jetbrains.uast.visitor.AbstractUastVisitor

class DropUnlessResumedOnClickDetector : Detector(), Detector.UastScanner {
    companion object {
        val ISSUE: Issue = Issue.create(
            id = "DropUnlessResumedOnClick",
            briefDescription = "onClick must use dropUnlessResumed",
            explanation = "All onClick handlers must wrap their logic in dropUnlessResumed to avoid lifecycle issues.",
            category = Category.CORRECTNESS,
            priority = 6,
            severity = Severity.ERROR,
            implementation = Implementation(
                DropUnlessResumedOnClickDetector::class.java,
                Scope.JAVA_FILE_SCOPE
            )
        )
    }

    override fun getApplicableUastTypes() = listOf(UCallExpression::class.java)

    override fun getApplicableMethodNames() = listOf("Button", "clickable", "IconButton")

    override fun visitMethodCall(context: JavaContext, node: UCallExpression, method: com.intellij.psi.PsiMethod) {
        val methodName = node.methodName ?: return
        if (methodName == "Button" || methodName == "clickable" || methodName == "IconButton") {
            // Check all arguments for a lambda (named or positional)
            val lambdaArgs = node.valueArguments.filterIsInstance<ULambdaExpression>()
            if (lambdaArgs.isEmpty()) {
                // Also check for named arguments
                node.valueArguments.forEach { arg ->
                    if (arg is ULambdaExpression) {
                        lambdaArgs.plus(arg)
                    }
                }
            }
            for (lambda in lambdaArgs) {
                if (!lambdaCallsDropUnlessResumed(lambda)) {
                    context.report(
                        ISSUE,
                        node,
                        context.getLocation(node),
                        "onClick handler must use dropUnlessResumed"
                    )
                }
            }
        }
    }

    private fun lambdaCallsDropUnlessResumed(lambda: ULambdaExpression): Boolean {
        var found = false
        lambda.accept(object : AbstractUastVisitor() {
            override fun visitCallExpression(node: UCallExpression): Boolean {
                if (node.methodName == "dropUnlessResumed") {
                    found = true
                    return true
                }
                return super.visitCallExpression(node)
            }
        })
        return found
    }
}
