package backend.academy.linktracker.bot.enums;

public enum BotMenuCommandType {
    START("start", "Запуск бота"),
    HELP("help", "Список доступных команд"),
    TRACK("track", "Начать отслеживание ссылки"),
    UNTRACK("untrack", "Прекратить отслеживание ссылки"),
    LIST("list", "Список отслеживаемых ссылок");

    private final String botMenuCommand;
    private final String descriptionBotCommand;

    BotMenuCommandType(String botMenuCommand, String descriptionBotCommand) {
        this.botMenuCommand = botMenuCommand;
        this.descriptionBotCommand = descriptionBotCommand;
    }

    public String getBotMenuCommand() {
        return botMenuCommand;
    }

    public String getDescriptionBotCommand() {
        return descriptionBotCommand;
    }
}
