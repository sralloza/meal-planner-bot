from json import loads

from behave import *
from behave.api.async_step import async_run_until_complete
from hamcrest import assert_that, equal_to

from common.telegram import parse_keyboard


@step('I send the message "{msg}" to the bot')
@async_run_until_complete
async def step_impl(context, msg):
    async with context.client.conversation(context.bot_username, timeout=5) as conv:
        await conv.send_message(msg)
        context.res = await conv.get_response()


@step('the bot sends the message "{msg}" with the keyboard')
def step_impl(context, msg):
    expected = loads(context.text)
    keyboard = context.res.reply_markup
    actual = parse_keyboard(keyboard)

    assert_that(msg, equal_to(context.res.raw_text))
    assert_that(expected, equal_to(actual))
