Feature: Start command

    As a user
    I want to start a conversation with the bot

    @wip
    Scenario: Send start command to the bot
        When I send the message "/start" to the bot
        Then The bot sends the message "API connected" with the keyboard
            """
            [
                [
                    "Skip",
                    "Tasks",
                    "Complete task"
                ],
                [
                    "Unskip",
                    "Tickets",
                    "Transfer"
                ]
            ]
            """
