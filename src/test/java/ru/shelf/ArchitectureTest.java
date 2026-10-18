package ru.shelf;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

@AnalyzeClasses(packages = "ru.shelf", importOptions = ImportOption.DoNotIncludeTests.class)
class ArchitectureTest {
    @ArchTest
    static final ArchRule DOMAIN_IS_INDEPENDENT =
            classes()
                    .that()
                    .resideInAPackage("..domain..")
                    .should()
                    .onlyDependOnClassesThat()
                    .resideInAnyPackage("java..", "jakarta.persistence..", "..domain..");

    @ArchTest
    static final ArchRule REPOSITORY_DEPENDS_ON_DOMAIN =
            classes()
                    .that()
                    .resideInAPackage("..repository..")
                    .should()
                    .onlyDependOnClassesThat()
                    .resideInAnyPackage(
                            "java..", "org.springframework.data..", "..domain..", "..repository..");

    @ArchTest
    static final ArchRule SERVICE_DOES_NOT_DEPEND_ON_WEB =
            noClasses()
                    .that()
                    .resideInAPackage("..service..")
                    .should()
                    .dependOnClassesThat()
                    .resideInAnyPackage(
                            "..web..",
                            "..config..",
                            "jakarta.servlet..",
                            "org.springframework.http..",
                            "org.springframework.web..",
                            "org.springframework.security..",
                            "jakarta.persistence..");

    @ArchTest
    static final ArchRule CONTROLLERS_DO_NOT_USE_REPOSITORIES =
            noClasses()
                    .that()
                    .resideInAPackage("..web..")
                    .should()
                    .dependOnClassesThat()
                    .resideInAPackage("..repository..");

    @ArchTest
    static final ArchRule CONTROLLERS_DO_NOT_USE_ENTITIES =
            noClasses()
                    .that()
                    .resideInAPackage("..web..")
                    .should()
                    .dependOnClassesThat()
                    .haveFullyQualifiedName("ru.shelf.domain.ShelfItem");

    @ArchTest
    static final ArchRule SERVICE_DTOS_ARE_INDEPENDENT =
            classes()
                    .that()
                    .resideInAPackage("..service.dto..")
                    .should()
                    .onlyDependOnClassesThat()
                    .resideInAnyPackage(
                            "java..", "jakarta.validation..", "..service.dto..", "..domain..");

    @ArchTest
    static final ArchRule SERVICE_DTOS_DO_NOT_EXPOSE_ENTITIES =
            noClasses()
                    .that()
                    .resideInAPackage("..service.dto..")
                    .should()
                    .dependOnClassesThat()
                    .haveFullyQualifiedName("ru.shelf.domain.ShelfItem");
}
