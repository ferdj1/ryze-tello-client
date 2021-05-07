package my.project.fer.ryzetello.server.mock;

import my.project.fer.ryzetello.constants.ReadCommands;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class RyzeTelloCommunicatorServiceImpl implements RyzeTelloCommunicatorService {

    private RyzeTelloCommandService commandService;
    private RyzeTelloReadService readService;

    public RyzeTelloCommunicatorServiceImpl() {
        this.commandService = new RyzeTelloCommandServiceImpl();
        this.readService = new RyzeTelloReadServiceImpl();
    }

    public String execute(String command) {
        final String[] commandSplit = command.split("\\s+");
        final String commandName = commandSplit[0].replaceAll("\\?", "");

        if (ReadCommands.valuesList().contains(commandName)) {
            try {
                Method method = readService.getClass().getMethod(commandName);
                String res = (String) method.invoke(readService);

                return String.format("Executed: %s, Status: OK, Response: %s", command, res);
            } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
                e.printStackTrace();
            }
        } else {
            // TODO

            // Temporary just return OK
            return String.format("Executed: %s, Status: OK", command);
        }

        // TODO
        return "ERROR";
    }

}
