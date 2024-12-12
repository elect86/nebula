rootProject.name = "nebula"

plugins {
    id("com.gradle.develocity") version "3.18.2"
}

develocity.buildScan {
    termsOfUseUrl = "https://gradle.com/help/legal-terms-of-use"
    termsOfUseAgree = "yes"
    publishing.onlyIf { false }
}

include(
    "dependencies-comparison",
    "test",
    "plugins:dependency-lock",
)

enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")