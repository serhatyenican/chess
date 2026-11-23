package org.srt.chess;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

/**
 * Enforces ADR 0009: the engine is pure Java, with no framework, persistence or web code.
 * ArchUnit reads the compiled classes, so this is plain JUnit: no Spring context, no database.
 */
@AnalyzeClasses(packages = "org.srt.chess", importOptions = ImportOption.DoNotIncludeTests.class)
class EngineArchitectureTests {

    @ArchTest
    static final ArchRule engineDoesNotDependOnFrameworks = noClasses()
            .that().resideInAPackage("org.srt.chess.engine..")
            .should().dependOnClassesThat().resideInAnyPackage(
                    "org.springframework..",
                    "jakarta.persistence..",
                    "org.hibernate..",
                    "jakarta.servlet..")
            .because("the engine is pure Java (ADR 0009)");
}
