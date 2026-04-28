package backend.academy.linktracker.bot.enums;

public enum BotCommandType {
    START(
            "/start",
            "Добро пожаловать! Я LinkTracker — бот для отслеживания изменений на GitHub и StackOverflow.\n"
                    + "Используйте /help, чтобы посмотреть доступные команды."),
    HELP(
            "/help",
            "Список доступных команд:\n"
                    + "/start — начать работу с ботом\n"
                    + "/help — посмотреть список доступных команд\n"
                    + "/track — начать отслеживание ссылки\n"
                    + "/untrack — прекратить отслеживание ссылки\n"
                    + "/list — список отслеживаемых ссылок"),
    TRACK("/track", "Введите ссылку для отслеживания:"),
    UNTRACK("/untrack", "Введите ссылку, которую хотите удалить из отслеживания:"),
    LIST("/list", ""),
    CANCEL("/cancel", "Действие отменено."),
    UNKNOWN("", "Неизвестная команда. Воспользуйтесь /help, чтобы посмотреть список доступных команд.");

    private final String command;
    private final String message;

    BotCommandType(String command, String message) {
        this.command = command;
        this.message = message;
    }

    public static BotCommandType fromString(String text) {
        if (text == null) {
            return UNKNOWN;
        }
        String cmd = text.trim().split("\\s+")[0].toLowerCase();
        for (BotCommandType type : values()) {
            if (type.command.equals(cmd)) {
                return type;
            }
        }
        return UNKNOWN;
    }

    public String getCommand() {
        return command;
    }

    public String getMessage() {
        return message;
    }
}
