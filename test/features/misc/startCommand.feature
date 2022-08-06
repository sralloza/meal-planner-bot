@command.start
@startCommand
Feature: Start command

    As a user
    I want to start a conversation with the bot


    Scenario: Send start command to the bot
        When I send the message "/start" to the bot
        Then the bot returns the menu
