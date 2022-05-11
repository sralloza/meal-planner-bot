# Meal Planner Bot

Telegram bot which uses the [Meal Planner API](https://github.com/sralloza/meal-planner).

## Deploy

Docker images are provided in [dockerhub](https://hub.docker.com/r/sralloza/meal-planner-bot).

## Configuration

Configuration is done by setting environment variables.

### Required
- ***TELEGRAM_BOT_TOKEN***: telegram bot token.
- ***TELEGRAM_BOT_USERNAME***: telegram bot username.
- ***TELEGRAM_CREATOR_ID***: telegram userID of the bot creator.

### Optional
- ***TELEGRAM_STYLE_FROZEN_TOKENS_LEFT***: markdown token to start frozen meals. Defaults to `__*_`, meaning starting bold, italics and underlined text.
- ***TELEGRAM_STYLE_FROZEN_TOKENS_RIGHT***: markdown token to close frozen meals. Defaults to `_*__`, meaning closing bold, italics and underlined text. 
- ***API_HTTP2***: Enable HTTP/2. defaults to `true`.

