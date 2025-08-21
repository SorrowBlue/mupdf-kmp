plugins {
    alias(libs.plugins.androidApplication) apply false
    alias(libs.plugins.androidLibrary) apply false
    alias(libs.plugins.buildkonfig) apply false
    alias(libs.plugins.detekt) apply false
    alias(libs.plugins.composeHotReload) apply false
    alias(libs.plugins.composeMultiplatform) apply false
    alias(libs.plugins.composeCompiler) apply false
    alias(libs.plugins.kotlinMultiplatform) apply false
    alias(libs.plugins.versionCatalogLinter)
}
tasks.register("reportMerge", io.gitlab.arturbosch.detekt.report.ReportMergeTask::class) {
    group = LifecycleBasePlugin.VERIFICATION_GROUP
    output.set(rootProject.layout.buildDirectory.file("reports/detekt/merge.sarif"))
}
