package com.example.lint

import com.android.tools.lint.checks.infrastructure.LintDetectorTest
import com.android.tools.lint.detector.api.Issue
import org.junit.Test

class DropUnlessResumedOnClickDetectorTest : LintDetectorTest() {
    override fun getDetector() = DropUnlessResumedOnClickDetector()
    override fun getIssues(): List<Issue> = listOf(DropUnlessResumedOnClickDetector.ISSUE)

    private val buttonStub = kotlin(
        """
        package androidx.compose.material3
        import androidx.compose.runtime.Composable
        @Composable
        fun Button(onClick: () -> Unit, content: @Composable () -> Unit = {}) {}
        """
    )
    private val composableStub = kotlin(
        """
        package androidx.compose.runtime
        annotation class Composable
        """
    )

    @Test
    fun testMissingDropUnlessResumed() {
        val result = lint().files(
            buttonStub,
            composableStub,
            kotlin(
                """
                package test
                import androidx.compose.material3.Button
                import androidx.compose.runtime.Composable
                @Composable
                fun MyButton() {
                    Button(onClick = { println("Clicked") })
                }
                """.trimIndent()
            )
        ).run()
        result.expect(
            """
src/test/test.kt:6: Error: onClick handler must use dropUnlessResumed [DropUnlessResumedOnClick]
    Button(onClick = { println("Clicked") })
    ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
1 errors, 0 warnings
"""
        )
    }

    @Test
    fun testWithDropUnlessResumed() {
        val result = lint().files(
            buttonStub,
            composableStub,
            kotlin(
                """
                package test
                import androidx.compose.material3.Button
                import androidx.compose.runtime.Composable
                @Composable
                fun MyButton() {
                    Button(onClick = { dropUnlessResumed { println("Clicked") } })
                }
                fun dropUnlessResumed(block: () -> Unit) = block()
                """.trimIndent()
            )
        ).run()
        result.expectClean()
    }
}
