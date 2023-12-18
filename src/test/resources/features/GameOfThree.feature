Feature: Game of three creation

  Background:
  This is a simulation of the same example provided in the coding challenge description
  Game between 2 parties: "player 1" & "player 2"


  Scenario: player 1 starts the game with autoplay is false!
    Given Player 2 is mocked
    When player 1 starts the game with id 1 and number 56 and autoPlay "false"
    Then player 1 receives status code of 202
    And the game data is saved locally
    When player 2 update the number to 19 and send turn to player 1
    Then The game data updates number to 19
    When player 1 take turn to do a "MINUS_ONE" operation on the number
    Then The game data number changes to 6
    When player 2 update the number to 2 and send turn to player 1
    Then The game data updates number to 2
    When player 1 take turn to do a "ONE" operation on the number
    Then The game data number changes to 1
    And The game is finished and current player wins
    And Report player 2 as a loser

  Scenario: The a player starts the game with autoplay is true!
    Given Player 2 is mocked
    When player 1 starts the game with id 1 and number 56 and autoPlay "true"
    Then player 1 receives status code of 202
    And the game data is saved locally
    When player 2 update the number to 19 and send turn to player 1
    Then player 1 take turn automatically


  Scenario:  This is a simulation of the same example provided in the coding challenge description
  Game between 2 parties: "player 1 " & "player 2"
    Given Player 1 is mocked
    When player 1 starts the game with id "1" and number 56 and autoPlay "false" and communicate it to player 2
    Then player 2 receives and handles game and the game data is saved locally
    When player 2 update number locally by applying operation "ONE"
    Then The game data number changes to 19
    When player 1 update the number to 6 and send turn to player 2
    Then The game data number changes to 6
    When player 2 update number locally by applying operation "ZERO"
    Then The game data number changes to 2
    When player 1 update the number to 1 and send turn to player 2
    Then The game data number changes to 1
    And Player 2 receive game result as a loser
