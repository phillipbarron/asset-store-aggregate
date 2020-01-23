@Status
Feature: ELB Health Check

  Scenario: Status requested
    When I request the application status
    Then a healthy status is returned

  Scenario: Root requested
    When I request the application root
    Then a healthy status is returned

