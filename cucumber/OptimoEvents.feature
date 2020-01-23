@OptimoEvents
Feature: Consuming Optimo Events

  Background: Ensure database has been set up
    Given the database is empty
    And the relevant tables exist

  Scenario: Consuming the first saved message adds a created event and snapshot
    When an article saved message is received
    Then a created event is added to the article's history
    And a snapshot is saved
    And a history updated notification is sent

  Scenario: Consuming a second save message for the same article adds a saved event
    When an article saved message is received
    Then a created event is added to the article's history
    When a second article saved message is received
    Then a saved event is added to the article's history

  Scenario: Consuming a published message
    When an article published message is received
    Then a published event is added to the article's history

  Scenario: Consuming a minor change message
    Given a published event exists for an article
    When an article minor change message is received
    Then a published minor change event is added to the article's history

  Scenario: Consuming a news update message
    Given a published event exists for an article
    When a news update is published
    Then a news update published event is added to the article's history

  Scenario: Duplicate events are ignored
    When two duplicate article published messages are received
    Then a published event is added to the article's history

  Scenario: Event processing continues despite failures
    When an invalid message is received
    And an article saved message is received
    Then a created event is added to the article's history
