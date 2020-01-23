@RetrieveHistory
Feature: Retrieve history of an asset

  Background: Ensure database has been set up
    Given the database is empty
    And the relevant tables exist
    And asset events exist

  Scenario: Retrieve the entire history of an existing asset which is paginated into 1 page
    Given A history of events exists for an asset
    When I retrieve the history of the asset with the default values for page and size
    Then I am returned the history as a list of events in a single page

  Scenario: Retrieve page 1 of an asset's history which is paginated into 4 pages
    Given A history of events exists for an asset
    When I retrieve the history of the asset with page 1 and size 2
    Then I am returned page 1 as a list of events with size 2 and with a next page value

  Scenario: Retrieve page 2 of an asset's history which is paginated into 4 pages
    Given A history of events exists for an asset
    When I retrieve the history of the asset with page 2 and size 2
    Then I am returned page 2 as a list of events with size 2 and with a next page value

  Scenario: Retrieve page 4 of an asset's history which is paginated into 4 pages
    Given A history of events exists for an asset
    When I retrieve the history of the asset with page 4 and size 2
   Then I am returned page 4 as a list of events with size 2 and no next page value

  Scenario: Retrieve the history of an asset that has no history
    Given an asset has no history
    When I retrieve the history of the asset with the default values for page and size
    Then I am told that the history for the asset does not exist

  Scenario: Provide invalid request parameters
    When I retrieve the history of an asset providing an invalid page
    Then I am told the request parameters are invalid

  Scenario: Attempt to retrieve the history of an asset when the database model has changed
    Given the database model has changed
    And a history event exists that corresponds to the new model
    When I retrieve the history of the asset with the default values for page and size
    Then I am told the model is incorrect
