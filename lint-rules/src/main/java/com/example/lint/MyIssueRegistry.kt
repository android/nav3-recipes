package com.example.lint

import com.android.tools.lint.client.api.IssueRegistry
import com.android.tools.lint.client.api.Vendor
import com.android.tools.lint.detector.api.Issue

class MyIssueRegistry : IssueRegistry() {
    override val issues: List<Issue>
        get() = listOf(DropUnlessResumedOnClickDetector.ISSUE)
    override val vendor = Vendor(
        vendorName = "Nav3 Recipes",
    )
}
