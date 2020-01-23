@RetrieveSnapshot
Feature: Retrieve a snapshot

    Scenario: Retrieve an existing snapshot by its asset id and event id
      Given A snapshot exists for assetId foo and eventId bar
      When I retrieve an existing snapshot with assetId foo and eventId bar
      Then I am returned that snapshot as JSON

    Scenario: Returns a 404 when no snapshot was found
      When I request a snapshot that does not exist
      Then I receive a JSON response indicating that the snapshot does not exist
      And The status code is 404

    Scenario: Returns a 500 when the snapshot cannot be read
      When I request a snapshot that causes an exception
      Then I receive a JSON response indicating that there has been an exception
      And The status code is 500

    Scenario: Returns a 403 when the snapshot cannot be accessed
      When I request a snapshot where its access is forbidden
      Then I receive a JSON response indicating that there has been an exception
      And The status code is 403
