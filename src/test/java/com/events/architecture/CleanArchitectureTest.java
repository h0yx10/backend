package com.events.architecture;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;
import org.springframework.stereotype.Repository;
import org.springframework.web.bind.annotation.RestController;

@AnalyzeClasses(packages = "com.events")
class CleanArchitectureTest {

    @ArchTest
    static final ArchRule domainIsIndependent = noClasses()
            .that()
            .resideInAPackage("..domain..")
            .should()
            .dependOnClassesThat()
            .resideInAnyPackage("..application..", "..infrastructure..");

    // El dominio se mantiene libre de Spring, Lombok y Bean Validation. Se acepta jakarta.persistence
    // directamente en las entidades como compromiso pragmatico: evita duplicar cada entidad en una
    // clase de dominio pura + una entidad JPA + un mapper adicional, dado el alcance del mini-proyecto.
    @ArchTest
    static final ArchRule domainDoesNotDependOnFrameworks = noClasses()
            .that()
            .resideInAPackage("..domain..")
            .should()
            .dependOnClassesThat()
            .resideInAnyPackage("org.springframework..", "jakarta.validation..", "lombok..");

    @ArchTest
    static final ArchRule applicationDoesNotDependOnInfrastructure = noClasses()
            .that()
            .resideInAPackage("..application..")
            .should()
            .dependOnClassesThat()
            .resideInAPackage("..infrastructure..");

    @ArchTest
    static final ArchRule inputAdaptersDoNotDependOnOutputAdapters = noClasses()
            .that()
            .resideInAPackage("..infrastructure.adapter.in..")
            .should()
            .dependOnClassesThat()
            .resideInAPackage("..infrastructure.adapter.out..");

    @ArchTest
    static final ArchRule outputAdaptersDoNotDependOnInputAdapters = noClasses()
            .that()
            .resideInAPackage("..infrastructure.adapter.out..")
            .should()
            .dependOnClassesThat()
            .resideInAPackage("..infrastructure.adapter.in..");

    @ArchTest
    static final ArchRule outputPortsAreInterfaces = classes()
            .that()
            .resideInAPackage("..application.port.out..")
            .should()
            .beInterfaces();

    @ArchTest
    static final ArchRule inputPortsAreInterfaces = classes()
            .that()
            .resideInAPackage("..application.port.in..")
            .and()
            .areTopLevelClasses()
            .and()
            .doNotHaveSimpleName("NuevaSubtareaData")
            .and()
            .doNotHaveSimpleName("TodayGroups")
            .and()
            .doNotHaveSimpleName("OverloadCheckResult")
            .and()
            .doNotHaveSimpleName("EventoProgress")
            .should()
            .beInterfaces();

    @ArchTest
    static final ArchRule inputAdaptersDependOnPortsInsteadOfUseCaseImplementations = noClasses()
            .that()
            .resideInAPackage("..infrastructure.adapter.in..")
            .should()
            .dependOnClassesThat()
            .resideInAPackage("..application.usecase..");

    @ArchTest
    static final ArchRule repositoryAdaptersResideInPersistencePackage = classes()
            .that()
            .areAnnotatedWith(Repository.class)
            .should()
            .resideInAPackage("..infrastructure.adapter.out.persistence..");

    @ArchTest
    static final ArchRule restControllersBelongToInputAdapters = classes()
            .that()
            .areAnnotatedWith(RestController.class)
            .should()
            .resideInAPackage("..infrastructure.adapter.in.rest.controller..");
}
