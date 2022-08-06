@unexpectedText
Feature: Unexpected text

    As a user
    I want to receive the menu if I send an unexpected text to the bot


    Scenario Outline: Send an unexpected text to the bot
        When I send the message "<text>" to the bot
        Then the bot returns the menu with text "Undefined command"

        Examples: text = <text>
            | text     |
            | Hi       |
            | _        |
