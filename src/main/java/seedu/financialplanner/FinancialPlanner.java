package seedu.financialplanner;

import seedu.financialplanner.commands.Command;
import seedu.financialplanner.commands.Exit;
import seedu.financialplanner.exceptions.FinancialPlannerException;
import seedu.financialplanner.investments.WatchList;
import seedu.financialplanner.list.FinancialList;
import seedu.financialplanner.storage.Storage;
import seedu.financialplanner.utils.Parser;
import seedu.financialplanner.utils.Ui;

public class FinancialPlanner {
    private Ui ui;
    private WatchList watchList;
    private FinancialList financialList;
    private Storage storage;

    public FinancialPlanner() {
        ui = new Ui();
        financialList = new FinancialList();
        watchList = new WatchList();
        storage = new Storage();
        storage.load(financialList);
    }

    public void run() {
        ui.welcomeMessage();
        String input;
        Command command = null;

        while (!(command instanceof Exit)) {
            input = ui.input();
            command = Parser.parse(input);
            command.execute(ui, financialList, watchList);
        }

        save();
        ui.exitMessage();
    }

    public void save() {
        try {
            storage.save(financialList);
        } catch (FinancialPlannerException e) {
            ui.showMessage(e.getMessage());
        }
    }

    public static void main(String[] args) {
        new FinancialPlanner().run();
    }
}